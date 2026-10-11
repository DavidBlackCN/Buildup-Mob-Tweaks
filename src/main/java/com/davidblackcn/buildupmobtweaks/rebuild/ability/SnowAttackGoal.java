/* Mob AI Tweaks adaptation: Copyright (c) 2024 N0t_UN_Owen, MIT. See NOTICE.md. */
package com.davidblackcn.buildupmobtweaks.rebuild.ability;

import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import net.minecraft.world.entity.animal.golem.SnowGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.level.entity.EntityTypeTest;
import java.util.*;

/** Bounded retaliation sweep; normal movement/aiming/firing stays in RangedAttackGoal. */
public final class SnowAttackGoal extends RangedAttackGoal {
    private final SnowGolem mob;private final AbilityBehavior behavior;
    private final ArrayDeque<Monster> sweep=new ArrayDeque<>();private long nextSweepShot;
    public SnowAttackGoal(SnowGolem m,AbilityBehavior b){super(m,1,10,20);mob=m;behavior=b;}
    @Override public boolean canUse(){return behavior.enabled(mob,FeatureId.SNOW_GOLEM_RANGED_BEHAVIOR)&&AbilityBehavior.valid(mob,mob.getTarget())&&super.canUse();}
    @Override public boolean canContinueToUse(){return canUse()&&super.canContinueToUse();}
    @Override public boolean requiresUpdateEveryTick(){return true;}
    @Override public void stop(){super.stop();sweep.clear();behavior.cancel(mob);}
    @Override public void tick(){long now=mob.level().getGameTime();var d=mob.getAttached(AbilityState.DATA);
        if(sweep.isEmpty()&&now>=d.getLongOr("snow_sweep_ready",0)){d.putLong("snow_sweep_ready",now+100);var raw=new ArrayList<Monster>();
            mob.level().getEntities(EntityTypeTest.forClass(Monster.class),mob.getBoundingBox().inflate(20),m->m.distanceToSqr(mob)<=400,raw,32);AbilityState.runtime(mob).scans++;
            for(var m:raw)if(m.getTarget()==mob&&AbilityBehavior.valid(mob,m)&&mob.hasLineOfSight(m)){sweep.add(m);if(sweep.size()==3)break;}
            nextSweepShot=now;}
        if(!sweep.isEmpty()){mob.getNavigation().stop();if(now>=nextSweepShot&&AbilityState.runtime(mob).target==null){var t=sweep.removeFirst();nextSweepShot=now+10;
                if(t.getTarget()==mob&&AbilityBehavior.valid(mob,t)&&mob.distanceToSqr(t)<=400&&mob.hasLineOfSight(t)){mob.getLookControl().setLookAt(t,30,30);mob.performRangedAttack(t,1);}}
        }else super.tick();
    }
}
