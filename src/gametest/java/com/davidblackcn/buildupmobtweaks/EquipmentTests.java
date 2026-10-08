package com.davidblackcn.buildupmobtweaks;

import com.davidblackcn.buildupmobtweaks.config.BuildupConfig;
import com.davidblackcn.buildupmobtweaks.equipment.*;
import com.davidblackcn.buildupmobtweaks.feature.FeatureRegistry;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.util.List;
import java.util.Set;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;

public class EquipmentTests {
    private static final BlockPos POS = new BlockPos(2, 2, 2);
    private static final String BASE = """
            {"version":1,"entities":["minecraft:zombie","minecraft:husk","minecraft:drowned"],
             "slot":"mainhand","entries":[{"item":"minecraft:wooden_sword","weight":1}]}
            """;
    private static EquipmentPool decode(String json) {
        var parsed = JsonParser.parseString(json);
        EquipmentPools.validateFields(parsed);
        return EquipmentPool.CODEC.parse(JsonOps.INSTANCE, parsed).getOrThrow();
    }
    private static EquipmentPools.Resolved resolve(GameTestHelper helper, String json) {
        return EquipmentPools.resolve(BuildupMobTweaks.id("test"), decode(json), helper.getLevel().registryAccess(), helper.getLevel().enabledFeatures());
    }
    private static EquipmentPools.Snapshot snapshot(GameTestHelper helper, String json) {
        return new EquipmentPools.Snapshot(List.of(resolve(helper, json)), 0);
    }
    private static BuildupConfig config() {
        var config = new BuildupConfig();
        config.equipment.assignmentChance.accept(1000);
        return config;
    }
    private static Mob create(GameTestHelper helper, EntityType<? extends Mob> type, EntitySpawnReason reason) {
        var mob = type.create(helper.getLevel(), reason);
        mob.snapTo(helper.absolutePos(POS), 0, 0);
        mob.setNoAi(true);
        return mob;
    }
    private static void rejects(GameTestHelper helper, Runnable action) {
        boolean rejected = false;
        try { action.run(); } catch (IllegalArgumentException | IllegalStateException expected) { rejected = true; }
        helper.assertTrue(rejected, "Invalid pool must be rejected");
    }
    private static Mob reload(GameTestHelper helper, Mob mob) {
        var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, helper.getLevel().registryAccess());
        mob.saveWithoutId(output);
        var type = mob.getType();
        mob.discard();
        var input = TagValueInput.create(ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), output.buildResult());
        var loaded = (Mob) EntityType.create(type, input, helper.getLevel(), EntitySpawnReason.LOAD).orElseThrow();
        helper.getLevel().addFreshEntity(loaded);
        return loaded;
    }

    @GameTest
    public void codecAndReferenceValidation(GameTestHelper helper) {
        for (String invalid : List.of(BASE.replace("\"version\":1", "\"version\":2"),
                BASE.replace("\"weight\":1", "\"weight\":0"), BASE.replace("\"weight\":1", "\"weight\":1.5"), BASE.replace("\"version\":1", "\"version\":1.5"), BASE.replace("\"weight\":1", "\"weight\":10001"),
                BASE.replace("\"slot\":\"mainhand\"", "\"slot\":\"saddle\""), BASE.replace("\"version\":1", "\"version\":1,\"replace\":true"),
                BASE.replace("\"weight\":1", "\"weight\":1,\"count\":64"))) rejects(helper, () -> decode(invalid));
        for (String item : List.of("missing:weapon", "#missing:weapons", "#buildupmobtweaks:empty_test", "minecraft:air")) {
            rejects(helper, () -> resolve(helper, BASE.replace("minecraft:wooden_sword", item)));
        }
        rejects(helper, () -> resolve(helper, BASE.replace("minecraft:zombie", "minecraft:wither")));
        helper.succeed();
    }

    @GameTest
    public void tagsAndWeightedDistribution(GameTestHelper helper) {
        String json = BASE.replace("\"minecraft:zombie\",\"minecraft:husk\",\"minecraft:drowned\"", "\"#buildupmobtweaks:test_zombies\"")
                .replace("{\"item\":\"minecraft:wooden_sword\",\"weight\":1}",
                        "{\"item\":\"#buildupmobtweaks:test_weapons\",\"weight\":2},{\"item\":\"minecraft:shield\",\"weight\":1}");
        var pool = resolve(helper, json);
        helper.assertValueEqual(pool.entities(), Set.of(EntityTypes.ZOMBIE, EntityTypes.HUSK, EntityTypes.DROWNED), "Entity tag resolves");
        var random = RandomSource.create(68241);
        int sword = 0, bow = 0, shield = 0;
        for (int i = 0; i < 60000; i++) {
            var item = pool.select(random);
            if (item == Items.WOODEN_SWORD) sword++;
            else if (item == Items.BOW) bow++;
            else if (item == Items.SHIELD) shield++;
            else helper.assertTrue(false, "Unexpected tag member");
        }
        for (int count : new int[]{sword, bow, shield}) helper.assertTrue(Math.abs(count - 20000) < 700, "Weights apply to entry, then uniform tag selection");
        BuildupMobTweaks.LOGGER.info("Equipment sampling seed=68241 n=60000 sword={} bow={} shield={}", sword, bow, shield);
        helper.succeed();
    }

    @GameTest
    public void existingEquipmentAndDropChanceAreUntouched(GameTestHelper helper) {
        var mob = create(helper, EntityTypes.ZOMBIE, EntitySpawnReason.COMMAND);
        var existing = new ItemStack(Items.DIAMOND_SWORD);
        existing.setDamageValue(23);
        existing.set(DataComponents.CUSTOM_NAME, Component.literal("external equipment"));
        var expected = existing.copy();
        mob.setItemSlot(EquipmentSlot.MAINHAND, existing);
        mob.setDropChance(EquipmentSlot.MAINHAND, .42f);
        new EquipmentService(new FeatureRegistry(config())).initialize(mob, snapshot(helper, BASE));
        helper.assertTrue(ItemStack.matches(mob.getMainHandItem(), expected), "Existing item including components is preserved");
        helper.assertValueEqual(mob.getDropChances().byEquipment(EquipmentSlot.MAINHAND), .42f, "Existing drop chance untouched");
        mob.discard(); helper.succeed();
    }

    @GameTest
    public void oneAttemptSurvivesReloadAndEmptySlot(GameTestHelper helper) {
        var mob = create(helper, EntityTypes.ZOMBIE, EntitySpawnReason.COMMAND);
        var service = new EquipmentService(new FeatureRegistry(config()));
        var pool = snapshot(helper, BASE);
        float drop = mob.getDropChances().byEquipment(EquipmentSlot.MAINHAND);
        service.initialize(mob, pool);
        helper.getLevel().addFreshEntity(mob);
        helper.assertValueEqual(mob.getMainHandItem().getItem(), Items.WOODEN_SWORD, "Fresh assignment");
        helper.assertValueEqual(mob.getMainHandItem().getCount(), 1, "Exactly one item");
        helper.assertValueEqual(mob.getDropChances().byEquipment(EquipmentSlot.MAINHAND), drop, "Generated item does not boost drop rate");
        var saved = mob.getAttached(EquipmentService.DATA).copy();
        var loaded = reload(helper, mob);
        helper.assertValueEqual(loaded.getAttached(EquipmentService.DATA), saved, "Assignment state saved");
        helper.assertValueEqual(loaded.getMainHandItem().getItem(), Items.WOODEN_SWORD, "Item saved");
        loaded.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        for (int i = 0; i < 10; i++) service.initialize(loaded, snapshot(helper, BASE.replace("wooden_sword", "trident")));
        helper.assertTrue(loaded.getMainHandItem().isEmpty(), "Removed equipment must not be replenished");
        loaded.discard(); helper.succeed();
    }

    @GameTest
    public void disabledMissedAndMissingRuleDoNotBackfill(GameTestHelper helper) {
        for (int scenario = 0; scenario < 4; scenario++) {
            var config = config();
            if (scenario == 0) config.equipment.poolAssignment.accept(false);
            if (scenario == 1) config.equipment.assignmentChance.accept(0);
            if (scenario == 3) config.general.enabled.accept(false);
            var service = new EquipmentService(new FeatureRegistry(config));
            var mob = create(helper, EntityTypes.ZOMBIE, EntitySpawnReason.COMMAND);
            service.initialize(mob, scenario == 2 ? new EquipmentPools.Snapshot(List.of(), 0) : snapshot(helper, BASE));
            helper.assertTrue(mob.getMainHandItem().isEmpty(), "Initial attempt must skip");
            var saved = mob.getAttached(EquipmentService.DATA).copy();
            config.general.enabled.accept(true); config.equipment.poolAssignment.accept(true); config.equipment.assignmentChance.accept(1000);
            service.initialize(mob, snapshot(helper, BASE));
            helper.assertTrue(mob.getMainHandItem().isEmpty(), "No backfill after settings/rules change");
            helper.assertValueEqual(mob.getAttached(EquipmentService.DATA), saved, "No reroll");
            mob.discard();
        }
        helper.succeed();
    }
    @GameTest
    public void filtersAndArmorSlotAreRespected(GameTestHelper helper) {
        var mob = create(helper, EntityTypes.ZOMBIE, EntitySpawnReason.COMMAND);
        var service = new EquipmentService(new FeatureRegistry(config()));
        for (String json : List.of(BASE.replace("\"slot\"", "\"dimensions\":[\"minecraft:the_nether\"],\"slot\""),
                BASE.replace("\"slot\"", "\"difficulties\":[\"peaceful\"],\"slot\""))) {
            helper.assertTrue(!resolve(helper, json).matches(mob), "Environment filter excludes entity");
        }
        ((net.minecraft.world.entity.monster.zombie.Zombie) mob).setBaby(true);
        helper.assertTrue(!resolve(helper, BASE).matches(mob), "Adult-only filter");
        ((net.minecraft.world.entity.monster.zombie.Zombie) mob).setBaby(false);
        service.initialize(mob, snapshot(helper, BASE.replace("mainhand", "head")));
        helper.assertTrue(mob.getItemBySlot(EquipmentSlot.HEAD).isEmpty(), "Sword cannot be forced into armor slot");
        mob.discard();
        var slots = List.of("head", "chest", "legs", "feet");
        var armor = List.of("iron_helmet", "iron_chestplate", "iron_leggings", "iron_boots");
        var actualSlots = List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);
        for (int i = 0; i < slots.size(); i++) {
            var armored = create(helper, EntityTypes.ZOMBIE, EntitySpawnReason.COMMAND);
            service.initialize(armored, snapshot(helper, BASE.replace("mainhand", slots.get(i)).replace("wooden_sword", armor.get(i))));
            helper.assertValueEqual(armored.getItemBySlot(actualSlots.get(i)).getCount(), 1, "Matching armor fills its slot");
            helper.assertTrue(armored.getMainHandItem().isEmpty(), "Armor must not overwrite another slot");
            armored.discard();
        }
        helper.succeed();
    }

    @GameTest
    public void conversionTransfersOneOwnedItem(GameTestHelper helper) {
        var husk = EntityTypes.HUSK.create(helper.getLevel(), EntitySpawnReason.COMMAND);
        husk.snapTo(helper.absolutePos(POS), 0, 0);
        new EquipmentService(new FeatureRegistry(config())).initialize(husk, snapshot(helper, BASE));
        helper.getLevel().addFreshEntity(husk);
        var saved = husk.getAttached(EquipmentService.DATA).copy();
        var zombie = husk.convertTo(EntityTypes.ZOMBIE, ConversionParams.single(husk, true, true), ignored -> {});
        helper.assertTrue(husk.getMainHandItem().isEmpty(), "Vanilla transfers ownership out of source");
        helper.assertValueEqual(zombie.getMainHandItem().getCount(), 1, "One item in target");
        helper.assertValueEqual(zombie.getAttached(EquipmentService.DATA), saved, "Assignment marker inherited");
        var drowned = zombie.convertTo(EntityTypes.DROWNED, ConversionParams.single(zombie, true, true), ignored -> {});
        helper.assertTrue(zombie.getMainHandItem().isEmpty(), "Second source cleared");
        helper.assertValueEqual(drowned.getMainHandItem().getItem(), Items.WOODEN_SWORD, "Same equipment after second conversion");
        helper.assertValueEqual(drowned.getMainHandItem().getCount(), 1, "No conversion duplication");
        drowned.discard(); helper.succeed();
    }

    @GameTest
    public void realDeathProducesAtMostOneEquipmentDrop(GameTestHelper helper) {
        var mob = create(helper, EntityTypes.ZOMBIE, EntitySpawnReason.COMMAND);
        // A pre-existing guaranteed rate makes the death assertion deterministic; production never sets it.
        mob.setDropChance(EquipmentSlot.MAINHAND, 2.0f);
        new EquipmentService(new FeatureRegistry(config())).initialize(mob, snapshot(helper, BASE));
        helper.getLevel().addFreshEntity(mob);
        mob.kill(helper.getLevel());
        helper.runAfterDelay(2, () -> {
            int count = helper.getEntities(EntityTypes.ITEM, POS, 6).stream()
                    .map(entity -> entity.getItem()).filter(stack -> stack.getItem() == Items.WOODEN_SWORD)
                    .mapToInt(ItemStack::getCount).sum();
            helper.assertValueEqual(count, 1, "A real death emits exactly one guaranteed equipment stack");
            helper.succeed();
        });
    }

    @GameTest
    public void legacyUnknownDataAndBossesAreSkipped(GameTestHelper helper) {
        var service = new EquipmentService(new FeatureRegistry(config()));
        var pool = snapshot(helper, BASE);
        for (var reason : List.of(EntitySpawnReason.LOAD, EntitySpawnReason.CONVERSION, EntitySpawnReason.DIMENSION_TRAVEL)) {
            var mob = create(helper, EntityTypes.ZOMBIE, reason);
            service.initialize(mob, pool);
            helper.assertTrue(mob.getMainHandItem().isEmpty(), "Skip unknown or old source"); mob.discard();
        }
        var mob = create(helper, EntityTypes.ZOMBIE, EntitySpawnReason.COMMAND);
        var future = new CompoundTag(); future.putInt("version", 99); future.putString("future", "keep");
        mob.setAttached(EquipmentService.DATA, future);
        service.initialize(mob, pool);
        var loaded = reload(helper, mob);
        helper.assertValueEqual(loaded.getAttached(EquipmentService.DATA), future, "Unknown data preserved");
        helper.assertTrue(loaded.getMainHandItem().isEmpty(), "Unknown data cannot trigger assignment"); loaded.discard();
        for (var type : List.of(EntityTypes.WITHER, EntityTypes.ENDER_DRAGON, EntityTypes.ELDER_GUARDIAN, EntityTypes.RAVAGER, EntityTypes.WARDEN)) {
            var boss = type.create(helper.getLevel(), EntitySpawnReason.COMMAND);
            helper.assertTrue(!EquipmentService.eligible(boss), "Boss excluded even if pack targets it"); boss.discard();
        }
        helper.succeed();
    }

    @GameTest
    public void capabilitiesUseCurrentItemWithoutTraits(GameTestHelper helper) {
        var items = List.of(Items.BOW, Items.CROSSBOW, Items.TRIDENT, Items.SHIELD, Items.WOODEN_SWORD);
        var abilities = List.of(EquipmentCapabilities.Ability.BOW, EquipmentCapabilities.Ability.CROSSBOW,
                EquipmentCapabilities.Ability.TRIDENT, EquipmentCapabilities.Ability.SHIELD, EquipmentCapabilities.Ability.MELEE);
        var mob = create(helper, EntityTypes.ZOMBIE, EntitySpawnReason.COMMAND);
        for (int i = 0; i < items.size(); i++) {
            mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(items.get(i)));
            helper.assertValueEqual(EquipmentCapabilities.identify(mob.getMainHandItem()), Set.of(abilities.get(i)), "Current actual weapon, no Trait required");
        }
        helper.assertTrue(EquipmentCapabilities.identify(new ItemStack(Items.STICK)).isEmpty(), "Unsupported items provide no claimed ability");
        helper.assertTrue(EquipmentCapabilities.identify(ItemStack.EMPTY).isEmpty(), "Empty hand");
        mob.discard(); helper.succeed();
    }

    @GameTest
    public void serverResourceStoreContainsDefaultPool(GameTestHelper helper) {
        var pools = EquipmentPools.get(helper.getLevel().getServer());
        helper.assertValueEqual(pools.rejected(), 0, "Default resources are valid");
        helper.assertTrue(pools.rules().stream().anyMatch(rule -> rule.id().equals(BuildupMobTweaks.id("zombie_demo"))), "Actual Fabric reload populated server resource store");
        helper.succeed();
    }
}