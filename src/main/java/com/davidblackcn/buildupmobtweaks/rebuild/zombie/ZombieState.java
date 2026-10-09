/* Behavioral reference: Mob AI Tweaks, Copyright (c) 2024 N0t_UN_Owen, MIT. */
package com.davidblackcn.buildupmobtweaks.rebuild.zombie;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.attachment.v1.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Display.BlockDisplay;
import net.minecraft.world.entity.monster.zombie.Zombie;

public final class ZombieState {
    public static final AttachmentType<CompoundTag> DATA=AttachmentRegistry.createPersistent(BuildupMobTweaks.id("zombie_rebuild"),CompoundTag.CODEC);
    public static final AttachmentType<Boolean> DISPLAY=AttachmentRegistry.create(BuildupMobTweaks.id("door_rebuild_display"),b->b.persistent(com.mojang.serialization.Codec.BOOL).syncWith(net.minecraft.network.codec.ByteBufCodecs.BOOL,AttachmentSyncPredicate.all()));
    public static final AttachmentType<Integer> DISPLAY_HALF=AttachmentRegistry.create(BuildupMobTweaks.id("door_rebuild_half"),b->b.initializer(()->0).syncWith(net.minecraft.network.codec.ByteBufCodecs.VAR_INT,AttachmentSyncPredicate.all()));
    public static final AttachmentType<Boolean> DISPLAY_LIVE=AttachmentRegistry.createDefaulted(BuildupMobTweaks.id("door_rebuild_display_live"),()->false);
    private static final AttachmentType<Runtime> RUNTIME=AttachmentRegistry.createDefaulted(BuildupMobTweaks.id("zombie_rebuild_runtime"),Runtime::new);
    public static boolean known(Zombie m){return m.hasAttached(DATA)&&m.getAttached(DATA).getIntOr("version",-1)==1;}
    public static Runtime runtime(Zombie m){return m.getAttachedOrCreate(RUNTIME);}
    public static final class Runtime {
        public boolean installed;public int blocks,breaks,guards,burrows,exits,unseen;public String mode="vanilla";
        public net.minecraft.world.entity.LivingEntity remembered;
        public final List<BlockDisplay> displays=new ArrayList<>();
    }
    private ZombieState(){}
}
