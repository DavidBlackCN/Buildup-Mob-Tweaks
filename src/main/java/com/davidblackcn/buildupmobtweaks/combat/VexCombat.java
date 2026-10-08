package com.davidblackcn.buildupmobtweaks.combat;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import com.davidblackcn.buildupmobtweaks.feature.*;
import java.util.UUID;
import net.fabricmc.fabric.api.attachment.v1.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.phys.Vec3;

/** Server-only charge policy. Vanilla still owns charge selection, sound, collision damage and flight. */
public final class VexCombat {
    public static final int MAX_CHARGE_TICKS = 40;
    public static final AttachmentType<CompoundTag> DATA = AttachmentRegistry.createPersistent(BuildupMobTweaks.id("vex_combat"), CompoundTag.CODEC);
    private static final AttachmentType<Runtime> RUNTIME = AttachmentRegistry.createDefaulted(BuildupMobTweaks.id("vex_runtime"), Runtime::new);
    private static VexCombat instance;
    private final FeatureRegistry features;
    private static final class Runtime {
        boolean active;
        long endsAt;
        UUID target;
    }
    public VexCombat(FeatureRegistry features) { this.features = features; }
    public static VexCombat instance() { return instance; }
    public void register() {
        instance = this;
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (entity.getType() == EntityTypes.VEX) {
                var vex = (Vex) entity; initialize(vex);
                // In-flight goals are transient. A saved reservation becomes a bounded recovery on load.
                if (recovering(vex)) { vex.setIsCharging(false); halt(vex); }
            }
        });
    }
    public void initialize(Vex vex) {
        if (!vex.hasAttached(DATA)) {
            var data = new CompoundTag(); data.putInt("version", 1); data.putLong("recover_until", 0);
            vex.setAttached(DATA, data);
        }
    }
    public boolean enabled(Vex vex, FeatureId id) {
        var data = vex.getAttached(DATA);
        return vex.getType() == EntityTypes.VEX && !vex.level().isClientSide() && data != null
                && data.getIntOr("version", -1) == 1 && features.isEnabled(id);
    }
    public boolean fixed(Vex vex) { return enabled(vex, FeatureId.VEX_FIXED_CHARGE); }
    private boolean managed(Vex vex) { return fixed(vex) || enabled(vex, FeatureId.VEX_RECOVERY_PAUSE); }
    public boolean recovering(Vex vex) {
        if (!enabled(vex, FeatureId.VEX_RECOVERY_PAUSE)) return false;
        var rt = vex.getAttached(RUNTIME);
        return (rt == null || !rt.active) && vex.level().getGameTime() < vex.getAttached(DATA).getLongOr("recover_until", 0);
    }
    public boolean allowStart(Vex vex) {
        if (recovering(vex)) return false;
        var target = vex.getTarget();
        int distance = features.vexMinimumDistance();
        return !enabled(vex, FeatureId.VEX_CLOSE_RANGE_GUARD) || target == null || vex.distanceToSqr(target) > distance * distance;
    }
    public void start(Vex vex) {
        if (!managed(vex) || vex.getTarget() == null) return;
        var rt = vex.getAttachedOrCreate(RUNTIME);
        rt.active = true; rt.target = vex.getTarget().getUUID();
        rt.endsAt = vex.level().getGameTime() + MAX_CHARGE_TICKS;
        if (enabled(vex, FeatureId.VEX_RECOVERY_PAUSE)) reserve(vex, rt.endsAt + features.vexRecoveryTicks());
    }
    public boolean allowContinue(Vex vex) {
        var rt = vex.getAttached(RUNTIME);
        if (rt == null || !rt.active || !managed(vex)) return true;
        return vex.level().getGameTime() < rt.endsAt && SkeletonCombat.validTarget(vex, vex.getTarget())
                && rt.target.equals(vex.getTarget().getUUID());
    }
    public void stop(Vex vex) {
        var rt = vex.getAttached(RUNTIME);
        if (rt == null || !rt.active) return;
        rt.active = false; rt.target = null;
        if (managed(vex)) halt(vex);
        if (enabled(vex, FeatureId.VEX_RECOVERY_PAUSE)) reserve(vex, vex.level().getGameTime() + features.vexRecoveryTicks());
    }
    private static void halt(Vex vex) {
        vex.getMoveControl().setWait(); vex.setDeltaMovement(Vec3.ZERO);
    }
    private static void reserve(Vex vex, long until) {
        var data = vex.getAttached(DATA).copy(); data.putLong("recover_until", until); vex.setAttached(DATA, data);
    }
}