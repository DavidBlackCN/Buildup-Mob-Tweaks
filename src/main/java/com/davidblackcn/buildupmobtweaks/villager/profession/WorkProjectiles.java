package com.davidblackcn.buildupmobtweaks.villager.profession;
import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import net.fabricmc.fabric.api.attachment.v1.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.npc.villager.Villager;
import java.util.*;

/** Provenance belongs to the projectile, not zombie combat data or Trait storage. */
public final class WorkProjectiles {
    public static final AttachmentType<Boolean> HOOK=AttachmentRegistry.create(BuildupMobTweaks.id("villager_fishing_hook"),b->b.initializer(()->false).syncWith(ByteBufCodecs.BOOL,AttachmentSyncPredicate.all()));
    public static final AttachmentType<CompoundTag> POTION=AttachmentRegistry.createPersistent(BuildupMobTweaks.id("villager_healing_potion"),CompoundTag.CODEC);
    public static final AttachmentType<CompoundTag> TRAINING=AttachmentRegistry.createPersistent(BuildupMobTweaks.id("villager_training_arrow"),CompoundTag.CODEC);
    public static void mark(Entity p,AttachmentType<CompoundTag> type,Villager owner,Entity target,int lifetime){
        var d=new CompoundTag();d.putInt("version",1);d.putString("owner",owner.getUUID().toString());d.putString("target",target==null?"":target.getUUID().toString());d.putLong("expires",ProfessionWorkState.now(owner)+lifetime);p.setAttached(type,d);
    }
    public static boolean valid(Entity p,CompoundTag d){
        if(!(p.level() instanceof ServerLevel l)||d==null||d.getIntOr("version",-1)!=1||ProfessionWorkState.now(p)>=d.getLongOr("expires",-1))return false;
        if(!(p instanceof net.minecraft.world.entity.projectile.Projectile projectile)||!(projectile.getOwner() instanceof Villager owner)||!owner.getUUID().toString().equals(d.getStringOr("owner","")))return false;
        var r=ProfessionWorkState.peek(owner).orElse(null);
        return owner.isAlive()&&r!=null&&r.projectile==p&&r.active!=null&&ProfessionModule.allowed(owner,l,r.context.profile(),r.meal);
    }
    public static List<LivingEntity> patients(Entity p,List<LivingEntity> original){
        if(!p.hasAttached(POTION))return original;var d=p.getAttached(POTION);if(!valid(p,d))return List.of();
        return original.stream().filter(e->e instanceof Villager&&e.isAlive()&&!e.isInvertedHealAndHarm()&&e.getUUID().toString().equals(d.getStringOr("target",""))).toList();
    }
    public static boolean owned(Villager owner,Entity p){
        if(!(p instanceof net.minecraft.world.entity.projectile.Projectile projectile)||projectile.getOwner()!=owner)return false;
        if(p instanceof net.minecraft.world.entity.projectile.FishingHook h&&NpcFishingHooks.marked(h))return true;
        for(var type:List.of(POTION,TRAINING))if(p.hasAttached(type)){var d=p.getAttached(type);if(d.getIntOr("version",-1)==1&&d.getStringOr("owner","").equals(owner.getUUID().toString()))return true;}
        return false;
    }
    public static void bootstrap(){}
    private WorkProjectiles(){}
}
