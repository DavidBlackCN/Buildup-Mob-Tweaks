package com.davidblackcn.buildupmobtweaks.rebuild.riding;
import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import net.fabricmc.fabric.api.attachment.v1.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
public final class RidingState {
    public static final AttachmentType<CompoundTag> DATA=AttachmentRegistry.createPersistent(BuildupMobTweaks.id("riding_rebuild"),CompoundTag.CODEC);
    public static final AttachmentType<ItemStack> RESERVE=AttachmentRegistry.createPersistent(BuildupMobTweaks.id("converted_skeleton_reserve"),ItemStack.OPTIONAL_CODEC);
    public static final AttachmentType<String> TRAP_ORIGIN=AttachmentRegistry.createPersistent(BuildupMobTweaks.id("trap_origin"),com.mojang.serialization.Codec.STRING);
    private static final AttachmentType<Runtime> RUNTIME=AttachmentRegistry.createDefaulted(BuildupMobTweaks.id("riding_runtime"),Runtime::new);
    public static boolean known(Mob m){return m.hasAttached(DATA)&&m.getAttached(DATA).getIntOr("version",-1)==1;}
    public static Runtime runtime(Mob m){return m.getAttachedOrCreate(RUNTIME);}
    public static final class Runtime {public boolean installed;public int pounces,charges,hits,circles,escapes,cancels;public String phase="idle";public RidingGoal action;public boolean hurtPending;public float hurtAmount;public long hurtExpires;}
    private RidingState(){}
}
