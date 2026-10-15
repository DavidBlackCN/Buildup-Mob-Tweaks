package com.davidblackcn.buildupmobtweaks.villager.farmer;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import com.davidblackcn.buildupmobtweaks.villager.NavigationSafety;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.pathfinder.*;
import java.util.*;

/** A bounded native path search for one work target. No replacement navigator or terrain edits. */
public final class FarmNavigation {
    public record Route(Path path,double cost,int length){}
    private static final int MAX_NODES=512;
    public static boolean canWorkFrom(ServerLevel l,Villager v,BlockPos target){
        var feet=BlockPos.containing(v.getX(),v.getY()+0.5,v.getZ());
        return l.hasChunkAt(target)&&Math.abs(v.getY()-target.getY())<=0.65
                &&target.closerToCenterThan(v.position(),1.8)&&safeFoot(l,feet);
    }
    public static Route plan(ServerLevel l,Villager v,BlockPos target){
        if(!v.onGround()||!l.hasChunkAt(target))return null;
        if(canWorkFrom(l,v,target))return new Route(null,0,0);
        var start=v.blockPosition();int margin=4;
        var min=new BlockPos(Math.min(start.getX(),target.getX())-margin,Math.min(start.getY(),target.getY())-3,Math.min(start.getZ(),target.getZ())-margin);
        var max=new BlockPos(Math.max(start.getX(),target.getX())+margin,Math.max(start.getY(),target.getY())+3,Math.max(start.getZ(),target.getZ())+margin);
        var evaluator=new FarmNodes(l,target,min,max);
        // Preserve native door capabilities, without mutating the villager's global path maluses.
        var nativeNodes=v.getNavigation().getNodeEvaluator();
        evaluator.setCanPassDoors(nativeNodes.canPassDoors());evaluator.setCanOpenDoors(nativeNodes.canOpenDoors());
        var region=new PathNavigationRegion(l,min,max); // getChunkNow only in 26.3
        var finder=new PathFinder(evaluator,MAX_NODES);
        Path path;
        try{path=finder.findPath(region,v,Set.of(target),Math.min(48,BuildupMobTweaks.config().villagers.farmer.radius.get()*3+8),0,1);}
        finally{if(evaluator.prepared)evaluator.done();}
        if(path==null||!path.canReach()||!safeRemaining(l,path,0))return null;
        double cost=0;for(int i=1;i<path.getNodeCount();i++)cost+=Math.sqrt(path.getNodePos(i).distSqr(path.getNodePos(i-1)))+path.getNode(i).costMalus;
        return new Route(path,cost,path.getNodeCount());
    }
    public static boolean ownsRoute(Villager v){return FarmerState.peek(v).map(s->s.active&&s.path!=null
            &&v.getNavigation().getPath()==s.path&&v.getBrain().hasMemoryValue(net.minecraft.world.entity.ai.memory.MemoryModuleType.WALK_TARGET)
            &&v.getBrain().getMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.WALK_TARGET).orElse(null)==s.walk).orElse(false);}
    public static boolean handlesRecompute(Villager v){
        if(!ownsRoute(v)||!(v.level() instanceof ServerLevel l))return false;
        var s=FarmerState.runtime(v);
        // A safe door/crop update need not discard the route; unsafe changes retire it on the next tick.
        if(!safeRemaining(l,s.path,s.path.getNextNodeIndex()))s.routeInvalidated=true;
        return true;
    }
    public static boolean safeRemaining(ServerLevel l,Path path,int start){
        int from=Math.max(0,start),end=Math.min(path.getNodeCount(),from+5);
        for(int i=from;i<end;i++)if(!safeFoot(l,path.getNodePos(i))||i>0&&!safeEdge(l,path.getNodePos(i-1),path.getNodePos(i)))return false;
        return true;
    }
    private static boolean safeFoot(ServerLevel l,BlockPos p){
        // A body-width margin is required even at a chunk boundary. Unknown space is not free space.
        for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)if(!l.hasChunkAt(p.offset(x,0,z)))return false;
        if(NavigationSafety.hazardous(l,p)||l.getBlockState(p).getFluidState().is(FluidTags.WATER)
                ||l.getBlockState(p).is(Blocks.SWEET_BERRY_BUSH)||l.getBlockState(p).is(Blocks.WITHER_ROSE))return false;
        if(!bodySpace(l,p)||!bodySpace(l,p.above()))return false;
        return !l.getBlockState(p.below()).getCollisionShape(l,p.below()).isEmpty();
    }
    private static boolean bodySpace(ServerLevel l,BlockPos p){
        var state=l.getBlockState(p);return state.getCollisionShape(l,p).isEmpty()||state.is(net.minecraft.tags.BlockTags.WOODEN_DOORS);
    }
    private static boolean stair(ServerLevel l,BlockPos p,Direction up){
        var state=l.getBlockState(p.below());return state.getBlock() instanceof StairBlock&&state.getValue(StairBlock.HALF)==Half.BOTTOM
                &&state.getValue(StairBlock.SHAPE)==StairsShape.STRAIGHT&&state.getValue(StairBlock.FACING)==up;
    }
    private static boolean safeEdge(ServerLevel l,BlockPos a,BlockPos b){
        if(!safeFoot(l,a)||!safeFoot(l,b))return false;
        if(a.getX()!=b.getX()&&a.getZ()!=b.getZ()&&(!safeFoot(l,new BlockPos(a.getX(),b.getY(),b.getZ()))||!safeFoot(l,new BlockPos(b.getX(),b.getY(),a.getZ()))))return false;
        double dy=WalkNodeEvaluator.getFloorLevel(l,b)-WalkNodeEvaluator.getFloorLevel(l,a);
        if(Math.abs(dy)<=0.625)return true;
        int dx=b.getX()-a.getX(),dz=b.getZ()-a.getZ();if(Math.abs(dx)+Math.abs(dz)!=1||Math.abs(dy)>1.01)return false;
        Direction direction=dx==1?Direction.EAST:dx==-1?Direction.WEST:dz==1?Direction.SOUTH:Direction.NORTH;
        return dy>0?stair(l,b,direction):stair(l,a,direction.getOpposite());
    }
    private static float terrainCost(ServerLevel l,BlockPos p,BlockPos target){
        float cost=0;
        if(!p.equals(target)){
            if(l.getBlockState(p.below()).getBlock() instanceof FarmlandBlock)cost+=2;
            if(CropProfiles.get(l.getServer()).byCrop().containsKey(l.getBlockState(p).getBlock()))cost+=4;
        }
        for(var d:Direction.Plane.HORIZONTAL){var side=p.relative(d);if(l.hasChunkAt(side)&&l.getBlockState(side).getFluidState().is(FluidTags.WATER))cost+=1;}
        return cost;
    }
    private static final class FarmNodes extends WalkNodeEvaluator {
        final ServerLevel level;final BlockPos target,min,max;boolean prepared;
        FarmNodes(ServerLevel l,BlockPos target,BlockPos min,BlockPos max){this.level=l;this.target=target;this.min=min;this.max=max;}
        @Override public void prepare(PathNavigationRegion region,Mob mob){super.prepare(region,mob);prepared=true;}
        @Override public void done(){if(prepared){super.done();prepared=false;}}
        @Override public PathType getPathTypeOfMob(PathfindingContext ctx,int x,int y,int z,Mob mob){
            if(x<min.getX()||x>max.getX()||y<min.getY()||y>max.getY()||z<min.getZ()||z>max.getZ()||!level.hasChunkAt(new BlockPos(x,0,z)))return PathType.BLOCKED;
            return super.getPathTypeOfMob(ctx,x,y,z,mob);
        }
        @Override public int getNeighbors(Node[] out,Node from){
            int count=super.getNeighbors(out,from),kept=0;var a=new BlockPos(from.x,from.y,from.z);
            for(int i=0;i<count;i++){var n=out[i];var b=new BlockPos(n.x,n.y,n.z);
                if(safeEdge(level,a,b)){n.costMalus=Math.max(n.costMalus,terrainCost(level,b,target));out[kept++]=n;}}
            return kept;
        }
    }
    private FarmNavigation(){}
}
