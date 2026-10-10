/* Mob AI Tweaks adaptation: Copyright (c) 2024 N0t_UN_Owen, MIT. See NOTICE.md. */
package com.davidblackcn.buildupmobtweaks.client.rebuild;
import net.minecraft.client.model.*;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
public final class ProfessionVisuals {
    public static void crossbow(HumanoidModel<?> model,HumanoidRenderState state){
        boolean right=state.rightArmPose==HumanoidModel.ArmPose.CROSSBOW_CHARGE||state.rightArmPose==HumanoidModel.ArmPose.CROSSBOW_HOLD;
        var pose=right?state.rightArmPose:state.leftArmPose;
        if(pose==HumanoidModel.ArmPose.CROSSBOW_CHARGE)AnimationUtils.animateCrossbowCharge(model.rightArm,model.leftArm,state.maxCrossbowChargeDuration,state.ticksUsingItem,right);
        else if(pose==HumanoidModel.ArmPose.CROSSBOW_HOLD)AnimationUtils.animateCrossbowHold(model.rightArm,model.leftArm,model.head,right);
    }
    public static Vec3 rodHand(Mob m,float partial){
        var hand=com.davidblackcn.buildupmobtweaks.rebuild.profession.ProfessionBehavior.hand(m,net.minecraft.world.item.Items.FISHING_ROD);
        var arm=hand==net.minecraft.world.InteractionHand.OFF_HAND?m.getMainArm().getOpposite():m.getMainArm();
        double side=(arm==HumanoidArm.RIGHT?1:-1)*.35*m.getScale();
        float rot=Mth.lerp(partial,m.yBodyRotO,m.yBodyRot)*(float)(Math.PI/180);
        double tilt=m instanceof Zombie z?(Math.PI/(z.isBaby()?1.5:2.25)-.66+Math.sin(m.getSwingAnimation(partial)*Math.PI)*1.2):0;
        double forward=-Math.cos(tilt)*m.getScale();double rise=Math.sin(tilt)*m.getScale();
        return m.getEyePosition(partial).add(-Math.cos(rot)*side-Math.sin(rot)*forward,rise-.45*m.getScale(),-Math.sin(rot)*side+Math.cos(rot)*forward);
    }
    private ProfessionVisuals(){}
}
