package com.davidblackcn.buildupmobtweaks.villager.profession;
import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import com.davidblackcn.buildupmobtweaks.villager.farmer.InteractionPolicy;
import net.minecraft.core.*;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.projectile.arrow.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
public final class ArcheryWork extends ResourceWork {
    private BlockPos targetBlock;private ItemStack bow;private Arrow arrow;private int settled;
    public ArcheryWork(){super("archery_training");}
    public boolean canStart(Context c){var v=c.villager();if(!BuildupMobTweaks.config().villagers.professions.archery_training.get()||!v.getMainHandItem().is(Items.BOW)||slot(v,s->s.is(Items.ARROW))<0)return false;int checks=0;for(var p:BlockPos.betweenClosed(v.blockPosition().offset(-5,-1,-5),v.blockPosition().offset(5,1,5))){if(++checks>363||ProfessionModule.scanBudget(c.level(),1)==0)break;if(!c.level().hasChunkAt(p)||!c.level().getBlockState(p).is(Blocks.TARGET)||!permits(c,p,InteractionPolicy.Operation.TRAIN))continue;var hit=c.level().clip(new ClipContext(v.getEyePosition(),Vec3.atCenterOf(p),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,v));if(hit.getBlockPos().equals(p)){targetBlock=p.immutable();return true;}}return false;}
    public void start(Context c){super.start(c);bow=c.villager().getMainHandItem();WorkMotion.look(c,targetBlock);ProfessionWorkState.visual(c.villager(),Items.BOW,2);c.villager().startUsingItem(net.minecraft.world.InteractionHand.MAIN_HAND);}
    public Result tick(Context c){var v=c.villager();if(!valid(c)||v.getMainHandItem()!=bow||!bow.is(Items.BOW))return Result.FAILED;if(committed)return arrow.isRemoved()||++settled>=80?Result.COMPLETE:Result.CONTINUE;if(!c.level().hasChunkAt(targetBlock)||!c.level().getBlockState(targetBlock).is(Blocks.TARGET))return Result.FAILED;if(++workTicks<30)return Result.CONTINUE;int source=slot(v,s->s.is(Items.ARROW));if(source<0||!permits(c,targetBlock,InteractionPolicy.Operation.TRAIN))return Result.FAILED;commit(c);var stack=v.getInventory().removeItem(source,1);arrow=new Arrow(c.level(),v,stack,bow);arrow.pickup=AbstractArrow.Pickup.DISALLOWED;arrow.setBaseDamage(0);WorkProjectiles.mark(arrow,WorkProjectiles.TRAINING,v,null,120);var delta=Vec3.atCenterOf(targetBlock).subtract(arrow.position());arrow.shoot(delta.x,delta.y+delta.horizontalDistance()*.05,delta.z,1.6F,0);ProfessionWorkState.runtime(v).projectile=arrow;c.level().addFreshEntity(arrow);if(v.getUseItem()==bow)v.stopUsingItem();bow.hurtAndBreak(1,v,EquipmentSlot.MAINHAND);v.swingForAttack(net.minecraft.world.InteractionHand.MAIN_HAND);return Result.CONTINUE;}
    public void stop(Context c,StopReason reason){if(c.villager().getUseItem()==bow)c.villager().stopUsingItem();}
}
