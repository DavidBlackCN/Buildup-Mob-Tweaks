package com.davidblackcn.buildupmobtweaks;

import com.davidblackcn.buildupmobtweaks.combat.VexCombat;
import com.davidblackcn.buildupmobtweaks.config.BuildupConfig;
import com.davidblackcn.buildupmobtweaks.feature.*;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import static com.davidblackcn.buildupmobtweaks.CombatTestWorld.*;

public class VexTests {
    private static <T extends Mob> T placed(GameTestHelper h, EntityType<T> type, int x, int z) {
        var mob = mob(h, type, x, z); h.getLevel().addFreshEntity(mob); return mob;
    }
    private static Goal charge(Vex vex) {
        return vex.getGoalSelector().getAvailableGoals().stream().map(WrappedGoal::getGoal)
                .filter(g -> g.getClass().getName().endsWith("$VexChargeAttackGoal")).findFirst().orElseThrow();
    }
    private static Vec3 wanted(Vex vex) {
        var c = vex.getMoveControl(); return new Vec3(c.getWantedX(), c.getWantedY(), c.getWantedZ());
    }
    @GameTest
    public void independentGatesMasterAndValidatedBounds(GameTestHelper h) {
        var cfg = new BuildupConfig(); var f = new FeatureRegistry(cfg); var v = cfg.hostile.vex;
        var ids = List.of(FeatureId.VEX_FIXED_CHARGE, FeatureId.VEX_RECOVERY_PAUSE, FeatureId.VEX_CLOSE_RANGE_GUARD);
        var flags = List.of(v.fixedCharge, v.recoveryPause, v.closeRangeGuard);
        cfg.traits.enabled.accept(false);
        for (int i = 0; i < ids.size(); i++) {
            flags.get(i).accept(false);
            for (int j = 0; j < ids.size(); j++) h.assertValueEqual(f.isEnabled(ids.get(j)), i != j, "Independent gate, no trait dependency");
            flags.get(i).accept(true);
        }
        h.assertValueEqual(f.vexRecoveryTicks(), 20, "One second recovery");
        h.assertValueEqual(f.vexMinimumDistance(), 3, "Three block start guard");
        v.recoveryTicks.accept(999); v.minimumChargeDistance.accept(-10);
        h.assertTrue(f.vexRecoveryTicks() <= 60 && f.vexMinimumDistance() >= 2, "Validated numerical limits");
        cfg.general.enabled.accept(false); for (var id : ids) h.assertFalse(f.isEnabled(id), "Master gate"); h.succeed();
    }
    @GameTest(structure = ARENA)
    public void actualGoalKeepsInitialEndpointWhenTargetDodges(GameTestHelper h) {
        floor(h, Blocks.STONE); var vex = placed(h, EntityTypes.VEX, 5, 5); var target = placed(h, EntityTypes.COW, 7, 5);
        vex.setTarget(target); var goal = charge(vex); goal.start(); var initial = wanted(vex);
        target.snapTo(target.position().add(0, 0, 1), 0, 0); goal.tick();
        h.assertValueEqual(wanted(vex), initial, "Near-target correction suppressed in actual vanilla goal");
        h.assertTrue(vex.isCharging() && vex.getTarget() == target, "Vanilla charge state and target retained");
        goal.stop(); vex.discard(); target.discard(); h.succeed();
    }
    @GameTest(structure = ARENA)
    public void unknownSchemaPreservedAndVanillaTrackingRestored(GameTestHelper h) {
        floor(h, Blocks.STONE); var vex = placed(h, EntityTypes.VEX, 5, 5); var target = placed(h, EntityTypes.COW, 7, 5);
        var unknown = vex.getAttached(VexCombat.DATA).copy(); unknown.putInt("version", 99); unknown.putString("future", "retain"); vex.setAttached(VexCombat.DATA, unknown);
        vex.setTarget(target); var goal = charge(vex); goal.start(); target.snapTo(target.position().add(0, 0, 1), 0, 0); goal.tick();
        h.assertValueEqual(wanted(vex), target.getEyePosition(), "Unknown schema delegates near-target tracking to vanilla");
        goal.stop(); var loaded = reload(h, vex);
        h.assertValueEqual(loaded.getAttached(VexCombat.DATA), unknown, "Unknown bytes preserved through real load");
        loaded.discard(); target.discard(); h.succeed();
    }
    @GameTest(structure = ARENA)
    public void actualCloseGuardExtendsVanillaTwoBlockBoundary(GameTestHelper h) {
        floor(h, Blocks.STONE); var vex = placed(h, EntityTypes.VEX, 5, 5); var target = placed(h, EntityTypes.COW, 7, 5);
        target.snapTo(target.position().add(.5, 0, 0), 0, 0); vex.setTarget(target); var goal = charge(vex);
        for (int i = 0; i < 100; i++) h.assertFalse(goal.canUse(), "No start at 2.5 blocks with default three-block guard");
        var data = vex.getAttached(VexCombat.DATA).copy(); data.putInt("version", 99); vex.setAttached(VexCombat.DATA, data);
        boolean vanillaStarts = false; vex.getRandom().setSeed(15);
        for (int i = 0; i < 100; i++) vanillaStarts |= goal.canUse();
        h.assertTrue(vanillaStarts, "Vanilla is eligible outside two blocks when policy falls back");
        target.snapTo(vex.position().add(1.5, 0, 0), 0, 0);
        for (int i = 0; i < 100; i++) h.assertFalse(goal.canUse(), "Original two-block guard still present");
        vex.discard(); target.discard(); h.succeed();
    }
    @GameTest(structure = ARENA)
    public void actualCollisionStillDamagesAndEndsCharge(GameTestHelper h) {
        floor(h, Blocks.STONE); var vex = placed(h, EntityTypes.VEX, 5, 5); var target = placed(h, EntityTypes.COW, 5, 5);
        vex.setTarget(target); float before = target.getHealth(); var goal = charge(vex); goal.start(); goal.tick();
        h.assertTrue(target.getHealth() < before && !vex.isCharging(), "Vanilla collision damage and charge ending");
        goal.stop(); h.assertTrue(VexCombat.instance().recovering(vex), "Collision enters recovery");
        vex.discard(); target.discard(); h.succeed();
    }
    @GameTest(structure = ARENA, maxTicks = 35)
    public void actualMovementControllerPausesThenResumes(GameTestHelper h) {
        floor(h, Blocks.STONE); var vex = placed(h, EntityTypes.VEX, 5, 5); var target = placed(h, EntityTypes.COW, 12, 5);
        vex.setTarget(target); var goal = charge(vex); goal.start(); vex.setDeltaMovement(1, 0, 0); goal.stop();
        h.assertValueEqual(vex.getDeltaMovement(), Vec3.ZERO, "Charge momentum stops once");
        vex.getMoveControl().setWantedPosition(vex.getX() + 4, vex.getY(), vex.getZ(), 1); vex.getMoveControl().tick();
        h.assertFalse(vex.getMoveControl().hasWanted(), "Recovery also blocks random AI flight");
        h.assertFalse(goal.canUse(), "No charge while recovering");
        vex.setDeltaMovement(.2, .1, 0); vex.getMoveControl().tick();
        h.assertValueEqual(vex.getDeltaMovement(), new Vec3(.2, .1, 0), "Recovery does not erase external knockback each tick"); vex.setDeltaMovement(Vec3.ZERO);
        h.runAtTickTime(25, () -> {
            h.assertFalse(VexCombat.instance().recovering(vex), "Deadline expires");
            vex.getMoveControl().setWantedPosition(vex.getX() + 4, vex.getY(), vex.getZ(), 1); vex.getMoveControl().tick();
            h.assertTrue(vex.getDeltaMovement().x > 0, "Original controller accelerates after pause");
            vex.discard(); target.discard(); h.succeed();
        });
    }
    @GameTest(structure = ARENA, maxTicks = 60)
    public void missedChargeHasBoundedDurationAndIdempotentStop(GameTestHelper h) {
        floor(h, Blocks.STONE); var vex = placed(h, EntityTypes.VEX, 5, 5); var target = placed(h, EntityTypes.COW, 12, 5);
        vex.setTarget(target); var goal = charge(vex); goal.start();
        h.runAtTickTime(43, () -> {
            h.assertFalse(goal.canContinueToUse(), "Fixed endpoint cannot keep charging indefinitely");
            goal.tick(); h.assertFalse(vex.isCharging(), "Tick guard stops timed-out attack");
            long deadline = vex.getAttached(VexCombat.DATA).getLongOr("recover_until", 0); goal.stop(); goal.stop();
            h.assertValueEqual(vex.getAttached(VexCombat.DATA).getLongOr("recover_until", 0), deadline, "Repeated stop does not extend recovery");
            vex.discard(); target.discard(); h.succeed();
        });
    }
    @GameTest(structure = ARENA)
    public void changingOrLosingTargetCancelsBeforeDamage(GameTestHelper h) {
        floor(h, Blocks.STONE); var vex = placed(h, EntityTypes.VEX, 5, 5); var old = placed(h, EntityTypes.COW, 12, 5);
        var replacement = placed(h, EntityTypes.COW, 5, 5); vex.setTarget(old); var goal = charge(vex); goal.start();
        vex.setTarget(replacement); float before = replacement.getHealth(); goal.tick();
        h.assertValueEqual(replacement.getHealth(), before, "No redirected hit against replacement target");
        h.assertFalse(vex.isCharging(), "Changed target aborts");
        h.assertTrue(vex.getTarget() == replacement, "Do not erase another AI target");
        vex.discard(); old.discard(); replacement.discard(); h.succeed();
    }
    @GameTest(structure = ARENA)
    public void realReloadCancelsInFlightChargeButKeepsReservation(GameTestHelper h) {
        floor(h, Blocks.STONE); var vex = placed(h, EntityTypes.VEX, 5, 5); var target = placed(h, EntityTypes.COW, 12, 5);
        vex.setTarget(target); charge(vex).start(); vex.setDeltaMovement(.7, 0, 0);
        var saved = vex.getAttached(VexCombat.DATA).copy(); var uuid = vex.getUUID(); var loaded = reload(h, vex);
        h.assertValueEqual(loaded.getUUID(), uuid, "Same saved entity");
        h.assertValueEqual(loaded.getAttached(VexCombat.DATA), saved, "Reserved deadline persists");
        h.assertTrue(VexCombat.instance().recovering(loaded) && !loaded.isCharging(), "Transient attack cancelled, recovery preserved");
        h.assertValueEqual(loaded.getDeltaMovement(), Vec3.ZERO, "Saved charge momentum cleared");
        h.assertFalse(charge(loaded).canUse(), "Cannot unload to bypass recovery");
        loaded.discard(); target.discard(); h.succeed();
    }
    @GameTest(structure = ARENA)
    public void disabledPoliciesKeepSavedDataAndIndependentBehavior(GameTestHelper h) {
        floor(h, Blocks.STONE); var vex = placed(h, EntityTypes.VEX, 5, 5); var target = placed(h, EntityTypes.COW, 7, 5); vex.setTarget(target);
        var cfg = new BuildupConfig(); var service = new VexCombat(new FeatureRegistry(cfg)); var v = cfg.hostile.vex;
        h.assertFalse(service.allowStart(vex), "Default close guard"); v.closeRangeGuard.accept(false); h.assertTrue(service.allowStart(vex), "Close gate independently disabled");
        v.fixedCharge.accept(false); h.assertFalse(service.fixed(vex), "Fixed endpoint independently disabled");
        service.start(vex); service.stop(vex); h.assertTrue(service.recovering(vex), "Pause works without fixed endpoint");
        var data = vex.getAttached(VexCombat.DATA).copy(); v.recoveryPause.accept(false);
        h.assertFalse(service.recovering(vex), "Disable immediately releases recovery");
        h.assertTrue(service.allowStart(vex), "All disabled delegates eligibility");
        v.fixedCharge.accept(true); service.start(vex); service.stop(vex);
        h.assertFalse(service.recovering(vex), "Fixed endpoint does not force pause");
        h.assertValueEqual(vex.getAttached(VexCombat.DATA), data, "Disabled pause does not rewrite saved deadline");
        cfg.general.enabled.accept(false); h.assertFalse(service.fixed(vex), "Master off restores homing");
        vex.discard(); target.discard(); h.succeed();
    }
    @GameTest(structure = ARENA, maxTicks = 65)
    public void realAiChargeMovesAndFinishesWithoutLosingTarget(GameTestHelper h) {
        floor(h, Blocks.STONE); var vex = placed(h, EntityTypes.VEX, 5, 5); var target = placed(h, EntityTypes.COW, 12, 5);
        // Run the actual vanilla goal and movement controller with a stationary target, including its random start delay.
        vex.setTarget(target); vex.setNoAi(false);
        vex.getGoalSelector().removeAllGoals(g -> !g.getClass().getName().endsWith("$VexChargeAttackGoal"));
        var initial = vex.position();
        h.succeedWhen(() -> {
            h.assertTrue(vex.position().distanceToSqr(initial) > 1, "Actual AI flew toward target");
            h.assertTrue(VexCombat.instance().recovering(vex), "Actual goal selector eventually enters recovery");
            h.assertTrue(vex.getTarget() == target && !vex.isCharging(), "Recovery retains target and ends charge flag");
            vex.discard(); target.discard();
        });
    }
}