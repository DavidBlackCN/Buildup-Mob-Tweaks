package com.davidblackcn.buildupmobtweaks.combat;
import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import java.util.EnumSet;
import net.fabricmc.fabric.api.attachment.v1.*;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.GameEventTags;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.gameevent.*;
import net.minecraft.world.phys.Vec3;
public final class RestingHostiles {
    private static final AttachmentType<RestGoal> GOAL=AttachmentRegistry.create(BuildupMobTweaks.id("rest_goal"));
    public static void register() {
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity,source,base,damage,blocked)->{ if(entity instanceof Mob mob && damage>0) wake(mob); });
        ServerEntityEvents.ENTITY_UNLOAD.register((entity,level)-> { if(entity instanceof Mob mob) {var goal=mob.getAttached(GOAL); if(goal!=null) goal.stop();} });
    }
    public static boolean eligible(Mob mob) { var t=mob.getType(); return t==EntityTypes.ZOMBIE || t==EntityTypes.SKELETON || t==EntityTypes.CREEPER || t==EntityTypes.SPIDER; }
    public static void install(Mob mob) {
        if(!eligible(mob)) return;
        var goal=new RestGoal(mob); mob.setAttached(GOAL,goal); mob.getGoalSelector().addGoal(0,goal);
    }
    public static boolean resting(Mob mob) { var g=mob.getAttached(GOAL); return g!=null && g.active && EnvironmentCombat.on(mob,FeatureId.HOSTILE_REST); }
    public static void wake(Mob mob) { var g=mob.getAttached(GOAL); if(g!=null) g.stop(); }
    private static final class RestGoal extends Goal implements GameEventListener {
        private final Mob mob; private final DynamicGameEventListener<RestGoal> listener;
        private boolean active; private long until;
        RestGoal(Mob mob) { this.mob=mob; listener=new DynamicGameEventListener<>(this); setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK,Flag.JUMP)); }
        @Override public boolean canUse() {
            if(!EnvironmentCombat.on(mob,FeatureId.HOSTILE_REST) || mob.getTarget()!=null || mob.isOnFire() || mob.isInWater()
                    || !mob.onGround() || mob.isPassenger() || mob.isVehicle() || mob.getHealth()<mob.getMaxHealth()
                    || !HostileCombat.instance().ready(mob,"rest_next")) return false;
            HostileCombat.instance().reserve(mob,"rest_next",1200);
            return mob.getRandom().nextInt(1000)<20 && mob.level().getNearestPlayer(mob,8)==null;
        }
        @Override public void start() {
            active=true; until=mob.level().getGameTime()+200; mob.getNavigation().stop(); mob.getMoveControl().setWait();
            listener.add((ServerLevel)mob.level());
        }
        @Override public boolean canContinueToUse() { return resting(mob) && mob.isAlive() && mob.level().getGameTime()<until && !mob.isOnFire() && !mob.isInWater(); }
        @Override public boolean requiresUpdateEveryTick() {return true;}
        @Override public void tick() {
            mob.getNavigation().stop(); mob.getMoveControl().setWait(); listener.move((ServerLevel)mob.level());
            if(mob.tickCount%20==0) ((ServerLevel)mob.level()).sendParticles(ParticleTypes.SPLASH,mob.getX(),mob.getEyeY()+.25,mob.getZ(),1,0,0,0,0);
        }
        @Override public void stop() { active=false; listener.remove((ServerLevel)mob.level()); }
        @Override public PositionSource getListenerSource() { return new EntityPositionSource(mob,mob.getEyeHeight()); }
        @Override public int getListenerRadius() { return 8; }
        @Override public boolean handleGameEvent(ServerLevel level,Holder<GameEvent> event,GameEvent.Context context,Vec3 pos) {
            if(!active || !EnvironmentCombat.on(mob,FeatureId.REST_SOUND_WAKE) || !event.is(GameEventTags.VIBRATIONS) || event.is(S2Tags.REST_IGNORED) || context.sourceEntity()==mob) return false;
            if(context.sourceEntity()!=null && context.sourceEntity().isCrouching() && event.is(GameEventTags.IGNORE_VIBRATIONS_SNEAKING)) return false;
            stop(); return true;
        }
    }
    private RestingHostiles() {}
}