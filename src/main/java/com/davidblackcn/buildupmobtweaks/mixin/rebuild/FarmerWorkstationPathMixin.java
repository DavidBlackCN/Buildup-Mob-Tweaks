package com.davidblackcn.buildupmobtweaks.mixin.rebuild;

import com.davidblackcn.buildupmobtweaks.villager.farmer.WorkstationNavigation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.ai.navigation.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GroundPathNavigation.class)
public abstract class FarmerWorkstationPathMixin extends PathNavigation {
    protected FarmerWorkstationPathMixin(Mob mob,Level level){super(mob,level);}
    @Inject(method="createPath(Lnet/minecraft/core/BlockPos;I)Lnet/minecraft/world/level/pathfinder/Path;",at=@At("HEAD"),cancellable=true)
    private void buildup$standBesideWorkstation(BlockPos pos,int reach,CallbackInfoReturnable<Path> cir){
        if(!(mob instanceof Villager v)||!(level instanceof ServerLevel l)||!WorkstationNavigation.isJobTarget(v,l,pos))return;
        var targets=WorkstationNavigation.standingTargets(l,pos);
        // Native search keeps target/recompute/stuck bookkeeping, with an exact safe endpoint.
        var path=super.createPath(targets,8,false,0);
        cir.setReturnValue(WorkstationNavigation.safeApproach(l,path,targets)?path:null);
    }
}
