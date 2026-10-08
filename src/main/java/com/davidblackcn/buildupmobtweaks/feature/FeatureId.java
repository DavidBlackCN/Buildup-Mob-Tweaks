package com.davidblackcn.buildupmobtweaks.feature;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import net.minecraft.resources.Identifier;

public enum FeatureId {
    DIAGNOSTIC_PROBE("diagnostic_probe"),
    TRAITS("traits"),
    VEX_FIXED_CHARGE("vex_fixed_charge"),
    VEX_RECOVERY_PAUSE("vex_recovery_pause"),
    VEX_CLOSE_RANGE_GUARD("vex_close_range_guard"),
    PILLAGER_RETREAT("pillager_retreat"),
    PILLAGER_WEAPON_SWITCH("pillager_weapon_switch"),
    VINDICATOR_SUPPORT("vindicator_support"),
    EVOKER_VEX_LIMIT("evoker_vex_limit"),
    EVOKER_SUMMON_COOLDOWN("evoker_summon_cooldown"),
    WITCH_WINDUP("witch_windup"),
    WITCH_THROW_COOLDOWN("witch_throw_cooldown"),
    ZOMBIE_SHIELD_USE("zombie_shield_use"),
    ZOMBIE_DOOR_GUARD("zombie_door_guard"),
    ZOMBIE_ACTIVE_GUARD("zombie_active_guard"),
    HUSK_SAND_BURROW("husk_sand_burrow"),
    ZOMBIE_BABY_RIDER("zombie_baby_rider"),
    DROWNED_TRIDENT_CONSERVATION("drowned_trident_conservation"),
    DROWNED_TRIDENT_RECOVERY("drowned_trident_recovery"),
    DROWNED_TRIDENT_PLAYER_PICKUP("drowned_trident_player_pickup"),
    SKELETON_SAFE_STRAFING("skeleton_safe_strafing"),
    SKELETON_TARGET_VALIDATION("skeleton_target_validation"),
    SKELETON_WEAPON_SWITCHING("skeleton_weapon_switching"),
    SKELETON_BOW_COMPATIBILITY("skeleton_bow_compatibility"),
    SKELETON_SNIPING("skeleton_sniping"),
    STRAY_JUMP_SHOT("stray_jump_shot"),
    BOGGED_SPORE_RETREAT("bogged_spore_retreat"),
    EQUIPMENT_ASSIGNMENT("equipment_assignment"),
    TRAIT_COMMON_MARKER("demo_common"),
    TRAIT_ADVANCED_MARKER("demo_advanced"),
    TRAIT_RARE_MARKER("demo_rare");

    private final Identifier id;

    FeatureId(String path) {
        this.id = BuildupMobTweaks.id(path);
    }

    public Identifier id() {
        return id;
    }
}
