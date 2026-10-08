package com.davidblackcn.buildupmobtweaks.mixin;
import com.davidblackcn.buildupmobtweaks.combat.GeneralHostileRules;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ZombifiedPiglin.class)
public abstract class PiglinAngerMixin {
    @Inject(method="setTarget(Lnet/minecraft/world/entity/LivingEntity;)V",at=@At("HEAD"),cancellable=true)
    private void buildup$anger(LivingEntity target,CallbackInfo ci){if(GeneralHostileRules.refusesTarget((ZombifiedPiglin)(Object)this,target))ci.cancel();}
}