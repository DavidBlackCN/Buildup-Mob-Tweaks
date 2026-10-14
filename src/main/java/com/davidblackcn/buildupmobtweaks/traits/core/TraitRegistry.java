package com.davidblackcn.buildupmobtweaks.traits.core;

import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import java.util.*;
import net.minecraft.resources.Identifier;
import static com.davidblackcn.buildupmobtweaks.traits.core.TraitDefinition.Category.*;
import static com.davidblackcn.buildupmobtweaks.traits.core.TraitDefinition.Polarity.*;

/** First approved migration batch. FeatureId inventory is not automatically a trait registry. */
public final class TraitRegistry {
    private static final Map<Identifier, TraitDefinition> DEFINITIONS;
    static {
        var definitions = new LinkedHashMap<Identifier, TraitDefinition>();
        add(definitions, FeatureId.ZOMBIE_DOOR_GUARD, 2, DEFENSE, MIXED, "zombie", "husk");
        add(definitions, FeatureId.ENDERMAN_COMBO, 3, COMBAT_TACTICS, MIXED, "enderman");
        add(definitions, FeatureId.EVOKER_FIREBALL, 3, COMBAT_TACTICS, MIXED, "evoker");
        add(definitions, FeatureId.WITHER_SKELETON_SKULL, 3, COMBAT_TACTICS, MIXED, "wither_skeleton");
        add(definitions, FeatureId.HUSK_SAND_BURROW, 2, ENVIRONMENTAL_COMBAT, POSITIVE, "husk");
        add(definitions, FeatureId.BOGGED_SPORE_RETREAT, 2, COMBAT_TACTICS, POSITIVE, "bogged");
        add(definitions, FeatureId.PIGLIN_ITEM_DODGE, 2, COMBAT_TACTICS, POSITIVE, "piglin");
        DEFINITIONS = Collections.unmodifiableMap(definitions);
    }
    private static void add(Map<Identifier, TraitDefinition> map, FeatureId feature, int tier,
                            TraitDefinition.Category category, TraitDefinition.Polarity polarity, String... species) {
        var ids = Arrays.stream(species).map(s -> "minecraft:" + s).collect(java.util.stream.Collectors.toSet());
        var definition = new TraitDefinition(feature, 1, tier, category, polarity, ids);
        if (map.putIfAbsent(definition.id(), definition) != null) throw new IllegalStateException("Duplicate trait");
    }
    public static Optional<TraitDefinition> find(Identifier id) { return Optional.ofNullable(DEFINITIONS.get(id)); }
    public static Optional<TraitDefinition> find(FeatureId feature) { return find(feature.id()); }
    public static Collection<TraitDefinition> definitions() { return DEFINITIONS.values(); }
    private TraitRegistry() {}
}
