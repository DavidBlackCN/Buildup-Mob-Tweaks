package com.davidblackcn.buildupmobtweaks.mixin;
import com.davidblackcn.buildupmobtweaks.combat.SkeletonExtras;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Snowball.class)
public abstract class MushroomProjectileMixin {
    @Inject(method="onHit(Lnet/minecraft/world/phys/HitResult;)V",at=@At("HEAD"),cancellable=true)
    private void buildup$mushroom(HitResult hit,CallbackInfo ci){if(SkeletonExtras.mushroomHit((Snowball)(Object)this))ci.cancel();}
}