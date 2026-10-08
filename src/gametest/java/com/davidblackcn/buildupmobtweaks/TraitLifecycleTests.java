package com.davidblackcn.buildupmobtweaks;

import com.davidblackcn.buildupmobtweaks.config.BuildupConfig;
import com.davidblackcn.buildupmobtweaks.feature.FeatureRegistry;
import com.davidblackcn.buildupmobtweaks.traits.TraitKind;
import com.davidblackcn.buildupmobtweaks.traits.TraitService;
import com.davidblackcn.buildupmobtweaks.traits.TraitState;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;

public class TraitLifecycleTests {
    private static final BlockPos POSITION = new BlockPos(2, 2, 2);

    private static TraitService service(BuildupConfig config) {
        return new TraitService(new FeatureRegistry(config));
    }

    private static CompoundTag marker() {
        var state = new TraitState(1, "rolled", "COMMAND", List.of(new TraitState.Entry(TraitKind.RARE.id(), 3)));
        return (CompoundTag) TraitState.CODEC.encodeStart(NbtOps.INSTANCE, state).getOrThrow();
    }

    @GameTest
    public void spawnReasonsInitializeOnce(GameTestHelper helper) {
        var service = service(new BuildupConfig());
        for (var reason : List.of(EntitySpawnReason.NATURAL, EntitySpawnReason.BREEDING,
                EntitySpawnReason.COMMAND, EntitySpawnReason.SPAWNER, EntitySpawnReason.TRIAL_SPAWNER)) {
            var cow = EntityTypes.COW.spawn(helper.getLevel(), helper.absolutePos(POSITION), reason);
            var initial = TraitService.read(cow).orElseThrow();
            helper.assertValueEqual(initial.outcome(), "rolled", "Fresh entity must roll");
            helper.assertValueEqual(initial.origin(), reason.name(), "Spawn reason recorded");
            for (int i = 0; i < 10; i++) service.initialize(cow);
            helper.assertValueEqual(TraitService.read(cow).orElseThrow(), initial, "Repeated load must not roll again");
            cow.discard();
        }
        helper.succeed();
    }

    @GameTest
    public void entitySaveRoundTrip(GameTestHelper helper) {
        var cow = helper.spawn(EntityTypes.COW, POSITION, EntitySpawnReason.COMMAND);
        cow.setAttached(TraitService.DATA, marker());
        var expected = cow.getAttached(TraitService.DATA).copy();
        Entity loaded = reload(helper, cow);
        helper.assertValueEqual(loaded.getAttached(TraitService.DATA), expected, "Entity save/load must preserve attachment");
        helper.assertValueEqual(TraitService.read(loaded).orElseThrow().entries().size(), 1, "Non-empty marker survives");
        loaded.discard();
        helper.succeed();
    }

