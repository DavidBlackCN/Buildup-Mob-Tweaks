/* Mob AI Tweaks adaptation: Copyright (c) 2024 N0t_UN_Owen, MIT. See NOTICE.md. */
package com.davidblackcn.buildupmobtweaks.rebuild.ability;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import com.davidblackcn.buildupmobtweaks.traits.core.TraitRuntime;
import com.davidblackcn.buildupmobtweaks.feature.*;
import com.davidblackcn.buildupmobtweaks.rebuild.remaining.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.entity.animal.golem.SnowGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.BreezeWindCharge;
import net.minecraft.world.item.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.sounds.SoundEvents;
import java.util.*;

public final class AbilityBehavior {
    private static AbilityBehavior instance;
    private final FeatureRegistry features;
    public static final TagKey<EntityType<?>> EXCLUDED=TagKey.create(Registries.ENTITY_TYPE,BuildupMobTweaks.id("ability_ai_excluded"));
    private static final net.minecraft.resources.Identifier ARMOR=BuildupMobTweaks.id("snow_pumpkin_armor");
    private final Set<Entity> pending=Collections.newSetFromMap(new IdentityHashMap<>());
    public AbilityBehavior(FeatureRegistry f){features=f;}
    public static AbilityBehavior instance(){return instance;}
    public static boolean skeleton(Mob m){return m.getType()==EntityTypes.SKELETON||m.getType()==EntityTypes.STRAY||m.getType()==EntityTypes.BOGGED||m.getType()==EntityTypes.WITHER_SKELETON||m.getType()==EntityTypes.PARCHED;}
    private static boolean supported(Mob m){return skeleton(m)||m.getType()==EntityTypes.SNOW_GOLEM||m.getType()==EntityTypes.BREEZE;}
    public boolean enabled(Mob m,FeatureId id){return AbilityState.known(m)&&features.isEnabled(id)&&TraitRuntime.permits(m,id)&&!m.getType().builtInRegistryHolder().is(EXCLUDED)
        &&!m.entityTags().contains("buildupmobtweaks:vanilla_ai")&&!m.entityTags().contains("buildupmobtweaks:disable_"+id.id().getPath());}
    public static boolean valid(Mob m,LivingEntity t){return m.isAlive()&&!m.isRemoved()&&!m.isNoAi()&&t!=null&&t!=m&&t.isAlive()&&!t.isRemoved()&&t.level()==m.level()
        &&(!(t instanceof Player p)||!p.isCreative()&&!p.isSpectator());}
    public void register(){instance=this;Objects.requireNonNull(AbilityState.DATA);
        ServerEntityEvents.ENTITY_LOAD.register((e,l)->{
            if(e instanceof Mob m&&supported(m)&&AbilityState.initialize(m)){if(skeleton(m)&&!m.getAttached(AbilityState.DATA).contains("baby_choice"))m.getAttached(AbilityState.DATA).putBoolean("baby_choice",false);boundary(m);}
            if(e instanceof BreezeWindCharge&&AbilityState.known(e)&&e.getAttached(AbilityState.DATA).getStringOr("role","").equals("shotgun"))pending.add(e);
        });
        ServerEntityEvents.ENTITY_UNLOAD.register((e,l)->{if(e instanceof Mob m&&supported(m)&&AbilityState.known(m)){pending.addAll(AbilityState.runtime(m).extras);AbilityState.runtime(m).extras.clear();if(m instanceof SnowGolem s)cancel(s);}pending.remove(e);});
        ServerTickEvents.END_SERVER_TICK.register(s->{for(var e:List.copyOf(pending))e.discard();pending.clear();});
        ServerLifecycleEvents.SERVER_STOPPED.register(s->pending.clear());
    }
    public void born(AbstractSkeleton m,EntitySpawnReason reason){if(!skeleton(m)||!AbilityState.initialize(m))return;var d=m.getAttached(AbilityState.DATA);if(d.contains("baby_choice"))return;
        boolean eligible=reason==EntitySpawnReason.NATURAL||reason==EntitySpawnReason.CHUNK_GENERATION||reason==EntitySpawnReason.SPAWN_ITEM_USE||reason==EntitySpawnReason.SPAWNER||reason==EntitySpawnReason.TRIAL_SPAWNER;
        d.putBoolean("baby_choice",eligible&&enabled(m,FeatureId.SKELETON_BABY_VARIANT)&&m.getRandom().nextInt(1000)<features.babySkeletonChance());
        m.setBaby(d.getBooleanOr("baby_choice",false)&&enabled(m,FeatureId.SKELETON_BABY_VARIANT));
    }
    public void boundary(Mob m){if(!(m.level() instanceof ServerLevel)||!supported(m)||!AbilityState.known(m))return;
        if(m instanceof AbstractSkeleton s){boolean baby=enabled(m,FeatureId.SKELETON_BABY_VARIANT)&&m.getAttached(AbilityState.DATA).getBooleanOr("baby_choice",false);if(s.isBaby()!=baby)s.setBaby(baby);}
        if(m instanceof SnowGolem s){s.setAttached(AbilityState.PUMPKIN_RETURN,enabled(s,FeatureId.SNOW_GOLEM_PUMPKIN_RETURN));armor(s);reconcile(s);var r=AbilityState.runtime(s);
            if((enabled(s,FeatureId.SNOW_GOLEM_RANGED_BEHAVIOR)||enabled(s,FeatureId.SNOW_GOLEM_THROW_VISUAL))&&s.getTarget()!=null&&!valid(s,s.getTarget()))s.setTarget(null);
            if(r.target!=null){boolean allowed=valid(s,r.target)&&s.getTarget()==r.parent&&valid(s,r.parent)&&s.hasLineOfSight(r.target)&&s.distanceToSqr(r.target)<=Math.pow(r.ranged?20:10,2)
                &&enabled(s,FeatureId.SNOW_GOLEM_RANGED_BEHAVIOR)==r.ranged&&enabled(s,FeatureId.SNOW_GOLEM_THROW_VISUAL)==r.visual;
                if(!allowed)cancel(s);else if(s.level().getGameTime()>=r.release){var target=r.target;r.target=null;r.parent=null;release(s,target,r.ranged);}}
            int time=s.getAttachedOrCreate(AbilityState.THROW);if(time>0)s.setAttached(AbilityState.THROW,time-1);
            if(!enabled(s,FeatureId.SNOW_GOLEM_THROW_VISUAL)||s.isNoAi()||!s.isAlive())s.setAttached(AbilityState.THROW,0);
        }
        if(m instanceof Breeze b){var r=AbilityState.runtime(b);var t=b.getBrain().getMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.ATTACK_TARGET).orElse(null);
            if(!enabled(b,FeatureId.BREEZE_SHOTGUN)||!valid(b,t)||t!=r.parent){for(var e:r.extras)e.discard();r.extras.clear();}else r.extras.removeIf(Entity::isRemoved);}
    }
    private void reconcile(SnowGolem s){var r=AbilityState.runtime(s);var goals=s.getGoalSelector();
        if(!r.installed){r.installed=true;for(var g:goals.getAvailableGoals())if(g.getGoal().getClass()==RangedAttackGoal.class){r.original=g.getGoal();break;}
            if(r.original!=null)r.replacement=new SnowAttackGoal(s,this);}
        boolean managed=enabled(s,FeatureId.SNOW_GOLEM_RANGED_BEHAVIOR);if(r.original==null||r.managed==managed)return;
        cancel(s);goals.removeGoal(r.managed?r.replacement:r.original);goals.addGoal(1,managed?r.replacement:r.original);r.managed=managed;
    }
    public void armor(SnowGolem s){var attr=s.getAttribute(Attributes.ARMOR);if(attr==null)return;
        if(s.hasPumpkin()&&enabled(s,FeatureId.SNOW_GOLEM_PUMPKIN_DEFENSE)){if(!attr.hasModifier(ARMOR))attr.addTransientModifier(new AttributeModifier(ARMOR,10,AttributeModifier.Operation.ADD_VALUE));}
        else attr.removeModifier(ARMOR);
    }
    public boolean returnPumpkin(SnowGolem s,Player p,InteractionHand hand){boolean allowed=s.level().isClientSide()?s.getAttachedOrCreate(AbilityState.PUMPKIN_RETURN):enabled(s,FeatureId.SNOW_GOLEM_PUMPKIN_RETURN);
        if(!allowed||s.hasPumpkin()||!p.getItemInHand(hand).is(Items.CARVED_PUMPKIN)||p.isSpectator())return false;
        if(!s.level().isClientSide()){p.getItemInHand(hand).consume(1,p);s.setPumpkin(true);s.gameEvent(GameEvent.EQUIP,p);}return true;
    }
    /** Invoked only by the native Goal's performRangedAttack dispatch. */
    public boolean prepare(SnowGolem s,LivingEntity t){boolean ranged=enabled(s,FeatureId.SNOW_GOLEM_RANGED_BEHAVIOR),visual=enabled(s,FeatureId.SNOW_GOLEM_THROW_VISUAL);
        if(!ranged&&!visual)return false;if(!valid(s,t)||!valid(s,s.getTarget())||!s.hasLineOfSight(t))return true;
        var r=AbilityState.runtime(s);if(r.target!=null)return true;r.prepared++;r.ranged=ranged;r.visual=visual;
        if(visual){s.setAttached(AbilityState.LEFT,!s.getAttachedOrCreate(AbilityState.LEFT));s.setAttached(AbilityState.THROW,10);r.target=t;r.parent=s.getTarget();r.release=s.level().getGameTime()+6;}
        else release(s,t,ranged);return true;
    }
    private void release(SnowGolem s,LivingEntity t,boolean ranged){var l=(ServerLevel)s.level();var ball=new Snowball(l,s,Items.SNOWBALL.getDefaultInstance());
        // A visual preparation has no world entity; only this one native projectile is created.
        if(enabled(s,FeatureId.SNOW_GOLEM_THROW_VISUAL)){var side=s.calculateViewVector(0,s.getYHeadRot()+(s.getAttachedOrCreate(AbilityState.LEFT)?-90:90));ball.setPos(s.getEyePosition().add(0,-.3,0).add(side.scale(.55)));}
        double x=t.getX()-ball.getX(),z=t.getZ()-ball.getZ(),y=t.getEyeY()-1.1-ball.getY()+Math.sqrt(x*x+z*z)*.2;
        Projectile.spawnProjectile(ball,l,Items.SNOWBALL.getDefaultInstance(),p->p.shoot(x,y,z,1.6f,ranged&&!s.hasPumpkin()?2:s.rangedAttackUncertainty(l)));
        s.playSound(SoundEvents.SNOW_GOLEM_SHOOT,1,1);AbilityState.runtime(s).released++;
    }
    public void cancel(SnowGolem s){var r=AbilityState.runtime(s);if(r.target!=null)r.cancelled++;r.target=null;r.parent=null;s.setAttached(AbilityState.THROW,0);}
    public void clear(Mob m){if(m instanceof SnowGolem s){cancel(s);var a=s.getAttribute(Attributes.ARMOR);if(a!=null)a.removeModifier(ARMOR);}for(var e:AbilityState.runtime(m).extras)e.discard();AbilityState.runtime(m).extras.clear();}
    /** Shared budget only participates while the newly approved shotgun is enabled. */
    public boolean claimBreezeSpecial(Breeze b){if(!enabled(b,FeatureId.BREEZE_SHOTGUN))return true;if(!RemainingState.known(b))return false;
        var d=b.getAttached(RemainingState.DATA);long now=b.level().getGameTime();if(now<d.getLongOr("breeze_special_ready",0))return false;d.putLong("breeze_special_ready",now+100);return true;}
    public void shotgun(Projectile original){if(!(original.getOwner() instanceof Breeze b)||!(b.level() instanceof ServerLevel l)||!enabled(b,FeatureId.BREEZE_SHOTGUN)||!RemainingState.known(b))return;
        var t=b.getBrain().getMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.ATTACK_TARGET).orElse(null);if(!valid(b,t)||!b.hasLineOfSight(t)||!claimBreezeSpecial(b))return;
        var r=AbilityState.runtime(b);r.volleys++;r.parent=t;
        for(double angle:new double[]{Math.toRadians(20),Math.toRadians(-20)}){var ball=new BreezeWindCharge(b,l);ball.setPos(original.position());var v=original.getDeltaMovement();
            // Vec3.yRot uses the native 65536-entry sine table; use exact rotation for the approved angle/speed budget.
            double c=Math.cos(angle),s=Math.sin(angle);ball.setDeltaMovement(new net.minecraft.world.phys.Vec3(v.x*c+v.z*s,v.y,v.z*c-v.x*s));
            var d=new net.minecraft.nbt.CompoundTag();d.putInt("version",1);d.putString("role","shotgun");d.putLong("expires",l.getGameTime()+100);ball.setAttached(AbilityState.DATA,d);
            // ENTITY_LOAD sees an extra as saved unless this spawn is explicitly recognized here.
            if(l.addFreshEntity(ball)){pending.remove(ball);r.extras.add(ball);r.extra++;}}
    }
    public boolean expireExtra(BreezeWindCharge p){if(!(p.level() instanceof ServerLevel)||!AbilityState.known(p)||!p.getAttached(AbilityState.DATA).getStringOr("role","").equals("shotgun"))return false;
        if(!(p.getOwner() instanceof Breeze b)||!enabled(b,FeatureId.BREEZE_SHOTGUN)||!valid(b,b.getBrain().getMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.ATTACK_TARGET).orElse(null))||b.getBrain().getMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.ATTACK_TARGET).orElse(null)!=AbilityState.runtime(b).parent||p.level().getGameTime()>=p.getAttached(AbilityState.DATA).getLongOr("expires",0)){p.discard();return true;}return false;
    }
}
