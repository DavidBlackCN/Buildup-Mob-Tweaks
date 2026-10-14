package com.davidblackcn.buildupmobtweaks.traits.core;

import net.minecraft.resources.Identifier;

/** Entitlement only; action recovery and equipment ownership stay in their existing attachments. */
public record TraitInstance(Identifier id, int version, SpawnSource source) {
    public TraitInstance {
        java.util.Objects.requireNonNull(id);
        java.util.Objects.requireNonNull(source);
        if (version < 1) throw new IllegalArgumentException("Invalid trait version");
    }
}
