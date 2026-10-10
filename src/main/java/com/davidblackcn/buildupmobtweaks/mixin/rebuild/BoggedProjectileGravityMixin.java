package com.davidblackcn.buildupmobtweaks.mixin.rebuild;

import com.davidblackcn.buildupmobtweaks.rebuild.skeleton.SkeletonDifferences;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ThrowableProjectile.class)
public abstract class BoggedProjectileGravityMixin {
    @Inject(method="getDefaultGravity()D",at=@At("RETURN"),cancellable=true)
    private void buildup$mushroomGravity(CallbackInfoReturnable<Double> ci){
        if((Object)this instanceof Snowball ball&&ball.hasAttached(SkeletonDifferences.MUSHROOM)&&SkeletonDifferences.allowed(ball))ci.setReturnValue(.05);
    }
}
