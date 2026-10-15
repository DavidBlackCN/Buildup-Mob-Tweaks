package com.davidblackcn.buildupmobtweaks.villager.profession;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import com.davidblackcn.buildupmobtweaks.villager.*;
import com.davidblackcn.buildupmobtweaks.villager.farmer.FarmerModule;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.npc.villager.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.schedule.Activity;
import java.util.*;

public final class ProfessionModule {
    private static final Map<net.minecraft.server.MinecraftServer,Set<net.minecraft.resources.Identifier>> FAILED=new WeakHashMap<>();
    public static boolean failed(ServerLevel l,net.minecraft.resources.Identifier id){return FAILED.getOrDefault(l.getServer(),Set.of()).contains(id);}
    public static void pause(ServerLevel l,net.minecraft.resources.Identifier id,RuntimeException error){FAILED.computeIfAbsent(l.getServer(),k->new HashSet<>()).add(id);BuildupMobTweaks.LOGGER.error("Profession activity paused: {}",id,error);}
    private static final Map<UUID,Villager> CLAIMS=new HashMap<>();
    private static final class ScanBudget {long tick=-1;int left;}
    private static final Map<ServerLevel,ScanBudget> BUDGETS=new WeakHashMap<>();
    public static int scanBudget(ServerLevel l,int wanted){var b=BUDGETS.computeIfAbsent(l,k->new ScanBudget());if(b.tick!=l.getGameTime()){b.tick=l.getGameTime();b.left=BuildupMobTweaks.config().villagers.professions.world_scan_budget.get();}int n=Math.min(wanted,b.left);b.left-=n;return n;}
    public static boolean activityEnabled(net.minecraft.resources.Identifier id){
        var c=BuildupMobTweaks.config().villagers.professions;
        if(!id.getNamespace().equals(BuildupMobTweaks.MOD_ID))return true;
        return switch(id.getPath()){
            case "eat_meal"->c.self_heal.get();case "fish"->c.fishing.get();case "shear_sheep"->c.shearing.get();
            case "heal_villager"->c.cleric_healing.get();case "repair_golem"->c.golem_repair.get();case "feed_baby_animal"->c.baby_feeding.get();
            case "archery_training"->c.archery_training.get();case "display_frame"->c.decoration_frames.get();case "display_stand"->c.decoration_stands.get();case "display_block"->c.decoration_blocks.get();
            default->!id.getPath().endsWith("_work")||c.daily_routines.get();
        };
    }
    private static boolean professionEnabled(ProfessionProfile p){var c=BuildupMobTweaks.config().villagers.professions.profession_switches;return switch(p.profession().identifier().toString()){
        case "minecraft:armorer"->c.armorer.get();
        case "minecraft:butcher"->c.butcher.get();
        case "minecraft:cartographer"->c.cartographer.get();
        case "minecraft:cleric"->c.cleric.get();
        case "minecraft:farmer"->c.farmer.get();
        case "minecraft:fisherman"->c.fisherman.get();
        case "minecraft:fletcher"->c.fletcher.get();
        case "minecraft:leatherworker"->c.leatherworker.get();
        case "minecraft:librarian"->c.librarian.get();
        case "minecraft:mason"->c.mason.get();
        case "minecraft:shepherd"->c.shepherd.get();
        case "minecraft:toolsmith"->c.toolsmith.get();
        case "minecraft:weaponsmith"->c.weaponsmith.get();
        default->true;
    };}
    public static boolean enabled(Villager v,ServerLevel l){
        var c=BuildupMobTweaks.config().villagers.professions;var tags=v.entityTags();
        return c.enabled.get()&&!c.external_control.get()&&!tags.contains("buildupmobtweaks:profession_external")
                &&!tags.contains("buildupmobtweaks:disable_villager_work")&&ProfessionWorkState.known(v)
                &&ControlPolicy.query(v,l,ControlPolicy.Scope.WORK)==ControlPolicy.Decision.ALLOW&&!v.isBaby();
    }
    public static boolean allowed(Villager v,ServerLevel l,ProfessionProfile p,boolean meal){
        if(!enabled(v,l))return false;
        if(meal)return BuildupMobTweaks.config().villagers.professions.self_heal.get();
        if(!professionEnabled(p)||!v.getVillagerData().profession().is(p.profession())||!v.getBrain().isActive(Activity.WORK))return false;
        var site=v.getBrain().getMemory(MemoryModuleType.JOB_SITE);
        return site.isPresent()&&site.get().dimension()==l.dimension()&&l.hasChunkAt(site.get().pos())
                &&site.get().pos().closerToCenterThan(v.position(),p.workRadius())&&l.getPoiManager().existsAtPosition(p.workPoi(),site.get().pos());
    }
    public static boolean available(Villager v){return !ProfessionWorkState.busy(v)&&!FarmerModule.working(v);}
    public static boolean claimable(Villager v,Entity target){return !CLAIMS.containsKey(target.getUUID())||CLAIMS.get(target.getUUID())==v;}
    public static boolean claim(Villager v,Entity target){return CLAIMS.putIfAbsent(target.getUUID(),v)==null||CLAIMS.get(target.getUUID())==v;}
    public static boolean begin(WorkActivity.Context c,WorkActivity activity,boolean meal){
        var v=c.villager();if(!available(v)||!allowed(v,c.level(),c.profile(),meal)||!activityEnabled(activity.id())||v.entityTags().contains("buildupmobtweaks:disable_work/"+activity.id()))return false;
        var r=ProfessionWorkState.runtime(v);r.active=activity;r.context=c;r.brain=v.getBrain();r.meal=meal;r.starts++;r.reason=activity.id().toString();
        ProfessionWorkState.defer(v,activity.id(),BuildupMobTweaks.config().villagers.professions.action_cooldown.get());
        try{activity.start(c);return true;}catch(RuntimeException error){fault(v,error);return false;}
    }
    public static void tick(Villager v){
        var r=ProfessionWorkState.runtime(v);if(r.active==null)return;
        try{var result=r.active.tick(r.context);if(result!=WorkActivity.Result.CONTINUE){if(result==WorkActivity.Result.COMPLETE)r.completed++;stop(v,result==WorkActivity.Result.COMPLETE?WorkActivity.StopReason.COMPLETE:WorkActivity.StopReason.FAILED);}}
        catch(RuntimeException error){fault(v,error);}
    }
    public static void boundary(Villager v,ServerLevel l){
        OwnedWorkDisplays.boundary(v,l);
        ProfessionWorkState.peek(v).filter(r->r.active!=null).ifPresent(r->{
            if(r.brain!=v.getBrain()||!allowed(v,l,r.context.profile(),r.meal)
                    ||!activityEnabled(r.active.id())||v.entityTags().contains("buildupmobtweaks:disable_work/"+r.active.id())
                    ||r.adapter!=null&&(v.entityTags().contains("buildupmobtweaks:disable_adapter/"+r.adapter)||ProfessionRegistry.activities(r.context.profile().profession()).stream().noneMatch(b->b.adapter().equals(r.adapter)&&b.entry().id().equals(r.active.id()))))stop(v,WorkActivity.StopReason.INTERRUPTED);
        });
    }
    private static void fault(Villager v,RuntimeException error){var r=ProfessionWorkState.runtime(v);if(r.active!=null)pause((ServerLevel)v.level(),r.active.id(),error);stop(v,WorkActivity.StopReason.FAILED);ProfessionWorkState.runtime(v).reason="fault";}
    public static void stop(Villager v,WorkActivity.StopReason reason){
        var r=ProfessionWorkState.peek(v).orElse(null);if(r==null||r.active==null)return;
        var activity=r.active;var context=r.context;r.active=null;
        try{activity.stop(context,reason);}catch(RuntimeException error){pause(context.level(),activity.id(),error);}finally{
            var b=v.getBrain();if(r.walk!=null&&owns(b,MemoryModuleType.WALK_TARGET,r.walk)){v.getNavigation().stop();b.eraseMemory(MemoryModuleType.WALK_TARGET);}
            if(r.look!=null&&owns(b,MemoryModuleType.LOOK_TARGET,r.look))b.eraseMemory(MemoryModuleType.LOOK_TARGET);
            if(r.path!=null&&owns(b,MemoryModuleType.PATH,r.path))b.eraseMemory(MemoryModuleType.PATH);
            if(r.projectile!=null&&!r.projectile.isRemoved()&&WorkProjectiles.owned(v,r.projectile))r.projectile.discard();r.projectile=null;
            r.walk=null;r.look=null;r.path=null;r.context=null;r.brain=null;r.adapter=null;r.stops++;r.reason=reason.name();ProfessionWorkState.clearVisual(v);
            CLAIMS.entrySet().removeIf(e->e.getValue()==v);
        }
    }
    private static <T> boolean owns(net.minecraft.world.entity.ai.Brain<?> b,MemoryModuleType<T> type,Object value){return b.checkMemory(type,net.minecraft.world.entity.ai.memory.MemoryStatus.REGISTERED)&&b.getMemory(type).orElse(null)==value;}
    public static void register(){
        ProfessionWorkState.bootstrap();WorkProjectiles.bootstrap();OwnedWorkDisplays.bootstrap();BuiltinProfessions.register();
        ServerEntityEvents.ENTITY_UNLOAD.register((e,l)->{if(e instanceof Villager v){if(v.getRemovalReason()==Entity.RemovalReason.KILLED)OwnedWorkDisplays.retire(v,l);stop(v,WorkActivity.StopReason.UNLOADED);ProfessionWorkState.unload(v);}});
        net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.MOB_CONVERSION.register((before,after,keep)->{if(before instanceof Villager old)OwnedWorkDisplays.retire(old,(ServerLevel)old.level());if(before.hasAttached(ProfessionWorkState.DATA))after.setAttached(ProfessionWorkState.DATA,before.getAttached(ProfessionWorkState.DATA).copy());});
        ServerLifecycleEvents.SERVER_STOPPED.register(s->{CLAIMS.clear();FAILED.remove(s);BUDGETS.keySet().removeIf(l->l.getServer()==s);});
    }
    private ProfessionModule(){}
}
