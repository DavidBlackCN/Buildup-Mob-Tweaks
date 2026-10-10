/* Mob AI Tweaks behavioral adaptation, Copyright (c) 2024 N0t_UN_Owen, MIT. See NOTICE.md. */
package com.davidblackcn.buildupmobtweaks.rebuild.illusion;
import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import net.fabricmc.fabric.api.attachment.v1.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.entity.*;
import net.minecraft.world.phys.Vec3;
public final class IllusionState {
    public static final AttachmentType<CompoundTag> DATA=AttachmentRegistry.createPersistent(BuildupMobTweaks.id("illusion_rebuild"),CompoundTag.CODEC);
    public static final AttachmentType<CompoundTag> ARROW=AttachmentRegistry.createPersistent(BuildupMobTweaks.id("illusion_arrow"),CompoundTag.CODEC);
    public static final AttachmentType<CompoundTag> VISUAL=AttachmentRegistry.create(BuildupMobTweaks.id("illusion_visual"),b->b.initializer(CompoundTag::new).syncWith(ByteBufCodecs.COMPOUND_TAG,AttachmentSyncPredicate.all()));
    private static final AttachmentType<Runtime> RUNTIME=AttachmentRegistry.createDefaulted(BuildupMobTweaks.id("illusion_runtime"),Runtime::new);
    public static void bootstrap(){}
    public static boolean known(Entity m){var d=m.getAttached(DATA);return d!=null&&d.getIntOr("version",-1)==1;}
    public static Runtime runtime(Entity m){return m.getAttachedOrCreate(RUNTIME);}
    public static final class Runtime {
        public boolean installed,visible; public long lastRefresh; public LivingEntity supportTarget;
        public Vec3[] positions=new Vec3[0]; public Vec3 anchor; public int swaps,volleys,arrows,shots,supports;
    }
    private IllusionState(){}
}
