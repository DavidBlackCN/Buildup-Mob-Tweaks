package com.davidblackcn.buildupmobtweaks;

import com.davidblackcn.buildupmobtweaks.rebuild.pillager.PillagerBehavior;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.illager.Pillager;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.level.GameType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;

/** Behavior assertions only observe real server ticks. Never invokes a production Goal or AI service. */
public class PillagerBehaviorTests {
    private static final String ARENA = "buildupmobtweaks:r2a_arena";
    private static void floor(GameTestHelper h) {
        h.getLevel().getServer().setDifficulty(Difficulty.NORMAL, true);
        for (int x=0;x<24;x++) for(int z=0;z<24;z++) h.setBlock(new BlockPos(x,1,z),Blocks.STONE);
    }
    private static Pillager pillager(GameTestHelper h, int x, int z) {
        var p=EntityTypes.PILLAGER.create(h.getLevel(),EntitySpawnReason.COMMAND);
        p.snapTo(h.absolutePos(new BlockPos(x,2,z)),0,0); p.setPersistenceRequired();
        p.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.CROSSBOW));
        // Entity LOAD is the real production installation path; disable only random birth supplies in fixtures.
        p.addTag("buildupmobtweaks:disable_pillager_spawn_supplies");
        h.getLevel().addFreshEntity(p); return p;
    }
    private static IronGolem target(GameTestHelper h, int x, int z) {
        var t=EntityTypes.IRON_GOLEM.create(h.getLevel(),EntitySpawnReason.COMMAND);
        t.snapTo(h.absolutePos(new BlockPos(x,2,z)),0,0); t.setNoAi(true); t.setPersistenceRequired();
        h.getLevel().addFreshEntity(t); return t;
    }
    private static int count(Pillager p, Item item) {
        int count=0;
        for(var slot:EquipmentSlot.values()) if(p.getItemBySlot(slot).is(item)) count+=p.getItemBySlot(slot).getCount();
        for(int i=0;i<p.getInventory().getContainerSize();i++) if(p.getInventory().getItem(i).is(item)) count+=p.getInventory().getItem(i).getCount();
        return count;
    }
    private static void evidence(String scenario, Pillager p, String details) {
        var rt=PillagerBehavior.runtime(p);
        BuildupMobTweaks.LOGGER.info("R2A_EVIDENCE scenario={} entity={} entityTicks={} swaps={} retreats={} meals={} exits={} {}",
                scenario,p.getUUID(),p.tickCount,rt.swaps,rt.retreats,rt.meals,rt.targetClears,details);
    }
    private static boolean[] observeArrows(GameTestHelper h, Pillager p) {
        boolean[] seen={false};
        h.onEachTick(()->{ if(!p.isRemoved() && !h.getLevel().getEntitiesOfClass(AbstractArrow.class,p.getBoundingBox().inflate(32),a->a.getOwner()==p).isEmpty()) seen[0]=true; });
        return seen;
    }
    private record TestPlayer(ServerPlayer player, EmbeddedChannel channel, Connection connection) {
        void close() {
            player.level().getServer().getConnection().getConnections().remove(connection);
            player.level().getServer().getPlayerList().remove(player); player.discard(); channel.finishAndReleaseAll();
        }
    }
    private static TestPlayer player(GameTestHelper h, int x, int z) {
        var profile=new GameProfile(UUID.randomUUID(),"R2A_"+UUID.randomUUID().toString().substring(0,8));
        var cookie=CommonListenerCookie.createInitial(profile,false);
        var player=new ServerPlayer(h.getLevel().getServer(),h.getLevel(),profile,cookie.clientInformation()) {
            @Override public boolean isClientAuthoritative() { return false; }
        };
        var connection=new Connection(PacketFlow.SERVERBOUND);
        var channel=new EmbeddedChannel(connection);
        h.getLevel().getServer().getConnection().getConnections().add(connection);
        h.getLevel().getServer().getPlayerList().placeNewPlayer(connection,player,cookie);
        player.connection.handleAcceptPlayerLoad(new net.minecraft.network.protocol.game.ServerboundPlayerLoadedPacket());
        player.setGameMode(GameType.SURVIVAL);player.snapTo(h.absolutePos(new BlockPos(x,2,z)),180,0);
        return new TestPlayer(player,channel,connection);
    }
    @GameTest(structure=ARENA,maxTicks=245)
    public void creativeSpectatorDistanceAndReacquisition(GameTestHelper h) {
        floor(h);var p=pillager(h,8,8);var fixture=player(h,8,18);var player=fixture.player();
        h.runAtTickTime(35,()->{
            h.assertTrue(p.getTarget()==player,"Natural survival player aggro");player.setGameMode(GameType.CREATIVE);
        });
        h.runAtTickTime(50,()->{
            h.assertTrue(p.getTarget()==null&&!p.isUsingItem()&&!p.isChargingCrossbow(),"Creative exits attack, look alone is allowed");
            evidence("P05_creative",p,"target=null charging=false");player.setGameMode(GameType.SURVIVAL);
        });
        h.runAtTickTime(90,()->{
            h.assertTrue(p.getTarget()==player,"Survival reacquires naturally");player.setGameMode(GameType.SPECTATOR);
        });
        h.runAtTickTime(105,()->{
            h.assertTrue(p.getTarget()==null&&!p.isUsingItem()&&!p.isChargingCrossbow(),"Spectator exits attack");
            evidence("P05_spectator",p,"target=null");player.setGameMode(GameType.SURVIVAL);
        });
        h.runAtTickTime(140,()->{
            h.assertTrue(p.getTarget()==player,"Second natural reacquisition");
            player.snapTo(p.position().add(45,0,0),0,0);
        });
        h.runAtTickTime(150,()->{
            h.assertTrue(p.getTarget()==null,"Target beyond follow range released");evidence("P05_distance",p,"target=null");
            p.discard();fixture.close();h.succeed();
        });
    }
    @GameTest(structure=ARENA,maxTicks=240)
    public void shieldPressureNeedsRealApproachAndMeleeHit(GameTestHelper h) {
        floor(h);var p=pillager(h,8,8);p.getInventory().setItem(0,new ItemStack(Items.IRON_AXE));
        var fixture=player(h,8,18);var player=fixture.player();var shield=new ItemStack(Items.SHIELD);
        player.setItemSlot(EquipmentSlot.OFFHAND,shield);
        h.runAtTickTime(5,()->player.getOffhandItem().use(h.getLevel(),player,InteractionHand.OFF_HAND));
        boolean[] broken={false};double[] nearest={100};
        h.onEachTick(()->{
            if(p.isRemoved())return;
            nearest[0]=Math.min(nearest[0],p.distanceTo(player));
            player.setYRot(180);player.setXRot(0);
            if(player.getCooldowns().isOnCooldown(shield))broken[0]=true;
        });
        h.runAtTickTime(210,()->{
            evidence("P04_observation",p,"minDistance="+nearest[0]+" useTicks="+player.getTicksUsingItem()+" blocking="+player.isBlocking()+" health="+player.getHealth()+" charged="+p.isChargingCrossbow()+" broken="+broken[0]);
            h.assertTrue(nearest[0]<3,"Navigation physically closed the shield distance");
            h.assertTrue(PillagerBehavior.runtime(p).swaps>0,"Owned axe actually selected");
            h.assertTrue(broken[0],"Real axe hit invoked vanilla shield cooldown");
            evidence("P04",p,"minDistance="+nearest[0]+" shieldCooldownObserved=true");p.discard();fixture.close();h.succeed();
        });
    }
    @GameTest(structure=ARENA,maxTicks=110)
    public void deathDuringMealDiscardsFoodButDropsOwnedEquipmentOnce(GameTestHelper h) {
        floor(h);var p=pillager(h,8,8);p.setHealth(15);p.getInventory().setItem(0,new ItemStack(Items.BREAD,3));
        p.setItemSlot(EquipmentSlot.OFFHAND,new ItemStack(Items.SHIELD));
        p.setDropChance(EquipmentSlot.MAINHAND,2);p.setDropChance(EquipmentSlot.OFFHAND,2);
        h.runAtTickTime(75,()->{
            h.assertTrue(p.isUsingItem(),"Death arranged during real use");
            p.hurtServer(h.getLevel(),p.damageSources().generic(),1000);
        });
        h.runAtTickTime(85,()->{
            int bread=0,bow=0,shield=0;
            for(var item:h.getLevel().getEntitiesOfClass(ItemEntity.class,p.getBoundingBox().inflate(8))) {
                var stack=item.getItem();if(stack.is(Items.BREAD))bread+=stack.getCount();if(stack.is(Items.CROSSBOW))bow+=stack.getCount();if(stack.is(Items.SHIELD))shield+=stack.getCount();
            }
            evidence("P03_death_observation",p,"bread="+bread+" bow="+bow+" shield="+shield);
            h.assertTrue(bread==0&&bow==1&&shield==1,"Death discards AI food but drops owned equipment once, including reserved hand");
            evidence("P03_death",p,"bread="+bread+" bow="+bow+" shield="+shield);h.succeed();
        });
    }
    @GameTest(structure=ARENA,maxTicks=240)
    public void naturalTargetSwapDamageReturnAndShoot(GameTestHelper h) {
        floor(h); var p=pillager(h,8,8); var t=target(h,8,10);
        p.getMainHandItem().setDamageValue(17); p.getMainHandItem().set(DataComponents.CUSTOM_NAME,Component.literal("owned crossbow"));
        p.getInventory().setItem(0,new ItemStack(Items.STONE_AXE));
        p.setDropChance(EquipmentSlot.MAINHAND,.73f); var arrows=observeArrows(h,p);
        h.runAtTickTime(65,()->{
            h.assertTrue(p.getTarget()==t,"Original target selector acquired iron golem");
            h.assertTrue(t.getHealth()<t.getMaxHealth(),"Real melee hurt target");
            h.assertTrue(p.getMainHandItem().is(Items.STONE_AXE),"Inventory axe moved to main hand");
            h.assertValueEqual(count(p,Items.CROSSBOW),1,"One owned crossbow");
            h.assertValueEqual(count(p,Items.STONE_AXE),1,"One owned axe");
            evidence("P01_melee",p,"targetHealth="+t.getHealth());
            t.snapTo(h.absolutePos(new BlockPos(8,2,20)),0,0);
        });
        h.runAtTickTime(210,()->{
            h.assertTrue(p.getMainHandItem().is(Items.CROSSBOW),"Returned to original ranged weapon");
            h.assertTrue(p.getMainHandItem().getHoverName().getString().equals("owned crossbow"),"Components retained");
            h.assertValueEqual(p.getDropChances().byEquipment(EquipmentSlot.MAINHAND),.73f,"Drop policy retained");
            h.assertTrue(arrows[0],"Vanilla crossbow Goal actually fired after handoff");
            h.assertValueEqual(count(p,Items.CROSSBOW),1,"No duplicate weapon after round trip");
            evidence("P01_ranged_return",p,"arrows_observed=true"); p.discard();t.discard();h.succeed();
        });
    }
    @GameTest(structure=ARENA,maxTicks=155)
    public void retreatWhileActuallyChargingAndShooting(GameTestHelper h) {
        floor(h);var p=pillager(h,10,8);var t=target(h,10,13);double start=p.distanceTo(t);
        var arrows=observeArrows(h,p); boolean[] charged={false}; h.onEachTick(()->{if(p.isChargingCrossbow())charged[0]=true;});
        h.runAtTickTime(140,()->{
            h.assertTrue(charged[0],"Actual charge animation state observed");
            h.assertTrue(PillagerBehavior.runtime(p).retreats>0 && p.distanceTo(t)>start+.3,"Goal-driven physical retreat");
            h.assertTrue(arrows[0],"Actual projectile while retreating");
            evidence("P02",p,"distance="+p.distanceTo(t)+" initial="+start);p.discard();t.discard();h.succeed();
        });
    }
    @GameTest(structure=ARENA,maxTicks=135)
    public void foodConsumesOnceHealsNutritionAndRestoresOccupiedHand(GameTestHelper h) {
        floor(h);var p=pillager(h,8,8);p.setHealth(20);
        p.getInventory().setItem(0,new ItemStack(Items.BREAD,2));
        var shield=new ItemStack(Items.SHIELD);shield.setDamageValue(11);p.setItemSlot(EquipmentSlot.OFFHAND,shield);
        h.runAtTickTime(45,()->h.assertValueEqual(p.getHealth(),20f,"No healing before off-combat wait and consumption"));
        h.runAtTickTime(75,()->h.assertTrue(p.isUsingItem() && p.getOffhandItem().is(Items.BREAD),"Visible real offhand consumption"));
        h.runAtTickTime(120,()->{
            h.assertValueEqual(p.getHealth(),24f,"Nutrition 5 capped at max health");
            h.assertValueEqual(count(p,Items.BREAD),1,"Exactly one real food consumed");
            h.assertTrue(p.getOffhandItem()==shield && shield.getDamageValue()==11,"Original occupied hand restored");
            h.assertValueEqual(PillagerBehavior.runtime(p).meals,1,"One completion");
            evidence("P03_consume",p,"bread=1 health="+p.getHealth());p.discard();h.succeed();
        });
    }
    @GameTest(structure=ARENA,maxTicks=150)
    public void foodReloadAndDisableConserveBothSlots(GameTestHelper h) {
        floor(h);var holder=new Pillager[]{pillager(h,8,8)};var p=holder[0];p.setHealth(15);
        p.getInventory().setItem(0,new ItemStack(Items.BREAD,3));p.setItemSlot(EquipmentSlot.OFFHAND,new ItemStack(Items.SHIELD));
        h.runAtTickTime(75,()->{
            h.assertTrue(p.isUsingItem(),"In-flight meal before unload");
            holder[0]=CombatTestWorld.reload(h,p);
            h.assertTrue(holder[0].getOffhandItem().is(Items.SHIELD),"Load recovers fixed original hand");
            h.assertValueEqual(count(holder[0],Items.BREAD),3,"Reload did not consume or duplicate");
            holder[0].addTag(PillagerBehavior.VANILLA_TAG);
        });
        h.runAtTickTime(135,()->{
            var loaded=holder[0];h.assertValueEqual(loaded.getHealth(),15f,"Disabled eating never resumed");
            h.assertValueEqual(count(loaded,Items.BREAD),3,"All food conserved after disabled ticks");
            evidence("P03_reload_disable",loaded,"bread=3 health=15");loaded.discard();h.succeed();
        });
    }
    @GameTest(structure=ARENA,maxTicks=240)
    public void obstructionExpiresTargetAndReacquiresNaturally(GameTestHelper h) {
        floor(h);var p=pillager(h,8,8);var t=target(h,8,18);
        h.runAtTickTime(65,()->{
            h.assertTrue(p.getTarget()==t,"Acquired target naturally");
            // Box target completely, preventing both LOS and a path around a thin wall.
            for(int x=6;x<=10;x++)for(int z=16;z<=20;z++)for(int y=2;y<=6;y++)
                if(x==6||x==10||z==16||z==20||y==6) h.setBlock(new BlockPos(x,y,z),Blocks.STONE);
        });
        h.runAtTickTime(140,()->{
            h.assertTrue(p.getTarget()==null&&!p.isUsingItem()&&!p.isChargingCrossbow(),"Occlusion ended combat and charge");
            evidence("P05_obstruction",p,"target=null");
            for(int x=6;x<=10;x++)for(int z=16;z<=20;z++)for(int y=2;y<=6;y++)
                if(x==6||x==10||z==16||z==20||y==6) h.setBlock(new BlockPos(x,y,z),Blocks.AIR);
        });
        h.runAtTickTime(220,()->{h.assertTrue(p.getTarget()==t,"Reacquired through original target selector within 80 ticks");p.discard();t.discard();h.succeed();});
    }
    @GameTest(structure=ARENA,maxTicks=130)
    public void optOutKeepsVanillaRangedAndNoOtherMobGoals(GameTestHelper h) {
        floor(h);var p=pillager(h,8,8);p.addTag(PillagerBehavior.VANILLA_TAG);var t=target(h,8,14);
        p.getInventory().setItem(0,new ItemStack(Items.STONE_AXE));p.getInventory().setItem(1,new ItemStack(Items.BREAD,2));p.setHealth(20);
        var arrows=observeArrows(h,p); var zombie=EntityTypes.ZOMBIE.spawn(h.getLevel(),h.absolutePos(new BlockPos(20,2,20)),EntitySpawnReason.COMMAND);
        h.runAtTickTime(115,()->{
            var rt=PillagerBehavior.runtime(p);
            h.assertTrue(rt.swaps==0&&rt.retreats==0&&rt.meals==0,"All rebuilt actions disabled");
            h.assertTrue(arrows[0],"Original ranged attack still runs");
            h.assertTrue(zombie.getGoalSelector().getAvailableGoals().stream().noneMatch(g->g.getGoal().getClass().getName().startsWith("com.davidblackcn.buildupmobtweaks.combat.")),"No legacy zombie behavior registered");
            evidence("disabled_vanilla",p,"arrow=true legacyZombieGoals=false");p.discard();t.discard();zombie.discard();h.succeed();
        });
    }
    @GameTest(structure=ARENA,maxTicks=155)
    public void compatibleCrossbowFiresFromActualHand(GameTestHelper h) {
        floor(h);var p=pillager(h,8,8);p.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(CombatTestItems.CROSSBOW));var t=target(h,8,18); var arrows=observeArrows(h,p);
        h.runAtTickTime(140,()->{
            h.assertTrue(arrows[0],"Subclass crossbow really loaded and fired");
            evidence("compat_crossbow",p,"arrow=true");p.discard();t.discard();h.succeed();
        });
    }
    @GameTest(structure=ARENA,maxTicks=540)
    public void liveGlobalAndIndependentDisableReturnToVanilla(GameTestHelper h) {
        floor(h);
        // Other scenarios finish by tick 245; global configuration changes are deliberately later.
        Pillager[] refs=new Pillager[2]; IronGolem[] targets=new IronGolem[1];
        h.runAtTickTime(300,()->{
            var p=pillager(h,8,8);refs[0]=p;p.setHealth(15);
            p.setItemSlot(EquipmentSlot.OFFHAND,new ItemStack(Items.SHIELD));
            p.getInventory().setItem(0,new ItemStack(Items.BREAD,3));
        });
        h.runAtTickTime(375,()->{
            h.assertTrue(refs[0].isUsingItem(),"Real meal active before global disable");
            BuildupMobTweaks.config().general.enabled.accept(false);
        });
        h.runAtTickTime(390,()->{
            var p=refs[0];h.assertTrue(!p.isUsingItem()&&p.getOffhandItem().is(Items.SHIELD),"Master disable restores borrowed hand");
            h.assertValueEqual(count(p,Items.BREAD),3,"Master disable preserves unconsumed food");
            h.assertValueEqual(p.getHealth(),15f,"No disabled heal");
            evidence("global_disable_meal",p,"food=3 health=15");p.discard();
            BuildupMobTweaks.config().general.enabled.accept(true);
            var melee=pillager(h,8,8);refs[1]=melee;melee.getInventory().setItem(0,new ItemStack(Items.STONE_AXE));
            targets[0]=target(h,8,10);
        });
        h.runAtTickTime(440,()->{
            h.assertTrue(refs[1].getMainHandItem().is(Items.STONE_AXE),"Real melee active before independent disable");
            BuildupMobTweaks.config().hostile.raid.pillagerWeaponSwitch.accept(false);
        });
        h.runAtTickTime(460,()->{
            var p=refs[1];h.assertTrue(p.getMainHandItem().is(Items.CROSSBOW),"Independent switch disable returns owned crossbow");
            h.assertValueEqual(count(p,Items.STONE_AXE),1,"Owned axe remains in inventory");
            h.assertTrue(p.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE).getModifier(BuildupMobTweaks.id("pillager_melee_balance"))==null,"Temporary melee balance removed on stop");
            evidence("independent_disable_switch",p,"crossbow=1 axe=1");
            p.discard();targets[0].discard();BuildupMobTweaks.config().hostile.raid.pillagerWeaponSwitch.accept(true);h.succeed();
        });
    }
    @GameTest(structure=ARENA,maxTicks=40)
    public void defaultBirthSuppliesAreOneAttemptAndReloadDoesNotRefill(GameTestHelper h) {
        floor(h);var p=EntityTypes.PILLAGER.spawn(h.getLevel(),h.absolutePos(new BlockPos(8,2,8)),EntitySpawnReason.SPAWN_ITEM_USE);
        h.runAtTickTime(10,()->{
            h.assertTrue(p.getMainHandItem().is(Items.CROSSBOW),"Default spawn retains real vanilla crossbow");
            h.assertValueEqual(count(p,Items.STONE_AXE),1,"Default enabled supply is in actual inventory");
            for(int i=0;i<p.getInventory().getContainerSize();i++)p.getInventory().setItem(i,ItemStack.EMPTY);
            var loaded=CombatTestWorld.reload(h,p);
            h.assertValueEqual(count(loaded,Items.STONE_AXE),0,"Lost/removed supply is never refilled on reload");
            evidence("default_birth_reload",loaded,"axe_before=1 axe_after_removal_reload=0");loaded.discard();h.succeed();
        });
    }
    @GameTest(structure=ARENA,maxTicks=40)
    public void loadedDisabledPillagerDoesNotDropMultipleBackpackFoods(GameTestHelper h) {
        floor(h);var p=pillager(h,8,8);
        Item[] foods={Items.BREAD,Items.APPLE,Items.CARROT,Items.GOLDEN_APPLE,Items.GOLDEN_CARROT};
        for(int i=0;i<foods.length;i++) {
            p.getInventory().setItem(i,new ItemStack(foods[i],3));
            // Legacy R2-A birth supplies were persisted with a guaranteed drop policy.
            p.getAttached(PillagerBehavior.DATA).putFloat("drop_"+i,1f);
        }
        p.setDropChance(EquipmentSlot.MAINHAND,2);
        var loaded=CombatTestWorld.reload(h,p);loaded.addTag(PillagerBehavior.VANILLA_TAG);
        h.runAtTickTime(10,()->{
            for(Item item:foods)h.assertValueEqual(count(loaded,item),3,"Reload keeps living inventory for use");
            loaded.hurtServer(h.getLevel(),loaded.damageSources().generic(),1000);
        });
        h.runAtTickTime(20,()->{
            int foodDrops=0,bows=0;
            for(var drop:h.getLevel().getEntitiesOfClass(ItemEntity.class,loaded.getBoundingBox().inflate(8))) {
                var stack=drop.getItem();if(stack.has(DataComponents.FOOD))foodDrops+=stack.getCount();
                if(stack.is(Items.CROSSBOW))bows+=stack.getCount();
            }
            h.assertValueEqual(foodDrops,0,"No backpack food loot, including saved golden foods with AI disabled");
            h.assertValueEqual(bows,1,"Normal weapon drop preserved");
            h.assertTrue(loaded.getInventory().isEmpty(),"Death clears supplies once");
            evidence("P03_loaded_food_death",loaded,"food=0 crossbow=1 inventory_empty=true");h.succeed();
        });
    }

    @GameTest(structure=ARENA,maxTicks=145)
    public void acquiringTargetInterruptsRealFoodUseWithoutHealing(GameTestHelper h) {
        floor(h);var p=pillager(h,8,8);p.setHealth(15);
        p.getInventory().setItem(0,new ItemStack(Items.BREAD,3));
        p.setItemSlot(EquipmentSlot.OFFHAND,new ItemStack(Items.SHIELD));
        IronGolem[] victim=new IronGolem[1];
        h.runAtTickTime(75,()->{
            h.assertTrue(p.isUsingItem(),"Real meal started");victim[0]=target(h,8,18);
            // A real damage event gives vanilla HurtByTargetGoal a deterministic reason to interrupt.
            // A newly visible target alone has a random scan delay and may be found after the meal ends.
            p.hurtServer(h.getLevel(),p.damageSources().mobAttack(victim[0]),1);
        });
        h.runAtTickTime(130,()->{
            h.assertTrue(p.getTarget()==victim[0],"Original target selector acquired combat target");
            h.assertTrue(p.getOffhandItem().is(Items.SHIELD),"Combat restored original offhand");
            h.assertValueEqual(p.getHealth(),14f,"Interrupted meal never healed after one damage");
            h.assertValueEqual(count(p,Items.BREAD),3,"Interrupted meal conserved food");
            evidence("P03_target_interrupt",p,"health=14 bread=3 shield_restored=true");p.discard();victim[0].discard();h.succeed();
        });
    }

    @GameTest(structure=ARENA,maxTicks=135)
    public void foodContainerRemainderSurvivesFullInventory(GameTestHelper h) {
        floor(h);var p=pillager(h,8,8);p.setHealth(20);
        p.getInventory().setItem(0,new ItemStack(Items.MUSHROOM_STEW));
        for(int i=1;i<p.getInventory().getContainerSize();i++)p.getInventory().setItem(i,new ItemStack(Items.STONE,64));
        p.setItemSlot(EquipmentSlot.OFFHAND,new ItemStack(Items.SHIELD));
        h.runAtTickTime(120,()->{
            h.assertValueEqual(p.getHealth(),24f,"Stew nutrition healed after real use");
            h.assertValueEqual(count(p,Items.BOWL),1,"Container remainder retained in reserved slot");
            h.assertValueEqual(count(p,Items.MUSHROOM_STEW),0,"Actual stew consumed once");
            h.assertValueEqual(count(p,Items.STONE),256,"Full inventory contents conserved");
            h.assertTrue(p.getOffhandItem().is(Items.SHIELD),"Original hand restored");
            evidence("P03_container_full_inventory",p,"bowl=1 stone=256 shield=1");p.discard();h.succeed();
        });
    }

    @GameTest(structure=ARENA,maxTicks=100)
    public void sparseInventoryReloadRestoresOriginalMealSlotAndSeparateStacks(GameTestHelper h) {
        floor(h);var p=pillager(h,8,8);p.setHealth(15);
        p.getInventory().setItem(1,new ItemStack(Items.STONE,2));
        p.getInventory().setItem(3,new ItemStack(Items.BREAD,3));
        p.getInventory().setItem(4,new ItemStack(Items.STONE,4));
        p.setItemSlot(EquipmentSlot.OFFHAND,new ItemStack(Items.SHIELD));p.setDropChance(EquipmentSlot.OFFHAND,.73f);
        h.runAtTickTime(75,()->{
            h.assertTrue(p.isUsingItem(),"Meal uses sparse inventory slot 3");
            var loaded=CombatTestWorld.reload(h,p);
            h.assertTrue(loaded.getOffhandItem().is(Items.SHIELD),"Original shield restored from slot 3, not compacted slot");
            h.assertValueEqual(loaded.getDropChances().byEquipment(EquipmentSlot.OFFHAND),.73f,"Shield drop policy restored");
            h.assertTrue(loaded.getInventory().getItem(0).isEmpty()&&loaded.getInventory().getItem(2).isEmpty(),"Empty slots remain empty");
            h.assertValueEqual(loaded.getInventory().getItem(1).getCount(),2,"First equal-item stack stays separate");
            h.assertValueEqual(loaded.getInventory().getItem(4).getCount(),4,"Second equal-item stack stays separate");
            h.assertTrue(loaded.getInventory().getItem(3).is(Items.BREAD),"Food returns to original slot");
            h.assertValueEqual(count(loaded,Items.BREAD),3,"No consumption or duplication during reload");
            evidence("P03_sparse_reload",loaded,"bread=3 stone_slots=2,4 shield_drop=.73");loaded.discard();h.succeed();
        });
    }

}
