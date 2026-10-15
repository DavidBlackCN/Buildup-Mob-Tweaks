package com.davidblackcn.buildupmobtweaks.villager.profession;
import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import com.davidblackcn.buildupmobtweaks.villager.farmer.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.Items;

public final class FeedBabyWork extends ResourceWork {
    public FeedBabyWork(){super("feed_baby_animal");}
    public boolean canStart(Context c){if(!BuildupMobTweaks.config().villagers.professions.baby_feeding.get()||FarmerModule.owner(c.villager())==FarmerModule.Owner.EXTERNAL||!c.villager().getMainHandItem().isEmpty())return false;target=choose(c,Animal.class,a->a.isBaby()&&a.canAgeUp()&&slot(c.villager(),a::isFood)>=0&&permits(c,a.blockPosition(),InteractionPolicy.Operation.FEED));return target!=null;}
    public void start(Context c){super.start(c);int source=slot(c.villager(),((Animal)target)::isFood);ProfessionWorkState.visual(c.villager(),c.villager().getInventory().getItem(source).getItem(),2);}
    public Result tick(Context c){
        if(FarmerModule.owner(c.villager())==FarmerModule.Owner.EXTERNAL||!valid(c)||!(target instanceof Animal a)||!a.isBaby()||!a.canAgeUp()||!c.villager().getMainHandItem().isEmpty())return Result.FAILED;
        int source=slot(c.villager(),a::isFood);if(source<0)return Result.FAILED;
        if(!WorkMotion.reach(c,a.blockPosition(),1.8)||!c.villager().hasLineOfSight(a))return Result.CONTINUE;
        if(++workTicks<20)return Result.CONTINUE;if(!permits(c,a.blockPosition(),InteractionPolicy.Operation.FEED))return Result.FAILED;
        commit(c);c.villager().getInventory().removeItem(source,1);a.ageUp(net.minecraft.world.entity.AgeableMob.getSpeedUpSecondsWhenFeeding(-a.getAge()),true);a.playSound(net.minecraft.sounds.SoundEvents.GENERIC_EAT.value(),1,1);return Result.COMPLETE;
    }
}
