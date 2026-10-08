package com.davidblackcn.buildupmobtweaks.traits;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import com.davidblackcn.buildupmobtweaks.feature.FeatureRegistry;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;

public final class TraitService {
    // Preserve unknown future data verbatim. Decode separately and fail closed, never re-roll it.
    public static final AttachmentType<CompoundTag> DATA = AttachmentRegistry.createPersistent(
            BuildupMobTweaks.id("traits"), CompoundTag.CODEC);
    private final FeatureRegistry features;

    public TraitService(FeatureRegistry features) { this.features = features; }

    public void register() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> initialize(entity));
        ServerLivingEntityEvents.MOB_CONVERSION.register((previous, converted, context) -> {
            if (!eligible(converted)) return;
            CompoundTag previousData = previous.getAttached(DATA);
            if (previousData != null) converted.setAttached(DATA, previousData.copy());
            else write(converted, TraitState.empty("conversion_without_source", "CONVERSION"));
        });
    }

    public static boolean eligible(Entity entity) {
        return entity instanceof Mob && TraitKind.ENTITY_IDS.contains(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString());
    }

    public void initialize(Entity entity) {
        if (entity.level().isClientSide() || !eligible(entity) || entity.hasAttached(DATA)) return;
        EntitySpawnReason reason = entity.spawnReason();
        String origin = reason == null ? "UNKNOWN" : reason.name();
        TraitState state;
        if (entity.isLoadedFromDisk() || reason == EntitySpawnReason.LOAD) {
            state = TraitState.empty("legacy_skipped", origin);
        } else if (reason == null || reason == EntitySpawnReason.CONVERSION || reason == EntitySpawnReason.DIMENSION_TRAVEL) {
            state = TraitState.empty("source_unknown_skipped", origin);
        } else if (!features.isEnabled(FeatureId.TRAITS)) {
            state = TraitState.empty("disabled", origin);
        } else {
            boolean cow = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString().equals("minecraft:cow");
            state = new TraitState(TraitState.VERSION, "rolled", origin,
                    TraitSampler.sample(features.traitWeights(cow), ((Mob) entity).getRandom().nextDouble()));
        }
        write(entity, state);
    }

    private static void write(Entity entity, TraitState state) {
        CompoundTag data = (CompoundTag) TraitState.CODEC.encodeStart(NbtOps.INSTANCE, state).getOrThrow();
        entity.setAttached(DATA, data);
    }

    public static Optional<TraitState> read(Entity entity) {
        CompoundTag data = entity.getAttached(DATA);
        return data == null ? Optional.empty() : TraitState.CODEC.parse(NbtOps.INSTANCE, data).result();
    }

    public List<TraitState.Entry> activeTraits(Entity entity) {
        if (!eligible(entity) || !features.isEnabled(FeatureId.TRAITS)) return List.of();
        return read(entity).map(state -> state.entries().stream()
                .filter(entry -> features.isTraitEnabled(entry.id())).toList()).orElseGet(List::of);
    }
}
