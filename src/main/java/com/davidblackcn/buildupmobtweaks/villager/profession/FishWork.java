package com.davidblackcn.buildupmobtweaks.villager.profession;
import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import com.davidblackcn.buildupmobtweaks.villager.farmer.InteractionPolicy;
import net.minecraft.core.*;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.*;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.parameters.*;
import net.minecraft.world.phys.Vec3;
import java.util.*;

public final class FishWork extends ResourceWork {
    private BlockPos water,bank;private FishingHook hook;private ItemStack rod;
    private static final List<BlockPos> OFFSETS;
    static {var out=new ArrayList<BlockPos>();for(int dx=-6;dx<=6;dx++)for(int dz=-6;dz<=6;dz++)for(int dy=-2;dy<=1;dy++)out.add(new BlockPos(dx,dy,dz));out.sort(Comparator.comparingInt((BlockPos p)->p.getX()*p.getX()+p.getY()*p.getY()+p.getZ()*p.getZ()).thenComparingLong(BlockPos::asLong));OFFSETS=List.copyOf(out);}
    public FishWork(){super("fish");}
    public boolean canStart(Context c){
        var v=c.villager();if(!BuildupMobTweaks.config().villagers.professions.fishing.get()||!v.getMainHandItem().is(Items.FISHING_ROD)||!v.getInventory().canAddItem(new ItemStack(Items.COD)))return false;
        int checked=0;
        for(var offset:OFFSETS){if(++checked>512||ProfessionModule.scanBudget(c.level(),1)==0)break;var p=v.blockPosition().offset(offset);if(!c.level().hasChunkAt(p)||!c.level().getFluidState(p).is(FluidTags.WATER)||!c.level().getBlockState(p.above()).getCollisionShape(c.level(),p.above()).isEmpty())continue;
            if(!permits(c,p,InteractionPolicy.Operation.FISH))continue;
            for(var direction:Direction.Plane.HORIZONTAL){var side=p.above().relative(direction);if(WorkMotion.clearFoot(c.level(),side)){water=p;bank=side;return true;}}
        }
        return false;
    }
    public void start(Context c){super.start(c);rod=c.villager().getMainHandItem();ProfessionWorkState.visual(c.villager(),Items.FISHING_ROD,4);WorkMotion.look(c,water);}
    public Result tick(Context c){
        var v=c.villager();if(!valid(c)||v.getMainHandItem()!=rod||!rod.is(Items.FISHING_ROD)||!c.level().hasChunkAt(water)||!c.level().getFluidState(water).is(FluidTags.WATER)||!permits(c,water,InteractionPolicy.Operation.FISH))return Result.FAILED;
        if(hook==null){
            if(!WorkMotion.reach(c,bank,.8))return Result.CONTINUE;if(++workTicks<20)return Result.CONTINUE;
            hook=new FishingHook(EntityTypes.FISHING_BOBBER,c.level());hook.setAttached(WorkProjectiles.HOOK,true);hook.setOwner(v);hook.setPos(v.getX(),v.getEyeY()-.2,v.getZ());
            var delta=Vec3.atCenterOf(water).add(0,.3,0).subtract(hook.position()).scale(1.0/12);hook.setDeltaMovement(delta.add(0,.165,0));ProfessionWorkState.runtime(v).projectile=hook;c.level().addFreshEntity(hook);v.swingForAttack(net.minecraft.world.InteractionHand.MAIN_HAND);return Result.CONTINUE;
        }
        if(hook.isRemoved())return Result.FAILED;
        if(((NpcFishingAccess)hook).buildup$nibble()<=0||!c.level().getFluidState(hook.blockPosition()).is(FluidTags.WATER))return Result.CONTINUE;
        commit(c);
        var params=new LootParams.Builder(c.level()).withParameter(LootContextParams.ORIGIN,hook.position()).withParameter(LootContextParams.TOOL,rod).withParameter(LootContextParams.THIS_ENTITY,hook).create(LootContextParamSets.FISHING);
        var table=c.level().getServer().reloadableRegistries().getLootTable(BuiltInLootTables.FISHING_FISH);
        for(var fish:table.getRandomItems(params)){
            var remaining=v.getInventory().addItem(fish);if(!remaining.isEmpty())c.level().addFreshEntity(new ItemEntity(c.level(),v.getX(),v.getY()+.5,v.getZ(),remaining));
        }
        rod.hurtAndBreak(1,v,EquipmentSlot.MAINHAND);v.swingForAttack(net.minecraft.world.InteractionHand.MAIN_HAND);hook.discard();return Result.COMPLETE;
    }
}
