package com.davidblackcn.buildupmobtweaks.combat;
import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;

/** Use the strongest member of each group, then combine effect and posture once. */
public final class CombatPerception {
    private static boolean on(Mob mob, FeatureId id) { return HostileCombat.instance() != null && HostileCombat.instance().enabled(mob,id); }
    public static double visibility(Mob observer, LivingEntity target, double vanilla) {
        double effect = 1, posture = vanilla;
        if (on(observer,FeatureId.SENSE_BLINDNESS) && observer.hasEffect(MobEffects.BLINDNESS)) effect=.25;
        if (on(observer,FeatureId.SENSE_DARKNESS) && observer.hasEffect(MobEffects.DARKNESS))
            effect=Math.min(effect, observer.tickCount % 80 < 40 ? .35 : .65);
        if (on(observer,FeatureId.SENSE_NAUSEA) && observer.hasEffect(MobEffects.NAUSEA)) effect=Math.min(effect,.7);
        if (on(observer,FeatureId.SENSE_INVISIBILITY) && target.isInvisible()) {
            boolean engaged=observer.getLastHurtByMob()==target && observer.tickCount-observer.getLastHurtByMobTimestamp()<100;
            if (engaged) posture=1; else posture=Math.min(posture,.35);
        }
        if(on(observer,FeatureId.SENSE_CRAWLING) && target.isVisuallyCrawling()) posture=Math.min(posture,.4);
        if(on(observer,FeatureId.SENSE_CROUCHING) && target.isCrouching()) posture=Math.min(posture,.65);
        return effect*posture;
    }
    public static boolean blindBeyondReach(Mob observer, Entity target) {
        return on(observer,FeatureId.SENSE_BLINDNESS) && observer.hasEffect(MobEffects.BLINDNESS) && observer.distanceToSqr(target)>16;
    }
    public static boolean seesGlow(Mob observer, Entity target) {
        // Perception only; TargetingConditions still decides teams, invulnerability, type and range.
        return on(observer,FeatureId.SENSE_GLOWING) && target.isCurrentlyGlowing() && observer.distanceToSqr(target)<=32*32;
    }
    public static int meleeTicks(Mob mob,int vanilla) {
        double factor=1;
        if(on(mob,FeatureId.MELEE_EFFECT_INTERVAL)) {
            var haste=mob.getEffect(MobEffects.HASTE); var fatigue=mob.getEffect(MobEffects.MINING_FATIGUE);
            if(fatigue!=null) factor=1+Math.min(3,fatigue.getAmplifier()+1)*.25;
            else if(haste!=null) factor=1-Math.min(3,haste.getAmplifier()+1)*.15;
        }
        if(on(mob,FeatureId.OMEN_PRESSURE) && mob.getTarget()!=null &&
                (mob.getTarget().hasEffect(MobEffects.BAD_OMEN)||mob.getTarget().hasEffect(MobEffects.TRIAL_OMEN))) factor*=.9;
        return factor==1 ? vanilla : Math.max(10,Math.min(60,(int)Math.ceil(vanilla*factor)));
    }
    public static void jump(Mob mob) {
        if(!on(mob,FeatureId.MELEE_HIGH_TARGET_JUMP) || !mob.onGround() || mob.isInWater() || mob.isPassenger()
                || !SkeletonCombat.validTarget(mob,mob.getTarget()) || !mob.hasLineOfSight(mob.getTarget())) return;
        double height=mob.getTarget().getY()-mob.getY();
        if(height>.5 && height<2 && mob.distanceToSqr(mob.getTarget())<9 && HostileCombat.instance().ready(mob,"melee_jump")
                && mob.level().noCollision(mob,mob.getBoundingBox().move(0,1,0))) {
            mob.getJumpControl().jump(); HostileCombat.instance().reserve(mob,"melee_jump",40);
        }
    }
    public static float uncertainty(Projectile projectile,float vanilla) {
        return projectile.getOwner() instanceof Mob mob && on(mob,FeatureId.SENSE_NAUSEA) && mob.hasEffect(MobEffects.NAUSEA)
                ? Math.max(vanilla,12) : vanilla;
    }
    public static void shot(Mob mob) {
        if(!on(mob,FeatureId.RANGED_REPOSITION) || !mob.onGround() || mob.isPassenger() || mob.isInWater()
                || !SkeletonCombat.validTarget(mob,mob.getTarget()) || !HostileCombat.instance().ready(mob,"reposition")) return;
        HostileCombat.instance().reserve(mob,"reposition",60);
        Vec3 toward=mob.getTarget().position().subtract(mob.position()).multiply(1,0,1).normalize();
        double sign=(mob.getId()&1)==0?1:-1;
        double dx=-toward.z*sign,dz=toward.x*sign;
        if(SkeletonCombat.safeStep(mob,dx,dz)) mob.getNavigation().moveTo(mob.getX()+dx,mob.getY(),mob.getZ()+dz,.8);
    }
    private CombatPerception() {}
}