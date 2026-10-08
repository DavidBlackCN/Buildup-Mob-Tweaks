package com.davidblackcn.buildupmobtweaks.client.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.client.rebuild.SkeletonAnimationState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(LivingEntityRenderer.class)
public abstract class SkeletonRotationMixin {
    @Inject(method="setupRotations(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;FF)V",at=@At("TAIL"))
    private void buildup$rotation(LivingEntityRenderState state,PoseStack pose,float bodyRotation,float scale,CallbackInfo ci){
        if(!(state instanceof SkeletonAnimationState a)||a.buildup$ticks()==0||a.buildup$kind()==0)return;
        // Upstream rotates only the final nine signed ticks of both FLIP and DODGE.
        if(Math.abs(a.buildup$ticks())>=10)return;
        float pivot=state.boundingBoxHeight/state.scale/2;
        pose.translate(0,pivot,0);
        pose.rotate(Axis.XP.rotationDegrees(-(a.buildup$ticks()-Math.signum(a.buildup$ticks())*a.buildup$partial())*36));
        pose.translate(0,-pivot,0);
    }
}
