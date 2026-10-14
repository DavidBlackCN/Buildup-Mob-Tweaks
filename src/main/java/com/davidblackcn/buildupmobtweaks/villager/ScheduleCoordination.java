package com.davidblackcn.buildupmobtweaks.villager;

import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.schedule.Activity;
import java.util.Optional;

/** Read-only profession entry. Native WorkAtPoi/restocking and all schedules keep their priority. */
public final class ScheduleCoordination {
    public static Optional<WorkActivity.Context> workContext(Villager v, ServerLevel level, ProfessionProfile profile) {
        if (v.level() != level || ControlPolicy.query(v, level, ControlPolicy.Scope.WORK) != ControlPolicy.Decision.ALLOW
                || v.isBaby() || !v.getVillagerData().profession().is(profile.profession())
                || !v.getBrain().isActive(Activity.WORK) || v.getNavigation().isInProgress()
                || v.getBrain().hasMemoryValue(MemoryModuleType.WALK_TARGET)
                || !v.getBrain().getRunningBehaviors().isEmpty()) return Optional.empty();
        Optional<GlobalPos> site = v.getBrain().getMemory(MemoryModuleType.JOB_SITE);
        if (site.isEmpty() || site.get().dimension() != level.dimension()
                || !level.hasChunkAt(site.get().pos())
                || !site.get().pos().closerToCenterThan(v.position(), profile.workRadius())
                || !level.getPoiManager().existsAtPosition(profile.workPoi(), site.get().pos())) return Optional.empty();
        return Optional.of(new WorkActivity.Context(level, v, profile));
    }
    private ScheduleCoordination() {}
}
