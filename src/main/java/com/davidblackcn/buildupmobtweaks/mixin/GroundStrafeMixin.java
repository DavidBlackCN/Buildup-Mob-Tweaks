package com.davidblackcn.buildupmobtweaks.mixin;
import com.davidblackcn.buildupmobtweaks.combat.*;
import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import net.minecraft.core.*;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

/** Narrow ground-controller policies; no enum ordinals, reflection or replacement controller. */
@Mixin(MoveControl.class)
public abstract class GroundStrafeMixin {
    @Shadow @Final protected Mob mob;
    @Shadow protected float strafeForwards;
    @Shadow protected float strafeRight;
    @Unique private boolean buildup$strafe, buildup$move, buildup$handled;
    @Unique private float buildup$airForward,buildup$airSide;
    @Unique private int buildup$airTicks;
    @Unique private boolean buildup$ground(){return mob.getMoveControl().getClass()==MoveControl.class && mob.getNavigation() instanceof GroundPathNavigation && !mob.isPassenger()&&!mob.isInWater();}
    @Inject(method="strafe(FF)V",at=@At("TAIL"))
    private void buildup$submitted(float forward,float side,CallbackInfo ci){buildup$strafe=true;buildup$move=false;}
    @Inject(method="setWantedPosition(DDDD)V",at=@At("TAIL"))
    private void buildup$moving(double x,double y,double z,double speed,CallbackInfo ci){buildup$move=true;buildup$strafe=false;}
    @Inject(method="setWait()V",at=@At("TAIL"))
    private void buildup$wait(CallbackInfo ci){buildup$strafe=false;buildup$move=false;buildup$airTicks=0;}
    @Inject(method="tick()V",at=@At("HEAD"))
    private void buildup$groundStep(CallbackInfo ci){
        buildup$handled=false;
        if(!buildup$ground())return;
        boolean strafe=buildup$strafe;buildup$strafe=false;
        if(!strafe && EnvironmentCombat.on(mob,FeatureId.MOVEMENT_CLEAR_STRAFE) && (mob.onGround()||buildup$move))mob.setXxa(0);
        buildup$move=false;
        if(!strafe)return;
        double yaw=Math.toRadians(mob.getYRot());double dx=strafeRight*Math.cos(yaw)-strafeForwards*Math.sin(yaw),dz=strafeForwards*Math.cos(yaw)+strafeRight*Math.sin(yaw);
        if(mob.onGround()){
            buildup$airForward=strafeForwards;buildup$airSide=strafeRight;buildup$airTicks=12;
            if(!SkeletonCombat.safeStep(mob,dx,dz)){
                double length=Math.hypot(dx,dz);
                boolean jump=false;
                if(length>.001 && EnvironmentCombat.on(mob,FeatureId.STRAFE_OBSTACLE_JUMP)){
                    var pos=BlockPos.containing(mob.getX()+dx/length,mob.getY(),mob.getZ()+dz/length);var state=mob.level().getBlockState(pos);
                    jump=state.isFaceSturdy(mob.level(),pos,Direction.UP)&&!state.is(Blocks.MAGMA_BLOCK)&&!state.is(Blocks.CAMPFIRE)&&!state.is(Blocks.SOUL_CAMPFIRE)
                            &&mob.level().getBlockState(pos.above()).isAir()&&mob.level().getBlockState(pos.above(2)).isAir()
                            &&mob.level().noCollision(mob,mob.getBoundingBox().move(0,1,0))&&mob.level().noCollision(mob,mob.getBoundingBox().move(dx/length,1,dz/length));
                }
                if(jump){mob.getJumpControl().jump();buildup$handled=true;}
                else if(EnvironmentCombat.on(mob,FeatureId.STRAFE_HAZARD_CHECK)){strafeForwards=0;strafeRight=0;buildup$airForward=0;buildup$airSide=0;buildup$handled=true;}
            }
        }
    }
    @Inject(method="isWalkable(FF)Z",at=@At("HEAD"),cancellable=true)
    private void buildup$noUnsafeForwardFallback(float x,float z,CallbackInfoReturnable<Boolean> cir){if(buildup$handled)cir.setReturnValue(true);}
    @Inject(method="tick()V",at=@At("TAIL"))
    private void buildup$airInput(CallbackInfo ci){
        if(!buildup$ground()||buildup$airTicks<=0)return;
        buildup$airTicks--;
        if(!mob.onGround()&&EnvironmentCombat.on(mob,FeatureId.MOVEMENT_JUMP_INPUT)){mob.setZza(buildup$airForward);mob.setXxa(buildup$airSide);}
    }
}