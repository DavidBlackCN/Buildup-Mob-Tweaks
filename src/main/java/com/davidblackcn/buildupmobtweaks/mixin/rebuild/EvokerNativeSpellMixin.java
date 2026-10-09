package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.evoker.EvokerBehavior;
import net.minecraft.world.entity.monster.illager.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(targets="net.minecraft.world.entity.monster.illager.SpellcasterIllager$SpellcasterUseSpellGoal")
public abstract class EvokerNativeSpellMixin {
    @Shadow @Final private SpellcasterIllager this$0;
    @Inject(method="canUse()Z",at=@At("HEAD"),cancellable=true)
    private void buildup$exclusive(CallbackInfoReturnable<Boolean> ci){if(this$0 instanceof Evoker m&&EvokerBehavior.instance()!=null&&EvokerBehavior.instance().busy(m))ci.setReturnValue(false);}
}
