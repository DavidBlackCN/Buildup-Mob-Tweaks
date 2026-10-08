package com.davidblackcn.buildupmobtweaks.mixin.rebuild;

import com.davidblackcn.buildupmobtweaks.rebuild.skeleton.*;
import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class SkeletonAiMixin {
    @Inject(method="serverAiStep()V",at=@At("HEAD"))
    private void buildup$beforeAi(CallbackInfo ci){if((Object)this instanceof AbstractSkeleton m&&SkeletonBehavior.instance()!=null)SkeletonBehavior.instance().beforeAi(m);}
    @Inject(method="setTarget(Lnet/minecraft/world/entity/LivingEntity;)V",at=@At("HEAD"),cancellable=true)
    private void buildup$target(LivingEntity target,CallbackInfo ci){if(target!=null&&(Object)this instanceof AbstractSkeleton m&&SkeletonBehavior.instance()!=null
            &&SkeletonBehavior.instance().enabled(m,FeatureId.SKELETON_TARGET_VALIDATION)&&!SkeletonBehavior.valid(m,target))ci.cancel();}
}
