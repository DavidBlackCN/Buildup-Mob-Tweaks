package com.davidblackcn.buildupmobtweaks;
import com.davidblackcn.buildupmobtweaks.villager.profession.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.npc.villager.*;
import net.minecraft.world.entity.ai.memory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import static com.davidblackcn.buildupmobtweaks.FarmerIntelligenceTests.*;
public final class ProfessionBoundaryTests implements ModInitializer {
 public record Settings(ProfessionResourceTests.Settings resources,boolean enabled,boolean shepherd,boolean shearing){}
 public record Environment(int mode) implements TestEnvironmentDefinition<Settings>{
  static final MapCodec<Environment> CODEC=RecordCodecBuilder.mapCodec(i->i.group(Codec.INT.fieldOf("mode").forGetter(Environment::mode)).apply(i,Environment::new));
  public Settings setup(ServerLevel l){var old=new ProfessionResourceTests.Environment().setup(l);var c=BuildupMobTweaks.config().villagers.professions;var out=new Settings(old,c.enabled.get(),c.profession_switches.shepherd.get(),c.shearing.get());if(mode==0)c.enabled.accept(false);if(mode==1)c.profession_switches.shepherd.accept(false);if(mode==2)l.clockManager().setTotalTicks(l.registryAccess().lookupOrThrow(Registries.WORLD_CLOCK).getOrThrow(net.minecraft.world.clock.WorldClocks.OVERWORLD),13000);if(mode==3)l.getServer().setDifficulty(net.minecraft.world.Difficulty.HARD,true);if(mode==4)c.shearing.accept(false);return out;}
  public void teardown(ServerLevel l,Settings o){var c=BuildupMobTweaks.config().villagers.professions;c.enabled.accept(o.enabled());c.profession_switches.shepherd.accept(o.shepherd());c.shearing.accept(o.shearing());if(mode==3)l.getServer().setDifficulty(o.resources().core().farmer().difficulty(),true);new ProfessionResourceTests.Environment().teardown(l,o.resources());}
  public MapCodec<Environment> codec(){return CODEC;}
 }
 public void onInitialize(){Registry.register(BuiltInRegistries.TEST_ENVIRONMENT_DEFINITION_TYPE,BuildupMobTweaks.id("profession_boundary_test"),Environment.CODEC);}
 static void noShear(GameTestHelper h,String reason){var v=ProfessionResourceTests.worker(h,VillagerProfession.SHEPHERD,Blocks.LOOM);v.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new ItemStack(Items.SHEARS));var s=ProfessionResourceTests.sheep(h,false);v.getBrain().updateActivityFromSchedule(h.getLevel().environmentAttributes(),h.getLevel().getGameTime(),v.position());h.runAtTickTime(600,()->{h.assertTrue(!s.isSheared()&&ProfessionResourceTests.ops(v)==0&&v.getMainHandItem().getDamageValue()==0,"Disabled or native non-work schedule preserves resources: "+reason);v.discard();h.succeed();});}
 @GameTest(environment="buildupmobtweaks:profession_disabled",structure=ARENA,maxTicks=700)public void masterOffRetainsNativeWorkAndResources(GameTestHelper h){noShear(h,"master");}
 @GameTest(environment="buildupmobtweaks:profession_disabled_one",structure=ARENA,maxTicks=700)public void professionSwitchDoesNotGrantResourceWork(GameTestHelper h){noShear(h,"profession");}
 @GameTest(environment="buildupmobtweaks:profession_shearing_off",structure=ARENA,maxTicks=700)public void resourceFeatureSwitchKeepsShearsAndSheep(GameTestHelper h){noShear(h,"activity flag");}
 @GameTest(environment="buildupmobtweaks:profession_night",structure=ARENA,maxTicks=700)public void nativeNightSchedulePreventsProfessionalResourceSettlement(GameTestHelper h){noShear(h,"night");}
 @GameTest(environment="buildupmobtweaks:profession_conversion",structure=ARENA,maxTicks=1000)
 public void paidMealCooldownSurvivesNativeZombificationAndCure(GameTestHelper h){var v=ProfessionResourceTests.worker(h,VillagerProfession.LIBRARIAN,Blocks.LECTERN);v.setHealth(14);v.getInventory().setItem(0,new ItemStack(Items.BREAD));net.minecraft.nbt.CompoundTag[] raw={null};boolean[] changed={false};h.onEachTick(()->{if(!changed[0]&&ProfessionResourceTests.ops(v)==1){changed[0]=true;raw[0]=v.getAttached(ProfessionWorkState.DATA).copy();var z=h.spawn(EntityTypes.ZOMBIE,new BlockPos(11,2,8));z.setNoAi(true);h.assertTrue(z.convertVillagerToZombieVillager(h.getLevel(),v),"Native HARD zombification");z.discard();var converted=h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.monster.zombie.ZombieVillager.class,v.getBoundingBox().inflate(6)).getFirst();h.assertTrue(raw[0].equals(converted.getAttached(ProfessionWorkState.DATA)),"Action cooldown copied, no inventory snapshot refund");converted.addTag("test_farmer_cure_inventory");converted.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WEAKNESS,200));var p=h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new ItemStack(Items.GOLDEN_APPLE));converted.mobInteract(p,net.minecraft.world.InteractionHand.MAIN_HAND);h.assertTrue(converted.isConverting(),"Actual apple cure starts");converted.setVillagerConversionTime(1);}});h.succeedWhen(()->{h.assertTrue(changed[0]&&raw[0]!=null,"Native conversion has started");var available=h.getLevel().getEntitiesOfClass(Villager.class,v.getBoundingBox().inflate(12));h.assertTrue(!available.isEmpty(),"Native cure completed before checking conservation");var cured=available.getFirst();h.assertTrue(changed[0]&&raw[0].equals(cured.getAttached(ProfessionWorkState.DATA))&&cured.getInventory().countItem(Items.BREAD)==0&&ProfessionResourceTests.ops(cured)==0,"Paid food cannot be duplicated or replayed across conversion");cured.discard();h.succeed();});}
}
