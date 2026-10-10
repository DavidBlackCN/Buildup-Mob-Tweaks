package com.davidblackcn.buildupmobtweaks.rebuild.piglin;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import net.fabricmc.fabric.api.attachment.v1.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public final class PiglinState {
    public static final AttachmentType<CompoundTag> DATA=AttachmentRegistry.createPersistent(BuildupMobTweaks.id("piglin_rebuild"),CompoundTag.CODEC);
    public static final AttachmentType<Boolean> COMPAT=AttachmentRegistry.create(BuildupMobTweaks.id("piglin_crossbow_visual"),b->b.initializer(()->false).syncWith(ByteBufCodecs.BOOL,AttachmentSyncPredicate.all()));
    private static final AttachmentType<Runtime> RUNTIME=AttachmentRegistry.createDefaulted(BuildupMobTweaks.id("piglin_runtime"),Runtime::new);
    public static void bootstrap(){}
    public static boolean known(Entity e){return e.hasAttached(DATA)&&e.getAttached(DATA).getIntOr("version",-1)==1;}
    public static Runtime runtime(Entity e){return e.getAttachedOrCreate(RUNTIME);}
    public static final class Runtime {
        public ItemStack charging,shotStack;
        public LivingEntity chargingTarget,dodgeTarget;
        public InteractionHand shotHand;
        public boolean shotCharged;
        public long dodgeUntil;
        public int shots,swings,dodges,cancels;
    }
    private PiglinState(){}
}
