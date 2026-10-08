/* Upstream behavior reference: Mob AI Tweaks, Copyright (c) 2024 N0t_UN_Owen (MIT). */
package com.davidblackcn.buildupmobtweaks.mixin.rebuild;

import com.davidblackcn.buildupmobtweaks.rebuild.pillager.PillagerBehavior;
import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.RangedCrossbowAttackGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.illager.Pillager;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RangedCrossbowAttackGoal.class)
public abstract class PillagerCrossbowGoalMixin {
    @Shadow @Final private Monster mob;
    @Shadow @Final private float attackRadiusSqr;
    @Inject(method = "isHoldingCrossbow()Z", at = @At("HEAD"), cancellable = true)
    private void buildup$weapon(CallbackInfoReturnable<Boolean> cir) {
        if (mob instanceof Pillager p && PillagerBehavior.instance() != null) {
            var behavior = PillagerBehavior.instance();
            if (behavior.melee(p)) cir.setReturnValue(false);
            else if (behavior.enabled(p, FeatureId.PILLAGER_CROSSBOW_COMPATIBILITY))
                cir.setReturnValue(behavior.crossbow(p, p.getMainHandItem()) || behavior.crossbow(p, p.getOffhandItem()));
        }
    }
    @Redirect(method = "tick()V", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/ai/goal/RangedCrossbowAttackGoal;attackRadiusSqr:F"))
    private float buildup$range(RangedCrossbowAttackGoal<?> goal) {
        return mob instanceof Pillager p && PillagerBehavior.instance() != null
                && PillagerBehavior.instance().enabled(p, FeatureId.PILLAGER_RANGE) ? 225 : attackRadiusSqr;
    }
    @Redirect(method = "stop()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Monster;setTarget(Lnet/minecraft/world/entity/LivingEntity;)V"))
    private void buildup$handoff(Monster entity, LivingEntity target) {
        if (entity instanceof Pillager p && PillagerBehavior.instance() != null && PillagerBehavior.instance().melee(p)
                && PillagerBehavior.validTarget(p, p.getTarget())) return;
        entity.setTarget(target);
    }
    @Redirect(method = "tick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/ProjectileUtil;getWeaponHoldingHand(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/Item;)Lnet/minecraft/world/InteractionHand;"))
    private InteractionHand buildup$crossbowHand(LivingEntity user, Item item) {
        if (user instanceof Pillager p && PillagerBehavior.instance() != null
                && PillagerBehavior.instance().enabled(p, FeatureId.PILLAGER_CROSSBOW_COMPATIBILITY)) {
            var behavior = PillagerBehavior.instance();
            if (behavior.crossbow(p, p.getMainHandItem())) return InteractionHand.MAIN_HAND;
            if (behavior.crossbow(p, p.getOffhandItem())) return InteractionHand.OFF_HAND;
        }
        return ProjectileUtil.getWeaponHoldingHand(user, item);
    }
    @Redirect(method = "tick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/CrossbowItem;getChargeDuration(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;)I"))
    private int buildup$interruptedCharge(ItemStack stack, LivingEntity user) {
        // Vanilla resets CHARGING when use is interrupted, but still evaluates the empty stack this tick.
        if (user instanceof Pillager p && PillagerBehavior.instance() != null
                && PillagerBehavior.instance().enabled(p, FeatureId.PILLAGER_WEAPON_SWITCH) && stack.isEmpty()) return Integer.MAX_VALUE;
        return CrossbowItem.getChargeDuration(stack, user);
    }
    @Inject(method = "tick()V", at = @At("TAIL"))
    private void buildup$retreatAndShieldApproach(CallbackInfo ci) {
        if (mob instanceof Pillager p && PillagerBehavior.instance() != null) PillagerBehavior.instance().rangedTick(p);
    }
}
