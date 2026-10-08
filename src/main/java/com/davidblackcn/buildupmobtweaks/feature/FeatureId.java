package com.davidblackcn.buildupmobtweaks.feature;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import net.minecraft.resources.Identifier;

public enum FeatureId {
    DIAGNOSTIC_PROBE("diagnostic_probe");

    private final Identifier id;

    FeatureId(String path) {
        this.id = BuildupMobTweaks.id(path);
    }

    public Identifier id() {
        return id;
    }
}