    private Entity reload(GameTestHelper helper, Entity entity) {
        var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, helper.getLevel().registryAccess());
        entity.saveWithoutId(output);
        var type = entity.getType();
        entity.discard();
        var input = TagValueInput.create(ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), output.buildResult());
        Entity loaded = EntityType.create(type, input, helper.getLevel(), EntitySpawnReason.LOAD).orElseThrow();
        helper.getLevel().addFreshEntity(loaded);
        return loaded;
    }

    @GameTest
    public void conversionChainInherits(GameTestHelper helper) {
        var husk = helper.spawn(EntityTypes.HUSK, POSITION, EntitySpawnReason.COMMAND);
        husk.setAttached(TraitService.DATA, marker());
        var expected = husk.getAttached(TraitService.DATA).copy();
        var zombie = husk.convertTo(EntityTypes.ZOMBIE, ConversionParams.single(husk, true, true), ignored -> {});
        helper.assertTrue(zombie != null, "Husk conversion succeeded");
        helper.assertValueEqual(zombie.getAttached(TraitService.DATA), expected, "Husk -> zombie must inherit");
        var drowned = zombie.convertTo(EntityTypes.DROWNED, ConversionParams.single(zombie, true, true), ignored -> {});
        helper.assertTrue(drowned != null, "Zombie conversion succeeded");
        helper.assertValueEqual(drowned.getAttached(TraitService.DATA), expected, "Zombie -> drowned must inherit");
        drowned.discard();
        helper.succeed();
    }

    @GameTest
    public void missingConversionSourceDoesNotRoll(GameTestHelper helper) {
        var zombie = helper.spawn(EntityTypes.ZOMBIE, POSITION, EntitySpawnReason.COMMAND);
        zombie.removeAttached(TraitService.DATA);
        var drowned = zombie.convertTo(EntityTypes.DROWNED, ConversionParams.single(zombie, true, true), ignored -> {});
        helper.assertValueEqual(TraitService.read(drowned).orElseThrow().outcome(), "conversion_without_source", "Missing source must skip");
        helper.assertTrue(TraitService.read(drowned).orElseThrow().entries().isEmpty(), "No new traits on conversion");
        drowned.discard();
        helper.succeed();
    }

    @GameTest
    public void breedingCreatesIndependentState(GameTestHelper helper) {
        var first = helper.spawn(EntityTypes.COW, POSITION, EntitySpawnReason.COMMAND);
        var second = helper.spawn(EntityTypes.COW, POSITION, EntitySpawnReason.COMMAND);
        first.setAttached(TraitService.DATA, marker());
        second.setAttached(TraitService.DATA, marker());
        first.spawnChildFromBreeding(helper.getLevel(), second);
        var child = helper.getEntities(EntityTypes.COW, POSITION, 5).stream()
                .filter(entity -> entity.isBaby()).findFirst().orElseThrow();
        var state = TraitService.read(child).orElseThrow();
        helper.assertValueEqual(state.origin(), "BREEDING", "Offspring has a fresh breeding roll");
        helper.assertValueEqual(state.outcome(), "rolled", "Offspring initializes");
        helper.assertTrue(child.getAttached(TraitService.DATA) != first.getAttached(TraitService.DATA), "No shared parent state");
        first.discard(); second.discard(); child.discard();
        helper.succeed();
    }

    @GameTest
    public void configChangesDoNotReroll(GameTestHelper helper) {
        var config = new BuildupConfig();
        var service = service(config);
        config.traits.enabled.accept(false);
        var cow = EntityTypes.COW.create(helper.getLevel(), EntitySpawnReason.COMMAND);
        service.initialize(cow);
        helper.getLevel().addFreshEntity(cow);
        var initial = cow.getAttached(TraitService.DATA).copy();
        config.traits.enabled.accept(true);
        config.traits.cow.rare.accept(1000);
        service.initialize(cow);
        helper.assertValueEqual(cow.getAttached(TraitService.DATA), initial, "Re-enabling must not backfill disabled births");
        cow.setAttached(TraitService.DATA, marker());
        config.traits.rareMarker.accept(false);
        helper.assertTrue(service.activeTraits(cow).isEmpty(), "Independent switch masks saved marker");
        config.traits.rareMarker.accept(true);
        helper.assertValueEqual(service.activeTraits(cow).size(), 1, "Re-enable restores saved marker");
        config.general.enabled.accept(false);
        helper.assertTrue(service.activeTraits(cow).isEmpty(), "Master switch wins");
        cow.discard();
        helper.succeed();
    }

    @GameTest
    public void legacyAndBossesAreExcluded(GameTestHelper helper) {
        var cow = EntityTypes.COW.spawn(helper.getLevel(), helper.absolutePos(POSITION), EntitySpawnReason.LOAD);
        helper.assertValueEqual(TraitService.read(cow).orElseThrow().outcome(), "legacy_skipped", "Legacy mobs are not upgraded automatically");
        cow.discard();
        var service = service(new BuildupConfig());
        for (var type : List.of(EntityTypes.WITHER, EntityTypes.ENDER_DRAGON, EntityTypes.ELDER_GUARDIAN,
                EntityTypes.RAVAGER, EntityTypes.WARDEN, EntityTypes.SHEEP)) {
            var entity = type.create(helper.getLevel(), EntitySpawnReason.COMMAND);
            service.initialize(entity);
            helper.assertTrue(!entity.hasAttached(TraitService.DATA), "Boss or unlisted entity must not enter pool");
            entity.discard();
        }
        helper.succeed();
    }

    @GameTest
    public void unknownSpawnSourceIsSkipped(GameTestHelper helper) {
        // Simulates a caller bypassing EntityType.create; no claimed third-party compatibility.
        var cow = new net.minecraft.world.entity.animal.cow.Cow(EntityTypes.COW, helper.getLevel());
        cow.snapTo(helper.absolutePos(POSITION), 0, 0);
        helper.getLevel().addFreshEntity(cow);
        var initial = TraitService.read(cow).orElseThrow();
        helper.assertValueEqual(initial.outcome(), "source_unknown_skipped", "Unknown source policy");
        helper.assertValueEqual(initial.origin(), "UNKNOWN", "Missing reason is explicit");
        service(new BuildupConfig()).initialize(cow);
        helper.assertValueEqual(TraitService.read(cow).orElseThrow(), initial, "Unknown source must not retry");
        cow.discard();
        helper.succeed();
    }

    @GameTest
    public void emptyRollSurvivesChangedProbability(GameTestHelper helper) {
        var config = new BuildupConfig();
        config.traits.cow.common.accept(0);
        config.traits.cow.advanced.accept(0);
        config.traits.cow.rare.accept(0);
        var service = service(config);
        var cow = EntityTypes.COW.create(helper.getLevel(), EntitySpawnReason.COMMAND);
        service.initialize(cow);
        helper.assertTrue(TraitService.read(cow).orElseThrow().entries().isEmpty(), "Zero probability gives an empty roll");
        var initial = cow.getAttached(TraitService.DATA).copy();
        config.traits.cow.rare.accept(1000);
        service.initialize(cow);
        helper.assertValueEqual(cow.getAttached(TraitService.DATA), initial, "Empty roll counts as initialized");
        var loaded = reload(helper, cow);
        helper.assertValueEqual(loaded.getAttached(TraitService.DATA), initial, "Empty roll survives real serialization");
        loaded.discard();
        helper.succeed();
    }

    @GameTest
    public void invalidTraitCombinationsAreRejected(GameTestHelper helper) {
        var invalid = new TraitState(1, "rolled", "COMMAND", List.of(
                new TraitState.Entry(TraitKind.COMMON.id(), 1), new TraitState.Entry(TraitKind.RARE.id(), 3)));
        helper.assertTrue(TraitState.CODEC.encodeStart(NbtOps.INSTANCE, invalid).error().isPresent(), "Mutual exclusion also validated on save");
        var wrongLevel = new TraitState(1, "rolled", "COMMAND", List.of(new TraitState.Entry(TraitKind.RARE.id(), 1)));
        helper.assertTrue(TraitState.CODEC.encodeStart(NbtOps.INSTANCE, wrongLevel).error().isPresent(), "ID and level must match");
        helper.succeed();
    }

    @GameTest
    public void futureAndInvalidDataArePreserved(GameTestHelper helper) {
        var cow = helper.spawn(EntityTypes.COW, POSITION, EntitySpawnReason.COMMAND);
        var future = marker();
        future.putInt("version", 99);
        future.putString("future_field", "retain me");
        cow.setAttached(TraitService.DATA, future);
        var service = service(new BuildupConfig());
        service.initialize(cow);
        helper.assertTrue(service.activeTraits(cow).isEmpty(), "Unknown schema fails closed");
        var loaded = reload(helper, cow);
        helper.assertValueEqual(loaded.getAttached(TraitService.DATA), future, "Future payload retained verbatim");
        var invalid = new CompoundTag();
        invalid.putString("invalid", "retain me too");
        loaded.setAttached(TraitService.DATA, invalid);
        service.initialize(loaded);
        helper.assertTrue(service.activeTraits(loaded).isEmpty(), "Invalid schema fails closed");
        var secondLoad = reload(helper, loaded);
        helper.assertValueEqual(secondLoad.getAttached(TraitService.DATA), invalid, "Invalid payload retained without reroll");
        secondLoad.discard();
        helper.succeed();
    }
}
