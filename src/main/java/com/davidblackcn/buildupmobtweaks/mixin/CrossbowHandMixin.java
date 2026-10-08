package com.davidblackcn.buildupmobtweaks.mixin;
import com.davidblackcn.buildupmobtweaks.combat.HostileCombat;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(ProjectileUtil.class)
public abstract class CrossbowHandMixin {
    @Inject(method = "getWeaponHoldingHand(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/Item;)Lnet/minecraft/world/InteractionHand;", at = @At("RETURN"), cancellable = true)
    private static void buildup$hand(LivingEntity entity, Item item, CallbackInfoReturnable<InteractionHand> cir) {
        if (item == Items.CROSSBOW && HostileCombat.instance() != null) cir.setReturnValue(HostileCombat.instance().crossbowHand(entity, cir.getReturnValue()));
    }
}