/* Bounded adaptation of ZombieAttackGoalMixin, SkeletonSpecificGoal and HuskSwitchControlsOnCamelHuskGoal (MIT). */
package com.davidblackcn.buildupmobtweaks.rebuild.riding;
import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.equine.SkeletonHorse;
import net.minecraft.world.entity.animal.camel.CamelHusk;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
public final class RidingGoal extends Goal {
    private final Mob rider;private final RidingBehavior behavior;private Mob mount;private LivingEntity target;
    private String kind;private int ticks;private Vec3 heading;private boolean hit,ownsStanding;
    RidingGoal(Mob m,RidingBehavior b){rider=m;behavior=b;}
    // Native ranged/weapon Goals keep their flags and perform their own attacks. Only this Goal writes a mounted impulse.
    private String kind(){var v=RidingBehavior.vehicle(rider);var d=rider.getAttached(RidingState.DATA);
        if(v instanceof Chicken c&&v.getType()==EntityTypes.CHICKEN&&c.isChickenJockey&&rider instanceof net.minecraft.world.entity.monster.zombie.Zombie z&&z.isBaby()&&d.getBooleanOr("chicken",false)&&behavior.enabled(rider,FeatureId.CHICKEN_JOCKEY_CHARGE)&&behavior.gate(v,FeatureId.CHICKEN_JOCKEY_CHARGE))return "chicken";
        if(v!=null&&v.getType()==EntityTypes.SKELETON_HORSE&&d.getBooleanOr("horse",false)&&behavior.enabled(rider,FeatureId.SKELETON_HORSE_CHARGE)&&behavior.gate(v,FeatureId.SKELETON_HORSE_CHARGE))return "horse";
        if(v!=null&&v.getType()==EntityTypes.CAMEL_HUSK&&rider.getType()==EntityTypes.HUSK&&!rider.isUsingItem()&&behavior.enabled(rider,FeatureId.HUSK_CAMEL_CIRCLE)&&behavior.gate(v,FeatureId.HUSK_CAMEL_CIRCLE))return "circle";return null;
    }
    public boolean permitted(){return RidingBehavior.valid(rider)&&RidingBehavior.vehicle(rider)==mount&&rider.getTarget()==target&&kind!=null&&kind.equals(kind());}
    public boolean active(){return mount!=null;}
    public boolean canUse(){if(!RidingBehavior.valid(rider)||!RidingState.known(rider))return false;kind=kind();return kind!=null;}
    public boolean canContinueToUse(){return permitted();}
    public boolean requiresUpdateEveryTick(){return true;}
    public void start(){mount=RidingBehavior.vehicle(rider);target=rider.getTarget();ticks=0;hit=false;heading=null;}
    public void tick(){if(!permitted()){cancel();return;}ticks++;advance();var r=RidingState.runtime(rider);long now=rider.level().getGameTime();var d=rider.getAttached(RidingState.DATA);
        if(kind.equals("circle")){r.phase="circle";Vec3 radial=target.position().subtract(mount.position()).multiply(1,0,1).normalize();Vec3 tangent=new Vec3(-radial.z,0,radial.x).scale((rider.getId()&1)==0?.16:-.16);
            if(RidingBehavior.safe(mount,mount.position().add(tangent.scale(4)))){rider.getNavigation().stop();mount.getNavigation().stop();mount.setDeltaMovement(tangent.add(0,mount.getDeltaMovement().y,0));mount.lookAt(target,30,30);r.circles++;}return;}
        String key=kind+"_ready";long ready=d.getLongOr(key,now+200);if(now<ready){if(heading==null){r.phase="cooldown";if(mount.onGround())mount.setSprinting(false);}return;}
        if(kind.equals("chicken")){if(!mount.onGround()||rider.distanceTo(target)>6)return;Vec3 v=target.position().subtract(mount.position()).multiply(1,0,1).normalize();
            if(!RidingBehavior.safe(mount,mount.position().add(v.scale(1.5))))return;d.putLong(key,now+100);mount.getNavigation().stop();mount.setDeltaMovement(v.scale(.45).add(0,.35,0));mount.setSprinting(true);r.pounces++;r.phase="pounce";return;}
        if(heading==null){if(!mount.onGround()||((SkeletonHorse)mount).isStanding()||rider.distanceTo(target)<2||rider.distanceTo(target)>12)return;heading=target.position().subtract(mount.position()).multiply(1,0,1).normalize();
            if(!RidingBehavior.safe(mount,mount.position().add(heading))) {heading=null;return;}ticks=0;hit=false;d.putLong(key,now+200);r.phase="warning";((SkeletonHorse)mount).setStanding(20);ownsStanding=true;return;}
    }
    /** Drive an already scheduled charge during its earned cooldown, without starting a second Goal/attack. */
    public void advance(){if(!permitted()||heading==null||!"horse".equals(kind))return;var r=RidingState.runtime(rider);
        if(ticks<20){rider.getNavigation().stop();mount.getNavigation().stop();r.phase="warning";return;}
        if(!hit&&mount.getBoundingBox().inflate(.5).intersects(target.getBoundingBox())){hit=true;if(target.hurtServer((ServerLevel)rider.level(),rider.damageSources().mobAttack(rider),(float)Math.min(4,rider.getAttributeValue(Attributes.ATTACK_DAMAGE)))){r.hits++;target.push(heading.scale(.4));}heading=null;mount.setSprinting(false);clearWarning();r.phase="cooldown";return;}
        if(ticks>=40||mount.horizontalCollision||!RidingBehavior.safe(mount,mount.position().add(heading))){heading=null;mount.setSprinting(false);clearWarning();r.phase="cooldown";return;}
        // In 26.3 setStanding(0) enables the flag with no expiry; clearStanding is the native exit.
        if(ticks==20){r.charges++;clearWarning();}r.phase="charge";rider.getNavigation().stop();mount.getNavigation().stop();mount.setSprinting(true);mount.setDeltaMovement(heading.scale(.6).add(0,mount.getDeltaMovement().y,0));
    }
    private void clearWarning(){if(ownsStanding&&mount instanceof SkeletonHorse h)h.clearStanding();ownsStanding=false;}
    public void cancel(){if(mount!=null){mount.setSprinting(false);mount.setXxa(0);clearWarning();RidingState.runtime(rider).cancels++;}mount=null;target=null;heading=null;RidingState.runtime(rider).phase="idle";}
    public void stop(){cancel();}
}
