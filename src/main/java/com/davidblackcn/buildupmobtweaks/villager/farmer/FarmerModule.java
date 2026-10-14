package com.davidblackcn.buildupmobtweaks.villager.farmer;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import com.davidblackcn.buildupmobtweaks.villager.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.villager.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import java.util.*;

/** Controller selection is independent of start eligibility: paused Buildup never races vanilla agriculture. */
public final class FarmerModule {
    public enum Owner { VANILLA, BUILDUP, EXTERNAL }
    static final Map<Villager,FarmerActivity> ACTIVE=new WeakHashMap<>();
    public static Owner owner(Villager v){
        if(!v.getVillagerData().profession().is(VillagerProfession.FARMER))return Owner.VANILLA;
        var c=BuildupMobTweaks.config();var tags=v.entityTags();
        if(!c.general.enabled.get()||tags.contains("buildupmobtweaks:vanilla_ai")||tags.contains("buildupmobtweaks:disable_villager_core")
                ||tags.contains("buildupmobtweaks:disable_farmer_intelligence")||tags.contains("buildupmobtweaks:disable_villager_profession/minecraft:farmer")
                ||v.getType().builtInRegistryHolder().is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE,BuildupMobTweaks.id("villager_core_excluded"))))return Owner.VANILLA;
        if(tags.contains("buildupmobtweaks:farmer_external")||c.villagers.farmer.external_control.get())return Owner.EXTERNAL;
        if(!c.general.enabled.get()||!c.villagers.enabled.get()||!c.villagers.schedule_coordination.get()||!c.villagers.farmer.enabled.get()
                ||tags.contains("buildupmobtweaks:vanilla_ai")||tags.contains("buildupmobtweaks:disable_villager_core")
                ||tags.contains("buildupmobtweaks:disable_villager_schedule_coordination")||tags.contains("buildupmobtweaks:disable_farmer_intelligence")
                ||tags.contains("buildupmobtweaks:disable_villager_profession/minecraft:farmer"))return Owner.VANILLA;
        // Entity type and Core policy exclusions must also apply to Farmer, including datapack tags.
        if(ControlPolicy.query(v,(ServerLevel)v.level(),ControlPolicy.Scope.WORK)==ControlPolicy.Decision.EXCLUDED)return Owner.VANILLA;
        return Owner.BUILDUP;
    }
    public static boolean eligible(Villager v,ServerLevel level){return owner(v)==Owner.BUILDUP&&FarmerState.known(v)
            &&ControlPolicy.query(v,level,ControlPolicy.Scope.WORK)==ControlPolicy.Decision.ALLOW&&!v.isBaby()
            &&v.getBrain().isActive(net.minecraft.world.entity.schedule.Activity.WORK)
            &&v.getBrain().hasMemoryValue(MemoryModuleType.JOB_SITE);}
    public static ProfessionProfile profile(){return new ProfessionProfile(BuildupMobTweaks.id("farmer"),VillagerProfession.FARMER,
            net.minecraft.world.entity.ai.village.poi.PoiTypes.FARMER,BuildupMobTweaks.config().villagers.farmer.radius.get()+2);}
    public static void register(){
        FarmerState.bootstrap();CropHandlers.bootstrap();CropProfiles.register();
        ServerEntityEvents.ENTITY_UNLOAD.register((e,l)->{if(e instanceof Villager v){stop(v);FarmerState.unload(v);}});
        net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.MOB_CONVERSION.register((before,after,keepEquipment)->{
            if(before.hasAttached(FarmerState.DATA))after.setAttached(FarmerState.DATA,before.getAttached(FarmerState.DATA).copy());
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(s->{ACTIVE.clear();FarmerActivity.clearClaims(s);InteractionPolicy.resetFailures();});
    }
    public static void boundary(Villager v,ServerLevel level){
        if(!eligible(v,level)){if(ACTIVE.containsKey(v))stop(v);return;}
        if(v.hasAttached(FarmerState.DATA))FarmerState.decrement(v);
    }
    public static void stop(Villager v){FarmerActivity.releaseClaims(v);var a=ACTIVE.remove(v);if(a!=null)a.stop(new WorkActivity.Context((ServerLevel)v.level(),v,profile()),WorkActivity.StopReason.INTERRUPTED);}
    public static void stopForReload(MinecraftServer server){for(var v:List.copyOf(ACTIVE.keySet()))if(v.level().getServer()==server)stop(v);}
    private FarmerModule(){}
}
