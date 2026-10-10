package com.davidblackcn.buildupmobtweaks;

import com.davidblackcn.buildupmobtweaks.equipment.*;
import com.davidblackcn.buildupmobtweaks.feature.*;
import com.davidblackcn.buildupmobtweaks.qa.QaHarness;
import com.davidblackcn.buildupmobtweaks.rebuild.skeleton.ExtendedSkeletonState;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import java.util.*;

/** Actual spawn/finalize, registered resource store, Server Tick and native GoalSelector. */
public class BirthEquipmentTests {
    static final String ARENA="buildupmobtweaks:r2a_arena", ENV="buildupmobtweaks:r4e2a";
    static void proof(String name,Mob m){BuildupMobTweaks.LOGGER.info("R4E2A_PROOF scenario={} ticks={} main={} state={}",name,m.tickCount,m.getMainHandItem(),m.getAttached(EquipmentService.DATA));}
    static <T extends Mob> T raw(GameTestHelper h,EntityType<T> type,EntitySpawnReason reason){
        var m=type.spawn(h.getLevel(),h.absolutePos(new BlockPos(8,2,8)),reason);m.setPersistenceRequired();return m;
    }
    static <T extends Mob> T born(GameTestHelper h,EntityType<T> type,EntitySpawnReason reason){
        var c=BuildupMobTweaks.config().equipment;int w=c.witherBowChance.get(),z=c.zombifiedCrossbowChance.get(),a=c.assignmentChance.get();
        try{c.witherBowChance.accept(1000);c.zombifiedCrossbowChance.accept(1000);c.assignmentChance.accept(1000);return raw(h,type,reason);}
        finally{c.witherBowChance.accept(w);c.zombifiedCrossbowChance.accept(z);c.assignmentChance.accept(a);}
    }
    static void floor(GameTestHelper h){SkeletonBehaviorTests.floor(h,true);}
    static void delayed(GameTestHelper h,Mob m,Item item,String label){
        h.assertTrue(m.getMainHandItem().is(item)&&m.getMainHandItem().getCount()==1,"Actual birth already owns one item before ticks");
        // Distant GameTest arenas must not let native distance despawn end a retention fixture.
        m.setPersistenceRequired();
        h.runAtTickTime(25,()->{
        proof(label,m);BuildupMobTweaks.LOGGER.info("R4E2A_RETENTION scenario={} alive={} removed={} persistent={}",label,m.isAlive(),m.isRemoved(),m.isPersistenceRequired());
        h.assertTrue(m.isAlive()&&!m.isRemoved()&&m.tickCount>=20&&m.getMainHandItem().is(item)&&m.getMainHandItem().getCount()==1,"Native ticks retain one birth item");m.discard();h.succeed();});}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=40)
    public void actualResourceStoreContainsThreeValidPools(GameTestHelper h){
        var pools=EquipmentPools.get(h.getLevel().getServer());h.assertValueEqual(pools.rejected(),0,"Built-in strict pools valid");
        h.assertTrue(pools.rules().stream().map(p->p.id().getPath()).toList().containsAll(List.of("wither_birth","zombified_birth","zombie_demo")),"Real loaded rules, not supplied test snapshot");
        h.runAtTickTime(5,()->h.succeed());
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=210)
    public void nativeWitherBirthBowActuallyFiresAndHurts(GameTestHelper h){
        floor(h);var m=born(h,EntityTypes.WITHER_SKELETON,EntitySpawnReason.SPAWN_ITEM_USE);m.addTag("buildupmobtweaks:disable_wither_skeleton_skull");
        m.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);var t=SkeletonBehaviorTests.target(h,8,18);t.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);t.setHealth(1000);m.setTarget(t);
        h.runAtTickTime(190,()->{h.assertTrue(ExtendedSkeletonState.runtime(m).shots>0&&t.getHealth()<1000&&m.getMainHandItem().is(Items.BOW)&&m.getMainHandItem().getDamageValue()>0,"Born real bow used through native Goal, actual arrow hits and durability");proof("born_bow_native_goal",m);m.discard();t.discard();h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=40) public void zombifiedActualEggBirthCrossbow(GameTestHelper h){floor(h);delayed(h,born(h,EntityTypes.ZOMBIFIED_PIGLIN,EntitySpawnReason.SPAWN_ITEM_USE),Items.CROSSBOW,"born_zp");}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=40)
    public void actualNativeBabyZombifiedBirthHasOneCrossbowWithoutExtraJockey(GameTestHelper h){
        floor(h);var m=EntityTypes.ZOMBIFIED_PIGLIN.create(h.getLevel(),EntitySpawnReason.SPAWN_ITEM_USE);m.snapTo(h.absolutePos(new BlockPos(8,2,8)),0,0);
        var c=BuildupMobTweaks.config().equipment;int old=c.zombifiedCrossbowChance.get();
        try{c.zombifiedCrossbowChance.accept(1000);m.finalizeSpawn(h.getLevel(),h.getLevel().getCurrentDifficultyAt(m.blockPosition()),EntitySpawnReason.SPAWN_ITEM_USE,new net.minecraft.world.entity.monster.zombie.Zombie.ZombieGroupData(true,false));h.getLevel().addFreshEntity(m);}finally{c.zombifiedCrossbowChance.accept(old);}
        h.assertTrue(m.isBaby()&&!m.isPassenger(),"Native baby group, no extra jockey fixture");delayed(h,m,Items.CROSSBOW,"native_baby_zp");
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=40) public void witherSpawnerReasonUsesSameBirthPipeline(GameTestHelper h){floor(h);delayed(h,born(h,EntityTypes.WITHER_SKELETON,EntitySpawnReason.SPAWNER),Items.BOW,"spawner_reason");}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=40) public void witherNaturalReasonUsesSameBirthPipeline(GameTestHelper h){floor(h);delayed(h,born(h,EntityTypes.WITHER_SKELETON,EntitySpawnReason.NATURAL),Items.BOW,"natural_reason");}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=40)
    public void registeredGeneralPoolEquipsActualEmptyZombie(GameTestHelper h){
        floor(h);var m=EntityTypes.ZOMBIE.create(h.getLevel(),EntitySpawnReason.COMMAND);m.snapTo(h.absolutePos(new BlockPos(8,2,8)),0,0);
        var c=BuildupMobTweaks.config().equipment;int a=c.assignmentChance.get();try{c.assignmentChance.accept(1000);h.getLevel().addFreshEntity(m);}finally{c.assignmentChance.accept(a);}
        delayed(h,m,Items.WOODEN_SWORD,"registered_empty_zombie");
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=140)
    public void registeredPoolWeaponParticipatesInNativeMelee(GameTestHelper h){
        floor(h);var m=EntityTypes.ZOMBIE.create(h.getLevel(),EntitySpawnReason.COMMAND);m.snapTo(h.absolutePos(new BlockPos(8,2,8)),0,0);m.setPersistenceRequired();
        var c=BuildupMobTweaks.config().equipment;int a=c.assignmentChance.get();try{c.assignmentChance.accept(1000);h.getLevel().addFreshEntity(m);}finally{c.assignmentChance.accept(a);}
        var t=SkeletonBehaviorTests.target(h,8,10);t.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);t.setHealth(1000);m.setTarget(t);
        h.runAtTickTime(120,()->{proof("pool_native_melee_probe",m);BuildupMobTweaks.LOGGER.info("R4E2A_MELEE targetHP={} alive={} distance={} goal={}",t.getHealth(),m.isAlive(),m.distanceTo(t),m.getGoalSelector().getAvailableGoals());h.assertTrue(m.isAlive()&&m.getMainHandItem().is(Items.WOODEN_SWORD)&&t.getHealth()<1000,"Pool item is in native equipment and original melee Goal approaches and hurts a real target");proof("pool_native_melee",m);m.discard();t.discard();h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=40)
    public void entityExitTagsAndTraitIndependenceUseRealBirth(GameTestHelper h){
        floor(h);var a=EntityTypes.ZOMBIE.create(h.getLevel(),EntitySpawnReason.COMMAND);a.snapTo(h.absolutePos(new BlockPos(8,2,8)),0,0);a.addTag("buildupmobtweaks:disable_equipment_assignment");h.getLevel().addFreshEntity(a);
        var c=BuildupMobTweaks.config();boolean traits=c.traits.enabled.get();Mob b;try{c.traits.enabled.accept(false);b=born(h,EntityTypes.WITHER_SKELETON,EntitySpawnReason.SPAWN_ITEM_USE);}finally{c.traits.enabled.accept(traits);}
        h.runAtTickTime(25,()->{h.assertTrue(a.getMainHandItem().isEmpty()&&a.getAttached(EquipmentService.DATA).getStringOr("outcome","").equals("disabled")&&b.getMainHandItem().is(Items.BOW),"Entity opt out and independent equipment birth without Traits/Marker lottery");proof("traits_independent",b);a.discard();b.discard();h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=40)
    public void existingComponentsAndDropRateSurviveNativeInitialization(GameTestHelper h){
        floor(h);var m=EntityTypes.WITHER_SKELETON.create(h.getLevel(),EntitySpawnReason.COMMAND);m.snapTo(h.absolutePos(new BlockPos(8,2,8)),0,0);
        var s=new ItemStack(Items.DIAMOND_SWORD);s.setDamageValue(23);s.set(DataComponents.CUSTOM_NAME,Component.literal("external sword"));m.setItemSlot(EquipmentSlot.MAINHAND,s);m.setDropChance(EquipmentSlot.MAINHAND,.42f);var expected=s.copy();
        m.finalizeSpawn(h.getLevel(),h.getLevel().getCurrentDifficultyAt(m.blockPosition()),EntitySpawnReason.COMMAND,null);h.getLevel().addFreshEntity(m);
        h.runAtTickTime(25,()->{h.assertTrue(ItemStack.matches(m.getMainHandItem(),expected)&&m.getDropChances().byEquipment(EquipmentSlot.MAINHAND)==.42f,"Occupied real components/drop chance protected");proof("occupied_native",m);m.discard();h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=50)
    public void saveReloadEmptyHandDoesNotRefillOrRepeatNativeBirth(GameTestHelper h){
        floor(h);var m=born(h,EntityTypes.WITHER_SKELETON,EntitySpawnReason.SPAWN_ITEM_USE);var d=m.getAttached(EquipmentService.DATA).copy();var uuid=m.getUUID();
        m.getMainHandItem().setDamageValue(19);m.getMainHandItem().set(DataComponents.CUSTOM_NAME,Component.literal("earned bow"));var expected=m.getMainHandItem().copy();var loaded=CombatTestWorld.reload(h,m);
        h.runAtTickTime(15,()->{h.assertTrue(loaded.getUUID().equals(uuid)&&ItemStack.matches(loaded.getMainHandItem(),expected)&&loaded.getAttached(EquipmentService.DATA).equals(d),"Actual save/load preserves item and ledger");loaded.setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);
            loaded.finalizeSpawn(h.getLevel(),h.getLevel().getCurrentDifficultyAt(loaded.blockPosition()),EntitySpawnReason.SPAWN_ITEM_USE,null);});
        h.runAtTickTime(35,()->{h.assertTrue(loaded.getMainHandItem().isEmpty()&&loaded.getAttached(EquipmentService.DATA).equals(d),"Repeated native finalize/load does not refund a weapon");proof("reload_no_refill",loaded);loaded.discard();h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=40)
    public void unknownVersionSurvivesRealLoadWithoutAssignment(GameTestHelper h){
        floor(h);var m=EntityTypes.ZOMBIE.create(h.getLevel(),EntitySpawnReason.COMMAND);m.snapTo(h.absolutePos(new BlockPos(8,2,8)),0,0);var d=new CompoundTag();d.putInt("version",99);d.putString("future","keep");m.setAttached(EquipmentService.DATA,d);h.getLevel().addFreshEntity(m);var l=CombatTestWorld.reload(h,m);
        h.runAtTickTime(25,()->{h.assertTrue(l.getMainHandItem().isEmpty()&&l.getAttached(EquipmentService.DATA).equals(d),"Unknown payload untouched");proof("unknown99",l);l.discard();h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=40)
    public void loadedLegacyEntityWithoutLedgerIsNeverBackfilled(GameTestHelper h){
        floor(h);var m=EntityTypes.ZOMBIE.create(h.getLevel(),EntitySpawnReason.LOAD);m.snapTo(h.absolutePos(new BlockPos(8,2,8)),0,0);h.getLevel().addFreshEntity(m);delayedEmpty(h,m,"skipped_source");
    }
    static void delayedEmpty(GameTestHelper h,Mob m,String outcome){h.runAtTickTime(25,()->{h.assertTrue(m.getMainHandItem().isEmpty()&&m.getAttached(EquipmentService.DATA).getStringOr("outcome","").equals(outcome),"No backfill");proof(outcome,m);m.discard();h.succeed();});}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=40)
    public void conversionAndDimensionTravelNeverDrawEquipment(GameTestHelper h){
        floor(h);var a=EntityTypes.ZOMBIE.create(h.getLevel(),EntitySpawnReason.CONVERSION);var b=EntityTypes.ZOMBIE.create(h.getLevel(),EntitySpawnReason.DIMENSION_TRAVEL);
        for(var m:List.of(a,b)){m.snapTo(h.absolutePos(new BlockPos(8,2,8)),0,0);h.getLevel().addFreshEntity(m);}
        h.runAtTickTime(25,()->{for(var m:List.of(a,b)){h.assertTrue(m.getMainHandItem().isEmpty()&&m.getAttached(EquipmentService.DATA).getStringOr("outcome","").equals("skipped_source"),"Source filtered");m.discard();}h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=40)
    public void individualAndMasterGatesKeepNativeWeaponsAndFinalAttempts(GameTestHelper h){
        floor(h);var c=BuildupMobTweaks.config();var mobs=new ArrayList<Mob>();
        boolean master=c.general.enabled.get(),pool=c.equipment.poolAssignment.get(),w=c.hostile.extended.wither_skeleton_bow.get(),z=c.hostile.extended.zombified_piglin_spawn_crossbow.get();
        try{c.general.enabled.accept(false);mobs.add(raw(h,EntityTypes.WITHER_SKELETON,EntitySpawnReason.SPAWN_ITEM_USE));c.general.enabled.accept(true);
            c.equipment.poolAssignment.accept(false);mobs.add(raw(h,EntityTypes.WITHER_SKELETON,EntitySpawnReason.SPAWN_ITEM_USE));c.equipment.poolAssignment.accept(true);
            c.hostile.extended.wither_skeleton_bow.accept(false);mobs.add(raw(h,EntityTypes.WITHER_SKELETON,EntitySpawnReason.SPAWN_ITEM_USE));c.hostile.extended.wither_skeleton_bow.accept(true);
            c.hostile.extended.zombified_piglin_spawn_crossbow.accept(false);mobs.add(raw(h,EntityTypes.ZOMBIFIED_PIGLIN,EntitySpawnReason.SPAWN_ITEM_USE));
        }finally{c.general.enabled.accept(master);c.equipment.poolAssignment.accept(pool);c.hostile.extended.wither_skeleton_bow.accept(w);c.hostile.extended.zombified_piglin_spawn_crossbow.accept(z);}
        h.runAtTickTime(25,()->{for(var m:mobs){h.assertTrue(!m.getMainHandItem().is(Items.BOW)&&!m.getMainHandItem().is(Items.CROSSBOW)&&m.getAttached(EquipmentService.DATA).getStringOr("outcome","").equals("disabled"),"Native default and final disabled record");proof("gates",m);m.discard();}h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=40)
    public void zeroChancesRetainStoneSwordAndGoldenSwordOrSpear(GameTestHelper h){
        floor(h);var c=BuildupMobTweaks.config().equipment;int w=c.witherBowChance.get(),z=c.zombifiedCrossbowChance.get();Mob a,b;
        try{c.witherBowChance.accept(0);c.zombifiedCrossbowChance.accept(0);a=raw(h,EntityTypes.WITHER_SKELETON,EntitySpawnReason.SPAWN_ITEM_USE);b=raw(h,EntityTypes.ZOMBIFIED_PIGLIN,EntitySpawnReason.SPAWN_ITEM_USE);}finally{c.witherBowChance.accept(w);c.zombifiedCrossbowChance.accept(z);}
        h.runAtTickTime(25,()->{h.assertTrue(a.getMainHandItem().is(Items.STONE_SWORD)&&(b.getMainHandItem().is(Items.GOLDEN_SWORD)||b.getMainHandItem().is(Items.GOLDEN_SPEAR)),"Native weapon fallback");proof("zero_chances",b);a.discard();b.discard();h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=40)
    public void generatedGearUsesOnlyActualNativeDeathDrop(GameTestHelper h){
        floor(h);var a=born(h,EntityTypes.WITHER_SKELETON,EntitySpawnReason.SPAWN_ITEM_USE);var b=born(h,EntityTypes.ZOMBIFIED_PIGLIN,EntitySpawnReason.SPAWN_ITEM_USE);
        a.snapTo(h.absolutePos(new BlockPos(5,2,8)),0,0);b.snapTo(h.absolutePos(new BlockPos(12,2,8)),0,0);
        for(var m:List.of(a,b)){m.getMainHandItem().setDamageValue(17);m.getMainHandItem().set(DataComponents.CUSTOM_NAME,Component.literal("E2a owned "+m.getUUID()));m.setDropChance(EquipmentSlot.MAINHAND,2);m.hurtServer(h.getLevel(),m.damageSources().genericKill(),10000);}
        h.runAtTickTime(25,()->{for(var m:List.of(a,b)){var drops=h.getLevel().getEntitiesOfClass(ItemEntity.class,m.getBoundingBox().inflate(64),e->e.getItem().getHoverName().getString().equals("E2a owned "+m.getUUID()));h.assertTrue(drops.size()==1&&drops.getFirst().getItem().getCount()==1&&drops.getFirst().getItem().getDamageValue()==17,"One existing damaged named item, no supplemental drop");for(var e:drops)e.discard();proof("native_death",m);m.discard();}h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=40)
    public void zeroDropChanceProducesNoExtraEquipment(GameTestHelper h){
        floor(h);var m=born(h,EntityTypes.WITHER_SKELETON,EntitySpawnReason.SPAWN_ITEM_USE);m.getMainHandItem().set(DataComponents.CUSTOM_NAME,Component.literal("E2a zero "+m.getUUID()));m.setDropChance(EquipmentSlot.MAINHAND,0);m.hurtServer(h.getLevel(),m.damageSources().genericKill(),10000);
        h.runAtTickTime(25,()->{h.assertTrue(h.getLevel().getEntitiesOfClass(ItemEntity.class,m.getBoundingBox().inflate(64),e->e.getItem().getHoverName().getString().equals("E2a zero "+m.getUUID())).isEmpty(),"No custom death copy bypassing native zero drop");proof("zero_drop",m);m.discard();h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=40)
    public void actualNativeConversionPreservesEarnedLedgerAndWeapon(GameTestHelper h){
        floor(h);var m=EntityTypes.ZOMBIE.create(h.getLevel(),EntitySpawnReason.COMMAND);m.snapTo(h.absolutePos(new BlockPos(8,2,8)),0,0);h.getLevel().addFreshEntity(m);
        var d=m.getAttached(EquipmentService.DATA).copy();var s=new ItemStack(Items.DIAMOND_SWORD);s.setDamageValue(18);s.set(DataComponents.CUSTOM_NAME,Component.literal("converted external"));m.setItemSlot(EquipmentSlot.MAINHAND,s);m.setDropChance(EquipmentSlot.MAINHAND,.42f);var expected=s.copy();
        var converted=m.convertTo(EntityTypes.DROWNED,ConversionParams.single(m,true,true),v->v.setPersistenceRequired());
        h.runAtTickTime(25,()->{h.assertTrue(converted.getAttached(EquipmentService.DATA).equals(d)&&ItemStack.matches(converted.getMainHandItem(),expected)&&converted.getDropChances().byEquipment(EquipmentSlot.MAINHAND)==.42f,"Native conversion carries ledger/components/drop policy without another draw");proof("native_conversion",converted);converted.discard();h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=40)
    public void nativeMobDropsRuleSuppressesGeneratedEquipment(GameTestHelper h){
        floor(h);var m=born(h,EntityTypes.WITHER_SKELETON,EntitySpawnReason.SPAWN_ITEM_USE);m.getMainHandItem().set(DataComponents.CUSTOM_NAME,Component.literal("E2a rule "+m.getUUID()));m.setDropChance(EquipmentSlot.MAINHAND,2);
        var rules=h.getLevel().getGameRules();boolean old=rules.get(net.minecraft.world.level.gamerules.GameRules.MOB_DROPS);
        try{rules.set(net.minecraft.world.level.gamerules.GameRules.MOB_DROPS,false,h.getLevel().getServer());m.hurtServer(h.getLevel(),m.damageSources().genericKill(),10000);}finally{rules.set(net.minecraft.world.level.gamerules.GameRules.MOB_DROPS,old,h.getLevel().getServer());}
        h.runAtTickTime(25,()->{h.assertTrue(h.getLevel().getEntitiesOfClass(ItemEntity.class,m.getBoundingBox().inflate(64),e->e.getItem().getHoverName().getString().equals("E2a rule "+m.getUUID())).isEmpty(),"No custom drop bypassing vanilla mob_drops");proof("mob_drops_false",m);m.discard();h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=40)
    public void absentAdultRuleAttemptIsFinalAndNeverBackfilled(GameTestHelper h){
        floor(h);var m=EntityTypes.ZOMBIE.create(h.getLevel(),EntitySpawnReason.COMMAND);m.snapTo(h.absolutePos(new BlockPos(8,2,8)),0,0);m.setBaby(true);h.getLevel().addFreshEntity(m);var d=m.getAttached(EquipmentService.DATA).copy();m.setBaby(false);var l=CombatTestWorld.reload(h,m);
        h.runAtTickTime(25,()->{h.assertTrue(l.getMainHandItem().isEmpty()&&l.getAttached(EquipmentService.DATA).equals(d)&&d.getStringOr("outcome","").equals("no_rule"),"No matching rule is final even after growth/load");proof("no_rule_final",l);l.discard();h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=45)
    public void defaultRareNativeSampleReportsActualBirthCounts(GameTestHelper h){
        floor(h);var c=BuildupMobTweaks.config().equipment;int w=c.witherBowChance.get(),z=c.zombifiedCrossbowChance.get();int[] counts={0,0},spears={0};
        try{c.witherBowChance.accept(10);c.zombifiedCrossbowChance.accept(10);
            for(int i=0;i<1000;i++)for(int k=0;k<2;k++){
                Mob m=(k==0?EntityTypes.WITHER_SKELETON:EntityTypes.ZOMBIFIED_PIGLIN).create(h.getLevel(),EntitySpawnReason.NATURAL);m.snapTo(h.absolutePos(new BlockPos(8,2,8)),0,0);m.getRandom().setSeed(570000L+i);
                m.finalizeSpawn(h.getLevel(),h.getLevel().getCurrentDifficultyAt(m.blockPosition()),EntitySpawnReason.NATURAL,k==1?new net.minecraft.world.entity.monster.zombie.Zombie.ZombieGroupData(false,false):null);h.getLevel().addFreshEntity(m);
                if(m.getMainHandItem().is(k==0?Items.BOW:Items.CROSSBOW))counts[k]++;if(m.getMainHandItem().is(Items.GOLDEN_SPEAR))spears[0]++;m.discard();
            }
        }finally{c.witherBowChance.accept(w);c.zombifiedCrossbowChance.accept(z);}
        h.assertTrue(counts[0]>=2&&counts[0]<=25&&counts[1]>=2&&counts[1]<=25&&spears[0]>0,"Actual seeded rare births plus preserved native spear branch; not natural spawner distribution");
        BuildupMobTweaks.LOGGER.info("R4E2A_PROOF scenario=default_birth_sample n=1000 bow={} crossbow={} native_spears={} natural_spawner=NOT_RUN",counts[0],counts[1],spears[0]);h.runAtTickTime(5,()->h.succeed());
    }
    @GameTest(environment="buildupmobtweaks:r4e2a_qa",structure=ARENA,maxTicks=320)
    public void independentQaUsesNativeBornItemsAndCleansTerrain(GameTestHelper h){
        var level=h.getLevel();var server=level.getServer();var pos=h.absolutePos(new BlockPos(50,30,50));
        var c=BuildupMobTweaks.config().equipment;int chance=c.witherBowChance.get();boolean rejected=false;
        try{c.witherBowChance.accept(0);try{QaHarness.spawn(level,pos,QaHarness.Scene.WITHER_BIRTH);}catch(IllegalArgumentException expected){rejected=true;}}finally{c.witherBowChance.accept(chance);}
        h.assertTrue(rejected&&level.getBlockState(pos.below()).isAir()&&level.getBlockState(pos.above(4)).isAir(),"Disabled birth fails before any terrain mutation");
        QaHarness.Session[] s={QaHarness.spawn(level,pos,QaHarness.Scene.WITHER_BIRTH)};
        h.runAtTickTime(230,()->{h.assertTrue(s[0].mob.getMainHandItem().is(Items.BOW)&&s[0].mob.getAttached(EquipmentService.DATA).getStringOr("outcome","").equals("equipped")&&ExtendedSkeletonState.runtime((AbstractSkeleton)s[0].mob).shots>0&&s[0].target.getHealth()<1000,"Independent QA observes a genuinely born bow and native shots");QaHarness.cleanup(server);h.assertTrue(level.getBlockState(pos.below()).isAir(),"First terrain restored");s[0]=QaHarness.spawn(level,pos,QaHarness.Scene.ZOMBIFIED_BIRTH);});
        h.runAtTickTime(290,()->{h.assertTrue(s[0].mob.getMainHandItem().is(Items.CROSSBOW)&&s[0].mob.getAttached(EquipmentService.DATA).getStringOr("outcome","").equals("equipped"),"Born crossbow retained, no invented ranged AI");QaHarness.cleanup(server);h.assertTrue(level.getBlockState(pos.below()).isAir()&&s[0].mob.isRemoved()&&s[0].target.isRemoved(),"QA entities/terrain cleaned");h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=40)
    public void strictPoolTagsAndInvalidFieldsStillRejected(GameTestHelper h){
        String good="{\"version\":1,\"entities\":[\"minecraft:wither_skeleton\"],\"slot\":\"mainhand\",\"entries\":[{\"item\":\"minecraft:bow\",\"weight\":1}]}";
        for(String bad:List.of(good.replace("\"version\":1","\"version\":2"),good.replace("\"weight\":1","\"weight\":1.5"),good.replace("\"weight\":1","\"weight\":1,\"count\":64"),good.replace("minecraft:bow","missing:bow"))){boolean fail=false;try{var json=JsonParser.parseString(bad);EquipmentPools.validateFields(json);var p=EquipmentPool.CODEC.parse(JsonOps.INSTANCE,json).getOrThrow();EquipmentPools.resolve(Identifier.fromNamespaceAndPath("test","bad"),p,h.getLevel().registryAccess(),h.getLevel().enabledFeatures());}catch(IllegalArgumentException|IllegalStateException expected){fail=true;}h.assertTrue(fail,"Strict invalid definition rejected");}h.runAtTickTime(5,()->h.succeed());
    }
}
