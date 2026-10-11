/* Mob AI Tweaks adaptation: Copyright (c) 2024 N0t_UN_Owen, MIT. See NOTICE.md. */
package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.general.E4Behavior;
import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Projectile.class)
public abstract class E4ProjectileMixin {
    @ModifyVariable(method="shoot(DDDFF)V",at=@At("HEAD"),argsOnly=true,ordinal=1)
    private float buildup$nausea(float original){
        var p=(Projectile)(Object)this;
        if(Float.isFinite(original)&&original>=0&&!p.level().isClientSide()&&p.getOwner() instanceof Mob m&&E4Behavior.enabled(m,FeatureId.SENSE_NAUSEA)&&m.hasEffect(MobEffects.NAUSEA))
            return original+(float)Math.sqrt(Math.clamp(m.getEffect(MobEffects.NAUSEA).getAmplifier(),0,255)/255.0)*122.5f+10;
        return original;
    }
    @Inject(method="tick()V",at=@At("TAIL")) private void buildup$burn(CallbackInfo ci){
        var p=(Projectile)(Object)this;
        if(p.level() instanceof ServerLevel l&&p.tickCount%4==0&&p.isOnFire()&&p.getOwner() instanceof Mob m&&E4Behavior.enabled(m,FeatureId.BURNING_PROJECTILE_VISUAL))
            l.sendParticles(ParticleTypes.FLAME,p.getX(),p.getY(),p.getZ(),1,.03,.03,.03,0);
    }
}
