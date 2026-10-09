package com.davidblackcn.buildupmobtweaks.rebuild.skeleton;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import net.fabricmc.fabric.api.attachment.v1.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.projectile.hurtingprojectile.WitherSkull;
import java.util.*;

/** Separate from skeleton_rebuild: no reserve items or legacy attachment migration. */
public final class ExtendedSkeletonState {
    public static final AttachmentType<CompoundTag> DATA=AttachmentRegistry.createPersistent(BuildupMobTweaks.id("extended_skeleton_rebuild"),CompoundTag.CODEC);
    public static final AttachmentType<CompoundTag> SKULL=AttachmentRegistry.createPersistent(BuildupMobTweaks.id("skeleton_owned_skull"),CompoundTag.CODEC);
    public static final AttachmentType<Integer> SPIN=AttachmentRegistry.create(BuildupMobTweaks.id("parched_spin"),b->b.initializer(()->0).syncWith(ByteBufCodecs.VAR_INT,AttachmentSyncPredicate.all()));
    private static final AttachmentType<Runtime> RUNTIME=AttachmentRegistry.createDefaulted(BuildupMobTweaks.id("extended_skeleton_runtime"),Runtime::new);
    private static final AttachmentType<Boolean> LIVE=AttachmentRegistry.createDefaulted(BuildupMobTweaks.id("skeleton_skull_live"),()->false);
    public static void bootstrap(){}
    public static boolean known(Entity e,AttachmentType<CompoundTag> type){return e.hasAttached(type)&&e.getAttached(type).getIntOr("version",-1)==1;}
    public static Runtime runtime(Entity e){return e.getAttachedOrCreate(RUNTIME);}
    public static boolean live(WitherSkull s){return Boolean.TRUE.equals(s.getAttached(LIVE));}
    public static void markLive(WitherSkull s){s.setAttached(LIVE,true);}
    public static final class Runtime {
        public boolean installed;public LivingEntity target;public Goal active;
        public String phase="idle";public int spins,skulls,shots,homingTicks,hits,cancels;
        public final List<WitherSkull> projectiles=new ArrayList<>();
    }
    private ExtendedSkeletonState(){}
}
