/* Mob AI Tweaks adaptation: Copyright (c) 2024 N0t_UN_Owen, MIT. See NOTICE.md. */
package com.davidblackcn.buildupmobtweaks.rebuild.vex;
import com.davidblackcn.buildupmobtweaks.feature.*;
import com.davidblackcn.buildupmobtweaks.rebuild.raid.*;
import com.davidblackcn.buildupmobtweaks.rebuild.zombie.ZombieBehavior;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.phys.Vec3;

public final class VexBehavior {
    private static VexBehavior instance;private final FeatureRegistry features;
    public VexBehavior(FeatureRegistry f){features=f;}public static VexBehavior instance(){return instance;}
    public boolean enabled(Vex m,FeatureId id){return RaidState.known(m,RaidState.VEX)&&RaidGate.enabled(m,features,id);}
    public void register(){instance=this;ServerEntityEvents.ENTITY_LOAD.register((e,l)->{if(e instanceof Vex m&&RaidGate.supported(m)){RaidState.initialize(m,RaidState.VEX);if(RaidState.known(m,RaidState.VEX)){m.setIsCharging(false);}}});
        ServerEntityEvents.ENTITY_UNLOAD.register((e,l)->{if(e instanceof Vex m&&RaidState.known(m,RaidState.VEX))finish(m);});}
    public boolean canCharge(Vex m){if(!RaidState.known(m,RaidState.VEX))return true;
        boolean any=enabled(m,FeatureId.VEX_FIXED_CHARGE)||enabled(m,FeatureId.VEX_RECOVERY_PAUSE)||enabled(m,FeatureId.VEX_CLOSE_RANGE_GUARD);
        return !any||ZombieBehavior.valid(m,m.getTarget())&&(!enabled(m,FeatureId.VEX_CLOSE_RANGE_GUARD)||m.distanceToSqr(m.getTarget())>=Math.pow(features.vexMinimumDistance(),2))
            &&(!enabled(m,FeatureId.VEX_RECOVERY_PAUSE)||m.level().getGameTime()>=m.getAttached(RaidState.VEX).getLongOr("charge_ready",0));}
    public void charging(Vex m,boolean start){if(!RaidState.known(m,RaidState.VEX))return;var r=RaidState.runtime(m);
        if(start&&enabled(m,FeatureId.VEX_FIXED_CHARGE)&&ZombieBehavior.valid(m,m.getTarget())){var delta=m.getTarget().getEyePosition().subtract(m.getEyePosition());
            r.charging=true;r.target=m.getTarget();r.direction=delta.normalize();r.start=m.level().getGameTime();r.end=r.start+Math.min(40,Math.max(1,(int)(delta.length()*1.5)));r.charges++;
            // An absolute endpoint keeps native hasWanted/canContinue semantics; direction is never treated as coordinates.
            var end=m.position().add(r.direction.scale(60));m.getMoveControl().setWantedPosition(end.x,end.y,end.z,1);
        }else if(!start&&r.charging){r.charging=false;r.target=null;r.direction=Vec3.ZERO;m.setDeltaMovement(Vec3.ZERO);m.getMoveControl().setWantedPosition(m.getX(),m.getY(),m.getZ(),0);
            if(enabled(m,FeatureId.VEX_RECOVERY_PAUSE))m.getAttached(RaidState.VEX).putLong("charge_ready",m.level().getGameTime()+features.vexRecoveryTicks());}
        else if(!start&&enabled(m,FeatureId.VEX_RECOVERY_PAUSE)&&m.isCharging())m.getAttached(RaidState.VEX).putLong("charge_ready",m.level().getGameTime()+features.vexRecoveryTicks());
    }
    public boolean move(Vex m){if(!RaidState.known(m,RaidState.VEX))return false;
        if(enabled(m,FeatureId.VEX_RECOVERY_PAUSE)&&m.level().getGameTime()<m.getAttached(RaidState.VEX).getLongOr("charge_ready",0)){m.setDeltaMovement(Vec3.ZERO);return true;}
        if(!RaidState.runtime(m).charging)return false;var r=RaidState.runtime(m);
        m.setDeltaMovement(r.direction);m.setYRot((float)(-Math.atan2(r.direction.x,r.direction.z)*180/Math.PI));m.yBodyRot=m.getYRot();return true;}
    public void tick(Vex m){if(!RaidState.known(m,RaidState.VEX))return;var r=RaidState.runtime(m);
        boolean any=enabled(m,FeatureId.VEX_FIXED_CHARGE)||enabled(m,FeatureId.VEX_RECOVERY_PAUSE)||enabled(m,FeatureId.VEX_CLOSE_RANGE_GUARD);
        if(any&&m.getTarget()!=null&&!ZombieBehavior.valid(m,m.getTarget())){m.setTarget(null);finish(m);}
        if(r.charging&&(!enabled(m,FeatureId.VEX_FIXED_CHARGE)||m.isNoAi()||!m.isAlive()||m.getTarget()!=r.target||!ZombieBehavior.valid(m,r.target)||m.level().getGameTime()>=r.end))finish(m);
    }
    private void finish(Vex m){if(RaidState.runtime(m).charging)m.setIsCharging(false);}
}
