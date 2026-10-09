package com.davidblackcn.buildupmobtweaks;

import com.davidblackcn.buildupmobtweaks.qa.QaHarness;
import com.davidblackcn.buildupmobtweaks.qa.QaHarness.Scene;
import com.davidblackcn.buildupmobtweaks.rebuild.raid.RaidState;
import com.davidblackcn.buildupmobtweaks.rebuild.remaining.RemainingState;
import com.davidblackcn.buildupmobtweaks.rebuild.zombie.ZombieState;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.block.Blocks;

/** The same command fixtures run exclusively on real ticks, with existing production selectors. */
public class QaHarnessTests {
    @GameTest(environment="buildupmobtweaks:r4d",structure="buildupmobtweaks:r2a_arena",maxTicks=1400)
    public void allFiveQaScenesRunNativeGoalsAndRestore(GameTestHelper h){
        var level=h.getLevel();var server=level.getServer();var origin=h.absolutePos(new BlockPos(14,2,14));
        QaHarness.Session[] current={null};
        h.runAtTickTime(5,()->{
            h.assertTrue(server.getCommands().getDispatcher().getRoot().getChild("buildupqa")!=null,"Development commands registered in test mod");
            current[0]=QaHarness.spawn(level,origin,Scene.PILLAGER);
        });
        h.runAtTickTime(220,()->{var s=current[0];h.assertTrue(s.sawAxe&&s.sawBow&&s.sawProjectile,"Actual pillager axe then crossbow/native projectile");proof(s);QaHarness.cleanup(server);h.assertTrue(s.mob.isRemoved()&&s.target.isRemoved(),"Owned entities removed");});
        h.runAtTickTime(230,()->current[0]=QaHarness.spawn(level,origin,Scene.SKELETON));
        h.runAtTickTime(575,()->{var s=current[0];h.assertTrue(s.sawWalk&&s.maxMove>.5&&s.sawSword&&s.sawProjectile,"Actual skeleton moving bow, real sword, native arrow");proof(s);QaHarness.cleanup(server);});
        h.runAtTickTime(585,()->current[0]=QaHarness.spawn(level,origin,Scene.ENDERMAN));
        h.runAtTickTime(830,()->{var s=current[0];var r=RemainingState.runtime(s.mob);h.assertTrue(r.combos>=1&&r.teleports>=3&&r.attacks>=3&&s.target.getHealth()<1000,"Native Enderman aggro -> real teleports and melee damage");proof(s);QaHarness.cleanup(server);});
        h.runAtTickTime(840,()->current[0]=QaHarness.spawn(level,origin,Scene.EVOKER));
        h.runAtTickTime(1110,()->{var s=current[0];var r=RaidState.runtime(s.mob);h.assertTrue(r.casts>=1&&r.releases>=3&&s.sawProjectile,"Evoker actual selector casts and releases three native fireballs");proof(s);QaHarness.cleanup(server);});
        h.runAtTickTime(1120,()->current[0]=QaHarness.spawn(level,origin,Scene.DOOR));
        h.runAtTickTime(1160,()->{var s=current[0];var m=(Zombie)s.mob;h.assertTrue(s.sawDoor&&ZombieState.runtime(m).displays.size()==2,"Door attached in actual tick");
            var halves=ZombieState.runtime(m).displays;h.assertTrue(halves.stream().allMatch(d->Boolean.TRUE.equals(d.getAttached(ZombieState.DISPLAY))),"Sync attachment markers exist");
            h.assertTrue(Math.abs(halves.get(0).getY()-halves.get(1).getY())>.9,"Two actual display halves at distinct heights");proof(s);
            m.addTag("buildupmobtweaks:disable_zombie_door_guard");});
        h.runAtTickTime(1170,()->{h.assertTrue(ZombieState.runtime((Zombie)current[0].mob).displays.isEmpty(),"Independent off retires actual door");QaHarness.cleanup(server);
            h.assertTrue(level.getBlockState(origin.offset(0,4,0)).isAir(),"Generated roof restored");
            h.assertTrue(level.getBlockState(origin.offset(0,-1,0)).isAir(),"Original air under floor restored");
            level.setBlockAndUpdate(origin,Blocks.CHEST.defaultBlockState());boolean rejected=false;try{QaHarness.spawn(level,origin,Scene.DOOR);}catch(IllegalArgumentException expected){rejected=true;}
            h.assertTrue(rejected&&level.getBlockEntity(origin)!=null,"Arena refuses block entities before mutation");level.setBlockAndUpdate(origin,Blocks.AIR.defaultBlockState());
            var m=EntityTypes.ZOMBIE.create(level,EntitySpawnReason.COMMAND);m.snapTo(origin,0,0);m.setPersistenceRequired();level.addFreshEntity(m);current[0]=QaHarness.select(m,Scene.DOOR);
        });
        h.runAtTickTime(1185,()->{h.assertTrue(ZombieState.runtime((Zombie)current[0].mob).displays.size()==2,"Selected existing idle entity forced via saved birth");QaHarness.cleanup(server);});
        h.runAtTickTime(1195,()->{var m=(Zombie)current[0].mob;h.assertTrue(m.getAttached(ZombieState.DATA).getStringOr("birth","").equals("none")&&ZombieState.runtime(m).displays.isEmpty(),"Selection restored after native cleanup ticks");
            h.assertTrue(!m.entityTags().contains("buildupmobtweaks:vanilla_ai")&&m.getMainHandItem().isEmpty(),"No gate residue or copied equipment");
            var original=new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_SWORD);original.setDamageValue(17);m.setItemSlot(EquipmentSlot.MAINHAND,original);
            QaHarness.select(m,Scene.DOOR);m.addTag("external:new_tag");m.getAttached(ZombieState.DATA).putLong("guard_ready",999999);QaHarness.cleanup(server);});
        h.runAtTickTime(1205,()->{var m=(Zombie)current[0].mob;
            h.assertTrue(m.getMainHandItem().is(net.minecraft.world.item.Items.DIAMOND_SWORD)&&m.getMainHandItem().getDamageValue()==17&&m.entityTags().contains("external:new_tag"),"Selection keeps actual external item and tags added during session");
            h.assertTrue(m.getAttached(ZombieState.DATA).getLongOr("guard_ready",0)==999999,"QA does not refund earned cooldown");
            m.getAttached(ZombieState.DATA).putInt("version",99);boolean rejected=false;try{QaHarness.select(m,Scene.DOOR);}catch(IllegalArgumentException expected){rejected=true;}
            h.assertTrue(rejected&&m.getAttached(ZombieState.DATA).getIntOr("version",0)==99,"Future state not overwritten by QA");m.discard();
            var f=SkeletonBehaviorTests.player(h);h.assertTrue(!server.getCommands().getDispatcher().getRoot().getChild("buildupqa").canUse(f.player().createCommandSourceStack()),"Non-operator cannot use QA mutation commands");f.close();h.succeed();});
    }
    private static void proof(QaHarness.Session s){BuildupMobTweaks.LOGGER.info("R4E0_QA_PROOF scene={} elapsed={} axe={} bow={} sword={} walk={} move={} projectile={} door={} targetHP={} {}",s.scene.id,s.elapsed,s.sawAxe,s.sawBow,s.sawSword,s.sawWalk,s.maxMove,s.sawProjectile,s.sawDoor,s.target.getHealth(),QaHarness.inspect(s.mob));}
}
