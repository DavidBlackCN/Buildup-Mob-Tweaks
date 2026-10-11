package com.davidblackcn.buildupmobtweaks.client.rebuild;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.animal.golem.SnowGolemModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.SnowGolemRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
public final class SnowballHandLayer extends RenderLayer<SnowGolemRenderState,SnowGolemModel> {
    public SnowballHandLayer(RenderLayerParent<SnowGolemRenderState,SnowGolemModel> parent){super(parent);}
    @Override public void submit(PoseStack pose,SubmitNodeCollector collector,int light,SnowGolemRenderState state,float yaw,float pitch){
        var visual=(SnowVisualState)state;if(state.isInvisible||visual.buildup$throwTicks()<=4||visual.buildup$heldSnowball().isEmpty())return;
        // Model draws are deferred in 26.3. Resolve this entity's pose before transforming its hand.
        getParentModel().setupAnim(state);
        pose.pushPose();((SnowArmAccess)getParentModel()).buildup$throwArm(visual.buildup$throwLeft()).translateAndRotate(pose);
        pose.translate(visual.buildup$throwLeft()?-.6:.6,0,0);pose.scale(.7f,.7f,.7f);
        visual.buildup$heldSnowball().submit(pose,collector,light,OverlayTexture.NO_OVERLAY,state.outlineColor);pose.popPose();
    }
}
