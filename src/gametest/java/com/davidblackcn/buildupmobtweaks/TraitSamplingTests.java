package com.davidblackcn.buildupmobtweaks;

import com.davidblackcn.buildupmobtweaks.traits.TraitKind;
import com.davidblackcn.buildupmobtweaks.traits.TraitSampler;
import java.util.Arrays;
import java.util.Random;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public class TraitSamplingTests {
    @GameTest
    public void distributionAndExclusion(GameTestHelper helper) {
        verify(helper, new TraitSampler.Weights(200, 70, 10), new double[]{.20, .07, .01, .72}, 74123);
        verify(helper, new TraitSampler.Weights(100, 30, 5), new double[]{.10, .03, .005, .865}, 92731);
        verify(helper, new TraitSampler.Weights(1000, 1000, 1000), new double[]{1.0/3, 1.0/3, 1.0/3, 0}, 42731);
        helper.succeed();
    }

    private void verify(GameTestHelper helper, TraitSampler.Weights weights, double[] probabilities, long seed) {
        int samples = 200_000;
        int[] counts = new int[4];
        Random random = new Random(seed);
        for (int i = 0; i < samples; i++) {
            var entries = TraitSampler.sample(weights, random.nextDouble());
            helper.assertTrue(entries.size() <= 1, "Exclusive group must never contain two traits");
            if (entries.isEmpty()) counts[3]++;
            else {
                for (int j = 0; j < 3; j++) {
                    if (entries.getFirst().id().equals(TraitKind.values()[j].id())) counts[j]++;
                }
            }
        }
        for (int i = 0; i < counts.length; i++) {
            double tolerance = 6 * Math.sqrt(samples * probabilities[i] * (1 - probabilities[i])) + 1;
            helper.assertTrue(Math.abs(counts[i] - samples * probabilities[i]) <= tolerance,
                    "Distribution outside six-sigma bound for category " + i);
        }
        BuildupMobTweaks.LOGGER.info("Trait sampling: weights={}, seed={}, n={}, counts={}", weights, seed, samples, Arrays.toString(counts));
    }

    @GameTest
    public void boundariesAndInvalidInput(GameTestHelper helper) {
        var weights = new TraitSampler.Weights(200, 70, 10);
        helper.assertValueEqual(TraitSampler.sample(weights, 0).getFirst().id(), TraitKind.COMMON.id(), "Lower boundary");
        helper.assertValueEqual(TraitSampler.sample(weights, .20).getFirst().id(), TraitKind.ADVANCED.id(), "Common/advanced boundary");
        helper.assertValueEqual(TraitSampler.sample(weights, .27).getFirst().id(), TraitKind.RARE.id(), "Advanced/rare boundary");
        helper.assertTrue(TraitSampler.sample(weights, .28).isEmpty(), "No-trait boundary");
        helper.assertTrue(TraitSampler.sample(new TraitSampler.Weights(0, 0, 0), 0).isEmpty(), "Disabled pool");
        helper.assertValueEqual(TraitSampler.sample(new TraitSampler.Weights(0, 1000, 0), .999).getFirst().id(), TraitKind.ADVANCED.id(), "Only active candidate");
        rejects(helper, () -> TraitSampler.sample(weights, Double.NaN));
        rejects(helper, () -> TraitSampler.sample(weights, 1));
        rejects(helper, () -> new TraitSampler.Weights(-1, 0, 0));
        rejects(helper, () -> new TraitSampler.Weights(1001, 0, 0));
        helper.succeed();
    }

    private void rejects(GameTestHelper helper, Runnable action) {
        boolean rejected = false;
        try { action.run(); } catch (IllegalArgumentException expected) { rejected = true; }
        helper.assertTrue(rejected, "Invalid input must be rejected");
    }
}
