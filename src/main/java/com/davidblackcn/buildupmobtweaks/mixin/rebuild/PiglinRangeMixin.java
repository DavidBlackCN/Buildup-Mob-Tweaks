package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.piglin.PiglinBehavior;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.monster.piglin.Piglin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(BehaviorUtils.class)
public abstract class PiglinRangeMixin {
    @Inject(method="isWithinAttackRange(Lnet/minecraft/world/entity/Mob;Lnet/minecraft/world/entity/LivingEntity;I)Z",at=@At("HEAD"),cancellable=true)
    private static void buildup$range(Mob e,LivingEntity target,int reduction,CallbackInfoReturnable<Boolean> cir){if(e instanceof Piglin m&&PiglinBehavior.instance()!=null&&PiglinBehavior.instance().compatible(m)&&PiglinBehavior.hand(m)!=null)cir.setReturnValue(PiglinBehavior.inRange(m,target,reduction));}
}
