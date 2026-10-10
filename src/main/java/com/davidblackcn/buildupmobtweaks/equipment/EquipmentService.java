package com.davidblackcn.buildupmobtweaks.equipment;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import com.davidblackcn.buildupmobtweaks.feature.FeatureRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

/** Single birth-time attempt, independent of Traits. No reload backfill, replacement or custom drops. */
public final class EquipmentService {
    public static final AttachmentType<CompoundTag> DATA = AttachmentRegistry.createPersistent(
            BuildupMobTweaks.id("equipment_assignment"), CompoundTag.CODEC);
    private final FeatureRegistry features;
    private static EquipmentService instance;
    public static final TagKey<EntityType<?>> EXCLUDED = TagKey.create(Registries.ENTITY_TYPE, BuildupMobTweaks.id("equipment_assignment_excluded"));
    public EquipmentService(FeatureRegistry features) { this.features = features; }
    public static EquipmentService instance() { return instance; }
    public static void bootstrap() {}

    public void register() {
        instance = this;
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (eligible(entity) && !entity.hasAttached(DATA)) initialize((Mob) entity, EquipmentPools.get(level.getServer()));
        });
        ServerLivingEntityEvents.MOB_CONVERSION.register((previous, converted, context) -> {
            if (!eligible(converted)) return;
            var data = previous.getAttached(DATA);
            converted.setAttached(DATA, data == null ? state("conversion_skipped") : data.copy());
        });
    }
    /** Narrow native initialization hook, before its default weapon setter. Never replaces an occupied slot. */
    public boolean defaultWeapon(Mob mob) {
        var previous = mob.getAttached(DATA);
        if (previous != null && previous.getIntOr("version", -1) == 1) return false;
        if (previous == null && mob.level() instanceof ServerLevel level) initialize(mob, EquipmentPools.get(level.getServer()));
        return mob.getMainHandItem().isEmpty();
    }
    public static boolean eligible(Entity entity) {
        return entity instanceof Mob && EquipmentPool.SUPPORTED_ENTITIES.contains(
                BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString());
    }
    private static CompoundTag state(String outcome) {
        var data = new CompoundTag();
        data.putInt("version", 1);
        data.putString("outcome", outcome);
        return data;
    }

    public void initialize(Mob mob, EquipmentPools.Snapshot pools) {
        if (!(mob.level() instanceof ServerLevel) || !eligible(mob) || mob.hasAttached(DATA)) return;
        var data = state("skipped_source");
        // Even an empty or disabled attempt is final; preserve unknown versions on subsequent loads.
        mob.setAttached(DATA, data);
        var reason = mob.spawnReason();
        if (mob.isLoadedFromDisk() || reason == null || reason == EntitySpawnReason.LOAD
                || reason == EntitySpawnReason.CONVERSION || reason == EntitySpawnReason.DIMENSION_TRAVEL) return;
        data.putString("origin", reason.name());
        if (!features.isEnabled(FeatureId.EQUIPMENT_ASSIGNMENT) || mob.getType().builtInRegistryHolder().is(EXCLUDED)
                || mob.entityTags().contains("buildupmobtweaks:vanilla_ai")
                || mob.entityTags().contains("buildupmobtweaks:disable_equipment_assignment")) { data.putString("outcome", "disabled"); return; }
        var pool = pools.rules().stream().filter(rule -> rule.matches(mob)).findFirst();
        if (pool.isEmpty()) { data.putString("outcome", "no_rule"); return; }
        var rule = pool.orElseThrow();
        data.putString("pool", rule.id().toString());
        EquipmentSlot slot = rule.rule().slot();
        if (!mob.getItemBySlot(slot).isEmpty()) { data.putString("outcome", "occupied"); return; }
        var birth = slot == EquipmentSlot.MAINHAND ? mob.getType() == EntityTypes.WITHER_SKELETON ? FeatureId.WITHER_SKELETON_BOW
                : mob.getType() == EntityTypes.ZOMBIFIED_PIGLIN ? FeatureId.ZOMBIFIED_PIGLIN_SPAWN_CROSSBOW : null : null;
        if (birth != null && (!features.isEnabled(birth) || mob.entityTags().contains("buildupmobtweaks:disable_" + birth.id().getPath()))) {
            data.putString("outcome", "disabled"); return;
        }
        int chance = birth == null ? features.equipmentChance() : features.birthEquipmentChance(birth);
        if (mob.getRandom().nextInt(1000) >= chance) { data.putString("outcome", "missed"); return; }
        ItemStack stack = new ItemStack(rule.select(mob.getRandom()), 1);
        if (birth == FeatureId.WITHER_SKELETON_BOW && !stack.is(Items.BOW)
                || birth == FeatureId.ZOMBIFIED_PIGLIN_SPAWN_CROSSBOW && !stack.is(Items.CROSSBOW)) {
            data.putString("outcome", "incompatible_weapon"); return;
        }
        if (!mob.canUseSlot(slot) || (slot != EquipmentSlot.MAINHAND && slot != EquipmentSlot.OFFHAND
                && mob.getEquipmentSlotForItem(stack) != slot)) {
            data.putString("outcome", "incompatible_slot"); return;
        }
        mob.setItemSlot(slot, stack);
        // Leave this slot's existing vanilla/mod drop chance unchanged. Never create a second stack on death.
        data.putString("outcome", "equipped");
        data.putString("slot", slot.getSerializedName());
        data.putString("item", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
    }
}
