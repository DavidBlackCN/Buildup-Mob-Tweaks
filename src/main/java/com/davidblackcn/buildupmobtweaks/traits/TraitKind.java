package com.davidblackcn.buildupmobtweaks.traits;

import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import java.util.Set;

/** Diagnostic markers only; these definitions grant no combat or equipment behavior. */
public enum TraitKind {
    COMMON(FeatureId.TRAIT_COMMON_MARKER, 1),
    ADVANCED(FeatureId.TRAIT_ADVANCED_MARKER, 2),
    RARE(FeatureId.TRAIT_RARE_MARKER, 3);

    public static final Set<String> ENTITY_IDS = Set.of(
            "minecraft:cow", "minecraft:zombie", "minecraft:husk", "minecraft:drowned");
    private final FeatureId featureId;
    private final int level;

    TraitKind(FeatureId featureId, int level) {
        this.featureId = featureId;
        this.level = level;
    }

    public String id() { return featureId.id().toString(); }
    public FeatureId featureId() { return featureId; }
    public int level() { return level; }
    public String exclusiveGroup() { return "diagnostic_marker"; }
    public Set<String> requiredEquipmentAbilities() { return Set.of(); }
    public int cooldownTicks() { return 0; }
}
