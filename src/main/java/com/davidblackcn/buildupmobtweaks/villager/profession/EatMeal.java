package com.davidblackcn.buildupmobtweaks.villager.profession;
import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import com.davidblackcn.buildupmobtweaks.villager.WorkActivity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.*;
import net.minecraft.sounds.SoundEvents;

public final class EatMeal implements WorkActivity {
    private int slot=-1,ticks;private ItemStack food;
    public Identifier id(){return BuildupMobTweaks.id("eat_meal");}public int interactionTier(){return 2;}
    private boolean diet(ItemStack s){return s.is(Items.BREAD)||s.is(Items.CARROT)||s.is(Items.POTATO)||s.is(Items.BEETROOT);}
    public boolean canStart(Context c){var v=c.villager();if(v.getHealth()>=v.getMaxHealth()||!v.getMainHandItem().isEmpty())return false;for(int i=0;i<v.getInventory().getContainerSize();i++)if(diet(v.getInventory().getItem(i))){slot=i;return true;}return false;}
    public void start(Context c){ticks=0;food=c.villager().getInventory().getItem(slot);ProfessionWorkState.visual(c.villager(),food.getItem(),3);}
    public Result tick(Context c){
        var v=c.villager();if(v.getInventory().getItem(slot)!=food||!diet(food)||food.isEmpty()||v.getHealth()>=v.getMaxHealth()||!v.getMainHandItem().isEmpty())return Result.FAILED;
        if(++ticks%10==0)v.playSound(SoundEvents.GENERIC_EAT.value(),.6F,1);if(ticks<32)return Result.CONTINUE;
        var nutrition=food.get(DataComponents.FOOD);if(nutrition==null)return Result.FAILED;
        ProfessionWorkState.defer(v,id(),BuildupMobTweaks.config().villagers.professions.action_cooldown.get());food.shrink(1);v.heal(nutrition.nutrition());ProfessionWorkState.runtime(v).operations++;return Result.COMPLETE;
    }
    public void stop(Context c,StopReason reason){}
}
