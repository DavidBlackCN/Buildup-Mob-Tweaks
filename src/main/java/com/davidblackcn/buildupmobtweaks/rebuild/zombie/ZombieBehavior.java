/* Mob AI Tweaks adaptation: Copyright (c) 2024 N0t_UN_Owen, MIT. See NOTICE.md. */
package com.davidblackcn.buildupmobtweaks.rebuild.zombie;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import com.davidblackcn.buildupmobtweaks.feature.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.Display.BlockDisplay;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.*;
import net.minecraft.world.entity.npc.villager.VillagerType;

public final class ZombieBehavior {
    private final java.util.Set<BlockDisplay> pending=java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
    private static ZombieBehavior instance;
    private final FeatureRegistry features;
    public static final TagKey<EntityType<?>> EXCLUDED=TagKey.create(Registries.ENTITY_TYPE,BuildupMobTweaks.id("zombie_ai_excluded"));
    private static final net.minecraft.resources.Identifier DOOR_MOD=BuildupMobTweaks.id("door_rebuild");
    public ZombieBehavior(FeatureRegistry f){features=f;}
    public static ZombieBehavior instance(){return instance;}
    public static boolean supported(Zombie m){return m.getType()==EntityTypes.ZOMBIE||m.getType()==EntityTypes.HUSK;}
    public boolean enabled(Zombie m,FeatureId id){return supported(m)&&ZombieState.known(m)&&features.isEnabled(id)&&!m.getType().builtInRegistryHolder().is(EXCLUDED)
            &&!m.entityTags().contains("buildupmobtweaks:vanilla_ai")&&!m.entityTags().contains("buildupmobtweaks:disable_"+id.id().getPath());}
    public static boolean valid(Mob m,LivingEntity t){return t!=null&&t.isAlive()&&t.level()==m.level()&&!m.isAlliedTo(t)&&m.canAttack(t)
            &&(!(t instanceof Player p)||!p.isCreative()&&!p.isSpectator());}
    private void initialize(Zombie m){if(!m.hasAttached(ZombieState.DATA)){var d=new CompoundTag();d.putInt("version",1);d.putString("birth","none");m.setAttached(ZombieState.DATA,d);}}
    /** Called only from the verified finalizeSpawn TAIL, never from ENTITY_LOAD. */
    public void born(Zombie m,ServerLevelAccessor level,DifficultyInstance difficulty,EntitySpawnReason reason){
        if(!supported(m)||m.hasAttached(ZombieState.DATA))return;initialize(m);
        if(m.isBaby()||m.isPassenger()||m.isVehicle()||(reason!=EntitySpawnReason.NATURAL&&reason!=EntitySpawnReason.SPAWN_ITEM_USE))return;
        var cfg=BuildupMobTweaks.config().hostile.zombie;
        int door=reason==EntitySpawnReason.NATURAL&&difficulty.getEffectiveDifficulty()>2.5&&enabled(m,FeatureId.ZOMBIE_DOOR_GUARD)?cfg.doorChance.get():0;
        int rider=enabled(m,FeatureId.ZOMBIE_BABY_RIDER)?cfg.riderChance.get():0;
        int total=door+rider,roll=m.getRandom().nextInt(Math.max(1000,total));var d=m.getAttached(ZombieState.DATA);
        if(roll<door){d.putString("birth","door");d.putFloat("door_health",40);d.putString("material",BuiltInRegistries.BLOCK.getKey(birthMaterial(m)).toString());}
        else if(roll<total){
            // At most one same-family rider. JOCKEY births are ineligible, preventing recursive spawning.
            Zombie child=(Zombie)m.getType().create(level.getLevel(),EntitySpawnReason.JOCKEY);
            if(child!=null){child.snapTo(m.position(),m.getYRot(),0);child.setBaby(true);child.finalizeSpawn(level,difficulty,EntitySpawnReason.JOCKEY,new Zombie.ZombieGroupData(true,false));
                if(child.startRiding(m,true,false)){d.putString("birth","rider");d.putString("rider",child.getUUID().toString());}}
        }
    }
    public void register(){
        instance=this;
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPED.register(server->pending.clear());
        // Tracking callbacks may run while vanilla iterates an entity section. Retire displays after that iteration.
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server->{var batch=java.util.List.copyOf(pending);pending.clear();for(var d:batch)if(!d.isRemoved())d.discard();});
        ServerEntityEvents.ENTITY_LOAD.register((e,l)->{
            if(e instanceof BlockDisplay d&&Boolean.TRUE.equals(e.getAttached(ZombieState.DISPLAY))&&!Boolean.TRUE.equals(e.getAttached(ZombieState.DISPLAY_LIVE)))pending.add(d);
            if(e instanceof Zombie m&&supported(m)){
                initialize(m);if(!ZombieState.known(m))return;ZombieGoals.restore(m);
                var r=ZombieState.runtime(m);if(!r.installed){r.installed=true;m.getGoalSelector().addGoal(1,new ZombieGoals.Guard(m,this));
                    if(m.getType()==EntityTypes.HUSK)m.getGoalSelector().addGoal(1,new ZombieGoals.Burrow(m,this));}
                beforeAi(m);
            }
        });
        ServerEntityEvents.ENTITY_UNLOAD.register((e,l)->{if(e instanceof Zombie m&&supported(m)&&ZombieState.known(m)){ZombieGoals.restore(m);unloadDoor(m);}});
        ServerLivingEntityEvents.ALLOW_DEATH.register((e,s,a)->{if(e instanceof Zombie m&&supported(m)&&ZombieState.known(m)){ZombieGoals.restore(m);clearDoor(m);}return true;});
        ServerLivingEntityEvents.MOB_CONVERSION.register((old,next,c)->{if(old instanceof Zombie m&&supported(m)&&ZombieState.known(m)){ZombieGoals.restore(m);clearDoor(m);detachRider(m);}});
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((e,s,a)->!(e instanceof Zombie m&&block(m,s,a)));
        ZombieCommands.register();
    }
    public void beforeAi(Zombie m){
        if(!supported(m)||!ZombieState.known(m))return;var r=ZombieState.runtime(m);
        if(m.getAttached(ZombieState.DATA).getBooleanOr("burrow_active",false)&&!r.mode.equals("burrow"))ZombieGoals.restore(m);
        boolean active=enabled(m,FeatureId.ZOMBIE_DOOR_GUARD)&&m.isAlive()&&m.getAttached(ZombieState.DATA).getFloatOr("door_health",0)>0;
        if(active){r.displays.removeIf(Entity::isRemoved);if(r.displays.size()!=2){clearDoor(m);makeDoor(m);}doorAttributes(m,true);}
        else clearDoor(m);
        if(!enabled(m,FeatureId.ZOMBIE_BABY_RIDER))detachRider(m);
        if(enabled(m,FeatureId.ZOMBIE_SHIELD_USE)||enabled(m,FeatureId.HUSK_SAND_BURROW)||active){
            var t=m.getTarget();if(t!=r.remembered){r.remembered=t;r.unseen=0;}
            if(t!=null){r.unseen=m.hasLineOfSight(t)||m.getAttached(ZombieState.DATA).getBooleanOr("burrow_active",false)?0:r.unseen+1;double range=m.getAttributeValue(Attributes.FOLLOW_RANGE);
                if(!valid(m,t)||m.distanceToSqr(t)>range*range||r.unseen>60){m.setTarget(null);m.stopUsingItem();m.getNavigation().stop();ZombieGoals.restore(m);r.exits++;}}
        }
    }
    public void afterTick(Zombie m){
        if(!supported(m)||!ZombieState.known(m))return;
        // A controlling mob passenger can suppress the owner's serverAiStep.
        if(!enabled(m,FeatureId.ZOMBIE_BABY_RIDER))detachRider(m);
        if(m.isNoAi())beforeAi(m);
    }
    private void makeDoor(Zombie m){
        var r=ZombieState.runtime(m);Block material=material(m);
        for(int half=0;half<2;half++){
            var display=EntityTypes.BLOCK_DISPLAY.create(m.level(),EntitySpawnReason.COMMAND);if(display==null)continue;
            var output=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,m.registryAccess());
            var state=material.defaultBlockState().setValue(DoorBlock.FACING,net.minecraft.core.Direction.EAST)
                    .setValue(DoorBlock.HALF,half==0?net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER:net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER);
            output.store("block_state",net.minecraft.world.level.block.state.BlockState.CODEC,state);
            display.load(TagValueInput.create(ProblemReporter.DISCARDING,m.registryAccess(),output.buildResult()));
            display.setAttached(ZombieState.DISPLAY,true);display.setAttached(ZombieState.DISPLAY_LIVE,true);
            display.setAttached(ZombieState.DISPLAY_HALF,half);
            display.addTag("buildupmobtweaks:door_half_"+half);display.snapTo(m.position(),m.getYRot()-90,0);
            if(m.level().addFreshEntity(display)&&display.startRiding(m,true,false))r.displays.add(display);else display.discard();
        }
    }
    public boolean placeDisplay(Zombie m,Entity e,Entity.MoveFunction update){
        if(!supported(m)||!Boolean.TRUE.equals(e.getAttached(ZombieState.DISPLAY)))return false;
        double yaw=Math.toRadians(m.yBodyRot);var forward=new Vec3(-Math.sin(yaw),0,Math.cos(yaw));var side=new Vec3(Math.cos(yaw),0,Math.sin(yaw));
        var p=m.position().add(forward.scale(.65)).subtract(side.scale(.5));int half=e.getAttachedOrCreate(ZombieState.DISPLAY_HALF);
        update.accept(e,p.x,p.y+half,p.z);e.setYRot(m.yBodyRot-90);return true;
    }
    private void doorAttributes(Zombie m,boolean add){
        for(var holder:java.util.List.of(Attributes.KNOCKBACK_RESISTANCE,Attributes.MOVEMENT_SPEED)){
            var attr=m.getAttribute(holder);if(attr==null)continue;
            if(!add)attr.removeModifier(DOOR_MOD);else if(!attr.hasModifier(DOOR_MOD))attr.addTransientModifier(new AttributeModifier(DOOR_MOD,holder==Attributes.MOVEMENT_SPEED?-.05:.5,AttributeModifier.Operation.ADD_VALUE));
        }
    }
    public void clearDoor(Zombie m){for(var d:ZombieState.runtime(m).displays)d.discard();ZombieState.runtime(m).displays.clear();doorAttributes(m,false);}
    private void unloadDoor(Zombie m){for(var d:ZombieState.runtime(m).displays){d.setAttached(ZombieState.DISPLAY_LIVE,false);pending.add(d);}ZombieState.runtime(m).displays.clear();doorAttributes(m,false);}
    private void detachRider(Zombie m){String id=m.getAttached(ZombieState.DATA).getStringOr("rider","");for(var p:m.getPassengers())if(p.getUUID().toString().equals(id))p.stopRiding();}
    public boolean block(Zombie m,DamageSource source,float damage){
        if(!enabled(m,FeatureId.ZOMBIE_DOOR_GUARD)||ZombieState.runtime(m).displays.size()!=2||source.is(DamageTypeTags.BYPASSES_SHIELD)||damage<=0)return false;
        var pos=source.getSourcePosition();if(pos==null)return false;var direction=pos.subtract(m.position()).multiply(1,0,1).normalize();
        var forward=m.calculateViewVector(0,m.yBodyRot);if(direction.dot(forward)<Math.cos(Math.toRadians(10)))return false;
        var state=m.getAttached(ZombieState.DATA);float multiplier=material(m).defaultDestroyTime()/Blocks.OAK_DOOR.defaultDestroyTime();
        float health=Math.max(0,state.getFloatOr("door_health",0)-damage/Math.max(.1f,multiplier));state.putFloat("door_health",health);
        var r=ZombieState.runtime(m);r.blocks++;m.playSound(SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR,1,1);
        if(health<=0){r.breaks++;clearDoor(m);}return true;
    }
    private static Block material(Zombie m){
        var key=net.minecraft.resources.Identifier.tryParse(m.getAttached(ZombieState.DATA).getStringOr("material","minecraft:oak_door"));
        Block block=key==null?Blocks.OAK_DOOR:BuiltInRegistries.BLOCK.getValue(key);
        return block instanceof DoorBlock&&block.defaultDestroyTime()>0?block:Blocks.OAK_DOOR;
    }
    private static Block birthMaterial(Zombie m){
        int sea=m.level().getSeaLevel();
        if(m.getY()<sea*.25)return Blocks.COPPER_DOOR.weathering().oxidized();
        if(m.getY()<sea*.5)return Blocks.COPPER_DOOR.weathering().weathered();
        if(m.getY()<sea*.75)return Blocks.COPPER_DOOR.weathering().exposed();
        if(m.getY()<sea)return Blocks.COPPER_DOOR.weathering().unaffected();
        var type=VillagerType.byBiome(m.level().getBiome(m.blockPosition()));
        if(type.equals(VillagerType.DESERT)||type.equals(VillagerType.SAVANNA))return Blocks.ACACIA_DOOR;
        if(type.equals(VillagerType.SNOW)||type.equals(VillagerType.TAIGA))return Blocks.SPRUCE_DOOR;
        return type.equals(VillagerType.JUNGLE)?Blocks.JUNGLE_DOOR:Blocks.OAK_DOOR;
    }
}
