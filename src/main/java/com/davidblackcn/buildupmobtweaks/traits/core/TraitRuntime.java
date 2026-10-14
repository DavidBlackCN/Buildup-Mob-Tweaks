package com.davidblackcn.buildupmobtweaks.traits.core;

import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import com.davidblackcn.buildupmobtweaks.rebuild.zombie.ZombieState;
import com.davidblackcn.buildupmobtweaks.rebuild.remaining.RemainingState;
import com.davidblackcn.buildupmobtweaks.rebuild.raid.RaidState;
import com.davidblackcn.buildupmobtweaks.rebuild.skeleton.ExtendedSkeletonState;
import com.davidblackcn.buildupmobtweaks.rebuild.skeleton.SkeletonState;
import com.davidblackcn.buildupmobtweaks.rebuild.piglin.PiglinState;
import java.util.List;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.nbt.CompoundTag;

/** Compatibility bridge: legacy modules still own their v1 qualification fields and action data. */
public final class TraitRuntime {
    public static void register() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (entity instanceof Mob mob && mob instanceof net.minecraft.world.entity.monster.Monster)
                SpawnStorage.onLoad(mob);
            if (entity instanceof Mob mob && subject(mob)) {
                ensure(mob);
                for (var definition : TraitRegistry.definitions())
                    if (definition.appliesTo(mob)) has(mob, definition.feature());
            }
        });
        ServerLivingEntityEvents.MOB_CONVERSION.register((previous, next, context) -> {
            if (!(next instanceof Mob mob)) return;
            var raw = previous.getAttached(TraitStorage.DATA);
            var source = previous.getAttached(SpawnStorage.DATA);
            if (source != null && !next.hasAttached(SpawnStorage.DATA)) next.setAttached(SpawnStorage.DATA, source.copy());
            if (raw != null && !next.hasAttached(TraitStorage.DATA)) next.setAttached(TraitStorage.DATA, raw.copy());
            else if (subject(mob)) TraitStorage.initialize(next, new TraitStorage.Snapshot(TraitStorage.Mode.COMPATIBILITY,
                    "conversion_without_source", List.of()));
        });
    }
    private static boolean subject(Mob mob) {
        return TraitRegistry.definitions().stream().anyMatch(d -> d.appliesTo(mob));
    }
    private static void ensure(Mob mob) {
        SpawnStorage.capture(mob);
        if (mob.hasAttached(TraitStorage.DATA)) return;
        var source = SpawnStorage.read(mob);
        if (source.isEmpty()) return;
        boolean unified = source.get().mode() == TraitStorage.Mode.UNIFIED;
        TraitStorage.initialize(mob, unified ? TraitSelector.select(mob, source.get())
                : new TraitStorage.Snapshot(TraitStorage.Mode.COMPATIBILITY, "compatibility", List.of()));
    }
    /** A feature gate is not a birth qualification check; callers may still be initializing birth. */
    public static boolean permits(Mob mob, FeatureId feature) {
        FeatureId owner = owner(feature);
        if (owner == null) return true;
        ensure(mob);
        var state = readRecord(mob);
        if (state.isEmpty()) return false;
        if (state.get().mode() == TraitStorage.Mode.COMPATIBILITY) return true;
        return com.davidblackcn.buildupmobtweaks.BuildupMobTweaks.config().traits.enabled.get()
                && TraitRegistry.find(owner).map(d -> TraitStorage.has(mob, d)).orElse(false);
    }
    public static boolean has(Mob mob, FeatureId feature) {
        var definition = TraitRegistry.find(feature).orElseThrow();
        if (!definition.appliesTo(mob) || mob.level().isClientSide()) return false;
        ensure(mob);
        if (readRecord(mob).isEmpty()) return false;
        if (readRecord(mob).map(s -> s.mode() == TraitStorage.Mode.UNIFIED).orElse(false))
            return TraitStorage.has(mob, definition);
        var legacy = legacy(mob, feature);
        if (legacy == null) return false; // Unknown or unavailable old format; keep its raw data.
        TraitStorage.observeCompatibility(mob, definition, legacy,
                SpawnStorage.read(mob).orElseThrow().source());
        return TraitStorage.has(mob, definition);
    }
    /** Null means unrecognized, false means a recognized empty qualification. No random backfill. */
    private static Boolean legacy(Mob mob, FeatureId feature) {
        return switch (feature) {
            case ZOMBIE_DOOR_GUARD -> flag(mob.getAttached(ZombieState.DATA), "door");
            case ENDERMAN_COMBO -> flag(mob.getAttached(RemainingState.DATA), "combo");
            case EVOKER_FIREBALL -> flag(mob.getAttached(RaidState.EVOKER), "fireball");
            case WITHER_SKELETON_SKULL -> flag(mob.getAttached(ExtendedSkeletonState.DATA), "skull");
            case PIGLIN_ITEM_DODGE -> flag(mob.getAttached(PiglinState.DATA), "dodge");
            case HUSK_SAND_BURROW -> known(mob.getAttached(ZombieState.DATA)) ? Boolean.TRUE : null;
            case BOGGED_SPORE_RETREAT -> known(mob.getAttached(SkeletonState.DATA)) ? Boolean.TRUE : null;
            default -> throw new IllegalArgumentException("Not in first trait batch");
        };
    }
    private static boolean known(CompoundTag data) { return data != null && data.getIntOr("version", -1) == 1; }
    public static boolean unified(Mob mob) {
        ensure(mob);
        return readRecord(mob).map(s -> s.mode() == TraitStorage.Mode.UNIFIED).orElse(false);
    }
    /** Read only. Inconsistent provenance and entitlement modes suspend execution without rewriting either. */
    public static java.util.Optional<TraitStorage.Snapshot> readRecord(Entity entity) {
        var state = TraitStorage.read(entity);
        var source = SpawnStorage.read(entity);
        return state.filter(s -> source.isPresent() && s.mode() == source.get().mode());
    }
    /** Sub-effects inherit a main qualification. Unmigrated candidates stay outside the opt-in pool. */
    private static FeatureId owner(FeatureId feature) {
        return switch (feature) {
            case WITHER_SKELETON_HOMING -> FeatureId.WITHER_SKELETON_SKULL;
            case BOGGED_OMEN_MUSHROOMS -> FeatureId.BOGGED_SPORE_RETREAT;
            case ENDERMAN_COMBO_ANIMATION -> FeatureId.ENDERMAN_COMBO;
            case EVOKER_FIREBALL_NO_FIRE -> FeatureId.EVOKER_FIREBALL;
            case BREEZE_TAKEOFF_BURST, BREEZE_LANDING_BURST -> FeatureId.BREEZE_SHOTGUN;
            case ZOMBIE_DOOR_GUARD, ENDERMAN_COMBO, EVOKER_FIREBALL, WITHER_SKELETON_SKULL,
                 HUSK_SAND_BURROW, BOGGED_SPORE_RETREAT, PIGLIN_ITEM_DODGE, SKELETON_BABY_VARIANT,
                 BREEZE_SHOTGUN, CREEPER_EMBEDDED_ARROWS, SKELETON_HORSE_CHARGE, CHICKEN_JOCKEY_CHARGE,
                 PARCHED_DODGE, ILLUSIONER_CLONE_ARROWS, EVOKER_TOTEM, SILVERFISH_BURROW,
                 ZOMBIE_BABY_RIDER, STRAY_JUMP_SHOT -> feature;
            default -> null;
        };
    }
    private static Boolean flag(CompoundTag data, String value) {
        return known(data) ? value.equals(data.getStringOr("birth", "none")) : null;
    }
    private TraitRuntime() {}
}
