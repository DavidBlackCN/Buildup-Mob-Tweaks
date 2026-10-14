/* Mob AI Tweaks Piglin adaptation: Copyright (c) 2024 N0t_UN_Owen, MIT. See NOTICE.md. */
package com.davidblackcn.buildupmobtweaks.rebuild.piglin;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import com.davidblackcn.buildupmobtweaks.traits.core.TraitRuntime;
import com.davidblackcn.buildupmobtweaks.traits.core.TraitPool;
import com.davidblackcn.buildupmobtweaks.feature.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import java.util.*;

public final class PiglinBehavior {
    public static final int DODGE_TICKS=10,DODGE_COOLDOWN=200;
    public static final TagKey<EntityType<?>> EXCLUDED=TagKey.create(Registries.ENTITY_TYPE,BuildupMobTweaks.id("piglin_ai_excluded"));
    public static final TagKey<Item> EXCLUDED_ITEMS=TagKey.create(Registries.ITEM,BuildupMobTweaks.id("ranged_items_excluded"));
    private static PiglinBehavior instance;
    private final FeatureRegistry features;
    private final Set<Piglin> loaded=Collections.newSetFromMap(new IdentityHashMap<>());
    public PiglinBehavior(FeatureRegistry features){this.features=features;}
    public static PiglinBehavior instance(){return instance;}
    public boolean enabled(Mob m,FeatureId id){return (m.getType()==EntityTypes.PIGLIN||m.getType()==EntityTypes.ZOMBIFIED_PIGLIN)&&features.isEnabled(id)&&TraitRuntime.permits(m,id)
            &&!m.getType().builtInRegistryHolder().is(EXCLUDED)&&!m.entityTags().contains("buildupmobtweaks:vanilla_ai")
            &&!m.entityTags().contains("buildupmobtweaks:disable_"+id.id().getPath())&&(!(m instanceof Piglin)||PiglinState.known(m));}
    public boolean compatible(Piglin m){return enabled(m,FeatureId.PIGLIN_CROSSBOW_COMPATIBILITY)&&!m.isNoAi();}
    public static boolean valid(Mob m,LivingEntity t){return t!=null&&t.isAlive()&&t.level()==m.level()&&!m.isAlliedTo(t)&&m.canAttack(t)
            &&(!(t instanceof Player p)||!p.isCreative()&&!p.isSpectator());}
    public static boolean crossbow(ItemStack s){return s.getItem() instanceof CrossbowItem&&!s.is(EXCLUDED_ITEMS);}
    /** Main hand takes precedence, matching the native weapon-hand helper. No item movement. */
    public static InteractionHand hand(Piglin m){if(crossbow(m.getMainHandItem()))return InteractionHand.MAIN_HAND;if(crossbow(m.getOffhandItem()))return InteractionHand.OFF_HAND;return null;}
    public static boolean inRange(Piglin m,LivingEntity t,int reduction){var hand=hand(m);return hand!=null&&valid(m,t)
            &&m.closerThan(t,Math.max(0,((CrossbowItem)m.getItemInHand(hand).getItem()).getDefaultProjectileRange()-reduction));}
    public void register(){
        instance=this;
        ServerEntityEvents.ENTITY_LOAD.register((e,l)->{if(e.getType()==EntityTypes.PIGLIN){var m=(Piglin)e;loaded.add(m);install(m);}});
        ServerEntityEvents.ENTITY_UNLOAD.register((e,l)->{if(e instanceof Piglin m&&loaded.remove(m))cancel(m);});
        ServerTickEvents.END_SERVER_TICK.register(s->{for(var m:List.copyOf(loaded))if(PiglinState.known(m))beforeAi(m);});
        ServerLifecycleEvents.SERVER_STOPPED.register(s->loaded.clear());
    }
    private void install(Piglin m){
        if(!m.hasAttached(PiglinState.DATA)){
            var d=new CompoundTag();d.putInt("version",1);d.putString("birth","none");m.setAttached(PiglinState.DATA,d);
            boolean fresh=!m.isLoadedFromDisk()&&m.spawnReason()!=null&&m.spawnReason()!=EntitySpawnReason.LOAD&&m.spawnReason()!=EntitySpawnReason.CONVERSION&&m.spawnReason()!=EntitySpawnReason.DIMENSION_TRAVEL;
            if(fresh&&!m.isBaby()&&enabled(m,FeatureId.PIGLIN_ITEM_DODGE)&&(TraitRuntime.unified(m)||TraitPool.legacySingle(m.getRandom(),features.majorChance(FeatureId.PIGLIN_ITEM_DODGE))))d.putString("birth","dodge");
            d.putLong("dodge_ready",m.level().getGameTime()+DODGE_COOLDOWN);
        }
        if(PiglinState.known(m)&&!m.getAttached(PiglinState.DATA).contains("dodge_ready"))m.getAttached(PiglinState.DATA).putLong("dodge_ready",m.level().getGameTime()+DODGE_COOLDOWN);
        if(!Objects.equals(m.getAttached(PiglinState.COMPAT),compatible(m)))m.setAttached(PiglinState.COMPAT,compatible(m));
    }
    public void beforeAi(Piglin m){
        if(!PiglinState.known(m))return;
        if(!Objects.equals(m.getAttached(PiglinState.COMPAT),compatible(m)))m.setAttached(PiglinState.COMPAT,compatible(m));
        var r=PiglinState.runtime(m);var t=m.getTarget();
        if(r.charging!=null&&(!compatible(m)||!m.isAlive()||!valid(m,t)||r.chargingTarget!=t||m.getUseItem()!=r.charging||m.getItemInHand(m.getUsedItemHand())!=r.charging||!crossbow(r.charging))){cancel(m);}
        if(r.dodgeUntil>0&&(m.level().getGameTime()>=r.dodgeUntil||m.isNoAi()||!m.isAlive()||!enabled(m,FeatureId.PIGLIN_ITEM_DODGE)||!valid(m,t)||r.dodgeTarget!=t)){r.dodgeUntil=0;r.dodgeTarget=null;}
    }
    public void cancel(Piglin m){var r=PiglinState.runtime(m);if(r.charging!=null){if(m.getUseItem()==r.charging)m.stopUsingItem();m.setChargingCrossbow(false);r.cancels++;}
        r.charging=null;r.chargingTarget=null;r.dodgeUntil=0;r.dodgeTarget=null;m.setAttached(PiglinState.COMPAT,false);}
    public void trackUse(Piglin m){var r=PiglinState.runtime(m);if(compatible(m)&&m.isUsingItem()&&crossbow(m.getUseItem())){r.charging=m.getUseItem();r.chargingTarget=m.getTarget();}else{r.charging=null;r.chargingTarget=null;}}
    public void stopUse(Piglin m){if(PiglinState.known(m)){var r=PiglinState.runtime(m);if(r.charging!=null||compatible(m))m.setChargingCrossbow(false);r.charging=null;r.chargingTarget=null;}}
    public void beforeShot(Piglin m){var r=PiglinState.runtime(m);r.shotHand=ProjectileUtil.getWeaponHoldingHand(m,Items.CROSSBOW);r.shotStack=m.getItemInHand(r.shotHand);r.shotCharged=crossbow(r.shotStack)&&CrossbowItem.isCharged(r.shotStack);}
    public void afterShot(Piglin m){var r=PiglinState.runtime(m);if(r.shotCharged&&r.shotStack!=null&&!CrossbowItem.isCharged(r.shotStack)){r.shots++;
        if(enabled(m,FeatureId.PIGLIN_SHOT_SWING)&&!m.isNoAi()){m.swingForAttack(r.shotHand);r.swings++;}}
        r.shotStack=null;r.shotCharged=false;r.shotHand=null;}
    public void afterAi(Piglin m){
        if(!PiglinState.known(m)||!enabled(m,FeatureId.PIGLIN_ITEM_DODGE)||m.isNoAi()||!m.isAlive()||m.isBaby()||m.isPassenger()||!m.onGround()||m.isInWater()||!m.isUsingItem()||!crossbow(m.getUseItem()))return;
        var d=m.getAttached(PiglinState.DATA);var t=m.getTarget();long now=m.level().getGameTime();
        if(!TraitRuntime.has(m,FeatureId.PIGLIN_ITEM_DODGE)||now<d.getLongOr("dodge_ready",Long.MAX_VALUE)||!valid(m,t)||m.distanceToSqr(t)>=4||!m.hasLineOfSight(t))return;
        var away=m.position().subtract(t.position()).multiply(1,0,1).normalize();if(away.lengthSqr()<.5)return;
        var impulse=away.scale(Math.min(.65,m.getAttributeValue(Attributes.MOVEMENT_SPEED)*2));
        if(!safe(m,impulse.scale(2))||!m.level().noCollision(m,m.getBoundingBox().move(0,1,0)))return;
        d.putLong("dodge_ready",now+DODGE_COOLDOWN);var r=PiglinState.runtime(m);r.dodges++;r.dodgeUntil=now+DODGE_TICKS;r.dodgeTarget=t;
        m.setDeltaMovement(impulse.add(0,.2,0));m.needsSync=true;
    }
    private static boolean safe(Piglin m,Vec3 delta){var p=BlockPos.containing(m.position().add(delta));var floor=p.below();var state=m.level().getBlockState(floor);
        return m.level().hasChunkAt(p)&&state.isFaceSturdy(m.level(),floor,Direction.UP)&&!state.is(Blocks.MAGMA_BLOCK)&&!state.is(Blocks.CAMPFIRE)&&!state.is(Blocks.SOUL_CAMPFIRE)
                &&!state.is(Blocks.FIRE)&&!state.is(Blocks.CACTUS)&&!m.level().getBlockState(p).is(Blocks.FIRE)&&!m.level().getBlockState(p).is(Blocks.SOUL_FIRE)
                &&m.level().getFluidState(p).isEmpty()&&m.level().getFluidState(floor).isEmpty()&&m.level().noCollision(m,m.getBoundingBox().move(delta));}
    public boolean refuse(Mob m,LivingEntity target){return m instanceof ZombifiedPiglin&&target instanceof ZombifiedPiglin&&enabled(m,FeatureId.ZOMBIFIED_PIGLIN_ANGER_FIX);}
    public void cleanAnger(ZombifiedPiglin m){
        if(!enabled(m,FeatureId.ZOMBIFIED_PIGLIN_ANGER_FIX))return;
        if(refuse(m,m.getTarget()))m.setTarget(null);
        var ref=m.getPersistentAngerTarget();var target=EntityReference.getLivingEntity(ref,m.level());
        if(target instanceof ZombifiedPiglin){m.setPersistentAngerTarget(null);m.setPersistentAngerEndTime(-1);}
    }
}
