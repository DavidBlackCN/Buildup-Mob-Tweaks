/* Behavioral reference: Mob AI Tweaks, Copyright (c) 2024 N0t_UN_Owen, MIT. */
package com.davidblackcn.buildupmobtweaks.rebuild.raid;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import net.fabricmc.fabric.api.attachment.v1.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.phys.Vec3;
import java.util.*;

public final class RaidState {
    /** Force attachment type registration during Mod initialization, before any saved entity is decoded. */
    public static void bootstrap(){}
    public static final AttachmentType<CompoundTag> WITCH=AttachmentRegistry.createPersistent(BuildupMobTweaks.id("witch_rebuild"),CompoundTag.CODEC);
    public static final AttachmentType<CompoundTag> EVOKER=AttachmentRegistry.createPersistent(BuildupMobTweaks.id("evoker_rebuild"),CompoundTag.CODEC);
    public static final AttachmentType<CompoundTag> VEX=AttachmentRegistry.createPersistent(BuildupMobTweaks.id("vex_rebuild"),CompoundTag.CODEC);
    public static final AttachmentType<CompoundTag> BALL=AttachmentRegistry.createPersistent(BuildupMobTweaks.id("evoker_rebuild_ball"),CompoundTag.CODEC);
    public static final AttachmentType<Boolean> ORBIT=AttachmentRegistry.create(BuildupMobTweaks.id("evoker_rebuild_orbit"),b->b.persistent(com.mojang.serialization.Codec.BOOL).syncWith(net.minecraft.network.codec.ByteBufCodecs.BOOL,AttachmentSyncPredicate.all()));
    public static final AttachmentType<Integer> INDEX=AttachmentRegistry.create(BuildupMobTweaks.id("evoker_rebuild_index"),b->b.initializer(()->0).syncWith(net.minecraft.network.codec.ByteBufCodecs.VAR_INT,AttachmentSyncPredicate.all()));
    public static final AttachmentType<Boolean> ACTIVE=AttachmentRegistry.create(BuildupMobTweaks.id("evoker_rebuild_active"),b->b.initializer(()->false).syncWith(net.minecraft.network.codec.ByteBufCodecs.BOOL,AttachmentSyncPredicate.all()));
    public static final AttachmentType<Boolean> LIVE=AttachmentRegistry.createDefaulted(BuildupMobTweaks.id("evoker_rebuild_ball_live"),()->false);
    private static final AttachmentType<Runtime> RUNTIME=AttachmentRegistry.createDefaulted(BuildupMobTweaks.id("raid_rebuild_runtime"),Runtime::new);
    public static boolean known(Entity m,AttachmentType<CompoundTag> type){return m.hasAttached(type)&&m.getAttached(type).getIntOr("version",-1)==1;}
    public static void initialize(Entity m,AttachmentType<CompoundTag> type){if(!m.hasAttached(type)){var d=new CompoundTag();d.putInt("version",1);d.putString("birth","none");m.setAttached(type,d);}}
    public static Runtime runtime(Entity m){return m.getAttachedOrCreate(RUNTIME);}
    public static final class Runtime {
        public boolean installed,preview,charging;public LivingEntity target;public long end,start;public int previews,throwsMade,leaps,jumps,casts,releases,totems,charges,hits;
        public Vec3 direction=Vec3.ZERO;public final List<SmallFireball> balls=new ArrayList<>();
    }
    private RaidState(){}
}
