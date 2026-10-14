package com.davidblackcn.buildupmobtweaks.villager;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;

/** Bounded loaded-block checks; does not change path costs, terrain or movement physics. */
public final class NavigationSafety {
    public static boolean hazardous(ServerLevel level, BlockPos feet) {
        if (!level.hasChunkAt(feet) || !level.hasChunkAt(feet.below(4))) return false;
        if (harmful(level.getBlockState(feet)) || harmful(level.getBlockState(feet.above()))
                || harmful(level.getBlockState(feet.below()))) return true;
        // Water channels and a fall beyond three blocks are excluded from elective movement.
        for (int drop = 0; drop <= 4; drop++) {
            var p = feet.below(drop);
            var s = level.getBlockState(p);
            if (harmful(s) || s.getFluidState().is(FluidTags.WATER)) return true;
            if (drop > 0 && !s.getCollisionShape(level, p).isEmpty()) return false;
        }
        return true;
    }
    private static boolean harmful(BlockState s) {
        return s.getFluidState().is(FluidTags.LAVA) || s.is(BlockTags.FIRE) || s.is(Blocks.MAGMA_BLOCK)
                || s.is(Blocks.CACTUS) || s.is(Blocks.POWDER_SNOW)
                || s.getBlock() instanceof CampfireBlock && s.getValue(CampfireBlock.LIT);
    }
    public static boolean hazardousPath(ServerLevel level, Path path) {
        if (path == null || path.isDone()) return false;
        for (int i = path.getNextNodeIndex(); i < Math.min(path.getNodeCount(), path.getNextNodeIndex() + 4); i++)
            if (hazardous(level, path.getNodePos(i))) return true;
        return false;
    }
    private NavigationSafety() {}
}
