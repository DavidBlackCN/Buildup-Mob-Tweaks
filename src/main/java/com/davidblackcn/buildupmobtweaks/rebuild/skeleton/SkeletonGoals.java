/* Behavioral reference: Mob AI Tweaks, Copyright (c) 2024 N0t_UN_Owen, MIT. */
package com.davidblackcn.buildupmobtweaks.rebuild.skeleton;

import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public final class SkeletonGoals {
    private SkeletonGoals(){}
    static final class Melee extends MeleeAttackGoal {
        final AbstractSkeleton mob;final SkeletonBehavior behavior;
        Melee(AbstractSkeleton m,SkeletonBehavior b){super(m,1.1,false);mob=m;behavior=b;}
        private boolean allowed(){return SkeletonState.runtime(mob).managed&&!behavior.hasBow(mob)
                &&!(mob.getMainHandItem().getItem() instanceof ProjectileWeaponItem)&&SkeletonBehavior.valid(mob,mob.getTarget());}
        @Override public boolean canUse(){return allowed()&&super.canUse();}
        @Override public boolean canContinueToUse(){return allowed()&&super.canContinueToUse();}
        @Override public void start(){super.start();SkeletonState.runtime(mob).mode="melee";}
        @Override public void stop(){super.stop();mob.setSprinting(false);}
    }
    static final class Walk extends RangedBowAttackGoal<AbstractSkeleton> {
        final AbstractSkeleton mob;final SkeletonBehavior behavior;
        Walk(AbstractSkeleton m,SkeletonBehavior b){super(m,1,40,15);mob=m;behavior=b;}
        @Override protected boolean isHoldingBow(){return behavior.hasBow(mob);}
        @Override public boolean canUse(){return SkeletonState.runtime(mob).managed&&!behavior.sniper(mob)&&SkeletonBehavior.valid(mob,mob.getTarget())&&super.canUse();}
        @Override public boolean canContinueToUse(){return canUse();}
        @Override public void start(){setMinAttackInterval(behavior.interval(mob));super.start();SkeletonState.runtime(mob).mode="walk";}
        @Override public void stop(){super.stop();mob.getMoveControl().strafe(0,0);}
    }
    static final class Sniper extends RangedAttackGoal {
        final AbstractSkeleton mob;final SkeletonBehavior behavior;
        Path reposition;int repositionTicks;
        Sniper(AbstractSkeleton m,SkeletonBehavior b){super(m,1.25,((SkeletonAccess)m).buildup$attackInterval(true)+20,((SkeletonAccess)m).buildup$attackInterval(false)+20,15);mob=m;behavior=b;}
        @Override public boolean canUse(){return SkeletonState.runtime(mob).managed&&behavior.sniper(mob)&&SkeletonBehavior.valid(mob,mob.getTarget())&&super.canUse();}
        @Override public boolean canContinueToUse(){return canUse();}
        @Override public void start(){super.start();mob.setAggressive(true);SkeletonState.runtime(mob).mode="sniper";}
        @Override public void stop(){super.stop();mob.stopUsingItem();mob.setAggressive(false);mob.getMoveControl().strafe(0,0);reposition=null;}
        @Override public void tick(){
            super.tick();var target=mob.getTarget();if(!SkeletonBehavior.valid(mob,target))return;
            if(!behavior.enabled(mob,FeatureId.RANGED_REPOSITION)){reposition=null;return;}
            if(reposition==null&&!mob.isUsingItem()&&mob.getRandom().nextInt(((SkeletonAccess)mob).buildup$attackInterval(true)+20)==0){
                int dx=mob.getRandom().nextInt(8)+7,dz=mob.getRandom().nextInt(8)+7;
                if(target.getX()<mob.getX())dx=-dx;if(target.getZ()<mob.getZ())dz=-dz;
                if(mob.getRandom().nextInt(3)==1){if(mob.getRandom().nextBoolean())dx=-dx;else dz=-dz;}
                var destination=new Vec3(target.getX()-dx,mob.getY(),target.getZ()-dz);
                if(SkeletonBehavior.safeStep(mob,destination.subtract(mob.position()))){var path=mob.getNavigation().createPath(BlockPos.containing(destination),0);
                    if(path!=null&&path.canReach()){reposition=path;repositionTicks=0;SkeletonState.runtime(mob).repositions++;}}
            }
            if(reposition!=null){
                if(!mob.isUsingItem())mob.getNavigation().moveTo(reposition,1.25);
                if(++repositionTicks>=40||reposition.isDone())reposition=null;
            }else{
                float distance=mob.distanceTo(target);float motion=distance<5&&mob.level().getDifficulty().getId()>1?-1.25f:distance>12?1.25f:0;
                var direction=target.position().subtract(mob.position()).multiply(1,0,1).normalize().scale(Math.signum(motion));
                if(motion!=0&&SkeletonBehavior.safeStep(mob,direction)){mob.lookAt(target,60,60);mob.getMoveControl().strafe(motion,0);}
            }
        }
    }
    static final class Switch extends Goal {
        final AbstractSkeleton mob;final SkeletonBehavior behavior;
        Switch(AbstractSkeleton m,SkeletonBehavior b){mob=m;behavior=b;}
        @Override public boolean canUse(){return behavior.enabled(mob,FeatureId.SKELETON_WEAPON_SWITCHING)&&SkeletonBehavior.valid(mob,mob.getTarget());}
        @Override public boolean requiresUpdateEveryTick(){return true;}
        @Override public void tick(){behavior.switchWeapon(mob);}
        @Override public void stop(){behavior.restoreBow(mob);}
    }
    /** Buildup shelter is a separate product feature, not upstream random repositioning. */
    static final class Shelter extends Goal {
        final AbstractSkeleton mob;final SkeletonBehavior behavior;Path path;int time,nextSearch;
        Shelter(AbstractSkeleton m,SkeletonBehavior b){mob=m;behavior=b;setFlags(EnumSet.of(Flag.MOVE));}
        private boolean allowed(){return behavior.enabled(mob,FeatureId.SKELETON_SHELTER)&&SkeletonBehavior.valid(mob,mob.getTarget())
                &&mob.level().isBrightOutside()&&mob.level().canSeeSky(mob.blockPosition())&&!mob.isPassenger();}
        @Override public boolean canUse(){
            if(!allowed()||mob.tickCount<nextSearch)return false;
            nextSearch=mob.tickCount+20;
            var origin=mob.blockPosition();
            for(int radius=2;radius<=8;radius+=2)for(int direction=0;direction<8;direction++){
                double a=direction*Math.PI/4;BlockPos pos=origin.offset((int)Math.round(Math.cos(a)*radius),0,(int)Math.round(Math.sin(a)*radius));
                if(Vec3.atBottomCenterOf(pos).distanceToSqr(mob.getTarget().position())>225)continue;
                // Navigation may finish short of the node center; choose cover with a one-block margin.
                if(mob.level().canSeeSky(pos.north())||mob.level().canSeeSky(pos.south())
                        ||mob.level().canSeeSky(pos.east())||mob.level().canSeeSky(pos.west()))continue;
                if(mob.level().canSeeSky(pos)||!SkeletonBehavior.safeStep(mob,Vec3.atBottomCenterOf(pos).subtract(mob.position())))continue;
                var candidate=mob.getNavigation().createPath(pos,0);
                if(candidate!=null&&candidate.canReach()){path=candidate;return true;}
            }
            return false;
        }
        @Override public void start(){time=0;mob.stopUsingItem();mob.getNavigation().moveTo(path,1.1);SkeletonState.runtime(mob).shelters++;}
        @Override public boolean canContinueToUse(){return allowed()&&!mob.getNavigation().isDone()&&time<80;}
        @Override public boolean requiresUpdateEveryTick(){return true;}
        @Override public void tick(){time++;}
        @Override public void stop(){mob.getNavigation().stop();path=null;nextSearch=mob.tickCount+100;}
    }
}
