package com.davidblackcn.buildupmobtweaks.mixin.rebuild;

import com.davidblackcn.buildupmobtweaks.villager.VillagerCore;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.villager.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Villager.class)
public abstract class VillagerCoreMixin {
    @Inject(method="tick()V",at=@At("HEAD"))
    private void buildup$farmerBoundary(CallbackInfo ci){
        var v=(Villager)(Object)this;
        if(v.level() instanceof ServerLevel level)com.davidblackcn.buildupmobtweaks.villager.farmer.FarmerModule.boundary(v,level);
    }
    @Inject(method="customServerAiStep(Lnet/minecraft/server/level/ServerLevel;)V", at=@At("HEAD"))
    private void buildup$beforeBrain(ServerLevel level, CallbackInfo ci) {
        VillagerCore.beforeBrain((Villager)(Object)this, level);
    }
    @Inject(method="customServerAiStep(Lnet/minecraft/server/level/ServerLevel;)V", at=@At("RETURN"))
    private void buildup$afterBrain(ServerLevel level, CallbackInfo ci) {
        VillagerCore.afterBrain((Villager)(Object)this, level);
    }
}
