package com.davidblackcn.buildupmobtweaks.mixin.rebuild;

import com.davidblackcn.buildupmobtweaks.villager.farmer.WorkstationNavigation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Also covers native workstation strolls; priority activities and external owners are untouched. */
@Mixin(WalkNodeEvaluator.class)
public abstract class FarmerComposterNodeMixin {
    @Inject(method="getPathTypeOfMob",at=@At("HEAD"),cancellable=true)
    private void buildup$avoidComposterFooting(PathfindingContext ctx,int x,int y,int z,Mob mob,CallbackInfoReturnable<PathType> cir){
        if(!(mob instanceof Villager v)||!(v.level() instanceof ServerLevel l))return;
        var p=new BlockPos(x,y,z);
        if((ctx.getBlockState(p).is(Blocks.COMPOSTER)||ctx.getBlockState(p.below()).is(Blocks.COMPOSTER))
                &&WorkstationNavigation.controls(v,l))cir.setReturnValue(PathType.BLOCKED);
    }
}
