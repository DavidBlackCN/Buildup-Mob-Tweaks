package com.davidblackcn.buildupmobtweaks.combat;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import com.davidblackcn.buildupmobtweaks.equipment.EquipmentCapabilities;
import com.davidblackcn.buildupmobtweaks.feature.*;
import java.util.UUID;
import net.fabricmc.fabric.api.attachment.v1.*;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/** Server-only policy around vanilla goals. Never creates equipment or replaces arrow factories. */
public final class SkeletonCombat {
    public static final AttachmentType<CompoundTag> DATA = AttachmentRegistry.createPersistent(
            BuildupMobTweaks.id("skeleton_trait"), CompoundTag.CODEC);
    private static final AttachmentType<Runtime> RUNTIME = AttachmentRegistry.createDefaulted(
            BuildupMobTweaks.id("skeleton_runtime"), Runtime::new);
    private static SkeletonCombat instance;
    private final FeatureRegistry features;
    private static final class Runtime {
        long nextSwap, windupUntil;
        UUID windupTarget;
        FeatureId windup;
        int gates = -1;
    }

    public SkeletonCombat(FeatureRegistry features) { this.features = features; }
    public static SkeletonCombat instance() { return instance; }
    public void register() {
        instance = this;
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> initialize(entity));
        ServerLivingEntityEvents.MOB_CONVERSION.register((old, converted, context) -> {
            if (eligible(converted) && old.hasAttached(DATA)) converted.setAttached(DATA, old.getAttached(DATA).copy());
        });
    }
    public static boolean eligible(Entity entity) {
        return entity instanceof AbstractSkeleton && (entity.getType() == EntityTypes.SKELETON
                || entity.getType() == EntityTypes.STRAY || entity.getType() == EntityTypes.BOGGED);
    }
    public boolean enabled(Entity entity, FeatureId id) {
        return eligible(entity) && !entity.level().isClientSide() && features.isEnabled(id);
    }
    public static FeatureId special(Entity entity) {
        if (entity.getType() == EntityTypes.SKELETON) return FeatureId.SKELETON_SNIPING;
        if (entity.getType() == EntityTypes.STRAY) return FeatureId.STRAY_JUMP_SHOT;
        return FeatureId.BOGGED_SPORE_RETREAT;
    }
    public void initialize(Entity entity) {
        if (!eligible(entity) || entity.level().isClientSide() || entity.hasAttached(DATA)) return;
        var reason = entity.spawnReason();
        boolean fresh = !entity.isLoadedFromDisk() && reason != null && reason != EntitySpawnReason.LOAD
                && reason != EntitySpawnReason.CONVERSION && reason != EntitySpawnReason.DIMENSION_TRAVEL;
        var id = special(entity);
        var tag = new CompoundTag();
        tag.putInt("version", 1);
        tag.putString("origin", reason == null ? "UNKNOWN" : reason.name());
        tag.putString("status", fresh ? (enabled(entity, id) ? "rolled" : "disabled") : "legacy_or_source_skipped");
        // Trait tiers are classification metadata, never attribute multipliers.
        tag.putInt("level", id == FeatureId.BOGGED_SPORE_RETREAT ? 3 : 2);
        tag.putString("exclusive_group", "skeleton_special");
        tag.putString("trait", fresh && enabled(entity, id)
                && ((Mob) entity).getRandom().nextInt(1000) < features.skeletonChance(id) ? id.id().toString() : "none");
        tag.putLong("next_special_at", 0);
        entity.setAttached(DATA, tag);
    }
    public boolean active(Entity entity) {
        var data = entity.getAttached(DATA);
        return enabled(entity, special(entity)) && data != null && data.getIntOr("version", -1) == 1
                && data.getStringOr("trait", "").equals(special(entity).id().toString());
    }
    public boolean supportedBow(Entity entity, ItemStack stack) {
        return !stack.isEmpty() && (stack.getItem() == Items.BOW
                || enabled(entity, FeatureId.SKELETON_BOW_COMPATIBILITY) && EquipmentCapabilities.identify(stack).contains(EquipmentCapabilities.Ability.BOW));
    }
    public boolean usesBow(AbstractSkeleton mob) {
        if (enabled(mob, FeatureId.SKELETON_WEAPON_SWITCHING) && sword(mob.getMainHandItem())
                && supportedBow(mob, mob.getOffhandItem())) return false;
        return supportedBow(mob, mob.getMainHandItem()) || supportedBow(mob, mob.getOffhandItem());
    }
    public boolean handlesWeapons(Entity entity) {
        return enabled(entity, FeatureId.SKELETON_BOW_COMPATIBILITY) || enabled(entity, FeatureId.SKELETON_WEAPON_SWITCHING);
    }
    public InteractionHand bowHand(AbstractSkeleton mob, InteractionHand original) {
        if (!handlesWeapons(mob)) return original;
        if (supportedBow(mob, mob.getMainHandItem())) return InteractionHand.MAIN_HAND;
        if (supportedBow(mob, mob.getOffhandItem())) return InteractionHand.OFF_HAND;
        return original;
    }
    private static boolean sword(ItemStack stack) {
        return EquipmentCapabilities.identify(stack).contains(EquipmentCapabilities.Ability.MELEE);
    }
    public static boolean validTarget(Mob mob, LivingEntity target) {
        return target != null && target.isAlive() && target.level() == mob.level() && !mob.isAlliedTo(target)
                && mob.canAttack(target) && !(target instanceof Player player && (player.isCreative() || player.isSpectator()));
    }
    public void validateTarget(AbstractSkeleton mob) {
        if (enabled(mob, FeatureId.SKELETON_TARGET_VALIDATION) && mob.getTargetUnchecked() != null && !validTarget(mob, mob.getTargetUnchecked())) {
            mob.setTarget(null);
            mob.stopUsingItem();
            mob.getNavigation().stop();
        }
    }
    public void tick(AbstractSkeleton mob) {
        if (!eligible(mob) || mob.level().isClientSide()) return;
        validateTarget(mob);
        var rt = mob.getAttachedOrCreate(RUNTIME);
        int gates = (enabled(mob, FeatureId.SKELETON_BOW_COMPATIBILITY) ? 1 : 0)
                | (enabled(mob, FeatureId.SKELETON_WEAPON_SWITCHING) ? 2 : 0);
        if (gates != rt.gates) { rt.gates = gates; mob.reassessWeaponGoal(); }
        switchWeapon(mob);
        advanceSpecial(mob, rt);
    }
    public void switchWeapon(AbstractSkeleton mob) {
        if (!enabled(mob, FeatureId.SKELETON_WEAPON_SWITCHING) || !validTarget(mob, mob.getTarget())) return;
        var rt = mob.getAttachedOrCreate(RUNTIME);
        long now = mob.level().getGameTime();
        if (now < rt.nextSwap) return;
        double distance = mob.distanceToSqr(mob.getTarget());
        var main = mob.getMainHandItem(); var off = mob.getOffhandItem();
        if (!(distance <= 9 && supportedBow(mob, main) && sword(off)
                || distance >= 36 && sword(main) && supportedBow(mob, off))) return;
        mob.stopUsingItem();
        float mainDrop = mob.getDropChances().byEquipment(EquipmentSlot.MAINHAND);
        float offDrop = mob.getDropChances().byEquipment(EquipmentSlot.OFFHAND);
        // Transfer the actual stacks and their drop policies; no copies, inventory or fallback items.
        mob.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        mob.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        mob.setItemSlot(EquipmentSlot.MAINHAND, off);
        mob.setItemSlot(EquipmentSlot.OFFHAND, main);
        mob.setDropChance(EquipmentSlot.MAINHAND, offDrop);
        mob.setDropChance(EquipmentSlot.OFFHAND, mainDrop);
        rt.nextSwap = now + 20;
    }
    /** A one-block, flat-footprint check. Refuse cliffs, fluids, cramped spaces and damaging floors. */
    public static boolean safeStep(AbstractSkeleton mob, double dx, double dz) {
        double length = Math.sqrt(dx * dx + dz * dz);
        if (length < .001) return true;
        double half = mob.getBbWidth() / 2 + .05;
        for (double distance : new double[]{.5, 1.0}) {
            for (double x : new double[]{-half, half}) for (double z : new double[]{-half, half}) {
                var feet = BlockPos.containing(mob.getX() + dx / length * distance + x, mob.getY(), mob.getZ() + dz / length * distance + z);
                var floor = feet.below(); var state = mob.level().getBlockState(floor);
                if (!state.isFaceSturdy(mob.level(), floor, Direction.UP) || state.is(Blocks.MAGMA_BLOCK)
                        || state.is(Blocks.CAMPFIRE) || state.is(Blocks.SOUL_CAMPFIRE)
                        || !mob.level().getBlockState(feet).isAir() || !mob.level().getBlockState(feet.above()).isAir()) return false;
            }
        }
        return true;
    }
    public float[] strafe(AbstractSkeleton mob, float forward, float sideways) {
        if (!enabled(mob, FeatureId.SKELETON_SAFE_STRAFING)) return new float[]{forward, sideways};
        if (!mob.onGround() || mob.isInWater() || mob.isPassenger()) return new float[]{forward, sideways};
        double angle = Math.toRadians(mob.getYRot());
        for (float side : new float[]{sideways, -sideways, 0}) {
            double dx = side * Math.cos(angle) - forward * Math.sin(angle);
            double dz = forward * Math.cos(angle) + side * Math.sin(angle);
            if (safeStep(mob, dx, dz)) return new float[]{forward, side};
        }
        return new float[]{0, 0};
    }
    public void afterBowTick(AbstractSkeleton mob) {
        if (!eligible(mob) || !active(mob) || mob.getAttachedOrCreate(RUNTIME).windup != FeatureId.BOGGED_SPORE_RETREAT
                || !validTarget(mob, mob.getTarget()) || !mob.onGround() || mob.isInWater() || mob.isPassenger()) return;
        Vec3 away = mob.position().subtract(mob.getTarget().position()).multiply(1, 0, 1).normalize();
        // Override approach during the telegraph as well as established strafing; no path or entity scan.
        mob.getNavigation().stop();
        if (!safeStep(mob, away.x, away.z)) { mob.getMoveControl().strafe(0, 0); return; }
        double angle = Math.toRadians(mob.getYRot());
        mob.getMoveControl().strafe((float) ((away.z * Math.cos(angle) - away.x * Math.sin(angle)) * .5),
                (float) ((away.x * Math.cos(angle) + away.z * Math.sin(angle)) * .5));
    }
    private void advanceSpecial(AbstractSkeleton mob, Runtime rt) {
        var target = mob.getTarget();
        var level = (ServerLevel) mob.level();
        long now = level.getGameTime();
        if (!active(mob) || !usesBow(mob) || !validTarget(mob, target) || !mob.hasLineOfSight(target)) {
            rt.windup = null; rt.windupTarget = null; return;
        }
        if (rt.windup != null) {
            if (!target.getUUID().equals(rt.windupTarget)) { rt.windup = null; return; }
            if (now >= rt.windupUntil) {
                if (rt.windup == FeatureId.BOGGED_SPORE_RETREAT && mob.distanceToSqr(target) <= 9) {
                    target.addEffect(new MobEffectInstance(MobEffects.POISON, 40, 0), mob);
                }
                rt.windup = null;
            }
            return;
        }
        var data = mob.getAttached(DATA);
        if (now < data.getLongOr("next_special_at", Long.MAX_VALUE) || !mob.onGround() || mob.isInWater() || mob.isPassenger()) return;
        var id = special(mob);
        double distance = mob.distanceToSqr(target);
        if (id == FeatureId.SKELETON_SNIPING && (distance < 100 || distance > 225 || level.canSeeSky(mob.blockPosition()))) return;
        if (id == FeatureId.STRAY_JUMP_SHOT && (distance < 36 || distance > 144
                || !level.getBlockState(mob.blockPosition().above(2)).isAir())) return;
        if (id == FeatureId.BOGGED_SPORE_RETREAT && distance > 16) return;
        if (id != FeatureId.BOGGED_SPORE_RETREAT && (!mob.isUsingItem() || mob.getTicksUsingItem() < 10)) return;
        rt.windup = id; rt.windupTarget = target.getUUID(); rt.windupUntil = now + (id == FeatureId.BOGGED_SPORE_RETREAT ? 20 : 30);
        var updated = data.copy();
        updated.putLong("next_special_at", now + (id == FeatureId.BOGGED_SPORE_RETREAT ? 240 : 160));
        mob.setAttached(DATA, updated);
        level.sendParticles(id == FeatureId.BOGGED_SPORE_RETREAT ? ParticleTypes.SPORE_BLOSSOM_AIR : ParticleTypes.CRIT,
                mob.getX(), mob.getY() + 1, mob.getZ(), 12, .35, .4, .35, .03);
        if (id == FeatureId.STRAY_JUMP_SHOT) mob.jumpFromGround();
    }
    public float shotUncertainty(AbstractSkeleton mob, float original) {
        if (!eligible(mob)) return original;
        var rt = mob.getAttachedOrCreate(RUNTIME);
        if (!active(mob) || rt.windup == null || rt.windup == FeatureId.BOGGED_SPORE_RETREAT
                || !validTarget(mob, mob.getTarget()) || !mob.getTarget().getUUID().equals(rt.windupTarget)
                || !mob.hasLineOfSight(mob.getTarget()) || mob.level().getGameTime() >= rt.windupUntil) return original;
        var id = rt.windup;
        rt.windup = null;
        if (id == FeatureId.STRAY_JUMP_SHOT && mob.onGround()) return original;
        if (id == FeatureId.SKELETON_SNIPING && (mob.distanceToSqr(mob.getTarget()) < 100 || mob.level().canSeeSky(mob.blockPosition()))) return original;
        return Math.min(original, 1.0f);
    }
}