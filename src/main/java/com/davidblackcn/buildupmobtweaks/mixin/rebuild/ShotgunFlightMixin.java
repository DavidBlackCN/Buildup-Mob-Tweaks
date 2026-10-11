package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.ability.AbilityBehavior;
import net.minecraft.world.entity.projectile.hurtingprojectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.BreezeWindCharge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(AbstractHurtingProjectile.class)
public abstract class ShotgunFlightMixin {
    @Inject(method="tick()V",at=@At("HEAD"),cancellable=true) private void buildup$expiry(CallbackInfo ci){if((Object)this instanceof BreezeWindCharge p&&AbilityBehavior.instance()!=null&&AbilityBehavior.instance().expireExtra(p))ci.cancel();}
}
