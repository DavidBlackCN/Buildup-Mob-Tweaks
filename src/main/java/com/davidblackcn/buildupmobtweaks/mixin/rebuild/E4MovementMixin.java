package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.general.E4Behavior;
import com.davidblackcn.buildupmobtweaks.rebuild.skeleton.*;
import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
@Mixin(MoveControl.class)
public abstract class E4MovementMixin {
    @Shadow @Final protected Mob mob;
    @Shadow protected MoveControl.Operation operation;
    @Shadow protected float strafeForwards,strafeRight;
    @Unique private MoveControl.Operation buildup$before;
    @Unique private float buildup$x,buildup$z;
    @Unique private int buildup$air;
    @Unique private java.util.UUID buildup$target;
    @Unique private boolean buildup$owned(){return mob instanceof AbstractSkeleton s&&SkeletonBehavior.supported(s)&&SkeletonState.known(s)&&SkeletonState.runtime(s).managed
        &&(SkeletonState.runtime(s).mode.equals("walk")||SkeletonState.runtime(s).mode.equals("sniper"))&&((SkeletonAccess)s).buildup$special()==0
        &&mob.getTarget()!=null&&mob.getTarget().isAlive()&&!mob.isPassenger()&&!mob.isInWater()&&!mob.isNoAi();}
    @Inject(method="tick()V",at=@At("HEAD")) private void buildup$before(CallbackInfo ci){buildup$before=operation;}
    @ModifyReturnValue(method="isWalkable(FF)Z",at=@At("RETURN"))
    private boolean buildup$step(boolean original,float x,float z){
        if(!buildup$owned()||!E4Behavior.enabled(mob,FeatureId.SKELETON_SAFE_STRAFING))return original;
        var s=(AbstractSkeleton)mob;var direction=mob.getLookAngle().multiply(1,0,1).normalize();var d=direction.scale(strafeForwards).add(new Vec3(direction.z,0,-direction.x).scale(strafeRight)).normalize();
        if(SkeletonBehavior.safeStep(s,d))return true;
        if(buildup$air<12&&E4Behavior.obstacleJump(s,d)){if(mob.onGround())mob.getJumpControl().jump();return true;}
        return false;
    }
    @Inject(method="tick()V",at=@At("TAIL")) private void buildup$after(CallbackInfo ci){
        if(!buildup$owned()){buildup$x=buildup$z=0;buildup$air=0;buildup$target=null;return;}
        var target=mob.getTarget().getUUID();if(!target.equals(buildup$target)){buildup$x=buildup$z=0;buildup$air=0;buildup$target=target;}
        if(buildup$before==MoveControl.Operation.STRAFE){
            var dir=mob.getLookAngle().multiply(1,0,1).normalize();var delta=dir.scale(mob.zza).add(new Vec3(dir.z,0,-dir.x).scale(mob.xxa)).normalize();
            if(E4Behavior.enabled(mob,FeatureId.SKELETON_SAFE_STRAFING)&&!SkeletonBehavior.safeStep((AbstractSkeleton)mob,delta)&&!(buildup$air<12&&E4Behavior.obstacleJump((AbstractSkeleton)mob,delta))){mob.setXxa(0);mob.setZza(0);}
            buildup$x=mob.xxa;buildup$z=mob.zza;
            if(E4Behavior.enabled(mob,FeatureId.STRAFE_OBSTACLE_JUMP)&&mob.onGround()&&mob.horizontalCollision){
                Vec3 d=mob.getLookAngle().multiply(1,0,1).normalize().scale(.7);var p=mob.blockPosition().offset((int)Math.signum(d.x),0,(int)Math.signum(d.z));
                var b=mob.level().getBlockState(p);
                if(mob.level().hasChunkAt(p)&&!b.is(BlockTags.DOORS)&&!b.is(BlockTags.FENCES)&&!b.is(BlockTags.FENCE_GATES)
                    &&mob.level().noCollision(mob,mob.getBoundingBox().move(0,1,0))&&SkeletonBehavior.safeStep((AbstractSkeleton)mob,new Vec3(d.x,1,d.z))){mob.getJumpControl().jump();buildup$air=0;}
            }
        }
        if(mob.onGround()){if(buildup$before!=MoveControl.Operation.STRAFE)buildup$air=0;}
        else if(E4Behavior.enabled(mob,FeatureId.MOVEMENT_JUMP_INPUT)&&buildup$air++<12&&buildup$before==MoveControl.Operation.WAIT){mob.setXxa(buildup$x);mob.setZza(buildup$z);}
        if(E4Behavior.enabled(mob,FeatureId.MOVEMENT_CLEAR_STRAFE)&&buildup$before==MoveControl.Operation.WAIT&&(mob.onGround()||buildup$air>=12||!E4Behavior.enabled(mob,FeatureId.MOVEMENT_JUMP_INPUT)))mob.setXxa(0);
    }
}
