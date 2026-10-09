/* Mob AI Tweaks adaptation: Copyright (c) 2024 N0t_UN_Owen, MIT. See NOTICE.md. */
package com.davidblackcn.buildupmobtweaks.rebuild.remaining;
import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import com.davidblackcn.buildupmobtweaks.rebuild.zombie.ZombieBehavior;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.ai.goal.Goal;
import java.util.EnumSet;
/** A bounded jump/land/conceal escape, never removes the entity or grants invulnerability. */
final class SilverfishHide extends Goal {
    private final Silverfish m;private final RemainingBehavior behavior;private boolean left;
    SilverfishHide(Silverfish m,RemainingBehavior b){this.m=m;behavior=b;setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK,Flag.JUMP));}
    public boolean canUse(){long elapsed=m.tickCount-m.getLastHurtByMobTimestamp();return behavior.enabled(m,FeatureId.SILVERFISH_BURROW)&&!m.isNoAi()&&m.onGround()&&ZombieBehavior.valid(m,m.getLastHurtByMob())&&elapsed>20&&elapsed<220&&m.level().getGameTime()>=m.getAttached(RemainingState.DATA).getLongOr("hide_ready",0);}
    public void start(){var r=RemainingState.runtime(m);r.hidden=true;r.landed=false;r.start=m.level().getGameTime();r.end=r.start+Math.min(200,220-(m.tickCount-m.getLastHurtByMobTimestamp()));r.hides++;
        m.getAttached(RemainingState.DATA).putLong("hide_ready",r.end+100);left=m.getRandom().nextBoolean();m.getNavigation().stop();m.getJumpControl().jump();m.getMoveControl().strafe(1,0);}
    public boolean canContinueToUse(){var r=RemainingState.runtime(m);return r.hidden&&m.level().getGameTime()<r.end&&behavior.enabled(m,FeatureId.SILVERFISH_BURROW)&&m.isAlive()&&!m.isNoAi()&&ZombieBehavior.valid(m,m.getLastHurtByMob());}
    public boolean requiresUpdateEveryTick(){return true;}
    public void tick(){var r=RemainingState.runtime(m);if(!r.hidden||!ZombieBehavior.valid(m,m.getLastHurtByMob())){restore(m);return;}if(m.level().getGameTime()>r.start&&m.getDeltaMovement().y<0&&!m.onGround())r.landed=true;
        if(r.landed&&m.onGround()){var d=m.getAttached(RemainingState.DATA);if(!d.getBooleanOr("hide_owned",false)){d.putBoolean("hide_owned",true);d.putBoolean("old_invisible",m.isInvisible());d.putBoolean("old_sprinting",m.isSprinting());}
            m.setInvisible(true);m.setSprinting(true);m.getNavigation().stop();m.lookAt(m.getLastHurtByMob(),30,30);m.getMoveControl().strafe(0,left?-1:1);}
    }
    public void stop(){restore(m);m.getMoveControl().setWait();m.getNavigation().stop();}
    static void restore(Silverfish m){var d=m.getAttached(RemainingState.DATA);if(d!=null&&d.getIntOr("version",-1)==1&&d.getBooleanOr("hide_owned",false)){
            if(m.isInvisible())m.setInvisible(d.getBooleanOr("old_invisible",false));if(m.isSprinting())m.setSprinting(d.getBooleanOr("old_sprinting",false));d.putBoolean("hide_owned",false);}
        RemainingState.runtime(m).hidden=false;RemainingState.runtime(m).end=0;}
}
