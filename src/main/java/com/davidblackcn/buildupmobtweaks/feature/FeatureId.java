package com.davidblackcn.buildupmobtweaks.feature;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import net.minecraft.resources.Identifier;

public enum FeatureId {
    DIAGNOSTIC_PROBE("diagnostic_probe"),
    TRAITS("traits"),
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
