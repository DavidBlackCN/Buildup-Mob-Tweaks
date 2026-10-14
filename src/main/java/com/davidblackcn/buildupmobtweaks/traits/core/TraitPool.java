package com.davidblackcn.buildupmobtweaks.traits.core;

import java.util.*;
import net.minecraft.util.RandomSource;

/** Separate total probability and conditional weights. All probabilities are integer per-mille. */
public record TraitPool<T>(int chancePerMille, List<Weighted<T>> entries) {
    public record Weighted<T>(T value, int weight) {
        public Weighted { Objects.requireNonNull(value); if (weight <= 0) throw new IllegalArgumentException("Weight"); }
    }
    public TraitPool {
        if (chancePerMille < 0 || chancePerMille > 1000) throw new IllegalArgumentException("Chance");
        entries = List.copyOf(entries);
        int sum = 0;
        for (var entry : entries) sum = Math.addExact(sum, entry.weight());
    }
    public int totalWeight() { return entries.stream().mapToInt(Weighted::weight).sum(); }
    public Optional<T> sample(RandomSource random) {
        if (entries.isEmpty() || random.nextInt(1000) >= chancePerMille) return Optional.empty();
        return Optional.of(atWeight(random.nextInt(totalWeight())));
    }
    public T atWeight(int roll) {
        if (roll < 0 || roll >= totalWeight()) throw new IllegalArgumentException("Weight roll");
        for (var entry : entries) { if (roll < entry.weight()) return entry.value(); roll -= entry.weight(); }
        throw new IllegalStateException("Invalid pool");
    }
    /** Old mutually exclusive groups become P=min(sum,1000), weights=old active chances.
     * Keeps the same single integer draw, including its seed consumption, and identical final probabilities.
     * Nonmigrated members are retained in their old group; this does not register them as new Traits. */
    public static <T> Optional<T> legacyEquivalent(RandomSource random, List<Weighted<T>> entries) {
        int sum = entries.stream().mapToInt(Weighted::weight).reduce(0, Math::addExact);
        var pool = new TraitPool<T>(Math.min(1000, sum), entries);
        int roll = random.nextInt(Math.max(1000, sum));
        return roll < sum ? Optional.of(pool.atWeight(roll)) : Optional.empty();
    }
    /** Exact single-candidate mapping, without changing the original random stream. */
    public static boolean legacySingle(RandomSource random, int chance) {
        var pool = new TraitPool<String>(chance, List.of(new Weighted<>("single", 1)));
        return random.nextInt(1000) < pool.chancePerMille();
    }
}
