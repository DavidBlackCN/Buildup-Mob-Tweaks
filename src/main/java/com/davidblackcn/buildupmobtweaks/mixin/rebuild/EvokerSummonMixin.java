/* Mob AI Tweaks adaptation: Copyright (c) 2024 N0t_UN_Owen, MIT. See NOTICE.md. */
package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.evoker.EvokerBehavior;
import net.minecraft.world.entity.monster.illager.Evoker;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(targets="net.minecraft.world.entity.monster.illager.Evoker$EvokerSummonSpellGoal")
public abstract class EvokerSummonMixin {
    @Shadow @Final private Evoker this$0;
    @Inject(method="canUse()Z",at=@At("RETURN"),cancellable=true)
    private void buildup$limit(CallbackInfoReturnable<Boolean> ci){if(ci.getReturnValueZ()&&EvokerBehavior.instance()!=null&&!EvokerBehavior.instance().canSummon(this$0))ci.setReturnValue(false);}
    @Inject(method="performSpellCasting()V",at=@At("HEAD"),cancellable=true)
    private void buildup$recheck(CallbackInfo ci){var b=EvokerBehavior.instance();if(b!=null&&b.summonsModified(this$0)&&!b.canSummonAtCast(this$0))ci.cancel();}
    @ModifyConstant(method="performSpellCasting()V",constant=@Constant(intValue=3))
    private int buildup$batch(int original){var b=EvokerBehavior.instance();return b!=null&&b.summonsModified(this$0)?b.batch(this$0):original;}
    @Inject(method="getCastingInterval()I",at=@At("RETURN"),cancellable=true)
    private void buildup$interval(CallbackInfoReturnable<Integer> ci){if(EvokerBehavior.instance()!=null)ci.setReturnValue(EvokerBehavior.instance().reserveSummon(this$0,ci.getReturnValueI()));}
}
