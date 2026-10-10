/* Mob AI Tweaks trap/conversion adaptation: Copyright (c) 2024 N0t_UN_Owen, MIT. See NOTICE.md. */
package com.davidblackcn.buildupmobtweaks.rebuild.riding;
import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import com.davidblackcn.buildupmobtweaks.rebuild.skeleton.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.skeleton.*;
import net.minecraft.world.item.ItemStack;
public final class RidingEvents {
    public static boolean convert(LivingEntity e,DamageSource source){var b=RidingBehavior.instance();
        if(!(e instanceof Skeleton s)||e.getType()!=EntityTypes.SKELETON||b==null||!b.enabled(s,FeatureId.SKELETON_WITHER_CONVERSION)||!(s.level() instanceof ServerLevel))return false;
        if(!s.hasEffect(MobEffects.WITHER)&&!(source.getEntity() instanceof WitherBoss)&&!(source.getEntity() instanceof WitherSkeleton))return false;
        if(s.hasAttached(SkeletonState.DATA)&&!SkeletonState.known(s))return false;
        // ALLOW_DEATH has already restored the held bow and retired any generated reserve sword.
        var reserve=SkeletonState.reserve(s);float chance=SkeletonState.known(s)?s.getAttached(SkeletonState.DATA).getFloatOr("reserve_drop",.085f):.085f;
        var converted=s.convertTo(EntityTypes.WITHER_SKELETON,ConversionParams.single(s,true,true),to->{
            to.setHealth(to.getMaxHealth());to.getAttribute(Attributes.SCALE).setBaseValue(5.0/6.0);
            var data=s.getAttached(RidingState.DATA).copy();data.putString("converted_from",s.getUUID().toString());data.putFloat("reserve_drop",chance);to.setAttached(RidingState.DATA,data);
            if(s.hasAttached(RidingState.TRAP_ORIGIN))to.setAttached(RidingState.TRAP_ORIGIN,s.getAttached(RidingState.TRAP_ORIGIN));
            if(!reserve.isEmpty()&&!SkeletonState.generatedSword(reserve)){s.removeAttached(SkeletonState.RESERVE);to.setAttached(RidingState.RESERVE,reserve);}
        });return converted!=null;
    }
    /** Replace a not-yet-added native rider. Transfer the actual enchanted stacks, no extra spawn or drop. */
    public static Entity trap(Entity root,int index,Mob trapHorse){var b=RidingBehavior.instance();if(b==null)return root;
        root.setAttached(RidingState.TRAP_ORIGIN,trapHorse.getUUID().toString());trapHorse.setAttached(RidingState.TRAP_ORIGIN,trapHorse.getUUID().toString());
        for(var passenger:root.getPassengers())passenger.setAttached(RidingState.TRAP_ORIGIN,trapHorse.getUUID().toString());
        if(index==0||!b.gate(trapHorse,FeatureId.SKELETON_TRAP_VARIANTS))return root;
        if(!(root.getFirstPassenger() instanceof Skeleton original))return root;
        EntityType<? extends AbstractSkeleton> type=index==1?EntityTypes.WITHER_SKELETON:index==2?EntityTypes.STRAY:EntityTypes.BOGGED;
        var replacement=type.create(root.level(),EntitySpawnReason.TRIGGERED);if(replacement==null)return root;
        replacement.setAttached(RidingState.TRAP_ORIGIN,trapHorse.getUUID().toString());
        replacement.snapTo(original.position(),original.getYRot(),original.getXRot());replacement.setPersistenceRequired();replacement.setInvulnerableTime(60);
        for(var slot:EquipmentSlot.values()){var stack=original.getItemBySlot(slot);replacement.setItemSlot(slot,stack);replacement.setDropChance(slot,original.getDropChances().byEquipment(slot));original.setItemSlot(slot,ItemStack.EMPTY);}
        replacement.setCustomName(original.getCustomName());replacement.setCustomNameVisible(original.isCustomNameVisible());replacement.reassessWeaponGoal();
        original.stopRiding();if(!replacement.startRiding(root,true,false)){for(var slot:EquipmentSlot.values()){original.setItemSlot(slot,replacement.getItemBySlot(slot));replacement.setItemSlot(slot,ItemStack.EMPTY);}original.startRiding(root,true,false);replacement.discard();return root;}
        original.discard();return root;
    }
    private RidingEvents(){}
}
