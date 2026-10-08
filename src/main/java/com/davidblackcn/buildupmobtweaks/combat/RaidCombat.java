package com.davidblackcn.buildupmobtweaks.combat;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import com.davidblackcn.buildupmobtweaks.feature.*;
import java.util.*;
import net.fabricmc.fabric.api.attachment.v1.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.core.particles.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.illager.*;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.*;
import net.minecraft.world.level.entity.EntityTypeTest;

/** Bounded server-side additions around vanilla raid goals; never changes raid membership or terrain. */
public final class RaidCombat {
    public static final AttachmentType<CompoundTag> DATA = AttachmentRegistry.createPersistent(BuildupMobTweaks.id("raid_combat"), CompoundTag.CODEC);
    private static final AttachmentType<Runtime> RUNTIME = AttachmentRegistry.createDefaulted(BuildupMobTweaks.id("raid_runtime"), Runtime::new);
    public static final AttachmentType<ItemStack> POTION_PREVIEW = AttachmentRegistry.create(BuildupMobTweaks.id("potion_preview"),
            builder -> builder.syncWith(ItemStack.OPTIONAL_STREAM_CODEC, AttachmentSyncPredicate.all()));
    private static RaidCombat instance;
    private final FeatureRegistry features;
    private static final class Runtime {
        boolean registered;
        long nextSwap, nextSupport, nextCount;
        int cachedVexes;
        LivingEntity supportTarget, requestTarget;
        Pending pending;
    }
    private record Pending(Projectile.ProjectileFactory<?> factory, ItemStack stack, LivingEntity target, long until, float speed, float uncertainty, boolean jumpOnly) {}
    public RaidCombat(FeatureRegistry features) { this.features = features; }
    public static RaidCombat instance() { return instance; }
    public static boolean eligible(Entity entity) {
        return entity.getType() == EntityTypes.PILLAGER || entity.getType() == EntityTypes.VINDICATOR
                || entity.getType() == EntityTypes.EVOKER || entity.getType() == EntityTypes.WITCH;
    }
    public void register() {
        instance = this;
        VexOwnership.register();
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (!eligible(entity)) return;
            var mob = (Mob) entity; initialize(mob);
            var rt = mob.getAttachedOrCreate(RUNTIME);
            if (!rt.registered && mob instanceof Pillager pillager) {
                rt.registered = true;
                pillager.getGoalSelector().addGoal(3, new MeleeAttackGoal(pillager, 1, false) {
                    @Override public boolean canUse() { return melee(pillager) && super.canUse(); }
                    @Override public boolean canContinueToUse() { return melee(pillager) && super.canContinueToUse(); }
                });
            }
        });
    }
    public void initialize(Mob mob) {
        if (eligible(mob) && !mob.hasAttached(DATA)) {
            var data = new CompoundTag(); data.putInt("version", 1); data.putLong("next_summon_at", 0); data.putLong("next_potion_at", 0);
            mob.setAttached(DATA, data);
        }
    }
    public boolean enabled(Entity mob, FeatureId id) {
        var data = mob.getAttached(DATA);
        return eligible(mob) && !mob.level().isClientSide() && data != null && data.getIntOr("version", -1) == 1 && features.isEnabled(id);
    }
    private void reserve(Mob mob, String key, int ticks) {
        var data = mob.getAttached(DATA).copy(); data.putLong(key, mob.level().getGameTime() + ticks); mob.setAttached(DATA, data);
    }
    private static boolean axe(ItemStack stack) {
        return stack.is(Items.WOODEN_AXE) || stack.is(Items.STONE_AXE) || stack.is(Items.IRON_AXE) || stack.is(Items.COPPER_AXE)
                || stack.is(Items.GOLDEN_AXE) || stack.is(Items.DIAMOND_AXE) || stack.is(Items.NETHERITE_AXE);
    }
    public boolean melee(Pillager mob) {
        return enabled(mob, FeatureId.PILLAGER_WEAPON_SWITCH) && axe(mob.getMainHandItem()) && mob.getOffhandItem().is(Items.CROSSBOW);
    }
    public void tick(Mob mob) {
        if (!eligible(mob) || mob.level().isClientSide()) return;
        if (mob instanceof Pillager pillager) switchWeapon(pillager);
        if (mob instanceof Vindicator vindicator) support(vindicator);
        if (mob instanceof Witch witch) advancePotion(witch);
    }
    public void switchWeapon(Pillager mob) {
        if (!enabled(mob, FeatureId.PILLAGER_WEAPON_SWITCH) || !SkeletonCombat.validTarget(mob, mob.getTarget())) return;
        var rt = mob.getAttachedOrCreate(RUNTIME); long now = mob.level().getGameTime();
        if (now < rt.nextSwap) return;
        var main = mob.getMainHandItem(); var off = mob.getOffhandItem(); double distance = mob.distanceToSqr(mob.getTarget());
        if (!(distance <= 9 && main.is(Items.CROSSBOW) && axe(off) || distance >= 36 && axe(main) && off.is(Items.CROSSBOW))) return;
        mob.stopUsingItem(); mob.setChargingCrossbow(false);
        float mainDrop = mob.getDropChances().byEquipment(EquipmentSlot.MAINHAND), offDrop = mob.getDropChances().byEquipment(EquipmentSlot.OFFHAND);
        mob.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY); mob.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        mob.setItemSlot(EquipmentSlot.MAINHAND, off); mob.setItemSlot(EquipmentSlot.OFFHAND, main);
        mob.setDropChance(EquipmentSlot.MAINHAND, offDrop); mob.setDropChance(EquipmentSlot.OFFHAND, mainDrop); rt.nextSwap = now + 20;
    }
    public void retreat(Pillager mob) {
        if (!enabled(mob, FeatureId.PILLAGER_RETREAT) || !mob.getMainHandItem().is(Items.CROSSBOW)
                || !SkeletonCombat.validTarget(mob, mob.getTarget()) || mob.distanceToSqr(mob.getTarget()) > 16
                || !mob.hasLineOfSight(mob.getTarget()) || !mob.onGround() || mob.isInWater() || mob.isPassenger()) return;
        var away = mob.position().subtract(mob.getTarget().position()).multiply(1, 0, 1).normalize();
        mob.getNavigation().stop();
        if (!SkeletonCombat.safeStep(mob, away.x, away.z)) { mob.getMoveControl().strafe(0, 0); return; }
        double angle = Math.toRadians(mob.getYRot());
        mob.getMoveControl().strafe((float) ((away.z * Math.cos(angle) - away.x * Math.sin(angle)) * .45),
                (float) ((away.x * Math.cos(angle) + away.z * Math.sin(angle)) * .45));
    }
    public void support(Vindicator mob) {
        var rt = mob.getAttachedOrCreate(RUNTIME);
        if (!enabled(mob, FeatureId.VINDICATOR_SUPPORT)) {
            if (rt.supportTarget != null && mob.getTarget() == rt.supportTarget) mob.setTarget(null);
            rt.supportTarget = null; return;
        }
        if (SkeletonCombat.validTarget(mob, mob.getTarget()) || mob.level().getGameTime() < rt.nextSupport) return;
        rt.nextSupport = mob.level().getGameTime() + 20;
        var casters = new ArrayList<Mob>();
        mob.level().getEntities(EntityTypeTest.forClass(Mob.class), mob.getBoundingBox().inflate(8),
                other -> other.isAlive() && (other.getType() == EntityTypes.EVOKER || other.getType() == EntityTypes.WITCH)
                        && mob.isAlliedTo(other) && other.tickCount - other.getLastHurtByMobTimestamp() <= 100, casters, 8);
        for (var caster : casters) {
            var attacker = caster.getLastHurtByMob();
            if (SkeletonCombat.validTarget(mob, attacker) && mob.distanceToSqr(attacker) <= 256 && mob.hasLineOfSight(attacker)) {
                rt.supportTarget = attacker; mob.setTarget(attacker); return;
            }
        }
    }
    public int ownedVexes(Evoker mob, boolean fresh) {
        return VexOwnership.count(mob);
    }
    public boolean canSummon(Evoker mob, boolean fresh) {
        if (enabled(mob, FeatureId.EVOKER_SUMMON_COOLDOWN)
                && mob.level().getGameTime() < mob.getAttached(DATA).getLongOr("next_summon_at", Long.MAX_VALUE)) return false;
        return !enabled(mob, FeatureId.EVOKER_VEX_LIMIT) || ownedVexes(mob, fresh) + 3 <= features.vexLimit();
    }
    public boolean beginSummon(Evoker mob) {
        if (!canSummon(mob, true)) return false;
        if (enabled(mob, FeatureId.EVOKER_SUMMON_COOLDOWN)) reserve(mob, "next_summon_at", features.summonCooldown());
        mob.getAttachedOrCreate(RUNTIME).nextCount = 0; return true;
    }
    public int summonInterval(Evoker mob, int vanilla) {
        return enabled(mob, FeatureId.EVOKER_SUMMON_COOLDOWN) ? features.summonCooldown() : vanilla;
    }
    private static boolean potionTarget(Witch mob, LivingEntity target) {
        return target != null && target.isAlive() && target.level() == mob.level() && mob.distanceToSqr(target) <= 256
                && mob.hasLineOfSight(target) && (target instanceof Raider || SkeletonCombat.validTarget(mob, target));
    }
    public boolean allowPotion(Witch mob, LivingEntity target) {
        var rt = mob.getAttachedOrCreate(RUNTIME); rt.requestTarget = target;
        if (!enabled(mob, FeatureId.WITCH_WINDUP) && !enabled(mob, FeatureId.WITCH_THROW_COOLDOWN) && !EnvironmentCombat.on(mob, FeatureId.WITCH_JUMP_THROW)) return true;
        return rt.pending == null && (mob.isDrinkingPotion() || potionTarget(mob, target)
                && (!enabled(mob, FeatureId.WITCH_THROW_COOLDOWN) || mob.level().getGameTime() >= mob.getAttached(DATA).getLongOr("next_potion_at", Long.MAX_VALUE)));
    }
    public Projectile potion(Projectile.ProjectileFactory<?> factory, ServerLevel level, ItemStack stack, Witch mob,
                             double dx, double dy, double dz, float speed, float uncertainty) {
        if (enabled(mob, FeatureId.WITCH_THROW_COOLDOWN)) reserve(mob, "next_potion_at", features.witchCooldownTicks());
        boolean windup = enabled(mob, FeatureId.WITCH_WINDUP);
        var requested = mob.getAttachedOrCreate(RUNTIME).requestTarget;
        boolean jump = EnvironmentCombat.on(mob, FeatureId.WITCH_JUMP_THROW) && requested != null && requested.getY() > mob.getY()+1
                && HostileCombat.instance().ready(mob,"witch_jump") && mob.onGround() && !mob.isInWater();
        if (!windup && !jump) return Projectile.spawnProjectileUsingShoot(factory, level, stack, mob, dx, dy, dz, speed, uncertainty);
        var rt = mob.getAttachedOrCreate(RUNTIME);
        rt.pending = new Pending(factory, stack, rt.requestTarget, level.getGameTime() + (windup ? features.witchWindupTicks() : 8), speed, uncertainty, !windup);
        mob.setAttached(POTION_PREVIEW, stack.copy());
        preview(mob, stack); return null; // Vanilla immediately discards the return value; no projectile was spawned yet.
    }
    private static void preview(Witch mob, ItemStack stack) {
        ((ServerLevel) mob.level()).sendParticles(new ItemParticleOption(ParticleTypes.ITEM, ItemStackTemplate.fromNonEmptyStack(stack)), mob.getX(), mob.getEyeY() + .3, mob.getZ(), 4, .12, .1, .12, .01);
    }
    public ItemStack pendingPotion(Witch mob) {
        var runtime = mob.getAttached(RUNTIME);
        var pending = runtime == null ? null : runtime.pending;
        return pending == null ? ItemStack.EMPTY : pending.stack.copy();
    }
    public void advancePotion(Witch mob) {
        var rt = mob.getAttachedOrCreate(RUNTIME); var pending = rt.pending;
        if (pending == null) return;
        var target = pending.target;
        if (!(pending.jumpOnly ? EnvironmentCombat.on(mob, FeatureId.WITCH_JUMP_THROW) : enabled(mob, FeatureId.WITCH_WINDUP)) || !potionTarget(mob, target) || mob.isDrinkingPotion()
                || (mob.getTarget() != target && !(target instanceof Raider && mob.getTarget() == null))) { rt.pending = null; mob.removeAttached(POTION_PREVIEW); return; }
        long now = mob.level().getGameTime();
        if (pending.until - now <= 8 && pending.until > now && EnvironmentCombat.on(mob,FeatureId.WITCH_JUMP_THROW)
                && target.getY() > mob.getY()+1 && mob.onGround() && !mob.isInWater()
                && HostileCombat.instance().ready(mob,"witch_jump") && mob.level().noCollision(mob,mob.getBoundingBox().move(0,1,0))) {
            mob.getJumpControl().jump(); HostileCombat.instance().reserve(mob,"witch_jump",100);
        }
        if (now < pending.until) { if (mob.tickCount % 5 == 0) preview(mob, pending.stack); return; }
        rt.pending = null; mob.removeAttached(POTION_PREVIEW);
        double dx = target.getX() - mob.getX(), dz = target.getZ() - mob.getZ();
        double dy = target.getEyeY() - 1.1 - mob.getY() + Math.sqrt(dx * dx + dz * dz) * .2;
        Projectile.spawnProjectileUsingShoot(pending.factory, (ServerLevel) mob.level(), pending.stack, mob, dx, dy, dz, pending.speed, pending.uncertainty);
    }
}
