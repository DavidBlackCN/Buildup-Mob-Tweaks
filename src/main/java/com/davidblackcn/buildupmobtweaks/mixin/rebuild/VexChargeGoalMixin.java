/* Mob AI Tweaks adaptation: Copyright (c) 2024 N0t_UN_Owen, MIT. See NOTICE.md. */
package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.vex.VexBehavior;
import com.davidblackcn.buildupmobtweaks.rebuild.raid.RaidState;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.ai.control.MoveControl;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(targets="net.minecraft.world.entity.monster.Vex$VexChargeAttackGoal")
public abstract class VexChargeGoalMixin {
    @Shadow @Final private Vex this$0;
    @Inject(method="canUse()Z",at=@At("RETURN"),cancellable=true)
    private void buildup$gate(CallbackInfoReturnable<Boolean> ci){if(ci.getReturnValueZ()&&VexBehavior.instance()!=null&&!VexBehavior.instance().canCharge(this$0))ci.setReturnValue(false);}
    @WrapOperation(method="tick()V",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/ai/control/MoveControl;setWantedPosition(DDDD)V"))
    private void buildup$snapshot(MoveControl<?> control,double x,double y,double z,double speed,Operation<Void> original){if(!RaidState.runtime(this$0).charging)original.call(control,x,y,z,speed);}
}
