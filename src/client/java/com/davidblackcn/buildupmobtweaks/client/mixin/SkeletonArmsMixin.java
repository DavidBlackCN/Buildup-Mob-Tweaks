package com.davidblackcn.buildupmobtweaks.client.mixin;
import com.davidblackcn.buildupmobtweaks.combat.HostileCombat;
import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.AbstractSkeletonRenderer;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.item.BowItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(AbstractSkeletonRenderer.class)
public abstract class SkeletonArmsMixin {
    private static boolean buildup$usingBow(AbstractSkeleton mob){return HostileCombat.instance()!=null&&HostileCombat.instance().gate(FeatureId.SKELETON_OFFHAND_VISUAL)&&mob.isUsingItem()&&mob.getUseItem().getItem() instanceof BowItem;}
    @Inject(method="extractRenderState(Lnet/minecraft/world/entity/monster/skeleton/AbstractSkeleton;Lnet/minecraft/client/renderer/entity/state/SkeletonRenderState;F)V",at=@At("TAIL"))
    private void buildup$bowState(AbstractSkeleton mob,SkeletonRenderState state,float partial,CallbackInfo ci){if(buildup$usingBow(mob))state.isHoldingBow=true;}
    @Inject(method="getArmPose(Lnet/minecraft/world/entity/monster/skeleton/AbstractSkeleton;Lnet/minecraft/world/entity/HumanoidArm;)Lnet/minecraft/client/model/HumanoidModel$ArmPose;",at=@At("HEAD"),cancellable=true)
    private void buildup$bowArm(AbstractSkeleton mob,HumanoidArm arm,CallbackInfoReturnable<HumanoidModel.ArmPose> cir){
        if(!buildup$usingBow(mob))return;
        var holding=mob.getUsedItemHand()==net.minecraft.world.InteractionHand.MAIN_HAND?mob.getMainArm():mob.getMainArm().getOpposite();
        if(arm==holding)cir.setReturnValue(HumanoidModel.ArmPose.BOW_AND_ARROW);
    }
}