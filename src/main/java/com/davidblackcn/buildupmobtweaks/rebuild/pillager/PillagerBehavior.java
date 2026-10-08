/* Behavioral adaptation of Mob AI Tweaks (MIT), Copyright (c) 2024 N0t_UN_Owen.
 * Buildup adaptations Copyright (c) 2026 Buildup contributors. See NOTICE.md.
 */
package com.davidblackcn.buildupmobtweaks.rebuild.pillager;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import com.davidblackcn.buildupmobtweaks.feature.FeatureRegistry;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.attachment.v1.*;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.illager.Pillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.Vec3;

/** Owns only pillager state. ItemStacks remain in vanilla equipment/container slots. */
public final class PillagerBehavior {
    public static final AttachmentType<CompoundTag> DATA = AttachmentRegistry.createPersistent(
            BuildupMobTweaks.id("pillager_rebuild"), CompoundTag.CODEC);
    private static final AttachmentType<Runtime> RUNTIME = AttachmentRegistry.createDefaulted(
            BuildupMobTweaks.id("pillager_rebuild_runtime"), Runtime::new);
    public static final TagKey<EntityType<?>> EXCLUDED = TagKey.create(Registries.ENTITY_TYPE, BuildupMobTweaks.id("pillager_ai_excluded"));
    public static final TagKey<Item> EXCLUDED_ITEMS = TagKey.create(Registries.ITEM, BuildupMobTweaks.id("ranged_items_excluded"));
    public static final TagKey<Item> MELEE_ITEMS = TagKey.create(Registries.ITEM, BuildupMobTweaks.id("pillager_melee_weapons"));
    public static final String VANILLA_TAG = "buildupmobtweaks:vanilla_ai";
    private static PillagerBehavior instance;
    private final FeatureRegistry features;
    public static final class Runtime {
        boolean installed;
        int unseen;
        LivingEntity remembered;
        public int swaps, retreats, meals, targetClears;
        public String phase = "idle";
    }
    public PillagerBehavior(FeatureRegistry features) { this.features = features; }
    public static PillagerBehavior instance() { return instance; }
    public static Runtime runtime(Pillager mob) { return mob.getAttachedOrCreate(RUNTIME); }

