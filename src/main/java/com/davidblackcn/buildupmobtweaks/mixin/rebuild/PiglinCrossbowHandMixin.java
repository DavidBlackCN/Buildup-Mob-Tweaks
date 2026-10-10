package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.piglin.PiglinBehavior;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(ProjectileUtil.class)
public abstract class PiglinCrossbowHandMixin {
    @Inject(method="getWeaponHoldingHand(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/Item;)Lnet/minecraft/world/InteractionHand;",at=@At("RETURN"),cancellable=true)
    private static void buildup$hand(LivingEntity e,Item item,CallbackInfoReturnable<InteractionHand> cir){if(item==Items.CROSSBOW&&e instanceof Piglin m&&PiglinBehavior.instance()!=null&&PiglinBehavior.instance().compatible(m)){var hand=PiglinBehavior.hand(m);if(hand!=null)cir.setReturnValue(hand);}}
}
