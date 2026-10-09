package com.davidblackcn.buildupmobtweaks;

import com.davidblackcn.buildupmobtweaks.rebuild.drowned.*;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.zombie.Drowned;
import net.minecraft.world.entity.projectile.arrow.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;

public class DrownedBehaviorTests {
    static final String ARENA=SkeletonBehaviorTests.ARENA;
    static Drowned mob(GameTestHelper h){var m=EntityTypes.DROWNED.create(h.getLevel(),EntitySpawnReason.COMMAND);
        m.snapTo(h.absolutePos(new BlockPos(8,2,8)),0,0);m.setPersistenceRequired();m.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.IRON_HELMET));
        var trident=new ItemStack(Items.TRIDENT);trident.setDamageValue(17);trident.set(DataComponents.CUSTOM_NAME,Component.literal("R2C actual trident "+m.getUUID()));
        m.setItemSlot(EquipmentSlot.MAINHAND,trident);m.setDropChance(EquipmentSlot.MAINHAND,2);h.getLevel().addFreshEntity(m);return m;}
    static boolean ours(ItemStack s){return s.is(Items.TRIDENT)&&s.getHoverName().getString().startsWith("R2C actual trident ");}
    static boolean own(ItemStack s,Drowned m){return ours(s)&&s.getHoverName().getString().endsWith(m.getUUID().toString());}
    static int count(GameTestHelper h,Drowned m){int n=ours(m.getMainHandItem())?m.getMainHandItem().getCount():0;
        for(var p:h.getLevel().getEntitiesOfClass(ThrownTrident.class,m.getBoundingBox().inflate(40)))if(own(p.getPickupItemStackOrigin(),m))n++;
        for(var i:h.getLevel().getEntitiesOfClass(ItemEntity.class,m.getBoundingBox().inflate(40)))if(own(i.getItem(),m))n+=i.getItem().getCount();return n;}
    static void evidence(String scenario,Drowned m,String detail){var r=DrownedBehavior.runtime(m);BuildupMobTweaks.LOGGER.info("R2C_EVIDENCE scenario={} ticks={} throws={} hits={} recoveries={} releases={} hand={} {}",scenario,m.tickCount,r.throwsCount,r.hits,r.recoveries,r.releases,m.getMainHandItem(),detail);}
    @GameTest(structure=ARENA,maxTicks=500)
    public void vanillaRangedGoalActuallyThrowsHitsAndRecoversOneWeapon(GameTestHelper h){
        SkeletonBehaviorTests.floor(h,true);var m=mob(h);var t=SkeletonBehaviorTests.target(h,8,17);boolean[] seen={false,false,false};
        h.onEachTick(()->{h.assertTrue(count(h,m)==1,"One actual hand/projectile/item weapon at every server tick");
            var p=DrownedBehavior.instance().projectile(m);if(p!=null){seen[0]=true;h.assertTrue(m.getMainHandItem().isEmpty(),"Actual weapon left hand");}
            if(t.getLastDamageSource()!=null&&t.getLastDamageSource().getDirectEntity() instanceof ThrownTrident&&t.getHealth()<100)seen[1]=true;
            if(DrownedBehavior.runtime(m).recoveries>0&&own(m.getMainHandItem(),m)&&seen[1]&&!seen[2]){seen[2]=true;m.addTag("buildupmobtweaks:vanilla_ai");t.discard();}});
        h.runAtTickTime(470,()->{h.assertTrue(seen[0]&&seen[1]&&seen[2],"Actual throw, collision damage and path recovery observed");
            h.assertTrue(ours(m.getMainHandItem())&&m.getMainHandItem().getDamageValue()>=17,"Components and durability retained");evidence("D01_land",m,"one_weapon_each_tick=true actual_damage=true");m.discard();h.succeed();});
    }
    @GameTest(structure=ARENA,maxTicks=260)
    public void realFlightSerializationKeepsUuidAndRecoversWithoutBackup(GameTestHelper h){
        SkeletonBehaviorTests.floor(h,true);Drowned[] refs={mob(h)};refs[0].getRandom().setSeed(430203);var t=SkeletonBehaviorTests.target(h,8,17);boolean[] done={false},recovered={false};
        h.onEachTick(()->{var m=refs[0];var p=DrownedBehavior.instance().projectile(m);if(!done[0]&&p!=null){var uuid=p.getUUID();
            refs[0]=CombatTestWorld.reload(h,m);var loaded=CombatTestWorld.reload(h,p);
            h.assertTrue(loaded.getUUID().equals(uuid)&&refs[0].getMainHandItem().isEmpty()&&DrownedBehavior.instance().projectile(refs[0])==loaded,"Actual serialized UUID ownership, empty hand, no backup");done[0]=true;}
            h.assertTrue(count(h,refs[0])==1,"Serialization preserves exactly one actual weapon");if(DrownedBehavior.runtime(refs[0]).recoveries>0&&!recovered[0]){recovered[0]=true;refs[0].addTag("buildupmobtweaks:vanilla_ai");t.discard();}});
        h.runAtTickTime(235,()->{evidence("D01_save_load_probe",refs[0],"done="+done[0]+" recovered="+recovered[0]+" pos="+refs[0].position()+" flight="+refs[0].getAttached(DrownedBehavior.FLIGHT));h.assertTrue(done[0]&&recovered[0]&&ours(refs[0].getMainHandItem()),"Real in-flight save/load then actual recovery");evidence("D01_save_load",refs[0],"UUID_preserved=true one_weapon=true");refs[0].discard();h.succeed();});
    }
    @GameTest(structure=ARENA,maxTicks=180)
    public void deathDuringActualFlightDropsExactlyOneTrident(GameTestHelper h){
        SkeletonBehaviorTests.floor(h,true);var m=mob(h);m.addTag("buildupmobtweaks:disable_drowned_trident_recovery");var t=SkeletonBehaviorTests.target(h,8,17);boolean[] dead={false};
        h.onEachTick(()->{if(!dead[0]&&DrownedBehavior.instance().projectile(m)!=null){m.hurtServer(h.getLevel(),m.damageSources().generic(),1000);dead[0]=true;}});
        h.runAtTickTime(155,()->{int n=0;for(var e:h.getLevel().getEntitiesOfClass(ItemEntity.class,m.getBoundingBox().inflate(32)))if(ours(e.getItem()))n+=e.getItem().getCount();
            h.assertTrue(dead[0]&&n==1&&h.getLevel().getEntitiesOfClass(ThrownTrident.class,m.getBoundingBox().inflate(32)).isEmpty(),"Real flight death drops only actual trident once");evidence("D01_flight_death",m,"item=1 projectile=0");t.discard();h.succeed();});
    }
    @GameTest(structure=ARENA,maxTicks=330)
    public void actualTimeoutReleasesAndSurvivalPlayerReallyPicksUp(GameTestHelper h){
        SkeletonBehaviorTests.floor(h,true);var m=mob(h);m.getRandom().setSeed(430205);m.addTag("buildupmobtweaks:disable_drowned_trident_recovery");var t=SkeletonBehaviorTests.target(h,8,17);h.runAtTickTime(10,()->m.hurtServer(h.getLevel(),m.damageSources().mobAttack(t),1));ThrownTrident[] saved={null};
        h.onEachTick(()->{var p=DrownedBehavior.instance().projectile(m);if(p!=null)saved[0]=p;h.assertTrue(count(h,m)<=1,"No duplicate during timeout");});
        SkeletonBehaviorTests.PlayerFixture[] players={null};
        h.runAtTickTime(260,()->{var p=saved[0];evidence("D01_timeout_probe",m,"projectile="+(p==null?"none":p.getAttached(DrownedBehavior.OWNED))+" now="+h.getLevel().getGameTime());h.assertTrue(p!=null&&p.pickup==AbstractArrow.Pickup.ALLOWED&&p.getAttached(DrownedBehavior.OWNED).getBooleanOr("released",false),"Actual 200 tick timeout releases pickup policy");
            h.assertTrue(!m.hasAttached(DrownedBehavior.FLIGHT)&&m.getMainHandItem().isEmpty(),"Owner reference cleared without synthetic replacement");
            t.discard();m.addTag("buildupmobtweaks:vanilla_ai");players[0]=SkeletonBehaviorTests.player(h);players[0].player().snapTo(p.position(),0,0);});
        h.runAtTickTime(310,()->{int n=0;var inventory=players[0].player().getInventory();for(int i=0;i<inventory.getContainerSize();i++)if(ours(inventory.getItem(i)))n+=inventory.getItem(i).getCount();
            h.assertTrue(n==1&&saved[0].isRemoved(),"Normal server player collision picked up exactly one actual projectile weapon");evidence("D01_timeout_pickup",m,"player_inventory=1");players[0].close();m.discard();h.succeed();});
    }
    @GameTest(structure=ARENA,maxTicks=250)
    public void actualWaterProjectileIsRecoveredBySwimmingNavigation(GameTestHelper h){
        SkeletonBehaviorTests.floor(h,true);for(int x=2;x<=25;x++)for(int z=2;z<=25;z++)for(int y=2;y<=4;y++)h.setBlock(new BlockPos(x,y,z),Blocks.WATER);
        var m=mob(h);m.getRandom().setSeed(430204);var t=SkeletonBehaviorTests.target(h,8,17);boolean[] water={false},recovered={false},swimming={false};
        h.onEachTick(()->{var p=DrownedBehavior.instance().projectile(m);if(p!=null&&p.isInWater())water[0]=true;if(m.isSwimming())swimming[0]=true;
            h.assertTrue(count(h,m)==1,"Exactly one underwater weapon");if(DrownedBehavior.runtime(m).recoveries>0&&!recovered[0]){recovered[0]=true;m.addTag("buildupmobtweaks:vanilla_ai");t.discard();}});
        h.runAtTickTime(230,()->{evidence("D01_water_probe",m,"water="+water[0]+" swimming="+swimming[0]+" recovered="+recovered[0]+" pos="+m.position()+" flight="+m.getAttached(DrownedBehavior.FLIGHT));h.assertTrue(water[0]&&swimming[0]&&recovered[0]&&ours(m.getMainHandItem()),"Actual water flight, swimming and recovery");evidence("D01_water",m,"water_flight=true swimming=true one_weapon=true");m.discard();h.succeed();});
    }
    @GameTest(structure=ARENA,maxTicks=235)
    public void conservationOptOutUsesOriginalVanillaRangedAttack(GameTestHelper h){
        SkeletonBehaviorTests.floor(h,true);var m=mob(h);m.addTag("buildupmobtweaks:disable_drowned_trident_conservation");var t=SkeletonBehaviorTests.target(h,8,17);boolean[] projectile={false};
        h.onEachTick(()->{if(!h.getLevel().getEntitiesOfClass(ThrownTrident.class,m.getBoundingBox().inflate(32),p->p.getOwner()==m&&!p.hasAttached(DrownedBehavior.OWNED)).isEmpty())projectile[0]=true;});
        h.runAtTickTime(210,()->{h.assertTrue(projectile[0]&&ours(m.getMainHandItem())&&DrownedBehavior.runtime(m).throwsCount==0,"Original vanilla ranged Goal actually fires when independently disabled");evidence("D01_disabled_vanilla",m,"vanilla_projectile=true hand_retained=true");m.discard();t.discard();h.succeed();});
    }
    @GameTest(structure=ARENA,maxTicks=330)
    public void independentPlayerPickupDisablePreservesSingleUnpickableProjectile(GameTestHelper h){
        SkeletonBehaviorTests.floor(h,true);var m=mob(h);m.addTag("buildupmobtweaks:disable_drowned_trident_recovery");m.addTag("buildupmobtweaks:disable_drowned_trident_player_pickup");
        var t=SkeletonBehaviorTests.target(h,8,17);ThrownTrident[] saved={null};h.onEachTick(()->{if(m.isRemoved())return;var p=DrownedBehavior.instance().projectile(m);if(p!=null)saved[0]=p;h.assertTrue(count(h,m)==1,"One weapon throughout disabled pickup flight");});
        h.runAtTickTime(310,()->{h.assertTrue(saved[0]!=null&&saved[0].getAttached(DrownedBehavior.OWNED).getBooleanOr("released",false)&&saved[0].pickup==AbstractArrow.Pickup.DISALLOWED,"Independent owner pickup gate respected at actual timeout");
            h.assertTrue(m.getMainHandItem().isEmpty()&&!m.hasAttached(DrownedBehavior.FLIGHT),"No synthesized hand replacement after release");evidence("D01_pickup_disabled",m,"projectile=1 pickup=DISALLOWED");saved[0].discard();m.discard();t.discard();h.succeed();});
    }
    @GameTest(structure=ARENA,maxTicks=230)
    public void realUnderwaterPlayerModesReleaseTargetAndReacquire(GameTestHelper h){
        SkeletonBehaviorTests.floor(h,true);for(int x=2;x<=25;x++)for(int z=2;z<=25;z++)for(int y=2;y<=4;y++)h.setBlock(new BlockPos(x,y,z),Blocks.WATER);
        var m=mob(h);var f=SkeletonBehaviorTests.player(h);var p=f.player();p.snapTo(h.absolutePos(new BlockPos(8,2,17)),0,0);boolean[] acquired={false};
        h.onEachTick(()->{if(!acquired[0]&&m.getTarget()==p){acquired[0]=true;p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);}});
        h.runAtTickTime(80,()->{h.assertTrue(acquired[0]&&m.getTarget()==null&&!m.isUsingItem(),"Actual underwater survival player acquired, then creative target released");
            p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);p.snapTo(m.position().add(0,0,4),0,0);});
        h.runAtTickTime(150,()->{h.assertTrue(m.getTarget()==p,"Original target selector reacquires survival player");p.setGameMode(net.minecraft.world.level.GameType.SPECTATOR);});
        h.runAtTickTime(210,()->{h.assertTrue(m.getTarget()==null&&!m.isUsingItem(),"Actual spectator mode clears target");evidence("D01_player_modes",m,"creative_spectator=true");m.discard();f.close();h.succeed();});
    }
    @GameTest(structure=ARENA,maxTicks=210)
    public void unknownFutureDrownedDataKeepsVanillaGoalAndOpaqueFlight(GameTestHelper h){
        SkeletonBehaviorTests.floor(h,true);var m=EntityTypes.DROWNED.create(h.getLevel(),EntitySpawnReason.COMMAND);
        m.snapTo(h.absolutePos(new BlockPos(8,2,8)),0,0);m.setPersistenceRequired();m.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.TRIDENT));
        var state=new net.minecraft.nbt.CompoundTag();state.putInt("version",999);state.putString("opaque","retain-state");m.setAttached(DrownedBehavior.STATE,state);
        var flight=new net.minecraft.nbt.CompoundTag();flight.putInt("version",999);flight.putString("opaque","retain-flight");m.setAttached(DrownedBehavior.FLIGHT,flight);
        h.getLevel().addFreshEntity(m);var t=SkeletonBehaviorTests.target(h,8,17);boolean[] vanilla={false};
        h.onEachTick(()->{if(!h.getLevel().getEntitiesOfClass(ThrownTrident.class,m.getBoundingBox().inflate(32),p->p.getOwner()==m&&!p.hasAttached(DrownedBehavior.OWNED)).isEmpty())vanilla[0]=true;});
        h.runAtTickTime(190,()->{h.assertTrue(m.getAttached(DrownedBehavior.STATE)==state&&m.getAttached(DrownedBehavior.FLIGHT)==flight,"Future state and flight data kept verbatim");
            h.assertTrue(vanilla[0]&&m.getMainHandItem().is(Items.TRIDENT)&&m.getGoalSelector().getAvailableGoals().stream().noneMatch(g->g.getGoal().getClass().getName().contains("rebuild.drowned")),"Unknown future data does not install recovery; original ranged goal actually fires");
            evidence("D01_future_data",m,"opaque_preserved=true original_goal=true");m.discard();t.discard();h.succeed();});
    }
    @GameTest(structure=ARENA,maxTicks=210)
    public void nativeConversionDuringActualFlightReleasesOnlyOriginalProjectile(GameTestHelper h){
        SkeletonBehaviorTests.floor(h,true);var m=mob(h);var t=SkeletonBehaviorTests.target(h,8,17);
        net.minecraft.world.entity.monster.zombie.Zombie[] converted={null};ThrownTrident[] actual={null};
        h.onEachTick(()->{if(converted[0]!=null)return;var p=DrownedBehavior.instance().projectile(m);if(p!=null){actual[0]=p;
            // Drowned has no normal water conversion. Directed lifecycle fixture using the verified native API.
            converted[0]=m.convertTo(EntityTypes.ZOMBIE,ConversionParams.single(m,true,true),z->z.setPersistenceRequired());}});
        h.runAtTickTime(190,()->{h.assertTrue(converted[0]!=null&&m.isRemoved()&&actual[0]!=null&&!actual[0].isRemoved(),"Actual ranged flight followed by native conversion event");
            h.assertTrue(actual[0].getAttached(DrownedBehavior.OWNED).getBooleanOr("released",false)&&!m.hasAttached(DrownedBehavior.FLIGHT),"Native conversion releases actual projectile and known old reference");
            h.assertTrue(converted[0].getMainHandItem().isEmpty()&&!converted[0].hasAttached(DrownedBehavior.FLIGHT)&&count(h,m)==1,"No unrelated family ownership copy or synthetic weapon");
            evidence("D01_conversion",m,"native_conversion_fixture=true projectile=1 new_hand=empty");actual[0].discard();converted[0].discard();t.discard();h.succeed();});
    }
}
