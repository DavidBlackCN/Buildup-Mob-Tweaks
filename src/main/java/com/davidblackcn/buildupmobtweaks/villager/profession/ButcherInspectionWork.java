package com.davidblackcn.buildupmobtweaks.villager.profession;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.Items;
public final class ButcherInspectionWork extends WorkCycle {
    public ButcherInspectionWork(){super("butcher_work",Items.BOWL);}
    public void start(Context c){super.start(c);int budget=ProfessionModule.scanBudget(c.level(),8);if(budget==0)return;var candidates=new java.util.ArrayList<Animal>();c.level().getEntities(net.minecraft.world.level.entity.EntityTypeTest.forClass(Animal.class),c.villager().getBoundingBox().inflate(6),e->e.isAlive()&&c.villager().hasLineOfSight(e),candidates,budget);if(!candidates.isEmpty())WorkMotion.look(c,candidates.getFirst());}
}
