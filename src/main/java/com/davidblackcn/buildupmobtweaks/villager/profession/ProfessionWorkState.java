package com.davidblackcn.buildupmobtweaks.villager.profession;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import com.davidblackcn.buildupmobtweaks.villager.*;
import net.fabricmc.fabric.api.attachment.v1.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.pathfinder.Path;
import java.util.*;

/** New action cooldowns only. Native inventory, farmer_work and zombie profession_rebuild are separate. */
public final class ProfessionWorkState {
    public static final AttachmentType<CompoundTag> DATA=AttachmentRegistry.createPersistent(BuildupMobTweaks.id("villager_work_v1"),CompoundTag.CODEC);
    public static final AttachmentType<CompoundTag> TARGET=AttachmentRegistry.createPersistent(BuildupMobTweaks.id("villager_work_target_v1"),CompoundTag.CODEC);
    public static final AttachmentType<Integer> VISUAL=AttachmentRegistry.create(BuildupMobTweaks.id("villager_work_visual"),b->b.initializer(()->0).syncWith(ByteBufCodecs.VAR_INT,AttachmentSyncPredicate.all()));
    private static final AttachmentType<Runtime> RUNTIME=AttachmentRegistry.createDefaulted(BuildupMobTweaks.id("villager_work_runtime"),Runtime::new);
    public static final class Runtime {
        public WorkActivity active;public WorkActivity.Context context;public Brain<?> brain;public boolean meal;public Identifier adapter;public boolean scanInitialized;
        public WalkTarget walk;public PositionTracker look;public Path path;public Entity projectile;
        public long nextScan;public int starts,completed,stops,operations,scans;public String reason="idle";
    }
    public static Runtime runtime(Villager v){return v.getAttachedOrCreate(RUNTIME);}
    public static Optional<Runtime> peek(Villager v){return v.hasAttached(RUNTIME)?Optional.of(v.getAttached(RUNTIME)):Optional.empty();}
    public static boolean busy(Villager v){return peek(v).map(r->r.active!=null).orElse(false);}
    public static long now(Entity e){return e.level().getServer().overworld().getGameTime();}
    public static boolean known(Entity e){
        if(!e.hasAttached(DATA))return true;var d=e.getAttached(DATA);
        if(d.getIntOr("version",-1)!=1||!d.keySet().equals(Set.of("version","cooldowns")))return false;
        var times=d.getCompound("cooldowns");if(times.isEmpty())return false;
        return times.get().keySet().stream().allMatch(k->Identifier.tryParse(k)!=null&&times.get().getLong(k).orElse(-1L)>=0);
    }
    public static long ready(Entity e,Identifier activity){return e.hasAttached(DATA)?e.getAttached(DATA).getCompound("cooldowns").map(c->c.getLongOr(activity.toString(),0)).orElse(Long.MAX_VALUE):0;}
    public static void defer(Entity e,Identifier activity,int ticks){
        if(!known(e))return;var d=e.hasAttached(DATA)?e.getAttached(DATA).copy():new CompoundTag();
        var times=d.getCompound("cooldowns").map(CompoundTag::copy).orElseGet(CompoundTag::new);
        times.putLong(activity.toString(),Math.max(times.getLongOr(activity.toString(),0),now(e)+ticks));d.putInt("version",1);d.put("cooldowns",times);e.setAttached(DATA,d);
    }
    public static boolean targetReady(Entity e){
        if(!e.hasAttached(TARGET))return true;var d=e.getAttached(TARGET);
        return d.keySet().equals(Set.of("version","ready"))&&d.getIntOr("version",-1)==1&&d.getLongOr("ready",-1)>=0&&now(e)>=d.getLongOr("ready",Long.MAX_VALUE);
    }
    public static void targetCooldown(Entity e,int ticks){var d=new CompoundTag();d.putInt("version",1);d.putLong("ready",now(e)+ticks);e.setAttached(TARGET,d);}
    public static void visual(Villager v,net.minecraft.world.item.Item item,int phase){int value=(net.minecraft.core.registries.BuiltInRegistries.ITEM.getId(item)<<4)|phase;if(v.getAttachedOrCreate(VISUAL)!=value)v.setAttached(VISUAL,value);}
    public static void clearVisual(Villager v){if(v.hasAttached(VISUAL)&&v.getAttached(VISUAL)!=0)v.setAttached(VISUAL,0);}
    public static void unload(Villager v){v.removeAttached(RUNTIME);}
    public static void bootstrap(){}
    private ProfessionWorkState(){}
}
