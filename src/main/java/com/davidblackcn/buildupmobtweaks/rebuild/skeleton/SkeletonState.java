package com.davidblackcn.buildupmobtweaks.rebuild.skeleton;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import net.fabricmc.fabric.api.attachment.v1.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.AreaEffectCloud;
import java.util.ArrayList;
import java.util.List;

/** RESERVE owns the actual unheld item, never a copy of an equipped weapon. */
public final class SkeletonState {
    public static final AttachmentType<CompoundTag> DATA=AttachmentRegistry.createPersistent(BuildupMobTweaks.id("skeleton_rebuild"),CompoundTag.CODEC);
    public static final AttachmentType<ItemStack> RESERVE=AttachmentRegistry.createPersistent(BuildupMobTweaks.id("skeleton_reserve"),ItemStack.OPTIONAL_CODEC);
    private static final AttachmentType<Runtime> RUNTIME=AttachmentRegistry.createDefaulted(BuildupMobTweaks.id("skeleton_runtime"),Runtime::new);
    public static Runtime runtime(AbstractSkeleton mob){return mob.getAttachedOrCreate(RUNTIME);}
    public static boolean known(AbstractSkeleton mob){return mob.hasAttached(DATA)&&mob.getAttached(DATA).getIntOr("version",-1)==1;}
    public static ItemStack reserve(AbstractSkeleton mob){var item=mob.getAttached(RESERVE);return item==null?ItemStack.EMPTY:item;}
    public static final class Runtime {
        public boolean installed,managed;
        public String mode="vanilla";
        public int swaps,shots,flips,dodges,snowballs,snowHits,shelters,repositions,exits,unseen;
        public LivingEntity remembered;
        final List<Goal> attacks=new ArrayList<>();
        public final List<AreaEffectCloud> clouds=new ArrayList<>();
    }
    private SkeletonState(){}
}
