package com.davidblackcn.buildupmobtweaks.mixin;
import com.davidblackcn.buildupmobtweaks.combat.HostileCombat;
import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import net.minecraft.world.entity.monster.illager.Pillager;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(Pillager.class)
public abstract class PillagerWeaponMixin {
    @Inject(method = "canUseNonMeleeWeapon(Lnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private void buildup$capability(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (HostileCombat.instance() != null && HostileCombat.instance().enabled((Pillager) (Object) this, FeatureId.PILLAGER_CROSSBOW_COMPATIBILITY)
                && !((Pillager)(Object)this).getType().builtInRegistryHolder().is(com.davidblackcn.buildupmobtweaks.combat.S2Tags.RANGED_EXCLUDED) && HostileCombat.compatibleCrossbow(stack)) cir.setReturnValue(true);
    }
}