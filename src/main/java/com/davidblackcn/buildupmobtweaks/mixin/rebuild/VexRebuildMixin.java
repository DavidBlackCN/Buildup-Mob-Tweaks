/* Mob AI Tweaks adaptation: Copyright (c) 2024 N0t_UN_Owen, MIT. See NOTICE.md. */
package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.vex.VexBehavior;
import net.minecraft.world.entity.monster.Vex;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(Vex.class)
public abstract class VexRebuildMixin {
    @Inject(method="setIsCharging(Z)V",at=@At("HEAD"))
    private void buildup$charge(boolean charge,CallbackInfo ci){var m=(Vex)(Object)this;if(!m.level().isClientSide()&&VexBehavior.instance()!=null)VexBehavior.instance().charging(m,charge);}
    @Inject(method="tick()V",at=@At("TAIL"))
    private void buildup$bounded(CallbackInfo ci){var m=(Vex)(Object)this;if(!m.level().isClientSide()&&VexBehavior.instance()!=null)VexBehavior.instance().tick(m);}
}
