package com.davidblackcn.buildupmobtweaks;

import com.davidblackcn.buildupmobtweaks.villager.*;
import com.mojang.serialization.MapCodec;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.*;
import net.minecraft.world.entity.ai.village.poi.*;
import net.minecraft.world.entity.npc.villager.*;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.trading.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.pathfinder.*;
import java.util.*;

/** Isolated environment configuration; assertions observe real entity/Brain/navigation ticks. */
public final class VillagerCoreTests implements ModInitializer {
    static final String ENV="buildupmobtweaks:villager_core", OFF="buildupmobtweaks:villager_off", NIGHT="buildupmobtweaks:villager_night", ARENA="buildupmobtweaks:r2a_arena";
    @Override public void onInitialize() {
        Registry.register(BuiltInRegistries.TEST_ENVIRONMENT_DEFINITION_TYPE, BuildupMobTweaks.id("villager_core_test"), Environment.CODEC);
    }
    public record Settings(boolean enabled,int poi,int nav,int stuck,int retry,int retries,long day,net.minecraft.world.Difficulty difficulty) {}
    public record Environment(boolean enabled, boolean night, boolean hard) implements TestEnvironmentDefinition<Settings> {
        static final MapCodec<Environment> CODEC=com.mojang.serialization.codecs.RecordCodecBuilder.mapCodec(i->i.group(
                com.mojang.serialization.Codec.BOOL.fieldOf("enabled").forGetter(Environment::enabled),
                com.mojang.serialization.Codec.BOOL.optionalFieldOf("night",false).forGetter(Environment::night),
                com.mojang.serialization.Codec.BOOL.optionalFieldOf("hard",false).forGetter(Environment::hard)).apply(i,Environment::new));
        public Settings setup(ServerLevel level) {
            var c=BuildupMobTweaks.config().villagers;
            var old=new Settings(c.enabled.get(),c.poi_interval.get(),c.navigation_interval.get(),c.stuck_ticks.get(),c.retry_ticks.get(),c.max_retries.get(),level.getOverworldClockTime(),level.getDifficulty());
            if(hard) level.getServer().setDifficulty(net.minecraft.world.Difficulty.HARD,true);
            c.enabled.accept(enabled);c.poi_interval.accept(20);c.navigation_interval.accept(5);c.stuck_ticks.accept(40);c.retry_ticks.accept(40);c.max_retries.accept(1);
            level.clockManager().setTotalTicks(level.registryAccess().lookupOrThrow(Registries.WORLD_CLOCK).getOrThrow(net.minecraft.world.clock.WorldClocks.OVERWORLD), night?13000:6000); return old;
        }
        public void teardown(ServerLevel level,Settings old) {
            level.getServer().setDifficulty(old.difficulty(),true);
            var c=BuildupMobTweaks.config().villagers;c.enabled.accept(old.enabled());c.poi_interval.accept(old.poi());c.navigation_interval.accept(old.nav());
            c.stuck_ticks.accept(old.stuck());c.retry_ticks.accept(old.retry());c.max_retries.accept(old.retries());level.clockManager().setTotalTicks(level.registryAccess().lookupOrThrow(Registries.WORLD_CLOCK).getOrThrow(net.minecraft.world.clock.WorldClocks.OVERWORLD), old.day());
        }
        public MapCodec<Environment> codec(){return CODEC;}
    }
    static void floor(GameTestHelper h){SkeletonBehaviorTests.floor(h,true);}
    static BlockPos pos(GameTestHelper h,int x,int z){return h.absolutePos(new BlockPos(x,2,z));}
    static Villager villager(GameTestHelper h,int x,int z){
        var v=EntityTypes.VILLAGER.create(h.getLevel(),EntitySpawnReason.COMMAND);
        var spawn=pos(h,x,z);v.snapTo(spawn.getX()+0.5,spawn.getY(),spawn.getZ()+0.5,0,0);v.setPersistenceRequired();v.setVillagerData(v.getVillagerData().withProfession(
                h.getLevel().registryAccess().lookupOrThrow(Registries.VILLAGER_PROFESSION).getOrThrow(VillagerProfession.FLETCHER)));
        v.setVillagerXp(20);v.refreshBrain(h.getLevel());h.getLevel().addFreshEntity(v);return v;
    }
    static void stationary(Villager v){v.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);}
    static GlobalPos global(GameTestHelper h,int x,int z){return GlobalPos.of(h.getLevel().dimension(),pos(h,x,z));}
    static void job(GameTestHelper h,int x,int z){h.setBlock(new BlockPos(x,2,z),Blocks.FLETCHING_TABLE);var p=pos(h,x,z);h.getLevel().getPoiManager().add(p,PoiTypes.forState(h.getLevel().getBlockState(p)).orElseThrow());}
    static void bed(GameTestHelper h,int x,int z){
        h.setBlock(new BlockPos(x-1,2,z),Blocks.BED.red().defaultBlockState().setValue(BedBlock.PART,BedPart.FOOT).setValue(BedBlock.FACING,Direction.EAST));
        h.setBlock(new BlockPos(x,2,z),Blocks.BED.red().defaultBlockState().setValue(BedBlock.PART,BedPart.HEAD).setValue(BedBlock.FACING,Direction.EAST));
        var p=pos(h,x,z);h.getLevel().getPoiManager().add(p,PoiTypes.forState(h.getLevel().getBlockState(p)).orElseThrow());
    }
    static void bind(GameTestHelper h,Villager v,MemoryModuleType<GlobalPos> memory,int x,int z){
        var p=pos(h,x,z);h.getLevel().getPoiManager().take(t->true,(t,b)->b.equals(p),p,1);
        v.getBrain().setMemory(memory,global(h,x,z));
    }
    static void proof(String name,Villager v){var s=VillagerState.runtime(v);BuildupMobTweaks.LOGGER.info("PHASE3_CORE_PROOF scenario={} ticks={} checks={} invalid={} stuck={} recomputes={} deferred={} hazards={} failures={} decision={} data={}",name,v.tickCount,s.poiChecks,s.invalidPois,s.stuckEvents,s.recomputes,s.deferrals,s.hazardStops,s.failures,s.lastDecision,v.getAttached(VillagerState.DATA));}
    static void finish(GameTestHelper h,Villager v,String name){proof(name,v);v.discard();h.succeed();}
    static void walk(Villager v,BlockPos p){v.getBrain().setMemory(MemoryModuleType.WALK_TARGET,new WalkTarget(p,0.5F,0));}
    // Movement fixtures isolate the original MoveToTargetSink inside a real ticked Brain.
    // Full native packages are retained in all POI, trading, raid and night tests.
    static void movementFixture(GameTestHelper h,Villager v,BlockPos target) {
        v.getBrain().stopAll(h.getLevel(),v);v.getBrain().removeAllBehaviors();
        v.getBrain().addActivity(Activity.CORE,com.google.common.collect.ImmutableList.of(com.mojang.datafixers.util.Pair.of(1,new net.minecraft.world.entity.ai.behavior.MoveToTargetSink(1000,1000))),Set.of(),Set.of());
        v.getBrain().setCoreActivities(Set.of(Activity.CORE));
        if(target!=null) h.runAtTickTime(5,()->walk(v,target));
    }


    @GameTest(environment=OFF,structure=ARENA,maxTicks=60)
    public void defaultOffLeavesNativeBrainAndStorageUntouched(GameTestHelper h){floor(h);var v=villager(h,8,8);stationary(v);var brain=v.getBrain();
        h.runAtTickTime(40,()->{h.assertFalse(VillagerState.hasRuntime(v)||v.hasAttached(VillagerState.DATA),"Default off creates no Core state");h.assertTrue(v.getBrain()==brain&&v.getVillagerXp()==20,"Native Brain/trade XP retained");v.discard();h.succeed();});}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=80)
    public void validJobAndBedRetainBindingsAndTradeState(GameTestHelper h){floor(h);job(h,9,8);bed(h,8,10);var v=villager(h,8,8);stationary(v);bind(h,v,MemoryModuleType.JOB_SITE,9,8);bind(h,v,MemoryModuleType.HOME,8,10);
        var offers=new MerchantOffers();offers.add(new MerchantOffer(new ItemCost(Items.EMERALD,3),new ItemStack(Items.ARROW,4),8,2,0.05F));v.setOffers(offers);
        h.runAtTickTime(60,()->{h.assertTrue(v.getBrain().isMemoryValue(MemoryModuleType.JOB_SITE,global(h,9,8))&&v.getBrain().isMemoryValue(MemoryModuleType.HOME,global(h,8,10)),"Valid native bindings remain");h.assertTrue(v.getOffers()==offers&&offers.getFirst().getUses()==0&&v.getVillagerXp()==20&&v.getVillagerData().profession().is(VillagerProfession.FLETCHER),"Offers, uses, XP and locked profession unchanged");h.assertTrue(VillagerState.runtime(v).poiChecks>0&&VillagerState.runtime(v).invalidPois==0,"Enabled checks really ran");finish(h,v,"valid_poi_trade");});}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=100)
    public void invalidDistantJobIsClearedWithoutUnlockingProfession(GameTestHelper h){floor(h);var v=villager(h,5,8);stationary(v);v.getBrain().setMemory(MemoryModuleType.JOB_SITE,global(h,28,8));
        h.runAtTickTime(70,()->{h.assertFalse(v.getBrain().hasMemoryValue(MemoryModuleType.JOB_SITE),"Invalid loaded binding beyond native 16 blocks cleared");h.assertTrue(VillagerState.runtime(v).invalidPois>0&&v.getVillagerData().profession().is(VillagerProfession.FLETCHER)&&v.getVillagerXp()==20,"Core ran without unlocking traded profession");finish(h,v,"invalid_far_job");});}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=100)
    public void invalidDistantBedIsCleared(GameTestHelper h){floor(h);var v=villager(h,5,8);stationary(v);v.getBrain().setMemory(MemoryModuleType.HOME,global(h,28,8));
        h.runAtTickTime(70,()->{h.assertFalse(v.getBrain().hasMemoryValue(MemoryModuleType.HOME),"Removed bed cleared even during work schedule");h.assertTrue(VillagerState.runtime(v).invalidPois>0,"Core invalidation observed");finish(h,v,"invalid_far_bed");});}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=400)
    public void nativeAcquisitionRestoresWorkstationAfterInvalidation(GameTestHelper h){floor(h);job(h,9,8);var v=villager(h,8,8);v.getBrain().setMemory(MemoryModuleType.JOB_SITE,global(h,28,8));
        h.runAtTickTime(320,()->{h.assertTrue(v.getBrain().isMemoryValue(MemoryModuleType.JOB_SITE,global(h,9,8)),"Native AcquirePoi/AssignProfession reacquire existing valid workstation");h.assertTrue(VillagerState.runtime(v).invalidPois>0&&v.getVillagerXp()==20,"Invalidation ran and XP persists");finish(h,v,"native_job_recovery");});}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=250)
    public void nativeAcquisitionRestoresBedAfterInvalidation(GameTestHelper h){floor(h);bed(h,10,8);var v=villager(h,8,8);v.getBrain().setMemory(MemoryModuleType.HOME,global(h,28,8));
        h.runAtTickTime(200,()->{h.assertTrue(v.getBrain().isMemoryValue(MemoryModuleType.HOME,global(h,10,8)),"Native AcquirePoi reacquires an available bed");h.assertTrue(VillagerState.runtime(v).invalidPois>0,"Core invalidation ran");finish(h,v,"native_bed_recovery");});}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=400)
    public void twoVillagersRespectSinglePoiCapacity(GameTestHelper h){floor(h);job(h,10,8);bed(h,12,8);var a=villager(h,8,8);var b=villager(h,14,8);
        h.runAtTickTime(320,()->{long jobs=List.of(a,b).stream().filter(v->v.getBrain().isMemoryValue(MemoryModuleType.JOB_SITE,global(h,10,8))).count();long beds=List.of(a,b).stream().filter(v->v.getBrain().isMemoryValue(MemoryModuleType.HOME,global(h,12,8))).count();h.assertTrue(jobs==1&&beds==1,"One ticket per workstation/bed; no stealing or duplicate binding");proof("poi_capacity_a",a);proof("poi_capacity_b",b);a.discard();b.discard();h.succeed();});}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=80)
    public void unloadedPoiIsUnknownAndDoesNotLoadChunk(GameTestHelper h){floor(h);var v=villager(h,8,8);stationary(v);var p=new BlockPos(20000000,80,20000000);var g=GlobalPos.of(h.getLevel().dimension(),p);v.getBrain().setMemory(MemoryModuleType.HOME,g);
        h.runAtTickTime(60,()->{h.assertFalse(h.getLevel().hasChunkAt(p),"Core did not load distant chunk");h.assertTrue(v.getBrain().isMemoryValue(MemoryModuleType.HOME,g)&&VillagerState.runtime(v).invalidPois==0,"Unloaded memory retained");finish(h,v,"unloaded_poi");});}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=80)
    public void crossDimensionBedBindingIsNotErasedByCore(GameTestHelper h){floor(h);var v=villager(h,8,8);stationary(v);var g=GlobalPos.of(net.minecraft.world.level.Level.NETHER,new BlockPos(0,80,0));v.getBrain().setMemory(MemoryModuleType.HOME,g);
        h.runAtTickTime(60,()->{h.assertTrue(v.getBrain().isMemoryValue(MemoryModuleType.HOME,g)&&VillagerState.runtime(v).invalidPois==0,"Core defers other dimension to native lifecycle");finish(h,v,"other_dimension");});}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=80)
    public void replacedPoiDoesNotReleaseNewTypesTicket(GameTestHelper h){floor(h);bed(h,28,8);var v=villager(h,5,8);stationary(v);bind(h,v,MemoryModuleType.HOME,28,8);v.getBrain().setMemory(MemoryModuleType.JOB_SITE,global(h,28,8));
        h.runAtTickTime(60,()->{h.assertFalse(v.getBrain().hasMemoryValue(MemoryModuleType.JOB_SITE),"Mismatched job memory cleared");h.assertTrue(v.getBrain().isMemoryValue(MemoryModuleType.HOME,global(h,28,8)),"Valid bed retains its ticket");h.assertTrue(h.getLevel().getPoiManager().getCountInRange(t->t.is(PoiTypes.HOME),pos(h,28,8),1,PoiManager.Occupancy.HAS_SPACE)==0,"No release of unrelated occupied bed");finish(h,v,"replaced_poi_ticket");});}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=100)
    public void idleStationaryVillagerIsNotStuck(GameTestHelper h){floor(h);job(h,8,8);var v=villager(h,8,10);stationary(v);bind(h,v,MemoryModuleType.JOB_SITE,8,8);movementFixture(h,v,null);
        h.runAtTickTime(80,()->{h.assertTrue(VillagerState.runtime(v).stuckEvents==0,"Standing at work without outstanding movement is not stuck");finish(h,v,"idle_not_stuck");});}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=160)
    public void actualNativeMovementHasProgressWithoutRecovery(GameTestHelper h){floor(h);var v=villager(h,5,8);var start=v.position();movementFixture(h,v,pos(h,15,8));
        h.runAtTickTime(120,()->{h.assertTrue(v.position().distanceTo(start)>3&&v.tickCount>100,"Real navigation moved without teleport");h.assertTrue(VillagerState.runtime(v).stuckEvents==0,"Moving villager not flagged");finish(h,v,"normal_navigation");});}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=160)
    public void noProgressTriggersRecomputeThenBoundedDeferral(GameTestHelper h){floor(h);var v=villager(h,5,8);stationary(v);var start=v.position();movementFixture(h,v,pos(h,15,8));
        h.runAtTickTime(130,()->{var s=VillagerState.runtime(v);h.assertTrue(s.stuckEvents>=2&&s.recomputes>=1&&s.deferrals>=1,"Native running task detected, recomputed then deferred");h.assertTrue(v.position().distanceTo(start)<0.1&&h.getBlockState(new BlockPos(10,1,8)).is(Blocks.STONE),"No teleport or terrain edits");finish(h,v,"stuck_recovery");});}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=130)
    public void nullNativePathRecordsRetryAndPreservesBinding(GameTestHelper h){floor(h);var v=villager(h,8,8);stationary(v);
        for(int x=7;x<=9;x++)for(int z=7;z<=9;z++)if(x!=8||z!=8)for(int y=2;y<=4;y++)h.setBlock(new BlockPos(x,y,z),Blocks.STONE);
        var target=pos(h,15,8);movementFixture(h,v,target);
        h.runAtTickTime(100,()->{h.assertTrue(VillagerState.runtime(v).failures>0||VillagerState.runtime(v).recomputes>0,"Failed/partial native path gets bounded recovery");h.assertTrue(v.getVillagerXp()==20&&v.getVillagerData().profession().is(VillagerProfession.FLETCHER),"Path failure does not directly unlock profession");finish(h,v,"path_failure");});}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=70)
    public void changingTerrainHazardStopsNativePath(GameTestHelper h){floor(h);var v=villager(h,5,8);stationary(v);movementFixture(h,v,pos(h,15,8));
        h.runAtTickTime(15,()->{var p=v.getNavigation().getPath();h.assertTrue(p!=null&&!p.isDone(),"Native path exists");var n=p.getNextNodePos();h.getLevel().setBlock(n,Blocks.LAVA.defaultBlockState(),3);});
        h.runAtTickTime(45,()->{h.assertTrue(VillagerState.runtime(v).hazardStops>0,"New lava on actual route cancels native elective path");h.assertTrue(v.getHealth()>0,"Still alive");finish(h,v,"dynamic_lava");});}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=30)
    public void safetyRecognizesWaterFireAndDeepDrops(GameTestHelper h){floor(h);var l=h.getLevel();var p=pos(h,18,8);h.setBlock(new BlockPos(18,2,8),Blocks.WATER);
        h.assertTrue(NavigationSafety.hazardous(l,p),"Water channel unsafe");h.setBlock(new BlockPos(18,2,8),Blocks.FIRE);h.assertTrue(NavigationSafety.hazardous(l,p),"Fire unsafe");h.setBlock(new BlockPos(18,2,8),Blocks.AIR);
        h.assertFalse(NavigationSafety.hazardous(l,p),"Normal supported floor safe");for(int y=1;y>=-3;y--)l.setBlock(p.atY(h.absolutePos(new BlockPos(18,y,8)).getY()),Blocks.AIR.defaultBlockState(),3);
        h.assertTrue(NavigationSafety.hazardous(l,p),"Deep unsupported fall unsafe");h.succeed();}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=80)
    public void foreignNavigationPathIsNotCleared(GameTestHelper h){floor(h);var v=villager(h,5,8);stationary(v);movementFixture(h,v,pos(h,15,8));
        h.runAtTickTime(20,()->{var foreign=new Path(List.of(new Node(pos(h,6,8).getX(),pos(h,6,8).getY(),pos(h,6,8).getZ()),new Node(pos(h,15,8).getX(),pos(h,15,8).getY(),pos(h,15,8).getZ())),pos(h,15,8),true);
            v.entityTags().add("buildupmobtweaks:disable_villager_navigation_safety");v.getNavigation().moveTo(foreign,0.5);});
        h.runAtTickTime(60,()->{h.assertTrue(VillagerState.runtime(v).stuckEvents==0&&VillagerState.runtime(v).lastDecision==ControlPolicy.Decision.EXCLUDED,"Excluded navigation yields without repair");finish(h,v,"external_nav_yield");});}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=80)
    public void entityAndProfessionExclusionsYieldCore(GameTestHelper h){floor(h);var a=villager(h,5,8);var b=villager(h,12,8);a.addTag("buildupmobtweaks:vanilla_ai");b.addTag("buildupmobtweaks:disable_villager_profession/minecraft:fletcher");stationary(a);stationary(b);
        h.runAtTickTime(60,()->{h.assertFalse(VillagerState.hasRuntime(a)||VillagerState.hasRuntime(b),"No Core state on explicitly excluded entities/professions");a.discard();b.discard();h.succeed();});}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=80)
    public void tradeAndPanicYieldWithoutChangingTrade(GameTestHelper h){floor(h);var v=villager(h,5,8);stationary(v);var player=h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);v.setTradingPlayer(player);v.getOffers();
        var offerCopy=v.getOffers().copy();
        h.runAtTickTime(20,()->{h.assertTrue(ControlPolicy.query(v,h.getLevel(),ControlPolicy.Scope.NAVIGATION)==ControlPolicy.Decision.VANILLA_PRIORITY,"Trade yields");v.setTradingPlayer(null);var danger=h.spawn(EntityTypes.ZOMBIE,new BlockPos(7,2,8));danger.setNoAi(true);v.hurtServer(h.getLevel(),h.getLevel().damageSources().mobAttack(danger),1);});
        h.runAtTickTime(45,()->{h.assertTrue(ControlPolicy.query(v,h.getLevel(),ControlPolicy.Scope.POI)==ControlPolicy.Decision.VANILLA_PRIORITY,"Panic yields");h.assertTrue(v.getVillagerXp()==20&&v.getOffers().size()==offerCopy.size(),"Trades/XP untouched");v.discard();h.succeed();});}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=80)
    public void raidActivityYieldsToNativeResponse(GameTestHelper h){floor(h);var v=villager(h,8,8);stationary(v);v.getBrain().setActiveActivityIfPossible(Activity.PRE_RAID);
        h.runAtTickTime(10,()->{v.getBrain().setActiveActivityIfPossible(Activity.PRE_RAID);h.assertTrue(ControlPolicy.query(v,h.getLevel(),ControlPolicy.Scope.NAVIGATION)==ControlPolicy.Decision.VANILLA_PRIORITY,"Pre-raid path and schedule left native");v.discard();h.succeed();});}
    @GameTest(environment=NIGHT,structure=ARENA,maxTicks=250)
    public void nativeNightScheduleAndSleepRemain(GameTestHelper h){floor(h);bed(h,9,8);var v=villager(h,8,8);bind(h,v,MemoryModuleType.HOME,9,8);boolean[] slept={false};
        h.onEachTick(()->{slept[0]|=v.isSleeping();});h.runAtTickTime(200,()->{h.assertTrue(slept[0],"Native rest schedule and SleepInBed actually run");h.assertTrue(v.getBrain().isMemoryValue(MemoryModuleType.HOME,global(h,9,8)),"Sleep binding preserved");finish(h,v,"native_night_sleep");});}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=130)
    public void disableReenableDoesNotResetPersistedRetry(GameTestHelper h){floor(h);var v=villager(h,5,8);stationary(v);movementFixture(h,v,pos(h,15,8));
        h.runAtTickTime(60,()->{h.assertTrue(v.hasAttached(VillagerState.DATA),"Recompute marker exists");var old=v.getAttached(VillagerState.DATA).copy();v.addTag("buildupmobtweaks:disable_villager_core");
            h.runAtTickTime(75,()->{h.assertTrue(old.equals(v.getAttached(VillagerState.DATA)),"Disabled preserves retry record");v.removeTag("buildupmobtweaks:disable_villager_core");});});
        h.runAtTickTime(105,()->{h.assertTrue(VillagerState.runtime(v).lastDecision==ControlPolicy.Decision.ALLOW,"Reenabled safely");finish(h,v,"off_on_retry");});}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=130)
    public void retrySerializationAndUnknownVersionsArePreserved(GameTestHelper h){floor(h);Villager[] ref={villager(h,5,8)};stationary(ref[0]);movementFixture(h,ref[0],pos(h,15,8));
        h.runAtTickTime(60,()->{var raw=ref[0].getAttached(VillagerState.DATA).copy();h.assertTrue(raw.getIntOr("retries",0)==1,"Actual recompute marker before save");ref[0]=CombatTestWorld.reload(h,ref[0]);h.assertTrue(raw.equals(ref[0].getAttached(VillagerState.DATA)),"Native entity serialization retains retry");
            var future=new CompoundTag();future.putInt("version",99);future.putString("opaque","preserve");ref[0].setAttached(VillagerState.DATA,future);});
        h.runAtTickTime(100,()->{var raw=ref[0].getAttached(VillagerState.DATA);h.assertTrue(raw.getIntOr("version",0)==99&&raw.getStringOr("opaque","").equals("preserve")&&!VillagerState.known(ref[0]),"Unknown data kept and Core paused");h.assertTrue(ControlPolicy.query(ref[0],h.getLevel(),ControlPolicy.Scope.POI)==ControlPolicy.Decision.UNKNOWN_STATE,"POI writes paused");ref[0].discard();h.succeed();});}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=40)
    public void professionEntryRejectsBabyWrongJobAndBusyWork(GameTestHelper h){floor(h);job(h,9,8);var v=villager(h,8,8);bind(h,v,MemoryModuleType.JOB_SITE,9,8);
        var profile=new ProfessionProfile(BuildupMobTweaks.id("test_fletcher"),VillagerProfession.FLETCHER,PoiTypes.FLETCHER,8);
        v.setAge(-24000);h.assertTrue(ScheduleCoordination.workContext(v,h.getLevel(),profile).isEmpty(),"Baby no work");v.setAge(0);walk(v,pos(h,15,8));
        h.assertTrue(ScheduleCoordination.workContext(v,h.getLevel(),profile).isEmpty(),"Native movement has priority");var wrong=new ProfessionProfile(BuildupMobTweaks.id("wrong"),VillagerProfession.LIBRARIAN,PoiTypes.LIBRARIAN,8);
        h.assertTrue(ScheduleCoordination.workContext(v,h.getLevel(),wrong).isEmpty(),"Wrong profession no guessed activity");v.discard();h.succeed();}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=80)
    public void professionChangeKeepsXpAndReevaluatesPoiPredicate(GameTestHelper h){floor(h);job(h,28,8);var v=villager(h,5,8);stationary(v);bind(h,v,MemoryModuleType.JOB_SITE,28,8);
        v.setVillagerData(v.getVillagerData().withProfession(h.getLevel().registryAccess().lookupOrThrow(Registries.VILLAGER_PROFESSION).getOrThrow(VillagerProfession.LIBRARIAN)));v.refreshBrain(h.getLevel());
        h.runAtTickTime(60,()->{h.assertTrue(v.getVillagerData().profession().is(VillagerProfession.LIBRARIAN)&&v.getVillagerXp()==20&&!v.getBrain().hasMemoryValue(MemoryModuleType.JOB_SITE),"Reevaluate native new-profession predicate without changing profession/XP");finish(h,v,"profession_change");});}

    @GameTest(environment=ENV,structure=ARENA,maxTicks=30)
    public void professionEntryAcceptsAnIdleWorkContextWithoutStartingActivity(GameTestHelper h){floor(h);job(h,9,8);var v=villager(h,8,8);bind(h,v,MemoryModuleType.JOB_SITE,9,8);movementFixture(h,v,null);
        v.getBrain().addActivity(Activity.WORK,com.google.common.collect.ImmutableList.of(),Set.of(),Set.of());v.getBrain().setActiveActivityIfPossible(Activity.WORK);
        var profile=new ProfessionProfile(BuildupMobTweaks.id("test_idle"),VillagerProfession.FLETCHER,PoiTypes.FLETCHER,8);
        h.runAtTickTime(10,()->{h.assertTrue(ScheduleCoordination.workContext(v,h.getLevel(),profile).isPresent(),"Reserved work entry admits matching safe idle context");h.assertTrue(v.getMainHandItem().isEmpty()&&v.getVillagerXp()==20,"Entry creates no equipment or profession action");finish(h,v,"idle_work_entry");});}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=80)
    public void singlePoiExclusionDoesNotDisableNavigation(GameTestHelper h){floor(h);var v=villager(h,5,8);stationary(v);movementFixture(h,v,pos(h,15,8));v.addTag("buildupmobtweaks:disable_villager_poi_intelligence");
        v.getBrain().setMemory(MemoryModuleType.HOME,global(h,28,8));
        h.runAtTickTime(65,()->{h.assertTrue(v.getBrain().hasMemoryValue(MemoryModuleType.HOME)&&VillagerState.runtime(v).poiChecks==0,"POI excluded without clearing native memory");h.assertTrue(VillagerState.runtime(v).recomputes>0,"Navigation remains independently active");finish(h,v,"independent_poi_gate");});}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=60)
    public void malformedV1DataIsRetainedAndPausesCore(GameTestHelper h){floor(h);var v=villager(h,8,8);var raw=new CompoundTag();raw.putInt("version",1);raw.putLong("target",pos(h,15,8).asLong());raw.putString("dimension","invalid dimension");raw.putInt("wait",-1);raw.putInt("retries",99);v.setAttached(VillagerState.DATA,raw);
        var expected=raw.copy();h.runAtTickTime(40,()->{h.assertTrue(expected.equals(v.getAttached(VillagerState.DATA))&&!VillagerState.hasRuntime(v),"Malformed current data is not overwritten or ticked");h.assertTrue(ControlPolicy.query(v,h.getLevel(),ControlPolicy.Scope.NAVIGATION)==ControlPolicy.Decision.UNKNOWN_STATE,"Safe exit on unsupported state");v.discard();h.succeed();});}
    @GameTest(environment="buildupmobtweaks:villager_conversion",structure=ARENA,maxTicks=140)
    public void nativeZombificationCureAndRemovalRetainTradeBoundary(GameTestHelper h){floor(h);var v=villager(h,8,8);stationary(v);var offers=new MerchantOffers();offers.add(new MerchantOffer(new ItemCost(Items.EMERALD,3),new ItemStack(Items.ARROW,4),8,2,0.05F));v.setOffers(offers);
        h.runAtTickTime(20,()->{h.assertTrue(VillagerState.hasRuntime(v),"Core observed original villager before conversion");var zombie=h.spawn(EntityTypes.ZOMBIE,new BlockPos(10,2,8));zombie.setNoAi(true);
            h.assertTrue(zombie.convertVillagerToZombieVillager(h.getLevel(),v),"Real original zombie conversion");zombie.discard();
            var converted=h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.monster.zombie.ZombieVillager.class,v.getBoundingBox().inflate(6)).getFirst();
            h.assertTrue(converted.getVillagerXp()==20,"Native zombie retains traded XP");converted.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WEAKNESS,200));
            var player=h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.GOLDEN_APPLE));converted.mobInteract(player,InteractionHand.MAIN_HAND);converted.setVillagerConversionTime(1);
        });
        h.runAtTickTime(100,()->{var cured=h.getLevel().getEntitiesOfClass(Villager.class,v.getBoundingBox().inflate(12)).getFirst();h.assertTrue(v.isRemoved()&&!VillagerState.hasRuntime(v),"Old runtime released on native removal");h.assertTrue(cured.getVillagerXp()==20&&cured.getVillagerData().profession().is(VillagerProfession.FLETCHER)&&cured.getOffers().size()==1&&cured.getOffers().getFirst().getBaseCostA().is(Items.EMERALD),"Cure retains native profession, XP and offer");h.assertTrue(cured.getMainHandItem().isEmpty()&&cured.getOffhandItem().isEmpty(),"Core does not inject equipment");cured.discard();h.assertFalse(VillagerState.hasRuntime(cured),"Removal releases rebuilt runtime");h.succeed();});}

    static final class PartialBrainVillager extends Villager {
        PartialBrainVillager(ServerLevel level){super(EntityTypes.VILLAGER,level);
            this.brain=net.minecraft.world.entity.ai.Brain.<Villager>provider(
                    List.of(MemoryModuleType.JOB_SITE,MemoryModuleType.POTENTIAL_JOB_SITE,MemoryModuleType.HOME),List.of(),v->List.of())
                    .makeBrain(this,net.minecraft.world.entity.ai.Brain.Packed.EMPTY);
        }
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=60)
    public void incompleteExternalBrainDefersPoiAndNavigationWithoutCrash(GameTestHelper h){floor(h);var v=new PartialBrainVillager(h.getLevel());v.snapTo(pos(h,5,8),0,0);v.setPersistenceRequired();h.getLevel().addFreshEntity(v);
        v.getBrain().setMemory(MemoryModuleType.JOB_SITE,global(h,28,8));
        h.runAtTickTime(40,()->{h.assertTrue(v.tickCount>=35&&v.getBrain().isMemoryValue(MemoryModuleType.JOB_SITE,global(h,28,8)),"Missing movement slots do not crash or clear external memory");h.assertTrue(ControlPolicy.query(v,h.getLevel(),ControlPolicy.Scope.POI)==ControlPolicy.Decision.UNKNOWN_STATE&&ControlPolicy.query(v,h.getLevel(),ControlPolicy.Scope.NAVIGATION)==ControlPolicy.Decision.UNKNOWN_STATE&&!VillagerState.hasRuntime(v),"Both dependent components yield to incomplete Brain");v.discard();h.succeed();});}

    @GameTest(environment=ENV,structure=ARENA,maxTicks=100)
    public void actualBlockRemovalClearsConfirmedLoadedJobAndBed(GameTestHelper h){floor(h);job(h,28,8);bed(h,28,10);var v=villager(h,5,8);stationary(v);bind(h,v,MemoryModuleType.JOB_SITE,28,8);bind(h,v,MemoryModuleType.HOME,28,10);
        h.runAtTickTime(25,()->{h.assertTrue(v.getBrain().hasMemoryValue(MemoryModuleType.JOB_SITE)&&v.getBrain().hasMemoryValue(MemoryModuleType.HOME),"Valid distant bindings retained before block removal");h.setBlock(new BlockPos(28,2,8),Blocks.AIR);h.setBlock(new BlockPos(27,2,10),Blocks.AIR);h.setBlock(new BlockPos(28,2,10),Blocks.AIR);});
        h.runAtTickTime(80,()->{h.assertTrue(!v.getBrain().hasMemoryValue(MemoryModuleType.JOB_SITE)&&!v.getBrain().hasMemoryValue(MemoryModuleType.HOME)&&VillagerState.runtime(v).invalidPois>=2,"Actual block and POI removal are confirmed before both memories clear");h.assertTrue(v.getVillagerXp()==20&&v.getVillagerData().profession().is(VillagerProfession.FLETCHER),"Removal keeps locked profession and XP");finish(h,v,"actual_job_bed_removal");});}
}
