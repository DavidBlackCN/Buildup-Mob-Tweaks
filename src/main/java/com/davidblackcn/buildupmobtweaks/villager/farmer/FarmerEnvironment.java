package com.davidblackcn.buildupmobtweaks.villager.farmer;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.Villager;

/** Preventing soil damage is independent of permission to begin a harvest or write movement. */
public final class FarmerEnvironment {
    public static boolean preventsTrampling(Entity entity){
        if(!(entity instanceof Villager v)||!(v.level() instanceof ServerLevel level)||FarmerModule.owner(v)!=FarmerModule.Owner.BUILDUP)return false;
        var s=FarmerState.runtime(v);
        var decision=com.davidblackcn.buildupmobtweaks.villager.ControlPolicy.query(v,level,com.davidblackcn.buildupmobtweaks.villager.ControlPolicy.Scope.WORK);
        s.landingDecision=decision.name()+"/work="+v.getBrain().isActive(net.minecraft.world.entity.schedule.Activity.WORK)+"/known="+FarmerState.known(v);
        // Trading, an activity transition or landing damage may pause work. Soil prevention remains
        // passive; it never resumes farming, changes a schedule, or authorizes a world operation.
        if(!FarmerState.known(v)||v.isBaby()||!v.isAlive()||v.isNoAi()||v.isPassenger()
                ||decision!=com.davidblackcn.buildupmobtweaks.villager.ControlPolicy.Decision.ALLOW
                &&decision!=com.davidblackcn.buildupmobtweaks.villager.ControlPolicy.Decision.VANILLA_PRIORITY)return false;
        s.protectedTramples++;return true;
    }
    public static boolean suppressesWorkJump(Entity entity){
        return entity instanceof Villager v&&v.level() instanceof ServerLevel l&&FarmerModule.eligible(v,l)&&FarmNavigation.ownsRoute(v);
    }
    private FarmerEnvironment(){}
}
