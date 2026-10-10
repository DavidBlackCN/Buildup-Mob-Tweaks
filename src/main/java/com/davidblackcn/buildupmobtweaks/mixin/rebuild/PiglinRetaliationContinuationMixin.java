package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.piglin.PiglinBehavior;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(TargetGoal.class)
public abstract class PiglinRetaliationContinuationMixin {
    @Shadow @Final protected Mob mob;
    @Shadow protected LivingEntity targetMob;
    @Inject(method="canContinueToUse()Z",at=@At("HEAD"),cancellable=true)
    private void buildup$continue(CallbackInfoReturnable<Boolean> cir){var b=PiglinBehavior.instance();if(b!=null&&b.refuse(mob,mob.getTarget()==null?targetMob:mob.getTarget()))cir.setReturnValue(false);}
}
