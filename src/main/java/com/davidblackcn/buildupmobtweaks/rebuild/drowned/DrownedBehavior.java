/* Mob AI Tweaks behavioral adaptation: Copyright (c) 2024 N0t_UN_Owen, MIT. See NOTICE.md. */
package com.davidblackcn.buildupmobtweaks.rebuild.drowned;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import com.davidblackcn.buildupmobtweaks.feature.*;
import com.davidblackcn.buildupmobtweaks.rebuild.zombie.ZombieBehavior;
import java.util.*;
import net.fabricmc.fabric.api.attachment.v1.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.zombie.Drowned;
import net.minecraft.world.entity.projectile.arrow.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.gamerules.GameRules;

/** A hand or a projectile owns the weapon. Persistent attachments contain UUID references only. */
public final class DrownedBehavior {
    public static final AttachmentType<CompoundTag> STATE=AttachmentRegistry.createPersistent(BuildupMobTweaks.id("drowned_rebuild"),CompoundTag.CODEC);
    public static final AttachmentType<CompoundTag> FLIGHT=AttachmentRegistry.createPersistent(BuildupMobTweaks.id("trident_rebuild_flight"),CompoundTag.CODEC);
    public static final AttachmentType<CompoundTag> OWNED=AttachmentRegistry.createPersistent(BuildupMobTweaks.id("trident_rebuild_owned"),CompoundTag.CODEC);
    private static final AttachmentType<Runtime> RUNTIME=AttachmentRegistry.createDefaulted(BuildupMobTweaks.id("drowned_rebuild_runtime"),Runtime::new);
    public static final class Runtime { public int throwsCount,hits,recoveries,releases,exits,unseen;public boolean installed;public LivingEntity remembered; }
    public static Runtime runtime(Drowned m){return m.getAttachedOrCreate(RUNTIME);}
    private static DrownedBehavior instance;private final FeatureRegistry features;
    public DrownedBehavior(FeatureRegistry f){features=f;}public static DrownedBehavior instance(){return instance;}
    public static boolean supported(Drowned m){return m.getType()==EntityTypes.DROWNED;}
    public boolean enabled(Drowned m,FeatureId id){return supported(m)&&known(m.getAttached(STATE))&&features.isEnabled(id)&&!m.getType().builtInRegistryHolder().is(ZombieBehavior.EXCLUDED)
            &&!m.entityTags().contains("buildupmobtweaks:vanilla_ai")&&!m.entityTags().contains("buildupmobtweaks:disable_"+id.id().getPath());}
    public static boolean known(CompoundTag d){return d!=null&&d.getIntOr("version",-1)==1;}
    private static UUID id(CompoundTag d,String key){if(!known(d))return null;try{return UUID.fromString(d.getStringOr(key,""));}catch(IllegalArgumentException invalid){return null;}}
    public void register(){
        instance=this;
        ServerEntityEvents.ENTITY_LOAD.register((e,l)->{if(e instanceof Drowned m&&supported(m)){
            if(!m.hasAttached(STATE)){var d=new CompoundTag();d.putInt("version",1);m.setAttached(STATE,d);}
            if(known(m.getAttached(STATE))&&!runtime(m).installed){runtime(m).installed=true;m.getGoalSelector().addGoal(0,new Recover(m));}
        }});
        ServerLivingEntityEvents.ALLOW_DEATH.register((e,s,a)->{if(e instanceof Drowned m)beforeDeath(m);return true;});
        ServerLivingEntityEvents.MOB_CONVERSION.register((old,next,c)->{if(old instanceof Drowned m&&supported(m)&&known(m.getAttached(STATE))){var p=projectile(m);if(p!=null)release(p);if(known(m.getAttached(FLIGHT)))m.removeAttached(FLIGHT);}});
        DrownedCommands.register();
    }
    public ThrownTrident projectile(Drowned m){var uuid=id(m.getAttached(FLIGHT),"projectile");
        if(uuid==null||!(m.level() instanceof ServerLevel l)||!(l.getEntity(uuid) instanceof ThrownTrident p))return null;
        var d=p.getAttached(OWNED);return known(d)&&!d.getBooleanOr("released",true)&&m.getUUID().equals(id(d,"owner"))?p:null;
    }
    public void beforeAi(Drowned m){
        if(!supported(m)||!known(m.getAttached(STATE)))return;var p=projectile(m);
        if(!enabled(m,FeatureId.DROWNED_TRIDENT_CONSERVATION)&&p!=null){if(((TridentAccess)p).buildup$ready()&&m.distanceToSqr(p)<1024&&m.getMainHandItem().isEmpty())transfer(m,p);else release(p);m.stopUsingItem();}
        var flight=m.getAttached(FLIGHT);if(known(flight)&&m.level().getGameTime()>=flight.getLongOr("deadline",Long.MAX_VALUE))m.removeAttached(FLIGHT);
        if(enabled(m,FeatureId.DROWNED_TRIDENT_CONSERVATION)&&m.getTarget()!=null){var r=runtime(m);var target=m.getTarget();if(r.remembered!=target){r.remembered=target;r.unseen=0;}
            r.unseen=m.hasLineOfSight(target)?0:r.unseen+1;double range=m.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.FOLLOW_RANGE);
            if(!ZombieBehavior.valid(m,target)||m.distanceToSqr(target)>range*range||r.unseen>60){m.setTarget(null);m.stopUsingItem();r.exits++;}}
    }
    public boolean throwHeld(Drowned m,LivingEntity target){
        if(!enabled(m,FeatureId.DROWNED_TRIDENT_CONSERVATION)||!(m.level() instanceof ServerLevel level)||!ZombieBehavior.valid(m,target))return false;
        var stack=m.getMainHandItem();if(m.hasAttached(FLIGHT)||!stack.is(Items.TRIDENT)||stack.getCount()!=1)return false;
        var p=new ThrownTrident(level,m,stack);var d=new CompoundTag();d.putInt("version",1);d.putString("owner",m.getUUID().toString());d.putBoolean("released",false);
        d.putLong("deadline",level.getGameTime()+BuildupMobTweaks.config().hostile.drowned.recoveryTimeout.get());d.putFloat("drop_chance",m.getDropChances().byEquipment(EquipmentSlot.MAINHAND));p.setAttached(OWNED,d);p.pickup=AbstractArrow.Pickup.DISALLOWED;
        d.putBoolean("pickup_allowed",enabled(m,FeatureId.DROWNED_TRIDENT_PLAYER_PICKUP));
        double dx=target.getX()-m.getX(),dz=target.getZ()-m.getZ();p.shoot(dx,target.getY(1.0/3)-p.getY()+Math.sqrt(dx*dx+dz*dz)*.2,dz,1.6f,m.rangedAttackUncertainty(level));
        var flight=new CompoundTag();flight.putInt("version",1);flight.putString("projectile",p.getUUID().toString());flight.putLong("deadline",d.getLongOr("deadline",0));
        m.setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);m.setAttached(FLIGHT,flight);m.stopUsingItem();
        if(!level.addFreshEntity(p)){m.removeAttached(FLIGHT);m.setItemSlot(EquipmentSlot.MAINHAND,stack);p.discard();return false;}
        p.applyOnProjectileSpawned(level,p.getPickupItemStackOrigin());m.playSound(net.minecraft.sounds.SoundEvents.DROWNED_SHOOT,1,1);runtime(m).throwsCount++;return true;
    }
    private void transfer(Drowned m,ThrownTrident p){
        var stack=p.getPickupItemStackOrigin();float chance=p.getAttached(OWNED).getFloatOr("drop_chance",0);p.discard();m.removeAttached(FLIGHT);
        m.setItemSlot(EquipmentSlot.MAINHAND,stack);m.setDropChance(EquipmentSlot.MAINHAND,chance);runtime(m).recoveries++;
    }
    public boolean recover(Drowned m,ThrownTrident p){
        if(!enabled(m,FeatureId.DROWNED_TRIDENT_CONSERVATION)||!enabled(m,FeatureId.DROWNED_TRIDENT_RECOVERY)||!m.isAlive()||!m.getMainHandItem().isEmpty()||projectile(m)!=p
                ||p.isRemoved()||!((TridentAccess)p).buildup$ready()||m.distanceToSqr(p)>2.25||!m.hasLineOfSight(p))return false;
        if(m.level().getGameTime()>=p.getAttached(OWNED).getLongOr("deadline",0)){release(p);return false;}transfer(m,p);return true;
    }
    private void beforeDeath(Drowned m){var p=projectile(m);if(p!=null){if(m.getMainHandItem().isEmpty())transfer(m,p);else release(p);}}
    public void tickProjectile(ThrownTrident p){
        var d=p.getAttached(OWNED);if(!known(d)||!(p.level() instanceof ServerLevel level))return;
        if(d.getBooleanOr("released",true)){
            p.pickup=d.getBooleanOr("player_pickup",false)&&features.isEnabled(FeatureId.DROWNED_TRIDENT_PLAYER_PICKUP)&&level.getGameRules().get(GameRules.MOB_DROPS)?AbstractArrow.Pickup.ALLOWED:AbstractArrow.Pickup.DISALLOWED;return;
        }
        var owner=id(d,"owner");var entity=owner==null?null:level.getEntity(owner);
        if(entity instanceof Drowned m){
            if(!m.isAlive()||projectile(m)!=p||!enabled(m,FeatureId.DROWNED_TRIDENT_CONSERVATION)){release(p);return;}
            if(recover(m,p))return;
        }
        if(level.getGameTime()>=d.getLongOr("deadline",Long.MAX_VALUE))release(p);
    }
    public void release(ThrownTrident p){
        var d=p.getAttached(OWNED);if(!known(d)||d.getBooleanOr("released",true)||!(p.level() instanceof ServerLevel l))return;
        d.putBoolean("released",true);var stack=p.getPickupItemStackOrigin();
        var owner=id(d,"owner");boolean ownerAllows=owner!=null&&l.getEntity(owner) instanceof Drowned m?enabled(m,FeatureId.DROWNED_TRIDENT_PLAYER_PICKUP):d.getBooleanOr("pickup_allowed",true);
        boolean pickup=ownerAllows&&features.isEnabled(FeatureId.DROWNED_TRIDENT_PLAYER_PICKUP)&&l.getGameRules().get(GameRules.MOB_DROPS)
                &&!EnchantmentHelper.has(stack,EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP)&&p.getRandom().nextFloat()<d.getFloatOr("drop_chance",0);
        d.putBoolean("player_pickup",pickup);p.pickup=pickup?AbstractArrow.Pickup.ALLOWED:AbstractArrow.Pickup.DISALLOWED;
        if(owner!=null&&l.getEntity(owner) instanceof Drowned m){if(p.getUUID().equals(id(m.getAttached(FLIGHT),"projectile")))m.removeAttached(FLIGHT);runtime(m).releases++;}
        p.setOwner((Entity)null);
    }
    public boolean seekingWater(Drowned m){var p=projectile(m);return p!=null&&p.isInWater()&&m.distanceToSqr(p)<=1024&&enabled(m,FeatureId.DROWNED_TRIDENT_CONSERVATION)&&enabled(m,FeatureId.DROWNED_TRIDENT_RECOVERY);}
    private final class Recover extends Goal {
        final Drowned mob;ThrownTrident p;long nextPath;
        Recover(Drowned m){mob=m;setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
        @Override public boolean canUse(){p=projectile(mob);return p!=null&&enabled(mob,FeatureId.DROWNED_TRIDENT_CONSERVATION)&&enabled(mob,FeatureId.DROWNED_TRIDENT_RECOVERY)
                &&mob.getMainHandItem().isEmpty()&&((TridentAccess)p).buildup$ready()&&mob.distanceToSqr(p)<=1024;}
        @Override public boolean canContinueToUse(){return canUse();}
        @Override public boolean requiresUpdateEveryTick(){return true;}
        @Override public void tick(){if(recover(mob,p))return;long now=mob.level().getGameTime();if(now>=nextPath){nextPath=now+10;mob.getNavigation().moveTo(p,1.25);}
            mob.getLookControl().setLookAt(p,60,60);if(mob.isSwimming()&&mob.hasLineOfSight(p))mob.push(p.position().subtract(mob.position()).normalize().scale(.04));}
        @Override public void stop(){mob.getNavigation().stop();}
    }
}
