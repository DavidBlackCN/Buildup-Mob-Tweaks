package com.davidblackcn.buildupmobtweaks.mixin.rebuild;

import com.davidblackcn.buildupmobtweaks.villager.farmer.FarmerEnvironment;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.JumpControl;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(JumpControl.class)
public abstract class FarmerJumpMixin {
    @Shadow @Final private Mob mob;
    @Inject(method="jump()V",at=@At("HEAD"),cancellable=true)
    private void buildup$walkWorkSteps(CallbackInfo ci){if(FarmerEnvironment.suppressesWorkJump(mob))ci.cancel();}
}
