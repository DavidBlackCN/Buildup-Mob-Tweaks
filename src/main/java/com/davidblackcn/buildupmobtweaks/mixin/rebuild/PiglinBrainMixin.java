package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.piglin.PiglinBehavior;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.server.level.ServerLevel;
@Mixin(PiglinAi.class)
public abstract class PiglinBrainMixin {
    @Inject(method="hasCrossbow(Lnet/minecraft/world/entity/LivingEntity;)Z",at=@At("RETURN"),cancellable=true)
    private static void buildup$bow(LivingEntity e,CallbackInfoReturnable<Boolean> cir){if(e instanceof Piglin m&&PiglinBehavior.instance()!=null&&PiglinBehavior.instance().compatible(m)&&PiglinBehavior.hand(m)!=null)cir.setReturnValue(true);}
    // Core cleanup and hurt handling move every non-admired offhand item into main hand.
    // Preserve actual compatible crossbows; holdInOffhand's separate native throw path
    // still transfers the old item when gold replaces it, so barter cannot overwrite it.
    @Inject(method="stopHoldingOffHandItem(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/monster/piglin/Piglin;Z)V",at=@At("HEAD"),cancellable=true)
    private static void buildup$keepCrossbow(ServerLevel level,Piglin m,boolean barter,CallbackInfo ci){if(PiglinBehavior.instance()!=null&&PiglinBehavior.instance().compatible(m)&&PiglinBehavior.crossbow(m.getOffhandItem()))ci.cancel();}
}
