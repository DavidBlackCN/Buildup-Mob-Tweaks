package com.davidblackcn.buildupmobtweaks;

import com.davidblackcn.buildupmobtweaks.villager.farmer.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.npc.villager.*;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.*;
import net.minecraft.world.entity.ai.village.poi.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.nbt.CompoundTag;
import com.mojang.serialization.MapCodec;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import java.util.*;

/** Real Brain work slots and inventory/world operations. No direct activity start/tick calls. */
public final class FarmerIntelligenceTests implements ModInitializer {
    static final String ENV="buildupmobtweaks:farmer",OFF="buildupmobtweaks:farmer_off",NO_GRIEF="buildupmobtweaks:farmer_no_grief",ARENA="buildupmobtweaks:r2a_arena";
    static Block TEST_CROP;static Item TEST_SEED;
    static final class TestCrop extends CropBlock {TestCrop(Properties props){super(props);}}
    @Override public void onInitialize(){
        var blockId=Identifier.fromNamespaceAndPath("buildupmobtweaks-test","test_crop");
        TEST_CROP=Registry.register(BuiltInRegistries.BLOCK,blockId,new TestCrop(Block.Properties.of().noCollision().randomTicks().instabreak().noLootTable().setId(ResourceKey.create(Registries.BLOCK,blockId))));
        var seedId=Identifier.fromNamespaceAndPath("buildupmobtweaks-test","test_seed");
        TEST_SEED=Registry.register(BuiltInRegistries.ITEM,seedId,new BlockItem(TEST_CROP,new Item.Properties().setId(ResourceKey.create(Registries.ITEM,seedId))));
        Registry.register(BuiltInRegistries.TEST_ENVIRONMENT_DEFINITION_TYPE,BuildupMobTweaks.id("farmer_test"),Environment.CODEC);
        net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.MOB_CONVERSION.register((before,after,keep)->{if(before.entityTags().contains("test_farmer_cure_inventory")&&after instanceof Villager cured)cured.setCanPickUpLoot(false);});
        InteractionPolicy.register(BuildupMobTweaks.id("test_protection"),(l,v,p,op)->{
            var tags=v.entityTags();if(tags.contains("test_deny_all"))return InteractionPolicy.Decision.DENY;
            if(op==InteractionPolicy.Operation.HARVEST&&tags.contains("test_deny_harvest"))return InteractionPolicy.Decision.DENY;
            if(op==InteractionPolicy.Operation.REPLANT&&(tags.contains("test_deny_replant")||tags.contains("test_deny_after_harvest")&&l.getBlockState(p).isAir()))return InteractionPolicy.Decision.DENY;
            return InteractionPolicy.Decision.ALLOW;
        });
    }
    public record Settings(boolean core,boolean farmer,int interval,int budget,int cooldown,long clock,boolean grief,net.minecraft.world.Difficulty difficulty){}
    public record Environment(boolean enabled,boolean grief,boolean hard) implements TestEnvironmentDefinition<Settings>{
        static final MapCodec<Environment> CODEC=com.mojang.serialization.codecs.RecordCodecBuilder.mapCodec(i->i.group(
            com.mojang.serialization.Codec.BOOL.fieldOf("enabled").forGetter(Environment::enabled),com.mojang.serialization.Codec.BOOL.optionalFieldOf("grief",true).forGetter(Environment::grief),
            com.mojang.serialization.Codec.BOOL.optionalFieldOf("hard",false).forGetter(Environment::hard)).apply(i,Environment::new));
        public Settings setup(ServerLevel l){var c=BuildupMobTweaks.config().villagers;var f=c.farmer;var old=new Settings(c.enabled.get(),f.enabled.get(),f.scan_interval.get(),f.scan_budget.get(),f.action_interval.get(),l.getOverworldClockTime(),l.getGameRules().get(GameRules.MOB_GRIEFING),l.getDifficulty());
            if(hard)l.getServer().setDifficulty(net.minecraft.world.Difficulty.HARD,true);c.enabled.accept(true);f.enabled.accept(enabled);f.scan_interval.accept(20);f.scan_budget.accept(128);f.action_interval.accept(20);l.getGameRules().set(GameRules.MOB_GRIEFING,grief,l.getServer());l.clockManager().setTotalTicks(l.registryAccess().lookupOrThrow(Registries.WORLD_CLOCK).getOrThrow(net.minecraft.world.clock.WorldClocks.OVERWORLD),6000);return old;}
        public void teardown(ServerLevel l,Settings old){var c=BuildupMobTweaks.config().villagers;var f=c.farmer;if(hard)l.getServer().setDifficulty(old.difficulty(),true);c.enabled.accept(old.core());f.enabled.accept(old.farmer());f.scan_interval.accept(old.interval());f.scan_budget.accept(old.budget());f.action_interval.accept(old.cooldown());l.getGameRules().set(GameRules.MOB_GRIEFING,old.grief(),l.getServer());l.clockManager().setTotalTicks(l.registryAccess().lookupOrThrow(Registries.WORLD_CLOCK).getOrThrow(net.minecraft.world.clock.WorldClocks.OVERWORLD),old.clock());}
        public MapCodec<Environment> codec(){return CODEC;}
    }
    static void floor(GameTestHelper h){SkeletonBehaviorTests.floor(h,false);}
    static BlockPos p(GameTestHelper h,int x,int z){return h.absolutePos(new BlockPos(x,2,z));}
    static Villager farmer(GameTestHelper h,int x,int z,boolean full){return farmer(h,x,z,full,EntityTypes.VILLAGER.create(h.getLevel(),EntitySpawnReason.COMMAND));}
    static Villager farmer(GameTestHelper h,int x,int z,boolean full,Villager v){var pos=p(h,x,z);v.snapTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5,0,0);v.setPersistenceRequired();
        v.setVillagerData(v.getVillagerData().withProfession(h.getLevel().registryAccess().lookupOrThrow(Registries.VILLAGER_PROFESSION).getOrThrow(VillagerProfession.FARMER)));v.setVillagerXp(20);
        var job=p(h,8,8);h.getLevel().setBlock(job,Blocks.COMPOSTER.defaultBlockState(),3);h.getLevel().getPoiManager().add(job,PoiTypes.forState(h.getLevel().getBlockState(job)).orElseThrow());h.getLevel().getPoiManager().take(t->t.is(PoiTypes.FARMER),(t,b)->b.equals(job),job,1);
        v.getBrain().setMemory(MemoryModuleType.JOB_SITE,GlobalPos.of(h.getLevel().dimension(),job));v.refreshBrain(h.getLevel());v.getBrain().setActiveActivityIfPossible(Activity.WORK);
        if(!full){v.getBrain().stopAll(h.getLevel(),v);v.getBrain().removeAllBehaviors();v.getBrain().addActivity(Activity.CORE,ImmutableList.of(Pair.of(1,new MoveToTargetSink())),Set.of(),Set.of());
            v.getBrain().addActivity(Activity.WORK,ImmutableList.of(Pair.of(5,new RunOne(ImmutableList.of(Pair.of(new FarmerWorkBehavior(),1))))),Set.of(),Set.of());v.getBrain().setCoreActivities(Set.of(Activity.CORE));v.getBrain().setActiveActivityIfPossible(Activity.WORK);}
        h.getLevel().addFreshEntity(v);return v;
    }
    static void crop(GameTestHelper h,int x,int z,Block block,boolean mature){h.setBlock(new BlockPos(x,1,z),Blocks.FARMLAND.defaultBlockState().setValue(FarmlandBlock.MOISTURE,7));var c=(CropBlock)block;h.setBlock(new BlockPos(x,2,z),c.getStateForAge(mature?c.getMaxAge():0));}
    static int harvests(Villager v){return FarmerState.peek(v).map(s->s.harvests).orElse(0);}static int replants(Villager v){return FarmerState.peek(v).map(s->s.replants).orElse(0);}
    static void proof(String label,Villager v){var r=FarmerState.peek(v);BuildupMobTweaks.LOGGER.info("PHASE3_FARMER_PROOF scenario={} ticks={} owner={} harvested={} replanted={} pending={} inventory={} runtime={}",label,v.tickCount,FarmerModule.owner(v),harvests(v),replants(v),v.getAttached(FarmerState.DATA),v.getInventory().getItems(),r.map(s->"scanned="+s.scanned+" paths="+s.pathRequests+" starts="+s.starts+" stops="+s.stops+" reason="+s.reason).orElse("none"));}
    static void done(GameTestHelper h,Villager v,String label){proof(label,v);v.discard();h.succeed();}
    static void sameCrop(GameTestHelper h,Block crop,Item seed,String label,boolean full){floor(h);crop(h,10,8,crop,true);var v=farmer(h,9,8,full);v.getInventory().setItem(0,new ItemStack(seed,1));
        h.runAtTickTime(full?500:180,()->{h.assertTrue(h.getLevel().getBlockState(p(h,10,8)).is(crop)&&harvests(v)==1&&replants(v)==1,"One mature target harvested once and restored to same crop");h.assertTrue(!FarmerState.pending(v),"Completed replant has no pending work");done(h,v,label);});}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=210) public void wheatRealHarvestAndSameSeedReplant(GameTestHelper h){sameCrop(h,Blocks.WHEAT,Items.WHEAT_SEEDS,"wheat",false);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=210) public void carrotsRealHarvestAndSameReplant(GameTestHelper h){sameCrop(h,Blocks.CARROTS,Items.CARROT,"carrots",false);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=210) public void potatoesRealHarvestAndSameReplant(GameTestHelper h){sameCrop(h,Blocks.POTATOES,Items.POTATO,"potatoes",false);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=210) public void beetrootUsesAgeThreeAndSameReplant(GameTestHelper h){sameCrop(h,Blocks.BEETROOTS,Items.BEETROOT_SEEDS,"beetroot",false);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=550) public void completeNativeWorkPackageHarvestsSingleCrop(GameTestHelper h){sameCrop(h,Blocks.WHEAT,Items.WHEAT_SEEDS,"full_native_work_package",true);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=160) public void noSeedDoesNotHarvestOrGenerateMaterial(GameTestHelper h){floor(h);crop(h,10,8,Blocks.WHEAT,true);var v=farmer(h,9,8,false);h.runAtTickTime(120,()->{h.assertTrue(h.getLevel().getBlockState(p(h,10,8)).getValue(CropBlock.AGE)==7&&harvests(v)==0&&v.getInventory().isEmpty(),"No seed means mature crop untouched, no material generated");done(h,v,"no_seed");});}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=160) public void fullInventoryPausesNewHarvest(GameTestHelper h){floor(h);crop(h,10,8,Blocks.WHEAT,true);var v=farmer(h,9,8,false);for(int i=0;i<v.getInventory().getContainerSize();i++)v.getInventory().setItem(i,new ItemStack(i==0?Items.WHEAT_SEEDS:Items.COBBLESTONE,64));h.runAtTickTime(120,()->{h.assertTrue(harvests(v)==0&&h.getLevel().getBlockState(p(h,10,8)).getValue(CropBlock.AGE)==7,"Full native inventory pauses");done(h,v,"full_inventory");});}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=160) public void immatureUnknownAndEmptyFarmlandRemainUntouched(GameTestHelper h){floor(h);crop(h,10,8,Blocks.WHEAT,false);h.setBlock(new BlockPos(11,1,8),Blocks.FARMLAND);h.setBlock(new BlockPos(12,2,8),Blocks.OAK_SAPLING);var v=farmer(h,9,8,false);v.getInventory().setItem(0,new ItemStack(Items.WHEAT_SEEDS,3));h.runAtTickTime(120,()->{h.assertTrue(harvests(v)==0&&h.getLevel().getBlockState(p(h,11,8)).isAir()&&h.getLevel().getBlockState(p(h,12,8)).is(Blocks.OAK_SAPLING)&&v.getInventory().countItem(Items.WHEAT_SEEDS)==3,"Immature/unknown/undetermined empty field not modified");done(h,v,"no_guessed_planting");});}
    @GameTest(environment=NO_GRIEF,structure=ARENA,maxTicks=160) public void mobGriefingFalseStopsHarvestAndPlant(GameTestHelper h){floor(h);crop(h,10,8,Blocks.WHEAT,true);var v=farmer(h,9,8,false);v.getInventory().setItem(0,new ItemStack(Items.WHEAT_SEEDS,1));h.runAtTickTime(120,()->{h.assertTrue(harvests(v)==0&&replants(v)==0&&v.getInventory().countItem(Items.WHEAT_SEEDS)==1,"Both world operations respect mobGriefing");done(h,v,"mob_griefing_off");});}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=160) public void protectionRejectsHarvestBeforeWorldChanges(GameTestHelper h){floor(h);crop(h,10,8,Blocks.WHEAT,true);var v=farmer(h,9,8,false);v.addTag("test_deny_harvest");v.getInventory().setItem(0,new ItemStack(Items.WHEAT_SEEDS,1));h.runAtTickTime(120,()->{h.assertTrue(harvests(v)==0&&v.getInventory().countItem(Items.WHEAT_SEEDS)==1,"Harvest denied before any consumption");done(h,v,"protection_harvest");});}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=160) public void protectionRejectsPlantDuringPreflight(GameTestHelper h){floor(h);crop(h,10,8,Blocks.WHEAT,true);var v=farmer(h,9,8,false);v.addTag("test_deny_replant");v.getInventory().setItem(0,new ItemStack(Items.WHEAT_SEEDS,1));h.runAtTickTime(120,()->{h.assertTrue(harvests(v)==0&&replants(v)==0,"Known planting denial prevents harvesting");done(h,v,"protection_plant_preflight");});}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=250) public void deniedPostHarvestReplantRetainsSeedAndResumesWithoutAnotherHarvest(GameTestHelper h){floor(h);crop(h,10,8,Blocks.WHEAT,true);var v=farmer(h,9,8,false);v.addTag("test_deny_after_harvest");v.getInventory().setItem(0,new ItemStack(Items.WHEAT_SEEDS,1));
        h.runAtTickTime(100,()->{h.assertTrue(harvests(v)==1&&replants(v)==0&&FarmerState.pending(v)&&v.getInventory().countItem(Items.WHEAT_SEEDS)>=1,"Pending retains original seed after successful harvest and planting denial");v.removeTag("test_deny_after_harvest");});
        h.runAtTickTime(200,()->{h.assertTrue(harvests(v)==1&&replants(v)==1&&!FarmerState.pending(v),"Resume only replants, never harvests twice");done(h,v,"post_harvest_resume");});}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=260) public void playerReplantIsNotOverwritten(GameTestHelper h){floor(h);crop(h,10,8,Blocks.WHEAT,true);var v=farmer(h,9,8,false);v.addTag("test_deny_after_harvest");v.getInventory().setItem(0,new ItemStack(Items.WHEAT_SEEDS,1));h.runAtTickTime(100,()->{h.assertTrue(FarmerState.pending(v),"Actual harvest created pending");crop(h,10,8,Blocks.CARROTS,false);v.removeTag("test_deny_after_harvest");});h.runAtTickTime(200,()->{h.assertTrue(h.getLevel().getBlockState(p(h,10,8)).is(Blocks.CARROTS)&&replants(v)==0&&!FarmerState.pending(v),"Intervening crop remains and no original seed debited");done(h,v,"player_replant");});}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=260) public void removedSubstrateClearsPendingWithoutTerrainRepair(GameTestHelper h){floor(h);crop(h,10,8,Blocks.WHEAT,true);var v=farmer(h,9,8,false);v.addTag("test_deny_after_harvest");v.getInventory().setItem(0,new ItemStack(Items.WHEAT_SEEDS,1));h.runAtTickTime(100,()->{h.assertTrue(FarmerState.pending(v),"Actual pending");h.setBlock(new BlockPos(10,1,8),Blocks.STONE);v.removeTag("test_deny_after_harvest");});h.runAtTickTime(200,()->{h.assertTrue(h.getBlockState(new BlockPos(10,1,8)).is(Blocks.STONE)&&replants(v)==0&&!FarmerState.pending(v),"Invalid field not repaired");done(h,v,"removed_substrate");});}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=220) public void twoFarmersCannotHarvestSameCropTwice(GameTestHelper h){floor(h);crop(h,10,8,Blocks.WHEAT,true);var a=farmer(h,9,8,false);var b=farmer(h,11,8,false);a.getInventory().setItem(0,new ItemStack(Items.WHEAT_SEEDS,1));b.getInventory().setItem(0,new ItemStack(Items.WHEAT_SEEDS,1));h.runAtTickTime(180,()->{h.assertTrue(harvests(a)+harvests(b)==1&&replants(a)+replants(b)==1,"Shared target changes only once");proof("two_farmers_a",a);proof("two_farmers_b",b);a.discard();b.discard();h.succeed();});}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=280) public void mixedFieldKeepsEachOriginalCrop(GameTestHelper h){floor(h);crop(h,10,8,Blocks.WHEAT,true);crop(h,10,10,Blocks.CARROTS,true);var v=farmer(h,9,9,false);v.getInventory().setItem(0,new ItemStack(Items.WHEAT_SEEDS,1));v.getInventory().setItem(1,new ItemStack(Items.CARROT,1));h.runAtTickTime(240,()->{h.assertTrue(h.getLevel().getBlockState(p(h,10,8)).is(Blocks.WHEAT)&&h.getLevel().getBlockState(p(h,10,10)).is(Blocks.CARROTS)&&harvests(v)==2&&replants(v)==2,"No random seed substitution");done(h,v,"mixed_field");});}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=160) public void waterBarrierAndHeightDifferenceAreNotSolvedByJumpOrTerrainChange(GameTestHelper h){floor(h);crop(h,12,8,Blocks.WHEAT,true);for(int z=0;z<28;z++)h.setBlock(new BlockPos(10,2,z),Blocks.WATER);var v=farmer(h,9,8,false);v.getInventory().setItem(0,new ItemStack(Items.WHEAT_SEEDS,1));var start=v.position();h.runAtTickTime(120,()->{h.assertTrue(harvests(v)==0&&v.position().distanceTo(start)<1&&h.getBlockState(new BlockPos(10,2,8)).is(Blocks.WATER),"Unsafe route is skipped, no teleport or terrain edits");done(h,v,"water_route");});}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=180) public void explicitExternalControllerPausesBothFarmingChoices(GameTestHelper h){floor(h);crop(h,10,8,Blocks.WHEAT,true);var v=farmer(h,9,8,true);v.addTag("buildupmobtweaks:farmer_external");v.getInventory().setItem(0,new ItemStack(Items.WHEAT_SEEDS,1));v.getInventory().setItem(1,new ItemStack(Items.BONE_MEAL,1));h.runAtTickTime(140,()->{h.assertTrue(FarmerModule.owner(v)==FarmerModule.Owner.EXTERNAL&&harvests(v)==0&&h.getLevel().getBlockState(p(h,10,8)).getValue(CropBlock.AGE)==7,"Buildup and native agriculture yield; no external AI force-disabled");done(h,v,"external_controller");});}
    @GameTest(environment=OFF,structure=ARENA,maxTicks=160) public void disabledModuleDelegatesOriginalHarvestAndCreatesNoState(GameTestHelper h){floor(h);crop(h,10,8,Blocks.WHEAT,true);var v=farmer(h,10,8,false);v.getBrain().setMemory(MemoryModuleType.SECONDARY_JOB_SITE,List.of(GlobalPos.of(h.getLevel().dimension(),p(h,10,8))));v.getInventory().setItem(0,new ItemStack(Items.WHEAT_SEEDS,1));h.runAtTickTime(120,()->{h.assertTrue(FarmerModule.owner(v)==FarmerModule.Owner.VANILLA&&!v.hasAttached(FarmerState.DATA)&&FarmerState.peek(v).isEmpty(),"Default-off keeps original farmer task and no custom state");h.assertTrue((h.getLevel().getBlockState(p(h,10,8)).isAir()||!((CropBlock)Blocks.WHEAT).isMaxAge(h.getLevel().getBlockState(p(h,10,8)))),"Original harvested or replanted its nearby crop");v.discard();h.succeed();});}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=220) public void unknownDataVersionPausesWithoutOverwriting(GameTestHelper h){floor(h);crop(h,10,8,Blocks.WHEAT,true);var v=farmer(h,9,8,false);var raw=new CompoundTag();raw.putInt("version",99);raw.putString("opaque","future");v.setAttached(FarmerState.DATA,raw);v.getInventory().setItem(0,new ItemStack(Items.WHEAT_SEEDS,1));h.runAtTickTime(120,()->{h.assertTrue(raw.equals(v.getAttached(FarmerState.DATA))&&harvests(v)==0&&replants(v)==0,"Unknown version retained with agriculture paused");v.discard();h.succeed();});}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=30) public void fourResourceProfilesAndStrictSchema(GameTestHelper h){var snapshot=CropProfiles.get(h.getLevel().getServer());h.assertTrue(snapshot.rules().size()==5&&snapshot.rejected()==0,"Four valid resource profiles loaded");var c=snapshot.byCrop().get(Blocks.BEETROOTS);h.assertTrue(c.matureAge()==3&&c.count()==1,"Beetroot has correct native age and material");
        for(var raw:List.of(new com.google.gson.JsonObject(),com.google.gson.JsonParser.parseString("{\"version\":99}").getAsJsonObject())){boolean rejected=false;try{CropProfiles.decode(BuildupMobTweaks.id("invalid"),raw);}catch(IllegalArgumentException e){rejected=true;}h.assertTrue(rejected,"Malformed/unknown schema rejected");}h.succeed();}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=1000) public void actualEntityReloadRetainsPendingAndDoesNotReplayHarvest(GameTestHelper h){floor(h);crop(h,10,8,Blocks.WHEAT,true);Villager[] ref={farmer(h,9,8,false)};ref[0].addTag("test_deny_after_harvest");ref[0].getInventory().setItem(0,new ItemStack(Items.WHEAT_SEEDS,1));
        h.runAtTickTime(100,()->{h.assertTrue(harvests(ref[0])==1&&FarmerState.pending(ref[0]),"Real harvest before serialization");var raw=ref[0].getAttached(FarmerState.DATA).copy();ref[0]=CombatTestWorld.reload(h,ref[0]);h.assertTrue(raw.equals(ref[0].getAttached(FarmerState.DATA)),"Pending round trips through native serializer");ref[0].removeTag("test_deny_after_harvest");});
        var original=ref[0];h.onEachTick(()->{if(ref[0]!=original)h.assertTrue(harvests(ref[0])==0,"Loaded runtime never replays the completed harvest");});
        h.succeedWhen(()->{h.assertTrue(replants(ref[0])==1&&harvests(ref[0])==0&&!FarmerState.pending(ref[0]),"Loaded runtime only replants; old harvest not repeated");done(h,ref[0],"pending_entity_reload");});}

    @GameTest(environment=ENV,structure=ARENA,maxTicks=210)
    public void registeredModCropUsesProfileAndConsumesExactlyOneExistingSeed(GameTestHelper h){floor(h);crop(h,10,8,TEST_CROP,true);var v=farmer(h,9,8,false);v.getInventory().setItem(0,new ItemStack(TEST_SEED,1));
        h.runAtTickTime(170,()->{h.assertTrue(harvests(v)==1&&replants(v)==1&&h.getLevel().getBlockState(p(h,10,8)).is(TEST_CROP),"Non-Minecraft registered age crop participates through its resource profile");h.assertTrue(v.getInventory().countItem(TEST_SEED)==0,"No-loot fixture proves one real seed consumed exactly once");done(h,v,"mod_crop_exact_seed");});}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=160)
    public void removingSeedDuringApproachDoesNotCreateReplacement(GameTestHelper h){floor(h);crop(h,14,8,Blocks.WHEAT,true);var v=farmer(h,9,8,false);v.getInventory().setItem(0,new ItemStack(Items.WHEAT_SEEDS,1));boolean[] removed={false};
        h.onEachTick(()->{if(!removed[0]&&FarmerState.peek(v).map(s->s.starts>0).orElse(false)&&harvests(v)==0){removed[0]=true;v.getInventory().removeItemType(Items.WHEAT_SEEDS,1);}});
        h.runAtTickTime(120,()->{h.assertTrue(removed[0]&&harvests(v)==0&&replants(v)==0&&v.getInventory().countItem(Items.WHEAT_SEEDS)==0,"Changed inventory is rechecked at target without free seed");done(h,v,"seed_replaced_approach");});}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=160)
    public void tradeInterruptsOwnedWorkBeforeHarvest(GameTestHelper h){floor(h);crop(h,14,8,Blocks.WHEAT,true);var v=farmer(h,9,8,false);v.getInventory().setItem(0,new ItemStack(Items.WHEAT_SEEDS,1));boolean[] traded={false};var player=h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);v.getOffers();
        h.onEachTick(()->{if(!traded[0]&&FarmerState.peek(v).map(s->s.starts>0).orElse(false)&&harvests(v)==0){traded[0]=true;v.setTradingPlayer(player);}});
        h.runAtTickTime(100,()->{h.assertTrue(traded[0]&&harvests(v)==0&&FarmerState.peek(v).map(s->s.stops>0).orElse(false),"Trade ends work and preserves unharvested target");v.setTradingPlayer(null);done(h,v,"trade_interruption");});}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=160)
    public void noAiStopsActivityAndReleasesOnlyOwnedMemories(GameTestHelper h){floor(h);crop(h,14,8,Blocks.WHEAT,true);var v=farmer(h,9,8,false);v.getInventory().setItem(0,new ItemStack(Items.WHEAT_SEEDS,1));boolean[] stopped={false};
        h.onEachTick(()->{if(!stopped[0]&&FarmerState.peek(v).map(s->s.starts>0).orElse(false)&&harvests(v)==0){stopped[0]=true;v.setNoAi(true);}});
        h.runAtTickTime(100,()->{h.assertTrue(stopped[0]&&harvests(v)==0&&FarmerState.peek(v).map(s->s.stops>0).orElse(false)&&!v.getBrain().hasMemoryValue(MemoryModuleType.WALK_TARGET),"NoAI boundary runs outside customServerAiStep and releases owned target");done(h,v,"noai_cleanup");});}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=60)
    public void malformedRulesAndSameCropAmbiguityAreRejected(GameTestHelper h){
        var json=com.google.gson.JsonParser.parseString("{\"version\":1,\"crop\":\"minecraft:wheat\",\"maturity\":{\"type\":\"age\",\"property\":\"age\",\"value\":7},\"harvest_handler\":\"buildupmobtweaks:break_and_replant\",\"replant\":{\"item\":\"minecraft:wheat_seeds\",\"count\":1,\"block\":\"minecraft:wheat\",\"state\":{\"age\":\"0\"},\"substrate\":\"minecraft:farmland\"}}").getAsJsonObject();
        var id=BuildupMobTweaks.id("a");var decoded=CropProfiles.decode(id,json);h.assertTrue(decoded.crop()==Blocks.WHEAT,"Valid strict schema");
        var ambiguous=CropProfiles.resolve(List.of(new CropProfiles.Raw(id,json,"test"),new CropProfiles.Raw(BuildupMobTweaks.id("b"),json,"test")));h.assertTrue(ambiguous.rules().isEmpty()&&ambiguous.rejected()==2,"Same-crop conflict pauses all rules");
        var bad=json.deepCopy();bad.getAsJsonObject("replant").addProperty("count",1.5);boolean rejected=false;try{CropProfiles.decode(id,bad);}catch(IllegalArgumentException e){rejected=true;}h.assertTrue(rejected,"Fractional seed count rejected");
        bad=json.deepCopy();bad.addProperty("version","1");rejected=false;try{CropProfiles.decode(id,bad);}catch(IllegalArgumentException e){rejected=true;}h.assertTrue(rejected,"String version rejected");
        var changed=json.deepCopy();changed.getAsJsonObject("replant").addProperty("count",2);h.assertFalse(decoded.signature().equals(CropProfiles.decode(id,changed).signature()),"Changed material rule has different recovery signature");h.succeed();}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=30)
    public void duplicateHandlerAndProtectionRegistrationDoNotReplaceExistingCode(GameTestHelper h){var id=BuildupMobTweaks.id("break_and_replant");var handler=CropHandlers.find(id).orElseThrow();boolean rejected=false;try{CropHandlers.register(id,handler);}catch(IllegalArgumentException e){rejected=true;}h.assertTrue(rejected&&CropHandlers.find(id).orElseThrow()==handler,"Duplicate handler rejected, original remains");
        rejected=false;try{InteractionPolicy.register(BuildupMobTweaks.id("test_protection"),(l,v,p,o)->InteractionPolicy.Decision.DENY);}catch(IllegalArgumentException e){rejected=true;}h.assertTrue(rejected,"Duplicate policy rejected");h.succeed();}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=240)
    public void shutdownThenRestorePendingDoesNotRepeatHarvest(GameTestHelper h){floor(h);crop(h,10,8,Blocks.WHEAT,true);var v=farmer(h,9,8,false);v.addTag("test_deny_after_harvest");v.getInventory().setItem(0,new ItemStack(Items.WHEAT_SEEDS,1));
        h.runAtTickTime(80,()->{h.assertTrue(FarmerState.pending(v),"Actual pending before disable");v.addTag("buildupmobtweaks:disable_farmer_intelligence");v.setNoAi(true);});
        h.runAtTickTime(120,()->{h.assertTrue(FarmerState.pending(v)&&harvests(v)==1,"Disable/NoAI preserves record without material transfer");v.removeTag("buildupmobtweaks:disable_farmer_intelligence");v.removeTag("test_deny_after_harvest");v.setNoAi(false);});
        h.runAtTickTime(200,()->{h.assertTrue(harvests(v)==1&&replants(v)==1&&!FarmerState.pending(v),"Reenable resumes only replant");done(h,v,"pending_off_on");});}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=100)
    public void foreignWalkTargetIsNotErasedDuringOwnCleanup(GameTestHelper h){floor(h);crop(h,14,8,Blocks.WHEAT,true);var v=farmer(h,9,8,false);v.getInventory().setItem(0,new ItemStack(Items.WHEAT_SEEDS,1));WalkTarget[] foreign={null};
        h.onEachTick(()->{if(foreign[0]==null&&FarmerState.peek(v).map(s->s.starts>0).orElse(false)&&harvests(v)==0){foreign[0]=new WalkTarget(p(h,20,8),0.5F,0);v.getBrain().setMemory(MemoryModuleType.WALK_TARGET,foreign[0]);}
            if(foreign[0]!=null&&FarmerState.peek(v).map(s->s.stops>0).orElse(false)){h.assertTrue(v.getBrain().getMemory(MemoryModuleType.WALK_TARGET).orElse(null)==foreign[0],"Cleanup leaves later foreign target intact");done(h,v,"foreign_walk_cleanup");}});}

    static final class BrainChangingFarmer extends Villager {
        BrainChangingFarmer(ServerLevel l){super(EntityTypes.VILLAGER,l);}
        void externalBrain(){this.brain=net.minecraft.world.entity.ai.Brain.<Villager>provider(List.of(MemoryModuleType.JOB_SITE),List.of(),v->List.of()).makeBrain(this,net.minecraft.world.entity.ai.Brain.Packed.EMPTY);}
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=140)
    public void externalBrainReplacementStopsOwnedActivityWithoutMissingMemoryCrash(GameTestHelper h){floor(h);crop(h,14,8,Blocks.WHEAT,true);var v=new BrainChangingFarmer(h.getLevel());farmer(h,9,8,false,v);v.getInventory().setItem(0,new ItemStack(Items.WHEAT_SEEDS,1));boolean[] replaced={false};
        h.onEachTick(()->{if(!replaced[0]&&FarmerState.peek(v).map(s->s.starts>0).orElse(false)&&harvests(v)==0){replaced[0]=true;v.externalBrain();}});
        h.runAtTickTime(100,()->{h.assertTrue(replaced[0]&&harvests(v)==0&&replants(v)==0&&FarmerState.peek(v).map(s->s.stops>0).orElse(false)&&v.isAlive(),"Replacing the whole Brain with missing slots ends own work safely");done(h,v,"external_brain_replaced");});}

    @GameTest(environment="buildupmobtweaks:farmer_conversion",structure=ARENA,maxTicks=280)
    public void pendingSurvivesNativeZombificationAndGoldenAppleCureWithoutMaterialRefund(GameTestHelper h){floor(h);crop(h,10,8,Blocks.WHEAT,true);var v=farmer(h,9,8,false);v.addTag("test_deny_after_harvest");v.getInventory().setItem(0,new ItemStack(Items.WHEAT_SEEDS,1));CompoundTag[] saved={null};
        h.runAtTickTime(100,()->{h.assertTrue(FarmerState.pending(v)&&harvests(v)==1,"Real harvest created pending before conversion");saved[0]=v.getAttached(FarmerState.DATA).copy();var zombie=h.spawn(EntityTypes.ZOMBIE,new BlockPos(12,2,8));zombie.setNoAi(true);
            h.assertTrue(zombie.convertVillagerToZombieVillager(h.getLevel(),v),"Native HARD zombification succeeds");zombie.discard();var converted=h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.monster.zombie.ZombieVillager.class,v.getBoundingBox().inflate(6)).getFirst();
            h.assertTrue(converted.getAttached(FarmerState.DATA).equals(saved[0]),"Only own raw work state carries through zombification");converted.addTag("test_farmer_cure_inventory");converted.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WEAKNESS,200));var player=h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new ItemStack(Items.GOLDEN_APPLE));converted.mobInteract(player,net.minecraft.world.InteractionHand.MAIN_HAND);h.assertTrue(converted.isConverting(),"Real golden apple starts cure");converted.setVillagerConversionTime(1);
        });
        h.runAtTickTime(220,()->{var cured=h.getLevel().getEntitiesOfClass(Villager.class,v.getBoundingBox().inflate(12)).getFirst();h.assertTrue(cured.getAttached(FarmerState.DATA).equals(saved[0])&&FarmerState.pending(cured),"Pending raw state remains identical after native cure");h.assertTrue(cured.getInventory().isEmpty()&&harvests(cured)==0&&replants(cured)==0&&h.getBlockState(new BlockPos(10,2,8)).isAir(),"Native inventory loss is not refunded, and harvesting is never replayed");done(h,cured,"native_farmer_conversion");});}

    static final class RestockObservingFarmer extends Villager {
        int nativeRestocks;RestockObservingFarmer(ServerLevel l){super(EntityTypes.VILLAGER,l);}
        @Override public void restock(){super.restock();nativeRestocks++;}
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=1850)
    public void nativeComposterWorkStillRestocksUsedOffersWithFarmerEnabled(GameTestHelper h){floor(h);crop(h,10,8,Blocks.WHEAT,true);var v=new RestockObservingFarmer(h.getLevel());farmer(h,9,8,true,v);v.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);v.getInventory().setItem(0,new ItemStack(Items.WHEAT_SEEDS,1));
        var offers=new net.minecraft.world.item.trading.MerchantOffers();var offer=new net.minecraft.world.item.trading.MerchantOffer(new net.minecraft.world.item.trading.ItemCost(Items.WHEAT,20),new ItemStack(Items.EMERALD,1),8,2,0.05F);for(int i=0;i<8;i++)offer.increaseUses();offers.add(offer);v.setOffers(offers);boolean[] stocked={false},reusedAfterDayCatchup={false};
        h.onEachTick(()->{var current=v.getOffers().getFirst();if(current.getUses()==0&&v.nativeRestocks==0&&!reusedAfterDayCatchup[0]){for(int i=0;i<8;i++)current.increaseUses();reusedAfterDayCatchup[0]=true;}stocked[0]|=current.getUses()==0&&v.nativeRestocks>0;});
        h.runAtTickTime(1800,()->{proof("restock_observation",v);BuildupMobTweaks.LOGGER.info("FARMER_RESTOCK_OBSERVATION stocked={} uses={} nativeRestocks={} job={} activities={} position={}",stocked[0],v.getOffers().getFirst().getUses(),v.nativeRestocks,v.getBrain().getMemory(MemoryModuleType.JOB_SITE),v.getBrain().getActiveActivities(),v.position());h.assertTrue(stocked[0]&&v.nativeRestocks>0,"Original WorkAtComposter actually runs and restocks without direct restock calls");h.assertTrue(v.getVillagerXp()==20&&v.getVillagerData().profession().is(VillagerProfession.FARMER)&&v.getOffers().size()==1,"Native profession and trade list remain");done(h,v,"native_farmer_restock");});}
}
