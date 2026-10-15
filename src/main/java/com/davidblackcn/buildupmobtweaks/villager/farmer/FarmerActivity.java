package com.davidblackcn.buildupmobtweaks.villager.farmer;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import com.davidblackcn.buildupmobtweaks.villager.*;
import net.minecraft.core.*;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.*;
import java.util.*;

/** One native work slot runs one crop target. Failed targets are skipped for the next scan cycle. */
public final class FarmerActivity implements WorkActivity {
    private record Target(ServerLevel level,BlockPos pos){}
    private record Claim(Villager owner,long expiry){}
    private static final Map<Target,Claim> CLAIMS=new HashMap<>();
    private static final Map<Integer,List<BlockPos>> OFFSETS=new HashMap<>();
    private static final Map<ServerLevel,Budget> BUDGETS=new WeakHashMap<>();
    private static final class Budget {long tick=-1;int remaining;}
    private static List<BlockPos> offsets(int r){return OFFSETS.computeIfAbsent(r,n->{var out=new ArrayList<BlockPos>();for(int x=-n;x<=n;x++)for(int z=-n;z<=n;z++)for(int y=-1;y<=1;y++)out.add(new BlockPos(x,y,z));out.sort(Comparator.comparingInt((BlockPos p)->p.getX()*p.getX()+p.getY()*p.getY()+p.getZ()*p.getZ()).thenComparingLong(BlockPos::asLong));return List.copyOf(out);});}
    private static int budget(ServerLevel l,int requested){var b=BUDGETS.computeIfAbsent(l,k->new Budget());if(b.tick!=l.getGameTime()){b.tick=l.getGameTime();b.remaining=BuildupMobTweaks.config().villagers.farmer.world_scan_budget.get();}int n=Math.min(requested,b.remaining);b.remaining-=n;return n;}
    public static void releaseClaims(Villager v){CLAIMS.entrySet().removeIf(e->e.getValue().owner()==v);}
    public static void clearClaims(MinecraftServer s){CLAIMS.keySet().removeIf(t->t.level().getServer()==s);}
    public Identifier id(){return BuildupMobTweaks.id("farmer_work");}public int interactionTier(){return 1;}
    public boolean canStart(Context ctx){
        var v=ctx.villager();var l=ctx.level();if(!FarmerModule.eligible(v,l)||FarmerState.peek(v).map(s->s.fault||s.active).orElse(false))return false;
        var s=FarmerState.runtime(v);var snapshot=CropProfiles.get(l.getServer());
        if(v.hasAttached(FarmerState.DATA)&&FarmerState.data(v).getIntOr("cooldown",0)>0)return false;
        if(ScheduleCoordination.nativeWorkSlot(v,l,ctx.profile()).isEmpty())return false;
        long now=l.getGameTime();if(now<s.nextScan)return false;
        if(FarmerState.pending(v))return restoreTarget(ctx,snapshot);
        if(!snapshot.generation().equals(s.generation)){s.generation=snapshot.generation();s.cursor=0;s.failed.clear();}
        var job=v.getBrain().getMemory(MemoryModuleType.JOB_SITE).orElseThrow();
        if(job.dimension()!=l.dimension()||!l.hasChunkAt(job.pos()))return false;
        if(!job.pos().equals(s.scanCenter)){s.scanCenter=job.pos();s.cursor=0;s.failed.clear();}
        int r=BuildupMobTweaks.config().villagers.farmer.radius.get();var offsets=offsets(r);int total=offsets.size();
        var candidates=new ArrayList<BlockPos>();
        int budget=budget(l,BuildupMobTweaks.config().villagers.farmer.scan_budget.get());
        for(int n=0;n<budget&&s.cursor<total;n++,s.cursor++){
            var p=job.pos().offset(offsets.get(s.cursor));
            if(!l.hasChunkAt(p))continue;s.scanned++;
            var c=snapshot.byCrop().get(l.getBlockState(p).getBlock());
            if(c!=null&&!s.failed.contains(p)&&CropHandlers.find(c.handler()).orElseThrow().mature(l,p,c)
                    &&l.getBlockState(p.below()).is(c.substrate())&&c.replant().canSurvive(l,p)&&hasMaterial(v,c)&&hasRoom(v)
                    &&permits(l,v,p,InteractionPolicy.Operation.HARVEST)&&permits(l,v,p,InteractionPolicy.Operation.REPLANT)&&available(l,p,v))candidates.add(p);
        }
        if(s.cursor>=total){s.cursor=0;s.nextScan=now+BuildupMobTweaks.config().villagers.farmer.scan_interval.get();s.failed.clear();}
        if(candidates.isEmpty())return false;
        // Local continuity changes selection, never the seed or the type planted in an empty block.
        var nearest=candidates.stream().min(Comparator.comparingDouble((BlockPos p)->p.distToCenterSqr(v.position())).thenComparingLong(BlockPos::asLong)).orElseThrow();
        var preferred=s.lastCrop!=null&&s.lastCropPos!=null&&s.lastCropPos.closerToCenterThan(v.position(),5)
                ?s.lastCrop:snapshot.byCrop().get(l.getBlockState(nearest).getBlock()).id();
        java.util.function.ToIntFunction<BlockPos> group=p->snapshot.byCrop().get(l.getBlockState(p).getBlock()).id().equals(preferred)&&p.closerToCenterThan(v.position(),5)?0:1;
        candidates.sort(Comparator.comparingInt(group).thenComparingDouble(p->p.distToCenterSqr(v.position())).thenComparingLong(BlockPos::asLong));
        BlockPos best=null;FarmNavigation.Route route=null;int bestGroup=2;
        // A fixed four-query shortlist keeps the existing per-attempt path budget.
        for(var p:candidates.stream().limit(4).toList()){
            s.pathRequests++;var found=FarmNavigation.plan(l,v,p);
            if(found==null){s.failed.add(p);continue;}
            int g=group.applyAsInt(p);
            if(best==null||g<bestGroup||g==bestGroup&&(found.cost()<route.cost()||found.cost()==route.cost()&&(found.length()<route.length()||found.length()==route.length()&&p.asLong()<best.asLong()))){best=p;route=found;bestGroup=g;}
        }
        if(best==null)return false;s.target=best;s.crop=snapshot.byCrop().get(l.getBlockState(best).getBlock());s.route=route;return true;
    }
    private boolean restoreTarget(Context ctx,CropProfiles.Snapshot snapshot){
        var v=ctx.villager();var l=ctx.level();var t=FarmerState.data(v);var s=FarmerState.runtime(v);
        if(!t.getStringOr("dimension","").equals(l.dimension().identifier().toString())){s.reason="pending_other_dimension";return false;}
        var p=BlockPos.of(t.getLongOr("pos",0));if(!l.hasChunkAt(p)){s.reason="pending_unloaded";return false;}
        var c=snapshot.rules().get(Identifier.parse(t.getStringOr("profile","")));
        if(c==null||!c.signature().equals(t.getStringOr("signature",""))){s.reason="pending_rule_missing_or_changed";return false;}
        if(!l.getBlockState(p).isAir()||!l.getBlockState(p.below()).is(c.substrate())){
            // Prepared-but-not-harvested or an intervening player/vanilla plant is never replayed/overwritten.
            FarmerState.clear(v,BuildupMobTweaks.config().villagers.farmer.action_interval.get());s.reason="pending_world_changed";return false;
        }
        if(!p.closerToCenterThan(net.minecraft.world.phys.Vec3.atCenterOf(v.getBrain().getMemory(MemoryModuleType.JOB_SITE).orElseThrow().pos()),ctx.profile().workRadius())){
            FarmerState.clear(v,BuildupMobTweaks.config().villagers.farmer.action_interval.get());s.reason="pending_outside_new_work_area";return false;
        }
        if(!hasMaterial(v,c)){s.reason="pending_waiting_seed";return false;}
        if(!permits(l,v,p,InteractionPolicy.Operation.REPLANT)){s.reason="pending_permission_denied";return false;}
        if(!available(l,p,v)){s.reason="pending_target_claimed";return false;}
        if(!safePath(l,v,p,s)){s.reason="pending_path_unsafe";return false;}
        s.target=p;s.crop=c;return true;
    }
    private static boolean permits(ServerLevel l,Villager v,BlockPos p,InteractionPolicy.Operation op){return InteractionPolicy.allows(l,v,p,op);}
    private static boolean hasRoom(Villager v){for(int i=0;i<v.getInventory().getContainerSize();i++)if(v.getInventory().getItem(i).isEmpty())return true;return false;}
    private static boolean hasMaterial(Villager v,CropProfile c){return v.getInventory().countItem(c.seed())>=c.count();}
    private static boolean available(ServerLevel l,BlockPos p,Villager v){var claim=CLAIMS.get(new Target(l,p));return claim==null||claim.owner()==v||claim.owner().isRemoved()||claim.expiry()<l.getGameTime();}
    private static boolean safePath(ServerLevel l,Villager v,BlockPos p,FarmerState.Runtime s){
        s.pathRequests++;s.route=FarmNavigation.plan(l,v,p);return s.route!=null;
    }
    public void start(Context ctx){var v=ctx.villager();var s=FarmerState.runtime(v);
        if(s.target==null||s.route==null||!available(ctx.level(),s.target,v))return;
        s.active=true;s.done=false;s.routeInvalidated=false;s.starts++;s.reason="working";s.deadline=ctx.level().getGameTime()+BuildupMobTweaks.config().villagers.farmer.target_timeout.get();
        CLAIMS.put(new Target(ctx.level(),s.target),new Claim(v,s.deadline));FarmerModule.ACTIVE.put(v,this);
        s.look=new BlockPosTracker(s.target);s.walk=new WalkTarget(s.look,0.5F,1);s.path=s.route.path();
        v.getBrain().setMemory(MemoryModuleType.LOOK_TARGET,s.look);
        if(s.path!=null){
            v.getBrain().setMemory(MemoryModuleType.WALK_TARGET,s.walk);
            // PATH reserves the native movement slot; native navigation still runs movement physics.
            v.getBrain().setMemory(MemoryModuleType.PATH,s.path);
            if(!v.getNavigation().moveTo(s.path,0.5))FarmerModule.stop(v);
        }
    }
    public Result tick(Context ctx){var v=ctx.villager();var l=ctx.level();var s=FarmerState.runtime(v);
        if(!s.active||s.done||s.routeInvalidated||!FarmerModule.eligible(v,l)||l.getGameTime()>s.deadline)return Result.FAILED;
        var snapshot=CropProfiles.get(l.getServer());var c=s.crop;
        if(!snapshot.rules().containsKey(c.id())||!snapshot.rules().get(c.id()).signature().equals(c.signature())||!l.hasChunkAt(s.target))return Result.FAILED;
        if(!FarmNavigation.canWorkFrom(l,v,s.target)){
            if(!FarmNavigation.ownsRoute(v)||v.getNavigation().isDone()||!FarmNavigation.safeRemaining(l,s.path,s.path.getNextNodeIndex()))return Result.FAILED;
            return Result.CONTINUE;
        }
        if(!hasMaterial(v,c))return Result.FAILED;
        var handler=CropHandlers.find(c.handler()).orElseThrow();
        if(!FarmerState.pending(v)){
            if(!hasRoom(v)||!handler.mature(l,s.target,c)||!l.getBlockState(s.target.below()).is(c.substrate())||!c.replant().canSurvive(l,s.target)
                    ||!permits(l,v,s.target,InteractionPolicy.Operation.HARVEST)||!permits(l,v,s.target,InteractionPolicy.Operation.REPLANT))return Result.FAILED;
            FarmerState.pending(v,s.target,c,"prepared");
            if(!handler.harvest(l,v,s.target,c)){FarmerState.clear(v,BuildupMobTweaks.config().villagers.farmer.action_interval.get());return Result.FAILED;}
            FarmerState.pending(v,s.target,c,"harvested");s.harvests++;
        }
        if(!l.getBlockState(s.target).isAir()){FarmerState.clear(v,BuildupMobTweaks.config().villagers.farmer.action_interval.get());s.done=true;return Result.COMPLETE;}
        if(!l.getBlockState(s.target.below()).is(c.substrate())){FarmerState.clear(v,BuildupMobTweaks.config().villagers.farmer.action_interval.get());return Result.FAILED;}
        if(!permits(l,v,s.target,InteractionPolicy.Operation.REPLANT)||!hasMaterial(v,c))return Result.FAILED;
        // Single server-thread operation: no item leaves native inventory until successful placement.
        if(!handler.plant(l,v,s.target,c))return Result.FAILED;
        int left=c.count();for(int i=0;i<v.getInventory().getContainerSize()&&left>0;i++)if(v.getInventory().getItem(i).is(c.seed()))left-=v.getInventory().removeItem(i,left).getCount();
        if(left!=0)throw new IllegalStateException("Reserved seed missing during same-thread placement");
        s.replants++;s.lastCrop=c.id();s.lastCropPos=s.target;s.done=true;FarmerState.clear(v,BuildupMobTweaks.config().villagers.farmer.action_interval.get());return Result.COMPLETE;
    }
    public void stop(Context ctx,StopReason reason){var v=ctx.villager();var optional=FarmerState.peek(v);if(optional.isEmpty())return;var s=optional.get();
        if(!s.active)return;s.active=false;s.stops++;s.reason=reason.name().toLowerCase(Locale.ROOT);
        var key=new Target(ctx.level(),s.target);var claim=CLAIMS.get(key);if(claim!=null&&claim.owner()==v)CLAIMS.remove(key);
        var brain=v.getBrain();
        if(s.path!=null&&v.getNavigation().getPath()==s.path)v.getNavigation().stop();
        if(s.path!=null&&brain.checkMemory(MemoryModuleType.PATH,MemoryStatus.REGISTERED)&&brain.getMemory(MemoryModuleType.PATH).orElse(null)==s.path)brain.eraseMemory(MemoryModuleType.PATH);
        if(brain.checkMemory(MemoryModuleType.WALK_TARGET,MemoryStatus.REGISTERED)&&brain.getMemory(MemoryModuleType.WALK_TARGET).orElse(null)==s.walk)brain.eraseMemory(MemoryModuleType.WALK_TARGET);
        if(brain.checkMemory(MemoryModuleType.LOOK_TARGET,MemoryStatus.REGISTERED)&&brain.getMemory(MemoryModuleType.LOOK_TARGET).orElse(null)==s.look)brain.eraseMemory(MemoryModuleType.LOOK_TARGET);
        if(!s.done){s.failed.add(s.target);s.nextScan=ctx.level().getGameTime()+BuildupMobTweaks.config().villagers.farmer.scan_interval.get();}
        FarmerModule.ACTIVE.remove(v);s.walk=null;s.look=null;s.target=null;s.crop=null;s.path=null;s.route=null;
    }
}
