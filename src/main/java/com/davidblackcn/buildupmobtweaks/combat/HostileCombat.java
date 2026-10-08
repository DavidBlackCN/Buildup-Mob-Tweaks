package com.davidblackcn.buildupmobtweaks.combat;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import com.davidblackcn.buildupmobtweaks.feature.*;
import net.fabricmc.fabric.api.attachment.v1.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.illager.Pillager;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.item.*;

/** Shared server gate and versioned deadlines for S2 additions; no world-wide entity scan. */
public final class HostileCombat {
    public static final AttachmentType<CompoundTag> DATA = AttachmentRegistry.createPersistent(BuildupMobTweaks.id("hostile_combat"), CompoundTag.CODEC);
    private static final AttachmentType<Boolean> REGISTERED=AttachmentRegistry.create(BuildupMobTweaks.id("hostile_goals"));
    private static HostileCombat instance;
    private final FeatureRegistry features;
    public HostileCombat(FeatureRegistry features) { this.features = features; }
    public static HostileCombat instance() { return instance; }
    public boolean gate(FeatureId id) { return features.isEnabled(id); }
    public static boolean eligible(Mob mob) {
        var type = mob.getType();
        return mob instanceof Enemy && type != EntityTypes.WITHER && type != EntityTypes.ELDER_GUARDIAN
                && type != EntityTypes.WARDEN && type != EntityTypes.RAVAGER && type != EntityTypes.PHANTOM
                && type != EntityTypes.SLIME && type != EntityTypes.MAGMA_CUBE;
    }
    public void register() {
        instance = this;
        RestingHostiles.register();
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> { if (entity instanceof Mob mob && eligible(mob)) {
                initialize(mob);
                if (!mob.hasAttached(REGISTERED)) {
                    mob.setAttached(REGISTERED,true);
                    RestingHostiles.install(mob);
                    if(mob instanceof Silverfish fish) fish.getGoalSelector().addGoal(2,EnvironmentCombat.burrowGoal(fish));
                    if(mob instanceof net.minecraft.world.entity.monster.illager.Evoker evoker)
                        evoker.getGoalSelector().addGoal(2,new net.minecraft.world.entity.ai.goal.AvoidEntityGoal<LivingEntity>(evoker,LivingEntity.class,
                                other -> other==evoker.getTarget() && !(other instanceof net.minecraft.world.entity.player.Player),8,.6,.8,other -> true) {
                            @Override public boolean canUse() { return enabled(evoker,FeatureId.EVOKER_AVOID_TARGET_FIX) && !evoker.isCastingSpell() && super.canUse(); }
                            @Override public boolean canContinueToUse() { return enabled(evoker,FeatureId.EVOKER_AVOID_TARGET_FIX) && !evoker.isCastingSpell() && super.canContinueToUse(); }
                        });
                }
            }
            if (entity instanceof net.minecraft.world.entity.projectile.Projectile projectile && projectile.getOwner() instanceof Mob owner) CombatPerception.shot(owner); });
    }
    public void initialize(Mob mob) {
        if (!mob.hasAttached(DATA)) { var data = new CompoundTag(); data.putInt("version", 1); mob.setAttached(DATA, data); }
    }
    public boolean enabled(Mob mob, FeatureId id) {
        var data = mob.getAttached(DATA);
        return !mob.level().isClientSide() && eligible(mob) && data != null && data.getIntOr("version", -1) == 1 && gate(id);
    }
    public boolean ready(Mob mob, String key) {
        var data = mob.getAttached(DATA);
        return data != null && data.getIntOr("version", -1) == 1 && mob.level().getGameTime() >= data.getLongOr(key, 0);
    }
    public void reserve(Mob mob, String key, int ticks) {
        var data = mob.getAttached(DATA);
        if (data == null || data.getIntOr("version", -1) != 1) return;
        data = data.copy(); data.putLong(key, mob.level().getGameTime() + ticks); mob.setAttached(DATA, data);
    }
    public void fireball(Ghast ghast, LargeFireball ball) {
        if (enabled(ghast, FeatureId.GHAST_SLOW_FIREBALL)) {
            ball.accelerationPower *= .6; ball.setDeltaMovement(ball.getDeltaMovement().scale(.6));
        }
        if (enabled(ghast, FeatureId.GHAST_COOLDOWN)) reserve(ghast, "ghast_next", 100);
    }
    public boolean canGhastCharge(Ghast mob) { return !enabled(mob, FeatureId.GHAST_COOLDOWN) || ready(mob, "ghast_next"); }
    public int blazeVolley(Blaze blaze) {
        return enabled(blaze, FeatureId.BLAZE_DIFFICULTY_VOLLEY) ? switch (blaze.level().getDifficulty()) {
            case PEACEFUL, EASY -> 1; case NORMAL -> 2; case HARD -> 3;
        } : 3;
    }
    public void blazeTick(Blaze blaze, int step, int ticks) {
        if (enabled(blaze, FeatureId.BLAZE_ORBIT) && step == 1 && ticks > 0 && blaze.tickCount % 4 == 0) {
            var level = (ServerLevel) blaze.level(); int count = blazeVolley(blaze);
            for (int i = 0; i < count; i++) {
                double angle = blaze.tickCount * .16 + Math.PI * 2 * i / count;
                level.sendParticles(ParticleTypes.FLAME, blaze.getX() + Math.cos(angle) * .9, blaze.getY() + 1.1,
                        blaze.getZ() + Math.sin(angle) * .9, 1, 0, 0, 0, 0);
            }
        }
        if (enabled(blaze, FeatureId.BLAZE_STRAFE) && step > 1 && SkeletonCombat.validTarget(blaze, blaze.getTarget())) {
            var delta = blaze.getTarget().position().subtract(blaze.position()).multiply(1, 0, 1).normalize();
            double sign = (blaze.getId() & 1) == 0 ? 1 : -1;
            var side = new net.minecraft.world.phys.Vec3(-delta.z * sign, 0, delta.x * sign);
            if (blaze.level().noCollision(blaze, blaze.getBoundingBox().move(side)))
                blaze.getMoveControl().setWantedPosition(blaze.getX() + side.x, blaze.getY(), blaze.getZ() + side.z, .3);
        }
    }
    public float damage(LivingEntity entity, DamageSource source, float amount) {
        if(source.getDirectEntity() instanceof net.minecraft.world.entity.projectile.hurtingprojectile.WitherSkull skull){
            var saved=skull.getAttached(SkeletonExtras.PROJECTILE);
            if(saved!=null && saved.getIntOr("version",-1)==1 && (saved.getStringOr("role","").equals("skull") || saved.getStringOr("role","").equals("homing")))
                amount=Math.min(amount,4); // A fired weak projectile stays weak even after its owner unloads or the feature is disabled.
        }
        if (entity instanceof Vex vex && enabled(vex, FeatureId.VEX_PROJECTILE_WEAKNESS)) {
            if (source.getDirectEntity() != null && source.getDirectEntity().getType() == EntityTypes.SNOWBALL) return 2;
            if (source.is(DamageTypeTags.IS_PROJECTILE)) return amount * 1.5f;
        }
        if(entity instanceof Silverfish fish && enabled(fish,FeatureId.SILVERFISH_SHOVEL_WEAKNESS)
                && source.getDirectEntity()==source.getEntity() && source.getEntity() instanceof LivingEntity attacker
                && attacker.getMainHandItem().is(net.minecraft.tags.ItemTags.SHOVELS)) return amount*1.5f;
        return amount;
    }
    public boolean crossbow(Mob mob) {
        return !mob.getType().builtInRegistryHolder().is(S2Tags.RANGED_EXCLUDED) && (mob instanceof Pillager && enabled(mob, FeatureId.PILLAGER_CROSSBOW_COMPATIBILITY) || mob instanceof net.minecraft.world.entity.monster.piglin.Piglin && enabled(mob,FeatureId.PIGLIN_CROSSBOW_COMPATIBILITY))
                && (compatibleCrossbow(mob.getMainHandItem()) || compatibleCrossbow(mob.getOffhandItem()));
    }
    public static boolean compatibleCrossbow(ItemStack stack){return stack.getItem() instanceof CrossbowItem && !stack.is(S2Tags.RANGED_ITEMS_EXCLUDED);}
    public InteractionHand crossbowHand(LivingEntity entity, InteractionHand fallback) {
        return entity instanceof Mob mob && crossbow(mob)
                ? (compatibleCrossbow(mob.getMainHandItem()) ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND) : fallback;
    }
}