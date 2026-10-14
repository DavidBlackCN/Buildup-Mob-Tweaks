package com.davidblackcn.buildupmobtweaks.villager;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.server.level.ServerLevel;

/** Internal Core policy. No external registration or Brain ownership inference. */
public final class ControlPolicy {
    public enum Scope { POI, NAVIGATION, WORK }
    public enum Decision { ALLOW, DISABLED, EXCLUDED, VANILLA_PRIORITY, UNKNOWN_STATE }
    private static final TagKey<EntityType<?>> EXCLUDED = TagKey.create(Registries.ENTITY_TYPE, BuildupMobTweaks.id("villager_core_excluded"));
    public static Decision query(Villager v, ServerLevel level, Scope scope) {
        FeatureId feature = switch(scope) {
            case POI -> FeatureId.VILLAGER_POI_INTELLIGENCE;
            case NAVIGATION -> FeatureId.VILLAGER_NAVIGATION_SAFETY;
            case WORK -> FeatureId.VILLAGER_SCHEDULE_COORDINATION;
        };
        if (!VillagerCore.enabled(feature)) return Decision.DISABLED;
        var tags = v.entityTags();
        String profession = v.getVillagerData().profession().unwrapKey().map(k -> k.identifier().toString()).orElse("unknown");
        if (v.getType() != EntityTypes.VILLAGER || v.getType().builtInRegistryHolder().is(EXCLUDED)
                || tags.contains("buildupmobtweaks:vanilla_ai") || tags.contains("buildupmobtweaks:disable_villager_core")
                || tags.contains("buildupmobtweaks:disable_" + feature.id().getPath())
                || tags.contains("buildupmobtweaks:disable_villager_profession/" + profession)) return Decision.EXCLUDED;
        if (!VillagerState.known(v)) return Decision.UNKNOWN_STATE;
        var brain = v.getBrain();
        var required = switch(scope) {
            case POI -> java.util.List.of(MemoryModuleType.JOB_SITE, MemoryModuleType.POTENTIAL_JOB_SITE, MemoryModuleType.HOME, MemoryModuleType.WALK_TARGET, MemoryModuleType.PATH);
            case NAVIGATION -> java.util.List.of(MemoryModuleType.WALK_TARGET, MemoryModuleType.PATH, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
            case WORK -> java.util.List.of(MemoryModuleType.JOB_SITE, MemoryModuleType.WALK_TARGET, MemoryModuleType.LOOK_TARGET, MemoryModuleType.PATH);
        };
        if (required.stream().anyMatch(m -> !brain.checkMemory(m, net.minecraft.world.entity.ai.memory.MemoryStatus.REGISTERED))) return Decision.UNKNOWN_STATE;
        var raid = level.getRaidAt(v.blockPosition());
        if (!v.isAlive() || v.isNoAi() || v.isPassenger() || v.isSleeping() || v.isTrading()
                || brain.isActive(Activity.PANIC) || brain.isActive(Activity.RAID) || brain.isActive(Activity.PRE_RAID)
                || brain.isActive(Activity.HIDE) || brain.hasMemoryValue(MemoryModuleType.HURT_BY)
                || brain.hasMemoryValue(MemoryModuleType.NEAREST_HOSTILE)
                || brain.hasMemoryValue(MemoryModuleType.HURT_BY_ENTITY)
                || raid != null && raid.isActive() && !raid.isOver()) return Decision.VANILLA_PRIORITY;
        return Decision.ALLOW;
    }
    private ControlPolicy() {}
}
