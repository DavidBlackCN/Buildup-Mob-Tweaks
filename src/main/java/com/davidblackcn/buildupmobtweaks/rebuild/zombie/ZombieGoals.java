/* Behavioral reference: Mob AI Tweaks, Copyright (c) 2024 N0t_UN_Owen, MIT. */
package com.davidblackcn.buildupmobtweaks.rebuild.zombie;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.*;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.Vec3;

public final class ZombieGoals {
    private ZombieGoals(){}
    public static Vec3 safeSurface(Zombie m,Vec3 anchor){
        for(int y=0;y<=8;y++)for(int radius=0;radius<=4;radius++)for(int a=0;a<8;a++){
            double angle=a*Math.PI/4;var p=anchor.add(Math.round(Math.cos(angle)*radius),y,Math.round(Math.sin(angle)*radius));var block=BlockPos.containing(p);
            if(m.level().hasChunkAt(block)&&m.level().getBlockState(block.below()).isSolidRender()&&m.level().noCollision(m,m.getBoundingBox().move(p.subtract(m.position()))))return p;
        }
        return null;
    }
    public static void restore(Zombie m){
        if(!ZombieState.known(m))return;var d=m.getAttached(ZombieState.DATA);if(!d.getBooleanOr("burrow_active",false))return;
        var anchor=new Vec3(d.getDoubleOr("restore_x",m.getX()),d.getDoubleOr("restore_y",m.getY()),d.getDoubleOr("restore_z",m.getZ()));var surface=safeSurface(m,anchor);
        if(surface==null){
            for(int radius=0;radius<=16&&surface==null;radius++)for(int a=0;a<8&&surface==null;a++){
                int x=(int)Math.floor(anchor.x+Math.cos(a*Math.PI/4)*radius),z=(int)Math.floor(anchor.z+Math.sin(a*Math.PI/4)*radius);
                if(!m.level().hasChunkAt(new BlockPos(x,(int)anchor.y,z)))continue;
                int y=m.level().getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,x,z);
                var candidate=new Vec3(x+.5,y,z+.5);
                if(m.level().noCollision(m,m.getBoundingBox().move(candidate.subtract(m.position()))))surface=candidate;
            }
            if(surface==null){var runtime=ZombieState.runtime(m);if(!runtime.mode.equals("recover"))BuildupMobTweaks.LOGGER.warn("Husk {} has no loaded safe surface; retrying recovery next tick",m.getUUID());runtime.mode="recover";return;}
        }
        m.setPos(surface);m.noPhysics=d.getBooleanOr("old_no_physics",false);m.setNoGravity(d.getBooleanOr("old_no_gravity",false));m.setSprinting(false);m.setDeltaMovement(Vec3.ZERO);
        d.putBoolean("burrow_active",false);ZombieState.runtime(m).mode="vanilla";
    }
    static final class Guard extends Goal {
        final Zombie mob;final ZombieBehavior behavior;long until,next;ItemStack held;
        Guard(Zombie m,ZombieBehavior b){mob=m;behavior=b;setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
        @Override public boolean canUse(){return behavior.enabled(mob,FeatureId.ZOMBIE_SHIELD_USE)&&!mob.isBaby()&&!mob.isUsingItem()
                &&mob.getOffhandItem().is(Items.SHIELD)&&ZombieBehavior.valid(mob,mob.getTarget())&&mob.hasLineOfSight(mob.getTarget())&&mob.distanceToSqr(mob.getTarget())<=9&&mob.level().getGameTime()>=next;}
        @Override public void start(){held=mob.getOffhandItem();mob.startUsingItem(InteractionHand.OFF_HAND);until=mob.level().getGameTime()+15;
            next=mob.level().getGameTime()+80;ZombieState.runtime(mob).guards++;ZombieState.runtime(mob).mode="guard";}
        @Override public boolean canContinueToUse(){return mob.level().getGameTime()<until&&mob.getOffhandItem()==held&&mob.isUsingItem()
                &&behavior.enabled(mob,FeatureId.ZOMBIE_SHIELD_USE)&&ZombieBehavior.valid(mob,mob.getTarget())&&mob.hasLineOfSight(mob.getTarget());}
        @Override public void tick(){mob.getNavigation().stop();mob.lookAt(mob.getTarget(),60,60);mob.getLookControl().setLookAt(mob.getTarget(),60,60);}
        @Override public boolean requiresUpdateEveryTick(){return true;}
        @Override public void stop(){if(mob.getUseItem()==held)mob.stopUsingItem();ZombieState.runtime(mob).mode="vanilla";}
    }
    static final class Burrow extends Goal {
        final Zombie mob;final ZombieBehavior behavior;Vec3 origin,destination;int step;boolean done;
        Burrow(Zombie m,ZombieBehavior b){mob=m;behavior=b;setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK,Flag.JUMP));}
        private boolean permitted(){return behavior.enabled(mob,FeatureId.HUSK_SAND_BURROW)&&!mob.isBaby()&&!mob.isPassenger()&&!mob.isVehicle()
                &&!mob.isInWater()&&((net.minecraft.server.level.ServerLevel)mob.level()).getGameRules().get(GameRules.MOB_GRIEFING)&&ZombieBehavior.valid(mob,mob.getTarget())
                &&(!mob.hasEffect(MobEffects.MINING_FATIGUE)||mob.hasEffect(MobEffects.HASTE));}
        private boolean sandRoute(Vec3 from,Vec3 to){
            int segments=(int)Math.ceil(from.distanceTo(to)*2);if(segments>64)return false;
            for(int i=0;i<=segments;i++){var pos=BlockPos.containing(from.lerp(to,(double)i/Math.max(1,segments)));
                if(!mob.level().hasChunkAt(pos))return false;
                for(int y=1;y<=3;y++)if(!mob.level().getBlockState(pos.below(y)).is(BlockTags.SAND))return false;
                if(!mob.level().noCollision(mob,mob.getBoundingBox().move(Vec3.atBottomCenterOf(pos).subtract(mob.position()))))return false;
            }
            return true;
        }
        @Override public boolean canUse(){
            if(!permitted()||mob.noPhysics||mob.isNoGravity()||mob.distanceTo(mob.getTarget())<=12||mob.level().getGameTime()<mob.getAttached(ZombieState.DATA).getLongOr("burrow_ready",0))return false;
            origin=mob.position();destination=mob.getTarget().position();return Math.abs(origin.y-destination.y)<.5&&sandRoute(origin,destination);
        }
        @Override public void start(){
            step=0;done=false;var d=mob.getAttached(ZombieState.DATA);d.putBoolean("burrow_active",true);d.putDouble("restore_x",origin.x);d.putDouble("restore_y",origin.y);d.putDouble("restore_z",origin.z);
            d.putBoolean("old_no_physics",mob.noPhysics);d.putBoolean("old_no_gravity",mob.isNoGravity());d.putLong("burrow_ready",mob.level().getGameTime()+BuildupMobTweaks.config().hostile.zombie.burrowCooldown.get());
            mob.getNavigation().stop();mob.stopUsingItem();mob.noPhysics=true;mob.setNoGravity(true);mob.setSprinting(true);ZombieState.runtime(mob).burrows++;ZombieState.runtime(mob).mode="burrow";
        }
        @Override public boolean canContinueToUse(){return !done&&step<40&&permitted();}
        @Override public void tick(){
            if(!permitted()){done=true;return;}mob.setDeltaMovement(Vec3.ZERO);mob.swingForAttack(InteractionHand.MAIN_HAND);
            ++step;double size=mob.getScale()*mob.getAgeScale();
            if(step<20)mob.setPos(origin.add(0,-.16*size*step,0));
            else if(step==20){if(!sandRoute(origin,destination)){done=true;return;}mob.setPos(destination.add(0,-2.9*size,0));}
            else if(step<40)mob.setPos(destination.add(0,-2.9*size+.16*size*(step-20),0));
            else{var d=mob.getAttached(ZombieState.DATA);d.putDouble("restore_x",destination.x);d.putDouble("restore_y",destination.y);d.putDouble("restore_z",destination.z);done=true;}
        }
        @Override public void stop(){restore(mob);mob.getAttached(ZombieState.DATA).putLong("burrow_ready",mob.level().getGameTime()+BuildupMobTweaks.config().hostile.zombie.burrowCooldown.get());done=true;}
    }
}
