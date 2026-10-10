package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.relations.RelationBehavior;
import net.minecraft.world.entity.ai.goal.target.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(HurtByTargetGoal.class)
public abstract class GolemRetaliationMixin extends TargetGoal {
    protected GolemRetaliationMixin(net.minecraft.world.entity.Mob m,boolean see){super(m,see);}
    @Inject(method="canUse()Z",at=@At("HEAD"),cancellable=true)
    private void buildup$golem(CallbackInfoReturnable<Boolean> cir){var b=RelationBehavior.instance();if(b!=null&&b.refuseGolemRevenge(mob,mob.getLastHurtByMob()))cir.setReturnValue(false);}
}
