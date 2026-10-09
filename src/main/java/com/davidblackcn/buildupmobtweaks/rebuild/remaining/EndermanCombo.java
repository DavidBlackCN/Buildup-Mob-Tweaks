/* Mob AI Tweaks adaptation: Copyright (c) 2024 N0t_UN_Owen, MIT. See NOTICE.md. */
package com.davidblackcn.buildupmobtweaks.rebuild.remaining;
import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import com.davidblackcn.buildupmobtweaks.rebuild.zombie.ZombieBehavior;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
/** Native scheduler and native melee helper retain range, LOS and the 20-tick attack interval. */
final class EndermanCombo extends MeleeAttackGoal {
    private final Enderman m;private final RemainingBehavior behavior;private long warmup;
    EndermanCombo(Enderman m,RemainingBehavior b){super(m,1,false);this.m=m;behavior=b;}
    public boolean canUse(){return ZombieBehavior.valid(m,m.getTarget())&&super.canUse();}
    public boolean canContinueToUse(){var r=RemainingState.runtime(m);var t=m.getTarget();return ZombieBehavior.valid(m,t)&&(behavior.combo(m)&&!(t instanceof Endermite)&&m.distanceToSqr(t)<=4096&&m.hasLineOfSight(t)||r.target!=null&&r.target==t&&m.level().getGameTime()<r.end||super.canContinueToUse());}
    public void start(){super.start();warmup=m.level().getGameTime()+60;}
    public void tick(){var r=RemainingState.runtime(m);var t=m.getTarget();long now=m.level().getGameTime();
        if(!behavior.combo(m)||t instanceof Endermite||!ZombieBehavior.valid(m,t)){r.target=null;super.tick();return;}
        if(r.target==null){if(now<warmup||now<m.getAttached(RemainingState.DATA).getLongOr("combo_ready",0)){super.tick();return;}
            r.target=t;r.start=now;r.end=now+61;r.combos++;m.getAttached(RemainingState.DATA).putLong("combo_ready",now+behavior.comboCooldown());}
        if(t!=r.target||now>=r.end){r.target=null;warmup=now+60;super.tick();return;}
        long elapsed=now-r.start;
        if(elapsed==10||elapsed==30||elapsed==50){var pos=t.position().subtract(t.getDeltaMovement().scale(3));if(m.level().hasChunkAt(BlockPos.containing(pos))&&m.teleport(pos.x,pos.y,pos.z)){r.teleports++;m.playSound(SoundEvents.ENDER_PEARL_THROW,1,.5f);}}
        float health=t.getHealth();super.tick();if(t.getHealth()<health)r.attacks++;
        m.getNavigation().stop();m.getMoveControl().setWait();m.getLookControl().setLookAt(t,90,30);
        if((elapsed==10||elapsed==30||elapsed==50)&&behavior.enabled(m,FeatureId.ENDERMAN_COMBO_ANIMATION))m.swingForAttack(InteractionHand.MAIN_HAND);
    }
    public void stop(){super.stop();var r=RemainingState.runtime(m);r.target=null;r.end=0;}
    protected boolean canPerformAttack(LivingEntity t){var r=RemainingState.runtime(m);long elapsed=m.level().getGameTime()-r.start;
        return (r.target==null||elapsed==20||elapsed==40||elapsed==60)&&super.canPerformAttack(t);}
}
