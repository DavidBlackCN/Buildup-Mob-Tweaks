package com.davidblackcn.buildupmobtweaks.villager;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import com.davidblackcn.buildupmobtweaks.feature.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.pathfinder.Path;
import java.util.List;

/** Before/after the native Brain tick: validate, observe and request bounded native retries. */
public final class VillagerCore {
    private static FeatureRegistry features;
    public static void register(FeatureRegistry registry) {
        features = registry;
        // Resolve attachment registration during initialization, not during the first entity tick.
        VillagerState.bootstrap();
        ServerEntityEvents.ENTITY_UNLOAD.register((e,l) -> { if (e instanceof Villager v) VillagerState.unload(v); });
    }
    static boolean enabled(FeatureId id) { return features != null && features.isEnabled(id); }
    public static void beforeBrain(Villager v, ServerLevel level) {
        var poi = ControlPolicy.query(v, level, ControlPolicy.Scope.POI);
        var nav = ControlPolicy.query(v, level, ControlPolicy.Scope.NAVIGATION);
        if (poi != ControlPolicy.Decision.ALLOW && nav != ControlPolicy.Decision.ALLOW) {
            if (VillagerState.hasRuntime(v)) { var s=VillagerState.runtime(v); s.resetProgress(); s.lastDecision=nav; }
            return;
        }
        var s = VillagerState.runtime(v); s.lastDecision = nav;
        long now = level.getGameTime();
        if (poi == ControlPolicy.Decision.ALLOW) {
            if (s.nextPoi == 0) s.nextPoi = now + Math.floorMod(v.getUUID().hashCode(), BuildupMobTweaks.config().villagers.poi_interval.get());
            if (now >= s.nextPoi) {
                s.nextPoi = now + BuildupMobTweaks.config().villagers.poi_interval.get();
                for (var memory : List.of(MemoryModuleType.JOB_SITE, MemoryModuleType.POTENTIAL_JOB_SITE, MemoryModuleType.HOME)) validatePoi(v, level, memory);
            }
        }
        if (nav != ControlPolicy.Decision.ALLOW) { s.resetProgress(); return; }
        if (v.hasAttached(VillagerState.DATA)) {
            var t = VillagerState.data(v);
            int wait = t.getIntOr("wait", 0);
            if (wait > 0) { var copy=t.copy(); copy.putInt("wait", wait-1); v.setAttached(VillagerState.DATA,copy); }
        }
        s.beforeTarget = v.getBrain().getMemory(MemoryModuleType.WALK_TARGET).orElse(null);
        s.beforePath = v.getNavigation().getPath();
        s.beforeNativeWalk = s.beforeTarget != null && nativeWalk(v,s.beforeTarget);
        inspectNavigation(v, level, true);
    }
    public static void afterBrain(Villager v, ServerLevel level) {
        if (ControlPolicy.query(v, level, ControlPolicy.Scope.NAVIGATION) != ControlPolicy.Decision.ALLOW) return;
        var s = VillagerState.runtime(v);
        // Both a null path and an exhausted partial native path may erase the target.
        // Do not classify successful arrival or an unrelated task's exit as a path failure.
        boolean failedStart = v.getBrain().getMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE).orElse(-1L) == level.getGameTime();
        boolean failedPartial = s.beforeNativeWalk && s.beforePath != null && !s.beforePath.canReach();
        if (s.beforeTarget != null && !v.getBrain().hasMemoryValue(MemoryModuleType.WALK_TARGET)
                && s.beforeTarget.getTarget().currentBlockPosition().distManhattan(v.blockPosition()) > s.beforeTarget.getCloseEnoughDist()
                && (failedStart || failedPartial)) {
            BlockPos failed = s.beforeTarget.getTarget().currentBlockPosition();
            int retries = VillagerState.matches(v, failed) ? VillagerState.data(v).getIntOr("retries",0) : 0;
            VillagerState.defer(v, failed, retries, BuildupMobTweaks.config().villagers.retry_ticks.get()); s.failures++;
        }
        inspectNavigation(v, level, false);
        s.beforeTarget = null; s.beforePath = null; s.beforeNativeWalk = false;
    }
    private static void validatePoi(Villager v, ServerLevel level, MemoryModuleType<GlobalPos> memory) {
        var current = v.getBrain().getMemory(memory);
        if (current.isEmpty()) return;
        var bound = current.get();
        // An unloaded/other-dimensional POI is unknown, not invalid. Never request its chunk.
        if (bound.dimension() != level.dimension() || !level.hasChunkAt(bound.pos())) return;
        var s=VillagerState.runtime(v); s.poiChecks++;
        var registered = level.getPoiManager().getType(bound.pos());
        var block = PoiTypes.forState(level.getBlockState(bound.pos()));
        // Block edits update the POI manager asynchronously. Wait for agreement before mutating memory.
        if (!registered.equals(block)) return;
        var predicate = Villager.POI_MEMORIES.get(memory);
        if (predicate == null || registered.filter(p -> predicate.test(v,p)).isPresent()) return;
        // No valid matching POI remains; releasePoi cannot release another type's ticket.
        if (!v.getBrain().getMemory(memory).filter(bound::equals).isPresent()) return;
        v.releasePoi(memory); v.getBrain().eraseMemory(memory); s.invalidPois++;
        var walk = v.getBrain().getMemory(MemoryModuleType.WALK_TARGET).orElse(null);
        if (walk != null && walk.getTarget().currentBlockPosition().equals(bound.pos())) cancelNativeWalk(v,walk);
        // AcquirePoi, AssignProfessionFromJobSite and native restocking handle reacquisition.
    }
    private static boolean nativeWalk(Villager v, WalkTarget walk) {
        var brain = v.getBrain(); Path path=v.getNavigation().getPath();
        return brain.getMemory(MemoryModuleType.WALK_TARGET).orElse(null) == walk
                && brain.getMemory(MemoryModuleType.PATH).orElse(null) == path
                && brain.getRunningBehaviors().stream().anyMatch(b -> b instanceof MoveToTargetSink);
    }
    private static void cancelNativeWalk(Villager v, WalkTarget walk) {
        if (!nativeWalk(v,walk)) return;
        var path=v.getNavigation().getPath();
        v.getNavigation().stop(); v.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        if (v.getBrain().getMemory(MemoryModuleType.PATH).orElse(null) == path) v.getBrain().eraseMemory(MemoryModuleType.PATH);
    }
    private static void inspectNavigation(Villager v, ServerLevel level, boolean sampleProgress) {
        var walk=v.getBrain().getMemory(MemoryModuleType.WALK_TARGET).orElse(null);
        var s=VillagerState.runtime(v); long now=level.getGameTime();
        if (walk == null) { s.target=null; s.anchor=null; return; }
        if (!nativeWalk(v,walk)) { s.target=null; s.anchor=null; return; }
        BlockPos target=walk.getTarget().currentBlockPosition();
        if (!level.hasChunkAt(target)) return;
        if (VillagerState.matches(v,target) && VillagerState.data(v).getIntOr("wait",0)>0) {
            cancelNativeWalk(v,walk); s.deferrals++; return;
        }
        Path path=v.getNavigation().getPath();
        if (NavigationSafety.hazardousPath(level,path)) {
            cancelNativeWalk(v,walk); VillagerState.defer(v,target,0,BuildupMobTweaks.config().villagers.retry_ticks.get());
            s.hazardStops++; s.target=null; s.anchor=null; return;
        }
        if (!sampleProgress || now < s.nextNavigation) return;
        s.nextNavigation=now+BuildupMobTweaks.config().villagers.navigation_interval.get();
        if (target.distManhattan(v.blockPosition()) <= walk.getCloseEnoughDist()) { s.target=null; s.anchor=null; return; }
        if (!target.equals(s.target) || s.anchor == null) { s.target=target.immutable(); s.anchor=v.position(); s.lastProgress=now; return; }
        if (v.position().distanceToSqr(s.anchor) >= 0.25) {
            s.anchor=v.position(); s.lastProgress=now;
            if (VillagerState.matches(v,target)) VillagerState.clearRetry(v);
            return;
        }
        if (now-s.lastProgress < BuildupMobTweaks.config().villagers.stuck_ticks.get()) return;
        s.stuckEvents++; s.lastProgress=now;
        int retries=VillagerState.matches(v,target) ? VillagerState.data(v).getIntOr("retries",0) : 0;
        if (retries < BuildupMobTweaks.config().villagers.max_retries.get()) {
            // Same native task and path; recomputePath uses the navigation's own target and native throttle.
            v.getNavigation().recomputePath(); s.recomputes++; VillagerState.defer(v,target,retries+1,0);
        } else {
            cancelNativeWalk(v,walk); VillagerState.defer(v,target,0,BuildupMobTweaks.config().villagers.retry_ticks.get());
            s.deferrals++; s.target=null; s.anchor=null;
        }
    }
    private VillagerCore() {}
}
