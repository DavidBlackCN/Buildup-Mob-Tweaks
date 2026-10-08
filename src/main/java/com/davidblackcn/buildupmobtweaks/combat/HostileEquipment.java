package com.davidblackcn.buildupmobtweaks.combat;

import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import java.util.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.illager.Pillager;
import net.minecraft.world.entity.monster.zombie.*;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.*;
import net.minecraft.world.level.entity.EntityTypeTest;

/** Uses actual hand stacks; no hidden replacement weapon or recoverable bonus ammunition. */
public final class HostileEquipment {
    private HostileEquipment() {}
    public static boolean fletcher(Mob mob) { return mob instanceof ZombieVillager zombie && zombie.getVillagerData().profession().is(VillagerProfession.FLETCHER); }
    public static boolean fisherman(Mob mob) { return mob instanceof ZombieVillager zombie && zombie.getVillagerData().profession().is(VillagerProfession.FISHERMAN); }
    public static boolean cleric(Mob mob) { return mob instanceof ZombieVillager zombie && zombie.getVillagerData().profession().is(VillagerProfession.CLERIC); }
    public static boolean crossbowEnabled(Mob mob) {
        if(mob.getType().builtInRegistryHolder().is(S2Tags.RANGED_EXCLUDED)||mob.getMainHandItem().is(S2Tags.RANGED_ITEMS_EXCLUDED))return false;
        return mob instanceof ZombifiedPiglin ? EnvironmentCombat.on(mob,FeatureId.ZOMBIFIED_PIGLIN_CROSSBOW)
                : fletcher(mob) && EnvironmentCombat.on(mob,FeatureId.ZOMBIE_FLETCHER_CROSSBOW);
    }
    public static void install(Mob mob) {
        if(mob instanceof ZombieVillager || mob instanceof ZombifiedPiglin)mob.getGoalSelector().addGoal(1,crossbowGoal(mob));
        if(mob instanceof ZombieVillager || mob instanceof Drowned)mob.getGoalSelector().addGoal(1,rodGoal(mob));
        if(mob instanceof ZombieVillager)mob.getGoalSelector().addGoal(1,potionGoal(mob));
        if(mob instanceof Pillager)mob.getGoalSelector().addGoal(1,foodGoal(mob));
        if(!(mob instanceof ZombieVillager))return;
        var data=mob.getAttached(AdvancedHostiles.DATA);
        if(data==null || data.getIntOr("version",-1)!=1 || data.getBooleanOr("equipment_checked",false))return;
        data=data.copy();data.putBoolean("equipment_checked",true);mob.setAttached(AdvancedHostiles.DATA,data);
        if(!data.getStringOr("origin","").equals("rolled") || !mob.getMainHandItem().isEmpty())return;
        Item item=fletcher(mob)&&EnvironmentCombat.on(mob,FeatureId.ZOMBIE_FLETCHER_CROSSBOW)?Items.CROSSBOW
                :fisherman(mob)&&EnvironmentCombat.on(mob,FeatureId.ZOMBIE_FISHERMAN_ROD)?Items.FISHING_ROD:null;
        if(item!=null){mob.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(item));mob.setDropChance(EquipmentSlot.MAINHAND,0);}
    }
    public static Goal crossbowGoal(Mob mob){return new CrossbowGoal(mob);}
    public static Goal rodGoal(Mob mob){return new RodGoal(mob);}
    public static Goal potionGoal(Mob mob){return new PotionGoal(mob);}
    public static Goal foodGoal(Mob mob){return new FoodGoal(mob);}
    private static boolean target(Mob mob,int range){return SkeletonCombat.validTarget(mob,mob.getTarget()) && mob.distanceToSqr(mob.getTarget())<=range*range && mob.hasLineOfSight(mob.getTarget());}
    private static boolean rod(Mob mob){return mob.getMainHandItem().is(Items.FISHING_ROD) && (mob instanceof Drowned?EnvironmentCombat.on(mob,FeatureId.DROWNED_FISHING_PULL):fisherman(mob)&&EnvironmentCombat.on(mob,FeatureId.ZOMBIE_FISHERMAN_ROD));}
    private static abstract class HeldGoal extends Goal {
        final Mob mob; ItemStack held; LivingEntity victim; int ticks;
        HeldGoal(Mob mob){this.mob=mob;setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
        @Override public void start(){held=mob.getMainHandItem();victim=mob.getTarget();ticks=0;mob.getNavigation().stop();}
        @Override public boolean requiresUpdateEveryTick(){return true;}
        @Override public void stop(){if(mob.getUseItem()==held)mob.stopUsingItem();victim=null;}
        boolean sameTarget(){return victim==mob.getTarget() && target(mob,16) && held==mob.getMainHandItem() && !held.isEmpty();}
        void look(){mob.getNavigation().stop();mob.getLookControl().setLookAt(victim,30,30);}
    }
    private static final class CrossbowGoal extends HeldGoal {
        CrossbowGoal(Mob mob){super(mob);}
        @Override public boolean canUse(){return crossbowEnabled(mob) && mob.getMainHandItem().getItem() instanceof CrossbowItem && target(mob,16) && HostileCombat.instance().ready(mob,"zombie_crossbow");}
        @Override public void start(){super.start();mob.startUsingItem(InteractionHand.MAIN_HAND);HostileCombat.instance().reserve(mob,"zombie_crossbow",100);}
        @Override public boolean canContinueToUse(){return ticks<100 && crossbowEnabled(mob) && sameTarget();}
        @Override public void tick(){
            if(!canContinueToUse())return;look();ticks++;
            if(!CrossbowItem.isCharged(held) && mob.isUsingItem() && mob.getTicksUsingItem()>=CrossbowItem.getChargeDuration(held,mob))mob.releaseUsingItem();
            if(CrossbowItem.isCharged(held)){
                ((CrossbowItem)held.getItem()).performShooting(mob.level(),mob,InteractionHand.MAIN_HAND,held,1.4f,14,victim);ticks=100;
            }
        }
    }
    private static final class RodGoal extends HeldGoal {
        RodGoal(Mob mob){super(mob);}
        @Override public boolean canUse(){return rod(mob)&&target(mob,8)&&mob.distanceToSqr(mob.getTarget())>9 && HostileCombat.instance().ready(mob,"fishing_pull");}
        @Override public void start(){super.start();HostileCombat.instance().reserve(mob,"fishing_pull",160);mob.swingForAttack(InteractionHand.MAIN_HAND);}
        @Override public boolean canContinueToUse(){return ticks<20 && rod(mob)&&sameTarget()&&mob.distanceToSqr(victim)<=64;}
        @Override public void tick(){
            if(!canContinueToUse())return;look();ticks++;
            if(ticks%4==0)for(int i=1;i<=8;i++){var point=mob.getEyePosition().lerp(victim.getEyePosition(),i/8.0);((ServerLevel)mob.level()).sendParticles(ParticleTypes.CRIT,point.x,point.y,point.z,1,0,0,0,0);}
            if(ticks==20){var toward=mob.position().subtract(victim.position()).multiply(1,0,1).normalize().scale(.35);
                victim.setDeltaMovement(victim.getDeltaMovement().add(toward).add(0,.12,0));victim.syncVelocity=true;
                held.hurtAndBreak(1,mob,InteractionHand.MAIN_HAND);}
        }
    }
    private static final class PotionGoal extends Goal {
        private final Mob mob;private LivingEntity recipient;private int ticks;
        PotionGoal(Mob mob){this.mob=mob;setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
        @Override public boolean canUse(){
            if(!cleric(mob)||!EnvironmentCombat.on(mob,FeatureId.ZOMBIE_CLERIC_POTIONS)||!HostileCombat.instance().ready(mob,"cleric_potion"))return false;
            HostileCombat.instance().reserve(mob,"cleric_potion",80);
            recipient=target(mob,10)?mob.getTarget():null;
            if(recipient==null){var found=new ArrayList<Mob>();mob.level().getEntities(EntityTypeTest.forClass(Mob.class),mob.getBoundingBox().inflate(8),
                    other->other!=mob && other.isAlive()&&HostileCombat.eligible(other)&&other.isInvertedHealAndHarm()&&other.getHealth()<other.getMaxHealth()&&mob.hasLineOfSight(other),found,8);
                if(!found.isEmpty())recipient=found.getFirst();}
            return recipient!=null;
        }
        @Override public void start(){ticks=0;HostileCombat.instance().reserve(mob,"cleric_potion",160);}
        @Override public boolean canContinueToUse(){return ticks<20 && cleric(mob)&&EnvironmentCombat.on(mob,FeatureId.ZOMBIE_CLERIC_POTIONS)&&recipient!=null&&recipient.isAlive()&&recipient.level()==mob.level()&&mob.distanceToSqr(recipient)<144&&mob.hasLineOfSight(recipient);}
        @Override public boolean requiresUpdateEveryTick(){return true;}
        @Override public void tick(){if(!canContinueToUse())return;ticks++;mob.getNavigation().stop();mob.getLookControl().setLookAt(recipient,30,30);
            var stack=PotionContents.createItemStack(Items.SPLASH_POTION,Potions.HARMING);
            if(ticks%5==0)((ServerLevel)mob.level()).sendParticles(new ItemParticleOption(ParticleTypes.ITEM,ItemStackTemplate.fromNonEmptyStack(stack)),mob.getX(),mob.getEyeY(),mob.getZ(),2,.1,.1,.1,0);
            if(ticks==20){var potion=new ThrownSplashPotion(mob.level(),mob,stack);var delta=recipient.getEyePosition().subtract(mob.getEyePosition());potion.shoot(delta.x,delta.y+delta.horizontalDistance()*.2,delta.z,.75f,5);mob.level().addFreshEntity(potion);}
        }
    }
    private static final class FoodGoal extends Goal {
        private final Mob mob;private ItemStack food;private int ticks;
        FoodGoal(Mob mob){this.mob=mob;setFlags(EnumSet.of(Flag.MOVE));}
        @Override public boolean canUse(){food=mob.getOffhandItem();return EnvironmentCombat.on(mob,FeatureId.PILLAGER_FOOD_HEAL)&&mob.getTarget()==null&&mob.getHealth()<mob.getMaxHealth()
                && mob.tickCount-mob.getLastHurtByMobTimestamp()>100&&food.has(DataComponents.FOOD)&&food.has(DataComponents.CONSUMABLE)&&HostileCombat.instance().ready(mob,"food_heal");}
        @Override public void start(){ticks=0;mob.getNavigation().stop();mob.startUsingItem(InteractionHand.OFF_HAND);HostileCombat.instance().reserve(mob,"food_heal",200);}
        @Override public boolean canContinueToUse(){return ticks<80&&EnvironmentCombat.on(mob,FeatureId.PILLAGER_FOOD_HEAL)&&mob.getTarget()==null&&food==mob.getOffhandItem()&&!food.isEmpty()&&mob.tickCount-mob.getLastHurtByMobTimestamp()>100;}
        @Override public boolean requiresUpdateEveryTick(){return true;}
        @Override public void tick(){if(!canContinueToUse())return;ticks++;mob.getNavigation().stop();mob.stopUsingItem(); // Own slow consumption; vanilla must not finish the food early.
            if(ticks%10==0)((ServerLevel)mob.level()).sendParticles(new ItemParticleOption(ParticleTypes.ITEM,ItemStackTemplate.fromNonEmptyStack(food)),mob.getX(),mob.getEyeY(),mob.getZ(),2,.1,.1,.1,0);
            if(ticks==80){mob.setItemSlot(EquipmentSlot.OFFHAND,food.finishUsingItem(mob.level(),mob));mob.heal(2);}}
        @Override public void stop(){if(mob.getUseItem()==food)mob.stopUsingItem();}
    }
    public static void shieldBreak(Pillager mob){
        if(!EnvironmentCombat.on(mob,FeatureId.PILLAGER_SHIELD_BREAK)||!target(mob,3)||!(mob.getTarget() instanceof Player player)
                ||!player.isBlocking()||player.getTicksUsingItem()<60||!player.getUseItem().is(Items.SHIELD)||!mob.getMainHandItem().is(net.minecraft.tags.ItemTags.AXES)
                ||!HostileCombat.instance().ready(mob,"shield_break"))return;
        player.getCooldowns().addCooldown(player.getUseItem(),100);player.stopUsingItem();mob.swingForAttack(InteractionHand.MAIN_HAND);HostileCombat.instance().reserve(mob,"shield_break",200);
    }
}