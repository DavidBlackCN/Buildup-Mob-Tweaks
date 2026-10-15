package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.villager.profession.*;
import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Projectile.class)
public abstract class WorkProjectileLifecycleMixin {
    @Inject(method="canHitEntity",at=@At("RETURN"),cancellable=true)
    private void buildup$trainingNeverHitsEntities(net.minecraft.world.entity.Entity target,org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> ci){if(((Projectile)(Object)this).hasAttached(WorkProjectiles.TRAINING))ci.setReturnValue(false);}
    @Inject(method="tick()V",at=@At("TAIL"))
    private void buildup$expireKnownWorkProjectile(CallbackInfo ci){var p=(Projectile)(Object)this;if(p.level().isClientSide())return;for(var type:java.util.List.of(WorkProjectiles.POTION,WorkProjectiles.TRAINING))if(p.hasAttached(type)){var d=p.getAttached(type);if(d.getIntOr("version",-1)==1&&!WorkProjectiles.valid(p,d))p.discard();}}
}
