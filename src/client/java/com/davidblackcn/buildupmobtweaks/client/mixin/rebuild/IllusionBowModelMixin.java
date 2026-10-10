package com.davidblackcn.buildupmobtweaks.client.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.client.rebuild.IllusionRenderAccess;
import net.minecraft.client.model.monster.illager.IllagerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.IllagerRenderState;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.monster.illager.AbstractIllager.IllagerArmPose;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(IllagerModel.class)
public abstract class IllusionBowModelMixin {
    @Shadow @Final private ModelPart rightArm;
    @Shadow @Final private ModelPart leftArm;
    @Shadow @Final private ModelPart head;
    @Inject(method="setupAnim(Lnet/minecraft/client/renderer/entity/state/IllagerRenderState;)V",at=@At("TAIL"))
    private void buildup$actualBow(IllagerRenderState state,CallbackInfo ci){if(state instanceof IllusionRenderAccess a&&a.buildup$managedBow()&&state.armPose==IllagerArmPose.BOW_AND_ARROW&&state.mainArm==HumanoidArm.LEFT){leftArm.yRot=.1f+head.yRot;rightArm.yRot=-.1f+head.yRot-.4f;leftArm.xRot=rightArm.xRot=-(float)Math.PI/2+head.xRot;}}
}
