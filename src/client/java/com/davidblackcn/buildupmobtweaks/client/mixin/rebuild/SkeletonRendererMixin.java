package com.davidblackcn.buildupmobtweaks.client.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.client.rebuild.SkeletonAnimationState;
import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import com.davidblackcn.buildupmobtweaks.rebuild.skeleton.*;
import net.minecraft.client.renderer.entity.AbstractSkeletonRenderer;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(AbstractSkeletonRenderer.class)
public abstract class SkeletonRendererMixin {
    private static boolean rebuiltBow(AbstractSkeleton m){return SkeletonBehavior.supported(m)&&BuildupMobTweaks.config().general.enabled.get()
            &&BuildupMobTweaks.config().hostile.skeleton.bowCompatibility.get()&&m.isUsingItem()&&m.getUseItem().getItem() instanceof BowItem;}
    @Inject(method="extractRenderState(Lnet/minecraft/world/entity/monster/skeleton/AbstractSkeleton;Lnet/minecraft/client/renderer/entity/state/SkeletonRenderState;F)V",at=@At("TAIL"))
    private void buildup$state(AbstractSkeleton mob,SkeletonRenderState state,float partial,CallbackInfo ci){
        int kind=mob.getType()==EntityTypes.STRAY?1:mob.getType()==EntityTypes.BOGGED?2:0;
        ((SkeletonAnimationState)state).buildup$animation(kind,((SkeletonAccess)mob).buildup$special(),partial);
        if(rebuiltBow(mob))state.isHoldingBow=true;
    }
    @Inject(method="getArmPose(Lnet/minecraft/world/entity/monster/skeleton/AbstractSkeleton;Lnet/minecraft/world/entity/HumanoidArm;)Lnet/minecraft/client/model/HumanoidModel$ArmPose;",at=@At("HEAD"),cancellable=true)
    private void buildup$arm(AbstractSkeleton mob,HumanoidArm arm,CallbackInfoReturnable<HumanoidModel.ArmPose> ci){
        if(rebuiltBow(mob)&&arm==(mob.getUsedItemHand()==InteractionHand.MAIN_HAND?mob.getMainArm():mob.getMainArm().getOpposite()))ci.setReturnValue(HumanoidModel.ArmPose.BOW_AND_ARROW);
    }
}
