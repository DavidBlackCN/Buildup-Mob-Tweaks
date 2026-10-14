package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.villager.farmer.FarmerModule;
import net.minecraft.world.entity.ai.behavior.UseBonemeal;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(UseBonemeal.class)
public abstract class FarmerBonemealMixin {
    @Inject(method="checkExtraStartConditions(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/npc/villager/Villager;)Z",at=@At("HEAD"),cancellable=true)
    private void buildup$start(ServerLevel l,Villager v,CallbackInfoReturnable<Boolean> ci){if(FarmerModule.owner(v)!=FarmerModule.Owner.VANILLA)ci.setReturnValue(false);}
    @Inject(method="canStillUse(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/npc/villager/Villager;J)Z",at=@At("HEAD"),cancellable=true)
    private void buildup$continue(ServerLevel l,Villager v,long time,CallbackInfoReturnable<Boolean> ci){if(FarmerModule.owner(v)!=FarmerModule.Owner.VANILLA)ci.setReturnValue(false);}
}
