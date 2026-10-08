package com.davidblackcn.buildupmobtweaks.mixin;

import com.davidblackcn.buildupmobtweaks.combat.DrownedTridents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.zombie.Drowned;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(Drowned.class)
public abstract class DrownedMixin {
    @Inject(method = "performRangedAttack(Lnet/minecraft/world/entity/LivingEntity;F)V", at = @At("HEAD"), cancellable = true)
    private void buildup$ownedThrow(LivingEntity target, float power, CallbackInfo ci) {
        var service = DrownedTridents.instance();
        if (service != null && service.conservation()) {
            service.throwHeld((Drowned) (Object) this, target);
            ci.cancel(); // Even a failed attempt cannot fall through to vanilla's synthetic projectile.
        }
    }
    @Inject(method = "wantsToSwim()Z", at = @At("HEAD"), cancellable = true)
    private void buildup$recoverInWater(CallbackInfoReturnable<Boolean> cir) {
        var service = DrownedTridents.instance();
        if (service != null && service.seekingWater((Drowned) (Object) this)) cir.setReturnValue(true);
    }
}