package com.davidblackcn.buildupmobtweaks.traits;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;

/** The presence of a state, including an empty roll, is the initialization marker. */
public record TraitState(int version, String outcome, String origin, List<Entry> entries) {
    public static final int VERSION = 1;
    private static final Set<String> OUTCOMES = Set.of(
            "rolled", "legacy_skipped", "source_unknown_skipped", "disabled", "conversion_without_source");
    public record Entry(String id, int level) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("id").forGetter(Entry::id),
                Codec.intRange(1, 3).fieldOf("level").forGetter(Entry::level)
        ).apply(instance, Entry::new));
    }

    public static final Codec<TraitState> CODEC = RecordCodecBuilder.<TraitState>create(instance -> instance.group(
            Codec.INT.fieldOf("version").forGetter(TraitState::version),
            Codec.STRING.fieldOf("outcome").forGetter(TraitState::outcome),
            Codec.STRING.fieldOf("origin").forGetter(TraitState::origin),
            Entry.CODEC.listOf().fieldOf("entries").forGetter(TraitState::entries)
    ).apply(instance, TraitState::new)).validate(TraitState::validate);

    public TraitState {
        entries = List.copyOf(entries);
    }

    private static DataResult<TraitState> validate(TraitState state) {
        if (state.version != VERSION || state.entries.size() > 1
                || !OUTCOMES.contains(state.outcome)
                || (!state.outcome.equals("rolled") && !state.entries.isEmpty())) {
            return DataResult.error(() -> "Unsupported trait version or conflicting entries");
        }
        for (Entry entry : state.entries) {
            boolean known = false;
            for (TraitKind kind : TraitKind.values()) {
                known |= kind.id().equals(entry.id()) && kind.level() == entry.level();
            }
            if (!known) return DataResult.error(() -> "Unknown trait or invalid level");
        }
        return DataResult.success(state);
    }

    public static TraitState empty(String outcome, String origin) {
        return new TraitState(VERSION, outcome, origin, List.of());
    }
}
