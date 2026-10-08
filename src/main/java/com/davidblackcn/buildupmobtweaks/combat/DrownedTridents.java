package com.davidblackcn.buildupmobtweaks.combat;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import com.davidblackcn.buildupmobtweaks.feature.*;
import java.util.*;
import net.fabricmc.fabric.api.attachment.v1.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.zombie.Drowned;
import net.minecraft.world.entity.projectile.arrow.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.gamerules.GameRules;

/** Exactly one owner: a hand OR a vanilla projectile. Attachments contain references, never backup items. */
public final class DrownedTridents {
    public static final AttachmentType<CompoundTag> FLIGHT = AttachmentRegistry.createPersistent(BuildupMobTweaks.id("trident_flight"), CompoundTag.CODEC);
    public static final AttachmentType<CompoundTag> OWNED = AttachmentRegistry.createPersistent(BuildupMobTweaks.id("owned_trident"), CompoundTag.CODEC);
    private static final AttachmentType<Boolean> GOAL = AttachmentRegistry.create(BuildupMobTweaks.id("trident_goal"));
    private static DrownedTridents instance;
    private final FeatureRegistry features;
    public DrownedTridents(FeatureRegistry features) { this.features = features; }
    public static DrownedTridents instance() { return instance; }
    public boolean conservation() { return features.isEnabled(FeatureId.DROWNED_TRIDENT_CONSERVATION); }
    public void register() {
        instance = this;
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (entity instanceof Drowned mob && !mob.hasAttached(GOAL)) {
                mob.setAttached(GOAL, true); mob.getGoalSelector().addGoal(0, new RecoverGoal(mob));
            }
        });
        ServerLivingEntityEvents.MOB_CONVERSION.register((old, converted, context) -> {
            if (old instanceof Drowned mob) {
                var projectile = projectile(mob);
                if (projectile != null) release(projectile);
                mob.removeAttached(FLIGHT); // Never duplicate a flight reference into another entity.
            }
        });
    }
    private static boolean known(CompoundTag tag) { return tag != null && tag.getIntOr("version", -1) == 1; }
    private static UUID reference(CompoundTag tag, String key) {
        if (!known(tag)) return null;
        try { return UUID.fromString(tag.getStringOr(key, "")); } catch (IllegalArgumentException invalid) { return null; }
    }
    public ThrownTrident projectile(Drowned mob) {
        var id = reference(mob.getAttached(FLIGHT), "projectile");
        if (id == null || !(mob.level() instanceof ServerLevel level)) return null;
        var entity = level.getEntity(id);
        return entity instanceof ThrownTrident trident && known(trident.getAttached(OWNED))
                && mob.getUUID().equals(reference(trident.getAttached(OWNED), "owner"))
                && !trident.getAttached(OWNED).getBooleanOr("released", true) ? trident : null;
    }
    public boolean throwHeld(Drowned mob, LivingEntity target) {
        if (!conservation() || !(mob.level() instanceof ServerLevel level)) return false;
        expireReference(mob);
        var stack = mob.getMainHandItem();
        if (mob.hasAttached(FLIGHT) || stack.getItem() != Items.TRIDENT || stack.getCount() != 1
                || !SkeletonCombat.validTarget(mob, target)) return false;
        var trident = new ThrownTrident(level, mob, stack.copy());
        var data = new CompoundTag(); data.putInt("version", 1); data.putString("owner", mob.getUUID().toString());
        data.putLong("deadline", level.getGameTime() + features.tridentTimeout());
        data.putFloat("drop_chance", mob.getDropChances().byEquipment(EquipmentSlot.MAINHAND));
        data.putBoolean("released", false); trident.setAttached(OWNED, data);
        trident.pickup = AbstractArrow.Pickup.DISALLOWED;
        double dx = target.getX() - mob.getX(), dz = target.getZ() - mob.getZ();
        trident.shoot(dx, target.getY(1.0 / 3) - trident.getY() + Math.sqrt(dx * dx + dz * dz) * .2,
                dz, 1.6f, mob.rangedAttackUncertainty(level));
        var flight = new CompoundTag(); flight.putInt("version", 1); flight.putString("projectile", trident.getUUID().toString());
        flight.putLong("deadline", data.getLongOr("deadline", 0));
        mob.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY); mob.setAttached(FLIGHT, flight); mob.stopUsingItem();
        if (!level.addFreshEntity(trident)) {
            mob.removeAttached(FLIGHT); mob.setItemSlot(EquipmentSlot.MAINHAND, stack); trident.discard(); return false;
        }
        trident.applyOnProjectileSpawned(level, trident.getPickupItemStackOrigin());
        mob.playSound(SoundEvents.DROWNED_SHOOT, 1, 1 / (mob.getRandom().nextFloat() * .4f + .8f));
        return true;
    }
    public void expireReference(Drowned mob) {
        var data = mob.getAttached(FLIGHT);
        if (known(data) && mob.level().getGameTime() >= data.getLongOr("deadline", Long.MAX_VALUE)) mob.removeAttached(FLIGHT);
    }
    public boolean recover(Drowned mob, ThrownTrident trident) {
        if (!features.isEnabled(FeatureId.DROWNED_TRIDENT_RECOVERY) || !mob.isAlive() || !mob.getMainHandItem().isEmpty()
                || trident.isRemoved() || projectile(mob) != trident || mob.distanceToSqr(trident) > 2.25
                || !((OwnedTrident) trident).buildup$readyForRecovery() || !mob.hasLineOfSight(trident)) return false;
        if (mob.level().getGameTime() >= trident.getAttached(OWNED).getLongOr("deadline", 0)) { release(trident); return false; }
        transfer(mob, trident); return true;
    }
    private static void transfer(Drowned mob, ThrownTrident trident) {
        ItemStack stack = trident.getPickupItemStackOrigin();
        float chance = trident.getAttached(OWNED).getFloatOr("drop_chance", 0);
        // Discard first, then transfer the actual stack, with no materialized backup in either attachment.
        trident.discard(); mob.removeAttached(FLIGHT);
        mob.setItemSlot(EquipmentSlot.MAINHAND, stack); mob.setDropChance(EquipmentSlot.MAINHAND, chance);
    }
    /** Before vanilla loot iteration: restore an accessible in-flight weapon to its empty original slot. */
    public void beforeDeathLoot(Drowned mob) {
        var trident = projectile(mob);
        if (trident != null && mob.getMainHandItem().isEmpty()) transfer(mob, trident);
        else if (trident != null) release(trident);
    }
    public void projectileTick(ThrownTrident trident) {
        var data = trident.getAttached(OWNED);
        if (!(trident.level() instanceof ServerLevel level) || !known(data)) return;
        if (data.getBooleanOr("released", true)) {
            trident.pickup = data.getBooleanOr("player_pickup", false) && features.isEnabled(FeatureId.DROWNED_TRIDENT_PLAYER_PICKUP)
                    ? AbstractArrow.Pickup.ALLOWED : AbstractArrow.Pickup.DISALLOWED;
            return;
        }
        var ownerId = reference(data, "owner");
        var entity = ownerId == null ? null : level.getEntity(ownerId);
        if (entity instanceof Drowned mob) {
            if (!mob.isAlive() || projectile(mob) != trident) { release(trident); return; }
            if (recover(mob, trident)) return;
        }
        if (level.getGameTime() >= data.getLongOr("deadline", Long.MAX_VALUE)) release(trident);
    }
    public void release(ThrownTrident trident) {
        var data = trident.getAttached(OWNED);
        if (!known(data) || data.getBooleanOr("released", true) || !(trident.level() instanceof ServerLevel level)) return;
        var updated = data.copy(); updated.putBoolean("released", true); trident.setAttached(OWNED, updated);
        var stack = trident.getPickupItemStackOrigin();
        boolean allow = features.isEnabled(FeatureId.DROWNED_TRIDENT_PLAYER_PICKUP) && level.getGameRules().get(GameRules.MOB_DROPS)
                && !EnchantmentHelper.has(stack, EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP)
                && trident.getRandom().nextFloat() < data.getFloatOr("drop_chance", 0);
        trident.pickup = allow ? AbstractArrow.Pickup.ALLOWED : AbstractArrow.Pickup.DISALLOWED;
        updated.putBoolean("player_pickup", allow);
        var ownerId = reference(data, "owner");
        if (ownerId != null && level.getEntity(ownerId) instanceof Drowned mob) {
            // Only clear the matching transaction; a late old projectile cannot clear a new throw.
            if (trident.getUUID().equals(reference(mob.getAttached(FLIGHT), "projectile"))) mob.removeAttached(FLIGHT);
        }
        trident.setOwner((Entity) null);
    }
    public boolean seekingWater(Drowned mob) {
        var trident = projectile(mob);
        return features.isEnabled(FeatureId.DROWNED_TRIDENT_RECOVERY) && mob.getMainHandItem().isEmpty()
                && trident != null && trident.isInWater() && mob.distanceToSqr(trident) <= 1024;
    }
    private final class RecoverGoal extends Goal {
        private final Drowned mob; private ThrownTrident trident;
        RecoverGoal(Drowned mob) { this.mob = mob; setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK)); }
        @Override public boolean canUse() {
            expireReference(mob); trident = projectile(mob);
            return features.isEnabled(FeatureId.DROWNED_TRIDENT_RECOVERY) && mob.getMainHandItem().isEmpty()
                    && trident != null && !trident.isRemoved() && ((OwnedTrident) trident).buildup$readyForRecovery()
                    && mob.distanceToSqr(trident) <= 1024;
        }
        @Override public boolean canContinueToUse() { return canUse(); }
        @Override public void tick() {
            if (recover(mob, trident)) return;
            if (mob.tickCount % 10 == 0) mob.getNavigation().moveTo(trident, 1.1);
            mob.getLookControl().setLookAt(trident, 30, 30);
        }
        @Override public void stop() { mob.getNavigation().stop(); }
        @Override public boolean requiresUpdateEveryTick() { return true; }
    }
}