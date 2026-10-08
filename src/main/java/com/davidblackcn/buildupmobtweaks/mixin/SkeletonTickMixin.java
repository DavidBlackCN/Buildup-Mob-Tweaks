package com.davidblackcn.buildupmobtweaks.mixin;

import com.davidblackcn.buildupmobtweaks.combat.SkeletonCombat;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class SkeletonTickMixin {
    @Inject(method = "serverAiStep()V", at = @At("HEAD"))
    private void buildup$combat(CallbackInfo ci) {
        if ((Object) this instanceof AbstractSkeleton skeleton && SkeletonCombat.instance() != null) {
            SkeletonCombat.instance().tick(skeleton);
        }
    }
}