    public void register() {
        instance = this;
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (entity instanceof Pillager mob) install(mob);
        });
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) -> {
            // Restore the borrowed hand before vanilla evaluates equipment drops.
            if (entity instanceof Pillager mob && known(mob)) restoreMeal(mob);
            return true;
        });
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (!(entity instanceof Pillager mob) || !(mob.level() instanceof ServerLevel level) || !known(mob)) return;
            // Backpack food is an AI supply, not bonus loot (including supplies saved by earlier R2-A builds).
            // Restore the borrowed hand before this point; preserve non-food equipment drop policy.
            // Clear exactly once, even with mob drops off.
            for (int i = 0; i < mob.getInventory().getContainerSize(); i++) {
                ItemStack stack = mob.getInventory().removeItemNoUpdate(i);
                float chance = data(mob).getFloatOr("drop_" + i, 1f);
                if (!stack.isEmpty() && !stack.has(DataComponents.FOOD)
                        && !EnchantmentHelper.has(stack, EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP)
                        && level.getGameRules().get(GameRules.MOB_DROPS) && mob.getRandom().nextFloat() < chance)
                    mob.spawnAtLocation(level, stack);
            }
        });
        PillagerCommands.register();
    }

    public void install(Pillager mob) {
        if (runtime(mob).installed) return;
        runtime(mob).installed = true;
        if (!mob.hasAttached(DATA)) {
            CompoundTag state = new CompoundTag(); state.putInt("version", 1); state.putInt("meal_slot", -1);
            mob.setAttached(DATA, state);
            boolean fresh = !mob.isLoadedFromDisk() && mob.spawnReason() != null
                    && mob.spawnReason() != EntitySpawnReason.LOAD && mob.spawnReason() != EntitySpawnReason.CONVERSION
                    && mob.spawnReason() != EntitySpawnReason.DIMENSION_TRAVEL;
            state.putBoolean("birth_checked", true);
            if (fresh && enabled(mob, FeatureId.PILLAGER_SPAWN_SUPPLIES)) seed(mob);
        }
        if (!known(mob)) return; // Future data belongs to its future owner; do not rewrite it.
        restoreMeal(mob); // Saved in-flight use never resumes; native inventory owns both stacks.
        mob.getGoalSelector().addGoal(1, new PillagerGoals.Melee(mob, this));
        mob.getGoalSelector().addGoal(4, new PillagerGoals.Eat(mob, this));
        mob.getGoalSelector().addGoal(9, new PillagerGoals.Switch(mob, this));
    }

    private void seed(Pillager mob) {
        if (!mob.getMainHandItem().is(Items.CROSSBOW)) return;
        if (enabled(mob, FeatureId.PILLAGER_WEAPON_SWITCH)) addSupply(mob, new ItemStack(Items.STONE_AXE), .085f);
        if (!enabled(mob, FeatureId.PILLAGER_FOOD_HEAL)) return;
        float difficulty = ((ServerLevel) mob.level()).getCurrentDifficultyAt(mob.blockPosition()).getEffectiveDifficulty();
        if (difficulty >= 1.5f) for (Item item : new Item[]{Items.BREAD, Items.APPLE, Items.CARROT, Items.BAKED_POTATO})
            if (mob.getRandom().nextBoolean()) addSupply(mob, new ItemStack(item, mob.getRandom().nextInt(16) + 1), 1);
        if (difficulty >= 4.5f) for (Item item : new Item[]{Items.GOLDEN_APPLE, Items.GOLDEN_CARROT})
            if (mob.getRandom().nextBoolean()) addSupply(mob, new ItemStack(item, mob.getRandom().nextInt(4) + 1), 1);
    }
    private void addSupply(Pillager mob, ItemStack stack, float chance) {
        int slot = find(mob, ItemStack::isEmpty);
        if (slot >= 0) { mob.getInventory().setItem(slot, stack); data(mob).putFloat("drop_" + slot, chance); }
    }
    public boolean enabled(Pillager mob, FeatureId id) {
        return known(mob) && features.isEnabled(id) && !mob.getType().builtInRegistryHolder().is(EXCLUDED)
                && !mob.entityTags().contains(VANILLA_TAG)
                && !mob.entityTags().contains("buildupmobtweaks:disable_" + id.id().getPath());
    }
    public static boolean known(Pillager mob) { return mob.hasAttached(DATA) && data(mob).getIntOr("version", -1) == 1; }
    private static CompoundTag data(Pillager mob) { return mob.getAttached(DATA); }
    public boolean crossbow(Pillager mob, ItemStack stack) {
        return !stack.is(EXCLUDED_ITEMS) && (stack.is(Items.CROSSBOW)
                || enabled(mob, FeatureId.PILLAGER_CROSSBOW_COMPATIBILITY) && stack.getItem() instanceof CrossbowItem);
    }
    public boolean meleeItem(ItemStack stack) {
        return !stack.isEmpty() && !stack.is(EXCLUDED_ITEMS) && (stack.is(ItemTags.AXES) || stack.is(MELEE_ITEMS));
    }
    public boolean melee(Pillager mob) {
        return enabled(mob, FeatureId.PILLAGER_WEAPON_SWITCH) && meleeItem(mob.getMainHandItem())
                && !(mob.getOffhandItem().getItem() instanceof ProjectileWeaponItem);
    }
    public static boolean validTarget(Pillager mob, LivingEntity target) {
        return target != null && target.isAlive() && target.level() == mob.level() && !mob.isAlliedTo(target)
                && mob.canAttack(target) && (!(target instanceof Player p) || !p.isCreative() && !p.isSpectator());
    }
    public void beforeAi(Pillager mob) {
        if (!known(mob)) return;
        Runtime rt = runtime(mob);
        if (!enabled(mob, FeatureId.PILLAGER_FOOD_HEAL)) restoreMeal(mob);
        if (!enabled(mob, FeatureId.PILLAGER_WEAPON_SWITCH) && data(mob).getBooleanOr("switched", false)) {
            int slot = find(mob, s -> crossbow(mob, s));
            if (slot >= 0 && meleeItem(mob.getMainHandItem())) exchange(mob, slot, EquipmentSlot.MAINHAND);
            data(mob).putBoolean("switched", false);
            mob.getNavigation().stop(); mob.getMoveControl().strafe(0, 0); mob.setAggressive(false);
        }
        if (!enabled(mob, FeatureId.PILLAGER_RETREAT) && rt.retreats > 0) mob.getMoveControl().strafe(0, 0);
        if (!enabled(mob, FeatureId.PILLAGER_TARGET_LIFECYCLE)) return;
        LivingEntity target = mob.getTarget();
        if (target != rt.remembered) { rt.remembered = target; rt.unseen = 0; }
        if (target == null) return;
        rt.unseen = mob.hasLineOfSight(target) ? 0 : rt.unseen + 1;
        double range = mob.getAttributeValue(Attributes.FOLLOW_RANGE);
        if (!validTarget(mob, target) || mob.distanceToSqr(target) > range * range || rt.unseen > 60) {
            mob.setTarget(null); mob.stopUsingItem(); mob.setChargingCrossbow(false); mob.setAggressive(false);
            mob.getNavigation().stop(); mob.getMoveControl().strafe(0, 0); rt.targetClears++; rt.phase = "target_exit";
        }
    }
    public static int find(Pillager mob, Predicate<ItemStack> predicate) {
        for (int i = 0; i < mob.getInventory().getContainerSize(); i++) if (predicate.test(mob.getInventory().getItem(i))) return i;
        return -1;
    }
    public void exchange(Pillager mob, int slot, EquipmentSlot hand) {
        ItemStack incoming = mob.getInventory().removeItemNoUpdate(slot);
        ItemStack outgoing = mob.getItemBySlot(hand);
        float incomingDrop = data(mob).getFloatOr("drop_" + slot, 1f);
        data(mob).putFloat("drop_" + slot, mob.getDropChances().byEquipment(hand));
        mob.setItemSlot(hand, incoming); mob.setDropChance(hand, incomingDrop);
        mob.getInventory().setItem(slot, outgoing);
    }
    public void switchWeapon(Pillager mob) {
        if (!enabled(mob, FeatureId.PILLAGER_WEAPON_SWITCH) || !validTarget(mob, mob.getTarget())
                || mob.isUsingItem() || mob.isSwinging() || mob.isChargingCrossbow()
                || mob.getOffhandItem().getItem() instanceof ProjectileWeaponItem) return;
        double distance = mob.distanceTo(mob.getTarget());
        boolean toMelee = distance < 3 && crossbow(mob, mob.getMainHandItem());
        boolean toRanged = distance > 3 && meleeItem(mob.getMainHandItem());
        if (toMelee || toRanged) {
            int slot = find(mob, toMelee ? this::meleeItem : s -> crossbow(mob, s));
            if (slot >= 0) {
                exchange(mob, slot, EquipmentSlot.MAINHAND);
                data(mob).putBoolean("switched", toMelee);
                runtime(mob).swaps++; runtime(mob).phase = toMelee ? "melee" : "ranged";
            }
        }
    }
    public boolean shieldApproach(Pillager mob) {
        LivingEntity target = mob.getTarget();
        return enabled(mob, FeatureId.PILLAGER_SHIELD_BREAK) && enabled(mob, FeatureId.PILLAGER_WEAPON_SWITCH) && validTarget(mob, target)
                && target.isBlocking() && target.getTicksUsingItem() > 60 && find(mob, this::meleeItem) >= 0;
    }
    public void rangedTick(Pillager mob) {
        LivingEntity target = mob.getTarget();
        if (!validTarget(mob, target)) return;
        if (shieldApproach(mob)) { mob.getNavigation().moveTo(target, 1); return; }
        if (!enabled(mob, FeatureId.PILLAGER_RETREAT) || mob.level().getDifficulty().getId() <= 1
                || !mob.onGround() || mob.isInWater() || mob.isPassenger() || !mob.hasLineOfSight(target)
                || target instanceof AgeableMob || mob.distanceTo(target) >= 6
                || target.getVehicle() != null && target.getControlledVehicle() == null
                || EnchantmentHelper.has(mob.getUseItem(), EnchantmentEffectComponents.PROJECTILE_COUNT)) return;
        Vec3 away = mob.position().subtract(target.position()).multiply(1, 0, 1).normalize();
        BlockPos step = BlockPos.containing(mob.position().add(away));
        if (!mob.level().hasChunkAt(step) || !mob.level().getBlockState(step.below()).isFaceSturdy(mob.level(), step.below(), Direction.UP)
                || !mob.level().getBlockState(step).isAir() || !mob.level().getBlockState(step.above()).isAir()) return;
        mob.lookAt(target, 60, 60); mob.getLookControl().setLookAt(target, 60, 60);
        mob.getNavigation().stop(); mob.getMoveControl().strafe(mob.isUsingItem() ? -.4f : -.8f, 0);
        runtime(mob).retreats++; runtime(mob).phase = "retreat";
    }
    public static boolean food(ItemStack stack) {
        return !stack.isEmpty() && stack.has(DataComponents.FOOD) && stack.has(DataComponents.CONSUMABLE);
    }
    public int mealSlot(Pillager mob) { return known(mob) ? data(mob).getIntOr("meal_slot", -1) : -1; }
    public boolean beginMeal(Pillager mob) {
        int slot = find(mob, PillagerBehavior::food);
        if (slot < 0) return false;
        exchange(mob, slot, EquipmentSlot.OFFHAND);
        data(mob).putInt("meal_slot", slot);
        mob.startUsingItem(InteractionHand.OFF_HAND);
        runtime(mob).phase = "eating";
        return true;
    }
    public void restoreMeal(Pillager mob) {
        int slot = mealSlot(mob);
        if (slot < 0) return;
        if (slot < mob.getInventory().getContainerSize()) {
            mob.stopUsingItem(); exchange(mob, slot, EquipmentSlot.OFFHAND);
        }
        data(mob).putInt("meal_slot", -1); runtime(mob).phase = "idle";
    }
    public void consumed(Pillager mob, int nutrition) {
        if (mealSlot(mob) < 0) return;
        if (enabled(mob, FeatureId.PILLAGER_FOOD_HEAL) && mob.getTarget() == null && nutrition > 0) {
            mob.heal(nutrition); runtime(mob).meals++;
        }
        restoreMeal(mob);
    }
}
