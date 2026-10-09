/* Mob AI Tweaks adaptation: Copyright (c) 2024 N0t_UN_Owen, MIT. See NOTICE.md. */
package com.davidblackcn.buildupmobtweaks.rebuild.evoker;
import com.davidblackcn.buildupmobtweaks.feature.*;
import com.davidblackcn.buildupmobtweaks.rebuild.raid.*;
import com.davidblackcn.buildupmobtweaks.rebuild.zombie.ZombieBehavior;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.illager.Evoker;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.item.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec3;
import java.util.EnumSet;

public final class EvokerBehavior {
    private static EvokerBehavior instance;private final FeatureRegistry features;
    public EvokerBehavior(FeatureRegistry f){features=f;}public static EvokerBehavior instance(){return instance;}
    public boolean enabled(Evoker m,FeatureId id){return RaidState.known(m,RaidState.EVOKER)&&RaidGate.enabled(m,features,id);}
    public void born(Evoker m,EntitySpawnReason reason){if(!RaidGate.supported(m)||m.hasAttached(RaidState.EVOKER))return;RaidState.initialize(m,RaidState.EVOKER);
        if(reason!=EntitySpawnReason.NATURAL&&reason!=EntitySpawnReason.SPAWN_ITEM_USE)return;int fire=enabled(m,FeatureId.EVOKER_FIREBALL)?features.majorChance(FeatureId.EVOKER_FIREBALL):0;
        int totem=enabled(m,FeatureId.EVOKER_TOTEM)?features.majorChance(FeatureId.EVOKER_TOTEM):0;int roll=m.getRandom().nextInt(Math.max(1000,fire+totem));
        m.getAttached(RaidState.EVOKER).putString("birth",roll<fire?"fireball":roll<fire+totem?"totem":"none");}
    public void register(){instance=this;VexLedger.register();RaidCommands.register();
        ServerEntityEvents.ENTITY_LOAD.register((e,l)->{
            if(e instanceof SmallFireball b&&RaidState.known(b,RaidState.BALL)&&Boolean.TRUE.equals(b.getAttached(RaidState.ORBIT))&&!Boolean.TRUE.equals(b.getAttached(RaidState.LIVE)))b.discard();
            if(e instanceof Evoker m&&RaidGate.supported(m)){RaidState.initialize(m,RaidState.EVOKER);if(!RaidState.known(m,RaidState.EVOKER))return;cleanup(m,true);
                var r=RaidState.runtime(m);if(!r.installed){r.installed=true;m.getGoalSelector().addGoal(1,new Special(m));}}});
        ServerEntityEvents.ENTITY_UNLOAD.register((e,l)->{if(e instanceof Evoker m&&RaidState.known(m,RaidState.EVOKER))cleanup(m,true);});
        // Fabric ALLOW_DEATH runs before native checkTotemDeathProtection. Cleanup belongs in die(), after protection failed.
    }
    public boolean busy(Evoker m){return RaidState.known(m,RaidState.EVOKER)&&Boolean.TRUE.equals(m.getAttached(RaidState.ACTIVE));}
    public int batch(Evoker m){return switch(m.level().getDifficulty()){case EASY->2;case HARD->4;default->3;};}
    public boolean summonsModified(Evoker m){return enabled(m,FeatureId.EVOKER_VEX_LIMIT)||enabled(m,FeatureId.EVOKER_SUMMON_COOLDOWN);}
    public boolean canSummon(Evoker m){return !summonsModified(m)||ZombieBehavior.valid(m,m.getTarget())&&!busy(m)
        &&(!enabled(m,FeatureId.EVOKER_VEX_LIMIT)||VexLedger.count(m)+batch(m)<=features.vexLimit())
        &&(!enabled(m,FeatureId.EVOKER_SUMMON_COOLDOWN)||m.level().getGameTime()>=m.getAttached(RaidState.EVOKER).getLongOr("summon_ready",0));}
    public boolean canSummonAtCast(Evoker m){return ZombieBehavior.valid(m,m.getTarget())&&!busy(m)&&(!enabled(m,FeatureId.EVOKER_VEX_LIMIT)||VexLedger.count(m)+batch(m)<=features.vexLimit());}
    public int reserveSummon(Evoker m,int vanilla){if(!enabled(m,FeatureId.EVOKER_SUMMON_COOLDOWN))return vanilla;
        int interval=features.summonCooldown();m.getAttached(RaidState.EVOKER).putLong("summon_ready",m.level().getGameTime()+interval);return interval;}
    public void beforeAi(Evoker m){if(!RaidState.known(m,RaidState.EVOKER))return;var r=RaidState.runtime(m);var t=m.getTarget();
        boolean enabled=enabled(m,FeatureId.EVOKER_FIREBALL)||enabled(m,FeatureId.EVOKER_TOTEM)||summonsModified(m);
        if(enabled&&t!=null&&(!ZombieBehavior.valid(m,t)||m.distanceToSqr(t)>Math.pow(m.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.FOLLOW_RANGE),2))){m.setTarget(null);}
        if(busy(m)&&(!m.isAlive()||m.isNoAi()||m.getTarget()!=r.target||!ZombieBehavior.valid(m,r.target)||!specialEnabled(m)))cleanup(m,true);
        if(!enabled(m,FeatureId.EVOKER_FIREBALL)){for(var b:r.balls)b.discard();r.balls.clear();}
        if(!enabled(m,FeatureId.EVOKER_TOTEM))clearTotem(m);
        r.balls.removeIf(Entity::isRemoved);
    }
    private boolean specialEnabled(Evoker m){String birth=m.getAttached(RaidState.EVOKER).getStringOr("birth","none");return birth.equals("fireball")?enabled(m,FeatureId.EVOKER_FIREBALL):birth.equals("totem")&&enabled(m,FeatureId.EVOKER_TOTEM);}
    public void cleanup(Evoker m,boolean all){m.setAttached(RaidState.ACTIVE,false);var r=RaidState.runtime(m);r.target=null;
        for(var b:r.balls)if(all||Boolean.TRUE.equals(b.getAttached(RaidState.ORBIT)))b.discard();r.balls.removeIf(Entity::isRemoved);clearTotem(m);
        // A loaded in-flight native casting flag must not retain our pose.
    }
    private void clearTotem(Evoker m){var d=m.getAttached(RaidState.EVOKER);if(TemporaryItem.is(m.getOffhandItem(),"totem"))m.setItemSlot(EquipmentSlot.OFFHAND,ItemStack.EMPTY);
        if(d.getBooleanOr("totem_drop_owned",false)){if(m.getDropChances().byEquipment(EquipmentSlot.OFFHAND)==0)m.setDropChance(EquipmentSlot.OFFHAND,d.getFloatOr("totem_drop",.085f));d.putBoolean("totem_drop_owned",false);}}
    public boolean placeBall(Evoker m,Entity e,Entity.MoveFunction move){if(!(e instanceof SmallFireball)||!Boolean.TRUE.equals(e.getAttached(RaidState.ORBIT)))return false;
        int i=e.getAttachedOrCreate(RaidState.INDEX);double angle=(m.tickCount/45.0+i*2/3.0)*Math.PI,radius=m.getBoundingBox().getSize()*1.3*Math.min(20,e.tickCount)/20.0;
        if(m.isLeftHanded())angle=-angle;
        move.accept(e,m.getX()+Math.cos(angle)*radius,m.getY()+.9,m.getZ()+Math.sin(angle)*radius);
        if(m.level() instanceof ServerLevel l&&RaidState.known(m,RaidState.EVOKER)&&ZombieBehavior.valid(m,m.getTarget())&&m.getTarget().getBoundingBox().intersects(e.getBoundingBox())
            &&m.getTarget().hurtServer(l,e.damageSources().fireball((SmallFireball)e,m),5)&&!noFire((SmallFireball)e))m.getTarget().setRemainingFireTicks(60);
        return true;}
    /** Called by the projectile's actual tick; no forced chunk loads. */
    public void ballTick(SmallFireball b){if(!(b.level() instanceof ServerLevel l)||!RaidState.known(b,RaidState.BALL))return;var d=b.getAttached(RaidState.BALL);
        Entity owner=b.getOwner();if(!(owner instanceof Evoker m)||!RaidState.known(m,RaidState.EVOKER)||!enabled(m,FeatureId.EVOKER_FIREBALL)||!m.isAlive()||l.getGameTime()>d.getLongOr("expires",0)){b.discard();return;}
        var r=RaidState.runtime(m);if(!r.balls.contains(b))r.balls.add(b);
        if(Boolean.TRUE.equals(b.getAttached(RaidState.ORBIT))&&(!busy(m)||b.getVehicle()!=m))b.discard();
    }
    public boolean noFire(SmallFireball b){return RaidState.known(b,RaidState.BALL)&&b.getOwner() instanceof Evoker m&&enabled(m,FeatureId.EVOKER_FIREBALL_NO_FIRE);}
    private final class Special extends Goal {
        private final Evoker m;private boolean fire;Special(Evoker m){this.m=m;setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
        public boolean canUse(){if(!specialEnabled(m)||m.isCastingSpell()||m.isNoAi()||!ZombieBehavior.valid(m,m.getTarget())||!m.hasLineOfSight(m.getTarget()))return false;
            var d=m.getAttached(RaidState.EVOKER);fire=d.getStringOr("birth","none").equals("fireball");
            return fire?m.tickCount>=20&&m.level().getGameTime()>=d.getLongOr("special_ready",0):!d.getBooleanOr("totem_spent",false)&&m.getHealth()<m.getMaxHealth()*.2f&&m.getOffhandItem().isEmpty();}
        public void start(){var r=RaidState.runtime(m);r.target=m.getTarget();r.start=m.level().getGameTime();r.end=r.start+(fire?80:40);m.setAttached(RaidState.ACTIVE,true);m.getNavigation().stop();
            var d=m.getAttached(RaidState.EVOKER);if(fire){d.putLong("special_ready",r.start+features.specialCooldown());r.casts++;m.playSound(SoundEvents.EVOKER_PREPARE_ATTACK,1,1);}
            else{d.putBoolean("totem_spent",true);d.putFloat("totem_drop",m.getDropChances().byEquipment(EquipmentSlot.OFFHAND));d.putBoolean("totem_drop_owned",true);
                m.setDropChance(EquipmentSlot.OFFHAND,0);m.setItemSlot(EquipmentSlot.OFFHAND,TemporaryItem.mark(new ItemStack(Items.TOTEM_OF_UNDYING),"totem"));r.totems++;}}
        public boolean canContinueToUse(){return busy(m)&&m.level().getGameTime()<RaidState.runtime(m).end;}
        public boolean requiresUpdateEveryTick(){return true;}
        public void tick(){beforeAi(m);if(!busy(m))return;var r=RaidState.runtime(m);m.getLookControl().setLookAt(r.target,30,30);long elapsed=m.level().getGameTime()-r.start;
            if(fire&&(elapsed==10||elapsed==30||elapsed==50)){var b=new SmallFireball(m.level(),m,new Vec3(0,0,1));var d=new CompoundTag();d.putInt("version",1);d.putLong("expires",r.start+200);b.setAttached(RaidState.BALL,d);b.setAttached(RaidState.ORBIT,true);b.setAttached(RaidState.LIVE,true);b.setAttached(RaidState.INDEX,(int)(elapsed-10)/20);
                b.snapTo(m.position().add(0,.9,0),0,0);if(m.level().addFreshEntity(b)&&b.startRiding(m,true,false))r.balls.add(b);else b.discard();}
            if(fire&&elapsed==70){for(var b:r.balls)if(Boolean.TRUE.equals(b.getAttached(RaidState.ORBIT))){b.stopRiding();b.setAttached(RaidState.ORBIT,false);var dir=b.position().subtract(m.position().add(0,.9,0)).normalize();b.setDeltaMovement(dir.scale(.1));b.needsSync=true;r.releases++;}m.playSound(SoundEvents.EVOKER_CAST_SPELL,1,1);}
            if(!fire&&m.getOffhandItem().isEmpty())cleanup(m,false);
        }
        public void stop(){cleanup(m,false);}
    }
}
