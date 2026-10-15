package com.davidblackcn.buildupmobtweaks.villager.profession;
import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import com.davidblackcn.buildupmobtweaks.villager.*;
import com.davidblackcn.buildupmobtweaks.villager.farmer.InteractionPolicy;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.*;
import java.util.*;
import java.util.function.Predicate;

abstract class ResourceWork implements WorkActivity {
    private final Identifier id;protected LivingEntity target;protected int ticks,workTicks;protected boolean committed;
    ResourceWork(String id){this.id=BuildupMobTweaks.id(id);}
    public Identifier id(){return id;}public int interactionTier(){return 2;}
    protected boolean permits(Context c,net.minecraft.core.BlockPos p,InteractionPolicy.Operation operation){return InteractionPolicy.allows(c.level(),c.villager(),p,operation,BuildupMobTweaks.config().villagers.professions.protection_required.get());}
    protected int slot(Villager v,Predicate<ItemStack> filter){for(int i=0;i<v.getInventory().getContainerSize();i++)if(filter.test(v.getInventory().getItem(i)))return i;return -1;}
    protected <T extends LivingEntity> T choose(Context c,Class<T> type,Predicate<T> filter){
        int budget=ProfessionModule.scanBudget(c.level(),16);if(budget==0)return null;var candidates=new ArrayList<T>();
        c.level().getEntities(net.minecraft.world.level.entity.EntityTypeTest.forClass(type),c.villager().getBoundingBox().inflate(8),e->e!=c.villager()&&e.isAlive()&&c.level().hasChunkAt(e.blockPosition())&&ProfessionWorkState.targetReady(e)&&ProfessionModule.claimable(c.villager(),e)&&!e.entityTags().contains("buildupmobtweaks:work_excluded")&&c.villager().hasLineOfSight(e)&&filter.test(e),candidates,budget);
        return candidates.stream().sorted(Comparator.comparingDouble((T e)->e.distanceToSqr(c.villager())).thenComparing(e->e.getUUID().toString())).limit(16).findFirst().orElse(null);
    }
    public void start(Context c){ticks=workTicks=0;committed=false;if(target!=null&&!ProfessionModule.claim(c.villager(),target))throw new IllegalStateException("Target already claimed");if(target!=null)WorkMotion.look(c,target);}
    protected boolean valid(Context c){var walk=c.villager().getBrain().getMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.WALK_TARGET).orElse(null);if(walk!=null&&walk!=ProfessionWorkState.runtime(c.villager()).walk)return false;return ++ticks<=1100&&(target==null||target.isAlive()&&target.level()==c.level()&&ProfessionModule.claimable(c.villager(),target));}
    protected void commit(Context c){committed=true;ProfessionWorkState.defer(c.villager(),id,BuildupMobTweaks.config().villagers.professions.action_cooldown.get());if(target!=null)ProfessionWorkState.targetCooldown(target,BuildupMobTweaks.config().villagers.professions.action_cooldown.get());ProfessionWorkState.runtime(c.villager()).operations++;}
    public void stop(Context c,StopReason reason){}
}
