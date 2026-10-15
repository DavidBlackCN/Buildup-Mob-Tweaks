package com.davidblackcn.buildupmobtweaks.villager;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.villager.Villager;

/** One interruptible activity. Farmer and A3 work slots share this lifecycle contract. */
public interface WorkActivity {
    enum Result { CONTINUE, COMPLETE, FAILED }
    enum StopReason { COMPLETE, FAILED, INTERRUPTED, DISABLED, UNLOADED }
    record Context(ServerLevel level, Villager villager, ProfessionProfile profile) {}
    Identifier id();
    int interactionTier();
    boolean canStart(Context context);
    void start(Context context);
    Result tick(Context context);
    void stop(Context context, StopReason reason);
}
