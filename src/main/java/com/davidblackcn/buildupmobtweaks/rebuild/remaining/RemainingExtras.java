/* Mob AI Tweaks adaptation: Copyright (c) 2024 N0t_UN_Owen, MIT. See NOTICE.md. */
package com.davidblackcn.buildupmobtweaks.rebuild.remaining;
import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.entity.projectile.arrow.*;
import net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.AbstractWindCharge;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Items;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.random.WeightedList;
public final class RemainingExtras {
    public static void explode(Creeper m){var behavior=RemainingBehavior.instance();if(behavior==null||!(m.level() instanceof ServerLevel l))return;
        if(behavior.enabled(m,FeatureId.CREEPER_EMBEDDED_ARROWS)){int n=Math.min(32,m.getArrowCount());for(int i=0;i<n;i++){
            var b=new Arrow(l,m,Items.ARROW.getDefaultInstance(),null);b.pickup=AbstractArrow.Pickup.DISALLOWED;b.setCritArrow(true);b.setPos(m.getX(),m.getY()+.5,m.getZ());
            b.shoot(m.getRandom().nextGaussian(),.2+m.getRandom().nextDouble()*.6,m.getRandom().nextGaussian(),m.isPowered()?1.2f:.6f,0);b.setRemainingFireTicks(m.getRemainingFireTicks());if(l.addFreshEntity(b))RemainingState.runtime(m).arrows++;}m.setArrowCount(0);}
        if(behavior.enabled(m,FeatureId.CREEPER_FIRE_VISUAL)&&m.isOnFire())l.sendParticles(ParticleTypes.FLAME,m.getX(),m.getY(),m.getZ(),12,.4,.5,.4,.1);
    }
    public static void breeze(Breeze m,boolean jumping){var b=RemainingBehavior.instance();if(b==null||!(m.level() instanceof ServerLevel l)||l.getDifficulty()!=net.minecraft.world.Difficulty.HARD||m.isNoAi()||!m.isAlive())return;
        var id=jumping?FeatureId.BREEZE_TAKEOFF_BURST:FeatureId.BREEZE_LANDING_BURST;if(!b.enabled(m,id))return;String key=jumping?"takeoff_ready":"landing_ready";var d=m.getAttached(RemainingState.DATA);if(l.getGameTime()<d.getLongOr(key,0))return;d.putLong(key,l.getGameTime()+100);
        l.explode(m,m.damageSources().windCharge(m,m),AbstractWindCharge.EXPLOSION_DAMAGE_CALCULATOR,m.getX(),m.getY(),m.getZ(),2.5f,false,
            l.getGameRules().get(GameRules.MOB_GRIEFING)?Level.ExplosionInteraction.TRIGGER:Level.ExplosionInteraction.NONE,
            ParticleTypes.GUST_EMITTER_SMALL,ParticleTypes.GUST_EMITTER_LARGE,WeightedList.of(),SoundEvents.WIND_CHARGE_BURST);RemainingState.runtime(m).bursts++;
    }
    private RemainingExtras(){}
}
