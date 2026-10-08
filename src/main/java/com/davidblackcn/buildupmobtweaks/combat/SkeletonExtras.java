package com.davidblackcn.buildupmobtweaks.combat;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import java.util.EnumSet;
import net.fabricmc.fabric.api.attachment.v1.*;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.skeleton.*;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.hurtingprojectile.WitherSkull;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.*;
import net.minecraft.world.phys.Vec3;

public final class SkeletonExtras {
    public static final AttachmentType<CompoundTag> PROJECTILE = AttachmentRegistry.createPersistent(BuildupMobTweaks.id("skeleton_projectile"), CompoundTag.CODEC);
    private SkeletonExtras() {}
    public static boolean omen(LivingEntity target) { return target != null && (target.hasEffect(MobEffects.BAD_OMEN) || target.hasEffect(MobEffects.TRIAL_OMEN)); }
    public static void register() {
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity,source,amount)-> {
            if (!(entity instanceof Skeleton skeleton) || !EnvironmentCombat.on(skeleton,FeatureId.SKELETON_WITHER_CONVERSION)
                    || !(skeleton.hasEffect(MobEffects.WITHER) || source.is(net.minecraft.world.damagesource.DamageTypes.WITHER))) return true;
            var converted=skeleton.convertTo(EntityTypes.WITHER_SKELETON,ConversionParams.single(skeleton,true,true),EntitySpawnReason.CONVERSION,
                    result -> result.setHealth(result.getMaxHealth()));
            return converted == null;
        });
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity,source,amount)-> {
            if (!(source.getEntity() instanceof AbstractSkeleton shooter) || !EnvironmentCombat.on(shooter,FeatureId.RIDER_FRIENDLY_FIRE)
                    || !(shooter.getVehicle() instanceof net.minecraft.world.entity.animal.equine.SkeletonHorse)) return true;
            return !(entity == shooter.getVehicle() || entity instanceof AbstractSkeleton other
                    && other.getVehicle() instanceof net.minecraft.world.entity.animal.equine.SkeletonHorse);
        });
    }
    public static void install(Mob mob) {
        if(mob instanceof Skeleton)mob.getGoalSelector().addGoal(1,shelterGoal(mob));
        if (mob instanceof Stray || mob instanceof WitherSkeleton || mob instanceof Parched) mob.getGoalSelector().addGoal(1,goal(mob));
        var data=mob.getAttached(AdvancedHostiles.DATA);
        if (mob instanceof WitherSkeleton && data != null && data.getIntOr("version",-1)==1 && data.getStringOr("origin","").equals("rolled")
                && !data.getBooleanOr("bow_checked",false)) {
            data=data.copy();data.putBoolean("bow_checked",true);mob.setAttached(AdvancedHostiles.DATA,data);
            // Do not overwrite a vanilla or another mod's sword. Equip only an empty hand.
            if(EnvironmentCombat.on(mob,FeatureId.WITHER_SKELETON_BOW) && mob.getMainHandItem().isEmpty() && mob.getRandom().nextInt(1000)<10) {
                mob.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.BOW));mob.setDropChance(EquipmentSlot.MAINHAND,0);
            }
        }
    }
    public static Goal goal(Mob mob) { return new AttackGoal(mob); }
    public static void tick(Mob mob) {
        if (mob instanceof Bogged && EnvironmentCombat.on(mob,FeatureId.BOGGED_OMEN_MUSHROOMS)
                && omen(mob.getTarget()) && SkeletonCombat.validTarget(mob,mob.getTarget()) && mob.distanceToSqr(mob.getTarget())<36
                && mob.hasLineOfSight(mob.getTarget()) && HostileCombat.instance().ready(mob,"mushrooms")) {
            HostileCombat.instance().reserve(mob,"mushrooms",300);
            for(int i=0;i<2;i++) {
                var ball=new Snowball(mob.level(),mob,new ItemStack(Items.BROWN_MUSHROOM));
                var delta=mob.getTarget().position().subtract(mob.position());ball.shoot(delta.x+(i==0?1:-1),delta.y+2,delta.z,.5f,0);
                tag(ball,"mushroom",mob.getTarget());mob.level().addFreshEntity(ball);
            }
        }
    }
    public static Goal shelterGoal(Mob mob){return new ShelterGoal(mob);}
    private static final class ShelterGoal extends Goal {
        private final Mob mob;private net.minecraft.world.level.pathfinder.Path path;private int ticks;
        ShelterGoal(Mob mob){this.mob=mob;setFlags(EnumSet.of(Flag.MOVE));}
        private boolean enabled(){return EnvironmentCombat.on(mob,FeatureId.SKELETON_SHELTER)&&SkeletonCombat.instance().active(mob)&&SkeletonCombat.validTarget(mob,mob.getTarget());}
        @Override public boolean canUse(){
            if(!enabled()||!mob.onGround()||mob.isInWater()||mob.isPassenger()||!mob.level().canSeeSky(mob.blockPosition())||!HostileCombat.instance().ready(mob,"shelter_search"))return false;
            HostileCombat.instance().reserve(mob,"shelter_search",100);
            // Eight candidates per search; only one path computation after a safe roof is found.
            for(int i=0;i<8;i++){
                double angle=i*Math.PI/4;var pos=mob.blockPosition().offset((int)Math.round(4*Math.cos(angle)),0,(int)Math.round(4*Math.sin(angle)));
                if(!mob.level().hasChunkAt(pos)||mob.level().canSeeSky(pos)||!mob.level().getBlockState(pos).isAir()||!mob.level().getBlockState(pos.above()).isAir()
                        ||!mob.level().getBlockState(pos.below()).isFaceSturdy(mob.level(),pos.below(),Direction.UP))continue;
                path=mob.getNavigation().createPath(pos,0);return path!=null&&path.canReach();
            }return false;
        }
        @Override public void start(){ticks=0;mob.getNavigation().moveTo(path,1);}
        @Override public boolean canContinueToUse(){return enabled()&&ticks<100&&!mob.getNavigation().isDone();}
        @Override public boolean requiresUpdateEveryTick(){return true;}
        @Override public void tick(){ticks++;}
        @Override public void stop(){mob.getNavigation().stop();path=null;}
    }
    public static void tag(Projectile projectile,String role,LivingEntity target) {
        var tag=new CompoundTag();tag.putInt("version",1);tag.putString("role",role);tag.putString("target",target.getStringUUID());
        tag.putLong("expires",projectile.level().getGameTime()+100);projectile.setAttached(PROJECTILE,tag);
    }
    public static void projectileTick(Projectile projectile) {
        if(!(projectile.level() instanceof ServerLevel level)) return;
        var data=projectile.getAttached(PROJECTILE);if(data==null || data.getIntOr("version",-1)!=1) return;
        if(level.getGameTime()>=data.getLongOr("expires",0)){projectile.discard();return;}
        if(!(projectile.getOwner() instanceof Mob owner)) return;
        String role=data.getStringOr("role","");
        if(role.equals("snow_omen") && EnvironmentCombat.on(owner,FeatureId.STRAY_OMEN_SNOW)) {
            var velocity=projectile.getDeltaMovement();if(velocity.length()<1.2) projectile.setDeltaMovement(velocity.scale(1.04));
        }
        if(role.equals("homing") && EnvironmentCombat.on(owner,FeatureId.WITHER_SKELETON_HOMING) && projectile.tickCount<40) {
            java.util.UUID id;try{id=java.util.UUID.fromString(data.getStringOr("target",""));}catch(IllegalArgumentException ex){return;}
            var target=level.getEntity(id);
            if(!(target instanceof LivingEntity living) || !SkeletonCombat.validTarget(owner,living) || !owner.hasLineOfSight(living))return;
            var current=projectile.getDeltaMovement();var desired=living.getEyePosition().subtract(projectile.position()).normalize().scale(.35);
            projectile.setDeltaMovement(current.scale(.9).add(desired.scale(.1)));
        }
    }
    public static boolean mushroomHit(Snowball ball) {
        var data=ball.getAttached(PROJECTILE);
        if(!(ball.level() instanceof ServerLevel level) || data==null || data.getIntOr("version",-1)!=1 || !data.getStringOr("role","").equals("mushroom"))return false;
        if(ball.getOwner() instanceof Mob owner && EnvironmentCombat.on(owner,FeatureId.BOGGED_OMEN_MUSHROOMS)) {
            var cloud=new AreaEffectCloud(level,ball.getX(),ball.getY(),ball.getZ());cloud.setOwner(owner);cloud.setRadius(1);cloud.setDuration(30);cloud.setWaitTime(10);
            cloud.setRadiusOnUse(-.5f);cloud.setPotionContents(new PotionContents(Potions.HARMING));level.addFreshEntity(cloud);
        }
        ball.discard();return true;
    }
    private static final class AttackGoal extends Goal {
        private final Mob mob;private final FeatureId id;private LivingEntity target;private int ticks;private boolean shot;
        AttackGoal(Mob mob){this.mob=mob;id=mob instanceof Stray?FeatureId.STRAY_SNOW_BARRAGE:mob instanceof Parched?FeatureId.PARCHED_DODGE:FeatureId.WITHER_SKELETON_SKULL;setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
        @Override public boolean canUse(){return AdvancedHostiles.instance().active(mob,id) && SkeletonCombat.validTarget(mob,mob.getTarget())
                && mob.hasLineOfSight(mob.getTarget()) && mob.distanceToSqr(mob.getTarget())>36 && mob.distanceToSqr(mob.getTarget())<225
                && mob.onGround() && !mob.isPassenger() && !mob.isInWater() && HostileCombat.instance().ready(mob,"skeleton_extra")
                && (id!=FeatureId.STRAY_SNOW_BARRAGE || mob.level().getDifficulty()==net.minecraft.world.Difficulty.HARD || !(mob.getMainHandItem().getItem() instanceof BowItem));}
        @Override public void start(){target=mob.getTarget();ticks=0;shot=false;HostileCombat.instance().reserve(mob,"skeleton_extra",id==FeatureId.PARCHED_DODGE?120:400);mob.getNavigation().stop();}
        @Override public boolean canContinueToUse(){return ticks<60 && !shot && AdvancedHostiles.instance().active(mob,id) && target==mob.getTarget() && SkeletonCombat.validTarget(mob,target) && mob.hasLineOfSight(target);}
        @Override public boolean requiresUpdateEveryTick(){return true;}
        @Override public void tick(){
            if(!canContinueToUse())return;ticks++;mob.getNavigation().stop();var level=(ServerLevel)mob.level();
            if(ticks<20 && ticks%4==0)level.sendParticles(ParticleTypes.CRIT,mob.getX(),mob.getEyeY(),mob.getZ(),3,.3,.3,.3,0);
            if(id==FeatureId.PARCHED_DODGE && ticks>=20){
                Vec3 delta=target.position().subtract(mob.position()).multiply(1,0,1).normalize();Vec3 side=new Vec3(-delta.z,0,delta.x);
                if(SkeletonCombat.safeStep(mob,side.x,side.z))mob.setDeltaMovement(side.scale(.25).add(0,mob.getDeltaMovement().y,0));
                mob.setYRot(mob.getYRot()+18);if(ticks>=40)shot=true;return;
            }
            if(id==FeatureId.STRAY_SNOW_BARRAGE){
                if(ticks==20 && level.noCollision(mob,mob.getBoundingBox().move(0,1,0)))mob.jumpFromGround();
                if(ticks>20 && !mob.onGround() && mob.getDeltaMovement().y<-.01){
                    Vec3 delta=target.getEyePosition().subtract(mob.getEyePosition());
                    for(int i=-1;i<=1;i++){var ball=new Snowball(level,mob,new ItemStack(Items.SNOWBALL));boolean fast=omen(target)&&EnvironmentCombat.on(mob,FeatureId.STRAY_OMEN_SNOW);
                        ball.shoot(delta.x+i*.7,delta.y,delta.z,fast?.35f:1f,4);tag(ball,fast?"snow_omen":"snow",target);level.addFreshEntity(ball);}shot=true;
                }
            }else if(ticks==30){
                Vec3 delta=target.getEyePosition().subtract(mob.getEyePosition());var skull=new WitherSkull(level,mob,delta.normalize());skull.setPos(mob.getEyePosition());
                skull.setDeltaMovement(delta.normalize().scale(.25));skull.accelerationPower=.025;
                tag(skull,omen(target)&&EnvironmentCombat.on(mob,FeatureId.WITHER_SKELETON_HOMING)?"homing":"skull",target);level.addFreshEntity(skull);mob.swingForAttack(InteractionHand.MAIN_HAND);shot=true;
            }
        }
        @Override public void stop(){target=null; if(id==FeatureId.PARCHED_DODGE)mob.setDeltaMovement(0,mob.getDeltaMovement().y,0);}
    }
}