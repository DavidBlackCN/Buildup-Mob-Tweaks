package com.davidblackcn.buildupmobtweaks;

import com.davidblackcn.buildupmobtweaks.combat.*;
import com.davidblackcn.buildupmobtweaks.config.BuildupConfig;
import com.davidblackcn.buildupmobtweaks.feature.*;
import java.util.*;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.*;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.illager.*;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion;
import net.minecraft.world.entity.raid.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.*;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.level.block.Blocks;
import static com.davidblackcn.buildupmobtweaks.CombatTestWorld.*;

public class RaidTests {
    private static RaidCombat service(BuildupConfig cfg) { return new RaidCombat(new FeatureRegistry(cfg)); }
    private static <T extends Mob> T placed(GameTestHelper h, EntityType<T> type, int x, int z) {
        var mob = mob(h, type, x, z); h.getLevel().addFreshEntity(mob); return mob;
    }
    private static Pillager armed(GameTestHelper h) {
        var mob = placed(h, EntityTypes.PILLAGER, 5, 5);
        mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.CROSSBOW));
        mob.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.IRON_AXE)); return mob;
    }
    @GameTest
    public void sevenIndependentGatesAndNoTraitDependency(GameTestHelper h) {
        var cfg = new BuildupConfig(); var registry = new FeatureRegistry(cfg); var r = cfg.hostile.raid;
        var ids = List.of(FeatureId.PILLAGER_RETREAT, FeatureId.PILLAGER_WEAPON_SWITCH, FeatureId.VINDICATOR_SUPPORT,
                FeatureId.EVOKER_VEX_LIMIT, FeatureId.EVOKER_SUMMON_COOLDOWN, FeatureId.WITCH_WINDUP, FeatureId.WITCH_THROW_COOLDOWN);
        var flags = List.of(r.pillagerRetreat, r.pillagerWeaponSwitch, r.vindicatorSupport, r.evokerVexLimit, r.evokerSummonCooldown, r.witchWindup, r.witchThrowCooldown);
        cfg.traits.enabled.accept(false);
        for (int i = 0; i < ids.size(); i++) {
            flags.get(i).accept(false);
            for (int j = 0; j < ids.size(); j++) h.assertValueEqual(registry.isEnabled(ids.get(j)), i != j, "Independent behavior gate, no advanced trait needed");
            flags.get(i).accept(true);
        }
        cfg.general.enabled.accept(false); for (var id : ids) h.assertFalse(registry.isEnabled(id), "Master switch"); h.succeed();
    }
    @GameTest(structure = ARENA, maxTicks = 40)
    public void weaponSwapConservesActualStacksComponentsAndDropChances(GameTestHelper h) {
        floor(h, Blocks.STONE); var mob = armed(h); var target = placed(h, EntityTypes.COW, 5, 7); mob.setTarget(target);
        var bow = mob.getMainHandItem(); var axe = mob.getOffhandItem(); bow.setDamageValue(21); axe.setDamageValue(7);
        bow.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.of(new ItemStackTemplate(Items.ARROW)));
        var expected = bow.copy(); mob.setDropChance(EquipmentSlot.MAINHAND, .13f); mob.setDropChance(EquipmentSlot.OFFHAND, .74f);
        RaidCombat.instance().switchWeapon(mob);
        h.assertTrue(mob.getMainHandItem() == axe && mob.getOffhandItem() == bow, "Transfer same two references");
        h.assertValueEqual(mob.getDropChances().byEquipment(EquipmentSlot.MAINHAND), .74f, "Axe drop follows item");
        target.snapTo(h.absolutePos(new BlockPos(13, 2, 5)), 0, 0); RaidCombat.instance().switchWeapon(mob);
        h.assertTrue(mob.getMainHandItem() == axe, "Swap cooldown prevents oscillation");
        h.runAtTickTime(25, () -> {
            RaidCombat.instance().switchWeapon(mob);
            h.assertTrue(mob.getMainHandItem() == bow && ItemStack.matches(bow, expected), "Return preserves charged ammunition and damage");
            var loaded = reload(h, mob); h.assertTrue(ItemStack.matches(loaded.getMainHandItem(), expected), "Real serialization retains crossbow components");
            h.assertValueEqual(loaded.getDropChances().byEquipment(EquipmentSlot.OFFHAND), .74f, "Saved axe drop policy");
            loaded.discard(); target.discard(); h.succeed();
        });
    }
    @GameTest(structure = ARENA)
    public void actualCrossbowGoalStopRetainsSwitchTarget(GameTestHelper h) {
        floor(h, Blocks.STONE); var mob = armed(h); var target = placed(h, EntityTypes.COW, 5, 7); mob.setTarget(target);
        var goal = mob.getGoalSelector().getAvailableGoals().stream().map(WrappedGoal::getGoal).filter(g -> g instanceof RangedCrossbowAttackGoal<?>).findFirst().orElseThrow();
        h.assertTrue(goal.canUse(), "Vanilla crossbow eligible before swapping"); RaidCombat.instance().switchWeapon(mob);
        h.assertFalse(goal.canUse(), "Offhand bow cannot steal melee role"); goal.stop();
        h.assertTrue(mob.getTarget() == target, "Actual vanilla stop injection retains valid target");
        mob.discard(); target.discard(); h.succeed();
    }
    @GameTest(structure = ARENA, maxTicks = 70)
    public void realMeleeGoalDamagesTargetWithoutExtraEquipment(GameTestHelper h) {
        floor(h, Blocks.STONE); var mob = armed(h); var target = placed(h, EntityTypes.COW, 5, 7); mob.setTarget(target); mob.setNoAi(false);
        float health = target.getHealth();
        h.succeedWhen(() -> {
            h.assertTrue(target.getHealth() < health, "Pillager actually attacks with owned axe");
            h.assertTrue(mob.getMainHandItem().is(Items.IRON_AXE) && mob.getOffhandItem().is(Items.CROSSBOW), "Exactly two original weapons remain");
            mob.discard(); target.discard();
        });
    }
    @GameTest(structure = ARENA, maxTicks = 50)
    public void crossbowRetreatMovesAwayOnSafeFloor(GameTestHelper h) {
        floor(h, Blocks.STONE); var mob = armed(h); mob.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        var target = placed(h, EntityTypes.IRON_GOLEM, 5, 8); mob.setTarget(target); mob.setNoAi(false); double start = mob.getZ();
        h.runAtTickTime(30, () -> {
            h.assertTrue(mob.getZ() < start - .25, "Actual retreat movement away from nearby target");
            h.assertTrue(mob.getTarget() == target, "Retreat retains combat target"); mob.discard(); target.discard(); h.succeed();
        });
    }
    @GameTest(structure = ARENA)
    public void cliffAndDisabledSwitchKeepVanillaBoundary(GameTestHelper h) {
        floor(h, Blocks.STONE); var cfg = new BuildupConfig(); var combat = service(cfg); var mob = armed(h);
        var target = placed(h, EntityTypes.COW, 5, 7); mob.setTarget(target); cfg.hostile.raid.pillagerWeaponSwitch.accept(false);
        combat.switchWeapon(mob); h.assertTrue(mob.getMainHandItem().is(Items.CROSSBOW), "Disabled switch never mutates hands");
        h.setBlock(new BlockPos(5, 1, 4), Blocks.AIR);
        h.assertFalse(SkeletonCombat.safeStep(mob, 0, -1), "Full-footprint cliff check shared with tested skeleton policy");
        mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.STICK)); cfg.hostile.raid.pillagerWeaponSwitch.accept(true);
        combat.switchWeapon(mob); h.assertTrue(mob.getMainHandItem().is(Items.STICK), "Unknown weapon left to original AI");
        mob.discard(); target.discard(); h.succeed();
    }
    @GameTest(structure = ARENA)
    public void vindicatorSupportsAlliedCasterWithoutStealingExistingTarget(GameTestHelper h) {
        floor(h, Blocks.STONE); var cfg = new BuildupConfig(); var combat = service(cfg);
        var mob = placed(h, EntityTypes.VINDICATOR, 3, 3); var caster = placed(h, EntityTypes.EVOKER, 5, 3);
        var attacker = placed(h, EntityTypes.COW, 7, 3); caster.setLastHurtByMob(attacker);
        combat.support(mob); h.assertTrue(mob.getTarget() == attacker, "Support actual allied spellcaster's attacker");
        cfg.hostile.raid.vindicatorSupport.accept(false); combat.support(mob);
        h.assertTrue(mob.getTarget() == null, "Disable clears only the target this feature assigned");
        cfg.hostile.raid.vindicatorSupport.accept(true);
        var original = placed(h, EntityTypes.IRON_GOLEM, 3, 6); mob.setTarget(original); combat.support(mob);
        h.assertTrue(mob.getTarget() == original, "Existing vanilla priority target retained");
        cfg.hostile.raid.vindicatorSupport.accept(false); combat.support(mob);
        h.assertTrue(mob.getTarget() == original, "Disable does not clear unrelated target");
        mob.setTarget(attacker); cfg.hostile.raid.vindicatorSupport.accept(true);
        mob.discard(); caster.discard(); attacker.discard(); original.discard(); h.succeed();
    }
    @GameTest(structure = ARENA)
    public void vexCapCountsOnlyOwnLoadedMinionsAndReservesThreeSlots(GameTestHelper h) {
        floor(h, Blocks.STONE); var cfg = new BuildupConfig(); cfg.hostile.raid.evokerSummonCooldown.accept(false);
        var combat = service(cfg); var first = placed(h, EntityTypes.EVOKER, 3, 3); var second = placed(h, EntityTypes.EVOKER, 10, 3);
        var vexes = new ArrayList<Vex>();
        for (int i = 0; i < 4; i++) { var vex = placed(h, EntityTypes.VEX, 5 + i, 4); vex.setOwner(first); vexes.add(vex); }
        h.assertFalse(combat.canSummon(first, true), "Four plus vanilla batch of three exceeds six");
        h.assertTrue(combat.canSummon(second, true), "Another evoker's minions excluded");
        vexes.getFirst().discard(); h.assertTrue(combat.canSummon(first, true), "Three plus three fits cap");
        cfg.hostile.raid.vexLimit.accept(3); h.assertFalse(combat.canSummon(first, true), "Changed cap applied on final cast check");
        cfg.hostile.raid.evokerVexLimit.accept(false); h.assertTrue(combat.canSummon(first, true), "Independent cap disable");
        for (var vex : vexes) vex.discard(); first.discard(); second.discard(); h.succeed();
    }
    @GameTest(structure = ARENA)
    public void actualSummonSpellCreatesThreeThenPersistsCooldown(GameTestHelper h) {
        floor(h, Blocks.STONE); var mob = placed(h, EntityTypes.EVOKER, 3, 3); var target = placed(h, EntityTypes.IRON_GOLEM, 12, 3); mob.setTarget(target);
        var goal = mob.getGoalSelector().getAvailableGoals().stream().map(WrappedGoal::getGoal)
                .filter(g -> g.getClass().getName().endsWith("$EvokerSummonSpellGoal")).findFirst().orElseThrow();
        h.assertTrue(goal.canUse(), "Vanilla summon allowed at zero owned vexes"); goal.start();
        for (int i = 0; i < 20; i++) goal.tick();
        h.assertValueEqual(RaidCombat.instance().ownedVexes(mob, true), 3, "Actual spell spawned exactly three owned vexes");
        var data = mob.getAttached(RaidCombat.DATA).copy();
        h.assertTrue(data.getLongOr("next_summon_at", 0) > h.getLevel().getGameTime(), "Persistent cooldown committed by actual spell");
        var loaded = reload(h, mob); h.assertValueEqual(loaded.getAttached(RaidCombat.DATA), data, "Real serialization preserves cooldown");
        h.assertFalse(RaidCombat.instance().canSummon(loaded, true), "Reload cannot reset summon window");
        for (var vex : h.getEntities(EntityTypes.VEX, new BlockPos(3, 2, 3), 12)) vex.discard(); loaded.discard(); target.discard(); h.succeed();
    }
    @GameTest(structure = ARENA)
    public void futureDataIsPreservedAndMasksExtraPolicies(GameTestHelper h) {
        floor(h, Blocks.STONE); var mob = armed(h); var data = new CompoundTag(); data.putInt("version", 99); data.putString("future", "keep"); mob.setAttached(RaidCombat.DATA, data);
        var loaded = reload(h, mob); RaidCombat.instance().initialize(loaded);
        h.assertValueEqual(loaded.getAttached(RaidCombat.DATA), data, "Unknown future data never rewritten");
        h.assertFalse(RaidCombat.instance().enabled(loaded, FeatureId.PILLAGER_WEAPON_SWITCH), "Unknown schema uses vanilla policies"); loaded.discard(); h.succeed();
    }
    @GameTest(structure = ARENA, maxTicks = 40)
    public void witchTelegraphsExactVanillaPotionThenThrowsOnce(GameTestHelper h) {
        floor(h, Blocks.STONE); var mob = placed(h, EntityTypes.WITCH, 3, 3); var target = placed(h, EntityTypes.IRON_GOLEM, 12, 3); mob.setTarget(target);
        mob.performRangedAttack(target, 1); var pending = RaidCombat.instance().pendingPotion(mob);
        h.assertFalse(pending.isEmpty(), "Actual vanilla potion selected before telegraph");
        h.assertTrue(pending.get(DataComponents.POTION_CONTENTS).is(Potions.SLOWNESS), "Long-range vanilla slowness choice retained");
        h.assertValueEqual(h.getEntities(EntityTypes.SPLASH_POTION, new BlockPos(3, 2, 3), 16).size(), 0, "No projectile during windup");
        for (int i = 0; i < 10; i++) mob.performRangedAttack(target, 1);
        h.runAtTickTime(22, () -> {
            RaidCombat.instance().advancePotion(mob); var potions = h.getEntities(EntityTypes.SPLASH_POTION, new BlockPos(3, 2, 3), 16);
            h.assertValueEqual(potions.size(), 1, "One telegraph releases one projectile");
            h.assertTrue(ItemStack.matches(potions.getFirst().getItem(), pending), "Exactly previewed components thrown");
            mob.performRangedAttack(target, 1); h.assertTrue(RaidCombat.instance().pendingPotion(mob).isEmpty(), "Independent cooldown blocks second throw");
            h.assertTrue(mob.getMainHandItem().isEmpty() && mob.getOffhandItem().isEmpty(), "Preview never inserts lootable equipment");
            potions.forEach(Entity::discard); mob.discard(); target.discard(); h.succeed();
        });
    }
    @GameTest(structure = ARENA, maxTicks = 40)
    public void witchRetainsVanillaAllyHealingTarget(GameTestHelper h) {
        floor(h, Blocks.STONE); var mob = placed(h, EntityTypes.WITCH, 3, 3); var ally = placed(h, EntityTypes.PILLAGER, 8, 3); ally.setHealth(3); mob.setTarget(ally);
        mob.performRangedAttack(ally, 1); var pending = RaidCombat.instance().pendingPotion(mob);
        h.assertTrue(pending.get(DataComponents.POTION_CONTENTS).is(Potions.HEALING), "Vanilla wounded raider healing choice preserved");
        h.assertTrue(mob.getTarget() == null, "Vanilla support target release preserved");
        h.runAtTickTime(22, () -> {
            RaidCombat.instance().advancePotion(mob);
            var potions = h.getEntities(EntityTypes.SPLASH_POTION, new BlockPos(3, 2, 3), 16);
            h.assertValueEqual(potions.size(), 1, "Healing is not rejected as friendly fire");
            potions.forEach(Entity::discard); mob.discard(); ally.discard(); h.succeed();
        });
    }
    @GameTest(structure = ARENA)
    public void witchWindupCancelsOnTargetChangeOrDisable(GameTestHelper h) {
        floor(h, Blocks.STONE); var cfg = new BuildupConfig(); var combat = service(cfg); var mob = placed(h, EntityTypes.WITCH, 3, 3); var target = placed(h, EntityTypes.COW, 10, 3); mob.setTarget(target);
        var stack = PotionContents.createItemStack(Items.SPLASH_POTION, Potions.POISON);
        combat.allowPotion(mob, target); combat.potion(ThrownSplashPotion::new, h.getLevel(), stack, mob, 1, 0, 0, .75f, 8);
        mob.setTarget(null); combat.advancePotion(mob); h.assertTrue(combat.pendingPotion(mob).isEmpty(), "Changed hostile target cancels queued attack");
        cfg.hostile.raid.witchThrowCooldown.accept(false); mob.setTarget(target); combat.allowPotion(mob, target);
        combat.potion(ThrownSplashPotion::new, h.getLevel(), stack, mob, 1, 0, 0, .75f, 8);
        cfg.hostile.raid.witchWindup.accept(false); combat.advancePotion(mob);
        h.assertTrue(combat.pendingPotion(mob).isEmpty(), "Disabling cancels windup without late projectile");
        h.assertValueEqual(h.getEntities(EntityTypes.SPLASH_POTION, new BlockPos(3, 2, 3), 16).size(), 0, "No delayed unexpected attack"); mob.discard(); target.discard(); h.succeed();
    }
    @GameTest(structure = ARENA)
    public void witchCooldownAndWindupDisableIndependentlyAndReloadCancelsPending(GameTestHelper h) {
        floor(h, Blocks.STONE); var cfg = new BuildupConfig(); var combat = service(cfg); var mob = placed(h, EntityTypes.WITCH, 3, 3); var target = placed(h, EntityTypes.COW, 10, 3); mob.setTarget(target);
        cfg.hostile.raid.witchWindup.accept(false); var stack = PotionContents.createItemStack(Items.SPLASH_POTION, Potions.POISON);
        h.assertTrue(combat.allowPotion(mob, target), "First throw allowed");
        var shot = combat.potion(ThrownSplashPotion::new, h.getLevel(), stack, mob, 1, 0, 0, .75f, 8);
        h.assertTrue(shot != null && !combat.allowPotion(mob, target), "Windup off keeps actual shot and cooldown"); shot.discard();
        cfg.hostile.raid.witchThrowCooldown.accept(false); cfg.hostile.raid.witchWindup.accept(true);
        h.assertTrue(combat.allowPotion(mob, target), "Cooldown independently disabled"); combat.potion(ThrownSplashPotion::new, h.getLevel(), stack, mob, 1, 0, 0, .75f, 8);
        var saved = mob.getAttached(RaidCombat.DATA).copy(); var loaded = reload(h, mob);
        h.assertTrue(combat.pendingPotion(loaded).isEmpty(), "Unsaved windup never fires on reload");
        h.assertValueEqual(loaded.getAttached(RaidCombat.DATA), saved, "Persistent cooldown survives reload"); loaded.discard(); target.discard(); h.succeed();
    }
    @GameTest(structure = ARENA, maxTicks = 90)
    public void controlledLastRaidWaveCanDieAndReachVictory(GameTestHelper h) {
        floor(h, Blocks.STONE); var center = h.absolutePos(new BlockPos(8, 2, 8));
        var home = h.getLevel().registryAccess().lookupOrThrow(Registries.POINT_OF_INTEREST_TYPE).getOrThrow(PoiTypes.HOME);
        h.getLevel().getPoiManager().add(center, home);
        h.getLevel().getPoiManager().take(holder -> holder.is(PoiTypes.HOME), (holder, pos) -> pos.equals(center), center, 1);
        var codec = Raid.MAP_CODEC.codec(); var tag = (CompoundTag) codec.encodeStart(NbtOps.INSTANCE, new Raid(center, Difficulty.EASY)).getOrThrow();
        tag.putBoolean("started", true); tag.putBoolean("active", true); tag.putInt("groups_spawned", 1); tag.putInt("group_count", 1); tag.putInt("cooldown_ticks", 0);
        var raid = codec.parse(NbtOps.INSTANCE, tag).getOrThrow();
        var mobs = List.of(placed(h, EntityTypes.PILLAGER, 3, 3), placed(h, EntityTypes.VINDICATOR, 4, 3), placed(h, EntityTypes.EVOKER, 5, 3), placed(h, EntityTypes.WITCH, 6, 3));
        for (var mob : mobs) raid.joinRaid(h.getLevel(), 1, (Raider) mob, null, true);
        h.assertValueEqual(raid.getTotalRaidersAlive(), 4, "Four actual raiders joined the original wave");
        h.runAtTickTime(10, () -> {
            h.assertTrue(h.getLevel().isVillage(center), "Occupied home POI makes a real village section");
            for (var mob : mobs) mob.kill(h.getLevel());
            h.assertValueEqual(raid.getTotalRaidersAlive(), 0, "Actual death removes all four raid members");
        });
        for (int tick = 11; tick < 75; tick++) h.runAtTickTime(tick, () -> raid.tick(h.getLevel()));
        h.runAtTickTime(76, () -> {
            h.assertTrue(raid.isVictory(), "Original Raid.tick reaches victory after last wave deaths");
            h.getLevel().getPoiManager().remove(center); h.succeed();
        });
    }
    @GameTest(structure = ARENA, maxTicks = 220)
    public void pillagerReturnsFromRealMeleeToRealCrossbowFire(GameTestHelper h) {
        floor(h, Blocks.STONE); var mob = armed(h); var target = placed(h, EntityTypes.IRON_GOLEM, 5, 7);
        mob.setTarget(target); mob.setNoAi(false); boolean[] switched = {false};
        h.runAtTickTime(20, () -> {
            h.assertTrue(mob.getMainHandItem().is(Items.IRON_AXE), "Actual AI entered melee role");
            target.snapTo(h.absolutePos(new BlockPos(13, 2, 5)), 0, 0); switched[0] = true;
        });
        h.succeedWhen(() -> {
            h.assertTrue(switched[0] && mob.getMainHandItem().is(Items.CROSSBOW), "Actual AI restored ranged role");
            h.assertTrue(!h.getEntities(EntityTypes.ARROW, new BlockPos(8, 2, 5), 16).isEmpty(), "Vanilla crossbow fired after switching back");
            h.assertTrue(mob.getTarget() == target, "Same target survives the full melee/ranged round trip");
            mob.discard(); target.discard();
        });
    }
}
