package com.davidblackcn.buildupmobtweaks.traits.core;

import net.minecraft.world.entity.EntitySpawnReason;

/** UNKNOWN is deliberately distinct from EXTERNAL: creating an entity is not a trait grant. */
public enum SpawnSource {
    NATURAL, SPAWN_EGG, SPAWNER, RAID, STRUCTURE, EXTERNAL, UNKNOWN;

    public boolean automaticallySelects() { return this == NATURAL || this == SPAWN_EGG; }
    public static SpawnSource classify(EntitySpawnReason reason, boolean raid) {
        if (reason == null) return UNKNOWN;
        return switch (reason) {
            case NATURAL -> NATURAL;
            // Vanilla SpawnEggItemBehavior uses DISPENSER for the hostile species in this framework.
            case SPAWN_ITEM_USE, DISPENSER -> SPAWN_EGG;
            case SPAWNER, TRIAL_SPAWNER -> SPAWNER;
            case STRUCTURE -> STRUCTURE;
            case EVENT -> raid ? RAID : UNKNOWN;
            default -> UNKNOWN;
        };
    }
}
