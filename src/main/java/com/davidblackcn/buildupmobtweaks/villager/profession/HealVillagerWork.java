package com.davidblackcn.buildupmobtweaks.villager.profession;
import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import com.davidblackcn.buildupmobtweaks.villager.farmer.InteractionPolicy;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.*;

public final class HealVillagerWork extends ResourceWork {
    private ThrownSplashPotion potion;
    public HealVillagerWork(){super("heal_villager");}
    private boolean medicine(ItemStack s){var p=s.get(DataComponents.POTION_CONTENTS);return s.is(Items.SPLASH_POTION)&&p!=null&&p.customEffects().isEmpty()&&(p.is(Potions.HEALING)||p.is(Potions.STRONG_HEALING));}
    public boolean canStart(Context c){if(!BuildupMobTweaks.config().villagers.professions.cleric_healing.get()||slot(c.villager(),this::medicine)<0||!c.villager().getMainHandItem().isEmpty())return false;target=choose(c,Villager.class,t->t.getHealth()<t.getMaxHealth()&&!t.isInvertedHealAndHarm()&&permits(c,t.blockPosition(),InteractionPolicy.Operation.HEAL));return target!=null;}
    public void start(Context c){super.start(c);ProfessionWorkState.visual(c.villager(),Items.SPLASH_POTION,2);}
    public Result tick(Context c){
        var v=c.villager();if(!valid(c)||!(target instanceof Villager patient)||patient.isInvertedHealAndHarm())return Result.FAILED;
        if(committed)return potion==null||potion.isRemoved()?Result.COMPLETE:ticks>180?Result.FAILED:Result.CONTINUE;
        if(patient.getHealth()>=patient.getMaxHealth()||!v.getMainHandItem().isEmpty())return Result.FAILED;
        int source=slot(v,this::medicine);if(source<0)return Result.FAILED;
        if(!WorkMotion.reach(c,patient.blockPosition(),3)||!v.hasLineOfSight(patient))return Result.CONTINUE;
        if(++workTicks<20)return Result.CONTINUE;if(!permits(c,patient.blockPosition(),InteractionPolicy.Operation.HEAL))return Result.FAILED;
        commit(c);var stack=v.getInventory().removeItem(source,1);potion=new ThrownSplashPotion(c.level(),v,stack);WorkProjectiles.mark(potion,WorkProjectiles.POTION,v,patient,100);
        double dx=patient.getX()-v.getX(),dz=patient.getZ()-v.getZ(),dy=patient.getEyeY()-1.1-potion.getY();double distance=Math.sqrt(dx*dx+dz*dz);
        potion.shoot(dx,dy+distance*.2,dz,.75F,0);ProfessionWorkState.runtime(v).projectile=potion;c.level().addFreshEntity(potion);v.swingForAttack(net.minecraft.world.InteractionHand.MAIN_HAND);return Result.CONTINUE;
    }
}
