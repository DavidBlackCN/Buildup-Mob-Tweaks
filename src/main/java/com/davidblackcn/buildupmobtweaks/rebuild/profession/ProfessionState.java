/* Mob AI Tweaks adaptation: Copyright (c) 2024 N0t_UN_Owen, MIT. See NOTICE.md. */
package com.davidblackcn.buildupmobtweaks.rebuild.profession;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import net.fabricmc.fabric.api.attachment.v1.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;

public final class ProfessionState {
    public static final AttachmentType<CompoundTag> DATA=AttachmentRegistry.createPersistent(BuildupMobTweaks.id("profession_rebuild"),CompoundTag.CODEC);
    public static final AttachmentType<CompoundTag> POTION=AttachmentRegistry.createPersistent(BuildupMobTweaks.id("cleric_potion"),CompoundTag.CODEC);
    public static final AttachmentType<Boolean> CROSSBOW_VISUAL=AttachmentRegistry.create(BuildupMobTweaks.id("profession_crossbow_visual"),b->b.initializer(()->false).syncWith(ByteBufCodecs.BOOL,AttachmentSyncPredicate.all()));
    public static final AttachmentType<Boolean> ROD_VISUAL=AttachmentRegistry.create(BuildupMobTweaks.id("profession_rod_visual"),b->b.initializer(()->false).syncWith(ByteBufCodecs.BOOL,AttachmentSyncPredicate.all()));
    public static final AttachmentType<Boolean> HOOK=AttachmentRegistry.create(BuildupMobTweaks.id("profession_hook_v1"),b->b.initializer(()->false).syncWith(ByteBufCodecs.BOOL,AttachmentSyncPredicate.all()));
    private static final AttachmentType<Runtime> RUNTIME=AttachmentRegistry.createDefaulted(BuildupMobTweaks.id("profession_runtime"),Runtime::new);
    public static void bootstrap(){}
    public static boolean known(Entity m){return known(m.getAttached(DATA));}
    public static boolean known(CompoundTag d){return d!=null&&d.getIntOr("version",-1)==1;}
    public static Runtime runtime(Entity m){return m.getAttachedOrCreate(RUNTIME);}
    public static final class Runtime {
        public boolean installed;
        public String phase="idle";
        public FeatureId active;
        public ItemStack stack;
        public InteractionHand hand;
        public LivingEntity target;
        public FishingHook hook;
        public int shots,casts,pulls,potions,cancels;
    }
    private ProfessionState(){}
}
