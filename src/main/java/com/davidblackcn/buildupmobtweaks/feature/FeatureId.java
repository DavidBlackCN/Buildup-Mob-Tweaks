package com.davidblackcn.buildupmobtweaks.feature;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import net.minecraft.resources.Identifier;

public enum FeatureId {
    DIAGNOSTIC_PROBE("diagnostic_probe"),
    TRAITS("traits"),
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
