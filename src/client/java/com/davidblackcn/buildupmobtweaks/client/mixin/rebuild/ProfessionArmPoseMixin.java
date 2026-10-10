package com.davidblackcn.buildupmobtweaks.client.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.profession.*;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(HumanoidMobRenderer.class)
public abstract class ProfessionArmPoseMixin {
    @Inject(method="getArmPose(Lnet/minecraft/world/entity/Mob;Lnet/minecraft/world/entity/HumanoidArm;)Lnet/minecraft/client/model/HumanoidModel$ArmPose;",at=@At("RETURN"),cancellable=true)
    private void buildup$bow(Mob m,HumanoidArm arm,CallbackInfoReturnable<HumanoidModel.ArmPose> ci){
        if(!ProfessionBehavior.supported(m)||!m.getAttachedOrCreate(ProfessionState.CROSSBOW_VISUAL))return;
        var hand=ProfessionBehavior.hand(m,Items.CROSSBOW);if(hand==null||arm!=(hand==net.minecraft.world.InteractionHand.MAIN_HAND?m.getMainArm():m.getMainArm().getOpposite()))return;
        // The synchronized use flag can precede the client's first local use-item tick.
        ci.setReturnValue(m.isUsingItem()&&m.getUsedItemHand()==hand?HumanoidModel.ArmPose.CROSSBOW_CHARGE:HumanoidModel.ArmPose.CROSSBOW_HOLD);
    }
}
