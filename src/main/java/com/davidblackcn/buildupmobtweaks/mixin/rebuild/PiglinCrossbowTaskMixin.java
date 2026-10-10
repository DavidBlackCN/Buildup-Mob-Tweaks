package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.piglin.PiglinBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.piglin.Piglin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(CrossbowAttack.class)
public abstract class PiglinCrossbowTaskMixin {
    @Inject(method="checkExtraStartConditions(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Mob;)Z",at=@At("HEAD"),cancellable=true)
    private void buildup$start(ServerLevel l,Mob e,CallbackInfoReturnable<Boolean> cir){if(e instanceof Piglin m&&PiglinBehavior.instance()!=null&&PiglinBehavior.instance().compatible(m)&&PiglinBehavior.hand(m)!=null){var t=m.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);cir.setReturnValue(PiglinBehavior.inRange(m,t,0)&&BehaviorUtils.canSee(m,t));}}
    @Inject(method="crossbowAttack(Lnet/minecraft/world/entity/Mob;Lnet/minecraft/world/entity/LivingEntity;)V",at=@At("TAIL"))
    private void buildup$track(Mob e,LivingEntity target,CallbackInfo ci){if(e instanceof Piglin m&&PiglinBehavior.instance()!=null)PiglinBehavior.instance().trackUse(m);}
    @Inject(method="stop(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Mob;J)V",at=@At("TAIL"))
    private void buildup$stop(ServerLevel l,Mob e,long time,CallbackInfo ci){if(e instanceof Piglin m&&PiglinBehavior.instance()!=null)PiglinBehavior.instance().stopUse(m);}
}
