package com.davidblackcn.buildupmobtweaks.combat;
import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import java.util.EnumSet;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.camel.CamelHusk;
import net.minecraft.world.entity.monster.cubemob.Slime;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.Vec3;

public final class RidingCombat {
    private RidingCombat() {}
    private static boolean active(Mob mob,FeatureId id){return AdvancedHostiles.instance().active(mob,id);}
    public static void install(Mob mob){
        if(mob instanceof Zombie || mob instanceof AbstractSkeleton)mob.getGoalSelector().addGoal(0,chargeGoal(mob));
        if(mob.getType()==EntityTypes.ZOMBIE)mob.getGoalSelector().addGoal(1,slimeGoal(mob));
        if(!(mob instanceof Zombie) && !(mob instanceof AbstractSkeleton))return;
        var data=mob.getAttached(AdvancedHostiles.DATA);
        if(data==null || data.getIntOr("version",-1)!=1 || data.getBooleanOr("companion_checked",false))return;
        data=data.copy();data.putBoolean("companion_checked",true);mob.setAttached(AdvancedHostiles.DATA,data);
        if(!data.getStringOr("origin","").equals("rolled") || mob.isNoAi() || mob.isPassenger() || mob.isVehicle())return;
        EntityType<? extends Mob> type=active(mob,FeatureId.ZOMBIE_HORSE_LEADER)?EntityTypes.ZOMBIE_HORSE
                :active(mob,FeatureId.SKELETON_HORSE_CHARGE)?EntityTypes.SKELETON_HORSE
                :active(mob,FeatureId.ZOMBIE_SLIME_CARRIER)?EntityTypes.SLIME:null;
        if(type==null || mob.isInWater())return;
        var other=type.create(mob.level(),EntitySpawnReason.MOB_SUMMONED);if(other==null)return;
        other.snapTo(mob.position(),mob.getYRot(),0);
        if(other instanceof Slime slime)slime.setSize(2,true);
        if(!mob.level().noCollision(other,other.getBoundingBox()))return;
        if(!mob.level().addFreshEntity(other))return;
        boolean mounted=other instanceof Slime?other.startRiding(mob):mob.startRiding(other);
        if(!mounted){other.discard();return;}
        data.putString("companion",other.getStringUUID());
        if(active(mob,FeatureId.ZOMBIE_HORSE_LEADER)&&mob.getItemBySlot(EquipmentSlot.HEAD).isEmpty()){
            mob.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.BANNER.red()));mob.setDropChance(EquipmentSlot.HEAD,0);
        }
    }
    public static void tick(Mob mob){
        if(mob.getType()==EntityTypes.HUSK && mob.getVehicle() instanceof CamelHusk camel && EnvironmentCombat.on(mob,FeatureId.HUSK_CAMEL_CIRCLE)
                && SkeletonCombat.validTarget(mob,mob.getTarget()) && (mob.tickCount+mob.getId())%20==0){
            var target=mob.getTarget();var delta=camel.position().subtract(target.position()).multiply(1,0,1).normalize();
            var point=target.position().add(delta.scale(5)).add(-delta.z*2,0,delta.x*2);
            var side=point.subtract(camel.position());if(SkeletonCombat.safeStep(camel,side.x,side.z))camel.getNavigation().moveTo(point.x,point.y,point.z,1);
            for(var passenger:camel.getPassengers())if(passenger instanceof AbstractSkeleton skeleton && skeleton.getType()==EntityTypes.PARCHED
                    && SkeletonCombat.validTarget(skeleton,target)&&skeleton.getTarget()==null)skeleton.setTarget(target);
        }
    }
    public static Goal slimeGoal(Mob mob){return new SlimeThrowGoal(mob);}
    private static final class SlimeThrowGoal extends Goal {
        private final Mob mob;private Slime slime;private LivingEntity target;private int ticks;
        SlimeThrowGoal(Mob mob){this.mob=mob;setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
        @Override public boolean canUse(){
            if(!active(mob,FeatureId.ZOMBIE_SLIME_CARRIER)||!SkeletonCombat.validTarget(mob,mob.getTarget())||!mob.hasLineOfSight(mob.getTarget())
                    ||mob.distanceToSqr(mob.getTarget())<=16||mob.distanceToSqr(mob.getTarget())>=64||!HostileCombat.instance().ready(mob,"slime_throw"))return false;
            for(var passenger:mob.getPassengers())if(passenger instanceof Slime s&&s.getStringUUID().equals(mob.getAttached(AdvancedHostiles.DATA).getStringOr("companion",""))){slime=s;return true;}
            return false;
        }
        @Override public void start(){target=mob.getTarget();ticks=0;HostileCombat.instance().reserve(mob,"slime_throw",400);}
        @Override public boolean canContinueToUse(){return ticks<20&&active(mob,FeatureId.ZOMBIE_SLIME_CARRIER)&&slime!=null&&slime.isAlive()&&slime.getVehicle()==mob
                &&target==mob.getTarget()&&SkeletonCombat.validTarget(mob,target)&&mob.hasLineOfSight(target)&&mob.distanceToSqr(target)<100;}
        @Override public boolean requiresUpdateEveryTick(){return true;}
        @Override public void tick(){if(!canContinueToUse())return;ticks++;mob.getNavigation().stop();mob.getLookControl().setLookAt(target,30,30);
            if(ticks%4==0)((ServerLevel)mob.level()).sendParticles(ParticleTypes.CRIT,mob.getX(),mob.getEyeY(),mob.getZ(),3,.3,.3,.3,0);
            if(ticks==20){slime.stopRiding();var delta=target.position().subtract(mob.position()).multiply(1,0,1).normalize();
                slime.setTarget(target);slime.setDeltaMovement(delta.scale(.6).add(0,.4,0));slime.syncVelocity=true;mob.swingForAttack(net.minecraft.world.InteractionHand.MAIN_HAND);}
        }
        @Override public void stop(){slime=null;target=null;}
    }
    public static Goal chargeGoal(Mob mob){return new ChargeGoal(mob);}
    private static final class ChargeGoal extends Goal {
        private final Mob mob;private Mob mount;private LivingEntity target;private Vec3 direction;private int ticks;private boolean hit;
        ChargeGoal(Mob mob){this.mob=mob;setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
        private boolean enabled(){
            if(mob.getType()==EntityTypes.HUSK && mob.getVehicle() instanceof CamelHusk)return EnvironmentCombat.on(mob,FeatureId.HUSK_SPEAR_CHARGE)&&mob.getMainHandItem().is(ItemTags.SPEARS);
            if(mob.getVehicle()!=null && mob.getVehicle().getType()==EntityTypes.CHICKEN)return active(mob,FeatureId.CHICKEN_JOCKEY_CHARGE);
            return mob.getVehicle()!=null && mob.getVehicle().getType()==EntityTypes.SKELETON_HORSE&&EnvironmentCombat.on(mob,FeatureId.SKELETON_HORSE_CHARGE);
        }
        @Override public boolean canUse(){return enabled()&&mob.getVehicle() instanceof Mob&&SkeletonCombat.validTarget(mob,mob.getTarget())&&mob.hasLineOfSight(mob.getTarget())
                &&mob.distanceToSqr(mob.getTarget())>16&&mob.distanceToSqr(mob.getTarget())<100&&HostileCombat.instance().ready(mob,"rider_charge");}
        @Override public void start(){mount=(Mob)mob.getVehicle();target=mob.getTarget();direction=target.position().subtract(mount.position()).multiply(1,0,1).normalize();ticks=0;hit=false;HostileCombat.instance().reserve(mob,"rider_charge",240);}
        @Override public boolean canContinueToUse(){return ticks<40&&!hit&&enabled()&&mob.getVehicle()==mount&&target==mob.getTarget()&&SkeletonCombat.validTarget(mob,target)&&mount.isAlive();}
        @Override public boolean requiresUpdateEveryTick(){return true;}
        @Override public void tick(){if(!canContinueToUse())return;ticks++;mob.getNavigation().stop();mount.getNavigation().stop();
            if(ticks<=20){if(ticks%4==0)((ServerLevel)mob.level()).sendParticles(ParticleTypes.CRIT,mount.getX(),mount.getY()+1,mount.getZ(),4,.4,.3,.4,0);return;}
            if(!SkeletonCombat.safeStep(mount,direction.x,direction.z)){hit=true;return;}
            mount.setDeltaMovement(direction.scale(.55).add(0,mount.getDeltaMovement().y,0));mount.syncVelocity=true;
            if(mount.distanceToSqr(target)<6.25 && mob.hasLineOfSight(target)){mob.doHurtTarget((ServerLevel)mob.level(),target);hit=true;}
        }
        @Override public void stop(){if(mount!=null)mount.setDeltaMovement(0,mount.getDeltaMovement().y,0);target=null;mount=null;}
    }
}