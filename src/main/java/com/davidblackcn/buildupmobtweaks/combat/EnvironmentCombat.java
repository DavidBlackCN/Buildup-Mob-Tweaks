package com.davidblackcn.buildupmobtweaks.combat;
import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import java.util.EnumSet;
import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.AbstractWindCharge;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.InfestedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
public final class EnvironmentCombat {
    public static boolean on(Mob mob,FeatureId id) { return HostileCombat.instance()!=null && HostileCombat.instance().enabled(mob,id); }
    public static void breezeTransition(Breeze mob,Pose old,Pose next) {
        if(mob.level().isClientSide() || mob.level().getDifficulty()!=Difficulty.HARD) return;
        FeatureId id=next==Pose.LONG_JUMPING?FeatureId.BREEZE_TAKEOFF_BURST:
                old==Pose.LONG_JUMPING && next==Pose.STANDING?FeatureId.BREEZE_LANDING_BURST:null;
        if(id==null || !on(mob,id) || !HostileCombat.instance().ready(mob,id.id().getPath())) return;
        HostileCombat.instance().reserve(mob,id.id().getPath(),100);
        var level=(ServerLevel)mob.level();
        level.explode(mob,null,AbstractWindCharge.EXPLOSION_DAMAGE_CALCULATOR,mob.getX(),mob.getY(),mob.getZ(),3,false,
                level.getGameRules().get(GameRules.MOB_GRIEFING)?Level.ExplosionInteraction.TRIGGER:Level.ExplosionInteraction.NONE,
                ParticleTypes.GUST_EMITTER_SMALL,ParticleTypes.GUST_EMITTER_LARGE,WeightedList.of(),SoundEvents.BREEZE_WIND_CHARGE_BURST);
    }
    public static Goal burrowGoal(Silverfish mob) { return new Burrow(mob); }
    private static final class Burrow extends Goal {
        private final Silverfish mob; private BlockPos destination; private BlockState original; private int remaining;
        Burrow(Silverfish mob) { this.mob=mob; setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK)); }
        private boolean allowed() { return on(mob,FeatureId.SILVERFISH_BURROW) && mob.isAlive() && mob.getHealth()<mob.getMaxHealth()*.5
                && ((ServerLevel)mob.level()).getGameRules().get(GameRules.MOB_GRIEFING); }
        @Override public boolean canUse() {
            if(!allowed() || !HostileCombat.instance().ready(mob,"silverfish_burrow")) return false;
            // At most six adjacent blocks once per two seconds, never search or load distant chunks.
            HostileCombat.instance().reserve(mob,"silverfish_burrow",40);
            for(var direction:Direction.values()) {
                var pos=mob.blockPosition().relative(direction); var state=mob.level().getBlockState(pos);
                if(InfestedBlock.isCompatibleHostBlock(state)) { destination=pos; original=state; return true; }
            }
            return false;
        }
        @Override public void start() { remaining=20; mob.getNavigation().stop(); }
        @Override public boolean requiresUpdateEveryTick() { return true; }
        @Override public boolean canContinueToUse() { return remaining>0 && allowed() && mob.level().getBlockState(destination)==original; }
        @Override public void tick() {
            mob.getNavigation().stop(); mob.getMoveControl().setWait();
            if(remaining%4==0) ((ServerLevel)mob.level()).sendParticles(ParticleTypes.POOF,mob.getX(),mob.getY()+.2,mob.getZ(),2,.15,.1,.15,.01);
            if(--remaining==0 && allowed() && mob.level().getBlockState(destination)==original
                    && mob.level().setBlockAndUpdate(destination,InfestedBlock.infestedStateByHost(original))) mob.discard();
        }
    }
    private EnvironmentCombat() {}
}