package com.davidblackcn.buildupmobtweaks.mixin;
import com.davidblackcn.buildupmobtweaks.combat.CombatPerception;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(LivingEntity.class)
public abstract class CombatPerceptionMixin {
    @Inject(method="getVisibilityPercent(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;)D",at=@At("RETURN"),cancellable=true)
    private void buildup$visibility(ServerLevel level,Entity observer,CallbackInfoReturnable<Double> cir) {
        if(observer instanceof Mob mob) cir.setReturnValue(CombatPerception.visibility(mob,(LivingEntity)(Object)this,cir.getReturnValue()));
    }
    @Inject(method="hasLineOfSight(Lnet/minecraft/world/entity/Entity;)Z",at=@At("HEAD"),cancellable=true)
    private void buildup$blind(Entity target,CallbackInfoReturnable<Boolean> cir) {
        if((Object)this instanceof Mob mob && CombatPerception.blindBeyondReach(mob,target)) cir.setReturnValue(false);
    }
}