package com.davidblackcn.buildupmobtweaks;

import com.davidblackcn.buildupmobtweaks.combat.*;
import com.davidblackcn.buildupmobtweaks.config.BuildupConfig;
import com.davidblackcn.buildupmobtweaks.feature.FeatureRegistry;
import java.util.*;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.zombie.Drowned;
import net.minecraft.world.entity.projectile.arrow.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import static com.davidblackcn.buildupmobtweaks.CombatTestWorld.*;

public class TridentTests {
    private static Drowned armed(GameTestHelper h) {
        var mob = mob(h, EntityTypes.DROWNED, 3, 3);
        var stack = new ItemStack(Items.TRIDENT); stack.setDamageValue(19); stack.set(DataComponents.CUSTOM_NAME, Component.literal("owned"));
        mob.setItemSlot(EquipmentSlot.MAINHAND, stack); mob.setDropChance(EquipmentSlot.MAINHAND, .42f);
        h.getLevel().addFreshEntity(mob); return mob;
    }
    private static Mob target(GameTestHelper h) { var target = mob(h, EntityTypes.IRON_GOLEM, 12, 3); h.getLevel().addFreshEntity(target); return target; }
    private static DrownedTridents service(BuildupConfig cfg) { return new DrownedTridents(new FeatureRegistry(cfg)); }
    private static void land(GameTestHelper h, ThrownTrident trident) {
        trident.snapTo(h.absolutePos(new BlockPos(7, 2, 3)), 0, 0); trident.setDeltaMovement(0, -.3, 0);
    }
    @GameTest(structure = ARENA)
    public void realThrowTransfersOneItemAndBlocksSyntheticThrows(GameTestHelper h) {
        floor(h, Blocks.STONE); var mob = armed(h); var target = target(h); var original = mob.getMainHandItem().copy();
        mob.performRangedAttack(target, 1); var projectile = DrownedTridents.instance().projectile(mob);
        h.assertTrue(projectile != null && mob.getMainHandItem().isEmpty(), "Actual injected throw removes held weapon");
        h.assertTrue(ItemStack.matches(projectile.getPickupItemStackOrigin(), original), "Projectile owns full original item components");
        for (int i = 0; i < 10; i++) mob.performRangedAttack(target, 1);
        h.assertValueEqual(h.getEntities(EntityTypes.TRIDENT, new BlockPos(4, 2, 3), 16).size(), 1, "No synthetic or repeated tridents with empty hand");
        h.assertValueEqual(projectile.pickup, AbstractArrow.Pickup.DISALLOWED, "Reserved projectile cannot be stolen early");
        projectile.discard(); mob.discard(); target.discard(); h.succeed();
    }
    @GameTest(structure = ARENA, maxTicks = 100)
    public void threeCyclesPreserveIdentityComponentsAndDropPolicy(GameTestHelper h) {
        floor(h, Blocks.STONE); var mob = armed(h); var target = target(h); var original = mob.getMainHandItem().copy();
        var service = DrownedTridents.instance(); Set<UUID> flights = new HashSet<>();
        mob.performRangedAttack(target, 1); var first = service.projectile(mob); flights.add(first.getUUID()); land(h, first);
        for (int cycle = 1; cycle <= 3; cycle++) {
            final int current = cycle;
            h.runAtTickTime(cycle * 22, () -> {
                var projectile = service.projectile(mob);
                h.assertTrue(projectile != null && ((OwnedTrident) projectile).buildup$readyForRecovery(), "Landed projectile ready");
                mob.snapTo(projectile.position(), 0, 0);
                h.assertTrue(service.recover(mob, projectile), "Recover actual item");
                h.assertTrue(projectile.isRemoved() && !mob.hasAttached(DrownedTridents.FLIGHT), "Old ownership consumed");
                h.assertTrue(ItemStack.matches(mob.getMainHandItem(), original), "Same named damaged item after each cycle");
                h.assertValueEqual(mob.getDropChances().byEquipment(EquipmentSlot.MAINHAND), .42f, "Original drop policy retained");
                h.assertFalse(service.recover(mob, projectile), "Cannot recover twice");
                if (current == 3) { h.assertValueEqual(flights.size(), 3, "Three separate physical flights, one item"); mob.discard(); target.discard(); h.succeed(); }
                else {
                    mob.snapTo(h.absolutePos(new BlockPos(3, 2, 3)), 0, 0); mob.performRangedAttack(target, 1);
                    var next = service.projectile(mob); flights.add(next.getUUID()); land(h, next);
                }
            });
        }
    }
    @GameTest(structure = ARENA, maxTicks = 60)
    public void realGoalWalksToRecoverWithoutChangingTarget(GameTestHelper h) {
        floor(h, Blocks.STONE); var mob = armed(h); var target = target(h); mob.setTarget(target);
        mob.performRangedAttack(target, 1); var projectile = DrownedTridents.instance().projectile(mob); land(h, projectile); mob.setNoAi(false);
        h.succeedWhen(() -> {
            h.assertTrue(projectile.isRemoved() && mob.getMainHandItem().getItem() == Items.TRIDENT, "Real recovery goal walks to landed weapon");
            h.assertTrue(mob.getTarget() == target, "Recovery keeps combat target");
            mob.discard(); target.discard();
        });
    }
    @GameTest(structure = ARENA, maxTicks = 60)
    public void loadedFlightAndOwnerSurviveActualSerialization(GameTestHelper h) {
        floor(h, Blocks.STONE); var mob = armed(h); var target = target(h);
        mob.performRangedAttack(target, 1); var projectile = DrownedTridents.instance().projectile(mob); land(h, projectile);
        var flight = mob.getAttached(DrownedTridents.FLIGHT).copy(); var owned = projectile.getAttached(DrownedTridents.OWNED).copy();
        var loadedProjectile = reload(h, projectile); var loadedMob = reload(h, mob);
        h.assertValueEqual(loadedMob.getAttached(DrownedTridents.FLIGHT), flight, "Owner transaction saved");
        h.assertValueEqual(loadedProjectile.getAttached(DrownedTridents.OWNED), owned, "Projectile ownership saved");
        h.runAtTickTime(25, () -> {
            loadedMob.snapTo(loadedProjectile.position(), 0, 0);
            h.assertTrue(DrownedTridents.instance().recover(loadedMob, loadedProjectile), "UUID resolution survives reload order");
            h.assertValueEqual(loadedMob.getMainHandItem().getDamageValue(), 19, "Recovered saved item"); loadedMob.discard(); target.discard(); h.succeed();
        });
    }
    @GameTest(structure = ARENA)
    public void timeoutLootDecisionIsFinalAndHonorsOriginalRate(GameTestHelper h) {
        floor(h, Blocks.STONE); var target = target(h);
        for (float chance : new float[]{0, 2}) {
            var mob = armed(h); mob.setDropChance(EquipmentSlot.MAINHAND, chance); mob.performRangedAttack(target, 1);
            var projectile = DrownedTridents.instance().projectile(mob);
            DrownedTridents.instance().release(projectile);
            h.assertValueEqual(projectile.pickup, chance == 0 ? AbstractArrow.Pickup.DISALLOWED : AbstractArrow.Pickup.ALLOWED, "No guaranteed rare-weapon farming");
            var saved = projectile.getAttached(DrownedTridents.OWNED).copy();
            for (int i = 0; i < 10; i++) DrownedTridents.instance().release(projectile);
            h.assertValueEqual(projectile.getAttached(DrownedTridents.OWNED), saved, "Release rolls at most once");
            h.assertTrue(mob.getMainHandItem().isEmpty(), "No fallback replacement"); projectile.discard(); mob.discard();
        }
        target.discard(); h.succeed();
    }
    @GameTest(structure = ARENA, maxTicks = 60)
    public void independentRecoveryAndPlayerPickupSwitches(GameTestHelper h) {
        floor(h, Blocks.STONE); var cfg = new BuildupConfig(); var service = service(cfg); var mob = armed(h); var target = target(h);
        cfg.hostile.drowned.tridentConservation.accept(false);
        h.assertFalse(service.throwHeld(mob, target), "Disabled conservation service leaves vanilla ownership untouched");
        h.assertTrue(mob.getMainHandItem().getItem() == Items.TRIDENT, "No hand mutation while disabled");
        cfg.hostile.drowned.tridentConservation.accept(true); mob.setDropChance(EquipmentSlot.MAINHAND, 2);
        h.assertTrue(service.throwHeld(mob, target), "Enabled conservation"); var projectile = service.projectile(mob); land(h, projectile);
        h.runAtTickTime(25, () -> {
            mob.snapTo(projectile.position(), 0, 0); cfg.hostile.drowned.tridentRecovery.accept(false);
            h.assertFalse(service.recover(mob, projectile), "Independent recovery disable");
            cfg.hostile.drowned.tridentPlayerPickup.accept(false); service.release(projectile);
            h.assertValueEqual(projectile.pickup, AbstractArrow.Pickup.DISALLOWED, "Independent public pickup disable even for guaranteed equipment");
            h.assertTrue(mob.getMainHandItem().isEmpty(), "Disabling never recreates the weapon"); projectile.discard(); mob.discard(); target.discard(); h.succeed();
        });
    }
    @GameTest(structure = ARENA, maxTicks = 25)
    public void deathRestoresLoadedFlightIntoVanillaLootExactlyOnce(GameTestHelper h) {
        floor(h, Blocks.STONE); var mob = armed(h); var target = target(h); mob.setDropChance(EquipmentSlot.MAINHAND, 2);
        mob.performRangedAttack(target, 1); var projectile = DrownedTridents.instance().projectile(mob); mob.kill(h.getLevel());
        h.runAfterDelay(2, () -> {
            h.assertTrue(projectile.isRemoved(), "Death consumed the projectile before vanilla loot iteration");
            int count = h.getEntities(EntityTypes.ITEM, new BlockPos(3, 2, 3), 6).stream().map(e -> e.getItem())
                    .filter(stack -> stack.getItem() == Items.TRIDENT).mapToInt(ItemStack::getCount).sum();
            h.assertValueEqual(count, 1, "One real death, one original guaranteed weapon"); target.discard(); h.succeed();
        });
    }
    @GameTest(structure = ARENA)
    public void invalidVersionFullHandAndMissingProjectileNeverBackfill(GameTestHelper h) {
        floor(h, Blocks.STONE); var mob = armed(h); var target = target(h); var future = new CompoundTag(); future.putInt("version", 99);
        mob.setAttached(DrownedTridents.FLIGHT, future); mob.performRangedAttack(target, 1);
        h.assertValueEqual(mob.getAttached(DrownedTridents.FLIGHT), future, "Unknown transaction preserved and blocks new owned throws");
        mob.removeAttached(DrownedTridents.FLIGHT); mob.performRangedAttack(target, 1); var projectile = DrownedTridents.instance().projectile(mob);
        var external = new ItemStack(Items.DIAMOND_SWORD); mob.setItemSlot(EquipmentSlot.MAINHAND, external);
        h.assertFalse(DrownedTridents.instance().recover(mob, projectile), "Never overwrite external equipment");
        projectile.discard(); var data = mob.getAttached(DrownedTridents.FLIGHT).copy(); data.putLong("deadline", 0); mob.setAttached(DrownedTridents.FLIGHT, data);
        DrownedTridents.instance().expireReference(mob);
        h.assertTrue(mob.getMainHandItem() == external && !mob.hasAttached(DrownedTridents.FLIGHT), "Missing flight expires without creating equipment");
        mob.discard(); target.discard(); h.succeed();
    }
    @GameTest(structure = ARENA, maxTicks = 60)
    public void loyaltyStaysOnItemButDoesNotDiscardMobRecovery(GameTestHelper h) {
        floor(h, Blocks.STONE); var mob = armed(h); var target = target(h);
        var loyalty = h.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.LOYALTY);
        mob.getMainHandItem().enchant(loyalty, 3); var original = mob.getMainHandItem().copy();
        mob.performRangedAttack(target, 1); var projectile = DrownedTridents.instance().projectile(mob); land(h, projectile);
        h.runAtTickTime(25, () -> {
            h.assertFalse(projectile.isRemoved(), "Vanilla non-player loyalty discard suppressed only for owned projectile");
            mob.snapTo(projectile.position(), 0, 0); h.assertTrue(DrownedTridents.instance().recover(mob, projectile), "Enchanted item recovered");
            h.assertTrue(ItemStack.matches(mob.getMainHandItem(), original), "Vanilla enchantments unchanged"); mob.discard(); target.discard(); h.succeed();
        });
    }
    @GameTest(structure = ARENA, maxTicks = 140)
    public void realSwimmingGoalRecoversAndRetainsTarget(GameTestHelper h) {
        floor(h, Blocks.STONE);
        for (int x = 1; x < 15; x++) for (int z = 1; z < 15; z++) for (int y = 2; y < 5; y++)
            h.setBlock(new BlockPos(x, y, z), Blocks.WATER);
        var mob = armed(h); var target = target(h); mob.setTarget(target);
        mob.performRangedAttack(target, 1); var projectile = DrownedTridents.instance().projectile(mob);
        land(h, projectile); mob.setNoAi(false);
        h.succeedWhen(() -> {
            h.assertTrue(projectile.isRemoved() && mob.getMainHandItem().is(Items.TRIDENT), "Swim to physical submerged weapon");
            h.assertTrue(mob.getTarget() == target, "Water recovery retains combat target");
            mob.discard(); target.discard();
        });
    }
    @GameTest(structure = ARENA, maxTicks = 60)
    public void actualGroundPickupThenThrowConsumesOneItem(GameTestHelper h) {
        floor(h, Blocks.STONE); var mob = armed(h); mob.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        mob.setCanPickUpLoot(true); mob.setNoAi(false);
        var item = h.spawnItem(Items.TRIDENT, 3.5f, 2f, 3.5f); item.setNoPickUpDelay();
        h.runAtTickTime(40, () -> {
            h.assertTrue(item.isRemoved() && mob.getMainHandItem().is(Items.TRIDENT), "Actual item entity consumed by vanilla mob pickup");
            float chance = mob.getDropChances().byEquipment(EquipmentSlot.MAINHAND);
            h.assertTrue(chance > 1, "Player supplied equipment preserves guaranteed drop");
            var target = target(h); mob.performRangedAttack(target, 1); var projectile = DrownedTridents.instance().projectile(mob);
            h.assertTrue(projectile != null && mob.getMainHandItem().isEmpty(), "Picked weapon transfers once");
            h.assertValueEqual(projectile.getAttached(DrownedTridents.OWNED).getFloatOr("drop_chance", 0), chance, "Pickup drop policy retained");
            projectile.discard(); mob.discard(); target.discard(); h.succeed();
        });
    }
    @GameTest(structure = ARENA, maxTicks = 70)
    public void elapsedTimeoutAllowsOneRealSurvivalPickup(GameTestHelper h) {
        floor(h, Blocks.STONE); var cfg = new BuildupConfig(); cfg.hostile.drowned.recoveryTimeout.accept(40);
        var combat = service(cfg); var mob = armed(h); mob.setDropChance(EquipmentSlot.MAINHAND, 2); var target = target(h);
        combat.throwHeld(mob, target); var projectile = combat.projectile(mob); land(h, projectile);
        var original = projectile.getPickupItemStackOrigin().copy(); var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        h.runAtTickTime(25, () -> {
            projectile.playerTouch(player);
            h.assertFalse(projectile.isRemoved(), "Reservation rejects real player pickup");
        });
        h.runAtTickTime(50, () -> {
            h.assertTrue(projectile.getAttached(DrownedTridents.OWNED).getBooleanOr("released", false), "Actual elapsed tick releases once");
            cfg.hostile.drowned.tridentPlayerPickup.accept(false); combat.projectileTick(projectile);
            projectile.playerTouch(player); h.assertFalse(projectile.isRemoved(), "Turning pickup off masks already released weapon");
            cfg.hostile.drowned.tridentPlayerPickup.accept(true); combat.projectileTick(projectile);
            projectile.playerTouch(player);
            h.assertTrue(projectile.isRemoved(), "Vanilla survival pickup consumed released projectile");
            h.assertValueEqual(player.getInventory().countItem(Items.TRIDENT), 1, "Player owns one trident");
            h.assertTrue(mob.getMainHandItem().isEmpty() && !mob.hasAttached(DrownedTridents.FLIGHT), "Former owner never backfills");
            h.assertTrue(ItemStack.matches(player.getInventory().getItem(0), original), "Pickup retains all components");
            mob.discard(); target.discard(); player.discard(); h.succeed();
        });
    }
    @GameTest(structure = ARENA)
    public void deathWithOccupiedHandPreservesBothDistinctItems(GameTestHelper h) {
        floor(h, Blocks.STONE); var mob = armed(h); mob.setDropChance(EquipmentSlot.MAINHAND, 2); var target = target(h);
        mob.performRangedAttack(target, 1); var projectile = DrownedTridents.instance().projectile(mob);
        mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_SWORD)); mob.kill(h.getLevel());
        h.assertFalse(projectile.isRemoved(), "Occupied hand does not consume distant original weapon");
        h.assertValueEqual(projectile.pickup, AbstractArrow.Pickup.ALLOWED, "Original guaranteed weapon released exactly once");
        h.assertItemEntityCountIs(Items.DIAMOND_SWORD, new BlockPos(3, 2, 3), 6, 1);
        h.assertItemEntityNotPresent(Items.TRIDENT); projectile.discard(); target.discard(); h.succeed();
    }
    @GameTest(structure = ARENA)
    public void vanillaVanishingIsRespectedOnRelease(GameTestHelper h) {
        floor(h, Blocks.STONE); var mob = armed(h); mob.setDropChance(EquipmentSlot.MAINHAND, 2); var target = target(h);
        mob.getMainHandItem().enchant(h.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.VANISHING_CURSE), 1);
        mob.performRangedAttack(target, 1); var projectile = DrownedTridents.instance().projectile(mob);
        DrownedTridents.instance().release(projectile);
        h.assertValueEqual(projectile.pickup, AbstractArrow.Pickup.DISALLOWED, "Vanishing never becomes farmable public loot");
        projectile.discard(); mob.discard(); target.discard(); h.succeed();
    }
}
