package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.skeleton.*;
import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(RangedBowAttackGoal.class)
public abstract class SkeletonStrafeMixin {
    @Shadow @Final private Monster mob;
    @Redirect(method="tick()V",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/ai/control/MoveControl;strafe(FF)V"))
    private void buildup$safeStrafe(MoveControl control,float forward,float side){
        if(mob instanceof AbstractSkeleton m&&SkeletonBehavior.instance()!=null&&SkeletonBehavior.instance().enabled(m,FeatureId.SKELETON_SAFE_STRAFING)){
            var direction=m.getLookAngle().multiply(1,0,1).normalize();var delta=direction.scale(forward).add(new Vec3(direction.z,0,-direction.x).scale(side)).normalize();
            if(!SkeletonBehavior.safeStep(m,delta)){control.strafe(0,0);return;}
            if(m.level().getDifficulty().getId()>1){forward*=1.5f;side*=1.5f;}
        }
        control.strafe(forward,side);
    }
}
