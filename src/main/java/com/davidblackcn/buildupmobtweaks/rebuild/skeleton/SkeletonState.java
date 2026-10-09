package com.davidblackcn.buildupmobtweaks.rebuild.skeleton;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import net.fabricmc.fabric.api.attachment.v1.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
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
    private static final String GENERATED_SWORD="buildupmobtweaks:generated_skeleton_sword";
    public static ItemStack generatedSword(){var sword=new ItemStack(Items.WOODEN_SWORD);markGeneratedSword(sword);return sword;}
    private static void markGeneratedSword(ItemStack sword){CustomData.update(DataComponents.CUSTOM_DATA,sword,tag->tag.putBoolean(GENERATED_SWORD,true));}
    public static boolean generatedSword(ItemStack sword){return sword.is(Items.WOODEN_SWORD)
            &&sword.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getBooleanOr(GENERATED_SWORD,false);}
    /** The old v1 implementation only generated a pristine default sword beside its bow. */
    public static void migrateGeneratedSword(AbstractSkeleton mob){
        if(!known(mob))return;var data=mob.getAttached(DATA);if(data.getIntOr("generated_sword_policy",0)!=0)return;
        var held=mob.getMainHandItem();var reserve=reserve(mob);var plain=new ItemStack(Items.WOODEN_SWORD);
        if(held.getItem() instanceof net.minecraft.world.item.BowItem&&ItemStack.matches(reserve,plain))markGeneratedSword(reserve);
        if(reserve.getItem() instanceof net.minecraft.world.item.BowItem&&ItemStack.matches(held,plain))markGeneratedSword(held);
        data.putInt("generated_sword_policy",1);
    }
    public static void discardGeneratedSword(AbstractSkeleton mob){
        if(generatedSword(reserve(mob)))mob.removeAttached(RESERVE);
        if(generatedSword(mob.getMainHandItem()))mob.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND,ItemStack.EMPTY);
    }
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
