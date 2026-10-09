/* Mob AI Tweaks adaptation: Copyright (c) 2024 N0t_UN_Owen, MIT. See NOTICE.md. */
package com.davidblackcn.buildupmobtweaks.rebuild.witch;
import com.davidblackcn.buildupmobtweaks.feature.*;
import com.davidblackcn.buildupmobtweaks.rebuild.raid.*;
import com.davidblackcn.buildupmobtweaks.rebuild.zombie.ZombieBehavior;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion;
import java.util.EnumSet;

public final class WitchBehavior {
    private static WitchBehavior instance;private final FeatureRegistry features;
    public WitchBehavior(FeatureRegistry f){features=f;}public static WitchBehavior instance(){return instance;}
    public boolean enabled(Witch m,FeatureId f){return RaidState.known(m,RaidState.WITCH)&&RaidGate.enabled(m,features,f);}
    public void register(){instance=this;
        ServerEntityEvents.ENTITY_LOAD.register((e,l)->{if(e instanceof Witch m&&RaidGate.supported(m)){RaidState.initialize(m,RaidState.WITCH);if(!RaidState.known(m,RaidState.WITCH))return;clear(m,true);
            var r=RaidState.runtime(m);if(!r.installed){r.installed=true;m.getGoalSelector().addGoal(1,new Preview(m));}}});
        ServerEntityEvents.ENTITY_UNLOAD.register((e,l)->{if(e instanceof Witch m&&RaidState.known(m,RaidState.WITCH))clear(m,true);});
        ServerLivingEntityEvents.ALLOW_DEATH.register((e,s,a)->{if(e instanceof Witch m&&RaidState.known(m,RaidState.WITCH))clear(m,true);return true;});
    }
    public boolean blocked(Witch m,LivingEntity target){return !(target instanceof Raider)&&enabled(m,FeatureId.WITCH_THROW_COOLDOWN)&&m.level().getGameTime()<m.getAttached(RaidState.WITCH).getLongOr("throw_ready",0);}
    public boolean defer(Witch m,LivingEntity t,ItemStack actual){
        if(!enabled(m,FeatureId.WITCH_WINDUP)||t instanceof Raider||!ZombieBehavior.valid(m,t)||m.isDrinkingPotion())return false;
        var r=RaidState.runtime(m);if(r.preview)return true;if(!m.getMainHandItem().isEmpty())return false;
        r.preview=true;r.target=t;r.end=m.level().getGameTime()+features.witchWindupTicks();r.previews++;
        m.setItemSlot(EquipmentSlot.MAINHAND,TemporaryItem.mark(actual,"preview"));return true;
    }
    public void thrown(Witch m){if(!RaidState.known(m,RaidState.WITCH))return;RaidState.runtime(m).throwsMade++;
        if(enabled(m,FeatureId.WITCH_THROW_COOLDOWN))m.getAttached(RaidState.WITCH).putLong("throw_ready",m.level().getGameTime()+features.witchCooldownTicks());}
    public void jump(Witch m,LivingEntity target){if(enabled(m,FeatureId.WITCH_JUMP_THROW)&&ZombieBehavior.valid(m,target)&&m.hasEffect(MobEffects.JUMP_BOOST)&&m.onGround()&&target.getY()>m.getY()+1){m.getJumpControl().jump();RaidState.runtime(m).jumps++;}}
    public boolean leap(Witch m){return !m.isNoAi()&&m.isAlive()&&enabled(m,FeatureId.WITCH_LEAPING_POTION)&&ZombieBehavior.valid(m,m.getTarget())&&m.onGround()&&m.getTarget().getY()>m.getY()+1&&!m.hasEffect(MobEffects.JUMP_BOOST)&&m.getMainHandItem().isEmpty();}
    public void beforeAi(Witch m){if(!RaidState.known(m,RaidState.WITCH))return;var r=RaidState.runtime(m);
        boolean active=enabled(m,FeatureId.WITCH_WINDUP)||enabled(m,FeatureId.WITCH_THROW_COOLDOWN)||enabled(m,FeatureId.WITCH_LEAPING_POTION)||enabled(m,FeatureId.WITCH_JUMP_THROW);
        var t=m.getTarget();if(active&&t!=null&&!(t instanceof Raider)&&(!ZombieBehavior.valid(m,t)||m.distanceToSqr(t)>Math.pow(m.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.FOLLOW_RANGE),2))){m.setTarget(null);m.getNavigation().stop();}
        if(r.preview&&(!enabled(m,FeatureId.WITCH_WINDUP)||m.isNoAi()||!m.isAlive()||m.getTarget()!=r.target||!ZombieBehavior.valid(m,r.target)||!TemporaryItem.is(m.getMainHandItem(),"preview")||m.isDrinkingPotion()))clear(m,false);
        if(!enabled(m,FeatureId.WITCH_LEAPING_POTION)&&TemporaryItem.is(m.getMainHandItem(),"leap"))clear(m,true);
    }
    public void clear(Witch m,boolean drinking){var r=RaidState.runtime(m);r.preview=false;r.target=null;
        if(TemporaryItem.is(m.getMainHandItem(),"preview")||drinking&&TemporaryItem.is(m.getMainHandItem(),"leap")){if(m.isDrinkingPotion())((WitchAccess)m).buildup$cancelDrink();m.setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);}}
    private final class Preview extends Goal {
        private final Witch m;Preview(Witch m){this.m=m;setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
        public boolean canUse(){return RaidState.runtime(m).preview;}public boolean canContinueToUse(){return canUse();}public boolean requiresUpdateEveryTick(){return true;}
        public void tick(){beforeAi(m);var r=RaidState.runtime(m);if(!r.preview)return;m.getNavigation().stop();m.getLookControl().setLookAt(r.target,30,30);
            if(m.level().getGameTime()<r.end)return;if(!m.hasLineOfSight(r.target)){clear(m,false);return;}
            var t=r.target;var stack=m.getMainHandItem();m.setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);r.preview=false;r.target=null;jump(m,t);
            double xd=t.getX()+t.getDeltaMovement().x-m.getX(),zd=t.getZ()+t.getDeltaMovement().z-m.getZ(),yd=t.getEyeY()-1.1-m.getY();double distance=Math.sqrt(xd*xd+zd*zd);
            Projectile.spawnProjectileUsingShoot(ThrownSplashPotion::new,(ServerLevel)m.level(),stack,m,xd,yd+distance*.2,zd,distance<=2?.45f:.75f,8);
            m.playSound(SoundEvents.WITCH_THROW,1,.8f+m.getRandom().nextFloat()*.4f);thrown(m);
        }
        public void stop(){clear(m,false);}
    }
}
