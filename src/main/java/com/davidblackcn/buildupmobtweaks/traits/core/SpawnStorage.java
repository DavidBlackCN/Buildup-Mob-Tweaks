package com.davidblackcn.buildupmobtweaks.traits.core;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import java.util.Optional;
import net.fabricmc.fabric.api.attachment.v1.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.raid.Raider;

/** Provenance and birth policy only. Load/travel never changes an existing record's source or mode. */
public final class SpawnStorage {
    public static final AttachmentType<CompoundTag> DATA = AttachmentRegistry.createPersistent(
            BuildupMobTweaks.id("trait_spawn"), CompoundTag.CODEC);
    private static final AttachmentType<Boolean> RAID_PENDING = AttachmentRegistry.createDefaulted(
            BuildupMobTweaks.id("trait_raid_context_pending"), () -> false);
    public record Source(SpawnSource source, TraitStorage.Mode mode, String reason) {}
    public static void bootstrap() {}
    public static Optional<Source> read(Entity entity) {
        var raw = entity.getAttached(DATA);
        if (raw == null || raw.getIntOr("version", -1) != 1) return Optional.empty();
        try {
            return Optional.of(new Source(SpawnSource.valueOf(raw.getStringOr("source", "")),
                    TraitStorage.Mode.valueOf(raw.getStringOr("mode", "")), raw.getString("reason").orElseThrow()));
        } catch (IllegalArgumentException | java.util.NoSuchElementException exception) { return Optional.empty(); }
    }
    public static void capture(Mob mob) {
        if (mob.level().isClientSide() || mob.hasAttached(DATA)) return;
        var reason = mob.spawnReason();
        boolean continuation = mob.isLoadedFromDisk() || reason == null || reason == EntitySpawnReason.LOAD
                || reason == EntitySpawnReason.CONVERSION || reason == EntitySpawnReason.DIMENSION_TRAVEL;
        // Preserve source ambiguity on old individuals instead of claiming their original source was LOAD.
        var source = continuation ? SpawnSource.UNKNOWN : SpawnSource.classify(reason,
                mob instanceof Raider raider && raider.hasRaid());
        var raw = new CompoundTag();
        raw.putInt("version", 1);
        raw.putString("source", source.name());
        raw.putString("reason", reason == null ? "UNKNOWN" : reason.name());
        boolean unified = !continuation && BuildupMobTweaks.config().traits.unified_selection.get();
        raw.putString("mode", (unified ? TraitStorage.Mode.UNIFIED : TraitStorage.Mode.COMPATIBILITY).name());
        mob.setAttached(DATA, raw);
        if (!continuation && reason == EntitySpawnReason.EVENT && mob instanceof Raider raider && !raider.hasRaid())
            mob.setAttached(RAID_PENDING, true);
    }
    /** Raid.spawnGroup assigns membership after finalizeSpawn, before the entity is added.
     * Refine provenance only during this same initialization; never run selection a second time. */
    static void onLoad(Mob mob) {
        capture(mob);
        if (!Boolean.TRUE.equals(mob.removeAttached(RAID_PENDING)) || mob.isLoadedFromDisk()
                || !(mob instanceof Raider raider) || !raider.hasRaid()) return;
        var record = read(mob);
        if (record.isEmpty() || record.get().source() != SpawnSource.UNKNOWN
                || !record.get().reason().equals("EVENT")) return;
        var raw = mob.getAttached(DATA).copy();raw.putString("source", SpawnSource.RAID.name());mob.setAttached(DATA, raw);
    }
    /** Deliberate external grants are reserved for a later validated API; COMMAND is not EXTERNAL. */
    private SpawnStorage() {}
}
