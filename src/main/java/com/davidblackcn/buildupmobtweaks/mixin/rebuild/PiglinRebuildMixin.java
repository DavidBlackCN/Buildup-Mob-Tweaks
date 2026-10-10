package com.davidblackcn.buildupmobtweaks.mixin.rebuild;

import com.davidblackcn.buildupmobtweaks.rebuild.piglin.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.*;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(Piglin.class)
public abstract class PiglinRebuildMixin {
    @Inject(method="customServerAiStep(Lnet/minecraft/server/level/ServerLevel;)V",at=@At("HEAD"))
    private void buildup$before(ServerLevel level,CallbackInfo ci){if(PiglinBehavior.instance()!=null)PiglinBehavior.instance().beforeAi((Piglin)(Object)this);}
    @Inject(method="customServerAiStep(Lnet/minecraft/server/level/ServerLevel;)V",at=@At("TAIL"))
    private void buildup$after(ServerLevel level,CallbackInfo ci){if(PiglinBehavior.instance()!=null)PiglinBehavior.instance().afterAi((Piglin)(Object)this);}
    @Inject(method="canUseNonMeleeWeapon(Lnet/minecraft/world/item/ItemStack;)Z",at=@At("RETURN"),cancellable=true)
    private void buildup$weapon(ItemStack item,CallbackInfoReturnable<Boolean> cir){var b=PiglinBehavior.instance();if(b!=null&&b.compatible((Piglin)(Object)this)&&PiglinBehavior.crossbow(item))cir.setReturnValue(true);}
    @Inject(method="performRangedAttack(Lnet/minecraft/world/entity/LivingEntity;F)V",at=@At("HEAD"))
    private void buildup$shotStart(LivingEntity target,float power,CallbackInfo ci){if(PiglinState.known((Piglin)(Object)this)&&PiglinBehavior.instance()!=null)PiglinBehavior.instance().beforeShot((Piglin)(Object)this);}
    @Inject(method="performRangedAttack(Lnet/minecraft/world/entity/LivingEntity;F)V",at=@At("RETURN"))
    private void buildup$shotEnd(LivingEntity target,float power,CallbackInfo ci){if(PiglinState.known((Piglin)(Object)this)&&PiglinBehavior.instance()!=null)PiglinBehavior.instance().afterShot((Piglin)(Object)this);}
    @Inject(method="getArmPose()Lnet/minecraft/world/entity/monster/piglin/PiglinArmPose;",at=@At("RETURN"),cancellable=true)
    private void buildup$pose(CallbackInfoReturnable<PiglinArmPose> cir){var m=(Piglin)(Object)this;
        if(!Boolean.TRUE.equals(m.getAttached(PiglinState.COMPAT))||cir.getReturnValue()==PiglinArmPose.DANCING||cir.getReturnValue()==PiglinArmPose.ADMIRING_ITEM)return;
        if(m.isUsingItem()&&PiglinBehavior.crossbow(m.getUseItem()))cir.setReturnValue(PiglinArmPose.CROSSBOW_CHARGE);
        else {var h=PiglinBehavior.hand(m);if(h!=null&&CrossbowItem.isCharged(m.getItemInHand(h)))cir.setReturnValue(PiglinArmPose.CROSSBOW_HOLD);}
    }
}
