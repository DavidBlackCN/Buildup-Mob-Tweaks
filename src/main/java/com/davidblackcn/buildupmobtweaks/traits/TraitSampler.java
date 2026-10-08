package com.davidblackcn.buildupmobtweaks.traits;

import java.util.List;

/** One categorical draw in the diagnostic_marker group; at most one result. */
public final class TraitSampler {
    private TraitSampler() {}

    public record Weights(int common, int advanced, int rare) {
        public Weights {
            if (common < 0 || common > 1000 || advanced < 0 || advanced > 1000 || rare < 0 || rare > 1000) {
                throw new IllegalArgumentException("Trait weights must be in [0, 1000]");
            }
        }
    }

    public static List<TraitState.Entry> sample(Weights weights, double draw) {
        if (!Double.isFinite(draw) || draw < 0 || draw >= 1) {
            throw new IllegalArgumentException("Draw must be in [0, 1)");
        }
        int total = weights.common + weights.advanced + weights.rare;
        // Below 1000, unused mass means no trait. Above 1000, normalize weights.
        double point = draw * Math.max(1000, total);
        TraitKind kind;
        if (point < weights.common) kind = TraitKind.COMMON;
        else if (point < weights.common + weights.advanced) kind = TraitKind.ADVANCED;
        else if (point < total) kind = TraitKind.RARE;
        else return List.of();
        return List.of(new TraitState.Entry(kind.id(), kind.level()));
    }
}
