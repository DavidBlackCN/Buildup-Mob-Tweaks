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
        add(gates, FeatureId.ZOMBIE_SHIELD_USE, () -> config.hostile.zombie.shieldUse.get());
        add(gates, FeatureId.ZOMBIE_DOOR_GUARD, () -> config.traits.enabled.get() && config.hostile.zombie.doorGuard.get());
        add(gates, FeatureId.ZOMBIE_ACTIVE_GUARD, () -> config.traits.enabled.get() && config.hostile.zombie.activeGuard.get());
        add(gates, FeatureId.HUSK_SAND_BURROW, () -> config.traits.enabled.get() && config.hostile.zombie.sandBurrow.get());
        add(gates, FeatureId.ZOMBIE_BABY_RIDER, () -> config.traits.enabled.get() && config.hostile.zombie.babyRider.get());
        add(gates, FeatureId.DROWNED_TRIDENT_CONSERVATION, () -> config.hostile.drowned.tridentConservation.get());
        add(gates, FeatureId.DROWNED_TRIDENT_RECOVERY, () -> config.hostile.drowned.tridentRecovery.get());
        add(gates, FeatureId.DROWNED_TRIDENT_PLAYER_PICKUP, () -> config.hostile.drowned.tridentPlayerPickup.get());
        add(gates, FeatureId.PILLAGER_RETREAT, () -> config.hostile.raid.pillagerRetreat.get());
        add(gates, FeatureId.PILLAGER_WEAPON_SWITCH, () -> config.hostile.raid.pillagerWeaponSwitch.get());
        add(gates, FeatureId.VINDICATOR_SUPPORT, () -> config.hostile.raid.vindicatorSupport.get());
        add(gates, FeatureId.EVOKER_VEX_LIMIT, () -> config.hostile.raid.evokerVexLimit.get());
        add(gates, FeatureId.EVOKER_SUMMON_COOLDOWN, () -> config.hostile.raid.evokerSummonCooldown.get());
        add(gates, FeatureId.WITCH_WINDUP, () -> config.hostile.raid.witchWindup.get());
        add(gates, FeatureId.WITCH_THROW_COOLDOWN, () -> config.hostile.raid.witchThrowCooldown.get());
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

    public int zombieChance(FeatureId id) {
        return switch (id) {
            case ZOMBIE_DOOR_GUARD -> config.hostile.zombie.doorChance.get();
            case ZOMBIE_ACTIVE_GUARD -> config.hostile.zombie.guardChance.get();
            case HUSK_SAND_BURROW -> config.hostile.zombie.burrowChance.get();
            case ZOMBIE_BABY_RIDER -> config.hostile.zombie.riderChance.get();
            default -> 0;
        };
    }
    public int zombieCooldown(FeatureId id) {
        return switch (id) {
            case ZOMBIE_DOOR_GUARD -> config.hostile.zombie.doorCooldown.get();
            case ZOMBIE_ACTIVE_GUARD -> config.hostile.zombie.guardCooldown.get();
            case HUSK_SAND_BURROW -> config.hostile.zombie.burrowCooldown.get();
            case ZOMBIE_BABY_RIDER -> config.hostile.zombie.riderCooldown.get();
            default -> 80;
        };
    }
    public int tridentTimeout() { return config.hostile.drowned.recoveryTimeout.get(); }

    public int vexLimit() { return config.hostile.raid.vexLimit.get(); }
    public int summonCooldown() { return config.hostile.raid.summonCooldown.get(); }
    public int witchWindupTicks() { return config.hostile.raid.witchWindupTicks.get(); }
    public int witchCooldownTicks() { return config.hostile.raid.witchCooldownTicks.get(); }

    public int equipmentChance() { return config.equipment.assignmentChance.get(); }

    public int diagnosticLines() {
        return config.performance.diagnosticLines.get();
    }
}
