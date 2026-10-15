package com.davidblackcn.buildupmobtweaks.villager.profession;
import com.davidblackcn.buildupmobtweaks.villager.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.*;
import net.minecraft.world.phys.Vec3;

/** Ordinary native movement, without Farmer terrain costs or borrowed equipment. */
public final class WorkMotion {
    public static boolean clearFoot(net.minecraft.server.level.ServerLevel l,BlockPos p){
        return l.hasChunkAt(p)&&l.hasChunkAt(p.above())&&!NavigationSafety.hazardous(l,p)
                &&l.getBlockState(p).getCollisionShape(l,p).isEmpty()&&l.getBlockState(p.above()).getCollisionShape(l,p.above()).isEmpty()
                &&!l.getBlockState(p.below()).getCollisionShape(l,p.below()).isEmpty();
    }
    public static boolean reach(WorkActivity.Context c,BlockPos pos,double range){
        var v=c.villager();var r=ProfessionWorkState.runtime(v);var b=v.getBrain();var current=b.getMemory(MemoryModuleType.WALK_TARGET).orElse(null);
        if(current!=null&&current!=r.walk)return false;
        if(current==r.walk&&current!=null)r.path=v.getNavigation().getPath();
        if(v.position().distanceToSqr(Vec3.atBottomCenterOf(pos))<=range*range){
            if(current!=null){v.getNavigation().stop();b.eraseMemory(MemoryModuleType.WALK_TARGET);if(r.path!=null&&b.getMemory(MemoryModuleType.PATH).orElse(null)==r.path)b.eraseMemory(MemoryModuleType.PATH);r.walk=null;r.path=null;}
            return true;
        }
        if(!clearFoot(c.level(),pos))return false;
        if(current==null){r.walk=new WalkTarget(pos,.5F,0);b.setMemory(MemoryModuleType.WALK_TARGET,r.walk);}
        if(v.getNavigation().getPath()!=null&&NavigationSafety.hazardousPath(c.level(),v.getNavigation().getPath()))return false;
        return false;
    }
    public static void look(WorkActivity.Context c,Entity entity){var r=ProfessionWorkState.runtime(c.villager());r.look=new EntityTracker(entity,true);c.villager().getBrain().setMemory(MemoryModuleType.LOOK_TARGET,r.look);}
    public static void look(WorkActivity.Context c,BlockPos pos){var r=ProfessionWorkState.runtime(c.villager());r.look=new BlockPosTracker(pos);c.villager().getBrain().setMemory(MemoryModuleType.LOOK_TARGET,r.look);}
    private WorkMotion(){}
}
