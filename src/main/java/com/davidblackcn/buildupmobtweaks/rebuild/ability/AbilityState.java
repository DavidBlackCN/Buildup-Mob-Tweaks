package com.davidblackcn.buildupmobtweaks.rebuild.ability;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import net.fabricmc.fabric.api.attachment.v1.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.Goal;
import java.util.*;

/** Birth choices/deadlines persist; preparation and entity references never do. */
public final class AbilityState {
    public static final AttachmentType<CompoundTag> DATA=AttachmentRegistry.createPersistent(BuildupMobTweaks.id("ability_rebuild"),CompoundTag.CODEC);
    public static final AttachmentType<Integer> THROW=AttachmentRegistry.create(BuildupMobTweaks.id("snow_throw"),b->b.initializer(()->0).syncWith(ByteBufCodecs.VAR_INT,AttachmentSyncPredicate.all()));
    public static final AttachmentType<Boolean> LEFT=AttachmentRegistry.create(BuildupMobTweaks.id("snow_throw_left"),b->b.initializer(()->false).syncWith(ByteBufCodecs.BOOL,AttachmentSyncPredicate.all()));
    public static final AttachmentType<Boolean> PUMPKIN_RETURN=AttachmentRegistry.create(BuildupMobTweaks.id("snow_pumpkin_interaction"),b->b.initializer(()->false).syncWith(ByteBufCodecs.BOOL,AttachmentSyncPredicate.all()));
    private static final AttachmentType<Runtime> RUNTIME=AttachmentRegistry.createDefaulted(BuildupMobTweaks.id("ability_runtime"),Runtime::new);
    public static boolean known(Entity e){return e.hasAttached(DATA)&&e.getAttached(DATA).getIntOr("version",-1)==1;}
    public static boolean initialize(Entity e){if(!e.hasAttached(DATA)){var d=new CompoundTag();d.putInt("version",1);e.setAttached(DATA,d);}return known(e);}
    public static Runtime runtime(Entity e){return e.getAttachedOrCreate(RUNTIME);}
    public static final class Runtime {
        public Goal original,replacement;
        public boolean installed,managed;
        public LivingEntity target,parent;
        public long release;
        public boolean ranged,visual;
        public int prepared,released,cancelled,volleys,extra,hits,scans;
        public final Set<Entity> extras=Collections.newSetFromMap(new IdentityHashMap<>());
    }
    private AbilityState(){}
}
