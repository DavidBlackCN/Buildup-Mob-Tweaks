package com.davidblackcn.buildupmobtweaks.mixin;
import com.davidblackcn.buildupmobtweaks.combat.SkeletonExtras;
import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Projectile.class)
public abstract class SkeletonProjectileMixin {
    @Inject(method="tick()V",at=@At("TAIL"))
    private void buildup$projectile(CallbackInfo ci){SkeletonExtras.projectileTick((Projectile)(Object)this);com.davidblackcn.buildupmobtweaks.combat.GeneralHostileRules.projectile((Projectile)(Object)this);}
}