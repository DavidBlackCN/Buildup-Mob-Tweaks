/* Mob AI Tweaks behavioral adaptation: Copyright (c) 2024 N0t_UN_Owen, MIT. See NOTICE.md. */
package com.davidblackcn.buildupmobtweaks.rebuild.skeleton;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import com.davidblackcn.buildupmobtweaks.feature.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.Vec3;

public final class SkeletonBehavior {
    private static SkeletonBehavior instance;
    private final FeatureRegistry features;
    public static final TagKey<EntityType<?>> EXCLUDED=TagKey.create(Registries.ENTITY_TYPE,BuildupMobTweaks.id("skeleton_ai_excluded"));
    public static final TagKey<Item> EXCLUDED_ITEMS=TagKey.create(Registries.ITEM,BuildupMobTweaks.id("ranged_items_excluded"));
    public SkeletonBehavior(FeatureRegistry features){this.features=features;}
    public static SkeletonBehavior instance(){return instance;}
    public static boolean supported(AbstractSkeleton mob){return mob.getType()==EntityTypes.SKELETON||mob.getType()==EntityTypes.STRAY||mob.getType()==EntityTypes.BOGGED;}
    public boolean enabled(AbstractSkeleton mob,FeatureId id){return supported(mob)&&SkeletonState.known(mob)&&features.isEnabled(id)
            && !mob.getType().builtInRegistryHolder().is(EXCLUDED)&&!mob.entityTags().contains("buildupmobtweaks:vanilla_ai")
            &&!mob.entityTags().contains("buildupmobtweaks:disable_"+id.id().getPath());}
    public boolean bow(AbstractSkeleton mob,ItemStack item){return !item.is(EXCLUDED_ITEMS)&&(item.is(Items.BOW)
            ||enabled(mob,FeatureId.SKELETON_BOW_COMPATIBILITY)&&item.getItem() instanceof BowItem);}
    public boolean hasBow(AbstractSkeleton mob){return bow(mob,mob.getMainHandItem())||bow(mob,mob.getOffhandItem());}
    public InteractionHand hand(AbstractSkeleton mob){return bow(mob,mob.getMainHandItem())?InteractionHand.MAIN_HAND:InteractionHand.OFF_HAND;}
    public static boolean melee(ItemStack item){return !item.is(EXCLUDED_ITEMS)&&(item.is(ItemTags.SWORDS)||item.is(ItemTags.AXES));}
    public static boolean valid(AbstractSkeleton mob,LivingEntity target){return target!=null&&target.isAlive()&&target.level()==mob.level()&&!mob.isAlliedTo(target)
            &&mob.canAttack(target)&&(!(target instanceof Player p)||!p.isCreative()&&!p.isSpectator());}
    public boolean sniper(AbstractSkeleton mob){return enabled(mob,FeatureId.SKELETON_SNIPING)&&hasBow(mob)&&!mob.isPassenger()
            &&(mob.isBaby()||!mob.level().canSeeSky(mob.blockPosition())||mob.level().isBrightOutside());}
    public int interval(AbstractSkeleton mob){var access=(SkeletonAccess)mob;int normal=access.buildup$attackInterval(false),hard=access.buildup$attackInterval(true);
        return mob.level().getDifficulty().getId()==3?hard:mob.level().getDifficulty().getId()==2?(normal+hard)/2:normal;}
    private boolean manages(AbstractSkeleton mob){
        for(var hand:InteractionHand.values()){var item=mob.getItemInHand(hand);
            if(item.is(EXCLUDED_ITEMS)||item.getItem() instanceof ProjectileWeaponItem&&!bow(mob,item))return false;}
        return enabled(mob,FeatureId.SKELETON_SNIPING)||enabled(mob,FeatureId.SKELETON_WEAPON_SWITCHING)
            ||enabled(mob,FeatureId.SKELETON_BOW_COMPATIBILITY)||enabled(mob,FeatureId.SKELETON_SAFE_STRAFING);
    }
    public void register(){
        instance=this;
        ServerEntityEvents.ENTITY_LOAD.register((e,l)->{
            if(e instanceof AreaEffectCloud&&Boolean.TRUE.equals(e.getAttached(SkeletonSpecificGoals.CLOUD))
                    &&!Boolean.TRUE.equals(e.getAttached(SkeletonSpecificGoals.CLOUD_LIVE)))e.discard();
            if(e instanceof AbstractSkeleton s&&supported(s))install(s);
        });
        ServerEntityEvents.ENTITY_UNLOAD.register((e,l)->{if(e instanceof AbstractSkeleton s&&supported(s)&&SkeletonState.known(s))cleanupSpecial(s);});
        // Fabric fires before the converted entity is added (and before ENTITY_LOAD installs Goals).
        ServerLivingEntityEvents.MOB_CONVERSION.register((old,converted,context)->{
            if(old instanceof AbstractSkeleton from&&converted instanceof AbstractSkeleton to
                    &&supported(from)&&supported(to)&&from.hasAttached(SkeletonState.DATA)){
                to.setAttached(SkeletonState.DATA,from.getAttached(SkeletonState.DATA).copy());
                var reserve=from.removeAttached(SkeletonState.RESERVE);
                if(reserve!=null)to.setAttached(SkeletonState.RESERVE,reserve);
                cleanupSpecial(from);
            }
        });
        ServerLivingEntityEvents.ALLOW_DEATH.register((e,s,a)->{if(e instanceof AbstractSkeleton m&&supported(m)&&SkeletonState.known(m)){restoreBow(m);cleanupSpecial(m);}return true;});
        ServerLivingEntityEvents.AFTER_DEATH.register((e,s)->{
            if(!(e instanceof AbstractSkeleton m)||!supported(m)||!SkeletonState.known(m)||!(m.level() instanceof ServerLevel level))return;
            ItemStack reserve=SkeletonState.reserve(m);m.removeAttached(SkeletonState.RESERVE);
            float chance=m.getAttached(SkeletonState.DATA).getFloatOr("reserve_drop",.085f);
            if(!reserve.isEmpty()&&!EnchantmentHelper.has(reserve,EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP)
                    &&level.getGameRules().get(GameRules.MOB_DROPS)&&m.getRandom().nextFloat()<chance)m.spawnAtLocation(level,reserve);
        });
        SkeletonCommands.register();
    }
    private void install(AbstractSkeleton mob){
        var rt=SkeletonState.runtime(mob);if(rt.installed)return;rt.installed=true;
        if(!mob.hasAttached(SkeletonState.DATA)){
            var state=new CompoundTag();state.putInt("version",1);state.putFloat("reserve_drop",.085f);mob.setAttached(SkeletonState.DATA,state);
            boolean fresh=!mob.isLoadedFromDisk()&&mob.spawnReason()!=null&&mob.spawnReason()!=EntitySpawnReason.LOAD
                    &&mob.spawnReason()!=EntitySpawnReason.CONVERSION&&mob.spawnReason()!=EntitySpawnReason.DIMENSION_TRAVEL;
            if(fresh&&enabled(mob,FeatureId.SKELETON_WEAPON_SWITCHING)&&mob.getMainHandItem().is(Items.BOW)&&!mob.hasAttached(SkeletonState.RESERVE))
                mob.setAttached(SkeletonState.RESERVE,new ItemStack(Items.WOODEN_SWORD));
        }
        if(!SkeletonState.known(mob))return;
        ((SkeletonAccess)mob).buildup$special(0);
        rt.attacks.add(new SkeletonGoals.Melee(mob,this));rt.attacks.add(new SkeletonGoals.Walk(mob,this));rt.attacks.add(new SkeletonGoals.Sniper(mob,this));
        mob.getGoalSelector().addGoal(0,new SkeletonGoals.Switch(mob,this));
        mob.getGoalSelector().addGoal(2,new SkeletonGoals.Shelter(mob,this));
        mob.getGoalSelector().addGoal(9,new SkeletonSpecificGoals(mob,this));
        beforeAi(mob);
    }
    public void beforeAi(AbstractSkeleton mob){
        if(!supported(mob)||!SkeletonState.known(mob))return;
        var rt=SkeletonState.runtime(mob);boolean managed=manages(mob);
        if(managed!=rt.managed){
            if(!managed){restoreBow(mob);for(var goal:rt.attacks)mob.getGoalSelector().removeGoal(goal);rt.managed=false;mob.reassessWeaponGoal();rt.mode="vanilla";}
            else{((SkeletonAccess)mob).buildup$removeOriginalAttacks();rt.managed=true;for(var goal:rt.attacks)mob.getGoalSelector().addGoal(4,goal);}
        }
        if(!enabled(mob,FeatureId.SKELETON_WEAPON_SWITCHING)||mob.getTarget()==null)restoreBow(mob);
        if(enabled(mob,FeatureId.SKELETON_TARGET_VALIDATION)){
            var target=mob.getTarget();if(target!=rt.remembered){rt.remembered=target;rt.unseen=0;}
            if(target!=null){rt.unseen=mob.hasLineOfSight(target)?0:rt.unseen+1;double range=mob.getAttributeValue(Attributes.FOLLOW_RANGE);
                if(!valid(mob,target)||mob.distanceToSqr(target)>range*range||rt.unseen>60){mob.setTarget(null);mob.stopUsingItem();mob.setAggressive(false);
                    mob.getNavigation().stop();mob.getMoveControl().strafe(0,0);restoreBow(mob);cleanupSpecial(mob);rt.exits++;}}
        }
        rt.clouds.removeIf(Entity::isRemoved);
        if(!enabled(mob,FeatureId.BOGGED_SPORE_RETREAT))for(var cloud:rt.clouds)cloud.discard();
    }
    public void swap(AbstractSkeleton mob){
        ItemStack incoming=SkeletonState.reserve(mob);if(incoming.isEmpty())return;
        ItemStack outgoing=mob.getMainHandItem();var state=mob.getAttached(SkeletonState.DATA);
        float chance=state.getFloatOr("reserve_drop",.085f);state.putFloat("reserve_drop",mob.getDropChances().byEquipment(EquipmentSlot.MAINHAND));
        mob.stopUsingItem();mob.removeAttached(SkeletonState.RESERVE);mob.setItemSlot(EquipmentSlot.MAINHAND,incoming);mob.setDropChance(EquipmentSlot.MAINHAND,chance);
        mob.setAttached(SkeletonState.RESERVE,outgoing);SkeletonState.runtime(mob).swaps++;
    }
    public void restoreBow(AbstractSkeleton mob){if(SkeletonState.reserve(mob).getItem() instanceof BowItem&&melee(mob.getMainHandItem()))swap(mob);}
    public void switchWeapon(AbstractSkeleton mob){
        if(!enabled(mob,FeatureId.SKELETON_WEAPON_SWITCHING)||!valid(mob,mob.getTarget())||mob.getOffhandItem().getItem() instanceof ProjectileWeaponItem)return;
        double distance=mob.distanceTo(mob.getTarget());var reserve=SkeletonState.reserve(mob);
        if(distance<3&&bow(mob,mob.getMainHandItem())&&melee(reserve)||distance>3&&melee(mob.getMainHandItem())&&bow(mob,reserve))swap(mob);
    }
    public static boolean safeStep(AbstractSkeleton mob,Vec3 delta){
        var dest=BlockPos.containing(mob.position().add(delta));return mob.level().hasChunkAt(dest)
                &&mob.level().getBlockState(dest.below()).isFaceSturdy(mob.level(),dest.below(),Direction.UP)
                &&mob.level().noCollision(mob,mob.getBoundingBox().move(delta));
    }
    public void cleanupSpecial(AbstractSkeleton mob){((SkeletonAccess)mob).buildup$special(0);for(var cloud:SkeletonState.runtime(mob).clouds)cloud.discard();SkeletonState.runtime(mob).clouds.clear();}
}
