package com.davidblackcn.buildupmobtweaks.mixin.rebuild;

import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import com.davidblackcn.buildupmobtweaks.rebuild.pillager.PillagerBehavior;
import net.minecraft.world.entity.monster.illager.Pillager;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Pillager.class)
public abstract class PillagerWeaponMixin {
    @Inject(method = "canUseNonMeleeWeapon(Lnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private void buildup$compatible(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        var p = (Pillager) (Object) this; var behavior = PillagerBehavior.instance();
        if (behavior != null && behavior.enabled(p, FeatureId.PILLAGER_CROSSBOW_COMPATIBILITY) && behavior.crossbow(p, stack)) cir.setReturnValue(true);
    }
}
