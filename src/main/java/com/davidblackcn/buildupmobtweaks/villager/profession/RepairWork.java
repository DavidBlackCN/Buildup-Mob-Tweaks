package com.davidblackcn.buildupmobtweaks.villager.profession;
import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import com.davidblackcn.buildupmobtweaks.villager.farmer.InteractionPolicy;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.item.Items;
import net.minecraft.sounds.SoundEvents;

public final class RepairWork extends ResourceWork {
    public RepairWork(){super("repair_golem");}
    public boolean canStart(Context c){if(!BuildupMobTweaks.config().villagers.professions.golem_repair.get()||slot(c.villager(),s->s.is(Items.IRON_INGOT))<0||!c.villager().getMainHandItem().isEmpty())return false;target=choose(c,IronGolem.class,g->g.getHealth()<g.getMaxHealth()&&g.getTarget()==null&&permits(c,g.blockPosition(),InteractionPolicy.Operation.REPAIR));return target!=null;}
    public void start(Context c){super.start(c);ProfessionWorkState.visual(c.villager(),Items.IRON_INGOT,2);}
    public Result tick(Context c){
        if(!valid(c)||!(target instanceof IronGolem g)||g.getTarget()!=null||g.getHealth()>=g.getMaxHealth()||!c.villager().getMainHandItem().isEmpty())return Result.FAILED;
        int source=slot(c.villager(),s->s.is(Items.IRON_INGOT));if(source<0)return Result.FAILED;
        if(!WorkMotion.reach(c,g.blockPosition(),2.6)||!c.villager().hasLineOfSight(g))return Result.CONTINUE;
        if(++workTicks<20)return Result.CONTINUE;if(!permits(c,g.blockPosition(),InteractionPolicy.Operation.REPAIR))return Result.FAILED;
        commit(c);c.villager().getInventory().removeItem(source,1);g.heal(25);g.playSound(SoundEvents.IRON_GOLEM_REPAIR,1,1);c.villager().swingForAttack(net.minecraft.world.InteractionHand.MAIN_HAND);return Result.COMPLETE;
    }
}
