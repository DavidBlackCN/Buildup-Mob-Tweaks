package com.davidblackcn.buildupmobtweaks.combat;
import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import java.util.ArrayList;
import net.fabricmc.fabric.api.attachment.v1.*;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.illager.AbstractIllager;
import net.minecraft.world.entity.monster.illager.Illusioner;
import net.minecraft.world.entity.monster.zombie.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.item.*;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.Vec3;

public final class IllagerRelations {
    public static final AttachmentType<CompoundTag> OMEN=AttachmentRegistry.createPersistent(BuildupMobTweaks.id("omen_encounter"),CompoundTag.CODEC);
    private IllagerRelations(){}
    public static void register(){ServerLivingEntityEvents.AFTER_DAMAGE.register((victim,source,base,taken,blocked)->{
        if(victim instanceof Animal && source.getEntity() instanceof ServerPlayer player && taken>0)omenAttack(player);
    });}
    public static void install(Mob mob){
        if(!(mob instanceof AbstractIllager raider))return;
        var data=mob.getAttached(AdvancedHostiles.DATA);
        if(data==null||data.getIntOr("version",-1)!=1||data.getBooleanOr("boat_checked",false))return;
        data=data.copy();data.putBoolean("boat_checked",true);mob.setAttached(AdvancedHostiles.DATA,data);
        if(!data.getStringOr("origin","").equals("rolled")||mob.isPassenger()||mob.isNoAi()||!EnvironmentCombat.on(mob,FeatureId.ILLAGER_BOATS)
                || !(raider.hasActiveRaid()||mob.spawnReason()==EntitySpawnReason.PATROL||mob.spawnReason()==EntitySpawnReason.EVENT)
                || !mob.isInWater() || !mob.level().getFluidState(mob.blockPosition().below(2)).is(FluidTags.WATER))return;
        var boat=EntityTypes.OAK_BOAT.create(mob.level(),EntitySpawnReason.MOB_SUMMONED);if(boat==null)return;
        boat.snapTo(mob.position(),mob.getYRot(),0);
        if(!mob.level().noCollision(boat,boat.getBoundingBox())||!mob.level().addFreshEntity(boat))return;
        if(mob.startRiding(boat))data.putString("boat",boat.getStringUUID());else boat.discard();
    }
    public static void tick(Mob mob){
        if(mob instanceof AbstractIllager illager){
            if(mob.getVehicle() instanceof AbstractBoat boat && boat.getFirstPassenger()==mob && EnvironmentCombat.on(mob,FeatureId.ILLAGER_BOATS)
                    && boat.getStringUUID().equals(mob.getAttached(AdvancedHostiles.DATA).getStringOr("boat",""))){
                Vec3 destination=SkeletonCombat.validTarget(mob,mob.getTarget())?mob.getTarget().position():illager.hasPatrolTarget()?Vec3.atCenterOf(illager.getPatrolTarget()):null;
                if(destination!=null){var move=destination.subtract(boat.position()).multiply(1,0,1).normalize().scale(.08);boat.setDeltaMovement(move.add(0,boat.getDeltaMovement().y,0));boat.syncVelocity=true;}
            }
            if((mob.tickCount+mob.getId())%40==0)chooseTarget(mob);
        }else if(mob instanceof Zombie && mob.getType()==EntityTypes.ZOMBIE && EnvironmentCombat.on(mob,FeatureId.ILLAGER_ZOMBIE_CONFLICT)
                && !mob.getType().builtInRegistryHolder().is(S2Tags.ILLAGER_EXCLUDED) && mob.getTarget()==null && (mob.tickCount+mob.getId())%40==0){
            var found=new ArrayList<AbstractIllager>();mob.level().getEntities(EntityTypeTest.forClass(AbstractIllager.class),mob.getBoundingBox().inflate(12),
                    other->SkeletonCombat.validTarget(mob,other)&&mob.hasLineOfSight(other),found,8);if(!found.isEmpty())mob.setTarget(found.getFirst());
        }
    }
    public static void chooseTarget(Mob mob){
        if(!EnvironmentCombat.on(mob,FeatureId.ILLAGER_ZOMBIE_CONFLICT)&&!EnvironmentCombat.on(mob,FeatureId.ILLAGER_ZOMBIE_VILLAGER))return;
        if(mob.getTarget()!=null && !(mob.getTarget() instanceof Zombie))return;
        var players=new ArrayList<Player>();mob.level().getEntities(EntityTypeTest.forClass(Player.class),mob.getBoundingBox().inflate(16),
                other->SkeletonCombat.validTarget(mob,other)&&mob.hasLineOfSight(other),players,8);
        if(!players.isEmpty()){mob.setTarget(players.getFirst());return;}
        if(SkeletonCombat.validTarget(mob,mob.getTarget()))return;
        var zombies=new ArrayList<Zombie>();mob.level().getEntities(EntityTypeTest.forClass(Zombie.class),mob.getBoundingBox().inflate(12),
                other->!other.getType().builtInRegistryHolder().is(S2Tags.ILLAGER_EXCLUDED)&&SkeletonCombat.validTarget(mob,other)&&mob.hasLineOfSight(other)
                        && (other instanceof ZombieVillager?EnvironmentCombat.on(mob,FeatureId.ILLAGER_ZOMBIE_VILLAGER):other.getType()==EntityTypes.ZOMBIE&&EnvironmentCombat.on(mob,FeatureId.ILLAGER_ZOMBIE_CONFLICT)),zombies,8);
        if(!zombies.isEmpty())mob.setTarget(zombies.getFirst());
    }
    public static void omenAttack(ServerPlayer player){
        if(HostileCombat.instance()==null||!HostileCombat.instance().gate(FeatureId.ILLUSIONER_OMEN_SPAWN)||!SkeletonExtras.omen(player)
                ||player.isCreative()||player.isSpectator()||player.level().getDifficulty()==Difficulty.PEACEFUL)return;
        var data=player.getAttached(OMEN);if(data==null){data=new CompoundTag();data.putInt("version",1);}
        if(data.getIntOr("version",-1)!=1||player.level().getGameTime()<data.getLongOr("next",0))return;
        data=data.copy();int attempts=Math.min(10,data.getIntOr("attempts",0)+1);data.putInt("attempts",attempts);data.putLong("next",player.level().getGameTime()+40);player.setAttached(OMEN,data);
        if(player.getRandom().nextInt(100)<attempts && spawnEncounter(player)){data.putInt("attempts",0);data.putLong("next",player.level().getGameTime()+1200);}
    }
    public static boolean spawnEncounter(ServerPlayer player){
        var level=player.level();var found=new ArrayList<Illusioner>();level.getEntities(EntityTypeTest.forClass(Illusioner.class),player.getBoundingBox().inflate(32),Entity::isAlive,found,1);if(!found.isEmpty())return false;
        for(int i=0;i<8;i++){
            double angle=i*Math.PI/4;var pos=player.blockPosition().offset((int)Math.round(Math.cos(angle)*6),0,(int)Math.round(Math.sin(angle)*6));
            if(!level.hasChunkAt(pos)||!level.getBlockState(pos.below()).isFaceSturdy(level,pos.below(),Direction.UP)||!level.getFluidState(pos).isEmpty())continue;
            var mob=EntityTypes.ILLUSIONER.create(level,EntitySpawnReason.MOB_SUMMONED);if(mob==null)return false;mob.snapTo(pos,0,0);
            if(!level.noCollision(mob,mob.getBoundingBox()))continue;
            mob.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.BOW));mob.setDropChance(EquipmentSlot.MAINHAND,0);mob.setTarget(player);
            if(!level.addFreshEntity(mob))return false;level.sendParticles(ParticleTypes.PORTAL,mob.getX(),mob.getY()+1,mob.getZ(),20,.5,1,.5,.05);return true;
        }
        return false;
    }
}