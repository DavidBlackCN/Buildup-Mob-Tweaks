package com.davidblackcn.buildupmobtweaks.mixin;
import com.davidblackcn.buildupmobtweaks.combat.CombatPerception;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.sensing.Sensing;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(Sensing.class)
public abstract class GlowingSenseMixin {
    @Shadow @Final private Mob mob;
    @Inject(method="hasLineOfSight(Lnet/minecraft/world/entity/Entity;)Z",at=@At("RETURN"),cancellable=true)
    private void buildup$glowing(Entity target,CallbackInfoReturnable<Boolean> cir) {
        if(CombatPerception.blindBeyondReach(mob,target)) cir.setReturnValue(false);
        else if(CombatPerception.seesGlow(mob,target)) cir.setReturnValue(true);
    }
}