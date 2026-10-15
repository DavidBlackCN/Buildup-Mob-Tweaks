package com.davidblackcn.buildupmobtweaks;

import com.davidblackcn.buildupmobtweaks.villager.*;
import com.davidblackcn.buildupmobtweaks.villager.profession.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.memory.*;
import net.minecraft.world.entity.ai.village.poi.*;
import net.minecraft.world.entity.npc.villager.*;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.block.*;
import net.minecraft.world.item.*;
import com.mojang.serialization.MapCodec;
import java.util.*;
import static com.davidblackcn.buildupmobtweaks.FarmerIntelligenceTests.*;

/** Every action assertion runs the native Brain work package on server ticks. */
public final class ProfessionCoreTests implements ModInitializer {
    static final String ENV="buildupmobtweaks:profession_core";
    static final ResourceKey<VillagerProfession> MOCK=ResourceKey.create(Registries.VILLAGER_PROFESSION,Identifier.fromNamespaceAndPath("buildupmobtweaks-test","worker"));
    static final ResourceKey<VillagerProfession> UNKNOWN=ResourceKey.create(Registries.VILLAGER_PROFESSION,Identifier.fromNamespaceAndPath("buildupmobtweaks-test","unsupported"));
    public record Settings(FarmerIntelligenceTests.Settings farmer,boolean enabled,boolean daily,boolean meal,int scan,int cooldown){}
    public record Environment() implements TestEnvironmentDefinition<Settings> {
        static final MapCodec<Environment> CODEC=MapCodec.unit(Environment::new);
        public Settings setup(ServerLevel l){var f=new FarmerIntelligenceTests.Environment(true,true,false).setup(l);var c=BuildupMobTweaks.config().villagers.professions;var old=new Settings(f,c.enabled.get(),c.daily_routines.get(),c.self_heal.get(),c.scan_interval.get(),c.action_cooldown.get());c.enabled.accept(true);c.daily_routines.accept(true);c.self_heal.accept(false);c.scan_interval.accept(20);c.action_cooldown.accept(1200);return old;}
        public void teardown(ServerLevel l,Settings old){new FarmerIntelligenceTests.Environment(true,true,false).teardown(l,old.farmer());var c=BuildupMobTweaks.config().villagers.professions;c.enabled.accept(old.enabled());c.daily_routines.accept(old.daily());c.self_heal.accept(old.meal());c.scan_interval.accept(old.scan());c.action_cooldown.accept(old.cooldown());}
        public MapCodec<Environment> codec(){return CODEC;}
    }
    public void onInitialize(){
        Registry.register(BuiltInRegistries.TEST_ENVIRONMENT_DEFINITION_TYPE,BuildupMobTweaks.id("profession_core_test"),Environment.CODEC);
        var base=BuiltInRegistries.VILLAGER_PROFESSION.getValue(VillagerProfession.LIBRARIAN.identifier());
        for(var key:List.of(MOCK,UNKNOWN))Registry.register(BuiltInRegistries.VILLAGER_PROFESSION,key.identifier(),new VillagerProfession(net.minecraft.network.chat.Component.literal(key.identifier().toString()),base.heldJobSite(),base.acquirableJobSite(),base.requestedItems(),base.secondaryPoi(),base.workSound(),base.tradeSetsByLevel()));
        var version=net.fabricmc.loader.api.FabricLoader.getInstance().getModContainer("buildupmobtweaks-test").orElseThrow().getMetadata().getVersion().getFriendlyString();
        if(!ProfessionRegistry.optional("buildupmobtweaks-test",version,com.davidblackcn.buildupmobtweaks.compat.test_profession.WorkerAdapter::create))throw new AssertionError("Installed exact-version simulated profession adapter skipped");
    }
    static Villager worker(GameTestHelper h,ResourceKey<VillagerProfession> profession,Block block){
        floor(h);var v=EntityTypes.VILLAGER.create(h.getLevel(),EntitySpawnReason.COMMAND);var pos=p(h,9,8);v.snapTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5,0,0);v.setPersistenceRequired();v.setVillagerData(v.getVillagerData().withProfession(h.getLevel().registryAccess().lookupOrThrow(Registries.VILLAGER_PROFESSION).getOrThrow(profession)));v.setVillagerXp(20);
        var job=p(h,8,8);h.getLevel().setBlock(job,block.defaultBlockState(),3);var poi=PoiTypes.forState(h.getLevel().getBlockState(job)).orElseThrow();h.getLevel().getPoiManager().add(job,poi);h.getLevel().getPoiManager().take(t->t.is(poi.unwrapKey().orElseThrow()),(t,b)->b.equals(job),job,1);
        v.getBrain().setMemory(MemoryModuleType.JOB_SITE,GlobalPos.of(h.getLevel().dimension(),job));v.refreshBrain(h.getLevel());if(h.getLevel().getOverworldClockTime()%24000<12000)v.getBrain().setActiveActivityIfPossible(Activity.WORK);v.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED).setBaseValue(0);h.getLevel().addFreshEntity(v);return v;
    }
    static void cycle(GameTestHelper h,ResourceKey<VillagerProfession> profession,Block block){var v=worker(h,profession,block);if(profession.equals(VillagerProfession.CARTOGRAPHER))v.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED).setBaseValue(.5);h.runAtTickTime(850,()->{var r=ProfessionWorkState.peek(v).orElseThrow();h.assertTrue(r.completed>0&&r.starts>0,"Native RunOne work slot completed the registered profession activity");h.assertTrue(v.getVillagerData().profession().is(profession)&&v.getBrain().getMemory(MemoryModuleType.JOB_SITE).orElseThrow().pos().equals(p(h,8,8))&&v.getInventory().isEmpty(),"Work preserves profession, real POI and inventory");BuildupMobTweaks.LOGGER.info("A3_CORE_PROOF profession={} completed={} starts={} cooldown={}",profession,r.completed,r.starts,v.getAttached(ProfessionWorkState.DATA));v.discard();h.succeed();});}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=920) public void nativeArmorerWorkSlot(GameTestHelper h){cycle(h,VillagerProfession.ARMORER,Blocks.BLAST_FURNACE);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=920) public void nativeButcherWorkSlot(GameTestHelper h){cycle(h,VillagerProfession.BUTCHER,Blocks.SMOKER);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=920) public void nativeCartographerWorkSlot(GameTestHelper h){cycle(h,VillagerProfession.CARTOGRAPHER,Blocks.CARTOGRAPHY_TABLE);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=920) public void nativeClericWorkSlot(GameTestHelper h){cycle(h,VillagerProfession.CLERIC,Blocks.BREWING_STAND);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=920) public void nativeFishermanWorkSlot(GameTestHelper h){cycle(h,VillagerProfession.FISHERMAN,Blocks.BARREL);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=920) public void nativeFletcherWorkSlot(GameTestHelper h){cycle(h,VillagerProfession.FLETCHER,Blocks.FLETCHING_TABLE);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=920) public void nativeLeatherworkerWorkSlot(GameTestHelper h){cycle(h,VillagerProfession.LEATHERWORKER,Blocks.CAULDRON);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=920) public void nativeLibrarianWorkSlot(GameTestHelper h){cycle(h,VillagerProfession.LIBRARIAN,Blocks.LECTERN);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=920) public void nativeMasonWorkSlot(GameTestHelper h){cycle(h,VillagerProfession.MASON,Blocks.STONECUTTER);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=920) public void nativeShepherdWorkSlot(GameTestHelper h){cycle(h,VillagerProfession.SHEPHERD,Blocks.LOOM);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=920) public void nativeToolsmithWorkSlot(GameTestHelper h){cycle(h,VillagerProfession.TOOLSMITH,Blocks.SMITHING_TABLE);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=920) public void nativeWeaponsmithWorkSlot(GameTestHelper h){cycle(h,VillagerProfession.WEAPONSMITH,Blocks.GRINDSTONE);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=920) public void simulatedProfessionUsesSameNativeWorkSlot(GameTestHelper h){cycle(h,MOCK,Blocks.LECTERN);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=480)
    public void unknownProfessionKeepsNativeWorkWithoutGuessedActivity(GameTestHelper h){var v=worker(h,UNKNOWN,Blocks.LECTERN);h.runAtTickTime(400,()->{h.assertTrue(ProfessionRegistry.profiles(UNKNOWN).isEmpty()&&ProfessionWorkState.peek(v).map(r->r.starts==0).orElse(true)&&v.isAlive(),"Unknown profession is Level 0, without synthetic work");v.discard();h.succeed();});}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=50)
    public void optionalModAbsenceAndVersionMismatchNeverLoadAdapterClass(GameTestHelper h){h.assertFalse(ProfessionRegistry.optional("missing_a3_test_mod","1.0",()->{throw new AssertionError("Missing mod factory invoked");}),"Absent dependency skipped");h.assertFalse(ProfessionRegistry.optional("buildupmobtweaks","not-installed-version",()->{throw new AssertionError("Wrong version factory invoked");}),"Exact version mismatch skipped");h.succeed();}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=50)
    public void duplicateAndAmbiguousAdaptersAreRejectedOrPaused(GameTestHelper h){var key=ResourceKey.create(Registries.VILLAGER_PROFESSION,BuildupMobTweaks.id("test_conflict"));var profile=new ProfessionProfile(BuildupMobTweaks.id("conflict"),key,PoiTypes.LIBRARIAN,8);var entry=new ProfessionAdapter.Entry(BuildupMobTweaks.id("conflicted_read"),0,()->new WorkCycle("conflicted_read",Items.BOOK));var a=new ProfessionAdapter(BuildupMobTweaks.id("test/conflict_a"),profile,List.of(entry),false);ProfessionRegistry.register(a);boolean duplicate=false;try{ProfessionRegistry.register(a);}catch(IllegalArgumentException expected){duplicate=true;}ProfessionRegistry.register(new ProfessionAdapter(BuildupMobTweaks.id("test/conflict_b"),profile,List.of(entry),false));h.assertTrue(duplicate&&ProfessionRegistry.ambiguous(key)&&ProfessionRegistry.activities(key).isEmpty(),"Equal priority pauses activity and duplicate ID cannot replace prior implementation");h.succeed();}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=350)
    public void futureWorkDataIsPreservedAndPausesOwnActions(GameTestHelper h){var v=worker(h,VillagerProfession.LIBRARIAN,Blocks.LECTERN);var raw=new net.minecraft.nbt.CompoundTag();raw.putInt("version",99);raw.putString("future","keep");v.setAttached(ProfessionWorkState.DATA,raw.copy());h.runAtTickTime(280,()->{h.assertTrue(raw.equals(v.getAttached(ProfessionWorkState.DATA))&&ProfessionWorkState.peek(v).map(r->r.starts==0).orElse(true),"Unknown format preserved without resource or runtime work");v.discard();h.succeed();});}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=1000)
    public void nativeReloadRetainsCooldownWithoutReplayingWork(GameTestHelper h){Villager[] ref={worker(h,MOCK,Blocks.LECTERN)};boolean[] loaded={false};net.minecraft.nbt.CompoundTag[] raw={null};h.onEachTick(()->{var v=ref[0];if(!loaded[0]&&ProfessionWorkState.peek(v).map(r->r.completed>0).orElse(false)){raw[0]=v.getAttached(ProfessionWorkState.DATA).copy();ref[0]=CombatTestWorld.reload(h,v);loaded[0]=true;}});h.runAtTickTime(920,()->{h.assertTrue(loaded[0]&&raw[0].equals(ref[0].getAttached(ProfessionWorkState.DATA))&&ProfessionWorkState.peek(ref[0]).map(r->r.starts==0).orElse(true),"Persistent cooldown survives native serialization and suppresses replay");ref[0].discard();h.succeed();});}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=750)
    public void noAiInterruptsActualWorkAndClearsOnlyOwnedVisual(GameTestHelper h){var v=worker(h,MOCK,Blocks.LECTERN);boolean[] changed={false};h.onEachTick(()->{if(!changed[0]&&ProfessionWorkState.busy(v)){changed[0]=true;v.setNoAi(true);}});h.runAtTickTime(650,()->{h.assertTrue(changed[0]&&!ProfessionWorkState.busy(v)&&v.getAttachedOrCreate(ProfessionWorkState.VISUAL)==0,"NoAI boundary stops real work and synced prop");v.discard();h.succeed();});}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=750)
    public void tradingInterruptsActualWorkWithoutChangingOffers(GameTestHelper h){var v=worker(h,VillagerProfession.LIBRARIAN,Blocks.LECTERN);v.getOffers();var offers=v.getOffers();var p=h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);boolean[] changed={false};h.onEachTick(()->{if(!changed[0]&&ProfessionWorkState.busy(v)){changed[0]=true;v.setTradingPlayer(p);}});h.runAtTickTime(650,()->{h.assertTrue(changed[0]&&ProfessionWorkState.peek(v).map(r->r.stops>0).orElse(false)&&v.getOffers()==offers,"Trade takes priority and original offers remain");v.setTradingPlayer(null);v.discard();h.succeed();});}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=350)
    public void explicitExternalOwnerReceivesNoProfessionWork(GameTestHelper h){var v=worker(h,MOCK,Blocks.LECTERN);v.addTag("buildupmobtweaks:profession_external");h.runAtTickTime(280,()->{h.assertTrue(!ProfessionWorkState.busy(v)&&ProfessionWorkState.peek(v).map(r->r.starts==0).orElse(true)&&!v.hasAttached(ProfessionWorkState.DATA),"External controller owns work, without Buildup saved actions");v.discard();h.succeed();});}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=750)
    public void professionChangeStopsActiveWorkAndPreservesNativeJobRules(GameTestHelper h){var v=worker(h,MOCK,Blocks.LECTERN);boolean[] changed={false};h.onEachTick(()->{if(!changed[0]&&ProfessionWorkState.busy(v)){changed[0]=true;v.setVillagerData(v.getVillagerData().withProfession(h.getLevel().registryAccess().lookupOrThrow(Registries.VILLAGER_PROFESSION).getOrThrow(UNKNOWN)));}});h.runAtTickTime(650,()->{h.assertTrue(changed[0]&&!ProfessionWorkState.busy(v)&&v.getVillagerData().profession().is(UNKNOWN),"Changed profession exits old activity, without restoring an obsolete profession");v.discard();h.succeed();});}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=750)
    public void farmerKeepsItsOriginalUniqueHarvestBeforeAdditionalWork(GameTestHelper h){floor(h);crop(h,10,8,Blocks.WHEAT,true);var v=farmer(h,9,8,true);v.getInventory().setItem(0,new ItemStack(Items.WHEAT_SEEDS,1));h.onEachTick(()->h.assertFalse(com.davidblackcn.buildupmobtweaks.villager.farmer.FarmerModule.working(v)&&ProfessionWorkState.busy(v),"Shared villager never runs two Buildup jobs"));h.runAtTickTime(650,()->{h.assertTrue(harvests(v)==1&&replants(v)==1&&!com.davidblackcn.buildupmobtweaks.villager.farmer.FarmerState.pending(v),"Original FarmerActivity performs its sole harvest/replant");v.discard();h.succeed();});}
}
