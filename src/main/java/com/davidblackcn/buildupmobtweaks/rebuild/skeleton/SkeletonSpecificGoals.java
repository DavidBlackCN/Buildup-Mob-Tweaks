/* Mob AI Tweaks FLIP/DODGE adaptation, Copyright (c) 2024 N0t_UN_Owen, MIT. */
package com.davidblackcn.buildupmobtweaks.rebuild.skeleton;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.*;
import java.util.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.phys.Vec3;

public final class SkeletonSpecificGoals extends Goal {
    public static final AttachmentType<Boolean> SNOW=AttachmentRegistry.createPersistent(BuildupMobTweaks.id("stray_rebuild_snow"),Codec.BOOL);
    public static final AttachmentType<Boolean> CLOUD=AttachmentRegistry.createPersistent(BuildupMobTweaks.id("bogged_rebuild_cloud"),Codec.BOOL);
    public static final AttachmentType<Boolean> CLOUD_LIVE=AttachmentRegistry.createDefaulted(BuildupMobTweaks.id("bogged_rebuild_cloud_live"),()->false);
    final AbstractSkeleton mob;final SkeletonBehavior behavior;final boolean stray;int remaining,snowDelay;
    SkeletonSpecificGoals(AbstractSkeleton mob,SkeletonBehavior behavior){this.mob=mob;this.behavior=behavior;stray=mob.getType()==EntityTypes.STRAY;}
    private boolean enabled(){return (stray||mob.getType()==EntityTypes.BOGGED)&&behavior.enabled(mob,stray?FeatureId.STRAY_JUMP_SHOT:FeatureId.BOGGED_SPORE_RETREAT);}
    @Override public boolean canUse(){return enabled()&&SkeletonBehavior.valid(mob,mob.getTarget())&&!mob.isPassenger()&&!mob.isBaby();}
    @Override public boolean canContinueToUse(){return canUse();}
    @Override public boolean requiresUpdateEveryTick(){return true;}
    @Override public void start(){var data=mob.getAttached(SkeletonState.DATA);if(!data.contains("special_ready"))data.putLong("special_ready",mob.level().getGameTime()+120);remaining=0;snowDelay=0;}
    @Override public void stop(){remaining=0;snowDelay=0;behavior.cleanupSpecial(mob);}
    @Override public void tick(){
        var access=(SkeletonAccess)mob;var target=mob.getTarget();var level=(ServerLevel)mob.level();var rt=SkeletonState.runtime(mob);
        if(remaining>0){
            remaining--;access.buildup$special(Integer.signum(access.buildup$special())*remaining);
            if(stray){
                level.sendParticles(ParticleTypes.SNOWFLAKE,mob.getX(),mob.getY()+.5,mob.getZ(),2,.1,.1,.1,.02);
                if(!mob.isUsingItem()&&remaining>0&&snowDelay--<=0){
                    snowDelay=6;mob.getLookControl().setLookAt(target,60,60);mob.lookAt(target,60,60);mob.swingForAttack(InteractionHand.OFF_HAND);
                    var ball=new Snowball(level,mob,Items.SNOWBALL.getDefaultInstance());ball.setAttached(SNOW,true);ball.setPos(mob.getEyePosition());
                    ball.setDeltaMovement(mob.getLookAngle().subtract(mob.getDeltaMovement().scale(.2)));level.addFreshEntity(ball);rt.snowballs++;
                    level.playSound(null,mob.blockPosition(),SoundEvents.SNOWBALL_THROW,SoundSource.HOSTILE,.5f,.8f+mob.getRandom().nextFloat()*.4f);
                }
            }
            return;
        }
        if(level.getGameTime()<mob.getAttached(SkeletonState.DATA).getLongOr("special_ready",0)||!mob.onGround()||mob.isInWater()||!mob.hasLineOfSight(target))return;
        float distance=mob.distanceTo(target);Vec3 direction=target.position().subtract(mob.position()).multiply(1,0,1).normalize();
        if(stray){
            if(behavior.hasBow(mob)&&(!mob.isUsingItem()||mob.getTicksUsingItem()<10||distance<=6))return;
            Vec3 motion=mob.getDeltaMovement();Vec3 impulse=mob.isUsingItem()?motion.scale(3):mob.getLookAngle().scale(-.5).subtract(motion);
            impulse=new Vec3(impulse.x,0,impulse.z);
            if(!SkeletonBehavior.safeStep(mob,impulse.normalize())||!level.noCollision(mob,mob.getBoundingBox().move(0,1,0)))return;
            mob.jumpFromGround();mob.push(impulse.x,mob.getDeltaMovement().y*.5,impulse.z);
            remaining=19;access.buildup$special(mob.zza>0?-19:19);rt.flips++;
        }else{
            if(distance>6&&distance<15)return;
            Vec3 impulse=direction.scale(distance<15?-1:1);if(!SkeletonBehavior.safeStep(mob,impulse))return;
            var cloud=new AreaEffectCloud(level,mob.getX(),mob.getY(),mob.getZ());cloud.setOwner(mob);cloud.setDuration(40);
            cloud.setAttached(CLOUD,true);cloud.setAttached(CLOUD_LIVE,true);
            cloud.setRadius(.4f);cloud.setRadiusPerTick(.1f);cloud.setRadiusOnUse(.1f);cloud.setCustomParticle(ParticleTypes.CRIMSON_SPORE);
            cloud.setPotionContents(new PotionContents(Optional.empty(),Optional.empty(),List.of(new MobEffectInstance(MobEffects.POISON,200)),Optional.empty()));
            level.addFreshEntity(cloud);rt.clouds.add(cloud);mob.push(impulse.x,.2,impulse.z);remaining=10;access.buildup$special(distance>6?-10:10);rt.dodges++;
        }
        mob.getAttached(SkeletonState.DATA).putLong("special_ready",level.getGameTime()+120);
    }
}
