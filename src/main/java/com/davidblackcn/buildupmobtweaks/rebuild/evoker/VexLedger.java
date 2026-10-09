package com.davidblackcn.buildupmobtweaks.rebuild.evoker;
import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import net.fabricmc.fabric.api.attachment.v1.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.monster.illager.Evoker;
/** Same v1 schema as the inactive S2 ledger. Never resolves or loads an owner's chunk. */
public final class VexLedger {
    public static final AttachmentType<CompoundTag> INDEX=AttachmentRegistry.createPersistent(BuildupMobTweaks.id("vex_owners"),CompoundTag.CODEC);
    private static CompoundTag index(ServerLevel l){var world=l.getServer().overworld();if(!world.hasAttached(INDEX)){var d=new CompoundTag();d.putInt("version",1);world.setAttached(INDEX,d);}return world.getAttached(INDEX);}
    public static void register(){ServerEntityEvents.ENTITY_LOAD.register((e,l)->{if(e instanceof Vex v)track(v);});
        ServerEntityEvents.ENTITY_UNLOAD.register((e,l)->{if(e instanceof Vex v&&v.getRemovalReason()!=null&&v.getRemovalReason().shouldDestroy())release(v);});}
    public static void track(Vex v){if(!(v.level() instanceof ServerLevel l)||v.getOwnerReference()==null)return;var d=index(l);if(d.getIntOr("version",-1)!=1)return;
        String owner=v.getOwnerReference().getUUID().toString();var entries=d.getCompoundOrEmpty(owner);entries.putBoolean(v.getStringUUID(),true);d.put(owner,entries);}
    private static void release(Vex v){if(!(v.level() instanceof ServerLevel l)||v.getOwnerReference()==null)return;var d=index(l);if(d.getIntOr("version",-1)!=1)return;
        String owner=v.getOwnerReference().getUUID().toString();var entries=d.getCompoundOrEmpty(owner);entries.remove(v.getStringUUID());if(entries.isEmpty())d.remove(owner);else d.put(owner,entries);}
    public static int count(Evoker m){var d=index((ServerLevel)m.level());return d.getIntOr("version",-1)==1?d.getCompoundOrEmpty(m.getStringUUID()).size():Integer.MAX_VALUE-24;}
    private VexLedger(){}
}
