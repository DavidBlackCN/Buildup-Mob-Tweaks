package com.davidblackcn.buildupmobtweaks.villager.profession;
import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import com.davidblackcn.buildupmobtweaks.villager.farmer.InteractionPolicy;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Items;
import java.util.*;

public final class ShearWork extends ResourceWork {
    private net.minecraft.world.item.ItemStack tool;
    public ShearWork(){super("shear_sheep");}
    public boolean canStart(Context c){var v=c.villager();if(!BuildupMobTweaks.config().villagers.professions.shearing.get()||!v.getMainHandItem().is(Items.SHEARS))return false;target=choose(c,Sheep.class,s->!s.isBaby()&&s.readyForShearing()&&permits(c,s.blockPosition(),InteractionPolicy.Operation.SHEAR));return target!=null;}
    public void start(Context c){super.start(c);tool=c.villager().getMainHandItem();ProfessionWorkState.visual(c.villager(),Items.SHEARS,2);}
    public Result tick(Context c){
        var v=c.villager();if(!valid(c)||!(target instanceof Sheep sheep)||!sheep.readyForShearing()||sheep.isBaby()||v.getMainHandItem()!=tool||!tool.is(Items.SHEARS))return Result.FAILED;
        if(!WorkMotion.reach(c,sheep.blockPosition(),1.8)||!v.hasLineOfSight(sheep))return Result.CONTINUE;
        if(++workTicks<20)return Result.CONTINUE;if(!permits(c,sheep.blockPosition(),InteractionPolicy.Operation.SHEAR))return Result.FAILED;
        var old=new HashSet<UUID>();for(var e:c.level().getEntitiesOfClass(ItemEntity.class,sheep.getBoundingBox().inflate(2)))old.add(e.getUUID());
        commit(c);sheep.shear(c.level(),v.getSoundSource(),tool);tool.hurtAndBreak(1,v,EquipmentSlot.MAINHAND);v.swingForAttack(net.minecraft.world.InteractionHand.MAIN_HAND);
        if(BuildupMobTweaks.config().villagers.professions.wool_pickup.get()&&permits(c,sheep.blockPosition(),InteractionPolicy.Operation.TRANSFER))
            for(var e:c.level().getEntitiesOfClass(ItemEntity.class,sheep.getBoundingBox().inflate(2)))if(!old.contains(e.getUUID())&&e.getItem().is(net.minecraft.tags.ItemTags.WOOL)){
                var remaining=v.getInventory().addItem(e.getItem());if(remaining.isEmpty())e.discard();else e.setItem(remaining);
            }
        return Result.COMPLETE;
    }
}
