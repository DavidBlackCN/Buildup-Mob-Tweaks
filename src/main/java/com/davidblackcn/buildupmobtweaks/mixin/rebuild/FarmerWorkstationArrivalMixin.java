package com.davidblackcn.buildupmobtweaks.mixin.rebuild;

import com.davidblackcn.buildupmobtweaks.villager.farmer.WorkstationNavigation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MoveToTargetSink.class)
public abstract class FarmerWorkstationArrivalMixin {
    @Inject(method="reachedTarget",at=@At("HEAD"),cancellable=true)
    private void buildup$insideWorkstationIsNotArrival(Mob mob,WalkTarget walk,CallbackInfoReturnable<Boolean> cir){
        if(!(mob instanceof Villager v)||!(v.level() instanceof ServerLevel l))return;
        var target=walk.getTarget().currentBlockPosition();
        // Inside the hollow block, native Manhattan distance is zero and skips path creation.
        if(v.blockPosition().equals(target)&&WorkstationNavigation.isJobTarget(v,l,target))cir.setReturnValue(false);
    }
}
