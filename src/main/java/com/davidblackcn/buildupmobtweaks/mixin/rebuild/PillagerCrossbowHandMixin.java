package com.davidblackcn.buildupmobtweaks.mixin.rebuild;

import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import com.davidblackcn.buildupmobtweaks.rebuild.pillager.PillagerBehavior;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.illager.Pillager;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ProjectileUtil.class)
public abstract class PillagerCrossbowHandMixin {
    @Inject(method = "getWeaponHoldingHand(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/Item;)Lnet/minecraft/world/InteractionHand;", at = @At("RETURN"), cancellable = true)
    private static void buildup$actualCrossbow(LivingEntity entity, Item item, CallbackInfoReturnable<InteractionHand> cir) {
        if (entity instanceof Pillager p && item == Items.CROSSBOW && PillagerBehavior.instance() != null) {
            var behavior = PillagerBehavior.instance();
            if (behavior.enabled(p, FeatureId.PILLAGER_CROSSBOW_COMPATIBILITY)) {
                if (behavior.crossbow(p, p.getMainHandItem())) cir.setReturnValue(InteractionHand.MAIN_HAND);
                else if (behavior.crossbow(p, p.getOffhandItem())) cir.setReturnValue(InteractionHand.OFF_HAND);
            }
        }
    }
}
