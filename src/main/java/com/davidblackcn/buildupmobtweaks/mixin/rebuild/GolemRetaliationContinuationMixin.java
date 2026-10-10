package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.relations.RelationBehavior;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.target.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(TargetGoal.class)
public abstract class GolemRetaliationContinuationMixin {
    @Shadow @Final protected Mob mob;
    @Shadow protected LivingEntity targetMob;
    @Inject(method="canContinueToUse()Z",at=@At("HEAD"),cancellable=true)
    private void buildup$golemContinue(CallbackInfoReturnable<Boolean> cir){var b=RelationBehavior.instance();if((Object)this instanceof HurtByTargetGoal&&b!=null&&b.refuseGolemRevenge(mob,mob.getTarget()==null?targetMob:mob.getTarget()))cir.setReturnValue(false);}
}
