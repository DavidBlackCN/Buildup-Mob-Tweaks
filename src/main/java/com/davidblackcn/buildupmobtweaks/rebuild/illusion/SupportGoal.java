package com.davidblackcn.buildupmobtweaks.rebuild.illusion;
import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.illager.Vindicator;
import java.util.EnumSet;
/** Lower priority than every native target goal; owns only the target it supplied. */
public final class SupportGoal extends Goal {
    private final Vindicator mob; private final IllusionBehavior behavior; private LivingEntity candidate; private Mob donor;
    public SupportGoal(Vindicator m,IllusionBehavior b){mob=m;behavior=b;setFlags(EnumSet.of(Flag.TARGET));}
    private boolean allowed(){return mob.isAlive()&&!mob.isNoAi()&&behavior.enabled(mob,FeatureId.VINDICATOR_SUPPORT);}
    public boolean canUse(){if(!allowed()||IllusionBehavior.valid(mob,mob.getTarget())&&com.davidblackcn.buildupmobtweaks.rebuild.relations.RelationBehavior.state(mob).owned!=mob.getTarget()||!IllusionBehavior.ready(mob,"support_ready"))return false;IllusionBehavior.reserve(mob,"support_ready",20);
        var nearby=new java.util.ArrayList<Mob>();mob.level().getEntities(net.minecraft.world.level.entity.EntityTypeTest.forClass(Mob.class),mob.getBoundingBox().inflate(8),o->o.isAlive()&&mob.distanceToSqr(o)<=64&&(o.getType()==EntityTypes.EVOKER||o.getType()==EntityTypes.WITCH)&&mob.isAlliedTo(o),nearby,8);
        for(var other:nearby){var t=other.getLastHurtByMob();int ago=other.tickCount-other.getLastHurtByMobTimestamp();if(other.getLastHurtByMobTimestamp()>0&&ago>=0&&ago<=100&&IllusionBehavior.valid(mob,t)&&!other.isAlliedTo(t)&&mob.distanceToSqr(t)<=256&&mob.hasLineOfSight(t)){donor=other;candidate=t;return true;}}return false;}
    public boolean canContinueToUse(){return allowed()&&mob.getTarget()==candidate&&IllusionBehavior.valid(mob,candidate)&&donor!=null&&donor.isAlive()&&mob.isAlliedTo(donor)&&mob.distanceToSqr(donor)<=64&&mob.distanceToSqr(candidate)<=256&&mob.hasLineOfSight(candidate);}
    public void start(){mob.setTarget(candidate);IllusionState.runtime(mob).supportTarget=candidate;IllusionState.runtime(mob).supports++;}
    public void stop(){if(mob.getTarget()==candidate)mob.setTarget(null);if(IllusionState.runtime(mob).supportTarget==candidate)IllusionState.runtime(mob).supportTarget=null;candidate=null;donor=null;}
}
