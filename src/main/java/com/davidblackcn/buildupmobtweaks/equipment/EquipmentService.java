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

/** Single birth-time attempt, independent of Traits. No reload backfill, replacement or custom drops. */
public final class EquipmentService {
    public static final AttachmentType<CompoundTag> DATA = AttachmentRegistry.createPersistent(
            BuildupMobTweaks.id("equipment_assignment"), CompoundTag.CODEC);
    private final FeatureRegistry features;
    public EquipmentService(FeatureRegistry features) { this.features = features; }

    public void register() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (eligible(entity) && !entity.hasAttached(DATA)) initialize((Mob) entity, EquipmentPools.get(level.getServer()));
        });
        ServerLivingEntityEvents.MOB_CONVERSION.register((previous, converted, context) -> {
            if (!eligible(converted)) return;
            var data = previous.getAttached(DATA);
            converted.setAttached(DATA, data == null ? state("conversion_skipped") : data.copy());
        });
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
        if (!features.isEnabled(FeatureId.EQUIPMENT_ASSIGNMENT)) { data.putString("outcome", "disabled"); return; }
        var pool = pools.rules().stream().filter(rule -> rule.matches(mob)).findFirst();
        if (pool.isEmpty()) { data.putString("outcome", "no_rule"); return; }
        var rule = pool.orElseThrow();
        data.putString("pool", rule.id().toString());
        EquipmentSlot slot = rule.rule().slot();
        if (!mob.getItemBySlot(slot).isEmpty()) { data.putString("outcome", "occupied"); return; }
        if (mob.getRandom().nextInt(1000) >= features.equipmentChance()) { data.putString("outcome", "missed"); return; }
        ItemStack stack = new ItemStack(rule.select(mob.getRandom()), 1);
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