package com.davidblackcn.buildupmobtweaks.mixin;
import com.davidblackcn.buildupmobtweaks.combat.GeneralHostileRules;
import net.minecraft.world.entity.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Mob.class)
public abstract class FriendlyTargetMixin {
    @Inject(method="setTarget(Lnet/minecraft/world/entity/LivingEntity;)V",at=@At("HEAD"),cancellable=true)
    private void buildup$target(LivingEntity target,CallbackInfo ci){if(GeneralHostileRules.refusesTarget((Mob)(Object)this,target))ci.cancel();}
}