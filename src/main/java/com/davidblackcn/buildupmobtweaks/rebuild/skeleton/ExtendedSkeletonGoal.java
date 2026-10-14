/* Mob AI Tweaks SPIN/GRENADE adaptation: Copyright (c) 2024 N0t_UN_Owen, MIT. */
package com.davidblackcn.buildupmobtweaks.rebuild.skeleton;

import com.davidblackcn.buildupmobtweaks.traits.core.TraitRuntime;
import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import java.util.EnumSet;

/** MOVE/LOOK arbitration suspends the native bow/melee goals only during this finite phase. */
final class ExtendedSkeletonGoal extends Goal {
    private final AbstractSkeleton mob;private final ExtendedSkeletonBehavior behavior;private final boolean spin;
    private int elapsed,direction;private Vec3 side=Vec3.ZERO;
    ExtendedSkeletonGoal(AbstractSkeleton m,ExtendedSkeletonBehavior b){mob=m;behavior=b;spin=m.getType()==net.minecraft.world.entity.EntityTypes.PARCHED;setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
    private FeatureId feature(){return spin?FeatureId.PARCHED_DODGE:FeatureId.WITHER_SKELETON_SKULL;}
    private boolean valid(){return behavior.enabled(mob,feature())&&!mob.isNoAi()&&mob.isAlive()&&!mob.isPassenger()&&SkeletonBehavior.valid(mob,mob.getTarget())&&mob.hasLineOfSight(mob.getTarget());}
    @Override public boolean canUse(){
        if(!valid()||!mob.onGround()||mob.isInWater())return false;
        var d=mob.getAttached(ExtendedSkeletonState.DATA);if(!(spin?d.getStringOr("birth","").equals("spin"):TraitRuntime.has(mob,FeatureId.WITHER_SKELETON_SKULL))||mob.level().getGameTime()<d.getLongOr("special_ready",Long.MAX_VALUE))return false;
        double distance=mob.distanceTo(mob.getTarget());if(distance<3||distance>24)return false;
        if(!spin)return distance>=6&&ExtendedSkeletonState.runtime(mob).projectiles.isEmpty();
        boolean bow=mob.getMainHandItem().is(Items.BOW)||mob.getOffhandItem().is(Items.BOW);
        if(bow&&(!mob.isUsingItem()||mob.getTicksUsingItem()<10))return false;
        var forward=mob.getTarget().position().subtract(mob.position()).multiply(1,0,1).normalize();
        side=new Vec3(-forward.z,0,forward.x).scale(Math.min(.65,mob.getAttributeValue(Attributes.MOVEMENT_SPEED)*3.5));direction=mob.getRandom().nextBoolean()?1:-1;
        if(!SkeletonBehavior.safeStep(mob,side.scale(direction*2)))direction=-direction;
        return SkeletonBehavior.safeStep(mob,side.scale(direction*2))&&mob.level().noCollision(mob,mob.getBoundingBox().move(0,1,0));
    }
    @Override public boolean canContinueToUse(){return valid()&&ExtendedSkeletonState.runtime(mob).active==this&&mob.getTarget()==ExtendedSkeletonState.runtime(mob).target&&elapsed<(spin?ExtendedSkeletonBehavior.SPIN_WARNING+ExtendedSkeletonBehavior.SPIN_TICKS:ExtendedSkeletonBehavior.SKULL_WARNING+1);}
    @Override public boolean requiresUpdateEveryTick(){return true;}
    @Override public void start(){elapsed=0;var r=ExtendedSkeletonState.runtime(mob);r.active=this;r.target=mob.getTarget();r.phase=spin?"spin_warning":"skull_warning";
        mob.getAttached(ExtendedSkeletonState.DATA).putLong("special_ready",mob.level().getGameTime()+(spin?ExtendedSkeletonBehavior.SPIN_COOLDOWN:ExtendedSkeletonBehavior.SKULL_COOLDOWN));
        mob.stopUsingItem();mob.getNavigation().stop();mob.getMoveControl().strafe(0,0);
        ((ServerLevel)mob.level()).playSound(null,mob.blockPosition(),spin?SoundEvents.SAND_FALL:SoundEvents.WITHER_AMBIENT,SoundSource.HOSTILE,.5f,1);
    }
    @Override public void tick(){
        var r=ExtendedSkeletonState.runtime(mob);
        // Vanilla may tick running goals between its every-third-tick continuation checks.
        if(r.active!=this||!valid()||r.target!=mob.getTarget()){behavior.reset(mob);return;}
        mob.getLookControl().setLookAt(r.target,60,60);mob.getNavigation().stop();elapsed++;
        var level=(ServerLevel)mob.level();level.sendParticles(spin?ParticleTypes.SMOKE:ParticleTypes.SOUL_FIRE_FLAME,mob.getX(),mob.getEyeY(),mob.getZ(),1,.1,.1,.1,.01);
        if(spin&&elapsed==ExtendedSkeletonBehavior.SPIN_WARNING){
            var impulse=side.scale(direction);if(!SkeletonBehavior.safeStep(mob,impulse.scale(2))){elapsed=ExtendedSkeletonBehavior.SPIN_WARNING+ExtendedSkeletonBehavior.SPIN_TICKS;return;}
            mob.jumpFromGround();mob.push(impulse.x,0,impulse.z);r.spins++;r.phase="spin";
        }
        if(spin&&elapsed>=ExtendedSkeletonBehavior.SPIN_WARNING)mob.setAttached(ExtendedSkeletonState.SPIN,direction*Math.max(0,ExtendedSkeletonBehavior.SPIN_WARNING+ExtendedSkeletonBehavior.SPIN_TICKS-elapsed));
        if(!spin&&elapsed==ExtendedSkeletonBehavior.SKULL_WARNING){behavior.release(mob,r.target);r.phase="skull_release";level.playSound(null,mob.blockPosition(),SoundEvents.WITHER_SHOOT,SoundSource.HOSTILE,.5f,1);}
    }
    @Override public void stop(){mob.getMoveControl().strafe(0,0);behavior.reset(mob);elapsed=0;side=Vec3.ZERO;}
}
