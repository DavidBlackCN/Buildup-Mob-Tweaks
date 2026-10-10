package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.piglin.PiglinBehavior;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ZombifiedPiglin.class)
public abstract class ZombifiedPiglinAngerMixin {
    @Inject(method="customServerAiStep(Lnet/minecraft/server/level/ServerLevel;)V",at=@At("HEAD"))
    private void buildup$cleanup(net.minecraft.server.level.ServerLevel level,CallbackInfo ci){var b=PiglinBehavior.instance();if(b!=null)b.cleanAnger((ZombifiedPiglin)(Object)this);}
    @Inject(method="setTarget(Lnet/minecraft/world/entity/LivingEntity;)V",at=@At("HEAD"),cancellable=true)
    private void buildup$target(LivingEntity target,CallbackInfo ci){var b=PiglinBehavior.instance();if(b!=null&&b.refuse((ZombifiedPiglin)(Object)this,target))ci.cancel();}
}
