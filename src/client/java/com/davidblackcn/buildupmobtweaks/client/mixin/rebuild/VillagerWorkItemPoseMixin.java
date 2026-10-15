package com.davidblackcn.buildupmobtweaks.client.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.client.rebuild.ProfessionRenderStateAccess;
import net.minecraft.client.renderer.entity.layers.CrossedArmsItemLayer;
import net.minecraft.client.renderer.entity.state.HoldingEntityRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(CrossedArmsItemLayer.class)
public abstract class VillagerWorkItemPoseMixin {
    @Inject(method="applyTranslation",at=@At("TAIL"))
    private void buildup$workGesture(HoldingEntityRenderState state,PoseStack pose,CallbackInfo ci){if(state instanceof ProfessionRenderStateAccess access){int phase=access.buildup$workVisual()&15;if(phase==3)pose.translate(0,-.2+Math.sin(state.ageInTicks*.8)*.025,-.12);else if(phase==2)pose.translate(0,Math.sin(state.ageInTicks*.4)*.04,0);}}
}
