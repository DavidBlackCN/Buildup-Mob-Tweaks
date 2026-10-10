package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.riding.RidingEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(LivingEntity.class)
public abstract class SkeletonWitherConversionMixin {
    @Inject(method="die(Lnet/minecraft/world/damagesource/DamageSource;)V",at=@At("HEAD"),cancellable=true)
    private void buildup$convert(DamageSource source,CallbackInfo ci){if(RidingEvents.convert((LivingEntity)(Object)this,source))ci.cancel();}
}
