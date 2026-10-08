package com.davidblackcn.buildupmobtweaks.mixin;
import com.davidblackcn.buildupmobtweaks.combat.GeneralHostileRules;
import net.minecraft.world.entity.monster.Creeper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Creeper.class)
public abstract class CreeperExplosionMixin {
    @Inject(method="explodeCreeper()V",at=@At("HEAD"))
    private void buildup$explode(CallbackInfo ci){if(!((Creeper)(Object)this).level().isClientSide())GeneralHostileRules.explode((Creeper)(Object)this);}
}