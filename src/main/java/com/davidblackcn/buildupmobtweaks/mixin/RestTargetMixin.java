package com.davidblackcn.buildupmobtweaks.mixin;
import com.davidblackcn.buildupmobtweaks.combat.RestingHostiles;
import net.minecraft.world.entity.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Mob.class)
public abstract class RestTargetMixin {
    @Inject(method="setTarget(Lnet/minecraft/world/entity/LivingEntity;)V",at=@At("HEAD"),cancellable=true)
    private void buildup$neutral(LivingEntity target,CallbackInfo ci) { if(target!=null && RestingHostiles.resting((Mob)(Object)this)) ci.cancel(); }
}