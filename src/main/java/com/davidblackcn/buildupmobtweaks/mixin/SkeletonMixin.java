package com.davidblackcn.buildupmobtweaks.mixin;

import com.davidblackcn.buildupmobtweaks.combat.SkeletonCombat;
import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractSkeleton.class)
public abstract class SkeletonMixin {
    @WrapOperation(method = "reassessWeaponGoal()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Ljava/lang/Object;)Z"))
    private boolean buildup$bowGoal(ItemStack stack, Object item, Operation<Boolean> original) {
        var combat = SkeletonCombat.instance();
        var self = (AbstractSkeleton) (Object) this;
        return item == Items.BOW && combat != null && combat.handlesWeapons(self) ? combat.usesBow(self) : original.call(stack, item);
    }
    @WrapOperation(method = {"reassessWeaponGoal()V", "performRangedAttack(Lnet/minecraft/world/entity/LivingEntity;F)V"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/ProjectileUtil;getWeaponHoldingHand(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/Item;)Lnet/minecraft/world/InteractionHand;"))
    private InteractionHand buildup$bowHand(LivingEntity entity, Item item, Operation<InteractionHand> original) {
        var hand = original.call(entity, item);
        var combat = SkeletonCombat.instance();
        return combat == null ? hand : combat.bowHand((AbstractSkeleton) (Object) this, hand);
    }
    @Inject(method = "canUseNonMeleeWeapon(Lnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private void buildup$bowCapability(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        var combat = SkeletonCombat.instance();
        if (combat != null && combat.enabled((AbstractSkeleton) (Object) this, FeatureId.SKELETON_BOW_COMPATIBILITY)
                && stack.getItem() instanceof BowItem) cir.setReturnValue(true);
    }
    @ModifyArg(method = "performRangedAttack(Lnet/minecraft/world/entity/LivingEntity;F)V", index = 7,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/Projectile;spawnProjectileUsingShoot(Lnet/minecraft/world/entity/projectile/Projectile;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;DDDFF)Lnet/minecraft/world/entity/projectile/Projectile;"))
    private float buildup$precision(float original) {
        var combat = SkeletonCombat.instance();
        return combat == null ? original : combat.shotUncertainty((AbstractSkeleton) (Object) this, original);
    }
}