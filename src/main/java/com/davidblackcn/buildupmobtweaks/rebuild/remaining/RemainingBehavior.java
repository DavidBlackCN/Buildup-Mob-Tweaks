/* Mob AI Tweaks adaptation: Copyright (c) 2024 N0t_UN_Owen, MIT. See NOTICE.md. */
package com.davidblackcn.buildupmobtweaks.rebuild.remaining;
import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import com.davidblackcn.buildupmobtweaks.feature.*;
import com.davidblackcn.buildupmobtweaks.rebuild.zombie.ZombieBehavior;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.projectile.hurtingprojectile.*;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.CompoundTag;
public final class RemainingBehavior {
    private final java.util.Set<SmallFireball> pending=java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
    private static RemainingBehavior instance;private final FeatureRegistry features;
    public static final TagKey<EntityType<?>> EXCLUDED=TagKey.create(Registries.ENTITY_TYPE,BuildupMobTweaks.id("remaining_ai_excluded"));
    private static final net.minecraft.resources.Identifier SPEED=BuildupMobTweaks.id("enderman_rebuild_balance");
    public RemainingBehavior(FeatureRegistry f){features=f;}public static RemainingBehavior instance(){return instance;}
    public static boolean supported(Mob m){return m.getType()==EntityTypes.GHAST||m.getType()==EntityTypes.BLAZE||m.getType()==EntityTypes.SILVERFISH||m.getType()==EntityTypes.ENDERMAN||m.getType()==EntityTypes.CREEPER||m.getType()==EntityTypes.SPIDER||m.getType()==EntityTypes.CAVE_SPIDER||m.getType()==EntityTypes.BREEZE;}
    public boolean enabled(Mob m,FeatureId id){return supported(m)&&RemainingState.known(m)&&features.isEnabled(id)&&!m.getType().builtInRegistryHolder().is(EXCLUDED)&&!m.entityTags().contains("buildupmobtweaks:vanilla_ai")&&!m.entityTags().contains("buildupmobtweaks:disable_"+id.id().getPath());}
    public int comboCooldown(){return features.specialCooldown();}
    public boolean combo(Enderman m){return enabled(m,FeatureId.ENDERMAN_COMBO)&&m.getAttached(RemainingState.DATA).getStringOr("birth","none").equals("combo");}
    public void born(Mob m,EntitySpawnReason reason){if(!supported(m)||m.hasAttached(RemainingState.DATA))return;RemainingState.initialize(m);
        if(m instanceof Enderman&&enabled(m,FeatureId.ENDERMAN_COMBO)&&(reason==EntitySpawnReason.NATURAL||reason==EntitySpawnReason.SPAWN_ITEM_USE)&&m.getRandom().nextInt(1000)<features.majorChance(FeatureId.ENDERMAN_COMBO))m.getAttached(RemainingState.DATA).putString("birth","combo");}
    public void register(){instance=this;RemainingCommands.register();
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPED.register(server->pending.clear());
        ServerEntityEvents.ENTITY_LOAD.register((e,l)->{
            if(e instanceof SmallFireball b&&RemainingState.known(b)&&b.getAttached(RemainingState.DATA).getStringOr("role","").equals("blaze_ball")&&Boolean.TRUE.equals(b.getAttached(RemainingState.ORBIT))&&!RemainingState.live(b))pending.add(b);
            if(e instanceof Mob m&&supported(m)){RemainingState.initialize(m);if(!RemainingState.known(m))return;cleanup(m,true);var r=RemainingState.runtime(m);
                if(!r.installed){r.installed=true;
                    if(m instanceof Silverfish s)m.getGoalSelector().addGoal(0,new SilverfishHide(s,this));
                    if(m instanceof Enderman end){var goals=m.getGoalSelector();for(var g:java.util.List.copyOf(goals.getAvailableGoals()))if(g.getGoal().getClass()==net.minecraft.world.entity.ai.goal.MeleeAttackGoal.class)goals.removeGoal(g.getGoal());goals.addGoal(2,new EndermanCombo(end,this));}
                    if(m instanceof Spider){hunt(m,Silverfish.class);hunt(m,Endermite.class);}
                }}});
        ServerEntityEvents.ENTITY_UNLOAD.register((e,l)->{if(e instanceof Mob m&&supported(m)&&RemainingState.known(m))cleanup(m,true);});
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server->{var batch=java.util.List.copyOf(pending);pending.clear();for(var b:batch)if(!b.isRemoved())b.discard();});
    }
    private <T extends LivingEntity> void hunt(Mob m,Class<T> type){((com.davidblackcn.buildupmobtweaks.mixin.rebuild.RemainingTargetAccess)m).buildup$targets().addGoal(4,new NearestAttackableTargetGoal<T>(m,type,10,true,false,(t,l)->enabled(m,FeatureId.SPIDER_HUNTS_PESTS)){
        public boolean canContinueToUse(){return enabled(m,FeatureId.SPIDER_HUNTS_PESTS)&&super.canContinueToUse();}});}
    public void beforeAi(Mob m){if(!supported(m)||!RemainingState.known(m))return;var r=RemainingState.runtime(m);var t=m.getTarget();
        boolean governed=enabled(m,FeatureId.GHAST_COOLDOWN)||enabled(m,FeatureId.GHAST_TELEGRAPH)||enabled(m,FeatureId.BLAZE_ORBIT)||enabled(m,FeatureId.BLAZE_STRAFE)||enabled(m,FeatureId.SILVERFISH_BURROW)||enabled(m,FeatureId.ENDERMAN_COMBO)||enabled(m,FeatureId.SPIDER_HUNTS_PESTS);
        if(governed&&t!=null&&!ZombieBehavior.valid(m,t))m.setTarget(null);
        if(m instanceof Ghast g&&(enabled(m,FeatureId.GHAST_COOLDOWN)||enabled(m,FeatureId.GHAST_TELEGRAPH)||enabled(m,FeatureId.GHAST_SLOW_FIREBALL))&&(m.isNoAi()||!m.isAlive()||!ZombieBehavior.valid(m,m.getTarget()))){r.paused=true;g.setCharging(false);m.setAttached(RemainingState.CHARGE,0);}
        if(m instanceof Blaze&&(!enabled(m,FeatureId.BLAZE_ORBIT)||m.isNoAi()||!m.isAlive()||!ZombieBehavior.valid(m,m.getTarget())||r.target!=null&&r.target!=m.getTarget()))clearBalls(m,true);
        if(m instanceof Silverfish s&&r.hidden&&(!enabled(m,FeatureId.SILVERFISH_BURROW)||s.isNoAi()||!s.isAlive()||!ZombieBehavior.valid(s,s.getLastHurtByMob())))SilverfishHide.restore(s);
        if(m instanceof Enderman end){var speed=end.getAttribute(Attributes.MOVEMENT_SPEED);if(enabled(m,FeatureId.ENDERMAN_SPEED)&&!end.isNoAi()){
                if(!speed.hasModifier(SPEED))speed.addTransientModifier(new AttributeModifier(SPEED,-.15,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }else speed.removeModifier(SPEED);
            if(r.target!=null&&(!combo(end)||end.isNoAi()||!end.isAlive()||r.target!=end.getTarget()||!ZombieBehavior.valid(end,r.target))){r.target=null;r.end=0;}}
    }
    public void cleanup(Mob m){cleanup(m,false);}
    private void cleanup(Mob m,boolean defer){var r=RemainingState.runtime(m);if(defer){pending.addAll(r.balls);r.balls.clear();}else clearBalls(m,true);r.target=null;r.end=0;m.setAttached(RemainingState.CHARGE,0);
        if(m instanceof Ghast ghast)ghast.setCharging(false);
        if(m instanceof Silverfish s)SilverfishHide.restore(s);
        if(m instanceof Enderman){m.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(SPEED);}
    }
    public boolean ghastWait(Ghast m){return enabled(m,FeatureId.GHAST_COOLDOWN)&&m.level().getGameTime()<m.getAttached(RemainingState.DATA).getLongOr("ghast_ready",0);}
    public void fireball(Ghast m,LargeFireball b){if(!RemainingState.known(m))return;if(enabled(m,FeatureId.GHAST_SLOW_FIREBALL)){b.accelerationPower*=.6;b.setDeltaMovement(b.getDeltaMovement().scale(.6));}
        RemainingState.runtime(m).shots++;if(enabled(m,FeatureId.GHAST_COOLDOWN))m.getAttached(RemainingState.DATA).putLong("ghast_ready",m.level().getGameTime()+101);}
    public int volley(Blaze m){return enabled(m,FeatureId.BLAZE_DIFFICULTY_VOLLEY)?switch(m.level().getDifficulty()){case EASY->2;case HARD->4;default->3;}:3;}
    public void blazeStep(Blaze m,int step){if(!RemainingState.known(m))return;var r=RemainingState.runtime(m);if(step==1){r.sequence=0;r.volley=volley(m);r.target=m.getTarget();}}
    public void bind(Blaze m,Entity e){if(!(e instanceof SmallFireball b)||!enabled(m,FeatureId.BLAZE_ORBIT)||!ZombieBehavior.valid(m,m.getTarget()))return;var r=RemainingState.runtime(m);
        var d=new CompoundTag();d.putInt("version",1);d.putString("role","blaze_ball");d.putLong("release",m.level().getGameTime()+40);d.putLong("expires",m.level().getGameTime()+200);
        b.setAttached(RemainingState.DATA,d);b.setAttached(RemainingState.ORBIT,true);b.setAttached(RemainingState.INDEX,r.sequence++);b.setAttached(RemainingState.COUNT,r.volley);RemainingState.live(b,true);
        if(b.startRiding(m,true,false)){r.balls.add(b);r.bindings++;}else b.discard();}
    public void clearBalls(Mob m,boolean all){var r=RemainingState.runtime(m);for(var b:r.balls)if(all||Boolean.TRUE.equals(b.getAttached(RemainingState.ORBIT)))b.discard();r.balls.removeIf(Entity::isRemoved);}
    public boolean placeBall(Blaze m,Entity e,Entity.MoveFunction move){if(!(e instanceof SmallFireball b)||!Boolean.TRUE.equals(b.getAttached(RemainingState.ORBIT)))return false;
        int n=Math.max(1,b.getAttachedOrCreate(RemainingState.COUNT)),i=b.getAttachedOrCreate(RemainingState.INDEX);double angle=n==1?Math.PI/2:Math.PI*i/(n-1),growth=Math.min(1,b.tickCount/15.0);
        var side=m.calculateViewVector(0,m.getYHeadRot()+(m.isLeftHanded()?-90:90));var v=m.getEyePosition().add(side.scale(Math.cos(angle)*growth)).add(0,Math.sin(angle)*growth,0);move.accept(b,v.x,v.y,v.z);return true;}
    public void ballTick(SmallFireball b){if(!(b.level() instanceof ServerLevel)||!RemainingState.known(b)||!b.getAttached(RemainingState.DATA).getStringOr("role","").equals("blaze_ball"))return;
        if(!(b.getOwner() instanceof Blaze m)||!enabled(m,FeatureId.BLAZE_ORBIT)||!m.isAlive()||m.isNoAi()||!ZombieBehavior.valid(m,m.getTarget())){b.discard();return;}
        var d=b.getAttached(RemainingState.DATA);if(b.level().getGameTime()>=d.getLongOr("expires",0)){b.discard();return;}
        var r=RemainingState.runtime(m);if(!r.balls.contains(b))r.balls.add(b);
        if(Boolean.TRUE.equals(b.getAttached(RemainingState.ORBIT))){if(b.getVehicle()!=m){b.discard();return;}if(b.level().getGameTime()>=d.getLongOr("release",0)){
            var dir=m.getTarget().getEyePosition().subtract(b.position()).normalize();b.stopRiding();b.setAttached(RemainingState.ORBIT,false);b.shoot(dir.x,dir.y,dir.z,.1f,5-m.level().getDifficulty().getId());b.needsSync=true;r.releases++;}}
        r.balls.removeIf(Entity::isRemoved);
    }
    public void strafe(Blaze m,int step){if(!enabled(m,FeatureId.BLAZE_STRAFE)||step<1||!ZombieBehavior.valid(m,m.getTarget())||!m.hasLineOfSight(m.getTarget()))return;
        var dir=m.getTarget().position().subtract(m.position()).multiply(1,0,1).normalize();double sign=m.isLeftHanded()?-1:1;var side=new Vec3(-dir.z*sign,0,dir.x*sign).scale(.6);
        if(m.level().noCollision(m,m.getBoundingBox().move(side)))m.getMoveControl().strafe(!m.onGround()&&m.distanceTo(m.getTarget())<6?-.3f:0,(float)(sign*.6));}
}
