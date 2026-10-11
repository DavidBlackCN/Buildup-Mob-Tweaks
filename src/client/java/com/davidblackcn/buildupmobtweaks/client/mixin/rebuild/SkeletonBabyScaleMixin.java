package com.davidblackcn.buildupmobtweaks.client.mixin.rebuild;

import com.davidblackcn.buildupmobtweaks.client.rebuild.SkeletonBabyVisuals;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class SkeletonBabyScaleMixin {
    @Inject(method="scale(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;)V",at=@At("TAIL"))
    private void buildup$babyMeshScale(LivingEntityRenderState state,PoseStack pose,CallbackInfo ci){
        // Scale the shared body and every layer once; retain native attribute scale and age-scaled shadows.
        if(state.isBaby&&SkeletonBabyVisuals.usesAdultMesh(state))pose.scale(state.ageScale,state.ageScale,state.ageScale);
    }
}
