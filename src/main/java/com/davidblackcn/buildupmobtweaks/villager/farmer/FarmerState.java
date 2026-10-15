package com.davidblackcn.buildupmobtweaks.villager.farmer;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import net.fabricmc.fabric.api.attachment.v1.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.core.BlockPos;
import java.util.*;

/** Inventory remains native; pending material reservation is a count, never another ItemStack. */
public final class FarmerState {
    public static final AttachmentType<CompoundTag> DATA=AttachmentRegistry.createPersistent(BuildupMobTweaks.id("farmer_work"),CompoundTag.CODEC);
    private static final AttachmentType<Runtime> RUNTIME=AttachmentRegistry.createDefaulted(BuildupMobTweaks.id("farmer_work_runtime"),Runtime::new);
    public static final class Runtime {
        BlockPos target,scanCenter;CropProfile crop;WalkTarget walk;PositionTracker look;
        UUID generation;long nextScan,deadline;int cursor;boolean active,done,fault;
        net.minecraft.world.level.pathfinder.Path path;FarmNavigation.Route route;boolean routeInvalidated;
        Identifier lastCrop;BlockPos lastCropPos;
        final Set<BlockPos> failed=new HashSet<>();
        public int scanned,harvests,replants,starts,stops,pathRequests,protectedTramples;
        public String reason="idle",landingDecision="unobserved";
    }
    public static Runtime runtime(Villager v){return v.getAttachedOrCreate(RUNTIME);}
    public static Optional<Runtime> peek(Villager v){return v.hasAttached(RUNTIME)?Optional.of(v.getAttached(RUNTIME)):Optional.empty();}
    public static boolean known(Villager v){
        if(!v.hasAttached(DATA))return true;var t=v.getAttached(DATA);if(t.getIntOr("version",-1)!=1||t.getIntOr("cooldown",-1)<0||t.getIntOr("cooldown",601)>600)return false;
        if(!t.contains("pos"))return t.keySet().equals(Set.of("version","cooldown"));
        return t.keySet().equals(Set.of("version","cooldown","pos","dimension","profile","signature","phase"))&&t.getLong("pos").isPresent()
                &&t.getString("dimension").filter(s->Identifier.tryParse(s)!=null).isPresent()&&t.getString("profile").filter(s->Identifier.tryParse(s)!=null).isPresent()
                &&t.getString("signature").isPresent()&&Set.of("prepared","harvested").contains(t.getStringOr("phase",""));
    }
    static CompoundTag data(Villager v){if(!v.hasAttached(DATA)){var t=new CompoundTag();t.putInt("version",1);t.putInt("cooldown",0);v.setAttached(DATA,t);}return v.getAttached(DATA);}
    public static boolean pending(Villager v){return v.hasAttached(DATA)&&v.getAttached(DATA).contains("pos");}
    static void pending(Villager v,BlockPos pos,CropProfile c,String phase){var t=data(v).copy();t.putLong("pos",pos.asLong());t.putString("dimension",v.level().dimension().identifier().toString());t.putString("profile",c.id().toString());t.putString("signature",c.signature());t.putString("phase",phase);v.setAttached(DATA,t);}
    static void clear(Villager v,int cooldown){var t=new CompoundTag();t.putInt("version",1);t.putInt("cooldown",cooldown);v.setAttached(DATA,t);}
    static void decrement(Villager v){if(v.hasAttached(DATA)&&known(v)){var t=v.getAttached(DATA);int n=t.getIntOr("cooldown",0);if(n>0){t=t.copy();t.putInt("cooldown",n-1);v.setAttached(DATA,t);}}}
    static void unload(Villager v){v.removeAttached(RUNTIME);}
    public static void bootstrap(){}
    private FarmerState(){}
}
