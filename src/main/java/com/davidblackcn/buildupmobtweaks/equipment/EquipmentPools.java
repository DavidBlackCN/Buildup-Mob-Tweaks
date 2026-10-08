package com.davidblackcn.buildupmobtweaks.equipment;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.fabricmc.fabric.api.resource.v1.DataResourceLoader;
import net.fabricmc.fabric.api.resource.v1.DataResourceStore;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.v1.reloader.SimpleReloadListener;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/** Immutable snapshots belong to a server's resource store, not a global last-reload cache. */
public final class EquipmentPools extends SimpleReloadListener<EquipmentPools.Prepared> {
    private static final DataResourceStore.Key<Loaded> KEY = new DataResourceStore.Key<>();
    private static final FileToIdConverter FILES = FileToIdConverter.json("buildupmobtweaks/equipment_pool");
    private static final Set<String> FIELDS = Set.of("version", "entities", "slot", "adult_only", "dimensions", "difficulties", "entries");
    private static final Set<String> ENTRY_FIELDS = Set.of("item", "weight");
    public record Parsed(Identifier id, String source, EquipmentPool rule) {}
    public record Prepared(List<Parsed> rules, int rejected) {}
    public record Choice(List<Item> items, int weight) {
        public Choice { items = List.copyOf(items); }
    }
    public record Resolved(Identifier id, EquipmentPool rule, Set<EntityType<?>> entities, List<Choice> choices) {
        public Resolved { entities = Set.copyOf(entities); choices = List.copyOf(choices); }
        public boolean matches(Mob mob) {
            return entities.contains(mob.getType()) && (!rule.adultOnly() || !mob.isBaby())
                    && (rule.dimensions().isEmpty() || rule.dimensions().contains(mob.level().dimension().identifier()))
                    && (rule.difficulties().isEmpty() || rule.difficulties().contains(mob.level().getDifficulty()));
        }
        public Item select(RandomSource random) {
            int point = random.nextInt(choices.stream().mapToInt(Choice::weight).sum());
            for (Choice choice : choices) {
                point -= choice.weight();
                if (point < 0) return choice.items().get(random.nextInt(choice.items().size()));
            }
            throw new IllegalStateException("Validated pool must contain a choice");
        }
    }
    public record Snapshot(List<Resolved> rules, int rejected) {
        public Snapshot { rules = List.copyOf(rules); }
    }

