package com.davidblackcn.buildupmobtweaks;

import com.davidblackcn.buildupmobtweaks.rebuild.zombie.*;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.Display.BlockDisplay;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;

/** Directed fixtures; only actual server and vanilla selectors drive AI. No Goal calls or injected targets. */
public class ZombieBehaviorTests {
    static final String ARENA=SkeletonBehaviorTests.ARENA;
    static Zombie mob(GameTestHelper h,EntityType<? extends Zombie> type,int x,int z,int y){
        var m=type.create(h.getLevel(),EntitySpawnReason.COMMAND);m.snapTo(h.absolutePos(new BlockPos(x,y,z)),0,0);
        m.setPersistenceRequired();m.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.IRON_HELMET));h.getLevel().addFreshEntity(m);return m;
    }
    static void door(Zombie m,String material){var d=m.getAttached(ZombieState.DATA);d.putString("birth","door");d.putString("material",material);d.putFloat("door_health",40);}
    static void sand(GameTestHelper h,int layers){for(int x=0;x<28;x++)for(int z=0;z<28;z++){
        h.setBlock(new BlockPos(x,0,z),Blocks.STONE);for(int y=1;y<=3;y++)h.setBlock(new BlockPos(x,y,z),y>3-layers?Blocks.SAND:Blocks.STONE);h.setBlock(new BlockPos(x,8,z),Blocks.STONE);}}
    static net.minecraft.world.entity.animal.golem.IronGolem target(GameTestHelper h,int z,int y){var t=SkeletonBehaviorTests.target(h,8,z);t.snapTo(h.absolutePos(new BlockPos(8,y,z)),0,0);return t;}
    static void evidence(String name,Zombie m,String detail){var r=ZombieState.runtime(m);BuildupMobTweaks.LOGGER.info("R2C_EVIDENCE scenario={} ticks={} mode={} blocks={} guards={} burrows={} exits={} {}",name,m.tickCount,r.mode,r.blocks,r.guards,r.burrows,r.exits,detail);}
    @GameTest(structure=ARENA,maxTicks=65)
    public void realDoorDamageBreakAndExistingEquipmentSurvive(GameTestHelper h){
        SkeletonBehaviorTests.floor(h,true);var m=mob(h,EntityTypes.ZOMBIE,8,8,2);door(m,"minecraft:oak_door");
        var sword=new ItemStack(Items.IRON_SWORD);m.setItemSlot(EquipmentSlot.MAINHAND,sword);var t=target(h,10,2);
        h.runAtTickTime(10,()->{h.assertTrue(ZombieState.runtime(m).displays.size()==2,"Two real native displays");
            for(var display:ZombieState.runtime(m).displays){var output=net.minecraft.world.level.storage.TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING,m.registryAccess());display.saveWithoutId(output);
                var input=net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING,m.registryAccess(),output.buildResult());
                var state=input.read("block_state",net.minecraft.world.level.block.state.BlockState.CODEC).orElseThrow();
                h.assertTrue(state.is(Blocks.OAK_DOOR),"Actual synchronized display contains a door, never air");
                boolean upper=display.entityTags().contains("buildupmobtweaks:door_half_1");h.assertTrue(state.getValue(net.minecraft.world.level.block.DoorBlock.HALF)==(upper?net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER:net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER),"Both real door halves serialized correctly");}
            h.assertTrue(ZombieState.runtime(m).displays.getLast().getY()-ZombieState.runtime(m).displays.getFirst().getY()>.9,"Real passenger tick places upper door one block above lower");
            m.yBodyRot=0;t.snapTo(m.position().add(0,0,2),0,0);float hp=m.getHealth();m.hurtServer(h.getLevel(),m.damageSources().mobAttack(t),10);
            h.assertTrue(m.getHealth()==hp&&m.getAttached(ZombieState.DATA).getFloatOr("door_health",0)==30,"Front damage blocked and real durability consumed");
            t.snapTo(m.position().add(2,0,0),0,0);m.hurtServer(h.getLevel(),m.damageSources().mobAttack(t),4);
            h.assertTrue(m.getHealth()<hp&&m.getAttached(ZombieState.DATA).getFloatOr("door_health",0)==30,"Side damages health without door wear");
            t.snapTo(m.position().add(0,0,2),0,0);m.yBodyRot=0;m.hurtServer(h.getLevel(),m.damageSources().mobAttack(t),35);
            h.assertTrue(ZombieState.runtime(m).breaks==1&&ZombieState.runtime(m).displays.isEmpty(),"Breaking hit blocked and actual displays discarded");
            h.assertTrue(m.getMainHandItem()==sword,"Existing sword never deleted");evidence("Z01_damage_break",m,"side_damage=true equipment_preserved=true");m.discard();t.discard();h.succeed();});
    }
    @GameTest(structure=ARENA,maxTicks=85)
    public void doorMaterialReloadDisableAndDeathCleanActualDisplays(GameTestHelper h){
        SkeletonBehaviorTests.floor(h,true);Zombie[] refs={mob(h,EntityTypes.ZOMBIE,8,8,2)};door(refs[0],"minecraft:iron_door");var t=target(h,10,2);
        h.runAtTickTime(10,()->{var m=refs[0];m.yBodyRot=0;t.snapTo(m.position().add(0,0,2),0,0);m.hurtServer(h.getLevel(),m.damageSources().mobAttack(t),10);
            h.assertTrue(m.getAttached(ZombieState.DATA).getFloatOr("door_health",0)>30,"Iron material loses less durability");
            var display=ZombieState.runtime(m).displays.getFirst();var old=CombatTestWorld.reload(h,display);h.assertTrue(old.isRemoved(),"Saved transient display not resurrected");
            refs[0]=CombatTestWorld.reload(h,m);});
        h.runAtTickTime(25,()->{var m=refs[0];h.assertTrue(ZombieState.runtime(m).displays.size()==2,"Reload rebuilt exactly two displays");
            h.assertTrue(h.getLevel().getEntitiesOfClass(BlockDisplay.class,m.getBoundingBox().inflate(10)).size()==2,"No orphan or duplicate display");m.addTag("buildupmobtweaks:disable_zombie_door_guard");});
        h.runAtTickTime(40,()->{var m=refs[0];h.assertTrue(ZombieState.runtime(m).displays.isEmpty()&&m.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE)<.5,"Independent disable removes displays and attribute");
            m.removeTag("buildupmobtweaks:disable_zombie_door_guard");});
        h.runAtTickTime(55,()->{var m=refs[0];h.assertTrue(ZombieState.runtime(m).displays.size()==2,"Reenable restores remaining owned door");m.hurtServer(h.getLevel(),m.damageSources().generic(),1000);});
        h.runAtTickTime(70,()->{h.assertTrue(h.getLevel().getEntitiesOfClass(BlockDisplay.class,refs[0].getBoundingBox().inflate(10)).isEmpty(),"Death leaves zero display entities");evidence("Z01_reload_disable_death",refs[0],"material=iron display=0");t.discard();h.succeed();});
    }
    @GameTest(structure=ARENA,maxTicks=185)
    public void actualOffhandShieldBlocksWearsAndVanillaMeleeResumes(GameTestHelper h){
        SkeletonBehaviorTests.floor(h,true);var m=mob(h,EntityTypes.ZOMBIE,8,8,2);m.setItemSlot(EquipmentSlot.OFFHAND,new ItemStack(Items.SHIELD));var t=target(h,10,2);boolean[] blocked={false};
        h.onEachTick(()->{if(!blocked[0]&&m.isBlocking()&&m.getTicksUsingItem()>=6){t.snapTo(m.position().add(m.getViewVector(1).multiply(1,0,1).normalize().scale(2)),0,0);
            float hp=m.getHealth();m.hurtServer(h.getLevel(),m.damageSources().mobAttack(t),6);evidence("Z02_probe",m,"before="+hp+" after="+m.getHealth()+" wear="+m.getOffhandItem().getDamageValue()+" yaw="+m.getYRot()+" body="+m.yBodyRot+" source="+t.position()+" pos="+m.position());h.assertTrue(m.getHealth()==hp&&m.getOffhandItem().getDamageValue()>0,"Native shield blocks damage and wears");blocked[0]=true;m.addTag("buildupmobtweaks:disable_zombie_shield_use");}});
        h.runAtTickTime(165,()->{h.assertTrue(blocked[0]&&!m.isUsingItem()&&t.getHealth()<100,"Real guard observed, stops and actual melee resumes");evidence("Z02_shield",m,"wear="+m.getOffhandItem().getDamageValue()+" targetHealth="+t.getHealth());m.discard();t.discard();h.succeed();});
    }
    @GameTest(structure=ARENA,maxTicks=175)
    public void actualHuskThreePhaseBurrowTracksSnapshotAndRestoresPhysics(GameTestHelper h){
        sand(h,3);var m=mob(h,EntityTypes.HUSK,8,6,4);var t=target(h,21,4);double base=m.getY();double[] min={base};boolean[] moved={false};
        h.onEachTick(()->{min[0]=Math.min(min[0],m.getY());if(!moved[0]&&ZombieState.runtime(m).burrows>0){moved[0]=true;t.snapTo(h.absolutePos(new BlockPos(14,4,19)),0,0);}});
        h.runAtTickTime(160,()->{h.assertTrue(moved[0]&&min[0]<base-2,"Actual downward phase in three-layer sand");h.assertTrue(m.getZ()>h.absolutePos(new BlockPos(8,4,18)).getZ(),"Actual snapshot relocation and upward phase");
            h.assertTrue(!m.noPhysics&&!m.isNoGravity()&&!m.getAttached(ZombieState.DATA).getBooleanOr("burrow_active",true),"End restores collision and gravity");
            h.assertTrue(m.getAttached(ZombieState.DATA).getLongOr("burrow_ready",0)>h.getLevel().getGameTime(),"World-time cooldown survives completed phase");evidence("Z03_complete",m,"minDepth="+(base-min[0])+" target_moved=true");m.discard();t.discard();h.succeed();});
    }
    @GameTest(structure=ARENA,maxTicks=240)
    public void thinSandFallsBackToActualVanillaMelee(GameTestHelper h){
        sand(h,1);var m=mob(h,EntityTypes.HUSK,8,6,4);var t=target(h,22,4);
        h.runAtTickTime(220,()->{h.assertTrue(ZombieState.runtime(m).burrows==0&&t.getHealth()<100,"Thin sand rejects burrow; actual vanilla path and melee work");evidence("Z03_thin_sand",m,"targetHealth="+t.getHealth());m.discard();t.discard();h.succeed();});
    }
    @GameTest(structure=ARENA,maxTicks=145)
    public void activeBurrowSaveLoadRestoresSurfaceAndKeepsCooldown(GameTestHelper h){
        sand(h,3);Zombie[] refs={mob(h,EntityTypes.HUSK,8,6,4)};var t=target(h,22,4);double base=refs[0].getY();boolean[] done={false};
        h.onEachTick(()->{var m=refs[0];if(!done[0]&&m.getY()<base-1&&m.getAttached(ZombieState.DATA).getBooleanOr("burrow_active",false)){
            long ready=m.getAttached(ZombieState.DATA).getLongOr("burrow_ready",0);refs[0]=CombatTestWorld.reload(h,m);var loaded=refs[0];
            h.assertTrue(!loaded.noPhysics&&!loaded.isNoGravity()&&loaded.getY()>=base-.1,"Actual save/load ends in-flight phase on surface");
            h.assertTrue(loaded.getAttached(ZombieState.DATA).getLongOr("burrow_ready",0)==ready,"Saved cooldown not rerolled");done[0]=true;}});
        h.runAtTickTime(125,()->{h.assertTrue(done[0],"Actual burrow occurred before serialization");evidence("Z03_save_load",refs[0],"surface=true cooldown=true");refs[0].discard();t.discard();h.succeed();});
    }
    @GameTest(structure=ARENA,maxTicks=145)
    public void activeBurrowIndependentDisableSafelyExits(GameTestHelper h){
        sand(h,3);var m=mob(h,EntityTypes.HUSK,8,6,4);var t=target(h,22,4);double base=m.getY();boolean[] done={false};
        h.onEachTick(()->{if(!done[0]&&m.getY()<base-1){m.addTag("buildupmobtweaks:disable_husk_sand_burrow");done[0]=true;}});
        h.runAtTickTime(125,()->{h.assertTrue(done[0]&&!m.noPhysics&&!m.isNoGravity()&&m.getY()>=base-.1,"Independent disable ends actual underground phase safely");evidence("Z03_disable",m,"surface=true");m.discard();t.discard();h.succeed();});
    }
    @GameTest(structure=ARENA,maxTicks=145)
    public void changedSandRouteNeverTeleportsIntoObstruction(GameTestHelper h){
        sand(h,3);var m=mob(h,EntityTypes.HUSK,8,6,4);var t=target(h,22,4);double base=m.getY();boolean[] done={false},restored={false};double[] stopZ={Double.MAX_VALUE};
        h.onEachTick(()->{if(!done[0]&&m.getY()<base-1){for(int y=4;y<=6;y++)h.setBlock(new BlockPos(8,y,15),Blocks.STONE);done[0]=true;}
            if(done[0]&&!restored[0]&&!m.getAttached(ZombieState.DATA).getBooleanOr("burrow_active",false)){restored[0]=true;stopZ[0]=m.getZ();}});
        h.runAtTickTime(125,()->{h.assertTrue(done[0]&&restored[0]&&!m.noPhysics&&!m.isNoGravity()&&stopZ[0]<h.absolutePos(new BlockPos(8,4,15)).getZ(),"Route change aborts relocation and restores safe origin before vanilla walking resumes");evidence("Z03_changed_route",m,"no_unsafe_teleport=true surface_exit_z="+stopZ[0]);m.discard();t.discard();h.succeed();});
    }
    @GameTest(structure=ARENA,maxTicks=185)
    public void realPlayerModesEndBurrowAndTargetThenReacquire(GameTestHelper h){
        sand(h,3);var m=mob(h,EntityTypes.HUSK,8,6,4);var f=SkeletonBehaviorTests.player(h);var p=f.player();p.snapTo(h.absolutePos(new BlockPos(8,4,21)),0,0);boolean[] phase={false};
        h.onEachTick(()->{if(!phase[0]&&m.getAttached(ZombieState.DATA).getBooleanOr("burrow_active",false)){phase[0]=true;p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);}});
        h.runAtTickTime(70,()->{h.assertTrue(phase[0]&&m.getTarget()==null&&!m.noPhysics&&!m.isNoGravity(),"Real creative switch ends target and active phase");p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);p.snapTo(m.position().add(0,0,4),0,0);});
        h.runAtTickTime(130,()->{h.assertTrue(m.getTarget()==p,"Vanilla selector reacquires survival player");p.setGameMode(net.minecraft.world.level.GameType.SPECTATOR);});
        h.runAtTickTime(165,()->{h.assertTrue(m.getTarget()==null&&!m.isUsingItem(),"Spectator exits actual target");evidence("Z03_player_modes",m,"creative_and_spectator=true");m.discard();f.close();h.succeed();});
    }
    @GameTest(structure=ARENA,maxTicks=2050)
    public void defaultBirthDistributionAndSingleSameFamilyRider(GameTestHelper h){
        SkeletonBehaviorTests.floor(h,true);
        h.runAtTickTime(1850,()->{int doors=0,riders=0;var difficulty=new DifficultyInstance(Difficulty.HARD,6000000,3600000,1);
            for(int i=0;i<1000;i++){var type=i%2==0?EntityTypes.ZOMBIE:EntityTypes.HUSK;var m=type.create(h.getLevel(),EntitySpawnReason.NATURAL);
                m.snapTo(h.absolutePos(new BlockPos(8,2,8)),0,0);m.setPersistenceRequired();m.getRandom().setSeed(72000L+i);
                m.finalizeSpawn(h.getLevel(),difficulty,EntitySpawnReason.NATURAL,new Zombie.ZombieGroupData(false,false));
                var d=m.getAttached(ZombieState.DATA);String birth=d.getStringOr("birth","");if(birth.equals("door"))doors++;
                if(birth.equals("rider")){riders++;h.assertTrue(m.getPassengers().size()==1&&m.getFirstPassenger() instanceof Zombie z&&z.isBaby()&&z.getType()==type&&!z.isVehicle(),"Exactly one same-family baby; no recursion");}
                h.getLevel().addFreshEntityWithPassengers(m);for(var child:java.util.List.copyOf(m.getPassengers()))child.discard();m.discard();}
            h.assertTrue(doors>=5&&doors<=80&&riders>=1&&riders<=40,"Default nonzero configured birth distribution within broad sample bounds");
            BuildupMobTweaks.LOGGER.info("R2C_EVIDENCE scenario=Z01_Z04_default_birth n=1000 door={} rider={} actual_finalizeSpawn=true controlled_high_local_difficulty=true natural_spawner=NOT_RUN",doors,riders);h.succeed();});
    }
    @GameTest(structure=ARENA,maxTicks=80)
    public void futureDataAndUnapprovedVariantsStayUntouched(GameTestHelper h){
        SkeletonBehaviorTests.floor(h,true);var m=EntityTypes.ZOMBIE.create(h.getLevel(),EntitySpawnReason.COMMAND);m.snapTo(h.absolutePos(new BlockPos(8,2,8)),0,0);
        var future=new CompoundTag();future.putInt("version",999);future.putString("opaque","preserve");m.setAttached(ZombieState.DATA,future);h.getLevel().addFreshEntity(m);
        var v=EntityTypes.ZOMBIE_VILLAGER.spawn(h.getLevel(),h.absolutePos(new BlockPos(20,2,20)),EntitySpawnReason.COMMAND);
        h.runAtTickTime(50,()->{h.assertTrue(m.getAttached(ZombieState.DATA)==future&&future.getStringOr("opaque","").equals("preserve"),"Unknown future data not overwritten");
            h.assertTrue(m.getGoalSelector().getAvailableGoals().stream().noneMatch(g->g.getGoal().getClass().getName().contains("rebuild.zombie"))&&!v.hasAttached(ZombieState.DATA),"Future data and unapproved variant not managed");
            evidence("compatibility_future_variant",m,"unchanged=true");m.discard();v.discard();h.succeed();});
    }
    @GameTest(structure=ARENA,maxTicks=160)
    public void realBabyRiderReloadAndDisableNeverDuplicateOrDelete(GameTestHelper h){
        SkeletonBehaviorTests.floor(h,true);var cfg=BuildupMobTweaks.config().hostile.zombie;int oldDoor=cfg.doorChance.get(),oldRider=cfg.riderChance.get();
        Zombie[] refs={null};
        try{cfg.doorChance.accept(0);cfg.riderChance.accept(1000);var m=EntityTypes.HUSK.create(h.getLevel(),EntitySpawnReason.SPAWN_ITEM_USE);m.snapTo(h.absolutePos(new BlockPos(8,2,8)),0,0);
            m.setPersistenceRequired();m.finalizeSpawn(h.getLevel(),h.getLevel().getCurrentDifficultyAt(m.blockPosition()),EntitySpawnReason.SPAWN_ITEM_USE,new Zombie.ZombieGroupData(false,false));
            h.getLevel().addFreshEntityWithPassengers(m);refs[0]=m;
        }finally{cfg.doorChance.accept(oldDoor);cfg.riderChance.accept(oldRider);}
        java.util.UUID[] childId={null};
        h.runAtTickTime(15,()->{var m=refs[0];h.assertTrue(m.getPassengers().size()==1,"Actual spawn adds one baby passenger");
            // Keep ordinary distance despawn out of the detach/no-delete assertion; production does not force persistence.
            ((Zombie)m.getFirstPassenger()).setPersistenceRequired();childId[0]=m.getFirstPassenger().getUUID();refs[0]=CombatTestWorld.reloadPassengers(h,m);});
        h.runAtTickTime(40,()->{var m=refs[0];h.assertTrue(m.getPassengers().size()==1&&m.getFirstPassenger().getUUID().equals(childId[0]),"Native recursive passenger load preserves same rider UUID");m.addTag("buildupmobtweaks:disable_zombie_baby_rider");});
        h.runAtTickTime(65,()->{var m=refs[0];evidence("Z04_probe",m,"vehicle="+m.isVehicle()+" child="+h.getLevel().getEntity(childId[0])+" data="+m.getAttached(ZombieState.DATA)+" tags="+m.entityTags());h.assertTrue(!m.isVehicle()&&h.getLevel().getEntity(childId[0]) instanceof Zombie child&&child.isBaby()&&!child.isRemoved(),"Disable detaches actual rider without deleting entity");m.removeTag("buildupmobtweaks:disable_zombie_baby_rider");});
        h.runAtTickTime(130,()->{h.assertTrue(!refs[0].isVehicle(),"Reenable does not synthesize or recruit another rider");evidence("Z04_reload_disable",refs[0],"same_UUID=true no_recruit=true");h.getLevel().getEntity(childId[0]).discard();refs[0].discard();h.succeed();});
    }
    @GameTest(structure=ARENA,maxTicks=2550)
    public void realConfigAndMobGriefingDisableUseVanillaFallback(GameTestHelper h){
        sand(h,3);Zombie[] refs={null};net.minecraft.world.entity.animal.golem.IronGolem[] targets={null};
        var rule=net.minecraft.world.level.gamerules.GameRules.MOB_GRIEFING;boolean original=h.getLevel().getGameRules().get(rule);
        h.runAtTickTime(2100,()->{h.getLevel().getGameRules().set(rule,false,h.getLevel().getServer());refs[0]=mob(h,EntityTypes.HUSK,8,6,4);targets[0]=target(h,21,4);});
        h.runAtTickTime(2300,()->{try{h.assertTrue(ZombieState.runtime(refs[0]).burrows==0&&targets[0].getHealth()<100,"mobGriefing false rejects skill and real melee still works");}finally{h.getLevel().getGameRules().set(rule,original,h.getLevel().getServer());}
            refs[0].discard();targets[0].discard();h.getLevel().getGameRules().set(rule,original,h.getLevel().getServer());
            refs[0]=mob(h,EntityTypes.HUSK,8,6,4);targets[0]=target(h,21,4);door(refs[0],"minecraft:oak_door");BuildupMobTweaks.config().general.enabled.accept(false);});
        h.runAtTickTime(2500,()->{var m=refs[0];try{h.assertTrue(ZombieState.runtime(m).burrows==0&&ZombieState.runtime(m).displays.isEmpty()&&targets[0].getHealth()<100,"Real master switch disables door/burrow and preserves original melee");}finally{BuildupMobTweaks.config().general.enabled.accept(true);}
            evidence("Z03_config_rules",m,"mobGriefing_false=true master_false=true actual_melee=true");m.discard();targets[0].discard();h.succeed();});
    }
    @GameTest(structure=ARENA,maxTicks=1050)
    public void realWaterConversionClearsDoorAndDoesNotCopyBirthSkill(GameTestHelper h){
        SkeletonBehaviorTests.floor(h,true);var m=mob(h,EntityTypes.ZOMBIE,8,8,2);door(m,"minecraft:oak_door");
        for(int x=7;x<=9;x++)for(int z=7;z<=9;z++)for(int y=2;y<=5;y++)h.setBlock(new BlockPos(x,y,z),x==8&&z==8?Blocks.WATER:Blocks.STONE);
        h.setBlock(new BlockPos(8,4,8),Blocks.STONE);
        h.runAtTickTime(1000,()->{var converted=h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.monster.zombie.Drowned.class,m.getBoundingBox().inflate(4));
            evidence("Z01_conversion_probe",m,"removed="+m.isRemoved()+" alive="+m.isAlive()+" water="+m.isInWater()+" converting="+m.isUnderWaterConverting()+" pos="+m.position()+" drowned="+converted.size()+" displays="+ZombieState.runtime(m).displays.size());
            h.assertTrue(m.isRemoved()&&converted.size()==1&&ZombieState.runtime(m).displays.isEmpty(),"Actual water conversion ends original owned door");
            h.assertTrue(!converted.getFirst().hasAttached(ZombieState.DATA),"Unrelated family does not inherit door/birth metadata");evidence("Z01_conversion",m,"actual_water_conversion=true displays=0");converted.getFirst().discard();h.succeed();});
    }
}
