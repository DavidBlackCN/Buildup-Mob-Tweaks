package com.davidblackcn.buildupmobtweaks.traits.core;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import com.davidblackcn.buildupmobtweaks.feature.*;
import java.util.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.*;
import static com.davidblackcn.buildupmobtweaks.feature.FeatureId.*;

/** Opt-in first-batch selector. No action starts and no item is assigned here. */
final class TraitSelector {
    static TraitStorage.Snapshot select(Mob mob, SpawnStorage.Source source) {
        var cfg = BuildupMobTweaks.config();
        if (!source.source().automaticallySelects()) return empty("source_skipped");
        if (!cfg.general.enabled.get() || !cfg.traits.enabled.get()
                || mob.entityTags().contains("buildupmobtweaks:vanilla_ai")) return empty("disabled");
        var c = cfg.traits.selection;
        var features = new FeatureRegistry(cfg);
        int chance;
        var weights = new LinkedHashMap<FeatureId,Integer>();
        if (mob.getType() == EntityTypes.ZOMBIE) {
            chance = fallback(c.zombie_chance_per_mille.get(), cfg.hostile.zombie.doorChance.get());
            weights.put(ZOMBIE_DOOR_GUARD, c.door_weight.get());
        } else if (mob.getType() == EntityTypes.HUSK) {
            chance = c.husk_chance_per_mille.get();
            weights.put(ZOMBIE_DOOR_GUARD, c.door_weight.get());
            weights.put(HUSK_SAND_BURROW, c.burrow_weight.get());
        } else if (mob.getType() == EntityTypes.BOGGED) {
            chance = c.bogged_chance_per_mille.get();
            weights.put(BOGGED_SPORE_RETREAT, c.bogged_weight.get());
        } else if (mob.getType() == EntityTypes.ENDERMAN) {
            chance = fallback(c.enderman_chance_per_mille.get(), features.majorChance(ENDERMAN_COMBO));
            weights.put(ENDERMAN_COMBO, c.combo_weight.get());
        } else if (mob.getType() == EntityTypes.EVOKER) {
            chance = fallback(c.evoker_chance_per_mille.get(), features.majorChance(EVOKER_FIREBALL));
            weights.put(EVOKER_FIREBALL, c.fireball_weight.get());
        } else if (mob.getType() == EntityTypes.WITHER_SKELETON) {
            chance = fallback(c.wither_chance_per_mille.get(), features.majorChance(WITHER_SKELETON_SKULL));
            weights.put(WITHER_SKELETON_SKULL, c.skull_weight.get());
        } else if (mob.getType() == EntityTypes.PIGLIN) {
            chance = fallback(c.piglin_chance_per_mille.get(), features.majorChance(PIGLIN_ITEM_DODGE));
            weights.put(PIGLIN_ITEM_DODGE, c.dodge_weight.get());
        } else return empty("empty_pool");
        if (chance < 0) return empty("unconfigured");
        var candidates = new ArrayList<TraitPool.Weighted<FeatureId>>();
        weights.forEach((feature, weight) -> {
            if (weight > 0 && features.isEnabled(feature) && eligible(mob, source.source(), feature))
                candidates.add(new TraitPool.Weighted<>(feature, weight));
        });
        if (candidates.isEmpty()) return empty("empty_pool");
        var chosen = new TraitPool<FeatureId>(chance, candidates).sample(mob.getRandom());
        if (chosen.isEmpty()) return empty("missed");
        var definition = TraitRegistry.find(chosen.get()).orElseThrow();
        return new TraitStorage.Snapshot(TraitStorage.Mode.UNIFIED, "selected",
                List.of(new TraitInstance(definition.id(), definition.version(), source.source())));
    }
    private static int fallback(int configured, int oldChance) { return configured < 0 ? oldChance : configured; }
    private static boolean eligible(Mob mob, SpawnSource source, FeatureId feature) {
        if (mob.entityTags().contains("buildupmobtweaks:disable_" + feature.id().getPath())) return false;
        String excluded = switch (feature) {
            case ZOMBIE_DOOR_GUARD, HUSK_SAND_BURROW -> "zombie_ai_excluded";
            case BOGGED_SPORE_RETREAT, WITHER_SKELETON_SKULL -> "skeleton_ai_excluded";
            case ENDERMAN_COMBO -> "remaining_ai_excluded";
            case EVOKER_FIREBALL -> "raid_ai_excluded";
            case PIGLIN_ITEM_DODGE -> "piglin_ai_excluded";
            default -> throw new IllegalArgumentException("Unmigrated trait");
        };
        if (mob.getType().builtInRegistryHolder().is(TagKey.create(Registries.ENTITY_TYPE, BuildupMobTweaks.id(excluded))))
            return false;
        if (feature == ZOMBIE_DOOR_GUARD) return source == SpawnSource.NATURAL && !mob.isBaby()
                && !mob.isPassenger() && !mob.isVehicle()
                && ((net.minecraft.server.level.ServerLevel)mob.level()).getCurrentDifficultyAt(mob.blockPosition()).getEffectiveDifficulty() > 2.5;
        if (feature == HUSK_SAND_BURROW) return !mob.isBaby() && !mob.isPassenger() && !mob.isVehicle();
        if (feature == BOGGED_SPORE_RETREAT) return !mob.isBaby() && !mob.isPassenger();
        return feature != PIGLIN_ITEM_DODGE || !mob.isBaby();
    }
    private static TraitStorage.Snapshot empty(String outcome) {
        return new TraitStorage.Snapshot(TraitStorage.Mode.UNIFIED, outcome, List.of());
    }
    private TraitSelector() {}
}
