package com.davidblackcn.buildupmobtweaks.mixin;

import com.davidblackcn.buildupmobtweaks.combat.SkeletonCombat;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RangedBowAttackGoal.class)
public abstract class SkeletonBowGoalMixin {
    @Shadow @Final private Monster mob;
    @Inject(method = "isHoldingBow()Z", at = @At("HEAD"), cancellable = true)
    private void buildup$holding(CallbackInfoReturnable<Boolean> cir) {
        var combat = SkeletonCombat.instance();
        if (mob instanceof AbstractSkeleton skeleton && combat != null && combat.handlesWeapons(skeleton)) cir.setReturnValue(combat.usesBow(skeleton));
    }
    @WrapOperation(method = "tick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/ProjectileUtil;getWeaponHoldingHand(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/Item;)Lnet/minecraft/world/InteractionHand;"))
    private InteractionHand buildup$drawHand(LivingEntity entity, Item item, Operation<InteractionHand> original) {
        var hand = original.call(entity, item);
        var combat = SkeletonCombat.instance();
        return mob instanceof AbstractSkeleton skeleton && combat != null ? combat.bowHand(skeleton, hand) : hand;
    }
    @Inject(method = "tick()V", at = @At("TAIL"))
    private void buildup$retreat(org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if (com.davidblackcn.buildupmobtweaks.combat.EnvironmentCombat.on(mob,com.davidblackcn.buildupmobtweaks.feature.FeatureId.SKELETON_AIM_FIX)
                && SkeletonCombat.validTarget(mob,mob.getTarget())) mob.getLookControl().setLookAt(mob.getTarget(),30,30);
        if (mob instanceof AbstractSkeleton skeleton && SkeletonCombat.instance() != null) SkeletonCombat.instance().afterBowTick(skeleton);
    }
    @WrapOperation(method = "tick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/control/MoveControl;strafe(FF)V"))
    private void buildup$safeStrafe(MoveControl control, float forward, float sideways, Operation<Void> original) {
        var combat = SkeletonCombat.instance();
        if (mob instanceof AbstractSkeleton skeleton && SkeletonCombat.eligible(skeleton) && combat != null) {
            var safe = combat.strafe(skeleton, forward, sideways);
            original.call(control, safe[0], safe[1]);
        } else original.call(control, forward, sideways);
    }
}