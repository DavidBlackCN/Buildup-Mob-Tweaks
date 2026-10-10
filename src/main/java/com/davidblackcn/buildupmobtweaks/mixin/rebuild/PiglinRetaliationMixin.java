package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.piglin.PiglinBehavior;
import net.minecraft.world.entity.ai.goal.target.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(HurtByTargetGoal.class)
public abstract class PiglinRetaliationMixin extends TargetGoal {
    protected PiglinRetaliationMixin(net.minecraft.world.entity.Mob mob,boolean see){super(mob,see);}
    @Inject(method="canUse()Z",at=@At("HEAD"),cancellable=true)
    private void buildup$retaliate(CallbackInfoReturnable<Boolean> cir){var b=PiglinBehavior.instance();if(b!=null&&b.refuse(mob,mob.getLastHurtByMob()))cir.setReturnValue(false);}
}
