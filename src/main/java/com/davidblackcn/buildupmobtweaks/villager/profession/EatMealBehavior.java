package com.davidblackcn.buildupmobtweaks.villager.profession;
import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import com.davidblackcn.buildupmobtweaks.villager.*;
import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.*;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;

public final class EatMealBehavior extends Behavior<Villager> {
    private EatMeal meal;private WorkActivity.Context context;
    public EatMealBehavior(){super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET,MemoryStatus.REGISTERED),80);}
    protected boolean checkExtraStartConditions(ServerLevel l,Villager v){
        if(!ProfessionModule.enabled(v,l)||!BuildupMobTweaks.config().villagers.professions.self_heal.get()||!ProfessionModule.available(v))return false;
        meal=new EatMeal();if(ProfessionModule.failed(l,meal.id())||ProfessionWorkState.now(v)<ProfessionWorkState.ready(v,meal.id()))return false;
        var key=v.getVillagerData().profession().unwrapKey();if(key.isEmpty())return false;
        context=new WorkActivity.Context(l,v,new ProfessionProfile(BuildupMobTweaks.id("common"),key.get(),PoiTypes.HOME,12));return meal.canStart(context);
    }
    protected void start(ServerLevel l,Villager v,long time){ProfessionModule.begin(context,meal,true);}
    protected boolean canStillUse(ServerLevel l,Villager v,long time){return ProfessionWorkState.peek(v).map(r->r.active==meal).orElse(false)&&ProfessionModule.allowed(v,l,context.profile(),true);}
    protected void tick(ServerLevel l,Villager v,long time){ProfessionModule.tick(v);}
    protected void stop(ServerLevel l,Villager v,long time){if(ProfessionWorkState.peek(v).map(r->r.active==meal).orElse(false))ProfessionModule.stop(v,WorkActivity.StopReason.INTERRUPTED);meal=null;context=null;}
}
