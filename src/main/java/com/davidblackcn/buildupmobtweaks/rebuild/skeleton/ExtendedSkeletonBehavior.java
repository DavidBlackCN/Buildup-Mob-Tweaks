/* Mob AI Tweaks SPIN/GRENADE adaptation: Copyright (c) 2024 N0t_UN_Owen, MIT. See NOTICE.md. */
package com.davidblackcn.buildupmobtweaks.rebuild.skeleton;

import com.davidblackcn.buildupmobtweaks.feature.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.projectile.hurtingprojectile.WitherSkull;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import java.util.*;

public final class ExtendedSkeletonBehavior {
    public static final int SPIN_WARNING=20,SPIN_TICKS=10,SPIN_COOLDOWN=120;
    public static final int SKULL_WARNING=30,SKULL_COOLDOWN=400,SKULL_LIFETIME=100,HOMING_TICKS=40;
    private static ExtendedSkeletonBehavior instance;
    private final FeatureRegistry features;
    // Entity unload callbacks must not mutate EntitySectionManager while it is iterating.
    private final Set<WitherSkull> pending=Collections.newSetFromMap(new IdentityHashMap<>());
    public ExtendedSkeletonBehavior(FeatureRegistry features){this.features=features;}
    public static ExtendedSkeletonBehavior instance(){return instance;}
    public static boolean supported(AbstractSkeleton m){return m.getType()==EntityTypes.PARCHED||m.getType()==EntityTypes.WITHER_SKELETON;}
    public boolean enabled(AbstractSkeleton m,FeatureId id){return supported(m)&&ExtendedSkeletonState.known(m,ExtendedSkeletonState.DATA)&&features.isEnabled(id)
            &&!m.getType().builtInRegistryHolder().is(SkeletonBehavior.EXCLUDED)&&!m.entityTags().contains("buildupmobtweaks:vanilla_ai")
            &&!m.entityTags().contains("buildupmobtweaks:disable_"+id.id().getPath());}
    public static boolean ominous(LivingEntity t){return t.hasEffect(MobEffects.BAD_OMEN)||t.hasEffect(MobEffects.RAID_OMEN)||t.hasEffect(MobEffects.TRIAL_OMEN);}
    public void register(){
        instance=this;
        ServerEntityEvents.ENTITY_LOAD.register((e,l)->{
            if(e instanceof AbstractSkeleton m&&supported(m))install(m);
            if(e instanceof WitherSkull s&&s.hasAttached(ExtendedSkeletonState.SKULL)&&!ExtendedSkeletonState.live(s))pending.add(s);
        });
        ServerEntityEvents.ENTITY_UNLOAD.register((e,l)->{
            if(e instanceof AbstractSkeleton m&&supported(m)&&ExtendedSkeletonState.known(m,ExtendedSkeletonState.DATA)){cancel(m);pending.addAll(ExtendedSkeletonState.runtime(m).projectiles);ExtendedSkeletonState.runtime(m).projectiles.clear();}
        });
        ServerTickEvents.END_SERVER_TICK.register(server->{for(var s:List.copyOf(pending))s.discard();pending.clear();});
        ServerLivingEntityEvents.ALLOW_DEATH.register((e,s,a)->{if(e instanceof AbstractSkeleton m&&supported(m)&&ExtendedSkeletonState.known(m,ExtendedSkeletonState.DATA)){cancel(m);retire(m);}return true;});
    }
    private void install(AbstractSkeleton m){
        var r=ExtendedSkeletonState.runtime(m);if(r.installed)return;r.installed=true;
        if(!m.hasAttached(ExtendedSkeletonState.DATA)){
            var d=new CompoundTag();d.putInt("version",1);d.putString("birth","none");m.setAttached(ExtendedSkeletonState.DATA,d);
            boolean fresh=!m.isLoadedFromDisk()&&m.spawnReason()!=null&&m.spawnReason()!=EntitySpawnReason.LOAD&&m.spawnReason()!=EntitySpawnReason.CONVERSION&&m.spawnReason()!=EntitySpawnReason.DIMENSION_TRAVEL;
            var id=m.getType()==EntityTypes.PARCHED?FeatureId.PARCHED_DODGE:FeatureId.WITHER_SKELETON_SKULL;
            if(fresh&&enabled(m,id)&&m.getRandom().nextInt(1000)<features.majorChance(id))d.putString("birth",m.getType()==EntityTypes.PARCHED?"spin":"skull");
            d.putLong("special_ready",m.level().getGameTime()+(m.getType()==EntityTypes.PARCHED?SPIN_COOLDOWN:SKULL_COOLDOWN));
        }
        if(!ExtendedSkeletonState.known(m,ExtendedSkeletonState.DATA))return;
        var state=m.getAttached(ExtendedSkeletonState.DATA);
        if(!state.contains("special_ready"))state.putLong("special_ready",m.level().getGameTime()+(m.getType()==EntityTypes.PARCHED?SPIN_COOLDOWN:SKULL_COOLDOWN));
        m.setAttached(ExtendedSkeletonState.SPIN,0);
        m.getGoalSelector().addGoal(3,new ExtendedSkeletonGoal(m,this));
    }
    public void beforeAi(AbstractSkeleton m){
        if(!supported(m)||!ExtendedSkeletonState.known(m,ExtendedSkeletonState.DATA))return;
        var r=ExtendedSkeletonState.runtime(m);var id=m.getType()==EntityTypes.PARCHED?FeatureId.PARCHED_DODGE:FeatureId.WITHER_SKELETON_SKULL;
        if(r.active!=null&&(!enabled(m,id)||m.isNoAi()||!m.isAlive()||m.isPassenger()||r.target!=m.getTarget()||!SkeletonBehavior.valid(m,r.target)||!m.hasLineOfSight(r.target)))cancel(m);
        if(!SkeletonBehavior.valid(m,m.getTarget())&&m.getTarget()!=null){m.setTarget(null);m.stopUsingItem();m.getNavigation().stop();}
        r.projectiles.removeIf(Entity::isRemoved);
        if(!enabled(m,FeatureId.WITHER_SKELETON_SKULL)||m.isNoAi()||!m.isAlive())retire(m);
    }
    public void cancel(AbstractSkeleton m){var r=ExtendedSkeletonState.runtime(m);if(r.active!=null){r.cancels++;m.getMoveControl().strafe(0,0);m.stopUsingItem();}reset(m);}
    public void reset(AbstractSkeleton m){var r=ExtendedSkeletonState.runtime(m);r.active=null;r.target=null;r.phase="idle";m.setAttached(ExtendedSkeletonState.SPIN,0);}
    public void retire(AbstractSkeleton m){for(var s:List.copyOf(ExtendedSkeletonState.runtime(m).projectiles))s.discard();ExtendedSkeletonState.runtime(m).projectiles.clear();}
    public void release(AbstractSkeleton m,LivingEntity target){
        var level=(ServerLevel)m.level();var r=ExtendedSkeletonState.runtime(m);r.projectiles.removeIf(Entity::isRemoved);if(!r.projectiles.isEmpty())return;
        Vec3 origin=m.getEyePosition(),direction=target.getEyePosition().subtract(origin).normalize();
        var skull=new WitherSkull(level,m,direction);skull.setPos(origin.add(direction.scale(.7)));skull.setDangerous(false);
        var d=new CompoundTag();d.putInt("version",1);d.putString("owner",m.getUUID().toString());d.putString("target",target.getUUID().toString());
        d.putLong("expires",level.getGameTime()+SKULL_LIFETIME);d.putLong("home_until",level.getGameTime()+HOMING_TICKS);
        skull.setAttached(ExtendedSkeletonState.SKULL,d);ExtendedSkeletonState.markLive(skull);
        if(level.addFreshEntity(skull)){r.projectiles.add(skull);r.skulls++;}
    }
    public void skullTick(WitherSkull s){
        if(!(s.level() instanceof ServerLevel level)||!s.hasAttached(ExtendedSkeletonState.SKULL))return;
        if(!ExtendedSkeletonState.known(s,ExtendedSkeletonState.SKULL)||!ExtendedSkeletonState.live(s)){s.discard();return;}
        var d=s.getAttached(ExtendedSkeletonState.SKULL);
        if(!(s.getOwner() instanceof AbstractSkeleton m)||!supported(m)||!m.getUUID().toString().equals(d.getStringOr("owner",""))
                ||!m.isAlive()||m.isNoAi()||!enabled(m,FeatureId.WITHER_SKELETON_SKULL)||level.getGameTime()>=d.getLongOr("expires",0)){s.discard();return;}
        var t=m.getTarget();
        if(!SkeletonBehavior.valid(m,t)||!t.getUUID().toString().equals(d.getStringOr("target",""))||!m.hasLineOfSight(t)||s.distanceToSqr(t)>32*32){s.discard();return;}
        if(enabled(m,FeatureId.WITHER_SKELETON_HOMING)&&ominous(t)&&level.getGameTime()<d.getLongOr("home_until",0)&&s.distanceTo(t)>5){
            var v=s.getDeltaMovement().scale(.8).add(t.getEyePosition().subtract(s.position()).normalize().scale(.1));
            s.setDeltaMovement(v.lengthSqr()>.36?v.normalize().scale(.6):v);s.needsSync=true;ExtendedSkeletonState.runtime(m).homingTicks++;
        }
    }
    /** Vanilla already supports main/offhand BOW; this gate controls our durability accounting only. */
    public void bowShot(AbstractSkeleton m){if(m.getType()!=EntityTypes.WITHER_SKELETON||!enabled(m,FeatureId.WITHER_SKELETON_BOW))return;
        InteractionHand hand=m.getMainHandItem().is(Items.BOW)?InteractionHand.MAIN_HAND:InteractionHand.OFF_HAND;
        if(m.getItemInHand(hand).is(Items.BOW)&&!m.getItemInHand(hand).is(SkeletonBehavior.EXCLUDED_ITEMS)){m.getItemInHand(hand).hurtAndBreak(1,m,hand==InteractionHand.MAIN_HAND?EquipmentSlot.MAINHAND:EquipmentSlot.OFFHAND);ExtendedSkeletonState.runtime(m).shots++;}
    }
}
