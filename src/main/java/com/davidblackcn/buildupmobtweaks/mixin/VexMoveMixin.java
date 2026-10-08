package com.davidblackcn.buildupmobtweaks.mixin;

import com.davidblackcn.buildupmobtweaks.combat.VexCombat;
import net.minecraft.world.entity.monster.Vex;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.world.entity.monster.Vex$VexMoveControl")
public abstract class VexMoveMixin {
    @Shadow @Final private Vex this$0;
    @Inject(method = "tick()V", at = @At("HEAD"), cancellable = true)
    private void buildup$pause(CallbackInfo ci) {
        if (VexCombat.instance() != null && VexCombat.instance().recovering(this$0)) {
            // Prevent AI acceleration; external knockback is still allowed after the initial stop.
            this$0.getMoveControl().setWait(); ci.cancel();
        }
    }
}