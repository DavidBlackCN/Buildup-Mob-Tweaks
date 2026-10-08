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
        add(gates, FeatureId.VEX_FIXED_CHARGE, () -> config.hostile.vex.fixedCharge.get());
        add(gates, FeatureId.VEX_RECOVERY_PAUSE, () -> config.hostile.vex.recoveryPause.get());
        add(gates, FeatureId.VEX_CLOSE_RANGE_GUARD, () -> config.hostile.vex.closeRangeGuard.get());
        add(gates, FeatureId.GHAST_SLOW_FIREBALL, () -> config.hostile.extended.ghast_slow_fireball.get());
        add(gates, FeatureId.GHAST_COOLDOWN, () -> config.hostile.extended.ghast_cooldown.get());
        add(gates, FeatureId.GHAST_TELEGRAPH, () -> config.hostile.extended.ghast_telegraph.get());
        add(gates, FeatureId.BLAZE_ORBIT, () -> config.hostile.extended.blaze_orbit.get());
        add(gates, FeatureId.BLAZE_DIFFICULTY_VOLLEY, () -> config.hostile.extended.blaze_difficulty_volley.get());
        add(gates, FeatureId.BLAZE_STRAFE, () -> config.hostile.extended.blaze_strafe.get());
        add(gates, FeatureId.VEX_PROJECTILE_WEAKNESS, () -> config.hostile.extended.vex_projectile_weakness.get());
        add(gates, FeatureId.PILLAGER_CROSSBOW_COMPATIBILITY, () -> config.hostile.extended.pillager_crossbow_compatibility.get());
        add(gates, FeatureId.PILLAGER_RANGE, () -> config.hostile.extended.pillager_range.get());
        add(gates, FeatureId.SENSE_GLOWING, () -> config.hostile.extended.sense_glowing.get());
        add(gates, FeatureId.SENSE_BLINDNESS, () -> config.hostile.extended.sense_blindness.get());
        add(gates, FeatureId.SENSE_DARKNESS, () -> config.hostile.extended.sense_darkness.get());
        add(gates, FeatureId.SENSE_NAUSEA, () -> config.hostile.extended.sense_nausea.get());
        add(gates, FeatureId.SENSE_INVISIBILITY, () -> config.hostile.extended.sense_invisibility.get());
        add(gates, FeatureId.SENSE_CRAWLING, () -> config.hostile.extended.sense_crawling.get());
        add(gates, FeatureId.SENSE_CROUCHING, () -> config.hostile.extended.sense_crouching.get());
        add(gates, FeatureId.MELEE_EFFECT_INTERVAL, () -> config.hostile.extended.melee_effect_interval.get());
        add(gates, FeatureId.MELEE_HIGH_TARGET_JUMP, () -> config.hostile.extended.melee_high_target_jump.get());
        add(gates, FeatureId.RANGED_REPOSITION, () -> config.hostile.extended.ranged_reposition.get());
        add(gates, FeatureId.OMEN_PRESSURE, () -> config.hostile.extended.omen_pressure.get());
        add(gates, FeatureId.SILVERFISH_BURROW, () -> config.hostile.extended.silverfish_burrow.get());
        add(gates, FeatureId.SILVERFISH_CALL_PAUSE, () -> config.hostile.extended.silverfish_call_pause.get());
        add(gates, FeatureId.SILVERFISH_CALL_PARTICLES, () -> config.hostile.extended.silverfish_call_particles.get());
        add(gates, FeatureId.SILVERFISH_SHOVEL_WEAKNESS, () -> config.hostile.extended.silverfish_shovel_weakness.get());
        add(gates, FeatureId.BREEZE_TAKEOFF_BURST, () -> config.hostile.extended.breeze_takeoff_burst.get());
        add(gates, FeatureId.BREEZE_LANDING_BURST, () -> config.hostile.extended.breeze_landing_burst.get());
        add(gates, FeatureId.EVOKER_FLEE_SPEED, () -> config.hostile.extended.evoker_flee_speed.get());
        add(gates, FeatureId.EVOKER_AVOID_TARGET_FIX, () -> config.hostile.extended.evoker_avoid_target_fix.get());
        add(gates, FeatureId.HOSTILE_REST, () -> config.hostile.extended.hostile_rest.get());
        add(gates, FeatureId.REST_SOUND_WAKE, () -> config.hostile.extended.rest_sound_wake.get());
        add(gates, FeatureId.WITCH_LEAPING_POTION, () -> config.hostile.extended.witch_leaping_potion.get());
        add(gates, FeatureId.WITCH_JUMP_THROW, () -> config.hostile.extended.witch_jump_throw.get());
        add(gates, FeatureId.EVOKER_FIREBALL, () -> config.traits.enabled.get() && config.hostile.extended.evoker_fireball.get());
        add(gates, FeatureId.EVOKER_TOTEM, () -> config.traits.enabled.get() && config.hostile.extended.evoker_totem.get());
        add(gates, FeatureId.EVOKER_FIREBALL_NO_FIRE, () -> config.hostile.extended.evoker_fireball_no_fire.get());
        add(gates, FeatureId.ENDERMAN_SPEED, () -> config.hostile.extended.enderman_speed.get());
        add(gates, FeatureId.ENDERMAN_COMBO, () -> config.traits.enabled.get() && config.hostile.extended.enderman_combo.get());
        add(gates, FeatureId.ENDERMAN_COMBO_ANIMATION, () -> config.hostile.extended.enderman_combo_animation.get());
        add(gates, FeatureId.ILLUSIONER_CLONE_ARROWS, () -> config.traits.enabled.get() && config.hostile.extended.illusioner_clone_arrows.get());
        add(gates, FeatureId.ILLUSIONER_SHOOT_PAUSE, () -> config.hostile.extended.illusioner_shoot_pause.get());
        add(gates, FeatureId.ILLUSIONER_SWAP, () -> config.hostile.extended.illusioner_swap.get());
        add(gates, FeatureId.SKELETON_SHELTER, () -> config.hostile.extended.skeleton_shelter.get());
        add(gates, FeatureId.STRAY_SNOW_BARRAGE, () -> config.traits.enabled.get() && config.hostile.extended.stray_snow_barrage.get());
        add(gates, FeatureId.STRAY_OMEN_SNOW, () -> config.hostile.extended.stray_omen_snow.get());
        add(gates, FeatureId.BOGGED_OMEN_MUSHROOMS, () -> config.hostile.extended.bogged_omen_mushrooms.get());
        add(gates, FeatureId.PARCHED_DODGE, () -> config.traits.enabled.get() && config.hostile.extended.parched_dodge.get());
        add(gates, FeatureId.WITHER_SKELETON_SKULL, () -> config.traits.enabled.get() && config.hostile.extended.wither_skeleton_skull.get());
        add(gates, FeatureId.WITHER_SKELETON_HOMING, () -> config.hostile.extended.wither_skeleton_homing.get());
        add(gates, FeatureId.WITHER_SKELETON_BOW, () -> config.hostile.extended.wither_skeleton_bow.get());
        add(gates, FeatureId.SKELETON_WITHER_CONVERSION, () -> config.hostile.extended.skeleton_wither_conversion.get());
        add(gates, FeatureId.SKELETON_TRAP_VARIANTS, () -> config.hostile.extended.skeleton_trap_variants.get());
        add(gates, FeatureId.RIDER_FRIENDLY_FIRE, () -> config.hostile.extended.rider_friendly_fire.get());
        add(gates, FeatureId.ZOMBIE_FLETCHER_CROSSBOW, () -> config.hostile.extended.zombie_fletcher_crossbow.get());
        add(gates, FeatureId.ZOMBIE_CLERIC_POTIONS, () -> config.hostile.extended.zombie_cleric_potions.get());
        add(gates, FeatureId.ZOMBIE_FISHERMAN_ROD, () -> config.hostile.extended.zombie_fisherman_rod.get());
        add(gates, FeatureId.ZOMBIFIED_PIGLIN_CROSSBOW, () -> config.hostile.extended.zombified_piglin_crossbow.get());
        add(gates, FeatureId.ZOMBIFIED_PIGLIN_SPAWN_CROSSBOW, () -> config.hostile.extended.zombified_piglin_spawn_crossbow.get());
        add(gates, FeatureId.DROWNED_FISHING_PULL, () -> config.hostile.extended.drowned_fishing_pull.get());
        add(gates, FeatureId.PILLAGER_SHIELD_BREAK, () -> config.hostile.extended.pillager_shield_break.get());
        add(gates, FeatureId.PILLAGER_FOOD_HEAL, () -> config.hostile.extended.pillager_food_heal.get());
        add(gates, FeatureId.PIGLIN_CROSSBOW_COMPATIBILITY, () -> config.hostile.extended.piglin_crossbow_compatibility.get());
        add(gates, FeatureId.ZOMBIE_HORSE_LEADER, () -> config.traits.enabled.get() && config.hostile.extended.zombie_horse_leader.get());
        add(gates, FeatureId.ZOMBIE_SLIME_CARRIER, () -> config.traits.enabled.get() && config.hostile.extended.zombie_slime_carrier.get());
        add(gates, FeatureId.CHICKEN_JOCKEY_CHARGE, () -> config.traits.enabled.get() && config.hostile.extended.chicken_jockey_charge.get());
        add(gates, FeatureId.SKELETON_HORSE_CHARGE, () -> config.traits.enabled.get() && config.hostile.extended.skeleton_horse_charge.get());
        add(gates, FeatureId.HUSK_CAMEL_CIRCLE, () -> config.hostile.extended.husk_camel_circle.get());
        add(gates, FeatureId.HUSK_SPEAR_CHARGE, () -> config.hostile.extended.husk_spear_charge.get());
        add(gates, FeatureId.ILLAGER_BOATS, () -> config.hostile.extended.illager_boats.get());
        add(gates, FeatureId.ILLAGER_ZOMBIE_VILLAGER, () -> config.hostile.extended.illager_zombie_villager.get());
        add(gates, FeatureId.ILLAGER_ZOMBIE_CONFLICT, () -> config.hostile.extended.illager_zombie_conflict.get());
        add(gates, FeatureId.ILLUSIONER_OMEN_SPAWN, () -> config.hostile.extended.illusioner_omen_spawn.get());
        add(gates, FeatureId.CREEPER_EMBEDDED_ARROWS, () -> config.hostile.extended.creeper_embedded_arrows.get());
        add(gates, FeatureId.CREEPER_HIT_DELAY, () -> config.hostile.extended.creeper_hit_delay.get());
        add(gates, FeatureId.CREEPER_FIRE_VISUAL, () -> config.hostile.extended.creeper_fire_visual.get());
        add(gates, FeatureId.SPIDER_HUNTS_PESTS, () -> config.hostile.extended.spider_hunts_pests.get());
        add(gates, FeatureId.HOSTILE_SPAWN_EFFECT, () -> config.traits.enabled.get() && config.hostile.extended.hostile_spawn_effect.get());
        add(gates, FeatureId.HOSTILE_ESCAPE_SEAT, () -> config.hostile.extended.hostile_escape_seat.get());
        add(gates, FeatureId.UNDEAD_HORSE_SUNBURN, () -> config.hostile.extended.undead_horse_sunburn.get());
        add(gates, FeatureId.BURN_FREEZE_VISUAL, () -> config.hostile.extended.burn_freeze_visual.get());
        add(gates, FeatureId.BURNING_PROJECTILE_VISUAL, () -> config.hostile.extended.burning_projectile_visual.get());
        add(gates, FeatureId.DROWNED_SWIM_VISUAL, () -> config.hostile.extended.drowned_swim_visual.get());
        add(gates, FeatureId.SKELETON_OFFHAND_VISUAL, () -> config.hostile.extended.skeleton_offhand_visual.get());
        add(gates, FeatureId.PIGLIN_ITEM_DODGE, () -> config.traits.enabled.get() && config.hostile.extended.piglin_item_dodge.get());
        add(gates, FeatureId.PIGLIN_SHOT_SWING, () -> config.hostile.extended.piglin_shot_swing.get());
        add(gates, FeatureId.STRAFE_OBSTACLE_JUMP, () -> config.hostile.extended.strafe_obstacle_jump.get());
        add(gates, FeatureId.MOVEMENT_CLEAR_STRAFE, () -> config.hostile.extended.movement_clear_strafe.get());
        add(gates, FeatureId.MOVEMENT_JUMP_INPUT, () -> config.hostile.extended.movement_jump_input.get());
        add(gates, FeatureId.STRAFE_HAZARD_CHECK, () -> config.hostile.extended.strafe_hazard_check.get());
        add(gates, FeatureId.ZOMBIFIED_PIGLIN_ANGER_FIX, () -> config.hostile.extended.zombified_piglin_anger_fix.get());
        add(gates, FeatureId.GOLEM_FRIENDLY_FIRE_FIX, () -> config.hostile.extended.golem_friendly_fire_fix.get());
        add(gates, FeatureId.SKELETON_AIM_FIX, () -> config.hostile.extended.skeleton_aim_fix.get());
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

    public int vexRecoveryTicks() { return config.hostile.vex.recoveryTicks.get(); }
    public int vexMinimumDistance() { return config.hostile.vex.minimumChargeDistance.get(); }

    public int vexLimit() { return config.hostile.raid.vexLimit.get(); }
    public int summonCooldown() { return config.hostile.raid.summonCooldown.get(); }
    public int witchWindupTicks() { return config.hostile.raid.witchWindupTicks.get(); }
    public int witchCooldownTicks() { return config.hostile.raid.witchCooldownTicks.get(); }

    public int majorChance(FeatureId id) {
        if (!isEnabled(id)) return 0;
        return switch(id) {
            case STRAY_SNOW_BARRAGE -> config.hostile.extended.stray_snow_barrage_chance.get();
            case PARCHED_DODGE -> config.hostile.extended.parched_dodge_chance.get();
            case WITHER_SKELETON_SKULL -> config.hostile.extended.wither_skeleton_skull_chance.get();
            case ZOMBIE_HORSE_LEADER -> config.hostile.extended.zombie_horse_leader_chance.get();
            case ZOMBIE_SLIME_CARRIER -> config.hostile.extended.zombie_slime_carrier_chance.get();
            case CHICKEN_JOCKEY_CHARGE -> config.hostile.extended.chicken_jockey_charge_chance.get();
            case SKELETON_HORSE_CHARGE -> config.hostile.extended.skeleton_horse_charge_chance.get();
            case PIGLIN_ITEM_DODGE -> 70;
            case EVOKER_FIREBALL -> config.hostile.extended.evoker_fireball_chance.get();
            case EVOKER_TOTEM -> config.hostile.extended.evoker_totem_chance.get();
            case ENDERMAN_COMBO -> config.hostile.extended.enderman_combo_chance.get();
            case ILLUSIONER_CLONE_ARROWS -> config.hostile.extended.illusioner_clone_chance.get();
            default -> 0;
        };
    }
    public int specialCooldown() { return config.hostile.extended.special_cooldown.get(); }
    public int equipmentChance() { return config.equipment.assignmentChance.get(); }

    public int diagnosticLines() {
        return config.performance.diagnosticLines.get();
    }
}
