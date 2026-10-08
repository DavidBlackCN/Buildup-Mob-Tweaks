package com.davidblackcn.buildupmobtweaks;

import com.davidblackcn.buildupmobtweaks.combat.SkeletonCombat;
import com.davidblackcn.buildupmobtweaks.config.BuildupConfig;
import com.davidblackcn.buildupmobtweaks.feature.*;
import java.util.*;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.*;

public class SkeletonTests {
    private static final String ARENA = "buildupmobtweaks:arena";
    private static BuildupConfig config() {
        var config = new BuildupConfig();
        config.hostile.skeleton.skeletonChance.accept(1000);
        config.hostile.skeleton.strayChance.accept(1000);
        config.hostile.skeleton.boggedChance.accept(1000);
        return config;
    }
    private static SkeletonCombat service(BuildupConfig config) { return new SkeletonCombat(new FeatureRegistry(config)); }
    private static AbstractSkeleton skeleton(GameTestHelper helper, EntityType<? extends AbstractSkeleton> type, int x, int z) {
        var mob = type.create(helper.getLevel(), EntitySpawnReason.COMMAND);
        mob.snapTo(helper.absolutePos(new BlockPos(x, 2, z)), 0, 0);
        mob.setNoAi(true); mob.setPermanentlyInvulnerable(true); mob.setPersistenceRequired();
        mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
        return mob;
    }
    private static Mob target(GameTestHelper helper, int x, int z) {
        var target = EntityTypes.IRON_GOLEM.create(helper.getLevel(), EntitySpawnReason.COMMAND);
        target.snapTo(helper.absolutePos(new BlockPos(x, 2, z)), 0, 0);
        target.setNoAi(true);
        helper.getLevel().addFreshEntity(target);
        return target;
    }
    private static void floor(GameTestHelper helper) {
        for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) {
            helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
            helper.setBlock(new BlockPos(x, 5, z), Blocks.STONE);
        }
    }
    private static AbstractSkeleton reload(GameTestHelper helper, AbstractSkeleton mob) {
        var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, helper.getLevel().registryAccess());
        mob.saveWithoutId(output); mob.discard();
        var input = TagValueInput.create(ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), output.buildResult());
        return (AbstractSkeleton) EntityType.create(mob.getType(), input, helper.getLevel(), EntitySpawnReason.LOAD).orElseThrow();
    }
    @GameTest
    public void birthRecordCooldownAndUnknownDataSurviveSave(GameTestHelper h) {
        var cfg = config(); var combat = service(cfg);
        for (var type : List.of(EntityTypes.SKELETON, EntityTypes.STRAY, EntityTypes.BOGGED)) {
            var mob = skeleton(h, type, 2, 2); combat.initialize(mob);
            h.assertTrue(combat.active(mob), "1000/1000 produces entity-specific trait");
            var saved = mob.getAttached(SkeletonCombat.DATA).copy(); saved.putLong("next_special_at", 87654);
            mob.setAttached(SkeletonCombat.DATA, saved);
            var loaded = reload(h, mob); combat.initialize(loaded);
            h.assertValueEqual(loaded.getAttached(SkeletonCombat.DATA), saved, "Trait and cooldown survive real serialization");
            h.assertTrue(combat.active(loaded), "Known trait activates after reload"); loaded.discard();
        }
        var mob = skeleton(h, EntityTypes.SKELETON, 2, 2);
        var future = new CompoundTag(); future.putInt("version", 99); future.putString("future", "keep");
        mob.setAttached(SkeletonCombat.DATA, future);
        var loaded = reload(h, mob); combat.initialize(loaded);
        h.assertValueEqual(loaded.getAttached(SkeletonCombat.DATA), future, "Unknown data preserved, never rerolled");
        h.assertFalse(combat.active(loaded), "Unknown version inactive"); loaded.discard(); h.succeed();
    }
    @GameTest
    public void noBackfillAndConversionKeepOriginalTrait(GameTestHelper h) {
        var cfg = config(); cfg.hostile.skeleton.skeletonChance.accept(0); var combat = service(cfg);
        var mob = skeleton(h, EntityTypes.SKELETON, 2, 2); combat.initialize(mob);
        cfg.hostile.skeleton.skeletonChance.accept(1000); combat.initialize(mob);
        h.assertFalse(combat.active(mob), "Changing probability does not reroll existing entity");
        mob.discard();
        for (var reason : List.of(EntitySpawnReason.LOAD, EntitySpawnReason.CONVERSION, EntitySpawnReason.DIMENSION_TRAVEL)) {
            var old = EntityTypes.STRAY.create(h.getLevel(), reason); combat.initialize(old);
            h.assertFalse(combat.active(old), "Old or ambiguous source receives no trait"); old.discard();
        }
        mob = skeleton(h, EntityTypes.SKELETON, 2, 2); combat.initialize(mob);
        h.getLevel().addFreshEntity(mob); var saved = mob.getAttached(SkeletonCombat.DATA).copy();
        var stray = mob.convertTo(EntityTypes.STRAY, ConversionParams.single(mob, true, true), ignored -> {});
        h.assertValueEqual(stray.getAttached(SkeletonCombat.DATA), saved, "Conversion inherits source without another lottery");
        h.assertFalse(combat.active(stray), "Skeleton-specific trait cannot turn into stray ability"); stray.discard();
        h.assertFalse(SkeletonCombat.eligible(EntityTypes.WITHER_SKELETON.create(h.getLevel(), EntitySpawnReason.COMMAND)), "Wither skeleton excluded"); h.succeed();
    }
    @GameTest
    public void sevenIndependentGatesAndMasterSwitch(GameTestHelper h) {
        var cfg = config(); var registry = new FeatureRegistry(cfg); var s = cfg.hostile.skeleton;
        var switches = List.of(s.safeStrafing, s.targetValidation, s.weaponSwitching, s.bowCompatibility,
                s.skeletonSniping, s.strayJumpShot, s.boggedSporeRetreat);
        var ids = List.of(FeatureId.SKELETON_SAFE_STRAFING, FeatureId.SKELETON_TARGET_VALIDATION, FeatureId.SKELETON_WEAPON_SWITCHING,
                FeatureId.SKELETON_BOW_COMPATIBILITY, FeatureId.SKELETON_SNIPING, FeatureId.STRAY_JUMP_SHOT, FeatureId.BOGGED_SPORE_RETREAT);
        for (int i = 0; i < ids.size(); i++) {
            switches.get(i).accept(false);
            for (int j = 0; j < ids.size(); j++) h.assertValueEqual(registry.isEnabled(ids.get(j)), i != j, "Only selected gate disabled");
            BuildupMobTweaks.LOGGER.info("S2-A independent gate {}=false; other six=true", ids.get(i));
            switches.get(i).accept(true);
        }
        cfg.traits.enabled.accept(false);
        for (int i = 0; i < ids.size(); i++) h.assertValueEqual(registry.isEnabled(ids.get(i)), i < 4, "Traits gates only advanced behavior");
        cfg.general.enabled.accept(false);
        for (var id : ids) h.assertFalse(registry.isEnabled(id), "Master disable"); h.succeed();
    }
    @GameTest(structure = ARENA, maxTicks = 50)
    public void switchHasHysteresisPreservesComponentsAndDrops(GameTestHelper h) {
        floor(h); var cfg = config(); var combat = service(cfg);
        var mob = skeleton(h, EntityTypes.SKELETON, 3, 3); var target = target(h, 5, 3); mob.setTarget(target);
        var bow = mob.getMainHandItem(); bow.setDamageValue(17); bow.set(DataComponents.CUSTOM_NAME, Component.literal("owned bow"));
        var sword = new ItemStack(Items.IRON_SWORD); sword.setDamageValue(9);
        mob.setItemSlot(EquipmentSlot.OFFHAND, sword);
        mob.setDropChance(EquipmentSlot.MAINHAND, .21f); mob.setDropChance(EquipmentSlot.OFFHAND, .72f);
        cfg.hostile.skeleton.weaponSwitching.accept(false); combat.switchWeapon(mob);
        h.assertTrue(mob.getMainHandItem() == bow, "Disabled switching leaves both hands intact");
        cfg.hostile.skeleton.weaponSwitching.accept(true); combat.switchWeapon(mob);
        h.assertTrue(mob.getMainHandItem() == sword && mob.getOffhandItem() == bow, "Same owned stacks swapped");
        h.assertFalse(combat.usesBow(mob), "Offhand bow no longer blocks melee");
        h.assertValueEqual(mob.getDropChances().byEquipment(EquipmentSlot.OFFHAND), .21f, "Bow drop chance follows item");
        target.snapTo(h.absolutePos(new BlockPos(11, 2, 3)), 0, 0); combat.switchWeapon(mob);
        h.assertTrue(mob.getMainHandItem() == sword, "Cooldown prevents immediate flip");
        h.runAfterDelay(21, () -> {
            combat.switchWeapon(mob);
            h.assertTrue(mob.getMainHandItem() == bow && mob.getOffhandItem() == sword, "Far target restores bow after cooldown");
            h.assertValueEqual(bow.getDamageValue(), 17, "Components remain intact");
            h.assertValueEqual(mob.getDropChances().byEquipment(EquipmentSlot.OFFHAND), .72f, "Sword drop chance follows item");
            var loaded = reload(h, mob);
            h.assertTrue(ItemStack.matches(loaded.getMainHandItem(), bow) && ItemStack.matches(loaded.getOffhandItem(), sword), "No lost or duplicated equipment on reload");
            loaded.discard(); target.discard(); h.succeed();
        });
    }    @GameTest(structure = ARENA)
    public void safeStrafingRejectsCliffLavaAndWall(GameTestHelper h) {
        floor(h); var cfg = config(); var combat = service(cfg); var mob = skeleton(h, EntityTypes.SKELETON, 3, 3); mob.setOnGround(true);
        h.assertTrue(SkeletonCombat.safeStep(mob, 0, 1), "Flat open path accepted");
        for (int x = 2; x <= 4; x++) {
            h.setBlock(new BlockPos(x, 1, 4), Blocks.AIR);
            h.setBlock(new BlockPos(x, 1, 5), Blocks.AIR);
        }
        var blocked = combat.strafe(mob, .5f, 0);
        h.assertTrue(blocked[0] == 0 && blocked[1] == 0, "Do not strafe off edge");
        cfg.hostile.skeleton.safeStrafing.accept(false);
        h.assertValueEqual(combat.strafe(mob, .5f, 0)[0], .5f, "Disabled feature preserves original movement");
        h.setBlock(new BlockPos(3, 1, 4), Blocks.LAVA);
        h.assertFalse(SkeletonCombat.safeStep(mob, 0, 1), "Lava rejected");
        floor(h); h.setBlock(new BlockPos(3, 2, 4), Blocks.STONE);
        h.assertFalse(SkeletonCombat.safeStep(mob, 0, 1), "Wall rejected"); mob.discard(); h.succeed();
    }
    @GameTest
    public void staleAlliedTargetIsReleasedOnlyWhenEnabled(GameTestHelper h) {
        var cfg = config(); var combat = service(cfg); var mob = skeleton(h, EntityTypes.SKELETON, 2, 2); var target = target(h, 5, 2);
        mob.setTarget(target);
        var scoreboard = h.getLevel().getScoreboard(); var team = scoreboard.addPlayerTeam("s2a_" + mob.getId());
        scoreboard.addPlayerToTeam(mob.getScoreboardName(), team); scoreboard.addPlayerToTeam(target.getScoreboardName(), team);
        cfg.hostile.skeleton.targetValidation.accept(false); combat.validateTarget(mob);
        h.assertTrue(mob.getTargetUnchecked() == target, "Disabled validation leaves original stored target");
        cfg.hostile.skeleton.targetValidation.accept(true); combat.validateTarget(mob);
        h.assertTrue(mob.getTargetUnchecked() == null, "Enabled validation clears stale allied target, without finding a replacement");
        scoreboard.removePlayerTeam(team); mob.discard(); target.discard(); h.succeed();
    }
    @GameTest
    public void bowAdapterAndUnknownItemFallback(GameTestHelper h) {
        var cfg = config(); var combat = service(cfg); var mob = skeleton(h, EntityTypes.SKELETON, 2, 2);
        mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(CombatTestItems.BOW));
        h.assertTrue(com.davidblackcn.buildupmobtweaks.equipment.EquipmentCapabilities.identify(mob.getMainHandItem()).contains(com.davidblackcn.buildupmobtweaks.equipment.EquipmentCapabilities.Ability.BOW), "Shared capability query recognizes current custom bow");
        h.assertTrue(combat.usesBow(mob) && mob.canUseNonMeleeWeapon(mob.getMainHandItem()), "Registered BowItem accepted by actual skeleton capability");
        cfg.hostile.skeleton.bowCompatibility.accept(false);
        h.assertFalse(combat.usesBow(mob), "Independent compatibility switch restores exact vanilla bow recognition");
        mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.STICK));
        h.assertFalse(combat.usesBow(mob), "Unknown item does not acquire invented shooting capability");
        h.assertTrue(mob.getGoalSelector().getAvailableGoals().stream().anyMatch(goal -> goal.getGoal() instanceof MeleeAttackGoal), "Original melee goal retained for unknown weapon");
        mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
        h.assertTrue(combat.usesBow(mob), "Vanilla bow works with adapter disabled"); mob.discard(); h.succeed();
    }
    private static void shooting(GameTestHelper h, EntityType<? extends AbstractSkeleton> type, Item bow) {
        floor(h); var mob = skeleton(h, type, 3, 3); var target = target(h, 12, 3);
        mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(bow));
        var noTrait = new CompoundTag(); noTrait.putInt("version", 1); noTrait.putString("trait", "none"); mob.setAttached(SkeletonCombat.DATA, noTrait);
        h.getLevel().addFreshEntity(mob); mob.setTarget(target); mob.setNoAi(false);
        Set<UUID> arrows = new HashSet<>();
        h.onEachTick(() -> h.getEntities(EntityTypes.ARROW, new BlockPos(8, 2, 8), 20).stream()
                .filter(arrow -> arrow.getOwner() == mob).forEach(arrow -> arrows.add(arrow.getUUID())));
        h.runAtTickTime(180, () -> {
            h.assertTrue(arrows.size() >= 2, "Vanilla ranged goal fires repeatedly without stalling; observed=" + arrows.size());
            h.assertTrue(mob.getMainHandItem().getItem() == bow && mob.getMainHandItem().getCount() == 1, "Shooting preserves one owned bow");
            h.assertTrue(mob.getTarget() == target, "Valid target retained");
            BuildupMobTweaks.LOGGER.info("S2-A ranged {} bow={} arrows={} position={}", type, bow, arrows.size(), mob.position());
            mob.discard(); target.discard(); h.succeed();
        });
    }
    @GameTest(structure = ARENA, maxTicks = 200)
    public void skeletonFiresRepeatedly(GameTestHelper h) { shooting(h, EntityTypes.SKELETON, Items.BOW); }
    @GameTest(structure = ARENA, maxTicks = 200)
    public void strayFiresRepeatedly(GameTestHelper h) { shooting(h, EntityTypes.STRAY, Items.BOW); }
    @GameTest(structure = ARENA, maxTicks = 200)
    public void boggedFiresRepeatedly(GameTestHelper h) { shooting(h, EntityTypes.BOGGED, Items.BOW); }
    @GameTest(structure = ARENA, maxTicks = 200)
    public void registeredCustomBowFiresRepeatedly(GameTestHelper h) { shooting(h, EntityTypes.SKELETON, CombatTestItems.BOW); }
    @GameTest(structure = ARENA, maxTicks = 60)
    public void realPickupUsesOneDroppedBow(GameTestHelper h) {
        floor(h); var mob = skeleton(h, EntityTypes.SKELETON, 3, 3);
        mob.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY); mob.setCanPickUpLoot(true); mob.setNoAi(false);
        h.getLevel().addFreshEntity(mob);
        var item = h.spawnItem(Items.BOW, 3.5f, 2f, 3.5f); item.setNoPickUpDelay();
        h.runAtTickTime(40, () -> {
            h.assertTrue(mob.getMainHandItem().getItem() == Items.BOW && mob.getMainHandItem().getCount() == 1, "Original item pickup equipped one bow");
            h.assertTrue(item.isRemoved(), "Original item entity consumed, no duplicate");
            h.assertTrue(mob.getGoalSelector().getAvailableGoals().stream().anyMatch(goal -> goal.getGoal() instanceof RangedBowAttackGoal<?>), "Pickup reassessed vanilla ranged goal");
            mob.discard(); h.succeed();
        });
    }    private static void precision(GameTestHelper h, EntityType<? extends AbstractSkeleton> type, int targetX) {
        floor(h); var cfg = config(); var combat = service(cfg); var mob = skeleton(h, type, 2, 3);
        combat.initialize(mob); combat = SkeletonCombat.instance(); h.getLevel().addFreshEntity(mob); var target = target(h, targetX, 3); mob.setTarget(target);
        combat.tick(mob); mob.getGoalSelector().removeAllGoals(goal -> true); mob.setNoAi(false);
        mob.startUsingItem(InteractionHand.MAIN_HAND);
        var liveCombat = combat;
        h.runAtTickTime(16, () -> {
            h.assertTrue(mob.getAttached(SkeletonCombat.DATA).getLongOr("next_special_at", 0) > h.getLevel().getGameTime(), "Special cooldown; target=" + mob.getTarget() + " ground=" + mob.onGround() + " draw=" + mob.getTicksUsingItem() + " visible=" + mob.hasLineOfSight(target) + " state=" + mob.getAttached(SkeletonCombat.DATA));
            if (type == EntityTypes.STRAY) h.assertFalse(mob.onGround(), "Stray jump is actual server movement");
            h.assertValueEqual(liveCombat.shotUncertainty(mob, 10), 1.0f, "Conditional shot improves uncertainty only");
            h.assertValueEqual(liveCombat.shotUncertainty(mob, 10), 10.0f, "Bonus consumed once");
            long cooldown = mob.getAttached(SkeletonCombat.DATA).getLongOr("next_special_at", 0);
            liveCombat.tick(mob);
            h.assertValueEqual(mob.getAttached(SkeletonCombat.DATA).getLongOr("next_special_at", 0), cooldown, "Cooldown prevents another activation");
            BuildupMobTweaks.LOGGER.info("S2-A special {} consumed once; cooldownUntil={}", type, cooldown);
            mob.discard(); target.discard(); h.succeed();
        });
    }
    @GameTest(structure = ARENA, maxTicks = 40)
    public void shadedSkeletonPrecisionHasWindupAndCooldown(GameTestHelper h) { precision(h, EntityTypes.SKELETON, 13); }
    @GameTest(structure = ARENA, maxTicks = 40)
    public void strayJumpPrecisionHasWindupAndCooldown(GameTestHelper h) { precision(h, EntityTypes.STRAY, 10); }
    @GameTest(structure = ARENA, maxTicks = 50)
    public void boggedSporeWindupPoisonsOnlyCurrentCloseTarget(GameTestHelper h) {
        floor(h); var cfg = config(); var combat = service(cfg); var mob = skeleton(h, EntityTypes.BOGGED, 3, 3);
        combat.initialize(mob); h.getLevel().addFreshEntity(mob);
        var victim = h.spawn(EntityTypes.COW, new BlockPos(5, 2, 3)); victim.setNoAi(true);
        var bystander = h.spawn(EntityTypes.COW, new BlockPos(3, 2, 5)); bystander.setNoAi(true);
        mob.setTarget(victim); SkeletonCombat.instance().tick(mob); mob.getGoalSelector().removeAllGoals(goal -> true); mob.setNoAi(false);
        h.runAtTickTime(10, () -> h.assertFalse(victim.hasEffect(MobEffects.POISON), "20-tick telegraph precedes poison"));
        h.runAtTickTime(28, () -> {
            h.assertTrue(victim.hasEffect(MobEffects.POISON), "Spore target=" + mob.getTarget() + " ground=" + mob.onGround() + " visible=" + mob.hasLineOfSight(victim) + " state=" + mob.getAttached(SkeletonCombat.DATA));
            h.assertFalse(bystander.hasEffect(MobEffects.POISON), "Nearby non-target never poisoned");
            h.assertTrue(mob.getAttached(SkeletonCombat.DATA).getLongOr("next_special_at", 0) > h.getLevel().getGameTime() + 180, "12-second cooldown reserved");
            mob.discard(); victim.discard(); bystander.discard(); h.succeed();
        });
    }
    @GameTest(structure = ARENA, maxTicks = 50)
    public void eachAdvancedSwitchPreventsActualActivation(GameTestHelper h) {
        floor(h); var mobs = new ArrayList<AbstractSkeleton>(); var combats = new ArrayList<SkeletonCombat>();
        var target = target(h, 13, 3);
        var types = List.of(EntityTypes.SKELETON, EntityTypes.STRAY, EntityTypes.BOGGED);
        for (int i = 0; i < 3; i++) {
            var cfg = config(); var combat = service(cfg); var mob = skeleton(h, types.get(i), i == 0 ? 2 : i == 1 ? 4 : 11, 3);
            combat.initialize(mob); h.assertTrue(combat.active(mob), "Seeded trait initially active");
            switch (i) {
                case 0 -> cfg.hostile.skeleton.skeletonSniping.accept(false);
                case 1 -> cfg.hostile.skeleton.strayJumpShot.accept(false);
                default -> cfg.hostile.skeleton.boggedSporeRetreat.accept(false);
            }
            h.getLevel().addFreshEntity(mob); mob.setTarget(target); mob.startUsingItem(InteractionHand.MAIN_HAND);
            mob.setOnGround(true); mobs.add(mob); combats.add(combat);
        }
        h.onEachTick(() -> { for (int i = 0; i < 3; i++) combats.get(i).tick(mobs.get(i)); });
        h.runAtTickTime(30, () -> {
            for (int i = 0; i < 3; i++) {
                var mob = mobs.get(i);
                h.assertValueEqual(mob.getAttached(SkeletonCombat.DATA).getLongOr("next_special_at", -1), 0L, "Disabled advanced behavior never starts");
                h.assertValueEqual(combats.get(i).shotUncertainty(mob, 10), 10.0f, "Original shot uncertainty retained"); mob.discard();
            }
            target.discard(); h.succeed();
        });
    }    @GameTest(structure = ARENA, maxTicks = 40)
    public void boggedActuallyRetreatsDuringTelegraph(GameTestHelper h) {
        floor(h); var mob = skeleton(h, EntityTypes.BOGGED, 6, 6);
        service(config()).initialize(mob); h.getLevel().addFreshEntity(mob);
        var target = target(h, 8, 6); mob.setTarget(target); mob.setNoAi(false);
        double initialX = mob.getX();
        h.runAtTickTime(12, () -> {
            h.assertTrue(mob.getAttached(SkeletonCombat.DATA).getLongOr("next_special_at", 0) > h.getLevel().getGameTime(), "Telegraph active");
            h.assertTrue(mob.getX() < initialX - .2, "Actual bow goal retreats away from close threat before first shot");
            mob.discard(); target.discard(); h.succeed();
        });
    }    @GameTest(structure = ARENA)
    public void specialArrowsRetainVanillaEffects(GameTestHelper h) {
        floor(h); var target = target(h, 12, 3);
        for (var type : List.of(EntityTypes.STRAY, EntityTypes.BOGGED)) {
            var mob = skeleton(h, type, 3, 3); h.getLevel().addFreshEntity(mob); mob.setTarget(target);
            mob.performRangedAttack(target, 1);
            var arrows = h.getEntities(EntityTypes.ARROW, new BlockPos(3, 2, 3), 6).stream().filter(a -> a.getOwner() == mob).toList();
            h.assertValueEqual(arrows.size(), 1, "One vanilla projectile per attack");
            var effect = type == EntityTypes.STRAY ? MobEffects.SLOWNESS : MobEffects.POISON;
            var contents = arrows.getFirst().getPickupItemStackOrigin().get(DataComponents.POTION_CONTENTS);
            h.assertTrue(contents != null && java.util.stream.StreamSupport.stream(contents.getAllEffects().spliterator(), false)
                    .anyMatch(e -> e.getEffect().equals(effect)), "Subclass arrow effect preserved");
            arrows.forEach(Entity::discard); mob.discard();
        }
        target.discard(); h.succeed();
    }
    @GameTest(structure = ARENA, maxTicks = 40)
    public void interruptedSporeWindupDoesNotPoisonReplacementTarget(GameTestHelper h) {
        floor(h); var mob = skeleton(h, EntityTypes.BOGGED, 3, 3);
        service(config()).initialize(mob); h.getLevel().addFreshEntity(mob);
        var first = h.spawn(EntityTypes.COW, new BlockPos(5, 2, 3)); first.setNoAi(true);
        var replacement = h.spawn(EntityTypes.COW, new BlockPos(3, 2, 5)); replacement.setNoAi(true);
        mob.setTarget(first); SkeletonCombat.instance().tick(mob); mob.getGoalSelector().removeAllGoals(goal -> true); mob.setNoAi(false);
        h.runAtTickTime(10, () -> {
            h.assertTrue(mob.getAttached(SkeletonCombat.DATA).getLongOr("next_special_at", 0) > 0, "Windup had started");
            mob.setTarget(replacement);
        });
        h.runAtTickTime(28, () -> {
            h.assertFalse(first.hasEffect(MobEffects.POISON) || replacement.hasEffect(MobEffects.POISON), "Target change cancels pending pulse and retains cooldown");
            mob.discard(); first.discard(); replacement.discard(); h.succeed();
        });
    }    @GameTest(structure = ARENA, maxTicks = 100)
    public void unknownWeaponStillUsesVanillaMelee(GameTestHelper h) {
        floor(h); var mob = skeleton(h, EntityTypes.SKELETON, 3, 3);
        mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.STICK));
        var victim = h.spawn(EntityTypes.COW, new BlockPos(5, 2, 3)); victim.setNoAi(true);
        float health = victim.getHealth(); h.getLevel().addFreshEntity(mob); mob.setTarget(victim); mob.setNoAi(false);
        h.runAtTickTime(60, () -> {
            h.assertTrue(victim.getHealth() < health, "Unsupported item retains real vanilla melee damage");
            h.assertTrue(mob.getMainHandItem().getItem() == Items.STICK && mob.getMainHandItem().getCount() == 1, "No substitute or duplicate equipment");
            mob.discard(); victim.discard(); h.succeed();
        });
    }
}