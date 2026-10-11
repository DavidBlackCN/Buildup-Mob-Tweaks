package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.ability.AbilityBehavior;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Mob.class)
public abstract class AbilityTickMixin {
    @Inject(method="tick()V",at=@At("TAIL")) private void buildup$abilities(CallbackInfo ci){var b=AbilityBehavior.instance();if(b!=null)b.boundary((Mob)(Object)this);}
}
