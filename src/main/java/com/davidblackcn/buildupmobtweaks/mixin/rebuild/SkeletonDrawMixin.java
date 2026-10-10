package com.davidblackcn.buildupmobtweaks.mixin.rebuild;

import com.davidblackcn.buildupmobtweaks.rebuild.skeleton.SkeletonDifferences;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class SkeletonDrawMixin {
    @Unique private boolean buildup$wasUsing;
    @Inject(method="startUsingItem(Lnet/minecraft/world/InteractionHand;)V",at=@At("HEAD"))
    private void buildup$beforeDraw(InteractionHand hand,CallbackInfo ci){buildup$wasUsing=((LivingEntity)(Object)this).isUsingItem();}
    @Inject(method="startUsingItem(Lnet/minecraft/world/InteractionHand;)V",at=@At("RETURN"))
    private void buildup$newDraw(InteractionHand hand,CallbackInfo ci){
        if(!buildup$wasUsing&&(Object)this instanceof AbstractSkeleton m&&!m.level().isClientSide())SkeletonDifferences.draw(m);
    }
}
