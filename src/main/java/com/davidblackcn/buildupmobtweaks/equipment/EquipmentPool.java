package com.davidblackcn.buildupmobtweaks.equipment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EquipmentSlot;

/** Version 1: a single weighted item, one slot, no components, counts or executable conditions. */
public record EquipmentPool(int version, List<Selector> entities, EquipmentSlot slot, boolean adultOnly,
                            List<Identifier> dimensions, List<Difficulty> difficulties, List<Entry> entries) {
    public static final Set<String> SUPPORTED_ENTITIES = Set.of("minecraft:zombie", "minecraft:husk", "minecraft:drowned");
    private static final Set<EquipmentSlot> SLOTS = Set.of(EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND,
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);
    private static final Codec<EquipmentSlot> SLOT_CODEC = EquipmentSlot.CODEC.validate(slot -> SLOTS.contains(slot)
            ? DataResult.success(slot) : DataResult.error(() -> "Only hand and humanoid armor slots are supported"));

    public record Selector(Identifier id, boolean tag) {
        public static final Codec<Selector> CODEC = Codec.STRING.comapFlatMap(value -> {
            boolean tag = value.startsWith("#");
            String raw = tag ? value.substring(1) : value;
            Identifier id = Identifier.tryParse(raw);
            return id != null && raw.contains(":") ? DataResult.success(new Selector(id, tag))
                    : DataResult.error(() -> "Expected namespaced ID or #namespace:tag: " + value);
        }, selector -> (selector.tag ? "#" : "") + selector.id);
    }

    public record Entry(Selector item, int weight) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Selector.CODEC.fieldOf("item").forGetter(Entry::item),
                Codec.intRange(1, 10000).fieldOf("weight").forGetter(Entry::weight)
        ).apply(instance, Entry::new));
    }

    public static final Codec<EquipmentPool> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(1, 1).fieldOf("version").forGetter(EquipmentPool::version),
            Selector.CODEC.listOf(1, 32).fieldOf("entities").forGetter(EquipmentPool::entities),
            SLOT_CODEC.fieldOf("slot").forGetter(EquipmentPool::slot),
            Codec.BOOL.optionalFieldOf("adult_only", true).forGetter(EquipmentPool::adultOnly),
            Identifier.CODEC.listOf(0, 16).optionalFieldOf("dimensions", List.of()).forGetter(EquipmentPool::dimensions),
            Difficulty.CODEC.listOf(0, 4).optionalFieldOf("difficulties", List.of()).forGetter(EquipmentPool::difficulties),
            Entry.CODEC.listOf(1, 128).fieldOf("entries").forGetter(EquipmentPool::entries)
    ).apply(instance, EquipmentPool::new));

    public EquipmentPool {
        entities = List.copyOf(entities);
        dimensions = List.copyOf(dimensions);
        difficulties = List.copyOf(difficulties);
        entries = List.copyOf(entries);
    }
}