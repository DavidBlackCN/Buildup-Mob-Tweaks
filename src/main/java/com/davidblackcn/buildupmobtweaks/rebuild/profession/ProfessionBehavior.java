/* Mob AI Tweaks adaptation: Copyright (c) 2024 N0t_UN_Owen, MIT. See NOTICE.md. */
package com.davidblackcn.buildupmobtweaks.rebuild.profession;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import com.davidblackcn.buildupmobtweaks.feature.*;
import com.davidblackcn.buildupmobtweaks.rebuild.drowned.DrownedBehavior;
import com.davidblackcn.buildupmobtweaks.rebuild.zombie.ZombieBehavior;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.zombie.*;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.*;
import net.minecraft.world.phys.Vec3;
import java.util.*;

public final class ProfessionBehavior {
    private static ProfessionBehavior instance;
    private final FeatureRegistry features;
    private final Set<FishingHook> pending=Collections.newSetFromMap(new IdentityHashMap<>());
    private final Set<ThrownSplashPotion> potions=Collections.newSetFromMap(new IdentityHashMap<>());
    public static final TagKey<EntityType<?>> EXCLUDED=TagKey.create(Registries.ENTITY_TYPE,BuildupMobTweaks.id("profession_ai_excluded"));
    public ProfessionBehavior(FeatureRegistry features){this.features=features;}
    public static ProfessionBehavior instance(){return instance;}
    public static boolean supported(Entity m){return m.getType()==EntityTypes.ZOMBIE_VILLAGER||m.getType()==EntityTypes.ZOMBIFIED_PIGLIN||m.getType()==EntityTypes.DROWNED;}
    public static boolean profession(ZombieVillager m,net.minecraft.resources.ResourceKey<VillagerProfession> p){return m.getVillagerData().profession().is(p);}
    public FeatureId crossbowFeature(Mob m){return m instanceof ZombifiedPiglin?FeatureId.ZOMBIFIED_PIGLIN_CROSSBOW:FeatureId.ZOMBIE_FLETCHER_CROSSBOW;}
    public FeatureId rodFeature(Mob m){return m instanceof Drowned?FeatureId.DROWNED_FISHING_PULL:FeatureId.ZOMBIE_FISHERMAN_ROD;}
    public boolean enabled(Mob m,FeatureId id){
        if(!supported(m)||!ProfessionState.known(m)||!features.isEnabled(id)||m.getType().builtInRegistryHolder().is(EXCLUDED)
                ||m.entityTags().contains("buildupmobtweaks:vanilla_ai")||m.entityTags().contains("buildupmobtweaks:disable_"+id.id().getPath()))return false;
        return switch(id){
            case ZOMBIFIED_PIGLIN_CROSSBOW -> m instanceof ZombifiedPiglin;
            case DROWNED_FISHING_PULL -> m instanceof Drowned;
            case ZOMBIE_FLETCHER_CROSSBOW -> m instanceof ZombieVillager z&&profession(z,VillagerProfession.FLETCHER);
            case ZOMBIE_FISHERMAN_ROD -> m instanceof ZombieVillager z&&profession(z,VillagerProfession.FISHERMAN);
            case ZOMBIE_CLERIC_POTIONS -> m instanceof ZombieVillager z&&profession(z,VillagerProfession.CLERIC);
            default -> false;
        };
    }
    public static InteractionHand hand(Mob m,Item item){return m.getMainHandItem().is(item)?InteractionHand.MAIN_HAND:m.getOffhandItem().is(item)?InteractionHand.OFF_HAND:null;}
    public static EquipmentSlot slot(InteractionHand h){return h==InteractionHand.MAIN_HAND?EquipmentSlot.MAINHAND:EquipmentSlot.OFFHAND;}
    private void initialize(Mob m){if(!m.hasAttached(ProfessionState.DATA)){
        var d=new CompoundTag();d.putInt("version",1);d.putString("birth","legacy_skipped");
        d.putLong("rod_ready",m.level().getGameTime()+60);d.putLong("potion_ready",m.level().getGameTime()+60);m.setAttached(ProfessionState.DATA,d);
    }}
    /** Only native fresh finalizeSpawn assigns a profession item; never ENTITY_LOAD. */
    public void born(ZombieVillager m,EntitySpawnReason reason){
        if(m.hasAttached(ProfessionState.DATA))return;initialize(m);var d=m.getAttached(ProfessionState.DATA);d.putString("birth","skipped_source");
        if(m.isLoadedFromDisk()||reason==EntitySpawnReason.LOAD||reason==EntitySpawnReason.CONVERSION||reason==EntitySpawnReason.DIMENSION_TRAVEL)return;
        d.putString("origin",reason.name());
        FeatureId id=profession(m,VillagerProfession.FLETCHER)?FeatureId.ZOMBIE_FLETCHER_CROSSBOW:
                profession(m,VillagerProfession.FISHERMAN)?FeatureId.ZOMBIE_FISHERMAN_ROD:profession(m,VillagerProfession.CLERIC)?FeatureId.ZOMBIE_CLERIC_POTIONS:null;
        if(id==null){d.putString("birth","no_profession");return;}
        if(!enabled(m,id)){d.putString("birth","disabled");return;}
        EquipmentSlot slot=m.getMainHandItem().isEmpty()?EquipmentSlot.MAINHAND:m.getOffhandItem().isEmpty()?EquipmentSlot.OFFHAND:null;
        if(slot==null){d.putString("birth","occupied");return;}
        var item=new ItemStack(id==FeatureId.ZOMBIE_FLETCHER_CROSSBOW?Items.CROSSBOW:id==FeatureId.ZOMBIE_FISHERMAN_ROD?Items.FISHING_ROD:Items.SPLASH_POTION);
        if(id==FeatureId.ZOMBIE_CLERIC_POTIONS)item.set(DataComponents.POTION_CONTENTS,new PotionContents(Potions.HARMING));
        m.setItemSlot(slot,item);d.putString("birth",id.id().getPath());d.putString("slot",slot.getSerializedName());
    }
    public void register(){
        instance=this;
        ServerEntityEvents.ENTITY_LOAD.register((e,l)->{
            if(e instanceof Mob m&&supported(m)){initialize(m);if(!ProfessionState.known(m))return;var r=ProfessionState.runtime(m);
                if(!r.installed){r.installed=true;
                    if(m instanceof ZombifiedPiglin||m instanceof ZombieVillager)m.getGoalSelector().addGoal(1,new ProfessionGoals.Crossbow(m,this));
                    if(m instanceof Drowned||m instanceof ZombieVillager)m.getGoalSelector().addGoal(1,new ProfessionGoals.Fishing(m,this));
                    if(m instanceof ZombieVillager)m.getGoalSelector().addGoal(1,new ProfessionGoals.Cleric(m,this));
                }tick(m);
            }
            if(e instanceof ThrownSplashPotion p&&ProfessionState.known(p.getAttached(ProfessionState.POTION)))potions.add(p);
        });
        ServerEntityEvents.ENTITY_UNLOAD.register((e,l)->{if(e instanceof Mob m&&supported(m)&&ProfessionState.known(m))cancel(m);});
        ServerLivingEntityEvents.ALLOW_DEATH.register((e,s,a)->{if(e instanceof Mob m&&supported(m)&&ProfessionState.known(m))cancel(m);return true;});
        ServerLivingEntityEvents.MOB_CONVERSION.register((old,m,c)->{if(supported(m)&&old.hasAttached(ProfessionState.DATA))m.setAttached(ProfessionState.DATA,old.getAttached(ProfessionState.DATA).copy());});
        ServerTickEvents.END_SERVER_TICK.register(server->{var batch=List.copyOf(pending);pending.clear();for(var h:batch)if(!h.isRemoved())h.discard();
            for(var p:List.copyOf(potions)){if(p.isRemoved()){potions.remove(p);continue;}var d=p.getAttached(ProfessionState.POTION);
                if(ProfessionState.known(d)&&p.level().getGameTime()>=d.getLongOr("expires",0)){p.discard();potions.remove(p);}}
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(s->{pending.clear();potions.clear();});
    }
    public boolean activeValid(Mob m){var r=ProfessionState.runtime(m);return m.isAlive()&&!m.isRemoved()&&!m.isNoAi()&&r.active!=null&&enabled(m,r.active)
            &&r.hand!=null&&m.getItemInHand(r.hand)==r.stack&&!r.stack.isEmpty()
            &&(r.active==FeatureId.ZOMBIE_CLERIC_POTIONS?healable(m,r.target):ZombieBehavior.valid(m,r.target)&&m.getTarget()==r.target)
            &&(!(m instanceof ZombieVillager z)||!z.isConverting());}
    public void tick(Mob m){if(!supported(m)||!ProfessionState.known(m))return;
        var r=ProfessionState.runtime(m);if(!r.phase.equals("idle")&&!activeValid(m))cancel(m);
        sync(m,ProfessionState.CROSSBOW_VISUAL,m.isAlive()&&!m.isNoAi()&&enabled(m,crossbowFeature(m))&&hand(m,Items.CROSSBOW)!=null);
        sync(m,ProfessionState.ROD_VISUAL,m.isAlive()&&!m.isNoAi()&&enabled(m,rodFeature(m))&&hand(m,Items.FISHING_ROD)!=null);
        if(m.getTarget()!=null&&!ZombieBehavior.valid(m,m.getTarget())&&(enabled(m,crossbowFeature(m))||enabled(m,rodFeature(m))))m.setTarget(null);
    }
    private static void sync(Mob m,AttachmentType<Boolean> type,boolean v){if(m.getAttachedOrCreate(type)!=v)m.setAttached(type,v);}
    public void begin(Mob m,FeatureId id,String phase,InteractionHand hand,LivingEntity target){var r=ProfessionState.runtime(m);r.active=id;r.phase=phase;r.hand=hand;r.stack=m.getItemInHand(hand);r.target=target;m.setAggressive(true);}
    public void cancel(Mob m){var r=ProfessionState.runtime(m);if(r.phase.equals("idle")&&r.hook==null)return;
        if(m.isUsingItem()&&m.getUseItem()==r.stack)m.stopUsingItem();
        if(r.hook!=null){pending.add(r.hook);r.hook=null;}
        r.phase="idle";r.active=null;r.stack=null;r.target=null;r.hand=null;r.cancels++;m.setAggressive(false);
    }
    public static boolean healable(Mob m,LivingEntity t){return t instanceof Monster hostile&&t!=m&&t.isAlive()&&t.level()==m.level()&&t.isInvertedHealAndHarm()
            &&t.getHealth()<t.getMaxHealth()&&m.getTarget()!=t&&hostile.getTarget()!=m;}
    public static boolean mobOwner(FishingHook h){return h.getOwner() instanceof Mob m&&(m.getType()==EntityTypes.DROWNED||m.getType()==EntityTypes.ZOMBIE_VILLAGER);}
    public static boolean mobHook(FishingHook h){return mobOwner(h)&&Boolean.TRUE.equals(h.getAttached(ProfessionState.HOOK));}
    public static FishingHook hook(Mob m){var h=ProfessionState.runtime(m).hook;return h!=null&&!h.isRemoved()&&h.getOwner()==m?h:null;}
    public static void ownerChanged(FishingHook h){if(mobHook(h))ProfessionState.runtime(h.getOwner()).hook=h;}
    public boolean canFish(Mob m){return enabled(m,rodFeature(m))&&hand(m,Items.FISHING_ROD)!=null
            &&(!(m instanceof Drowned d)||DrownedBehavior.instance()==null||DrownedBehavior.instance().projectile(d)==null);}
    public void tickHook(FishingHook h){
        if(!(h.getOwner() instanceof Mob m))return;var r=ProfessionState.runtime(m);
        if(h.level().isClientSide()){
            r.hook=h;
            // Owner/attachment packets can arrive in separate tracking updates; server removal is authoritative.
            if(!m.isAlive()||hand(m,Items.FISHING_ROD)==null){h.discard();return;}
        }else if(!ProfessionState.known(m)||r.hook!=h||!r.phase.equals("fishing")||!activeValid(m)||!canFish(m)||m.distanceToSqr(h)>1024){h.discard();if(ProfessionState.known(m))cancel(m);return;}
        var hit=h.getHookedIn();
        if(hit!=null&&!hit.isRemoved()){h.setPos(hit.getX(),hit.getY(.8),hit.getZ());h.setDeltaMovement(Vec3.ZERO);}
        else {if(!h.level().isClientSide())((FishingAccess)h).buildup$collision();h.move(MoverType.SELF,h.getDeltaMovement());
            h.setDeltaMovement(h.getDeltaMovement().add(0,h.isInWater()?0:-.03,0).scale(.92));}
        if(!h.level().isClientSide()&&h.tickCount>=58){
            if(hit==r.target&&ZombieBehavior.valid(m,r.target)&&m.hasLineOfSight(r.target)){((FishingAccess)h).buildup$pull(hit);h.level().broadcastEntityEvent(h,(byte)31);r.pulls++;}
            r.stack.hurtAndBreak(3,m,slot(r.hand));m.swingForAttack(r.hand);h.discard();cancel(m);
        }
    }
    public void track(ThrownSplashPotion p){potions.add(p);}
    public List<LivingEntity> potionTargets(ThrownSplashPotion p,List<LivingEntity> candidates){var d=p.getAttached(ProfessionState.POTION);
        if(!ProfessionState.known(d))return candidates;
        if(!(p.getOwner() instanceof Mob m)||!m.getUUID().toString().equals(d.getStringOr("owner",""))||!m.isAlive()||!enabled(m,FeatureId.ZOMBIE_CLERIC_POTIONS)||p.level().getGameTime()>=d.getLongOr("expires",0))return List.of();
        return candidates.stream().filter(t->t.getUUID().toString().equals(d.getStringOr("target",""))&&healable(m,t)).toList();
    }
}
