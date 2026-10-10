package com.davidblackcn.buildupmobtweaks.mixin.rebuild;

import com.davidblackcn.buildupmobtweaks.rebuild.skeleton.SkeletonDifferences;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import net.minecraft.world.entity.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Filter before native potion application, including HARMING's healing of undead allies. */
@Mixin(AreaEffectCloud.class)
public abstract class BoggedCloudMixin {
    @WrapOperation(method="serverTick(Lnet/minecraft/server/level/ServerLevel;)V",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/LivingEntity;isAffectedByPotions()Z"))
    private boolean buildup$eligible(LivingEntity victim,Operation<Boolean> original){var cloud=(AreaEffectCloud)(Object)this;
        return original.call(victim)&&(!SkeletonDifferences.known(cloud)||SkeletonDifferences.affects(cloud,victim));
    }
}
