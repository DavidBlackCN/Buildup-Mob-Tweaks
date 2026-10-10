package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.ecology.EcologyBehavior;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(PanicGoal.class)
public abstract class EcologyPanicMixin {
    @Shadow @Final protected PathfinderMob mob;
    @Inject(method="start()V",at=@At("TAIL")) private void buildup$panic(CallbackInfo ci){if(EcologyBehavior.instance()!=null)EcologyBehavior.instance().spread(mob);}
}
