package com.davidblackcn.buildupmobtweaks.traits.core;

import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import java.util.Set;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;

/** Immutable design metadata. No goals, item stacks, cooldowns or world counters. */
public record TraitDefinition(FeatureId feature, int version, int tier, Category category,
                              Polarity polarity, Set<String> species) {
    public enum Category { DEFENSE, COMBAT_TACTICS, ENVIRONMENTAL_COMBAT }
    public enum Polarity { POSITIVE, NEGATIVE, MIXED }
    public TraitDefinition {
        if (version < 1 || tier < 2 || tier > 3) throw new IllegalArgumentException("Invalid trait metadata");
        java.util.Objects.requireNonNull(feature);
        java.util.Objects.requireNonNull(category);
        java.util.Objects.requireNonNull(polarity);
        species = Set.copyOf(species);
    }
    public Identifier id() { return feature.id(); }
    public boolean appliesTo(Entity entity) {
        return species.contains(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString());
    }
}
