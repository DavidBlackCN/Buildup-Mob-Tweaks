/* Mob AI Tweaks adaptation: Copyright (c) 2024 N0t_UN_Owen, MIT. See NOTICE.md. */
package com.davidblackcn.buildupmobtweaks.rebuild.profession;

import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import com.davidblackcn.buildupmobtweaks.rebuild.zombie.ZombieBehavior;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.zombie.*;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.Vec3;
import java.util.*;

/** Narrow adaptations of native attack state machines, scheduled only by the original GoalSelector. */
public final class ProfessionGoals {
    abstract static class Attack extends Goal {
        final Mob mob;final ProfessionBehavior behavior;
        Attack(Mob m,ProfessionBehavior b){mob=m;behavior=b;setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
        boolean usable(){return mob.isAlive()&&!mob.isNoAi()&&(!(mob instanceof ZombieVillager z)||!z.isConverting());}
        public boolean requiresUpdateEveryTick(){return true;}
        public void stop(){behavior.cancel(mob);mob.getNavigation().stop();}
        void look(LivingEntity target){mob.getLookControl().setLookAt(target,30,30);}
    }
    public static final class Crossbow extends Attack {
        enum Stage {UNCHARGED,CHARGING,CHARGED,READY}
        Stage stage;int sight,delay,pathDelay;
        Crossbow(Mob m,ProfessionBehavior b){super(m,b);}
        public boolean canUse(){return usable()&&behavior.enabled(mob,behavior.crossbowFeature(mob))&&ProfessionBehavior.hand(mob,Items.CROSSBOW)!=null&&ZombieBehavior.valid(mob,mob.getTarget());}
        public boolean canContinueToUse(){return usable()&&behavior.activeValid(mob)&&behavior.enabled(mob,behavior.crossbowFeature(mob));}
        public void start(){var hand=ProfessionBehavior.hand(mob,Items.CROSSBOW);behavior.begin(mob,behavior.crossbowFeature(mob),"crossbow",hand,mob.getTarget());
            stage=CrossbowItem.isCharged(mob.getItemInHand(hand))?Stage.CHARGED:Stage.UNCHARGED;delay=20+mob.getRandom().nextInt(20);sight=0;pathDelay=0;}
        public void tick(){if(!canContinueToUse())return;var r=ProfessionState.runtime(mob);var target=r.target;boolean los=mob.hasLineOfSight(target);
            if(los!=(sight>0))sight=0;sight+=los?1:-1;look(target);
            boolean move=(mob.distanceToSqr(target)>64||sight<5)&&stage!=Stage.CHARGED;
            if(move){if(--pathDelay<=0){mob.getNavigation().moveTo(target,stage==Stage.UNCHARGED?1:.5);pathDelay=20+mob.getRandom().nextInt(21);}}
            else {pathDelay=0;mob.getNavigation().stop();}
            if(stage==Stage.UNCHARGED){if(!move&&!mob.isUsingItem()){mob.startUsingItem(r.hand);stage=Stage.CHARGING;}}
            else if(stage==Stage.CHARGING){
                if(!mob.isUsingItem()||mob.getUseItem()!=r.stack){stage=Stage.UNCHARGED;return;}
                if(mob.getTicksUsingItem()>=CrossbowItem.getChargeDuration(r.stack,mob)&&CrossbowItem.isCharged(r.stack)){
                    mob.releaseUsingItem();stage=Stage.CHARGED;delay=20+mob.getRandom().nextInt(20);
                }
            }else if(stage==Stage.CHARGED){if(--delay<=0)stage=Stage.READY;}
            else if(los&&mob.distanceToSqr(target)<=64){
                if(CrossbowItem.isCharged(r.stack)){
                    ((CrossbowItem)r.stack.getItem()).performShooting(mob.level(),mob,r.hand,r.stack,.9f,14-mob.level().getDifficulty().getId()*4,target);
                    r.shots++;mob.swingForAttack(r.hand);
                }stage=Stage.UNCHARGED;
            }
        }
    }
    public static final class Fishing extends Attack {
        Fishing(Mob m,ProfessionBehavior b){super(m,b);}
        public boolean canUse(){var t=mob.getTarget();double d=t==null?0:mob.distanceToSqr(t);
            return usable()&&behavior.canFish(mob)&&ZombieBehavior.valid(mob,t)&&mob.hasLineOfSight(t)&&d>36&&d<169
                    &&(!(mob instanceof Drowned)||!t.isUnderWater())&&ProfessionBehavior.hook(mob)==null
                    &&mob.level().getGameTime()>=mob.getAttached(ProfessionState.DATA).getLongOr("rod_ready",Long.MAX_VALUE)&&!mob.isUsingItem();}
        public boolean canContinueToUse(){return usable()&&behavior.activeValid(mob)&&behavior.canFish(mob)&&ProfessionBehavior.hook(mob)!=null;}
        public void start(){var target=mob.getTarget();var hand=ProfessionBehavior.hand(mob,Items.FISHING_ROD);
            behavior.begin(mob,behavior.rodFeature(mob),"fishing",hand,target);var r=ProfessionState.runtime(mob);
            mob.getAttached(ProfessionState.DATA).putLong("rod_ready",mob.level().getGameTime()+60);mob.getNavigation().stop();look(target);
            var h=new FishingHook(EntityTypes.FISHING_BOBBER,mob.level());h.setAttached(ProfessionState.HOOK,true);h.setOwner(mob);r.hook=h;
            Vec3 origin=mob.getEyePosition();Vec3 delta=target.getEyePosition().add(target.getDeltaMovement()).subtract(origin).normalize();
            h.setPos(origin.add(delta));h.setDeltaMovement(delta.scale(2));
            if(!mob.level().addFreshEntity(h)){h.discard();behavior.cancel(mob);return;}
            mob.startUsingItem(hand);mob.swingForAttack(hand);r.casts++;
        }
        public void tick(){if(canContinueToUse()){mob.getNavigation().stop();look(ProfessionState.runtime(mob).target);}}
    }
    public static final class Cleric extends Attack {
        LivingEntity candidate;long scan,release;
        Cleric(Mob m,ProfessionBehavior b){super(m,b);}
        public boolean canUse(){
            if(!usable()||!behavior.enabled(mob,FeatureId.ZOMBIE_CLERIC_POTIONS)||ProfessionBehavior.hand(mob,Items.SPLASH_POTION)==null
                    ||mob.isUsingItem()||mob.level().getGameTime()<mob.getAttached(ProfessionState.DATA).getLongOr("potion_ready",Long.MAX_VALUE))return false;
            long now=mob.level().getGameTime();if(now<scan)return false;scan=now+20;
            candidate=mob.level().getEntitiesOfClass(Monster.class,mob.getBoundingBox().inflate(12)).stream().limit(32)
                    .filter(t->ProfessionBehavior.healable(mob,t)&&mob.distanceToSqr(t)<=144&&mob.hasLineOfSight(t))
                    .min(Comparator.comparingDouble(mob::distanceToSqr)).orElse(null);
            var h=ProfessionBehavior.hand(mob,Items.SPLASH_POTION);var contents=mob.getItemInHand(h).get(net.minecraft.core.component.DataComponents.POTION_CONTENTS);
            return candidate!=null&&contents!=null&&contents.potion().map(p->p.is(net.minecraft.world.item.alchemy.Potions.HARMING)||p.is(net.minecraft.world.item.alchemy.Potions.STRONG_HARMING)).orElse(false);
        }
        public boolean canContinueToUse(){return usable()&&behavior.activeValid(mob)&&mob.distanceToSqr(ProfessionState.runtime(mob).target)<=144&&mob.hasLineOfSight(ProfessionState.runtime(mob).target);}
        public void start(){behavior.begin(mob,FeatureId.ZOMBIE_CLERIC_POTIONS,"potion",ProfessionBehavior.hand(mob,Items.SPLASH_POTION),candidate);release=mob.level().getGameTime()+20;
            mob.getAttached(ProfessionState.DATA).putLong("potion_ready",mob.level().getGameTime()+60);mob.getNavigation().stop();}
        public void tick(){if(!canContinueToUse())return;var r=ProfessionState.runtime(mob);look(r.target);mob.getNavigation().stop();
            if(mob.level().getGameTime()<release)return;
            var consumed=r.stack.split(1);var p=new ThrownSplashPotion(mob.level(),mob,consumed);
            double dx=r.target.getX()-p.getX(),dz=r.target.getZ()-p.getZ();
            p.shoot(dx,r.target.getY(.5)-p.getY()+Math.sqrt(dx*dx+dz*dz)*.2,dz,.75f,0);
            var d=new CompoundTag();d.putInt("version",1);d.putString("owner",mob.getUUID().toString());d.putString("target",r.target.getUUID().toString());d.putLong("expires",mob.level().getGameTime()+60);p.setAttached(ProfessionState.POTION,d);
            if(mob.level().addFreshEntity(p)){behavior.track(p);r.potions++;mob.swingForAttack(r.hand);}else{r.stack.grow(1);p.discard();}
            behavior.cancel(mob);
        }
    }
    private ProfessionGoals(){}
}
