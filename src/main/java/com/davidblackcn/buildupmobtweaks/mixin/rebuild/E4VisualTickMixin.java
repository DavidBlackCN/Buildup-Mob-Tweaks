package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.general.E4VisualState;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Mob.class)
public abstract class E4VisualTickMixin {
    @Inject(method="tick()V",at=@At("TAIL")) private void buildup$visual(CallbackInfo ci){var m=(Mob)(Object)this;if(!m.level().isClientSide())E4VisualState.update(m);}
}
