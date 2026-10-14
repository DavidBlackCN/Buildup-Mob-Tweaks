package com.davidblackcn.buildupmobtweaks.villager.farmer;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.villager.Villager;

/** Trusted server code; data packs may only reference a registered tier-1 handler. */
public interface CropHandler {
    int interactionTier();
    default int version(){return 1;}
    boolean mature(ServerLevel level, BlockPos pos, CropProfile profile);
    boolean harvest(ServerLevel level, Villager farmer, BlockPos pos, CropProfile profile);
    boolean plant(ServerLevel level, Villager farmer, BlockPos pos, CropProfile profile);
}
