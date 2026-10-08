package com.davidblackcn.buildupmobtweaks.combat;
import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import com.davidblackcn.buildupmobtweaks.mixin.CreeperFuseAccess;
import java.util.ArrayList;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.*;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.item.*;
import net.minecraft.world.level.entity.EntityTypeTest;

public final class GeneralHostileRules {
    private GeneralHostileRules(){}
    public static void register(){ServerLivingEntityEvents.AFTER_DAMAGE.register((entity,source,base,taken,blocked)->{
        if(entity instanceof Creeper creeper && taken>0 && EnvironmentCombat.on(creeper,FeatureId.CREEPER_HIT_DELAY)&&HostileCombat.instance().ready(creeper,"fuse_delay")){
            var access=(CreeperFuseAccess)creeper;access.buildup$setSwell(Math.max(0,access.buildup$getSwell()-5));HostileCombat.instance().reserve(creeper,"fuse_delay",10);
        }
    });}
    public static void initialize(Mob mob){
        var data=mob.getAttached(AdvancedHostiles.DATA);if(data==null||data.getIntOr("version",-1)!=1||data.getBooleanOr("effects_checked",false))return;
        data=data.copy();data.putBoolean("effects_checked",true);mob.setAttached(AdvancedHostiles.DATA,data);
        if(data.getStringOr("origin","").equals("rolled")&&data.getStringOr("trait","").equals("none")&&EnvironmentCombat.on(mob,FeatureId.HOSTILE_SPAWN_EFFECT)&&mob.getRandom().nextInt(1000)<30)
            mob.addEffect(new MobEffectInstance(mob.getRandom().nextBoolean()?MobEffects.SPEED:MobEffects.FIRE_RESISTANCE,600,0));
    }
    public static void tick(Mob mob){
        if(mob.level().isClientSide())return;
        if(refusesTarget(mob,mob.getTarget()))mob.setTarget(null);
        if(mob.getType()==EntityTypes.SKELETON_HORSE || mob.getType()==EntityTypes.ZOMBIE_HORSE){
            if(HostileCombat.instance()!=null&&HostileCombat.instance().gate(FeatureId.UNDEAD_HORSE_SUNBURN)&&mob.tickCount%20==0
                    &&mob.level().isBrightOutside()&&mob.level().canSeeSky(mob.blockPosition())&&!mob.isInWater()&&!mob.level().isRainingAt(mob.blockPosition()))mob.igniteForSeconds(4);
            return;
        }
        if(!HostileCombat.eligible(mob))return;
        if(EnvironmentCombat.on(mob,FeatureId.BURN_FREEZE_VISUAL)&&mob.tickCount%10==0&&(mob.isOnFire()||mob.getTicksFrozen()>0))
            ((ServerLevel)mob.level()).sendParticles(mob.isOnFire()?ParticleTypes.SMOKE:ParticleTypes.SNOWFLAKE,mob.getX(),mob.getY()+1,mob.getZ(),2,.25,.5,.25,0);
        if(mob instanceof Spider && EnvironmentCombat.on(mob,FeatureId.SPIDER_HUNTS_PESTS)&&mob.getTarget()==null&&(mob.tickCount+mob.getId())%40==0){
            var prey=new ArrayList<Mob>();mob.level().getEntities(EntityTypeTest.forClass(Mob.class),mob.getBoundingBox().inflate(8),
                    other->(other.getType()==EntityTypes.SILVERFISH||other.getType()==EntityTypes.ENDERMITE)&&SkeletonCombat.validTarget(mob,other)&&mob.hasLineOfSight(other),prey,8);
            if(!prey.isEmpty())mob.setTarget(prey.getFirst());
        }
        if(EnvironmentCombat.on(mob,FeatureId.HOSTILE_ESCAPE_SEAT)&&mob.isPassenger()&&(mob.getVehicle() instanceof AbstractBoat||mob.getVehicle().getType().builtInRegistryHolder().is(S2Tags.ESCAPABLE_SEATS))
                &&SkeletonCombat.validTarget(mob,mob.getTarget())&&HostileCombat.instance().ready(mob,"seat_escape")
                &&!(mob instanceof net.minecraft.world.entity.monster.illager.AbstractIllager && mob.getVehicle().getStringUUID().equals(mob.getAttached(AdvancedHostiles.DATA).getStringOr("boat","")))){
            HostileCombat.instance().reserve(mob,"seat_escape",100);
            var vehicle=mob.getVehicle();var base=vehicle.blockPosition();
            search: for(int dy=1;dy>=-1;dy--)for(var direction:net.minecraft.core.Direction.Plane.HORIZONTAL){
                var pos=base.relative(direction,2).offset(0,dy,0);var floor=pos.below();var state=mob.level().getBlockState(floor);
                if(!mob.level().hasChunkAt(pos)||!state.isFaceSturdy(mob.level(),floor,net.minecraft.core.Direction.UP)
                        ||state.is(net.minecraft.world.level.block.Blocks.MAGMA_BLOCK)||state.is(net.minecraft.world.level.block.Blocks.CAMPFIRE)
                        ||state.is(net.minecraft.world.level.block.Blocks.SOUL_CAMPFIRE)||!mob.level().getFluidState(pos).isEmpty()
                        ||!mob.level().getBlockState(pos).isAir()||!mob.level().getBlockState(pos.above()).isAir())continue;
                var point=net.minecraft.world.phys.Vec3.atBottomCenterOf(pos);
                if(mob.level().noCollision(mob,mob.getBoundingBox().move(point.subtract(mob.position())))){
                    mob.stopRiding();mob.teleportTo(point.x,point.y,point.z);break search;
                }
            }
        }
        if(mob instanceof Piglin && AdvancedHostiles.instance().active(mob,FeatureId.PIGLIN_ITEM_DODGE)&&mob.isUsingItem()&&mob.onGround()&&SkeletonCombat.validTarget(mob,mob.getTarget())
                &&mob.distanceToSqr(mob.getTarget())<16&&HostileCombat.instance().ready(mob,"piglin_dodge")){
            HostileCombat.instance().reserve(mob,"piglin_dodge",200);
            var away=mob.position().subtract(mob.getTarget().position()).multiply(1,0,1).normalize();
            if(SkeletonCombat.safeStep(mob,away.x,away.z)&&mob.level().noCollision(mob,mob.getBoundingBox().move(0,1,0))){mob.jumpFromGround();mob.setDeltaMovement(away.scale(.3).add(0,mob.getDeltaMovement().y,0));}
        }
    }
    public static void projectile(Projectile projectile){
        if(!(projectile.level() instanceof ServerLevel level)||HostileCombat.instance()==null)return;
        if(projectile.isOnFire()&&HostileCombat.instance().gate(FeatureId.BURNING_PROJECTILE_VISUAL)&&projectile.tickCount%4==0)
            level.sendParticles(ParticleTypes.FLAME,projectile.getX(),projectile.getY(),projectile.getZ(),1,0,0,0,0);
    }
    public static void explode(Creeper mob){
        var level=(ServerLevel)mob.level();
        if(EnvironmentCombat.on(mob,FeatureId.CREEPER_EMBEDDED_ARROWS)){
            int count=Math.min(8,mob.getArrowCount());mob.setArrowCount(0);
            for(int i=0;i<count;i++){double angle=Math.PI*2*i/count;var arrow=new Arrow(level,mob,new ItemStack(Items.ARROW),ItemStack.EMPTY);arrow.setBaseDamage(1);arrow.pickup=AbstractArrow.Pickup.DISALLOWED;
                arrow.shoot(Math.cos(angle),.2,Math.sin(angle),1.2f,0);level.addFreshEntity(arrow);}
        }
        if(mob.isOnFire()&&EnvironmentCombat.on(mob,FeatureId.CREEPER_FIRE_VISUAL))level.sendParticles(ParticleTypes.FLAME,mob.getX(),mob.getY()+1,mob.getZ(),40,1,1,1,.1);
    }
    public static boolean refusesTarget(Mob mob,LivingEntity target){
        if(target==null||mob.level().isClientSide()||HostileCombat.instance()==null)return false;
        if(mob instanceof net.minecraft.world.entity.animal.golem.AbstractGolem){
            if(HostileCombat.instance().gate(FeatureId.GOLEM_FRIENDLY_FIRE_FIX)&&target.getType().builtInRegistryHolder().is(S2Tags.GOLEM_EXCLUDED))return true;
            if(HostileCombat.instance().gate(FeatureId.GOLEM_FRIENDLY_FIRE_FIX)&&target instanceof net.minecraft.world.entity.animal.golem.AbstractGolem)return true;
        }
        return mob instanceof net.minecraft.world.entity.monster.zombie.ZombifiedPiglin&&target instanceof net.minecraft.world.entity.monster.zombie.ZombifiedPiglin&&HostileCombat.instance().gate(FeatureId.ZOMBIFIED_PIGLIN_ANGER_FIX);
    }
}