package com.davidblackcn.buildupmobtweaks.mixin.rebuild;

import com.davidblackcn.buildupmobtweaks.rebuild.pillager.PillagerBehavior;
import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.illager.Pillager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class PillagerLifecycleMixin {
    @Inject(method = "serverAiStep()V", at = @At("HEAD"))
    private void buildup$beforeSelectors(CallbackInfo ci) {
        if ((Object) this instanceof Pillager mob && PillagerBehavior.instance() != null) PillagerBehavior.instance().beforeAi(mob);
    }
    @Inject(method = "setTarget(Lnet/minecraft/world/entity/LivingEntity;)V", at = @At("HEAD"), cancellable = true)
    private void buildup$rejectInvalidTarget(LivingEntity target, CallbackInfo ci) {
        if (target != null && (Object) this instanceof Pillager mob && PillagerBehavior.instance() != null
                && PillagerBehavior.instance().enabled(mob, FeatureId.PILLAGER_TARGET_LIFECYCLE)
                && !PillagerBehavior.validTarget(mob, target)) ci.cancel();
    }
}
