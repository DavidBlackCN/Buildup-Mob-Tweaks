package com.davidblackcn.buildupmobtweaks.villager;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import net.fabricmc.fabric.api.attachment.v1.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.phys.Vec3;

/** Native POIs/trades are never duplicated. Only retry delay survives entity serialization. */
public final class VillagerState {
    public static final AttachmentType<CompoundTag> DATA = AttachmentRegistry.createPersistent(BuildupMobTweaks.id("villager_core"), CompoundTag.CODEC);
    private static final AttachmentType<Runtime> RUNTIME = AttachmentRegistry.createDefaulted(BuildupMobTweaks.id("villager_core_runtime"), Runtime::new);
    public static final class Runtime {
        long nextPoi, nextNavigation, lastProgress;
        BlockPos target;
        Vec3 anchor;
        WalkTarget beforeTarget;
        net.minecraft.world.level.pathfinder.Path beforePath;
        boolean beforeNativeWalk;
        public int poiChecks, invalidPois, stuckEvents, recomputes, deferrals, hazardStops, failures;
        public ControlPolicy.Decision lastDecision = ControlPolicy.Decision.DISABLED;
        void resetProgress() { target = null; anchor = null; beforeTarget = null; beforePath = null; beforeNativeWalk = false; lastProgress = 0; }
    }
    public static Runtime runtime(Villager v) { return v.getAttachedOrCreate(RUNTIME); }
    public static boolean hasRuntime(Villager v) { return v.hasAttached(RUNTIME); }
    public static void unload(Villager v) { v.removeAttached(RUNTIME); }
    public static boolean known(Villager v) {
        if (!v.hasAttached(DATA)) return true;
        var t = v.getAttached(DATA);
        if (t.getIntOr("version", -1) != 1) return false;
        if (!t.contains("target")) return t.keySet().equals(java.util.Set.of("version"));
        return t.keySet().equals(java.util.Set.of("version", "target", "dimension", "wait", "retries"))
                && t.getLong("target").isPresent() && t.getString("dimension").filter(s -> net.minecraft.resources.Identifier.tryParse(s) != null).isPresent()
                && t.getIntOr("wait", -1) >= 0 && t.getIntOr("wait", 1201) <= 1200
                && t.getIntOr("retries", -1) >= 0 && t.getIntOr("retries", 6) <= 5;
    }
    public static java.util.Optional<Runtime> peekRuntime(Villager v) {
        return v.hasAttached(RUNTIME) ? java.util.Optional.of(v.getAttached(RUNTIME)) : java.util.Optional.empty();
    }
    static CompoundTag data(Villager v) {
        if (!v.hasAttached(DATA)) { var t = new CompoundTag(); t.putInt("version", 1); v.setAttached(DATA, t); }
        return v.getAttached(DATA);
    }
    static boolean matches(Villager v, BlockPos target) {
        if (!v.hasAttached(DATA)) return false;
        var t = v.getAttached(DATA);
        return t.getLong("target").filter(p -> p == target.asLong()).isPresent()
                && t.getStringOr("dimension", "").equals(v.level().dimension().identifier().toString());
    }
    static void clearRetry(Villager v) {
        var t = new CompoundTag(); t.putInt("version", 1); v.setAttached(DATA, t);
    }
    static void defer(Villager v, BlockPos target, int retries, int wait) {
        var t = new CompoundTag(); t.putInt("version", 1); t.putLong("target", target.asLong());
        t.putString("dimension", v.level().dimension().identifier().toString());
        t.putInt("retries", retries); t.putInt("wait", wait); v.setAttached(DATA, t);
    }
    public static void bootstrap() {}
    private VillagerState() {}
}
