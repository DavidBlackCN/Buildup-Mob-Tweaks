package com.davidblackcn.buildupmobtweaks.mixin;

import com.davidblackcn.buildupmobtweaks.combat.VexCombat;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.monster.Vex;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(targets = "net.minecraft.world.entity.monster.Vex$VexChargeAttackGoal")
public abstract class VexChargeMixin {
    @Shadow @Final private Vex this$0;
    @Inject(method = "canUse()Z", at = @At("HEAD"), cancellable = true)
    private void buildup$startGate(CallbackInfoReturnable<Boolean> cir) {
        if (VexCombat.instance() != null && !VexCombat.instance().allowStart(this$0)) cir.setReturnValue(false);
    }
    @Inject(method = "start()V", at = @At("TAIL"))
    private void buildup$begin(CallbackInfo ci) {
        if (VexCombat.instance() != null) VexCombat.instance().start(this$0);
    }
    @Inject(method = "canContinueToUse()Z", at = @At("HEAD"), cancellable = true)
    private void buildup$continueGate(CallbackInfoReturnable<Boolean> cir) {
        if (VexCombat.instance() != null && !VexCombat.instance().allowContinue(this$0)) cir.setReturnValue(false);
    }
    @Inject(method = "tick()V", at = @At("HEAD"), cancellable = true)
    private void buildup$invalidTarget(CallbackInfo ci) {
        if (VexCombat.instance() != null && !VexCombat.instance().allowContinue(this$0)) {
            this$0.setIsCharging(false); VexCombat.instance().stop(this$0); ci.cancel();
        }
    }
    // Only suppress the near-target correction. start() retains the original sampled eye position.
    @Redirect(method = "tick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/control/MoveControl;setWantedPosition(DDDD)V"))
    private void buildup$fixedEndpoint(MoveControl<?> control, double x, double y, double z, double speed) {
        if (VexCombat.instance() == null || !VexCombat.instance().fixed(this$0)) control.setWantedPosition(x, y, z, speed);
    }
    @Inject(method = "stop()V", at = @At("TAIL"))
    private void buildup$recover(CallbackInfo ci) {
        if (VexCombat.instance() != null) VexCombat.instance().stop(this$0);
    }
}