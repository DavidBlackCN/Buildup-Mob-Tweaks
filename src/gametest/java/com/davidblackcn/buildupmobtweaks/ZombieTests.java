package com.davidblackcn.buildupmobtweaks;

import com.davidblackcn.buildupmobtweaks.combat.*;
import com.davidblackcn.buildupmobtweaks.config.BuildupConfig;
import com.davidblackcn.buildupmobtweaks.feature.*;
import java.util.*;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import static com.davidblackcn.buildupmobtweaks.CombatTestWorld.*;

public class ZombieTests {
    private static BuildupConfig config(FeatureId forced) {
        var cfg = new BuildupConfig(); var z = cfg.hostile.zombie;
        z.doorChance.accept(forced == FeatureId.ZOMBIE_DOOR_GUARD ? 1000 : 0);
        z.guardChance.accept(forced == FeatureId.ZOMBIE_ACTIVE_GUARD ? 1000 : 0);
        z.burrowChance.accept(forced == FeatureId.HUSK_SAND_BURROW ? 1000 : 0);
        z.riderChance.accept(forced == FeatureId.ZOMBIE_BABY_RIDER ? 1000 : 0); return cfg;
    }
    private static ZombieCombat service(BuildupConfig cfg) { return new ZombieCombat(new FeatureRegistry(cfg)); }
    @GameTest
    public void birthsFromNaturalSpawnerAndCommandPersistOnce(GameTestHelper h) {
        var cfg = config(FeatureId.ZOMBIE_ACTIVE_GUARD); var combat = service(cfg);
        for (var reason : List.of(EntitySpawnReason.NATURAL, EntitySpawnReason.SPAWNER, EntitySpawnReason.COMMAND)) {
            var mob = EntityTypes.ZOMBIE.create(h.getLevel(), reason); mob.setBaby(false); mob.setNoAi(true); combat.initialize(mob);
            h.assertTrue(combat.active(mob, FeatureId.ZOMBIE_ACTIVE_GUARD), "Explicit fresh spawn reason gets one trait");
            var saved = mob.getAttached(ZombieCombat.DATA).copy(); saved.putLong("next_special_at", 87654); mob.setAttached(ZombieCombat.DATA, saved);
            var loaded = reload(h, mob); cfg.hostile.zombie.guardChance.accept(0); combat.initialize(loaded);
            h.assertValueEqual(loaded.getAttached(ZombieCombat.DATA), saved, "Real save/load and chance change cannot reroll");
            cfg.hostile.zombie.guardChance.accept(1000); loaded.discard();
        }
        h.succeed();
    }
    @GameTest
    public void skippedSourcesUnknownVersionAndConversionDoNotReroll(GameTestHelper h) {
        var combat = service(config(FeatureId.HUSK_SAND_BURROW));
        for (var reason : List.of(EntitySpawnReason.LOAD, EntitySpawnReason.CONVERSION, EntitySpawnReason.DIMENSION_TRAVEL)) {
            var mob = EntityTypes.HUSK.create(h.getLevel(), reason); combat.initialize(mob);
            h.assertFalse(combat.active(mob, FeatureId.HUSK_SAND_BURROW), "Skip ambiguous source"); mob.discard();
        }
        var husk = mob(h, EntityTypes.HUSK, 3, 3); combat.initialize(husk); h.getLevel().addFreshEntity(husk);
        var saved = husk.getAttached(ZombieCombat.DATA).copy();
        var zombie = husk.convertTo(EntityTypes.ZOMBIE, ConversionParams.single(husk, true, true), ignored -> {});
        h.assertValueEqual(zombie.getAttached(ZombieCombat.DATA), saved, "Conversion copies original record once");
        h.assertFalse(combat.active(zombie, FeatureId.HUSK_SAND_BURROW), "Husk skill not activated on ordinary zombie"); zombie.discard();
        var future = new CompoundTag(); future.putInt("version", 99); future.putString("future", "keep");
        var mob = mob(h, EntityTypes.ZOMBIE, 3, 3); mob.setAttached(ZombieCombat.DATA, future);
        var loaded = reload(h, mob); combat.initialize(loaded);
        h.assertValueEqual(loaded.getAttached(ZombieCombat.DATA), future, "Unknown record preserved"); loaded.discard(); h.succeed();
    }
    @GameTest
    public void eightIndependentSwitchesAndTraitSeparation(GameTestHelper h) {
        var cfg = new BuildupConfig(); var registry = new FeatureRegistry(cfg); var z = cfg.hostile.zombie; var d = cfg.hostile.drowned;
        var flags = List.of(z.shieldUse, z.doorGuard, z.activeGuard, z.sandBurrow, z.babyRider, d.tridentConservation, d.tridentRecovery, d.tridentPlayerPickup);
        var ids = List.of(FeatureId.ZOMBIE_SHIELD_USE, FeatureId.ZOMBIE_DOOR_GUARD, FeatureId.ZOMBIE_ACTIVE_GUARD, FeatureId.HUSK_SAND_BURROW,
                FeatureId.ZOMBIE_BABY_RIDER, FeatureId.DROWNED_TRIDENT_CONSERVATION, FeatureId.DROWNED_TRIDENT_RECOVERY, FeatureId.DROWNED_TRIDENT_PLAYER_PICKUP);
        for (int i = 0; i < ids.size(); i++) {
            flags.get(i).accept(false);
            for (int j = 0; j < ids.size(); j++) h.assertValueEqual(registry.isEnabled(ids.get(j)), i != j, "Only selected switch disabled");
            BuildupMobTweaks.LOGGER.info("S2-B independent gate {}=false; other seven=true", ids.get(i)); flags.get(i).accept(true);
        }
        cfg.traits.enabled.accept(false);
        for (int i = 0; i < ids.size(); i++) h.assertValueEqual(registry.isEnabled(ids.get(i)), i == 0 || i >= 5, "Basic equipment does not require a trait");
        cfg.general.enabled.accept(false); for (var id : ids) h.assertFalse(registry.isEnabled(id), "Master gate"); h.succeed();
    }
    @GameTest(structure = ARENA)
    public void doorBlocksFrontOnlyWithCooldownAndThreeUseLimit(GameTestHelper h) {
        floor(h, Blocks.STONE); var cfg = config(FeatureId.ZOMBIE_DOOR_GUARD); var combat = service(cfg);
        var mob = mob(h, EntityTypes.ZOMBIE, 3, 3); mob.setNoAi(false); combat.initialize(mob); mob.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.OAK_DOOR));
        var attacker = mob(h, EntityTypes.COW, 3, 5); var source = h.getLevel().damageSources().mobAttack(attacker);
        cfg.hostile.zombie.doorGuard.accept(false); h.assertFalse(combat.blockWithDoor(mob, source, 5), "Door feature independently disabled");
        cfg.hostile.zombie.doorGuard.accept(true);
        attacker.snapTo(h.absolutePos(new BlockPos(3, 2, 1)), 0, 0);
        h.assertFalse(combat.blockWithDoor(mob, source, 5), "Rear attack not blocked"); attacker.snapTo(h.absolutePos(new BlockPos(3, 2, 5)), 0, 0);
        for (int i = 0; i < 3; i++) {
            h.assertTrue(combat.blockWithDoor(mob, source, 5), "Front damage blocked");
            h.assertFalse(combat.blockWithDoor(mob, source, 5), "Cooldown prevents repeated free blocks");
            var data = mob.getAttached(ZombieCombat.DATA).copy(); data.putLong("next_special_at", 0); mob.setAttached(ZombieCombat.DATA, data);
        }
        h.assertTrue(mob.getOffhandItem().isEmpty(), "Third block consumes exactly the held door"); mob.discard(); attacker.discard(); h.succeed();
    }
    @GameTest(structure = ARENA)
    public void actualDoorDamageEventAndBypassRemainCorrect(GameTestHelper h) {
        floor(h, Blocks.STONE); var mob = mob(h, EntityTypes.ZOMBIE, 3, 3); mob.setNoAi(false);
        service(config(FeatureId.ZOMBIE_DOOR_GUARD)).initialize(mob); mob.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.OAK_DOOR));
        h.getLevel().addFreshEntity(mob); var attacker = mob(h, EntityTypes.COW, 3, 5); float health = mob.getHealth();
        mob.hurtServer(h.getLevel(), h.getLevel().damageSources().mobAttack(attacker), 5);
        h.assertValueEqual(mob.getHealth(), health, "Registered Fabric event actually blocks front attack");
        mob.hurtServer(h.getLevel(), h.getLevel().damageSources().magic(), 5);
        h.assertTrue(mob.getHealth() < health, "Magic/environmental bypass is not blocked"); mob.discard(); attacker.discard(); h.succeed();
    }
    @GameTest(structure = ARENA, maxTicks = 35)
    public void vanillaShieldReallyBlocksThenReturnsToMelee(GameTestHelper h) {
        floor(h, Blocks.STONE); var mob = mob(h, EntityTypes.ZOMBIE, 3, 3); service(config(null)).initialize(mob);
        mob.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD)); var target = mob(h, EntityTypes.COW, 3, 5);
        h.getLevel().addFreshEntity(target); h.getLevel().addFreshEntity(mob); mob.setTarget(target); mob.setNoAi(false);
        h.runAtTickTime(10, () -> {
            h.assertTrue(mob.isBlocking(), "Basic shield Goal activates vanilla BlocksAttacks without trait");
            float health = mob.getHealth(); mob.hurtServer(h.getLevel(), h.getLevel().damageSources().mobAttack(target), 3);
            h.assertValueEqual(mob.getHealth(), health, "Actual vanilla shield prevents front damage");
        });
        h.runAtTickTime(22, () -> { h.assertFalse(mob.isUsingItem(), "Finite blocking period permits vanilla melee again"); mob.discard(); target.discard(); h.succeed(); });
    }
    @GameTest(structure = ARENA)
    public void activeAndBasicGuardGoalsHaveIndependentBehaviorGates(GameTestHelper h) {
        floor(h, Blocks.STONE); var cfg = config(FeatureId.ZOMBIE_ACTIVE_GUARD); var combat = service(cfg);
        var mob = mob(h, EntityTypes.ZOMBIE, 3, 3); combat.initialize(mob); mob.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
        var target = mob(h, EntityTypes.COW, 3, 9); mob.setTarget(target); var goal = combat.guardGoal(mob);
        h.assertTrue(goal.canUse(), "Advanced guard starts before melee range"); goal.start();
        h.assertTrue(mob.isUsingItem() && mob.getAttached(ZombieCombat.DATA).getLongOr("next_special_at", 0) > 0, "Advanced cooldown reserved");
        cfg.hostile.zombie.activeGuard.accept(false); h.assertFalse(goal.canContinueToUse(), "Disabling interrupts advanced guard"); goal.stop();
        h.assertFalse(mob.isUsingItem(), "Releases only its own held use");
        target.snapTo(h.absolutePos(new BlockPos(3, 2, 5)), 0, 0); h.assertTrue(goal.canUse(), "Basic guard still available independently");
        cfg.hostile.zombie.shieldUse.accept(false); h.assertFalse(goal.canUse(), "Basic guard independently disabled");
        mob.discard(); target.discard(); h.succeed();
    }
    @GameTest(structure = ARENA, maxTicks = 40)
    public void huskSandMoveHasWindupAndDoesNotBreakTerrain(GameTestHelper h) {
        floor(h, Blocks.SAND); var husk = mob(h, EntityTypes.HUSK, 3, 3); service(config(FeatureId.HUSK_SAND_BURROW)).initialize(husk);
        var target = mob(h, EntityTypes.COW, 11, 3); h.getLevel().addFreshEntity(target); h.getLevel().addFreshEntity(husk);
        husk.setTarget(target); husk.setNoAi(false); double start = husk.getX();
        h.runAtTickTime(8, () -> h.assertTrue(husk.getX() < start + 1, "Telegraph before displacement"));
        h.runAtTickTime(24, () -> {
            h.assertTrue(husk.getX() > start + 2.5, "Actual short sand reposition");
            h.assertTrue(husk.getAttached(ZombieCombat.DATA).getLongOr("next_special_at", 0) > h.getLevel().getGameTime(), "Cooldown persisted");
            h.assertTrue(h.getBlockState(new BlockPos(5, 1, 3)).is(Blocks.SAND), "No terrain destruction");
            husk.discard(); target.discard(); h.succeed();
        });
    }
    @GameTest(structure = ARENA)
    public void sandMoveRejectsWallMissingSandAndDisabledFeature(GameTestHelper h) {
        floor(h, Blocks.SAND); var cfg = config(FeatureId.HUSK_SAND_BURROW); var combat = service(cfg);
        var husk = mob(h, EntityTypes.HUSK, 3, 3); combat.initialize(husk); husk.setOnGround(true); var target = mob(h, EntityTypes.COW, 11, 3); husk.setTarget(target);
        h.assertTrue(combat.burrowDestination(husk) != null, "Valid flat sand corridor");
        h.setBlock(new BlockPos(5, 2, 3), Blocks.STONE); h.assertTrue(combat.burrowDestination(husk) == null, "Never cross wall");
        h.setBlock(new BlockPos(5, 2, 3), Blocks.AIR); h.setBlock(new BlockPos(5, 1, 3), Blocks.AIR);
        h.assertTrue(combat.burrowDestination(husk) == null, "Never cross missing support");
        h.setBlock(new BlockPos(5, 1, 3), Blocks.SAND); cfg.hostile.zombie.sandBurrow.accept(false);
        h.assertTrue(combat.burrowDestination(husk) == null, "Independent burrow disable"); husk.discard(); target.discard(); h.succeed();
    }
    @GameTest(structure = ARENA)
    public void rareBabyRidesExistingAdultOnceAndDisableDismountsOnlyOwnedRide(GameTestHelper h) {
        floor(h, Blocks.STONE); var cfg = config(FeatureId.ZOMBIE_BABY_RIDER); var combat = service(cfg);
        var adult = mob(h, EntityTypes.ZOMBIE, 3, 5); h.getLevel().addFreshEntity(adult);
        var baby = mob(h, EntityTypes.ZOMBIE, 3, 3); baby.setBaby(true); combat.initialize(baby); h.getLevel().addFreshEntity(baby);
        h.assertTrue(combat.attemptRide(baby) && baby.getVehicle() == adult, "Mount existing nearby adult");
        h.assertFalse(combat.attemptRide(baby), "Cannot spawn or stack more rides");
        h.assertValueEqual(h.getEntities(EntityTypes.ZOMBIE, new BlockPos(3, 2, 3), 6).size(), 2, "No new mount entity");
        cfg.hostile.zombie.babyRider.accept(false); combat.tick(baby);
        h.assertFalse(baby.isPassenger(), "Disabling dismounts only recorded ride");
        h.assertTrue(adult.isAlive(), "Mount not deleted"); baby.discard(); adult.discard(); h.succeed();
    }
    @GameTest(structure = ARENA)
    public void finalizedNaturalBirthInitializesOnlyOnce(GameTestHelper h) {
        floor(h, Blocks.STONE);
        var mob = EntityTypes.ZOMBIE.create(h.getLevel(), EntitySpawnReason.NATURAL);
        mob.snapTo(h.absolutePos(new BlockPos(3, 2, 3)), 0, 0);
        mob.finalizeSpawn(h.getLevel(), h.getLevel().getCurrentDifficultyAt(mob.blockPosition()), EntitySpawnReason.NATURAL, null);
        mob.setNoAi(true); h.getLevel().addFreshEntityWithPassengers(mob);
        var data = mob.getAttached(ZombieCombat.DATA).copy();
        h.assertValueEqual(data.getStringOr("origin", ""), "NATURAL", "Actual finalizeSpawn and entity-load pipeline retain natural origin");
        h.assertValueEqual(data.getStringOr("status", ""), "rolled", "One initial draw including valid none");
        var loaded = reload(h, mob);
        h.assertValueEqual(loaded.getAttached(ZombieCombat.DATA), data, "Reload does not replace natural result");
        loaded.discard(); h.succeed();
    }
    @GameTest(structure = ARENA, maxTicks = 100)
    public void actualSpawnerBlockCreatesPersistentTraitRecord(GameTestHelper h) {
        floor(h, Blocks.STONE); var pos = new BlockPos(8, 2, 8); h.setBlock(pos, Blocks.SPAWNER);
        var block = (net.minecraft.world.level.block.entity.SpawnerBlockEntity) h.getLevel().getBlockEntity(h.absolutePos(pos));
        var player = h.makeMockServerPlayerInLevel(); player.snapTo(h.absolutePos(new BlockPos(3, 2, 3)), 0, 0);
        var entity = new CompoundTag(); entity.putString("id", "minecraft:zombie");
        var range = new net.minecraft.util.InclusiveRange<Integer>(0, 15);
        var spawn = new net.minecraft.world.level.SpawnData(entity,
                Optional.of(new net.minecraft.world.level.SpawnData.CustomSpawnRules(range, range)), Optional.empty());
        var output = net.minecraft.world.level.storage.TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING, h.getLevel().registryAccess());
        output.putShort("Delay", (short) 0); output.putInt("SpawnCount", 1); output.putInt("SpawnRange", 3);
        output.store("SpawnData", net.minecraft.world.level.SpawnData.CODEC, spawn);
        block.getSpawner().load(h.getLevel(), h.absolutePos(pos), net.minecraft.world.level.storage.TagValueInput.create(
                net.minecraft.util.ProblemReporter.DISCARDING, h.getLevel().registryAccess(), output.buildResult()));
        h.succeedWhen(() -> {
            var mobs = h.getEntities(EntityTypes.ZOMBIE, pos, 7);
            h.assertTrue(!mobs.isEmpty(), "Real block ticks spawned a zombie near a player");
            for (var mob : mobs) {
                var data = mob.getAttached(ZombieCombat.DATA);
                h.assertTrue(data != null && data.getStringOr("origin", "").equals("SPAWNER") && data.getStringOr("status", "").equals("rolled"), "Spawner receives exactly one birth record");
                var saved = data.copy(); ZombieCombat.instance().initialize(mob);
                h.assertValueEqual(mob.getAttached(ZombieCombat.DATA), saved, "No duplicate initialization"); mob.discard();
            }
            h.setBlock(pos, Blocks.AIR); player.discard();
        });
    }
}
