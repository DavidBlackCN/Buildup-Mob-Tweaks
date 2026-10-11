/* Mob AI Tweaks throw animation adaptation: Copyright (c) 2024 N0t_UN_Owen, MIT. */
package com.davidblackcn.buildupmobtweaks.client.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.client.rebuild.*;
import net.minecraft.client.model.animal.golem.SnowGolemModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(SnowGolemModel.class)
public abstract class SnowModelMixin implements SnowArmAccess {
    @Shadow @Final private ModelPart leftArm;@Shadow @Final private ModelPart rightArm;
    public ModelPart buildup$throwArm(boolean left){return left?leftArm:rightArm;}
    @Inject(method="setupAnim(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;)V",at=@At("TAIL"))
    private void buildup$throw(LivingEntityRenderState state,CallbackInfo ci){if(!(state instanceof SnowVisualState s)||s.buildup$throwTicks()<=0)return;
        var arm=buildup$throwArm(s.buildup$throwLeft());float progress=Math.clamp((10-s.buildup$throwTicks()+s.buildup$throwPartial())/10f,0,1);
        arm.xRot=-(float)Math.sin(progress*Math.PI)*1.7f;arm.yRot+=(s.buildup$throwLeft()?-1:1)*progress*(float)Math.PI*1.5f;
        arm.zRot=s.buildup$throwLeft()?(float)Math.PI/3:-(float)Math.PI/3;
    }
}
