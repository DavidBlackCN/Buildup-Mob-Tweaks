package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.general.*;
import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(TargetingConditions.class)
public abstract class E4ConditionsMixin implements DiscoveryContext {
    @Shadow private double range;
    @Unique private Mob buildup$source;
    public void buildup$discoverer(Mob m){buildup$source=m;}
    @Inject(method="test(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/LivingEntity;)Z",at=@At("HEAD"),cancellable=true)
    private void buildup$limit(ServerLevel l,LivingEntity source,LivingEntity t,CallbackInfoReturnable<Boolean> ci){
        if(source!=null&&source==buildup$source&&buildup$source.getTarget()==null&&range>0){double r=E4Behavior.discoveryRange(buildup$source,t,range);if(source.distanceToSqr(t)>r*r)ci.setReturnValue(false);}
    }
    @ModifyExpressionValue(method="test(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/LivingEntity;)Z",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/ai/sensing/Sensing;hasLineOfSight(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean buildup$glow(boolean original,ServerLevel l,LivingEntity source,LivingEntity t){
        return original||source!=null&&source==buildup$source&&buildup$source.getTarget()==null&&range>0&&t.isCurrentlyGlowing()
            &&E4Behavior.enabled(buildup$source,FeatureId.SENSE_GLOWING)&&!buildup$source.hasEffect(MobEffects.BLINDNESS)
            &&source.distanceToSqr(t)<=Math.pow(Math.min(16,range),2);
    }
}
