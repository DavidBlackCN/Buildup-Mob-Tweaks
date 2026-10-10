package com.davidblackcn.buildupmobtweaks;

import com.davidblackcn.buildupmobtweaks.rebuild.profession.*;
import com.davidblackcn.buildupmobtweaks.rebuild.drowned.DrownedBehavior;
import com.davidblackcn.buildupmobtweaks.qa.QaHarness;
import com.davidblackcn.buildupmobtweaks.mixin.rebuild.RemainingTargetAccess;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.world.entity.monster.zombie.*;
import net.minecraft.world.entity.npc.villager.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import java.util.*;

/** Inputs are native spawning or explicitly supplied real external gear; all actions use Server Tick/GoalSelector. */
public class ProfessionBehaviorTests {
    static final String ENV="buildupmobtweaks:r4e2b",ARENA=SkeletonBehaviorTests.ARENA;
    static void floor(GameTestHelper h){SkeletonBehaviorTests.floor(h,true);}
    static <T extends Mob> T prepare(GameTestHelper h,T m){m.snapTo(h.absolutePos(new BlockPos(8,2,8)),0,0);m.setPersistenceRequired();
        m.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.IRON_HELMET));m.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);m.getRandom().setSeed(620010);return m;}
    static ZombieVillager born(GameTestHelper h,net.minecraft.resources.ResourceKey<VillagerProfession> profession){
        var m=prepare(h,EntityTypes.ZOMBIE_VILLAGER.create(h.getLevel(),EntitySpawnReason.SPAWN_ITEM_USE));m.setVillagerData(m.getVillagerData().withProfession(h.getLevel().registryAccess(),profession));
        m.finalizeSpawn(h.getLevel(),h.getLevel().getCurrentDifficultyAt(m.blockPosition()),EntitySpawnReason.SPAWN_ITEM_USE,new Zombie.ZombieGroupData(false,false));h.getLevel().addFreshEntity(m);return m;
    }
    static Mob crossbow(GameTestHelper h,boolean zp,boolean off,boolean left){Mob m=prepare(h,(zp?EntityTypes.ZOMBIFIED_PIGLIN:EntityTypes.ZOMBIE_VILLAGER).create(h.getLevel(),EntitySpawnReason.COMMAND));
        if(m instanceof ZombieVillager z)z.setVillagerData(z.getVillagerData().withProfession(h.getLevel().registryAccess(),VillagerProfession.FLETCHER));
        m.setLeftHanded(left);var s=new ItemStack(Items.CROSSBOW);s.setDamageValue(17);s.set(DataComponents.CUSTOM_NAME,Component.literal("E2b crossbow "+m.getUUID()));
        m.setItemSlot(off?EquipmentSlot.OFFHAND:EquipmentSlot.MAINHAND,s);m.setDropChance(off?EquipmentSlot.OFFHAND:EquipmentSlot.MAINHAND,.42f);h.getLevel().addFreshEntity(m);return m;
    }
    static Mob fisher(GameTestHelper h,boolean drowned){Mob m=prepare(h,(drowned?EntityTypes.DROWNED:EntityTypes.ZOMBIE_VILLAGER).create(h.getLevel(),EntitySpawnReason.COMMAND));
        if(m instanceof ZombieVillager z)z.setVillagerData(z.getVillagerData().withProfession(h.getLevel().registryAccess(),VillagerProfession.FISHERMAN));
        var s=new ItemStack(Items.FISHING_ROD);s.setDamageValue(17);s.set(DataComponents.CUSTOM_NAME,Component.literal("E2b rod "+m.getUUID()));m.setItemSlot(EquipmentSlot.MAINHAND,s);h.getLevel().addFreshEntity(m);return m;
    }
    static Mob target(GameTestHelper h,int z){var t=SkeletonBehaviorTests.target(h,8,z);t.setPersistenceRequired();t.setNoGravity(true);t.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);t.setHealth(1000);return t;}
    static Zombie injured(GameTestHelper h,int x,int z,float health){var t=EntityTypes.ZOMBIE.create(h.getLevel(),EntitySpawnReason.COMMAND);prepare(h,t);t.snapTo(h.absolutePos(new BlockPos(x,2,z)),0,0);t.setNoAi(true);t.setNoGravity(true);t.addTag("buildupmobtweaks:disable_equipment_assignment");t.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);t.setHealth(health);h.getLevel().addFreshEntity(t);return t;}
    static void proof(String n,Mob m,String detail){var r=ProfessionState.runtime(m);BuildupMobTweaks.LOGGER.info("R4E2B_PROOF scenario={} ticks={} phase={} shots={} casts={} pulls={} potions={} hand={}/{} state={} {}",n,m.tickCount,r.phase,r.shots,r.casts,r.pulls,r.potions,m.getMainHandItem(),m.getOffhandItem(),m.getAttached(ProfessionState.DATA),detail);}
    static void clean(GameTestHelper h,Mob m,Entity... others){for(var p:h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.projectile.Projectile.class,m.getBoundingBox().inflate(64),p->p.getOwner()==m))p.discard();m.discard();for(var e:others)e.discard();}
    static void crossbowCase(GameTestHelper h,boolean zp,boolean off,boolean left){floor(h);var m=crossbow(h,zp,off,left);var s=m.getItemInHand(off?InteractionHand.OFF_HAND:InteractionHand.MAIN_HAND);var t=target(h,14);m.setTarget(t);boolean[] seen={false,false};
        h.onEachTick(()->{seen[0]|=m.isUsingItem()&&m.getTicksUsingItem()>5;seen[1]|=!h.getLevel().getEntitiesOfClass(AbstractArrow.class,m.getBoundingBox().inflate(32),a->a.getOwner()==m).isEmpty();});
        h.runAtTickTime(210,()->{proof("crossbow_"+(zp?"zp":"fletcher")+"_"+(off?"off":"main")+"_"+(left?"left":"right"),m,"hp="+t.getHealth()+" charge="+seen[0]+" arrows="+seen[1]);h.assertTrue(seen[0]&&seen[1]&&ProfessionState.runtime(m).shots>0&&t.getHealth()<1000,"Actual use ticks, owned arrow collision and damage");h.assertTrue(m.getItemInHand(off?InteractionHand.OFF_HAND:InteractionHand.MAIN_HAND)==s&&s.getDamageValue()>17&&m.getDropChances().byEquipment(ProfessionBehavior.slot(off?InteractionHand.OFF_HAND:InteractionHand.MAIN_HAND))==.42f,"Same real weapon, components and drop chance, actual durability");clean(h,m,t);h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=235) public void fletcherMainRightActuallyChargesAndHits(GameTestHelper h){crossbowCase(h,false,false,false);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=235) public void fletcherOffRightActuallyChargesAndHits(GameTestHelper h){crossbowCase(h,false,true,false);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=235) public void zpMainLeftActuallyChargesAndHits(GameTestHelper h){crossbowCase(h,true,false,true);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=235) public void zpOffLeftActuallyChargesAndHits(GameTestHelper h){crossbowCase(h,true,true,true);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=45)
    public void nativeThreeProfessionsIssueOneActualItem(GameTestHelper h){floor(h);var ms=List.of(born(h,VillagerProfession.FLETCHER),born(h,VillagerProfession.FISHERMAN),born(h,VillagerProfession.CLERIC));
        for(int i=0;i<3;i++){var item=i==0?Items.CROSSBOW:i==1?Items.FISHING_ROD:Items.SPLASH_POTION;var m=ms.get(i);var hand=ProfessionBehavior.hand(m,item);h.assertTrue(hand!=null&&m.getItemInHand(hand).getCount()==1,"Native profession birth actual one item");}
        h.runAtTickTime(25,()->{for(var m:ms){proof("native_profession_birth",m,"profession="+m.getVillagerData().profession());h.assertTrue(m.isAlive()&&m.tickCount>=20,"Actual native ticks");clean(h,m);}h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=235)
    public void actualNativeZpBirthCrossbowWorksWithExistingAnger(GameTestHelper h){floor(h);var m=prepare(h,EntityTypes.ZOMBIFIED_PIGLIN.create(h.getLevel(),EntitySpawnReason.SPAWN_ITEM_USE));var c=BuildupMobTweaks.config().equipment;int old=c.zombifiedCrossbowChance.get();
        try{c.zombifiedCrossbowChance.accept(1000);m.finalizeSpawn(h.getLevel(),h.getLevel().getCurrentDifficultyAt(m.blockPosition()),EntitySpawnReason.SPAWN_ITEM_USE,new Zombie.ZombieGroupData(false,false));h.getLevel().addFreshEntity(m);}finally{c.zombifiedCrossbowChance.accept(old);}
        var t=target(h,14);h.runAtTickTime(5,()->m.hurtServer(h.getLevel(),m.damageSources().mobAttack(t),1));
        h.runAtTickTime(210,()->{proof("native_zp_birth_anger",m,"hp="+t.getHealth()+" target="+m.getTarget());h.assertTrue(m.getMainHandItem().is(Items.CROSSBOW)&&m.getTarget()==t&&ProfessionState.runtime(m).shots>0&&t.getHealth()<1000,"Native birth plus actual native retaliation leads to real crossbow hit");clean(h,m,t);h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=45)
    public void birthProtectsOccupiedHandsAndExistingDropComponents(GameTestHelper h){floor(h);var m=prepare(h,EntityTypes.ZOMBIE_VILLAGER.create(h.getLevel(),EntitySpawnReason.SPAWN_ITEM_USE));m.setVillagerData(m.getVillagerData().withProfession(h.getLevel().registryAccess(),VillagerProfession.FLETCHER));
        var main=new ItemStack(Items.DIAMOND_SWORD);main.setDamageValue(23);main.set(DataComponents.CUSTOM_NAME,Component.literal("external named sword"));var off=new ItemStack(Items.SHIELD);m.setItemSlot(EquipmentSlot.MAINHAND,main);m.setItemSlot(EquipmentSlot.OFFHAND,off);m.setDropChance(EquipmentSlot.MAINHAND,.42f);var expected=main.copy();
        m.finalizeSpawn(h.getLevel(),h.getLevel().getCurrentDifficultyAt(m.blockPosition()),EntitySpawnReason.SPAWN_ITEM_USE,new Zombie.ZombieGroupData(false,false));h.getLevel().addFreshEntity(m);
        h.runAtTickTime(25,()->{h.assertTrue(m.getMainHandItem()==main&&m.getOffhandItem()==off&&ItemStack.matches(main,expected)&&m.getDropChances().byEquipment(EquipmentSlot.MAINHAND)==.42f&&m.getAttached(ProfessionState.DATA).getStringOr("birth","").equals("occupied"),"Actual preexisting stacks/components/drop chance unchanged, no hidden birth item");proof("occupied_native",m,"no_issue=true");clean(h,m);h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=50)
    public void highDifficultyNativeBirthCannotEnchantExistingExternalWeapon(GameTestHelper h){floor(h);List<ZombieVillager> ms=new ArrayList<>();
        for(int i=0;i<16;i++){var m=prepare(h,EntityTypes.ZOMBIE_VILLAGER.create(h.getLevel(),EntitySpawnReason.SPAWN_ITEM_USE));m.getRandom().setSeed(620050+i);m.setVillagerData(m.getVillagerData().withProfession(h.getLevel().registryAccess(),VillagerProfession.FLETCHER));var s=new ItemStack(Items.DIAMOND_SWORD);s.setDamageValue(23);s.set(DataComponents.CUSTOM_NAME,Component.literal("external hard "+i));m.setItemSlot(EquipmentSlot.MAINHAND,s);m.setItemSlot(EquipmentSlot.OFFHAND,new ItemStack(Items.SHIELD));m.setDropChance(EquipmentSlot.MAINHAND,.42f);var expected=s.copy();m.finalizeSpawn(h.getLevel(),new net.minecraft.world.DifficultyInstance(net.minecraft.world.Difficulty.HARD,6000000,3600000,1),EntitySpawnReason.SPAWN_ITEM_USE,new Zombie.ZombieGroupData(false,false));m.setNoAi(true);h.getLevel().addFreshEntity(m);h.assertTrue(m.getMainHandItem()==s&&ItemStack.matches(s,expected)&&m.getDropChances().byEquipment(EquipmentSlot.MAINHAND)==.42f,"Sixteen genuine hard spawn seeds retain actual external components including enchantments");ms.add(m);}
        h.runAtTickTime(30,()->{for(var m:ms){proof("hard_external_components",m,"unchanged=true");clean(h,m);}h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=250)
    public void chargedCrossbowActuallyChasesTargetLeavingItsRange(GameTestHelper h){floor(h);var m=crossbow(h,false,true,true);m.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(.23);var t=target(h,14);m.setTarget(t);double start=m.getZ();boolean[] moved={false};
        h.onEachTick(()->{if(!moved[0]&&CrossbowItem.isCharged(m.getOffhandItem())){moved[0]=true;t.snapTo(h.absolutePos(new BlockPos(8,2,20)),0,0);}});
        h.runAtTickTime(225,()->{proof("charged_chase",m,"moved="+(m.getZ()-start)+" hp="+t.getHealth());h.assertTrue(moved[0]&&m.getZ()>start+.5&&ProfessionState.runtime(m).shots>0&&t.getHealth()<1000,"Already charged real offhand bow chases escaped target through native pathfinding and actually hits");clean(h,m,t);h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=50)
    public void genuineBabyProfessionBirthKeepsTheThreeExactRoleBoundaries(GameTestHelper h){floor(h);List<ZombieVillager> ms=new ArrayList<>();int i=0;
        for(var prof:List.of(VillagerProfession.FLETCHER,VillagerProfession.FISHERMAN,VillagerProfession.CLERIC)){var m=prepare(h,EntityTypes.ZOMBIE_VILLAGER.create(h.getLevel(),EntitySpawnReason.SPAWN_ITEM_USE));m.snapTo(h.absolutePos(new BlockPos(6+i*5,2,8)),0,0);m.setVillagerData(m.getVillagerData().withProfession(h.getLevel().registryAccess(),prof));m.finalizeSpawn(h.getLevel(),h.getLevel().getCurrentDifficultyAt(m.blockPosition()),EntitySpawnReason.SPAWN_ITEM_USE,new Zombie.ZombieGroupData(true,false));m.setNoAi(true);h.getLevel().addFreshEntity(m);ms.add(m);i++;}
        h.runAtTickTime(30,()->{for(int n=0;n<3;n++){var m=ms.get(n);var hand=ProfessionBehavior.hand(m,n==0?Items.CROSSBOW:n==1?Items.FISHING_ROD:Items.SPLASH_POTION);h.assertTrue(m.isBaby()&&hand!=null&&m.getItemInHand(hand).getCount()==1,"Actual baby finalizeSpawn produces only its real profession item");proof("native_baby_profession",m,"role="+n);clean(h,m);}h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=150)
    public void loadedChargedAbortKeepsAmmoTargetAndNativeMelee(GameTestHelper h){floor(h);var m=crossbow(h,false,false,false);var t=target(h,14);m.setTarget(t);var s=m.getMainHandItem();boolean[] off={false};
        h.onEachTick(()->{if(!off[0]&&CrossbowItem.isCharged(s)){off[0]=true;m.addTag("buildupmobtweaks:disable_zombie_fletcher_crossbow");t.snapTo(h.absolutePos(new BlockPos(8,2,9)),0,0);}});
        h.runAtTickTime(125,()->{proof("charged_disable_native_melee",m,"hp="+t.getHealth()+" target="+m.getTarget());h.assertTrue(off[0]&&CrossbowItem.isCharged(s)&&m.getMainHandItem()==s&&m.getTarget()==t&&!m.isUsingItem(),"Stopping owned Goal preserves loaded ammo and original target");h.assertTrue(t.getHealth()<1000&&ProfessionState.runtime(m).shots==0&&m.getGoalSelector().getAvailableGoals().stream().anyMatch(g->g.getGoal() instanceof ZombieAttackGoal),"Existing native melee actually damages after independent gate off");clean(h,m,t);h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=180)
    public void replacementSwordIsNotClearedOrDamagedAsTheOldCrossbow(GameTestHelper h){floor(h);var m=crossbow(h,false,false,false);var t=target(h,14);m.setTarget(t);var old=m.getMainHandItem();var sword=new ItemStack(Items.DIAMOND_SWORD);sword.set(DataComponents.CUSTOM_NAME,Component.literal("replacement"));boolean[] changed={false};ItemEntity[] dropped={null};
        h.onEachTick(()->{if(!changed[0]&&m.isUsingItem()&&m.getTicksUsingItem()>5){changed[0]=true;dropped[0]=new ItemEntity(h.getLevel(),m.getX(),m.getY()+1,m.getZ(),old);h.getLevel().addFreshEntity(dropped[0]);m.setItemSlot(EquipmentSlot.MAINHAND,sword);}});
        h.runAtTickTime(150,()->{h.assertTrue(changed[0]&&m.getMainHandItem()==sword&&sword.getHoverName().getString().equals("replacement")&&old.getCount()==1&&ProfessionState.runtime(m).shots==0,"No clear, replacement damage or ghost shot; actual old crossbow transferred to a world item");proof("replacement_crossbow",m,"old_damage="+old.getDamageValue());clean(h,m,t,dropped[0]);h.succeed();});
    }
    static void playerExit(GameTestHelper h,GameType mode){floor(h);var m=crossbow(h,true,false,false);m.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(12);var f=SkeletonBehaviorTests.player(h);f.player().snapTo(h.absolutePos(new BlockPos(8,2,14)),0,0);f.player().getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);f.player().setHealth(1000);f.player().setNoGravity(true);m.setTarget(f.player());boolean[] changed={false};
        h.onEachTick(()->{if(!changed[0]&&m.isUsingItem()&&m.getTicksUsingItem()>5){changed[0]=true;f.player().setGameMode(mode);}});
        h.runAtTickTime(120,()->{proof("player_exit_"+mode,m,"hp="+f.player().getHealth());h.assertTrue(changed[0]&&m.getTarget()==null&&!m.isUsingItem()&&ProfessionState.runtime(m).phase.equals("idle")&&f.player().getHealth()==1000,"Actual creative/spectator switch exits charge without damage");clean(h,m);f.close();h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=145) public void creativePlayerExitsActualCharge(GameTestHelper h){playerExit(h,GameType.CREATIVE);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=145) public void spectatorPlayerExitsActualCharge(GameTestHelper h){playerExit(h,GameType.SPECTATOR);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=150)
    public void actualNoAiDuringChargeStopsOnlyOwnedUse(GameTestHelper h){floor(h);var m=crossbow(h,false,false,false);var t=target(h,14);m.setTarget(t);boolean[] changed={false};
        h.onEachTick(()->{if(!changed[0]&&m.isUsingItem()&&m.getTicksUsingItem()>5){changed[0]=true;m.setNoAi(true);}});
        h.runAtTickTime(125,()->{h.assertTrue(changed[0]&&!m.isUsingItem()&&ProfessionState.runtime(m).shots==0&&m.getMainHandItem().getDamageValue()==17,"Real NoAI aborts owned charge, no ghost shot/durability");proof("noai_crossbow",m,"owned_use_stopped=true");clean(h,m,t);h.succeed();});
    }
    static void fishingCase(GameTestHelper h,boolean drowned,boolean water){floor(h);var m=fisher(h,drowned);if(water){m.setNoGravity(true);for(int x=7;x<=9;x++)for(int z=7;z<=9;z++)for(int y=2;y<=4;y++)h.setBlock(new BlockPos(x,y,z),Blocks.WATER);}var t=target(h,17);t.setNoAi(false);t.getGoalSelector().removeAllGoals(g->true);((RemainingTargetAccess)t).buildup$targets().removeAllGoals(g->true);t.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);m.setTarget(t);var rod=m.getMainHandItem();double start=t.getZ();boolean[] collision={false};Set<UUID> hooks=new HashSet<>();
        h.onEachTick(()->{var hs=h.getLevel().getEntitiesOfClass(FishingHook.class,m.getBoundingBox().inflate(32),p->p.getOwner()==m);h.assertTrue(hs.size()<=1,"Bounded one actual hook per owner on every native tick");for(var p:hs){hooks.add(p.getUUID());collision[0]|=p.getHookedIn()==t;}});
        h.runAtTickTime(180,()->{proof("fishing_"+(drowned?"drowned":"fisherman")+(water?"_water":"_land"),m,"hook_ids="+hooks.size()+" collision="+collision[0]+" dz="+(t.getZ()-start));h.assertTrue(collision[0]&&ProfessionState.runtime(m).pulls>0&&t.getZ()<start-.2,"Native hook collision, synchronized latch and actual pulled position");h.assertTrue(m.getMainHandItem()==rod&&rod.getDamageValue()>=20&&rod.getCount()==1,"Same actual rod, three durability loss on reel, no spare");if(water)h.assertTrue(m.isInWater(),"Real water owner");clean(h,m,t);h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=205) public void fishermanActualHookCollisionPullsAndWearsRod(GameTestHelper h){fishingCase(h,false,false);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=205) public void drownedActualLandHookCollisionPullsAndWearsRod(GameTestHelper h){fishingCase(h,true,false);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=205) public void drownedWaterOwnerCastsAtLandTarget(GameTestHelper h){fishingCase(h,true,true);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=180)
    public void drownedDoesNotFishRealUnderwaterTarget(GameTestHelper h){floor(h);var m=fisher(h,true);var t=target(h,17);for(int x=7;x<=9;x++)for(int z=16;z<=18;z++)for(int y=2;y<=5;y++)h.setBlock(new BlockPos(x,y,z),Blocks.WATER);m.setTarget(t);
        h.runAtTickTime(155,()->{proof("underwater_target",m,"underwater="+t.isUnderWater());h.assertTrue(t.isUnderWater()&&ProfessionState.runtime(m).casts==0&&m.getMainHandItem().getDamageValue()==17,"Actual underwater target fails the upstream fishing prerequisite");clean(h,m,t);h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=190)
    public void rodRequiresLineOfSightAndSixToThirteenRange(GameTestHelper h){floor(h);var m=fisher(h,false);var t=target(h,17);m.setTarget(t);for(int x=6;x<=10;x++)for(int y=2;y<=4;y++)h.setBlock(new BlockPos(x,y,12),Blocks.STONE);
        h.runAtTickTime(75,()->{h.assertTrue(!m.hasLineOfSight(t)&&ProfessionState.runtime(m).casts==0,"Opaque wall prevents cast");t.snapTo(h.absolutePos(new BlockPos(8,2,10)),0,0);});
        h.runAtTickTime(115,()->{h.assertTrue(ProfessionState.runtime(m).casts==0,"Near target does not cast");t.snapTo(h.absolutePos(new BlockPos(8,2,23)),0,0);});
        h.runAtTickTime(165,()->{h.assertTrue(ProfessionState.runtime(m).casts==0&&m.getMainHandItem().getDamageValue()==17,"Far target does not cast or charge durability");proof("rod_los_range",m,"casts=0");clean(h,m,t);h.succeed();});
    }
    static void fiveBlockRodBoundary(GameTestHelper h,boolean drowned){floor(h);var m=fisher(h,drowned);var t=target(h,13);m.setTarget(t);
        h.runAtTickTime(90,()->{h.assertTrue(m.distanceToSqr(t)>24&&m.distanceToSqr(t)<26&&m.hasLineOfSight(t)&&ProfessionState.runtime(m).casts==0&&m.getMainHandItem().getDamageValue()==17,"A visible five-block target intentionally does not start fishing or spend durability");t.snapTo(m.position().add(0,0,7),0,0);m.setTarget(t);});
        h.runAtTickTime(165,()->{h.assertTrue(ProfessionState.runtime(m).casts>0&&ProfessionState.runtime(m).pulls>0&&m.getMainHandItem().getDamageValue()>17,"The unchanged real Goal casts, collides, pulls and wears the rod after the target moves to seven blocks");proof("five_to_seven_blocks",m,"drowned="+drowned+" hp="+t.getHealth());clean(h,m,t);h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=190)
    public void fishermanFiveBlocksIsIntentionalAndSevenActuallyPulls(GameTestHelper h){fiveBlockRodBoundary(h,false);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=190)
    public void drownedFiveBlocksIsIntentionalAndSevenActuallyPulls(GameTestHelper h){fiveBlockRodBoundary(h,true);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=170)
    public void replacingActualRodRetiresHookWithoutTouchingNewEquipment(GameTestHelper h){floor(h);var m=fisher(h,false);var t=target(h,17);m.setTarget(t);var rod=m.getMainHandItem();var shield=new ItemStack(Items.SHIELD);boolean[] changed={false};FishingHook[] hook={null};ItemEntity[] dropped={null};
        h.onEachTick(()->{if(!changed[0]&&ProfessionBehavior.hook(m)!=null){changed[0]=true;hook[0]=ProfessionBehavior.hook(m);dropped[0]=new ItemEntity(h.getLevel(),m.getX(),m.getY()+1,m.getZ(),rod);h.getLevel().addFreshEntity(dropped[0]);m.setItemSlot(EquipmentSlot.MAINHAND,shield);}});
        h.runAtTickTime(145,()->{h.assertTrue(changed[0]&&hook[0].isRemoved()&&m.getMainHandItem()==shield&&rod.getDamageValue()==17&&ProfessionState.runtime(m).pulls==0,"Dropped old rod, removed hook, replacement protected and no late pull/wear");proof("rod_replacement",m,"hook_removed=true");clean(h,m,t,dropped[0]);h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=170)
    public void noAiAndIndependentRodGateAbortActiveHook(GameTestHelper h){floor(h);var m=fisher(h,true);var t=target(h,17);m.setTarget(t);boolean[] changed={false};FishingHook[] hook={null};
        h.onEachTick(()->{if(!changed[0]&&ProfessionBehavior.hook(m)!=null){changed[0]=true;hook[0]=ProfessionBehavior.hook(m);m.addTag("buildupmobtweaks:disable_drowned_fishing_pull");m.setNoAi(true);}});
        h.runAtTickTime(145,()->{h.assertTrue(changed[0]&&hook[0].isRemoved()&&!m.isUsingItem()&&ProfessionState.runtime(m).pulls==0&&m.getMainHandItem().getDamageValue()==17,"Active hook exits gate/NoAI with no refund or late action");proof("rod_noai_gate",m,"removed=true");clean(h,m,t);h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=245)
    public void actualSaveReloadRetiresHookAndKeepsFutureCooldown(GameTestHelper h){floor(h);Mob[] ref={fisher(h,false)};var t=target(h,17);ref[0].setTarget(t);boolean[] loaded={false};FishingHook[] old={null};long[] ready={0};
        h.onEachTick(()->{var m=ref[0];if(!loaded[0]&&ProfessionBehavior.hook(m)!=null){loaded[0]=true;old[0]=ProfessionBehavior.hook(m);ready[0]=m.getAttached(ProfessionState.DATA).getLongOr("rod_ready",0);ref[0]=CombatTestWorld.reload(h,m);ref[0].setTarget(t);}});
        h.runAtTickTime(95,()->{h.assertTrue(loaded[0]&&old[0].isRemoved()&&ProfessionState.runtime(ref[0]).casts==0&&ref[0].getAttached(ProfessionState.DATA).getLongOr("rod_ready",0)==ready[0],"Real save/load retires in-flight hook and retains earned future cooldown");});
        h.runAtTickTime(220,()->{h.assertTrue(ProfessionState.runtime(ref[0]).casts>0&&ref[0].getMainHandItem().getCount()==1,"A later native Goal can cast again without replenishing equipment");proof("rod_reload",ref[0],"future_ready="+ready[0]);clean(h,ref[0],t);h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=165)
    public void deathDuringHookDropsExactlyOneExistingDamagedRod(GameTestHelper h){floor(h);var m=fisher(h,false);m.setDropChance(EquipmentSlot.MAINHAND,2);var t=target(h,17);m.setTarget(t);boolean[] killed={false};FishingHook[] old={null};String name=m.getMainHandItem().getHoverName().getString();
        h.onEachTick(()->{if(!killed[0]&&ProfessionBehavior.hook(m)!=null){killed[0]=true;old[0]=ProfessionBehavior.hook(m);m.hurtServer(h.getLevel(),m.damageSources().genericKill(),10000);}});
        h.runAtTickTime(140,()->{var drops=h.getLevel().getEntitiesOfClass(ItemEntity.class,m.getBoundingBox().inflate(32),e->e.getItem().getHoverName().getString().equals(name));h.assertTrue(killed[0]&&old[0].isRemoved()&&drops.size()==1&&drops.getFirst().getItem().getCount()==1&&drops.getFirst().getItem().getDamageValue()==17,"Native death keeps one actual damaged rod, no hidden copy or late reel");proof("rod_native_death",m,"drops=1 damaged=17");for(var d:drops)d.discard();clean(h,m,t);h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=205)
    public void nativeTridentFlightHasPriorityOverOffhandFishingRod(GameTestHelper h){floor(h);var m=prepare(h,EntityTypes.DROWNED.create(h.getLevel(),EntitySpawnReason.COMMAND));m.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.TRIDENT));m.setItemSlot(EquipmentSlot.OFFHAND,new ItemStack(Items.FISHING_ROD));h.getLevel().addFreshEntity(m);var t=target(h,17);m.setTarget(t);boolean[] flight={false};
        h.onEachTick(()->flight[0]|=DrownedBehavior.instance().projectile(m)!=null);
        h.runAtTickTime(180,()->{proof("trident_priority",m,"trident_throws="+DrownedBehavior.runtime(m).throwsCount+" flight="+flight[0]);h.assertTrue(flight[0]&&DrownedBehavior.runtime(m).throwsCount>0&&ProfessionState.runtime(m).casts==0,"Actual native trident throw/recovery path excludes rod cast");var p=DrownedBehavior.instance().projectile(m);if(p!=null)p.discard();clean(h,m,t);h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=210)
    public void clericConsumesActualBirthPotionAndHealsUndeadWithoutReplacingEnemy(GameTestHelper h){floor(h);var m=born(h,VillagerProfession.CLERIC);var ally=injured(h,8,13,900);var enemy=target(h,14);m.setTarget(enemy);
        h.runAtTickTime(185,()->{proof("cleric_native_heal",m,"healed_hp="+ally.getHealth()+" enemy_target="+(m.getTarget()==enemy));h.assertTrue(ProfessionState.runtime(m).potions==1&&ally.getHealth()>900&&ProfessionBehavior.hand(m,Items.SPLASH_POTION)==null,"Actual one birth potion consumed and original collision heals undead");h.assertTrue(m.getTarget()==enemy,"Support Goal does not rewrite original hostile target");clean(h,m,ally,enemy);h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=200)
    public void clericRejectsLivingAndHealthyTargetsWithoutConsumption(GameTestHelper h){floor(h);var m=born(h,VillagerProfession.CLERIC);var healthy=injured(h,8,13,1000);var living=target(h,14);
        h.runAtTickTime(175,()->{h.assertTrue(ProfessionState.runtime(m).potions==0&&ProfessionBehavior.hand(m,Items.SPLASH_POTION)!=null&&healthy.getHealth()==1000&&living.getHealth()==1000,"No living/full health support target, actual bottle remains");proof("cleric_target_filter",m,"bottle_retained=true");clean(h,m,healthy,living);h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=210)
    public void ownedClericSplashHealsOnlyItsUndeadRecipient(GameTestHelper h){floor(h);var m=born(h,VillagerProfession.CLERIC);var ally=injured(h,8,13,900);var collateral=target(h,13);collateral.snapTo(ally.position().add(.15,0,0),0,0);var full=injured(h,9,13,1000);
        h.runAtTickTime(185,()->{proof("cleric_real_splash_filter",m,"recipient="+ally.getHealth()+" living="+collateral.getHealth()+" full="+full.getHealth());h.assertTrue(ProfessionState.runtime(m).potions==1&&ally.getHealth()>900&&collateral.getHealth()==1000&&full.getHealth()==1000,"Native actual splash heals chosen undead without living or healthy collateral");clean(h,m,ally,collateral,full);h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=240)
    public void clericSaveReloadAfterConsumptionNeverRefills(GameTestHelper h){floor(h);ZombieVillager[] ref={born(h,VillagerProfession.CLERIC)};var ally=injured(h,8,13,900);boolean[] saved={false};CompoundTag[] ledger={null};
        h.onEachTick(()->{if(!saved[0]&&ProfessionState.runtime(ref[0]).potions>0){saved[0]=true;ledger[0]=ref[0].getAttached(ProfessionState.DATA).copy();ref[0]=CombatTestWorld.reload(h,ref[0]);}});
        h.runAtTickTime(215,()->{h.assertTrue(saved[0]&&ProfessionBehavior.hand(ref[0],Items.SPLASH_POTION)==null&&ref[0].getAttached(ProfessionState.DATA).equals(ledger[0])&&ProfessionState.runtime(ref[0]).potions==0,"Actual consumed state survives save/load without bottle or extra shot");proof("cleric_consumed_reload",ref[0],"no_refill=true");clean(h,ref[0],ally);h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=210)
    public void externalStrongHarmingPotionComponentsActuallyHealAndAreConsumed(GameTestHelper h){floor(h);var m=prepare(h,EntityTypes.ZOMBIE_VILLAGER.create(h.getLevel(),EntitySpawnReason.COMMAND));m.setVillagerData(m.getVillagerData().withProfession(h.getLevel().registryAccess(),VillagerProfession.CLERIC));
        var s=new ItemStack(Items.SPLASH_POTION);s.set(DataComponents.POTION_CONTENTS,new PotionContents(Potions.STRONG_HARMING));s.set(DataComponents.CUSTOM_NAME,Component.literal("real external potion"));m.setItemSlot(EquipmentSlot.OFFHAND,s);h.getLevel().addFreshEntity(m);var ally=injured(h,8,13,900);
        h.runAtTickTime(185,()->{h.assertTrue(ProfessionState.runtime(m).potions==1&&ally.getHealth()>904&&m.getOffhandItem().isEmpty(),"Actual external strong potion components used, one actual bottle consumed");proof("cleric_external_components",m,"recipient="+ally.getHealth());clean(h,m,ally);h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=55)
    public void gatesPreventProfessionalBirthAndDoNotReissueAfterEnable(GameTestHelper h){floor(h);var c=BuildupMobTweaks.config().hostile.extended;boolean f=c.zombie_fletcher_crossbow.get(),r=c.zombie_fisherman_rod.get(),p=c.zombie_cleric_potions.get();List<ZombieVillager> ms;
        try{c.zombie_fletcher_crossbow.accept(false);c.zombie_fisherman_rod.accept(false);c.zombie_cleric_potions.accept(false);ms=List.of(born(h,VillagerProfession.FLETCHER),born(h,VillagerProfession.FISHERMAN),born(h,VillagerProfession.CLERIC));}finally{c.zombie_fletcher_crossbow.accept(f);c.zombie_fisherman_rod.accept(r);c.zombie_cleric_potions.accept(p);}
        h.runAtTickTime(30,()->{for(var m:ms){h.assertTrue(m.getAttached(ProfessionState.DATA).getStringOr("birth","").equals("disabled")&&ProfessionBehavior.hand(m,Items.CROSSBOW)==null&&ProfessionBehavior.hand(m,Items.FISHING_ROD)==null&&ProfessionBehavior.hand(m,Items.SPLASH_POTION)==null,"Independent disabled birth stays final after re-enable");proof("birth_disabled_final",m,"no_refill=true");clean(h,m);}h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=120)
    public void unknownVersionKeepsPayloadAndOriginalMelee(GameTestHelper h){floor(h);var m=prepare(h,EntityTypes.ZOMBIE_VILLAGER.create(h.getLevel(),EntitySpawnReason.COMMAND));m.setVillagerData(m.getVillagerData().withProfession(h.getLevel().registryAccess(),VillagerProfession.FLETCHER));var d=new CompoundTag();d.putInt("version",99);d.putString("sentinel","keep");m.setAttached(ProfessionState.DATA,d);m.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.DIAMOND_SWORD));h.getLevel().addFreshEntity(m);var t=target(h,9);m.setTarget(t);
        h.runAtTickTime(95,()->{h.assertTrue(m.getAttached(ProfessionState.DATA).equals(d)&&m.getGoalSelector().getAvailableGoals().stream().noneMatch(g->g.getGoal().getClass().getName().contains("ProfessionGoals"))&&t.getHealth()<1000,"Future payload preserved, no new Goal, original native melee active");proof("unknown99_native",m,"hp="+t.getHealth());clean(h,m,t);h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=55)
    public void nativeConversionCarriesLedgerAndActualWeaponWithoutIssue(GameTestHelper h){floor(h);var m=born(h,VillagerProfession.FLETCHER);var d=m.getAttached(ProfessionState.DATA).copy();var hand=ProfessionBehavior.hand(m,Items.CROSSBOW);m.getItemInHand(hand).setDamageValue(23);m.setDropChance(ProfessionBehavior.slot(hand),.42f);var expected=m.getItemInHand(hand).copy();
        var converted=m.convertTo(EntityTypes.DROWNED,ConversionParams.single(m,true,true),v->v.setPersistenceRequired());
        h.runAtTickTime(30,()->{h.assertTrue(converted!=null&&converted.getAttached(ProfessionState.DATA).equals(d)&&ItemStack.matches(converted.getItemInHand(hand),expected)&&converted.getDropChances().byEquipment(ProfessionBehavior.slot(hand))==.42f,"Real native conversion preserves ledger/equipment/durability/drop chance, no profession refill");proof("native_conversion",converted,"one_weapon=true");clean(h,converted);h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=125)
    public void nativeGoldenAppleCureStopsModuleAndNeverDuplicatesRod(GameTestHelper h){floor(h);var m=born(h,VillagerProfession.FISHERMAN);var hand=ProfessionBehavior.hand(m,Items.FISHING_ROD);var rod=m.getItemInHand(hand);rod.set(DataComponents.CUSTOM_NAME,Component.literal("cure actual rod "+m.getUUID()));m.setDropChance(ProfessionBehavior.slot(hand),2);var f=SkeletonBehaviorTests.player(h);f.player().setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.GOLDEN_APPLE));m.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WEAKNESS,200));
        m.mobInteract(f.player(),InteractionHand.MAIN_HAND);m.setVillagerConversionTime(1);
        h.runAtTickTime(95,()->{var villagers=h.getLevel().getEntitiesOfClass(Villager.class,m.getBoundingBox().inflate(12));var drops=h.getLevel().getEntitiesOfClass(ItemEntity.class,m.getBoundingBox().inflate(32),e->e.getItem().getHoverName().getString().startsWith("cure actual rod "));h.assertTrue(m.isRemoved()&&!villagers.isEmpty()&&f.player().getMainHandItem().isEmpty()&&drops.size()<=1,"Actual vanilla golden apple cure consumes apple and no equipment duplication");proof("native_cure",m,"villagers="+villagers.size()+" rod_drops="+drops.size());for(var d:drops)d.discard();for(var v:villagers)v.discard();f.close();h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=35)
    public void playerFishingHookRetainsVanillaOwnerAndTick(GameTestHelper h){floor(h);var f=SkeletonBehaviorTests.player(h);f.player().setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.FISHING_ROD));var p=new FishingHook(f.player(),h.getLevel(),0,0);h.getLevel().addFreshEntity(p);
        h.runAtTickTime(20,()->{h.assertTrue(p.getPlayerOwner()==f.player()&&f.player().fishing==p&&!ProfessionBehavior.mobHook(p)&&p.tickCount>=15&&!p.isRemoved(),"Native player hook owner, lifetime and ticks unchanged");BuildupMobTweaks.LOGGER.info("R4E2B_PROOF scenario=player_fishing ticks={} vanilla_owner=true",p.tickCount);p.discard();f.close();h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=175)
    public void realProfessionChangeAbortsRodWithoutIssuingAnotherItem(GameTestHelper h){floor(h);var m=(ZombieVillager)fisher(h,false);var t=target(h,17);m.setTarget(t);var rod=m.getMainHandItem();boolean[] changed={false};FishingHook[] old={null};
        h.onEachTick(()->{if(!changed[0]&&ProfessionBehavior.hook(m)!=null){changed[0]=true;old[0]=ProfessionBehavior.hook(m);m.setVillagerData(m.getVillagerData().withProfession(h.getLevel().registryAccess(),VillagerProfession.FLETCHER));}});
        h.runAtTickTime(150,()->{h.assertTrue(changed[0]&&old[0].isRemoved()&&m.getMainHandItem()==rod&&rod.getDamageValue()==17&&ProfessionBehavior.hand(m,Items.CROSSBOW)==null&&ProfessionState.runtime(m).phase.equals("idle"),"Native profession change cancels old hook and never reissues new profession gear");proof("profession_change",m,"no_issue=true");clean(h,m,t);h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=175)
    public void foreignHookCannotStealOrCancelTheOwnedNativeRodAction(GameTestHelper h){floor(h);var m=fisher(h,false);var t=target(h,17);m.setTarget(t);boolean[] added={false};FishingHook[] own={null},foreign={null};
        h.onEachTick(()->{if(!added[0]&&ProfessionBehavior.hook(m)!=null){added[0]=true;own[0]=ProfessionBehavior.hook(m);var f=new FishingHook(EntityTypes.FISHING_BOBBER,h.getLevel());f.snapTo(m.position(),0,0);f.setOwner(m);foreign[0]=f;h.getLevel().addFreshEntity(f);h.assertTrue(!ProfessionBehavior.mobHook(f)&&ProfessionBehavior.hook(m)==own[0],"Unmarked foreign native hook cannot overwrite module ownership");}});
        h.runAtTickTime(150,()->{h.assertTrue(added[0]&&ProfessionState.runtime(m).pulls>0&&m.getMainHandItem().getDamageValue()>=20,"Original owned hook completes real native collision/reel despite foreign same-owner hook");proof("foreign_hook_yield",m,"ownership_preserved=true");foreign[0].discard();clean(h,m,t);h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=200)
    public void clericGateDuringRealWindupKeepsActualBottleAndNativeEnemy(GameTestHelper h){floor(h);var m=born(h,VillagerProfession.CLERIC);var ally=injured(h,8,13,900);var enemy=target(h,14);m.setTarget(enemy);var hand=ProfessionBehavior.hand(m,Items.SPLASH_POTION);var bottle=m.getItemInHand(hand);boolean[] changed={false};
        h.onEachTick(()->{if(!changed[0]&&ProfessionState.runtime(m).phase.equals("potion")){changed[0]=true;m.addTag("buildupmobtweaks:disable_zombie_cleric_potions");}});
        h.runAtTickTime(175,()->{h.assertTrue(changed[0]&&m.getItemInHand(hand)==bottle&&bottle.getCount()==1&&ProfessionState.runtime(m).potions==0&&ally.getHealth()==900&&m.getTarget()==enemy,"Independent cleric gate aborts real preparation, preserves actual bottle and original hostile target");proof("cleric_gate_windup",m,"no_consumption=true");clean(h,m,ally,enemy);h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=90)
    public void actualOldEntityLoadDoesNotIssueMissingProfessionalGear(GameTestHelper h){floor(h);var m=prepare(h,EntityTypes.ZOMBIE_VILLAGER.create(h.getLevel(),EntitySpawnReason.COMMAND));m.setVillagerData(m.getVillagerData().withProfession(h.getLevel().registryAccess(),VillagerProfession.CLERIC));h.getLevel().addFreshEntity(m);var loaded=CombatTestWorld.reload(h,m);
        h.runAtTickTime(65,()->{proof("old_load_no_issue",loaded,"fromDisk="+loaded.isLoadedFromDisk()+" empty="+(loaded.getMainHandItem().isEmpty()&&loaded.getOffhandItem().isEmpty()));h.assertTrue(loaded.getMainHandItem().isEmpty()&&loaded.getOffhandItem().isEmpty()&&loaded.getAttached(ProfessionState.DATA).getStringOr("birth","").equals("legacy_skipped"),"Native existing entity serialization/load never grants a missing profession item");clean(h,loaded);h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=175)
    public void mobDeathTargetDuringChargePreventsGhostArrow(GameTestHelper h){floor(h);var m=crossbow(h,false,false,false);var t=target(h,14);m.setTarget(t);boolean[] changed={false};
        h.onEachTick(()->{if(!changed[0]&&m.isUsingItem()&&m.getTicksUsingItem()>5){changed[0]=true;t.hurtServer(h.getLevel(),t.damageSources().genericKill(),10000);}});
        h.runAtTickTime(150,()->{h.assertTrue(changed[0]&&m.getTarget()==null&&!m.isUsingItem()&&ProfessionState.runtime(m).shots==0&&m.getMainHandItem().getDamageValue()==17,"Actual target death retires owned charge without ghost arrow or durability");proof("target_death_charge",m,"no_shot=true");clean(h,m,t);h.succeed();});
    }
    @GameTest(environment="buildupmobtweaks:r4e2b_qa",structure=ARENA,maxTicks=550)
    public void qaActualProfessionBirthsUseGoalsAndCleanAllThreeScenes(GameTestHelper h){var level=h.getLevel();var pos=h.absolutePos(new BlockPos(50,30,50));QaHarness.Session[] s={QaHarness.spawn(level,pos,QaHarness.Scene.FLETCHER)};
        h.runAtTickTime(160,()->{h.assertTrue(ProfessionState.runtime(s[0].mob).shots>0&&s[0].target.getHealth()<1000,"QA native professional bow emits actual native scheduled arrows");QaHarness.cleanup(level.getServer());s[0]=QaHarness.spawn(level,pos,QaHarness.Scene.DROWNED_FISHING);});
        h.runAtTickTime(330,()->{h.assertTrue(ProfessionState.runtime(s[0].mob).pulls>0&&s[0].maxMove==0,"QA genuine drowned birth rod and native hooked pull");QaHarness.cleanup(level.getServer());s[0]=QaHarness.spawn(level,pos,QaHarness.Scene.CLERIC);});
        h.runAtTickTime(505,()->{h.assertTrue(ProfessionState.runtime(s[0].mob).potions==1&&s[0].target.getHealth()>900,"QA real birth potion consumed and undead healed");QaHarness.cleanup(level.getServer());h.assertTrue(s[0].mob.isRemoved()&&s[0].target.isRemoved()&&level.getBlockState(pos.below()).isAir()&&level.getBlockState(pos.above(4)).isAir(),"Actual QA cleanup retires actors/projectiles and restores terrain");h.succeed();});
    }
}
