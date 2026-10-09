package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.remaining.*;
import net.minecraft.world.entity.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(LivingEntity.class)
public abstract class RemainingDeathMixin {
    @Inject(method="die(Lnet/minecraft/world/damagesource/DamageSource;)V",at=@At("HEAD"))
    private void buildup$cleanup(net.minecraft.world.damagesource.DamageSource s,CallbackInfo ci){if((Object)this instanceof Mob m&&RemainingBehavior.supported(m)&&RemainingState.known(m)&&RemainingBehavior.instance()!=null)RemainingBehavior.instance().cleanup(m);}
}
