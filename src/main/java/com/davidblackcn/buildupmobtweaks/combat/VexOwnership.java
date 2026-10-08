package com.davidblackcn.buildupmobtweaks.combat;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import net.fabricmc.fabric.api.attachment.v1.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.monster.illager.Evoker;

/** World-persistent ownership ledger. Unloaded minions retain their slots; no chunks are loaded to count them. */
public final class VexOwnership {
    public static final AttachmentType<CompoundTag> INDEX = AttachmentRegistry.createPersistent(BuildupMobTweaks.id("vex_owners"), CompoundTag.CODEC);
    private VexOwnership() {}
    private static CompoundTag index(ServerLevel level) {
        var world = level.getServer().overworld();
        if (!world.hasAttached(INDEX)) { var tag = new CompoundTag(); tag.putInt("version", 1); world.setAttached(INDEX, tag); }
        return world.getAttached(INDEX);
    }
    public static void register() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> { if (entity instanceof Vex vex) track(vex); });
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, level) -> {
            if (entity instanceof Vex vex && vex.getRemovalReason() != null && vex.getRemovalReason().shouldDestroy()) release(vex);
        });
    }
    public static void track(Vex vex) {
        if (!(vex.level() instanceof ServerLevel level) || vex.getOwnerReference() == null) return;
        var tag = index(level); if (tag.getIntOr("version", -1) != 1) return;
        var owner = vex.getOwnerReference().getUUID().toString();
        var entries = tag.getCompoundOrEmpty(owner); entries.putBoolean(vex.getStringUUID(), true); tag.put(owner, entries);
    }
    public static void release(Vex vex) {
        if (!(vex.level() instanceof ServerLevel level) || vex.getOwnerReference() == null) return;
        var tag = index(level); if (tag.getIntOr("version", -1) != 1) return;
        var owner = vex.getOwnerReference().getUUID().toString(); var entries = tag.getCompoundOrEmpty(owner);
        entries.remove(vex.getStringUUID()); if (entries.isEmpty()) tag.remove(owner); else tag.put(owner, entries);
    }
    public static int count(Evoker evoker) {
        var tag = index((ServerLevel) evoker.level());
        return tag.getIntOr("version", -1) == 1 ? tag.getCompoundOrEmpty(evoker.getStringUUID()).size() : Integer.MAX_VALUE - 3;
    }
}