    public static void register() {
        DataResourceLoader.get().registerReloadListener(BuildupMobTweaks.id("equipment_pools"), new EquipmentPools());
        ServerLifecycleEvents.SERVER_STARTING.register(EquipmentPools::get);
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, manager, success) -> {
            if (success) get(server);
        });
    }
    public static Snapshot get(MinecraftServer server) { return server.getOrThrow(KEY).snapshot(server); }

    @Override
    protected Prepared prepare(SharedState state) {
        List<Parsed> rules = new ArrayList<>();
        int rejected = 0;
        var files = FILES.listMatchingResources(state.resourceManager());
        for (var entry : files.entrySet().stream().sorted(Comparator.comparing(e -> e.getKey().toString())).toList()) {
            String source = entry.getKey() + " (pack " + entry.getValue().sourcePackId() + ")";
            try (var reader = entry.getValue().openAsReader()) {
                JsonElement json = JsonParser.parseReader(reader);
                validateFields(json);
                var decoded = EquipmentPool.CODEC.parse(JsonOps.INSTANCE, json);
                if (decoded.error().isPresent()) {
                    reject(source, decoded.error().orElseThrow().message());
                    rejected++;
                } else rules.add(new Parsed(FILES.fileToId(entry.getKey()), source, decoded.result().orElseThrow()));
            } catch (IOException | JsonParseException | IllegalArgumentException error) {
                reject(source, error.getMessage());
                rejected++;
            }
        }
        return new Prepared(List.copyOf(rules), rejected);
    }

    /** Reject misspellings and unsupported count/component/replacement fields instead of silently ignoring them. */
    public static void validateFields(JsonElement json) {
        if (!json.isJsonObject()) throw new IllegalArgumentException("Expected pool object");
        for (String key : json.getAsJsonObject().keySet()) {
            if (!FIELDS.contains(key)) throw new IllegalArgumentException("Unknown pool field: " + key);
        }
        requireInteger(json.getAsJsonObject().get("version"), "version");
        var entries = json.getAsJsonObject().get("entries");
        if (entries != null && entries.isJsonArray()) {
            for (var entry : entries.getAsJsonArray()) {
                if (entry.isJsonObject()) requireInteger(entry.getAsJsonObject().get("weight"), "weight");
                if (entry.isJsonObject()) for (String key : entry.getAsJsonObject().keySet()) {
                    if (!ENTRY_FIELDS.contains(key)) throw new IllegalArgumentException("Unknown entry field: " + key);
                }
            }
        }
    }

    private static void requireInteger(JsonElement value, String field) {
        if (value == null) return; // Missing required fields are reported by the codec.
        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
            throw new IllegalArgumentException(field + " must be an integer number");
        }
        try { value.getAsBigDecimal().intValueExact(); }
        catch (ArithmeticException error) { throw new IllegalArgumentException(field + " must be an exact 32-bit integer", error); }
    }

    @Override
    protected void apply(Prepared prepared, SharedState state) {
        // 26.3's pending Named tag sets are not bound yet. Resolve their contents only after
        // WorldLoader / MinecraftServer has committed tags, against this server's resource store.
        state.get(DataResourceLoader.DATA_RESOURCE_STORE_KEY).put(KEY, new Loaded(prepared));
    }

    private static final class Loaded {
        private final Prepared prepared;
        private Snapshot resolved;
        private Loaded(Prepared prepared) { this.prepared = prepared; }

        // Called on the server thread at startup/successful reload or first entity/query access.
        private Snapshot snapshot(MinecraftServer server) {
            if (resolved != null) return resolved;
            List<Resolved> rules = new ArrayList<>();
            int rejected = prepared.rejected();
            for (Parsed parsed : prepared.rules()) {
                try {
                    rules.add(resolve(parsed.id(), parsed.rule(), server.registryAccess(), server.getWorldData().enabledFeatures()));
                } catch (IllegalArgumentException error) {
                    reject(parsed.source(), error.getMessage()); rejected++;
                }
            }
            resolved = new Snapshot(rules, rejected);
            BuildupMobTweaks.LOGGER.info("Equipment pools ready: {} active, {} rejected", rules.size(), rejected);
            return resolved;
        }
    }
    public static Resolved resolve(Identifier id, EquipmentPool rule, HolderLookup.Provider lookup, FeatureFlagSet flags) {
        Set<EntityType<?>> entities = new HashSet<>();
        for (var selector : rule.entities()) entities.addAll(resolveSelector(selector, Registries.ENTITY_TYPE, lookup));
        for (var type : entities) {
            if (!EquipmentPool.SUPPORTED_ENTITIES.contains(BuiltInRegistries.ENTITY_TYPE.getKey(type).toString())) {
                throw new IllegalArgumentException("S1-C supports only zombie/husk/drowned: " + BuiltInRegistries.ENTITY_TYPE.getKey(type));
            }
        }
        List<Choice> choices = new ArrayList<>();
        for (var entry : rule.entries()) {
            var items = resolveSelector(entry.item(), Registries.ITEM, lookup).stream()
                    .sorted(Comparator.comparing(item -> BuiltInRegistries.ITEM.getKey(item).toString())).toList();
            for (Item item : items) {
                if (item == Items.AIR || !item.isEnabled(flags)) {
                    throw new IllegalArgumentException("Air or disabled item: " + BuiltInRegistries.ITEM.getKey(item));
                }
            }
            choices.add(new Choice(items, entry.weight()));
        }
        return new Resolved(id, rule, entities, choices);
    }

    private static <T> List<T> resolveSelector(EquipmentPool.Selector selector, ResourceKey<? extends Registry<T>> registry,
                                              HolderLookup.Provider provider) {
        var lookup = provider.lookupOrThrow(registry);
        if (!selector.tag()) return List.of(lookup.get(ResourceKey.create(registry, selector.id()))
                .orElseThrow(() -> new IllegalArgumentException("Unknown ID: " + selector.id())).value());
        var values = lookup.get(TagKey.create(registry, selector.id()))
                .orElseThrow(() -> new IllegalArgumentException("Unknown tag: #" + selector.id())).stream().map(Holder::value).toList();
        if (values.isEmpty()) throw new IllegalArgumentException("Empty tag: #" + selector.id());
        return values;
    }

    private static void reject(String source, String reason) {
        BuildupMobTweaks.LOGGER.error("Equipment pool rejected: {}: {}. This rule is disabled for this reload.", source, reason);
    }
}