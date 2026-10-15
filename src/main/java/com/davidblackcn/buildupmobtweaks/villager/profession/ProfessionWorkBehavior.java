package com.davidblackcn.buildupmobtweaks.villager.profession;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import com.davidblackcn.buildupmobtweaks.villager.*;
import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.*;
import net.minecraft.world.entity.npc.villager.Villager;

/** Selected by the existing native work choice. It never replaces the Brain or work package. */
public final class ProfessionWorkBehavior extends Behavior<Villager> {
    private net.minecraft.resources.Identifier adapter;private WorkActivity candidate;private WorkActivity.Context context;
    public ProfessionWorkBehavior(){super(ImmutableMap.of(MemoryModuleType.JOB_SITE,MemoryStatus.VALUE_PRESENT,MemoryModuleType.WALK_TARGET,MemoryStatus.VALUE_ABSENT,MemoryModuleType.LOOK_TARGET,MemoryStatus.REGISTERED),1200);}
    @Override protected boolean checkExtraStartConditions(ServerLevel l,Villager v){
        if(!ProfessionModule.enabled(v,l)||!ProfessionModule.available(v))return false;
        var r=ProfessionWorkState.runtime(v);long now=ProfessionWorkState.now(v);if(!r.scanInitialized){r.scanInitialized=true;r.nextScan=now+Math.floorMod(v.getUUID().hashCode(),BuildupMobTweaks.config().villagers.professions.scan_interval.get());}if(now<r.nextScan)return false;
        r.nextScan=now+BuildupMobTweaks.config().villagers.professions.scan_interval.get();r.scans++;
        var key=v.getVillagerData().profession().unwrapKey();if(key.isEmpty())return false;
        for(var binding:ProfessionRegistry.activities(key.get())){
            if(!ProfessionModule.activityEnabled(binding.entry().id())||v.entityTags().contains("buildupmobtweaks:disable_adapter/"+binding.adapter())||v.entityTags().contains("buildupmobtweaks:disable_work/"+binding.entry().id())||ProfessionModule.failed(l,binding.entry().id())||now<ProfessionWorkState.ready(v,binding.entry().id()))continue;
            if(!ProfessionModule.allowed(v,l,binding.profile(),false))continue;var c=ScheduleCoordination.nativeWorkSlot(v,l,binding.profile());if(c.isEmpty())continue;
            try{var activity=binding.entry().factory().get();if(!activity.id().equals(binding.entry().id())||activity.interactionTier()<0||activity.interactionTier()>3)throw new IllegalArgumentException("Invalid profession activity declaration");if(activity.canStart(c.get())){adapter=binding.adapter();candidate=activity;context=c.get();return true;}}catch(RuntimeException error){ProfessionModule.pause(l,binding.entry().id(),error);}
        }
        return false;
    }
    @Override protected void start(ServerLevel l,Villager v,long time){ProfessionWorkState.runtime(v).adapter=adapter;ProfessionModule.begin(context,candidate,false);}
    @Override protected boolean canStillUse(ServerLevel l,Villager v,long time){return ProfessionWorkState.peek(v).map(r->r.active==candidate).orElse(false)&&ProfessionModule.allowed(v,l,context.profile(),false);}
    @Override protected void tick(ServerLevel l,Villager v,long time){ProfessionModule.tick(v);}
    @Override protected void stop(ServerLevel l,Villager v,long time){if(ProfessionWorkState.peek(v).map(r->r.active==candidate).orElse(false))ProfessionModule.stop(v,WorkActivity.StopReason.INTERRUPTED);candidate=null;context=null;}
}
