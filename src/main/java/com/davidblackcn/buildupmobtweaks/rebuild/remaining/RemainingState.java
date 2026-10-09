package com.davidblackcn.buildupmobtweaks.rebuild.remaining;
import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import net.fabricmc.fabric.api.attachment.v1.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import java.util.*;
public final class RemainingState {
    public static void bootstrap(){}
    public static final AttachmentType<CompoundTag> DATA=AttachmentRegistry.createPersistent(BuildupMobTweaks.id("remaining_rebuild"),CompoundTag.CODEC);
    public static final AttachmentType<Boolean> ORBIT=AttachmentRegistry.create(BuildupMobTweaks.id("blaze_rebuild_orbit"),b->b.persistent(com.mojang.serialization.Codec.BOOL).syncWith(net.minecraft.network.codec.ByteBufCodecs.BOOL,AttachmentSyncPredicate.all()));
    public static final AttachmentType<Integer> INDEX=AttachmentRegistry.create(BuildupMobTweaks.id("blaze_rebuild_index"),b->b.initializer(()->0).syncWith(net.minecraft.network.codec.ByteBufCodecs.VAR_INT,AttachmentSyncPredicate.all()));
    public static final AttachmentType<Integer> COUNT=AttachmentRegistry.create(BuildupMobTweaks.id("blaze_rebuild_count"),b->b.initializer(()->3).syncWith(net.minecraft.network.codec.ByteBufCodecs.VAR_INT,AttachmentSyncPredicate.all()));
    public static final AttachmentType<Integer> CHARGE=AttachmentRegistry.create(BuildupMobTweaks.id("ghast_rebuild_charge"),b->b.initializer(()->0).syncWith(net.minecraft.network.codec.ByteBufCodecs.VAR_INT,AttachmentSyncPredicate.all()));
    private static final AttachmentType<Boolean> LIVE=AttachmentRegistry.createDefaulted(BuildupMobTweaks.id("remaining_rebuild_live"),()->false);
    private static final AttachmentType<Runtime> RUNTIME=AttachmentRegistry.createDefaulted(BuildupMobTweaks.id("remaining_rebuild_runtime"),Runtime::new);
    public static boolean known(Entity e){return e.hasAttached(DATA)&&e.getAttached(DATA).getIntOr("version",-1)==1;}
    public static void initialize(Entity e){if(!e.hasAttached(DATA)){var d=new CompoundTag();d.putInt("version",1);d.putString("birth","none");e.setAttached(DATA,d);}}
    public static boolean live(Entity e){return Boolean.TRUE.equals(e.getAttached(LIVE));}
    public static void live(Entity e,boolean live){e.setAttached(LIVE,live);}
    public static Runtime runtime(Entity e){return e.getAttachedOrCreate(RUNTIME);}
    public static final class Runtime {
        public boolean installed,hidden,landed,paused;public LivingEntity target;public long start,end;public int sequence,volley=3;
        public int shots,bindings,releases,hides,helpBlocks,particles,combos,teleports,attacks,bursts,arrows;
        public final List<SmallFireball> balls=new ArrayList<>();
    }
    private RemainingState(){}
}
