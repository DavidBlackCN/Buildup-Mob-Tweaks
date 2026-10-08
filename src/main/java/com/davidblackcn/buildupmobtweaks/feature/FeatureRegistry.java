package com.davidblackcn.buildupmobtweaks.feature;

import com.davidblackcn.buildupmobtweaks.config.BuildupConfig;
import com.davidblackcn.buildupmobtweaks.traits.TraitSampler;
import com.davidblackcn.buildupmobtweaks.traits.TraitKind;
import java.util.Map;
import java.util.function.BooleanSupplier;

/** One shared gate for each implemented feature; read current synced values on every query. */
public final class FeatureRegistry {
    public record Declaration(FeatureId id, BooleanSupplier enabled) {}

    private final BuildupConfig config;
    private final Map<FeatureId, Declaration> declarations;

    public FeatureRegistry(BuildupConfig config) {
        this.config = config;
        declarations = Map.of(FeatureId.DIAGNOSTIC_PROBE,
                new Declaration(FeatureId.DIAGNOSTIC_PROBE, () -> config.general.diagnosticProbe.get()),
                FeatureId.EQUIPMENT_ASSIGNMENT, new Declaration(FeatureId.EQUIPMENT_ASSIGNMENT, () -> config.equipment.poolAssignment.get()),
                FeatureId.TRAITS, new Declaration(FeatureId.TRAITS, () -> config.traits.enabled.get()),
                FeatureId.TRAIT_COMMON_MARKER, new Declaration(FeatureId.TRAIT_COMMON_MARKER,
                        () -> config.traits.enabled.get() && config.traits.commonMarker.get()),
                FeatureId.TRAIT_ADVANCED_MARKER, new Declaration(FeatureId.TRAIT_ADVANCED_MARKER,
                        () -> config.traits.enabled.get() && config.traits.advancedMarker.get()),
                FeatureId.TRAIT_RARE_MARKER, new Declaration(FeatureId.TRAIT_RARE_MARKER,
                        () -> config.traits.enabled.get() && config.traits.rareMarker.get()));
    }

    public boolean isEnabled(FeatureId id) {
        Declaration declaration = declarations.get(id);
        return config.general.enabled.get() && declaration != null && declaration.enabled().getAsBoolean();
    }

    public boolean isTraitEnabled(String id) {
        for (TraitKind kind : TraitKind.values()) {
            if (kind.id().equals(id)) return isEnabled(kind.featureId());
        }
        return false;
    }

    public TraitSampler.Weights traitWeights(boolean cow) {
        BuildupConfig.Chances chances = cow ? config.traits.cow : config.traits.zombieFamily;
        return new TraitSampler.Weights(
                isEnabled(FeatureId.TRAIT_COMMON_MARKER) ? chances.common.get() : 0,
                isEnabled(FeatureId.TRAIT_ADVANCED_MARKER) ? chances.advanced.get() : 0,
                isEnabled(FeatureId.TRAIT_RARE_MARKER) ? chances.rare.get() : 0);
    }

    public int equipmentChance() { return config.equipment.assignmentChance.get(); }

    public int diagnosticLines() {
        return config.performance.diagnosticLines.get();
    }
}
