package com.davidblackcn.buildupmobtweaks.mixin;
import com.davidblackcn.buildupmobtweaks.combat.HostileCombat;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(Piglin.class)
public abstract class PiglinWeaponMixin {
    @Inject(method="onCrossbowAttackPerformed()V",at=@At("TAIL"))
    private void buildup$swing(org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci){
        if(com.davidblackcn.buildupmobtweaks.combat.EnvironmentCombat.on((Piglin)(Object)this,com.davidblackcn.buildupmobtweaks.feature.FeatureId.PIGLIN_SHOT_SWING))((Piglin)(Object)this).swingForAttack(net.minecraft.world.InteractionHand.MAIN_HAND);
    }
    @Inject(method="canUseNonMeleeWeapon(Lnet/minecraft/world/item/ItemStack;)Z",at=@At("HEAD"),cancellable=true)
    private void buildup$crossbow(ItemStack stack,CallbackInfoReturnable<Boolean> cir){
        if(HostileCombat.instance()!=null && HostileCombat.instance().crossbow((Piglin)(Object)this) && HostileCombat.compatibleCrossbow(stack))cir.setReturnValue(true);
    }
}