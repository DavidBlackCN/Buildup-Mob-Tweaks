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
        var gates = new java.util.EnumMap<FeatureId, Declaration>(FeatureId.class);
        add(gates, FeatureId.DIAGNOSTIC_PROBE, () -> config.general.diagnosticProbe.get());
        add(gates, FeatureId.EQUIPMENT_ASSIGNMENT, () -> config.equipment.poolAssignment.get());
        add(gates, FeatureId.TRAITS, () -> config.traits.enabled.get());
        add(gates, FeatureId.TRAIT_COMMON_MARKER, () -> config.traits.enabled.get() && config.traits.commonMarker.get());
        add(gates, FeatureId.TRAIT_ADVANCED_MARKER, () -> config.traits.enabled.get() && config.traits.advancedMarker.get());
        add(gates, FeatureId.TRAIT_RARE_MARKER, () -> config.traits.enabled.get() && config.traits.rareMarker.get());
        add(gates, FeatureId.SKELETON_SAFE_STRAFING, () -> config.hostile.skeleton.safeStrafing.get());
        add(gates, FeatureId.SKELETON_TARGET_VALIDATION, () -> config.hostile.skeleton.targetValidation.get());
        add(gates, FeatureId.SKELETON_WEAPON_SWITCHING, () -> config.hostile.skeleton.weaponSwitching.get());
        add(gates, FeatureId.SKELETON_BOW_COMPATIBILITY, () -> config.hostile.skeleton.bowCompatibility.get());
        add(gates, FeatureId.SKELETON_SNIPING, () -> config.traits.enabled.get() && config.hostile.skeleton.skeletonSniping.get());
        add(gates, FeatureId.STRAY_JUMP_SHOT, () -> config.traits.enabled.get() && config.hostile.skeleton.strayJumpShot.get());
        add(gates, FeatureId.BOGGED_SPORE_RETREAT, () -> config.traits.enabled.get() && config.hostile.skeleton.boggedSporeRetreat.get());
        declarations = Map.copyOf(gates);
    }

    private static void add(Map<FeatureId, Declaration> gates, FeatureId id, BooleanSupplier enabled) {
        gates.put(id, new Declaration(id, enabled));
    }

    public int skeletonChance(FeatureId id) {
        return switch (id) {
            case SKELETON_SNIPING -> config.hostile.skeleton.skeletonChance.get();
            case STRAY_JUMP_SHOT -> config.hostile.skeleton.strayChance.get();
            case BOGGED_SPORE_RETREAT -> config.hostile.skeleton.boggedChance.get();
            default -> 0;
        };
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
