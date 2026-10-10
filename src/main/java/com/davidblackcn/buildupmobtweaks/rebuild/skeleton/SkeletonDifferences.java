/* Mob AI Tweaks draw/DODGE adaptation, Copyright (c) 2024 N0t_UN_Owen, MIT. */
package com.davidblackcn.buildupmobtweaks.rebuild.skeleton;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import net.fabricmc.fabric.api.attachment.v1.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.*;
import net.minecraft.world.phys.*;
import java.util.*;

/** Differences within the existing Skeleton Goal chain; never installs another attack Goal. */
public final class SkeletonDifferences {
    public static final AttachmentType<Boolean> VISUAL=AttachmentRegistry.create(BuildupMobTweaks.id("skeleton_draw_visual"),b->b.initializer(()->false).syncWith(ByteBufCodecs.BOOL,AttachmentSyncPredicate.all()));
    public static final AttachmentType<Boolean> BOW_VISUAL=AttachmentRegistry.create(BuildupMobTweaks.id("skeleton_bow_visual"),b->b.initializer(()->false).syncWith(ByteBufCodecs.BOOL,AttachmentSyncPredicate.all()));
    public static final AttachmentType<CompoundTag> MUSHROOM=AttachmentRegistry.createPersistent(BuildupMobTweaks.id("bogged_mushroom"),CompoundTag.CODEC);
    public static final AttachmentType<Boolean> LIVE=AttachmentRegistry.createDefaulted(BuildupMobTweaks.id("bogged_mushroom_live"),()->false);
    private static final Set<AbstractSkeleton> owners=Collections.newSetFromMap(new IdentityHashMap<>());
    private static final Set<Entity> active=Collections.newSetFromMap(new IdentityHashMap<>()),pending=Collections.newSetFromMap(new IdentityHashMap<>());
    public static void bootstrap(){}
    public static boolean known(Entity e){var d=e.getAttached(MUSHROOM);return d!=null&&d.getIntOr("version",-1)==1;}
    public static boolean ominous(LivingEntity t){return t!=null&&(t.hasEffect(MobEffects.BAD_OMEN)||t.hasEffect(MobEffects.TRIAL_OMEN));}
    private static boolean enabled(AbstractSkeleton m,FeatureId id){var b=SkeletonBehavior.instance();return b!=null&&!m.isNoAi()&&m.isAlive()&&b.enabled(m,id);}
    public static void register(){
        ServerEntityEvents.ENTITY_LOAD.register((e,l)->{
            if(e instanceof AbstractSkeleton m&&SkeletonBehavior.supported(m))owners.add(m);
            if((e instanceof Snowball||e instanceof AreaEffectCloud)&&known(e)&&!Boolean.TRUE.equals(e.getAttached(LIVE)))pending.add(e);
        });
        ServerEntityEvents.ENTITY_UNLOAD.register((e,l)->{
            active.remove(e);pending.remove(e);
            if(e instanceof AbstractSkeleton m){owners.remove(m);for(var child:List.copyOf(active))if(owner(child)==m)pending.add(child);}
        });
        ServerTickEvents.END_SERVER_TICK.register(server->{
            for(var m:List.copyOf(owners)){
                boolean valid=SkeletonBehavior.valid(m,m.getTarget());
                var b=SkeletonBehavior.instance();boolean bow=b!=null&&b.hasBow(m)&&(!m.isUsingItem()||b.bow(m,m.getUseItem()));
                m.setAttached(VISUAL,valid&&bow&&enabled(m,FeatureId.SKELETON_OFFHAND_VISUAL));
                m.setAttached(BOW_VISUAL,bow&&enabled(m,FeatureId.SKELETON_BOW_COMPATIBILITY));
            }
            for(var e:List.copyOf(active)){if(!known(e))active.remove(e);else if(e.isRemoved()||!allowed(e))pending.add(e);}
            for(var e:List.copyOf(pending))if(known(e))e.discard();pending.clear();active.removeIf(e->e.isRemoved()||!known(e));
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server->{owners.clear();active.clear();pending.clear();});
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((victim,source,amount)->{
            var direct=source.getDirectEntity();if(direct instanceof AreaEffectCloud&&known(direct)){
                var m=owner(direct);return m!=null&&allowed(direct)&&SkeletonBehavior.valid(m,victim);
            }return true;
        });
    }
    public static void draw(AbstractSkeleton m){
        if(!enabled(m,FeatureId.SKELETON_OFFHAND_VISUAL)||!SkeletonBehavior.valid(m,m.getTarget())||!m.isUsingItem())return;
        var b=SkeletonBehavior.instance();if(!b.bow(m,m.getUseItem()))return;
        m.swingForAttack(m.getUsedItemHand()==InteractionHand.MAIN_HAND?InteractionHand.OFF_HAND:InteractionHand.MAIN_HAND);
        SkeletonState.runtime(m).drawSwings++;
    }
    private static AbstractSkeleton owner(Entity e){Entity o=e instanceof Snowball s?s.getOwner():e instanceof AreaEffectCloud c?c.getOwner():null;return o instanceof AbstractSkeleton m?m:null;}
    public static boolean allowed(Entity e){
        if(!(e.level() instanceof ServerLevel l)||!Boolean.TRUE.equals(e.getAttached(LIVE)))return false;
        var d=e.getAttached(MUSHROOM);if(d==null||d.getIntOr("version",-1)!=1||l.getGameTime()>=d.getLongOr("expires",0))return false;
        var m=owner(e);return m!=null&&m.getType()==EntityTypes.BOGGED&&enabled(m,FeatureId.BOGGED_OMEN_MUSHROOMS)
                &&enabled(m,FeatureId.BOGGED_SPORE_RETREAT)&&m.getUUID().toString().equals(d.getStringOr("owner",""))
                &&SkeletonBehavior.valid(m,m.getTarget())&&m.getTarget().getUUID().toString().equals(d.getStringOr("target",""));
    }
    public static boolean affects(AreaEffectCloud cloud,LivingEntity victim){var m=owner(cloud);return m!=null&&allowed(cloud)&&SkeletonBehavior.valid(m,victim);}
    private static void mark(Entity e,AbstractSkeleton m,LivingEntity t,int lifetime){
        var d=new CompoundTag();d.putInt("version",1);d.putString("owner",m.getUUID().toString());d.putString("target",t.getUUID().toString());d.putLong("expires",e.level().getGameTime()+lifetime);
        e.setAttached(MUSHROOM,d);e.setAttached(LIVE,true);
    }
    public static void release(AbstractSkeleton m,LivingEntity t){
        if(m.getType()!=EntityTypes.BOGGED||!enabled(m,FeatureId.BOGGED_OMEN_MUSHROOMS)||!SkeletonBehavior.valid(m,t)||!ominous(t))return;
        if(active.stream().anyMatch(e->!e.isRemoved()&&owner(e)==m))return;
        var l=(ServerLevel)m.level();for(int i=0;i<2;i++){
            var ball=new Snowball(l,m,(i==0?Items.RED_MUSHROOM:Items.BROWN_MUSHROOM).getDefaultInstance());
            var v=new Vec3(.5*(m.getRandom().nextDouble()-.5),m.getRandom().nextDouble()*.2+.1,.5*(m.getRandom().nextDouble()-.5));
            ball.setPos(m.getEyePosition().add(v));ball.setDeltaMovement(v);mark(ball,m,t,100);
            if(l.addFreshEntity(ball)){active.add(ball);SkeletonState.runtime(m).mushrooms++;}
        }
    }
    /** Called only by native Snowball collision. Entity/block collisions share a single burst. */
    public static void impact(Snowball ball,HitResult hit){
        if(!allowed(ball)){ball.discard();return;}var m=owner(ball);var l=(ServerLevel)ball.level();
        if(hit instanceof EntityHitResult entity){
            if(!(entity.getEntity() instanceof LivingEntity victim)||!SkeletonBehavior.valid(m,victim)){ball.discard();return;}
            if(victim.hurtServer(l,ball.damageSources().thrown(ball,m),l.getDifficulty().getId()+1))SkeletonState.runtime(m).mushroomHits++;
        }
        long clouds=active.stream().filter(e->e instanceof AreaEffectCloud&&!e.isRemoved()&&owner(e)==m).count();
        if(clouds<2){
            var pos=hit.getLocation();var cloud=new AreaEffectCloud(l,pos.x,pos.y,pos.z);cloud.setOwner(m);cloud.setDuration(20);cloud.setWaitTime(10);
            cloud.setRadius(.4f);cloud.setRadiusPerTick(.1f);cloud.setRadiusOnUse(.1f);cloud.setCustomParticle(ParticleTypes.WARPED_SPORE);cloud.setPotionContents(new PotionContents(Potions.HARMING));
            mark(cloud,m,m.getTarget(),30);if(l.addFreshEntity(cloud)){active.add(cloud);SkeletonState.runtime(m).mushroomClouds++;}
        }
        l.broadcastEntityEvent(ball,(byte)3);ball.discard();
    }
    private SkeletonDifferences(){}
}
