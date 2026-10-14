package com.davidblackcn.buildupmobtweaks.traits.core;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import java.util.*;
import net.fabricmc.fabric.api.attachment.v1.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

/** New entitlement record; never merges, removes or rewrites a legacy module attachment. */
public final class TraitStorage {
    public static final int VERSION = 1;
    public static final AttachmentType<CompoundTag> DATA = AttachmentRegistry.createPersistent(
            BuildupMobTweaks.id("trait_core"), CompoundTag.CODEC);
    public enum Mode { COMPATIBILITY, UNIFIED }
    public record Snapshot(Mode mode, String outcome, List<TraitInstance> entries) {
        public Snapshot { entries = List.copyOf(entries); }
    }
    private static final Set<String> OUTCOMES = Set.of("compatibility", "selected", "missed", "disabled",
            "source_skipped", "empty_pool", "unconfigured", "conversion_without_source");
    public static void bootstrap() {}

    /** Unknown formats, IDs, versions and malformed records are retained verbatim, but not executed. */
    public static Optional<Snapshot> read(Entity entity) {
        var raw = entity.getAttached(DATA);
        return raw == null ? Optional.empty() : decode(raw);
    }
    public static Optional<Snapshot> decode(CompoundTag raw) {
        if (raw.getIntOr("version", -1) != VERSION) return Optional.empty();
        try {
            var mode = Mode.valueOf(raw.getStringOr("mode", ""));
            String outcome = raw.getStringOr("outcome", "");
            if (!OUTCOMES.contains(outcome) || raw.getCompound("entries").isEmpty()) return Optional.empty();
            var entries = raw.getCompoundOrEmpty("entries");
            if (entries.size() > TraitRegistry.definitions().size() || mode == Mode.UNIFIED && entries.size() > 1)
                return Optional.empty();
            var instances = new ArrayList<TraitInstance>();
            for (String key : new TreeSet<>(entries.keySet())) {
                var id = Identifier.parse(key);
                var definition = TraitRegistry.find(id).orElseThrow();
                var data = entries.getCompound(key).orElseThrow();
                int version = data.getIntOr("version", -1);
                if (version != definition.version()) return Optional.empty();
                instances.add(new TraitInstance(id, version, SpawnSource.valueOf(data.getStringOr("source", ""))));
            }
            if (mode == Mode.UNIFIED && (!outcome.equals("selected") && !instances.isEmpty()
                    || outcome.equals("selected") && instances.size() != 1)) return Optional.empty();
            return Optional.of(new Snapshot(mode, outcome, instances));
        } catch (IllegalArgumentException | NoSuchElementException exception) {
            return Optional.empty();
        }
    }
    public static CompoundTag encode(Snapshot snapshot) {
        var raw = new CompoundTag();
        raw.putInt("version", VERSION);
        raw.putString("mode", snapshot.mode().name());
        raw.putString("outcome", snapshot.outcome());
        var entries = new CompoundTag();
        for (var instance : snapshot.entries()) {
            if (entries.contains(instance.id().toString())) throw new IllegalArgumentException("Duplicate trait");
            var data = new CompoundTag();
            data.putInt("version", instance.version());
            data.putString("source", instance.source().name());
            entries.put(instance.id().toString(), data);
        }
        raw.put("entries", entries);
        if (decode(raw).isEmpty()) throw new IllegalArgumentException("Invalid trait record");
        return raw;
    }
    /** One initial commit. Presence, even of unknown data or an empty result, prevents a second selection. */
    public static boolean initialize(Entity entity, Snapshot snapshot) {
        if (entity.level().isClientSide() || entity.hasAttached(DATA)) return false;
        entity.setAttached(DATA, encode(snapshot));
        return true;
    }
    public static boolean has(Entity entity, TraitDefinition definition) {
        return definition.appliesTo(entity) && read(entity).map(s -> s.entries().stream()
                .anyMatch(i -> i.id().equals(definition.id()))).orElse(false);
    }
    static void observeCompatibility(Entity entity, TraitDefinition definition, boolean present, SpawnSource source) {
        var snapshot = read(entity);
        if (snapshot.isEmpty() || snapshot.get().mode() != Mode.COMPATIBILITY) return;
        var entries = new ArrayList<>(snapshot.get().entries());
        boolean recorded = entries.stream().anyMatch(i -> i.id().equals(definition.id()));
        if (recorded == present) return;
        entries.removeIf(i -> i.id().equals(definition.id()));
        if (present) entries.add(new TraitInstance(definition.id(), definition.version(), source));
        var updated = entity.getAttached(DATA).copy();
        var encodedEntries = encode(new Snapshot(Mode.COMPATIBILITY, "compatibility", entries)).getCompoundOrEmpty("entries");
        updated.put("entries", encodedEntries);
        entity.setAttached(DATA, updated);
    }
    private TraitStorage() {}
}
