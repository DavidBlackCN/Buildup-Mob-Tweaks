package com.davidblackcn.buildupmobtweaks.rebuild.ecology;

import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.effect.*;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.Blocks;
import java.util.*;

/** All actions are started and stopped by native GoalSelector scheduling. */
public final class EcologyGoals {
    public static final class Escape extends Goal {
        private final Chicken chicken; private final EcologyBehavior behavior; private final boolean players;
        private LivingEntity threat; private Vec3 destination; private long next;
        Escape(Chicken c, EcologyBehavior b, boolean p) { chicken=c;behavior=b;players=p;setFlags(EnumSet.of(Flag.MOVE)); }
        private boolean valid(LivingEntity e) {
            return EcologyBehavior.livingValid(e) && (players ? e instanceof Player p && !chicken.isFood(p.getMainHandItem()) && !chicken.isFood(p.getOffhandItem()) : e instanceof Monster);
        }
        public boolean canUse() {
            if (!behavior.enabled(chicken,FeatureId.CHICKEN_THREAT_ESCAPE) || chicken.isPassenger() || chicken.level().getGameTime()<next) return false;
            next=chicken.level().getGameTime()+10;
            threat=EcologyBehavior.nearby(chicken,LivingEntity.class,players?8:6,this::valid,16).stream().min(Comparator.comparingDouble(chicken::distanceToSqr)).orElse(null);
            destination=threat==null?null:DefaultRandomPos.getPosAway(chicken,12,4,threat.position());
            return destination!=null && destination.distanceToSqr(threat.position())>chicken.distanceToSqr(threat);
        }
        public boolean canContinueToUse() { return behavior.enabled(chicken,FeatureId.CHICKEN_THREAT_ESCAPE) && !chicken.isPassenger() && threat!=null && valid(threat) && chicken.distanceToSqr(threat)<Math.pow(players?10:8,2) && !chicken.getNavigation().isDone(); }
        public void start() { chicken.getNavigation().moveTo(destination.x,destination.y,destination.z,.8);EcologyState.runtime(chicken).escapes++; }
        public void tick() { chicken.getNavigation().setSpeedModifier(chicken.distanceToSqr(threat)<16?1.2:.8); }
        public void stop() { chicken.getNavigation().stop();threat=null;destination=null; }
    }
    public static final class GroupPanic extends Goal {
        private final PathfinderMob mob; private final EcologyBehavior behavior; private Vec3 destination;
        GroupPanic(PathfinderMob m,EcologyBehavior b){mob=m;behavior=b;setFlags(EnumSet.of(Flag.MOVE));}
        public boolean canUse(){var r=EcologyState.runtime(mob);if(!behavior.enabled(mob,FeatureId.ANIMAL_GROUP_PANIC)||mob.isPassenger()||mob.level().getGameTime()>=r.panicUntil||r.threat==null)return false;
            destination=DefaultRandomPos.getPosAway(mob,12,4,r.threat);return destination!=null;}
        public boolean canContinueToUse(){return behavior.enabled(mob,FeatureId.ANIMAL_GROUP_PANIC)&&mob.level().getGameTime()<EcologyState.runtime(mob).panicUntil&&!mob.isPassenger()&&!mob.getNavigation().isDone();}
        public void start(){mob.getNavigation().moveTo(destination.x,destination.y,destination.z,1.25);EcologyState.runtime(mob).escapes++;}
        public void stop(){mob.getNavigation().stop();var r=EcologyState.runtime(mob);r.panicUntil=0;r.threat=null;destination=null;}
    }
    public static final class Dodge extends Goal {
        private final Cat cat;private final EcologyBehavior behavior;private Vec3 dodge;private long until,nextScan;
        Dodge(Cat c,EcologyBehavior b){cat=c;behavior=b;setFlags(EnumSet.of(Flag.MOVE,Flag.JUMP,Flag.LOOK));}
        public boolean canUse(){
            if(!behavior.enabled(cat,FeatureId.CAT_PROJECTILE_DODGE)||cat.isOrderedToSit()||cat.isInSittingPose()||cat.isPassenger()||!cat.onGround()||!EcologyState.ready(cat,"dodge_after")||cat.level().getGameTime()<nextScan)return false;
            nextScan=cat.level().getGameTime()+2;
            for(var p:EcologyBehavior.nearby(cat,Projectile.class,8,e->e.isAlive()&&e.getOwner()!=cat&&e.getDeltaMovement().lengthSqr()>.01,8)){
                Vec3 velocity=p.getDeltaMovement(),delta=cat.getBoundingBox().getCenter().subtract(p.position());double time=delta.dot(velocity)/velocity.lengthSqr();
                if(time<=0||time>8||delta.subtract(velocity.scale(time)).lengthSqr()>1)continue;
                Vec3 side=new Vec3(-velocity.z,0,velocity.x).normalize().scale(.35);
                if(safe(side)){dodge=side;return true;}if(safe(side.scale(-1))){dodge=side.scale(-1);return true;}
            }return false;
        }
        private boolean safe(Vec3 side){
            // Check the full bounded horizontal impulse corridor, not merely the first step.
            for(int i=1;i<=8;i++){var offset=side.scale(i);var pos=BlockPos.containing(cat.position().add(offset));
                if(!cat.level().isLoaded(pos)||!cat.level().isLoaded(pos.below())||!cat.level().getBlockState(pos.below()).isSolidRender()||!cat.level().getFluidState(pos).isEmpty()||!cat.level().noCollision(cat,cat.getBoundingBox().move(offset)))return false;
                var floor=cat.level().getBlockState(pos.below());var feet=cat.level().getBlockState(pos);
                if(floor.is(Blocks.MAGMA_BLOCK)||floor.is(Blocks.CAMPFIRE)||floor.is(Blocks.SOUL_CAMPFIRE)||feet.is(Blocks.FIRE)||feet.is(Blocks.SOUL_FIRE)||feet.is(Blocks.SWEET_BERRY_BUSH)||feet.is(Blocks.POWDER_SNOW))return false;
            }return true;
        }
        public void start(){until=cat.level().getGameTime()+8;EcologyState.cooldown(cat,"dodge_after",40);cat.getNavigation().stop();cat.getJumpControl().jump();cat.setDeltaMovement(dodge.add(0,.3,0));EcologyState.runtime(cat).dodges++;}
        public boolean canContinueToUse(){return behavior.enabled(cat,FeatureId.CAT_PROJECTILE_DODGE)&&!cat.isOrderedToSit()&&!cat.isPassenger()&&cat.level().getGameTime()<until;}
        public void stop(){dodge=null;}
    }
    public static final class Defense extends Goal {
        private final Mob trader;private final EcologyBehavior behavior;private LivingEntity attacker;private long release;
        Defense(Mob m,EcologyBehavior b){trader=m;behavior=b;setFlags(EnumSet.of(Flag.LOOK));}
        private boolean valid(){return behavior.enabled(trader,FeatureId.TRADER_SELF_DEFENSE)&&attacker!=null&&EcologyBehavior.livingValid(attacker)&&attacker.level()==trader.level()&&trader.canAttack(attacker)&&trader.distanceToSqr(attacker)<=64&&trader.hasLineOfSight(attacker);}
        public boolean canUse(){var r=EcologyState.runtime(trader);attacker=r.attacker;return trader.level().getGameTime()<r.defenseUntil&&EcologyState.ready(trader,"defense_after")&&valid();}
        public void start(){release=trader.level().getGameTime()+20;EcologyState.cooldown(trader,"defense_after",200);}
        public boolean canContinueToUse(){return valid()&&trader.level().getGameTime()<=release;}
        public boolean requiresUpdateEveryTick(){return true;}
        public void tick(){trader.getLookControl().setLookAt(attacker,60,60);if(trader.level().getGameTime()==release&&valid()){attacker.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,100));EcologyState.runtime(trader).defenses++;}}
        public void stop(){EcologyState.runtime(trader).attacker=null;attacker=null;}
    }
    private EcologyGoals(){}
}
