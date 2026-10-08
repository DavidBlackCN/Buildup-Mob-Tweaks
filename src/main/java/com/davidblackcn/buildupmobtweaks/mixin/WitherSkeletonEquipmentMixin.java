package com.davidblackcn.buildupmobtweaks.mixin;
import com.davidblackcn.buildupmobtweaks.combat.HostileCombat;
import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(WitherSkeleton.class)
public abstract class WitherSkeletonEquipmentMixin {
    @ModifyArg(method="populateDefaultEquipmentSlots(Lnet/minecraft/util/RandomSource;Lnet/minecraft/world/DifficultyInstance;)V",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/monster/skeleton/WitherSkeleton;setItemSlot(Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/world/item/ItemStack;)V"),index=1)
    private ItemStack buildup$weapon(ItemStack original){
        var mob=(WitherSkeleton)(Object)this;
        return HostileCombat.instance()!=null && HostileCombat.instance().gate(FeatureId.WITHER_SKELETON_BOW) && mob.getRandom().nextInt(1000)<10 ? new ItemStack(Items.BOW) : original;
    }
}