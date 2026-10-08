package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.skeleton.*;
import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(ProjectileUtil.class)
public abstract class SkeletonBowHandMixin {
    @Inject(method="getWeaponHoldingHand(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/Item;)Lnet/minecraft/world/InteractionHand;",at=@At("RETURN"),cancellable=true)
    private static void buildup$hand(LivingEntity entity,Item item,CallbackInfoReturnable<InteractionHand> ci){
        if(entity instanceof AbstractSkeleton mob&&item==Items.BOW&&SkeletonBehavior.instance()!=null){var b=SkeletonBehavior.instance();
            if(b.enabled(mob,FeatureId.SKELETON_BOW_COMPATIBILITY)&&b.hasBow(mob))ci.setReturnValue(b.hand(mob));}
    }
}
