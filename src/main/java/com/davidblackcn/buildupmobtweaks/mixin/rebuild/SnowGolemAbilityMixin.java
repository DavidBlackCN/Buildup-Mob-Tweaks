package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.ability.AbilityBehavior;
import net.minecraft.world.entity.animal.golem.SnowGolem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(SnowGolem.class)
public abstract class SnowGolemAbilityMixin {
    @Inject(method="mobInteract(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;",at=@At("HEAD"),cancellable=true)
    private void buildup$pumpkin(Player p,InteractionHand hand,CallbackInfoReturnable<InteractionResult> ci){var b=AbilityBehavior.instance();if(b!=null&&b.returnPumpkin((SnowGolem)(Object)this,p,hand))ci.setReturnValue(InteractionResult.SUCCESS);}
    @Inject(method="setPumpkin(Z)V",at=@At("TAIL")) private void buildup$armor(boolean pumpkin,CallbackInfo ci){var m=(SnowGolem)(Object)this;var b=AbilityBehavior.instance();if(b!=null&&!m.level().isClientSide())b.armor(m);}
    @Inject(method="performRangedAttack(Lnet/minecraft/world/entity/LivingEntity;F)V",at=@At("HEAD"),cancellable=true)
    private void buildup$throw(LivingEntity target,float power,CallbackInfo ci){var m=(SnowGolem)(Object)this;var b=AbilityBehavior.instance();if(b!=null&&!m.level().isClientSide()&&b.prepare(m,target))ci.cancel();}
}
