package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.villager.farmer.FarmerWorkBehavior;
import net.minecraft.world.entity.ai.behavior.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(VillagerGoalPackages.class)
public abstract class FarmerWorkPackageMixin {
    @Redirect(method="getWorkPackage",at=@At(value="NEW",target="net/minecraft/world/entity/ai/behavior/HarvestFarmland"))
    private static HarvestFarmland buildup$farmerSlot(){return new FarmerWorkBehavior();}
}
