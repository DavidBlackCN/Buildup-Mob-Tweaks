package com.davidblackcn.buildupmobtweaks.feature;

import com.davidblackcn.buildupmobtweaks.config.BuildupConfig;
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
                new Declaration(FeatureId.DIAGNOSTIC_PROBE, () -> config.general.diagnosticProbe.get()));
    }

    public boolean isEnabled(FeatureId id) {
        Declaration declaration = declarations.get(id);
        return config.general.enabled.get() && declaration != null && declaration.enabled().getAsBoolean();
    }

    public int diagnosticLines() {
        return config.performance.diagnosticLines.get();
    }
}
