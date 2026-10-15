package com.davidblackcn.buildupmobtweaks.villager.farmer;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;
import java.util.LinkedHashSet;
import java.util.Set;

/** Select feet positions for native WorkAtComposter; JOB_SITE always remains the real POI. */
public final class WorkstationNavigation {
    // WorkAtPoi's native interaction radius, measured from body feet to POI center.
    private static final double WORK_DISTANCE=1.73;
    public static boolean controls(Villager v,ServerLevel l){return FarmerModule.eligible(v,l);}
    public static boolean isJobTarget(Villager v,ServerLevel l,BlockPos target){
        if(!controls(v,l)||!l.hasChunkAt(target)||!l.getBlockState(target).is(Blocks.COMPOSTER))return false;
        return v.getBrain().getMemory(MemoryModuleType.JOB_SITE)
                .filter(j->j.dimension()==l.dimension()&&j.pos().equals(target)).isPresent();
    }
    public static Set<BlockPos> standingTargets(ServerLevel l,BlockPos job){
        var result=new LinkedHashSet<BlockPos>();
        for(int dy=-1;dy<=1;dy++)for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++){
            if(dx==0&&dz==0)continue;
            var feet=job.offset(dx,dy,dz);
            if(!FarmNavigation.safeFoot(l,feet))continue;
            var body=new Vec3(feet.getX()+.5,WalkNodeEvaluator.getFloorLevel(l,feet),feet.getZ()+.5);
            if(job.closerToCenterThan(body,WORK_DISTANCE))result.add(feet);
        }
        return result;
    }
    public static boolean safeApproach(ServerLevel l,Path path,Set<BlockPos> goals){
        if(path==null||!path.canReach()||path.getNodeCount()==0||!goals.contains(path.getEndNode().asBlockPos()))return false;
        for(int i=0;i<path.getNodeCount();i++){
            var p=path.getNodePos(i);
            // A farmer loaded or pushed onto a composter may leave its rim or hollow interior once, never enter it again.
            boolean escape=i==0&&composterFooting(l,p);
            if(!escape&&!FarmNavigation.safeFoot(l,p))return false;
            if(i==0)continue;
            var before=path.getNodePos(i-1);
            boolean exit=i==1&&composterFooting(l,before)
                    &&Math.abs(before.getX()-p.getX())+Math.abs(before.getZ()-p.getZ())==1
                    &&WalkNodeEvaluator.getFloorLevel(l,before)-WalkNodeEvaluator.getFloorLevel(l,p)>=0
                    &&WalkNodeEvaluator.getFloorLevel(l,before)-WalkNodeEvaluator.getFloorLevel(l,p)<=1.125;
            if(!exit&&!FarmNavigation.safeEdge(l,before,p))return false;
        }
        return true;
    }
    private static boolean composterFooting(ServerLevel l,BlockPos p){
        return l.getBlockState(p).is(Blocks.COMPOSTER)||l.getBlockState(p.below()).is(Blocks.COMPOSTER);
    }
    private WorkstationNavigation(){}
}
