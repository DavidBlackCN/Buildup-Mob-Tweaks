package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.general.E4Behavior;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvoidEntityGoal.class)
public abstract class E4AvoidMixin {
    @Shadow @Final protected PathfinderMob mob;
    @Shadow protected LivingEntity toAvoid;
    @ModifyArg(method="<init>(Lnet/minecraft/world/entity/PathfinderMob;Ljava/lang/Class;Ljava/util/function/Predicate;FDDLjava/util/function/Predicate;)V",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/ai/targeting/TargetingConditions;selector(Lnet/minecraft/world/entity/ai/targeting/TargetingConditions$Selector;)Lnet/minecraft/world/entity/ai/targeting/TargetingConditions;"),index=0)
    private TargetingConditions.Selector buildup$candidates(TargetingConditions.Selector original){return (t,l)->original.test(t,l)&&!E4Behavior.sneak(mob,t);}
    @ModifyReturnValue(method="canContinueToUse()Z",at=@At("RETURN"))
    private boolean buildup$continue(boolean original){return original&&!E4Behavior.sneak(mob,toAvoid)&&buildup$evokerReady();}
    @ModifyReturnValue(method="canUse()Z",at=@At("RETURN"))
    private boolean buildup$start(boolean original){return original&&buildup$evokerReady();}
    @Unique private boolean buildup$evokerReady(){
        if(!(mob instanceof net.minecraft.world.entity.monster.illager.Evoker e)||!E4Behavior.enabled(e,com.davidblackcn.buildupmobtweaks.feature.FeatureId.EVOKER_AVOID_TARGET_FIX))return true;
        return toAvoid!=null&&com.davidblackcn.buildupmobtweaks.rebuild.zombie.ZombieBehavior.valid(e,toAvoid)&&!e.isCastingSpell()
            &&!com.davidblackcn.buildupmobtweaks.rebuild.evoker.EvokerBehavior.instance().busy(e);
    }
    @ModifyArg(method="start()V",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/ai/navigation/PathNavigation;moveTo(Lnet/minecraft/world/level/pathfinder/Path;D)Z"),index=1)
    private double buildup$fleeStart(double original){return buildup$flee(original);}
    @ModifyArg(method="tick()V",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/ai/navigation/PathNavigation;setSpeedModifier(D)V"),index=0)
    private double buildup$fleeTick(double original){return buildup$flee(original);}
    @Unique private double buildup$flee(double original){return mob instanceof net.minecraft.world.entity.monster.illager.Evoker e&&E4Behavior.enabled(e,com.davidblackcn.buildupmobtweaks.feature.FeatureId.EVOKER_FLEE_SPEED)?toAvoid!=null&&e.distanceToSqr(toAvoid)<49?.8:.4:original;}
    @Inject(method="tick()V",at=@At("HEAD"),cancellable=true)
    private void buildup$avoidBoundary(CallbackInfo ci){if(!buildup$evokerReady()){mob.getNavigation().stop();mob.getMoveControl().strafe(0,0);ci.cancel();}}
}
