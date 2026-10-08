package com.davidblackcn.buildupmobtweaks.mixin.rebuild;

import com.davidblackcn.buildupmobtweaks.rebuild.pillager.PillagerBehavior;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.illager.Pillager;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class PillagerConsumptionMixin {
    @Unique private int buildup$nutrition;
    @Unique private int buildup$beforeCount;
    @Unique private Item buildup$food;
    @Inject(method = "completeUsingItem()V", at = @At("HEAD"))
    private void buildup$beforeConsumption(CallbackInfo ci) {
        buildup$nutrition = 0;
        if ((Object) this instanceof Pillager p && !p.level().isClientSide() && PillagerBehavior.instance() != null
                && PillagerBehavior.instance().mealSlot(p) >= 0 && p.getUsedItemHand() == InteractionHand.OFF_HAND && p.isUsingItem()) {
            var stack = p.getOffhandItem(); var food = stack.get(DataComponents.FOOD);
            if (food != null) { buildup$nutrition = food.nutrition(); buildup$beforeCount = stack.getCount(); buildup$food = stack.getItem(); }
        }
    }
    @Inject(method = "completeUsingItem()V", at = @At("RETURN"))
    private void buildup$afterConsumption(CallbackInfo ci) {
        if (buildup$nutrition > 0 && (Object) this instanceof Pillager p) {
            var remainder = p.getOffhandItem();
            if (remainder.getCount() < buildup$beforeCount || remainder.getItem() != buildup$food)
                PillagerBehavior.instance().consumed(p, buildup$nutrition);
        }
        buildup$nutrition = 0; buildup$food = null;
    }
}
