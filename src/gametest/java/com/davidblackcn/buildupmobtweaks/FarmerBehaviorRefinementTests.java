package com.davidblackcn.buildupmobtweaks;

import com.davidblackcn.buildupmobtweaks.villager.farmer.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.pathfinder.Path;
import com.mojang.serialization.MapCodec;
import static com.davidblackcn.buildupmobtweaks.FarmerIntelligenceTests.*;

/** Actual server physics/Brain work; never calls FarmerActivity or a block fall hook directly. */
public final class FarmerBehaviorRefinementTests implements ModInitializer {
    static final String REFINE="buildupmobtweaks:farmer_refinement";
    public record Environment() implements TestEnvironmentDefinition<FarmerIntelligenceTests.Settings>{
        static final MapCodec<Environment> CODEC=MapCodec.unit(Environment::new);
        public FarmerIntelligenceTests.Settings setup(ServerLevel l){var old=new FarmerIntelligenceTests.Environment(true,true,false).setup(l);BuildupMobTweaks.config().villagers.farmer.scan_budget.accept(256);return old;}
        public void teardown(ServerLevel l,FarmerIntelligenceTests.Settings old){new FarmerIntelligenceTests.Environment(true,true,false).teardown(l,old);}
        public MapCodec<Environment> codec(){return CODEC;}
    }
    public void onInitialize(){Registry.register(BuiltInRegistries.TEST_ENVIRONMENT_DEFINITION_TYPE,BuildupMobTweaks.id("farmer_refinement_test"),Environment.CODEC);
        var watched=new java.util.LinkedHashMap<java.util.UUID,Villager>();
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents.ENTITY_LOAD.register((e,l)->{if(e instanceof Villager v&&FarmerState.pending(v)&&watched.size()<8)watched.put(v.getUUID(),v);});
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents.ENTITY_UNLOAD.register((e,l)->watched.remove(e.getUUID()));
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server->{for(var v:watched.values())if(v.tickCount%20==0&&v.tickCount<=240){
            BuildupMobTweaks.LOGGER.info("PHASE3_A25_RELOAD_TRACE uuid={} age={} owner={} eligible={} pos={} ground={} activities={} inventory={} data={} runtime={}",v.getUUID(),v.tickCount,FarmerModule.owner(v),FarmerModule.eligible(v,(ServerLevel)v.level()),v.position(),v.onGround(),v.getBrain().getActiveActivities(),v.getInventory().getItems(),v.getAttached(FarmerState.DATA),FarmerState.peek(v).map(r->r.reason+" starts="+r.starts+" harvests="+r.harvests+" replants="+r.replants).orElse("none"));
        }});
    }
    static void seed(Villager v,int count){v.getInventory().setItem(0,new ItemStack(TEST_SEED,count));}
    static void proof25(String scene,Villager v){proof(scene,v);BuildupMobTweaks.LOGGER.info("PHASE3_A25_PROOF scene={} pos={} health={} seeds={} tramples={} landing={}",scene,v.position(),v.getHealth(),v.getInventory().countItem(TEST_SEED),FarmerState.peek(v).map(s->s.protectedTramples).orElse(0),FarmerState.peek(v).map(s->s.landingDecision).orElse("unobserved"));}
    static void finish(GameTestHelper h,Villager v,String scene){proof25(scene,v);v.discard();h.succeed();}
    static void testCrop(GameTestHelper h,int x,int y,int z){h.setBlock(new BlockPos(x,y-1,z),Blocks.FARMLAND.defaultBlockState().setValue(FarmlandBlock.MOISTURE,7));h.setBlock(new BlockPos(x,y,z),((CropBlock)TEST_CROP).getStateForAge(7));}
    static void noDuplication(GameTestHelper h,Villager v,BlockPos target){h.assertTrue(harvests(v)==1&&replants(v)==1&&v.getInventory().countItem(TEST_SEED)==0&&!FarmerState.pending(v)&&h.getLevel().getBlockState(target).is(TEST_CROP),"One native harvest/replant and one real seed, no cloned drops or replay");}
    static void platform(GameTestHelper h,boolean stairs){
        for(int x=12;x<=17;x++)for(int z=6;z<=10;z++)h.setBlock(new BlockPos(x,2,z),Blocks.STONE);
        if(stairs)h.setBlock(new BlockPos(11,2,8),Blocks.STONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.EAST));
    }
    @GameTest(environment=REFINE,structure=ARENA,maxTicks=720)
    public void flatFieldPrefersDryLaneAndPreservesNonTargetCrops(GameTestHelper h){floor(h);for(int x=10;x<=13;x++)for(int z=8;z<=9;z++)crop(h,x,z,Blocks.CARROTS,false);testCrop(h,15,2,8);var v=farmer(h,9,8,false);seed(v,1);boolean[] observed={false};
        h.onEachTick(()->{Path path=v.getNavigation().getPath();if(path!=null){observed[0]=true;for(int i=0;i<path.getNodeCount();i++){var n=path.getNodePos(i);h.assertFalse(h.getLevel().getBlockState(n).is(Blocks.CARROTS),"Safe lane is preferred over the non-target crop strip");}}});
        h.runAtTickTime(650,()->{h.assertTrue(observed[0],"Observed real native navigation");noDuplication(h,v,p(h,15,8));for(int x=10;x<=13;x++)for(int z=8;z<=9;z++)h.assertTrue(h.getBlockState(new BlockPos(x,1,z)).is(Blocks.FARMLAND)&&h.getBlockState(new BlockPos(x,2,z)).is(Blocks.CARROTS),"Non-target farmland and crops preserved");finish(h,v,"flat_dry_lane");});}
    @GameTest(environment=REFINE,structure=ARENA,maxTicks=300)
    public void standingOnFarmlandStillAllowsOrdinaryWork(GameTestHelper h){floor(h);crop(h,9,8,Blocks.CARROTS,false);testCrop(h,10,2,8);var v=farmer(h,9,8,false);seed(v,1);
        h.runAtTickTime(240,()->{noDuplication(h,v,p(h,10,8));h.assertTrue(h.getBlockState(new BlockPos(9,1,8)).is(Blocks.FARMLAND)&&v.getY()<p(h,9,8).getY(),"Farmland contact is allowed and does not block work");finish(h,v,"normal_farmland_standing");});}
    @GameTest(environment=REFINE,structure=ARENA,maxTicks=320)
    public void raisedPlatformWithoutStepsIsSkippedWithoutJumping(GameTestHelper h){floor(h);platform(h,false);testCrop(h,15,3,8);var v=farmer(h,9,8,false);seed(v,1);double[] top={v.getY()};
        h.onEachTick(()->{top[0]=Math.max(top[0],v.getY());});h.runAtTickTime(260,()->{h.assertTrue(harvests(v)==0&&replants(v)==0&&v.getInventory().countItem(TEST_SEED)==1&&top[0]<=p(h,9,8).getY()+0.1,"No jump or teleport onto a one-block platform");h.assertTrue(h.getBlockState(new BlockPos(15,2,8)).is(Blocks.FARMLAND),"Raised farmland remains intact");finish(h,v,"raised_without_steps");});}
    @GameTest(environment=REFINE,structure=ARENA,maxTicks=820)
    public void straightStairsReachRaisedFarmlandWithoutWorkJump(GameTestHelper h){floor(h);platform(h,true);testCrop(h,15,3,8);var v=farmer(h,9,8,false);seed(v,1);double[] high={v.getY()},up={0};
        h.onEachTick(()->{high[0]=Math.max(high[0],v.getY());if(FarmNavigation.ownsRoute(v))up[0]=Math.max(up[0],v.getDeltaMovement().y);});
        h.runAtTickTime(750,()->{proof25("stairs_observation",v);h.assertTrue(high[0]>p(h,9,8).getY()+0.7&&up[0]<0.3,"Native step movement ascends the stairs without a jump impulse");noDuplication(h,v,h.absolutePos(new BlockPos(15,3,8)));h.assertTrue(h.getBlockState(new BlockPos(15,2,8)).is(Blocks.FARMLAND),"Raised target soil preserved");finish(h,v,"stairs_raised_work");});}
    @GameTest(environment=REFINE,structure=ARENA,maxTicks=900)
    public void channelUsesDryCrossingWithoutEnteringWater(GameTestHelper h){floor(h);for(int z=0;z<28;z++)h.setBlock(new BlockPos(11,1,z),Blocks.WATER);h.setBlock(new BlockPos(11,1,5),Blocks.STONE);testCrop(h,15,2,8);var v=farmer(h,9,8,false);seed(v,1);boolean[] crossing={false},wet={false};
        h.onEachTick(()->{wet[0]|=v.isInWater();if(v.getBlockX()==p(h,11,5).getX()){crossing[0]=true;h.assertTrue(v.getBlockZ()==p(h,11,5).getZ(),"Actual crossing uses the loaded dry bridge");}});
        h.runAtTickTime(820,()->{h.assertTrue(crossing[0]&&!wet[0],"Native movement crossed safely, no water jump");noDuplication(h,v,p(h,15,8));h.assertTrue(h.getBlockState(new BlockPos(11,1,8)).is(Blocks.WATER),"Channel not repaired or filled");finish(h,v,"water_dry_crossing");});}
    @GameTest(environment=REFINE,structure=ARENA,maxTicks=450)
    public void sameCropNearbyIsPreferredAfterActualCompletedWork(GameTestHelper h){floor(h);testCrop(h,10,2,8);var v=farmer(h,9,8,false);seed(v,2);v.getInventory().setItem(1,new ItemStack(Items.CARROT,1));boolean[] added={false},observed={false};
        h.onEachTick(()->{if(!added[0]&&replants(v)==1){added[0]=true;testCrop(h,12,2,8);crop(h,10,9,Blocks.CARROTS,true);}
            if(!observed[0]&&replants(v)==2){observed[0]=true;h.assertTrue(h.getBlockState(new BlockPos(10,2,9)).getValue(CropBlock.AGE)==7&&v.getInventory().countItem(TEST_SEED)==0&&v.getInventory().countItem(Items.CARROT)==1,"Same nearby crop completes before the closer different crop, without seed substitution");v.setNoAi(true);}});
        h.runAtTickTime(380,()->{h.assertTrue(observed[0]&&harvests(v)==2,"Observed two real same-crop operations");finish(h,v,"mixed_local_priority");});}
    @GameTest(environment=REFINE,structure=ARENA,maxTicks=500)
    public void routeCostWinsOverStraightDistanceWithinSameCropGroup(GameTestHelper h){floor(h);for(int z=0;z<=11;z++)h.setBlock(new BlockPos(11,1,z),Blocks.WATER);h.setBlock(new BlockPos(11,1,4),Blocks.STONE);testCrop(h,13,2,8);testCrop(h,10,2,12);var v=farmer(h,9,8,false);seed(v,1);boolean[] checked={false};
        h.onEachTick(()->{if(!checked[0]&&replants(v)==1){checked[0]=true;h.assertTrue(h.getBlockState(new BlockPos(13,2,8)).getValue(CropBlock.AGE)==7,"The nearer crop behind a costly channel crossing is not selected first");v.setNoAi(true);}});
        h.runAtTickTime(420,()->{h.assertTrue(checked[0]&&harvests(v)==1&&v.getInventory().countItem(TEST_SEED)==0,"Reachable route cost is used for a bounded same-crop shortlist");finish(h,v,"route_distance_priority");});}
    @GameTest(environment=REFINE,structure=ARENA,maxTicks=520)
    public void twoFarmersShareOneTargetWithoutDuplicatedSeedOrSoilDamage(GameTestHelper h){floor(h);testCrop(h,13,2,8);var a=farmer(h,9,8,false);var b=farmer(h,16,8,false);seed(a,1);seed(b,1);
        h.runAtTickTime(450,()->{h.assertTrue(harvests(a)+harvests(b)==1&&replants(a)+replants(b)==1&&a.getInventory().countItem(TEST_SEED)+b.getInventory().countItem(TEST_SEED)==1,"One target produces exactly one harvest and one seed debit");h.assertTrue(h.getBlockState(new BlockPos(13,1,8)).is(Blocks.FARMLAND),"Target soil survives both routes");proof25("two_farmers_a",a);proof25("two_farmers_b",b);a.discard();b.discard();h.succeed();});}
    @GameTest(environment=REFINE,structure=ARENA,maxTicks=850)
    public void playerLayoutChangeRetiresRouteThenRecoversAfterRepair(GameTestHelper h){floor(h);testCrop(h,16,2,8);var v=farmer(h,9,8,false);seed(v,1);boolean[] changed={false};
        h.onEachTick(()->{if(!changed[0]&&FarmerState.peek(v).map(s->s.starts>0).orElse(false)&&harvests(v)==0){changed[0]=true;for(int z=0;z<28;z++)h.setBlock(new BlockPos(12,1,z),Blocks.WATER);}});
        h.runAtTickTime(260,()->{proof25("layout_blocked",v);h.assertTrue(changed[0]&&harvests(v)==0&&replants(v)==0&&!v.isInWater()&&v.getInventory().countItem(TEST_SEED)==1,"Unsafe changed route is abandoned without harvesting or consuming");for(int z=0;z<28;z++)h.setBlock(new BlockPos(12,1,z),Blocks.STONE);});
        h.runAtTickTime(760,()->{noDuplication(h,v,p(h,16,8));finish(h,v,"layout_recovered");});}
    @GameTest(environment=REFINE,structure=ARENA,maxTicks=450)
    public void playerCropReplacementDuringApproachIsPreserved(GameTestHelper h){floor(h);testCrop(h,15,2,8);var v=farmer(h,9,8,false);seed(v,1);boolean[] changed={false};h.onEachTick(()->{if(!changed[0]&&FarmerState.peek(v).map(s->s.starts>0).orElse(false)){changed[0]=true;crop(h,15,8,Blocks.CARROTS,false);}});
        h.runAtTickTime(380,()->{h.assertTrue(changed[0]&&harvests(v)==0&&replants(v)==0&&v.getInventory().countItem(TEST_SEED)==1&&h.getBlockState(new BlockPos(15,2,8)).is(Blocks.CARROTS),"Arriving farmer cannot overwrite changed crop or clone a seed");finish(h,v,"player_replaced_approach");});}
    static void fall(GameTestHelper h,String tag,boolean protect){floor(h);crop(h,9,8,Blocks.CARROTS,false);var v=farmer(h,9,8,false);if(tag!=null)v.addTag(tag);var pos=p(h,9,8);v.snapTo(pos.getX()+.5,pos.getY()+5,pos.getZ()+.5,0,0);v.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
        h.runAtTickTime(100,()->{proof25("landing_"+protect+"_"+tag,v);h.assertTrue(v.onGround()&&v.getHealth()<v.getMaxHealth(),"Actual fall completes and native fall damage remains");h.assertTrue(h.getBlockState(new BlockPos(9,1,8)).is(protect?Blocks.FARMLAND:Blocks.DIRT),"Only Buildup work landing receives soil protection");if(protect)h.assertTrue(FarmerState.peek(v).map(s->s.protectedTramples>0).orElse(false),"Real farmland conversion hook was prevented");finish(h,v,"landing_boundary");});}
    @GameTest(environment=REFINE,structure=ARENA,maxTicks=140)
    public void workLandingProtectsNonTargetSoilButStillAppliesFallDamage(GameTestHelper h){fall(h,null,true);}
    @GameTest(environment=REFINE,structure=ARENA,maxTicks=140)
    public void disabledFarmerKeepsNativeTrampling(GameTestHelper h){fall(h,"buildupmobtweaks:disable_farmer_intelligence",false);}
    @GameTest(environment=REFINE,structure=ARENA,maxTicks=140)
    public void externalOwnerKeepsNativeTrampling(GameTestHelper h){fall(h,"buildupmobtweaks:farmer_external",false);}
    @GameTest(environment=REFINE,structure=ARENA,maxTicks=140)
    public void nonVillagerTramplingIsNotGloballyDisabled(GameTestHelper h){floor(h);crop(h,9,8,Blocks.CARROTS,false);var pig=EntityTypes.PIG.create(h.getLevel(),EntitySpawnReason.COMMAND);var center=p(h,9,8);pig.snapTo(center.getX()+.5,center.getY()+5,center.getZ()+.5,0,0);pig.setNoGravity(false);pig.setNoAi(false);pig.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED).setBaseValue(0);h.getLevel().addFreshEntity(pig);
        h.runAtTickTime(100,()->{BuildupMobTweaks.LOGGER.info("PHASE3_A25_NONFARMER pos={} health={} soil={} onGround={}",pig.position(),pig.getHealth(),h.getBlockState(new BlockPos(9,1,8)),pig.onGround());h.assertTrue(h.getBlockState(new BlockPos(9,1,8)).is(Blocks.DIRT)&&pig.getHealth()<pig.getMaxHealth(),"Native fall and soil effects remain for non-farmers");pig.discard();h.succeed();});}
    @GameTest(environment=REFINE,structure=ARENA,maxTicks=900)
    public void pendingRaisedCropReloadResumesOnlyReplantWithOneSeed(GameTestHelper h){floor(h);platform(h,true);testCrop(h,15,3,8);Villager[] ref={farmer(h,9,8,false)};seed(ref[0],1);ref[0].addTag("test_deny_after_harvest");boolean[] loaded={false};
        h.onEachTick(()->{if(!loaded[0]&&FarmerState.pending(ref[0])&&harvests(ref[0])==1){var raw=ref[0].getAttached(FarmerState.DATA).copy();ref[0]=CombatTestWorld.reload(h,ref[0]);loaded[0]=true;h.assertTrue(raw.equals(ref[0].getAttached(FarmerState.DATA))&&ref[0].getInventory().countItem(TEST_SEED)==1,"Raw pending and actual seed survive native entity serialization");ref[0].removeTag("test_deny_after_harvest");}});
        h.runAtTickTime(820,()->{proof25("raised_reload_observation",ref[0]);h.assertTrue(loaded[0]&&harvests(ref[0])==0&&replants(ref[0])==1&&ref[0].getInventory().countItem(TEST_SEED)==0&&!FarmerState.pending(ref[0]),"Loaded farmer replants once without replaying old harvest or creating seeds");finish(h,ref[0],"raised_pending_reload");});}

    @GameTest(environment=REFINE,structure=ARENA,maxTicks=150)
    public void tradingPausesWorkButDoesNotRemovePassiveLandingProtection(GameTestHelper h){floor(h);crop(h,9,8,Blocks.CARROTS,false);var v=farmer(h,9,8,false);var player=h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);v.getOffers();v.setTradingPlayer(player);var pos=p(h,9,8);v.snapTo(pos.getX()+.5,pos.getY()+5,pos.getZ()+.5,0,0);
        h.runAtTickTime(100,()->{proof25("trading_landing",v);h.assertTrue(h.getBlockState(new BlockPos(9,1,8)).is(Blocks.FARMLAND)&&v.getHealth()<v.getMaxHealth()&&harvests(v)==0&&replants(v)==0,"Priority yields farming while passive soil protection and native damage remain");h.assertTrue(FarmerState.peek(v).map(r->r.protectedTramples>0&&r.landingDecision.startsWith("VANILLA_PRIORITY")).orElse(false),"Observed a genuine priority-paused landing");v.setTradingPlayer(null);finish(h,v,"trading_soil_safety");});}
}
