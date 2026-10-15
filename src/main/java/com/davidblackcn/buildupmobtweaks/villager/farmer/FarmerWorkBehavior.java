package com.davidblackcn.buildupmobtweaks.villager.farmer;

import com.davidblackcn.buildupmobtweaks.villager.*;
import net.minecraft.world.entity.ai.behavior.HarvestFarmland;
import net.minecraft.world.entity.ai.memory.*;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.server.level.ServerLevel;
import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;

/** Replaces only the HarvestFarmland choice in native RunOne; disabled mode delegates unchanged. */
public final class FarmerWorkBehavior extends HarvestFarmland {
    private final FarmerActivity activity=new FarmerActivity();private boolean custom;private WorkActivity.Context context;
    protected boolean hasRequiredMemories(Villager v){
        if(FarmerModule.owner(v)==FarmerModule.Owner.VANILLA)return super.hasRequiredMemories(v);
        return v.getBrain().checkMemory(MemoryModuleType.WALK_TARGET,MemoryStatus.VALUE_ABSENT)
                &&v.getBrain().checkMemory(MemoryModuleType.LOOK_TARGET,MemoryStatus.REGISTERED)&&v.getBrain().checkMemory(MemoryModuleType.JOB_SITE,MemoryStatus.VALUE_PRESENT);
    }
    protected boolean checkExtraStartConditions(ServerLevel l,Villager v){
        if(com.davidblackcn.buildupmobtweaks.villager.profession.ProfessionWorkState.busy(v))return false;
        var owner=FarmerModule.owner(v);if(owner==FarmerModule.Owner.VANILLA)return super.checkExtraStartConditions(l,v);
        if(owner!=FarmerModule.Owner.BUILDUP)return false;
        context=new WorkActivity.Context(l,v,FarmerModule.profile());
        try{return activity.canStart(context);}catch(RuntimeException error){FarmerState.runtime(v).fault=true;BuildupMobTweaks.LOGGER.error("Farmer selection paused for {}",v.getUUID(),error);return false;}
    }
    protected void start(ServerLevel l,Villager v,long time){custom=FarmerModule.owner(v)==FarmerModule.Owner.BUILDUP;if(custom)activity.start(context);else super.start(l,v,time);}
    protected boolean timedOut(long time){return custom?false:super.timedOut(time);}
    protected boolean canStillUse(ServerLevel l,Villager v,long time){return custom?FarmerModule.eligible(v,l)&&FarmerState.peek(v).map(s->s.active&&!s.done).orElse(false):FarmerModule.owner(v)==FarmerModule.Owner.VANILLA&&super.canStillUse(l,v,time);}
    protected void tick(ServerLevel l,Villager v,long time){
        if(!custom){super.tick(l,v,time);return;}
        try{var result=activity.tick(context);if(result!=WorkActivity.Result.CONTINUE)doStop(l,v,time);}
        catch(RuntimeException error){FarmerState.runtime(v).fault=true;BuildupMobTweaks.LOGGER.error("Farmer activity paused for {}",v.getUUID(),error);doStop(l,v,time);}
    }
    protected void stop(ServerLevel l,Villager v,long time){if(custom)activity.stop(context,FarmerState.peek(v).map(s->s.done).orElse(false)?WorkActivity.StopReason.COMPLETE:WorkActivity.StopReason.INTERRUPTED);else super.stop(l,v,time);custom=false;context=null;}
}
