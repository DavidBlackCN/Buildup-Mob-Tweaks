package com.davidblackcn.buildupmobtweaks.mixin.rebuild;

import com.davidblackcn.buildupmobtweaks.equipment.EquipmentService;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WitherSkeleton.class)
public abstract class WitherBirthEquipmentMixin {
    @WrapOperation(method="populateDefaultEquipmentSlots(Lnet/minecraft/util/RandomSource;Lnet/minecraft/world/DifficultyInstance;)V",
            at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/monster/skeleton/WitherSkeleton;setItemSlot(Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/world/item/ItemStack;)V"))
    private void buildup$birth(WitherSkeleton mob, EquipmentSlot slot, ItemStack original, Operation<Void> call) {
        var service=EquipmentService.instance();
        if(service==null||service.defaultWeapon(mob))call.call(mob,slot,original);
    }
}
