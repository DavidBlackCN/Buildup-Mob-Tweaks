package com.davidblackcn.buildupmobtweaks;

import com.davidblackcn.buildupmobtweaks.rebuild.skeleton.*;
import com.davidblackcn.buildupmobtweaks.qa.QaHarness;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.hurtingprojectile.WitherSkull;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import java.util.*;

/** Inputs/observations only: no direct Goal invocation or manual AI/tick calls. */
public class ExtendedSkeletonTests {
    private static final String ARENA="buildupmobtweaks:r2a_arena";
    private static AbstractSkeleton mob(GameTestHelper h,boolean spin){
        SkeletonBehaviorTests.floor(h,true);
        var m=SkeletonBehaviorTests.skeleton(h,spin?EntityTypes.PARCHED:EntityTypes.WITHER_SKELETON,8,8);
        m.getRandom().setSeed(491001);
        m.getAttached(ExtendedSkeletonState.DATA).putString("birth",spin?"spin":"skull");
        if(!spin){m.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.STONE_SWORD));m.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);}
        return m;
    }
    private static void proof(String scenario,AbstractSkeleton m,String extra){var r=ExtendedSkeletonState.runtime(m);BuildupMobTweaks.LOGGER.info("R4E1A_PROOF scenario={} uuid={} ticks={} phase={} spins={} skulls={} hits={} homing={} shots={} cancels={} {}",scenario,m.getUUID(),m.tickCount,r.phase,r.spins,r.skulls,r.hits,r.homingTicks,r.shots,r.cancels,extra);}
    private static void clean(Entity... entities){for(var e:entities)if(e!=null)e.discard();}
    @GameTest(environment="buildupmobtweaks:r4e1a",structure=ARENA,maxTicks=295)
    public void parchedActualWarningSpinMovementAndNativeWeaknessArrow(GameTestHelper h){
        var m=mob(h,true);var t=SkeletonBehaviorTests.target(h,8,18);var arrows=SkeletonBehaviorTests.arrows(h,m);boolean[] warning={false},animated={false},weak={false};Vec3[] begin={null};double[] movement={0};
        h.onEachTick(()->{var r=ExtendedSkeletonState.runtime(m);warning[0]|=r.phase.equals("spin_warning");animated[0]|=m.getAttachedOrCreate(ExtendedSkeletonState.SPIN)!=0;
            if(r.phase.equals("spin")){if(begin[0]==null)begin[0]=m.position();movement[0]=Math.max(movement[0],m.position().distanceTo(begin[0]));}weak[0]|=t.hasEffect(MobEffects.WEAKNESS);});
        h.runAtTickTime(100,()->h.assertTrue(ExtendedSkeletonState.runtime(m).spins==0,"Default first cooldown respected"));
        h.runAtTickTime(280,()->{h.assertTrue(warning[0]&&animated[0]&&movement[0]>.5,"Native selector warning, actual synced spin and physical displacement");h.assertTrue(arrows[0]&&weak[0],"Vanilla Parched arrow actually applies weakness");
            proof("parched_spin",m,"movement="+movement[0]+" warning="+warning[0]+" weakness="+weak[0]);clean(m,t);h.succeed();});
    }
    @GameTest(environment="buildupmobtweaks:r4e1a",structure=ARENA,maxTicks=240)
    public void parchedIndependentOffDuringWarningCancelsWithoutSpin(GameTestHelper h){
        var m=mob(h,true);var t=SkeletonBehaviorTests.target(h,8,18);boolean[] off={false};
        h.onEachTick(()->{if(!off[0]&&ExtendedSkeletonState.runtime(m).phase.equals("spin_warning")){off[0]=true;m.addTag("buildupmobtweaks:disable_parched_dodge");}});
        h.runAtTickTime(225,()->{h.assertTrue(off[0]&&ExtendedSkeletonState.runtime(m).spins==0&&m.getAttachedOrCreate(ExtendedSkeletonState.SPIN)==0,"Off cancels real warning before impulse");proof("spin_off",m,"off=true");clean(m,t);h.succeed();});
    }
    @GameTest(environment="buildupmobtweaks:r4e1a",structure=ARENA,maxTicks=505)
    public void skullActualWindupNativeCollisionDamageAndNoWither(GameTestHelper h){
        var m=mob(h,false);var t=SkeletonBehaviorTests.target(h,8,17);boolean[] warning={false},flight={false};Set<UUID> seen=new HashSet<>();
        h.onEachTick(()->{warning[0]|=ExtendedSkeletonState.runtime(m).phase.equals("skull_warning");for(var s:ExtendedSkeletonState.runtime(m).projectiles){seen.add(s.getUUID());flight[0]|=s.tickCount>1&&s.getDeltaMovement().lengthSqr()>0;}});
        h.runAtTickTime(399,()->h.assertTrue(ExtendedSkeletonState.runtime(m).skulls==0,"400 tick initial cooldown"));
        h.runAtTickTime(495,()->{var r=ExtendedSkeletonState.runtime(m);h.assertTrue(warning[0]&&flight[0]&&seen.size()==1&&r.skulls==1&&r.hits==1&&t.getHealth()<100,"One native skull travelled and hit for actual damage");
            h.assertTrue(!t.hasEffect(MobEffects.WITHER),"Owned skull suppresses long Wither debuff");h.assertTrue(h.getBlockState(new BlockPos(8,1,17)).is(net.minecraft.world.level.block.Blocks.STONE),"Floor intact after actual impact explosion");
            proof("skull_hit",m,"health="+t.getHealth()+" flight="+flight[0]+" terrain=intact");clean(m,t);h.succeed();});
    }
    @GameTest(environment="buildupmobtweaks:r4e1a",structure=ARENA,maxTicks=505)
    public void homingActualMotionBendsToMovingOminousTarget(GameTestHelper h){
        var m=mob(h,false);var t=SkeletonBehaviorTests.target(h,8,20);t.addEffect(new MobEffectInstance(MobEffects.TRIAL_OMEN,1000));boolean[] moved={false};double[] x={0};
        h.onEachTick(()->{var r=ExtendedSkeletonState.runtime(m);if(!moved[0]&&!r.projectiles.isEmpty()){moved[0]=true;t.snapTo(t.position().add(2,0,0),0,0);}for(var s:r.projectiles)x[0]=Math.max(x[0],Math.abs(s.getDeltaMovement().x));});
        h.runAtTickTime(495,()->{var r=ExtendedSkeletonState.runtime(m);h.assertTrue(moved[0]&&r.homingTicks>0&&r.homingTicks<=40&&x[0]>.005,"Native ticking owned skull bends after target moves; bounded tracking");h.assertTrue(r.hits>0&&t.getHealth()<100,"Bending projectile actually collides");proof("homing_hit",m,"bendX="+x[0]+" hp="+t.getHealth());clean(m,t);h.succeed();});
    }
    @GameTest(environment="buildupmobtweaks:r4e1a",structure=ARENA,maxTicks=505)
    public void homingIndependentDisableLeavesBallisticSkull(GameTestHelper h){
        var m=mob(h,false);m.addTag("buildupmobtweaks:disable_wither_skeleton_homing");var t=SkeletonBehaviorTests.target(h,8,17);t.addEffect(new MobEffectInstance(MobEffects.TRIAL_OMEN,1000));
        h.runAtTickTime(495,()->{var r=ExtendedSkeletonState.runtime(m);h.assertTrue(r.skulls==1&&r.hits==1&&r.homingTicks==0&&t.getHealth()<100,"Only homing off: native ballistic skull still damages");proof("homing_off",m,"hp="+t.getHealth());clean(m,t);h.succeed();});
    }
    @GameTest(environment="buildupmobtweaks:r4e1a",structure=ARENA,maxTicks=490)
    public void skullNoAiDuringWindupCancelsAndKeepsCooldown(GameTestHelper h){
        var m=mob(h,false);var t=SkeletonBehaviorTests.target(h,8,17);boolean[] canceled={false};long[] ready={0};
        h.onEachTick(()->{if(!canceled[0]&&ExtendedSkeletonState.runtime(m).phase.equals("skull_warning")){canceled[0]=true;ready[0]=m.getAttached(ExtendedSkeletonState.DATA).getLongOr("special_ready",0);m.setNoAi(true);}});
        h.runAtTickTime(475,()->{h.assertTrue(canceled[0]&&ExtendedSkeletonState.runtime(m).skulls==0&&ExtendedSkeletonState.runtime(m).active==null,"NoAI retires real windup");h.assertTrue(m.getAttached(ExtendedSkeletonState.DATA).getLongOr("special_ready",0)==ready[0],"No cooldown refund");proof("noai_windup",m,"ready="+ready[0]);clean(m,t);h.succeed();});
    }
    @GameTest(environment="buildupmobtweaks:r4e1a",structure=ARENA,maxTicks=505)
    public void skullTargetChangeDiscardsActualFlight(GameTestHelper h){
        var m=mob(h,false);var t=SkeletonBehaviorTests.target(h,8,20);var other=SkeletonBehaviorTests.target(h,16,20);WitherSkull[] fired={null};
        h.onEachTick(()->{var r=ExtendedSkeletonState.runtime(m);if(fired[0]==null&&!r.projectiles.isEmpty()){fired[0]=r.projectiles.getFirst();m.setTarget(other);}});
        h.runAtTickTime(490,()->{h.assertTrue(fired[0]!=null&&fired[0].isRemoved()&&ExtendedSkeletonState.runtime(m).hits==0,"A real in-flight skull does not retarget");proof("flight_target_change",m,"oldTargetHP="+t.getHealth());clean(m,t,other);h.succeed();});
    }
    @GameTest(environment="buildupmobtweaks:r4e1a",structure=ARENA,maxTicks=505)
    public void skullGateOffDiscardsFlightAndCannotRefire(GameTestHelper h){
        var m=mob(h,false);var t=SkeletonBehaviorTests.target(h,8,20);WitherSkull[] fired={null};
        h.onEachTick(()->{var r=ExtendedSkeletonState.runtime(m);if(fired[0]==null&&!r.projectiles.isEmpty()){fired[0]=r.projectiles.getFirst();m.addTag("buildupmobtweaks:disable_wither_skeleton_skull");}});
        h.runAtTickTime(490,()->{h.assertTrue(fired[0]!=null&&fired[0].isRemoved()&&ExtendedSkeletonState.runtime(m).skulls==1&&ExtendedSkeletonState.runtime(m).hits==0,"Gate off retires actual flight without damaging target");proof("flight_off",m,"removed=true");clean(m,t);h.succeed();});
    }
    @GameTest(environment="buildupmobtweaks:r4e1a",structure=ARENA,maxTicks=240)
    public void existingMainAndOffhandBowNativeFireConservesComponents(GameTestHelper h){
        SkeletonBehaviorTests.floor(h,true);var m=SkeletonBehaviorTests.skeleton(h,EntityTypes.WITHER_SKELETON,8,8);m.getAttached(ExtendedSkeletonState.DATA).putString("birth","none");var t=SkeletonBehaviorTests.target(h,8,18);
        var bow=m.getMainHandItem();bow.setDamageValue(17);bow.set(DataComponents.CUSTOM_NAME,Component.literal("E1 owned bow"));boolean[] flaming={false};
        h.onEachTick(()->{for(var a:h.getLevel().getEntitiesOfClass(AbstractArrow.class,m.getBoundingBox().inflate(32),a->a.getOwner()==m))flaming[0]|=a.isOnFire();});
        h.runAtTickTime(100,()->{h.assertTrue(ExtendedSkeletonState.runtime(m).shots>0&&flaming[0]&&bow.getDamageValue()>17,"Native Wither bow burns arrows and uses actual durability");m.setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);m.setItemSlot(EquipmentSlot.OFFHAND,bow);});
        h.runAtTickTime(225,()->{h.assertTrue(m.getOffhandItem()==bow&&bow.getHoverName().getString().equals("E1 owned bow")&&ExtendedSkeletonState.runtime(m).shots>=2,"Native offhand draw and firing preserve real item/name");h.assertTrue(!m.hasAttached(SkeletonState.RESERVE),"No generated sword or copied reserve");proof("native_bow",m,"fire="+flaming[0]+" damage="+bow.getDamageValue());clean(m,t);h.succeed();});
    }
    @GameTest(environment="buildupmobtweaks:r4e1a",structure=ARENA,maxTicks=240)
    public void bowGateOffKeepsVanillaAttackAndActualEquipmentSwap(GameTestHelper h){
        SkeletonBehaviorTests.floor(h,true);var m=SkeletonBehaviorTests.skeleton(h,EntityTypes.WITHER_SKELETON,8,8);m.getAttached(ExtendedSkeletonState.DATA).putString("birth","none");m.addTag("buildupmobtweaks:disable_wither_skeleton_bow");var t=SkeletonBehaviorTests.target(h,8,18);var seen=SkeletonBehaviorTests.arrows(h,m);var bow=m.getMainHandItem();
        h.runAtTickTime(100,()->{h.assertTrue(seen[0]&&ExtendedSkeletonState.runtime(m).shots==0&&bow.getDamageValue()==0,"Off retains native bow AI and disables only added durability accounting");m.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.STONE_SWORD));t.snapTo(m.position().add(0,0,2),0,0);});
        h.runAtTickTime(225,()->{h.assertTrue(t.getHealth()<100&&m.getMainHandItem().is(Items.STONE_SWORD)&&!m.hasAttached(SkeletonState.RESERVE),"Actual native equipment replacement reassesses melee, no automatic item fabrication");proof("bow_vanilla_fallback",m,"hp="+t.getHealth());clean(m,t);h.succeed();});
    }
    @GameTest(environment="buildupmobtweaks:r4e1a",structure=ARENA,maxTicks=510)
    public void skullSaveLoadCancelsFlightPreservesBirthCooldownAndUnknownState(GameTestHelper h){
        AbstractSkeleton[] m={mob(h,false)};var t=SkeletonBehaviorTests.target(h,8,20);WitherSkull[] fired={null},loaded={null};long[] ready={0};
        h.onEachTick(()->{var r=ExtendedSkeletonState.runtime(m[0]);if(fired[0]==null&&!r.projectiles.isEmpty()){fired[0]=r.projectiles.getFirst();ready[0]=m[0].getAttached(ExtendedSkeletonState.DATA).getLongOr("special_ready",0);loaded[0]=CombatTestWorld.reload(h,fired[0]);m[0]=CombatTestWorld.reload(h,m[0]);}});
        h.runAtTickTime(490,()->{h.assertTrue(fired[0]!=null&&loaded[0].isRemoved(),"Actual saved projectile not resumed at load");h.assertTrue(m[0].getAttached(ExtendedSkeletonState.DATA).getStringOr("birth","").equals("skull")&&m[0].getAttached(ExtendedSkeletonState.DATA).getLongOr("special_ready",0)==ready[0],"Birth and earned cooldown survive actual serialization");
            h.assertTrue(ExtendedSkeletonState.runtime(m[0]).skulls==0,"No duplicate release after load");m[0].getAttached(ExtendedSkeletonState.DATA).putInt("version",99);m[0]=CombatTestWorld.reload(h,m[0]);});
        h.runAtTickTime(500,()->{h.assertTrue(m[0].getAttached(ExtendedSkeletonState.DATA).getIntOr("version",0)==99&&ExtendedSkeletonState.runtime(m[0]).skulls==0,"Unknown version kept unchanged and inactive");proof("save_load",m[0],"unknown=99");clean(m[0],t);h.succeed();});
    }
    @GameTest(environment="buildupmobtweaks:r4e1a",structure=ARENA,maxTicks=210)
    public void creativeSpectatorTargetsExitWithoutAffectingOrdinarySkeletonScope(GameTestHelper h){
        var m=mob(h,true);var f=SkeletonBehaviorTests.player(h);var p=f.player();p.snapTo(h.absolutePos(new BlockPos(8,2,18)),0,0);m.setTarget(p);
        h.runAtTickTime(45,()->p.setGameMode(GameType.CREATIVE));
        h.runAtTickTime(55,()->{h.assertTrue(m.getTarget()==null,"Creative target exited");p.setGameMode(GameType.SURVIVAL);m.setTarget(p);});
        h.runAtTickTime(80,()->p.setGameMode(GameType.SPECTATOR));
        h.runAtTickTime(100,()->{h.assertTrue(m.getTarget()==null&&ExtendedSkeletonState.runtime(m).spins==0,"Spectator target exited");var ordinary=SkeletonBehaviorTests.skeleton(h,EntityTypes.SKELETON,14,8);h.assertTrue(!ordinary.hasAttached(ExtendedSkeletonState.DATA),"Ordinary skeleton never installs extended birth/goal");proof("player_exit",m,"creative/spectator");clean(m,ordinary);f.close();h.succeed();});
    }
    @GameTest(environment="buildupmobtweaks:r4e1a",structure=ARENA,maxTicks=25)
    public void nativeBirthSampleAndLoadDoesNotRerollOrIssueEquipment(GameTestHelper h){
        SkeletonBehaviorTests.floor(h,true);int spin=0,skull=0;
        for(int i=0;i<1000;i++)for(var type:List.of(EntityTypes.PARCHED,EntityTypes.WITHER_SKELETON)){
            var m=type.create(h.getLevel(),EntitySpawnReason.NATURAL);m.snapTo(h.absolutePos(new BlockPos(8,2,8)),0,0);m.getRandom().setSeed(495000L+i);
            m.finalizeSpawn(h.getLevel(),h.getLevel().getCurrentDifficultyAt(m.blockPosition()),EntitySpawnReason.NATURAL,null);h.getLevel().addFreshEntity(m);
            String birth=m.getAttached(ExtendedSkeletonState.DATA).getStringOr("birth","");if(birth.equals("spin"))spin++;if(birth.equals("skull"))skull++;
            h.assertTrue(m.getMainHandItem().is(type==EntityTypes.PARCHED?Items.BOW:Items.STONE_SWORD)&&!m.hasAttached(SkeletonState.RESERVE),"No natural bow distribution or reserve generation");m.discard();}
        h.assertTrue(spin>=30&&spin<=110&&skull>=30&&skull<=110,"Default 70/1000 native birth fixtures; not natural spawner evidence");
        var m=mob(h,true);m.getAttached(ExtendedSkeletonState.DATA).putString("birth","none");var loaded=CombatTestWorld.reload(h,m);h.assertTrue(loaded.getAttached(ExtendedSkeletonState.DATA).getStringOr("birth","").equals("none"),"Loaded none never rerolled");
        BuildupMobTweaks.LOGGER.info("R4E1A_PROOF scenario=birth_sample n=1000 spin={} skull={} natural_spawner=NOT_RUN",spin,skull);clean(loaded);h.succeed();
    }
    @GameTest(environment="buildupmobtweaks:r4e1a",structure=ARENA,maxTicks=550)
    public void skullLifetimeExpiresOnNativeTicksWithoutCooldownRefund(GameTestHelper h){
        var m=mob(h,false);var t=SkeletonBehaviorTests.target(h,8,20);WitherSkull[] fired={null};long[] born={0};
        h.onEachTick(()->{var r=ExtendedSkeletonState.runtime(m);if(fired[0]==null&&!r.projectiles.isEmpty()){
            fired[0]=r.projectiles.getFirst();born[0]=h.getLevel().getGameTime();
            // Controlled external deflection parks an actually fired skull, so collision cannot hide TTL.
            fired[0].setDeltaMovement(Vec3.ZERO);fired[0].accelerationPower=0;}
            if(fired[0]!=null&&h.getLevel().getGameTime()<born[0]+99)h.assertTrue(!fired[0].isRemoved(),"No premature expiry of parked native projectile");});
        h.runAtTickTime(540,()->{h.assertTrue(fired[0]!=null&&fired[0].isRemoved()&&fired[0].tickCount>=99,"100 real server ticks retire parked projectile");h.assertTrue(ExtendedSkeletonState.runtime(m).skulls==1&&ExtendedSkeletonState.runtime(m).hits==0,"No replacement or fake damage");proof("lifetime",m,"nativeAge="+fired[0].tickCount);clean(m,t);h.succeed();});
    }
    @GameTest(environment="buildupmobtweaks:r4e1a",structure=ARENA,maxTicks=505)
    public void ownerNoAiRetiresActualFlight(GameTestHelper h){
        var m=mob(h,false);var t=SkeletonBehaviorTests.target(h,8,20);WitherSkull[] fired={null};
        h.onEachTick(()->{var r=ExtendedSkeletonState.runtime(m);if(fired[0]==null&&!r.projectiles.isEmpty()){fired[0]=r.projectiles.getFirst();m.setNoAi(true);}});
        h.runAtTickTime(490,()->{h.assertTrue(fired[0]!=null&&fired[0].isRemoved()&&ExtendedSkeletonState.runtime(m).hits==0,"NoAI removes actual in-flight skull on native ticks");proof("noai_flight",m,"removed=true");clean(m,t);h.succeed();});
    }
    @GameTest(environment="buildupmobtweaks:r4e1a",structure=ARENA,maxTicks=505)
    public void ownerDeathRetiresFlightAndDropsOnlyActualSword(GameTestHelper h){
        var m=mob(h,false);var t=SkeletonBehaviorTests.target(h,8,20);m.setDropChance(EquipmentSlot.MAINHAND,2);m.setDropChance(EquipmentSlot.HEAD,0);var sword=m.getMainHandItem();sword.setDamageValue(17);sword.set(DataComponents.CUSTOM_NAME,Component.literal("E1 real sword"));WitherSkull[] fired={null};
        h.onEachTick(()->{var r=ExtendedSkeletonState.runtime(m);if(fired[0]==null&&!r.projectiles.isEmpty()){fired[0]=r.projectiles.getFirst();m.hurtServer(h.getLevel(),m.damageSources().genericKill(),10000);}});
        h.runAtTickTime(490,()->{h.assertTrue(fired[0]!=null&&fired[0].isRemoved()&&ExtendedSkeletonState.runtime(m).hits==0,"Actual death retires flight");var drops=h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,m.getBoundingBox().inflate(5));
            h.assertTrue(drops.stream().filter(d->d.getItem().is(Items.STONE_SWORD)&&d.getItem().getDamageValue()==17&&d.getItem().getHoverName().getString().equals("E1 real sword")).count()==1,"Exactly one actual component-bearing sword drops");h.assertTrue(drops.stream().noneMatch(d->d.getItem().is(Items.BOW)||d.getItem().is(Items.WOODEN_SWORD)),"No invented bow/reserve sword drops");proof("death_equipment",m,"ownedSword=1");clean(m,t);h.succeed();});
    }
    @GameTest(environment="buildupmobtweaks:r4e1a",structure=ARENA,maxTicks=245)
    public void spinTargetSwitchCannotCompleteOldWarning(GameTestHelper h){
        var m=mob(h,true);var t=SkeletonBehaviorTests.target(h,8,18);var other=SkeletonBehaviorTests.target(h,18,18);boolean[] changed={false};
        h.onEachTick(()->{if(!changed[0]&&ExtendedSkeletonState.runtime(m).phase.equals("spin_warning")){changed[0]=true;m.setTarget(other);}});
        h.runAtTickTime(230,()->{h.assertTrue(changed[0]&&ExtendedSkeletonState.runtime(m).spins==0&&m.getAttachedOrCreate(ExtendedSkeletonState.SPIN)==0,"Target switch cancels old warning without a stale impulse");proof("spin_target_switch",m,"changed=true");clean(m,t,other);h.succeed();});
    }
    @GameTest(environment="buildupmobtweaks:r4e1a_qa",structure=ARENA,maxTicks=1550)
    public void independentQaFourScenesUseNativeScheduling(GameTestHelper h){
        var origin=h.absolutePos(new BlockPos(14,2,14));var server=h.getLevel().getServer();QaHarness.Session[] s={null};
        h.runAtTickTime(5,()->s[0]=QaHarness.spawn(h.getLevel(),origin,QaHarness.Scene.PARCHED));
        h.runAtTickTime(300,()->{h.assertTrue(s[0].sawSpin&&s[0].maxMove>.5,"QA native Parched spin");proof("qa_parched",(AbstractSkeleton)s[0].mob,"movement="+s[0].maxMove);QaHarness.cleanup(server);});
        h.runAtTickTime(310,()->s[0]=QaHarness.spawn(h.getLevel(),origin,QaHarness.Scene.WITHER_BOW));
        h.runAtTickTime(510,()->{h.assertTrue(s[0].sawProjectile&&ExtendedSkeletonState.runtime(s[0].mob).shots>0,"QA native Wither bow");proof("qa_bow",(AbstractSkeleton)s[0].mob,"projectile=true");QaHarness.cleanup(server);});
        h.runAtTickTime(520,()->s[0]=QaHarness.spawn(h.getLevel(),origin,QaHarness.Scene.WITHER_SKULL));
        h.runAtTickTime(1010,()->{h.assertTrue(s[0].sawProjectile&&ExtendedSkeletonState.runtime(s[0].mob).hits>0,"QA native skull collision");proof("qa_skull",(AbstractSkeleton)s[0].mob,"hp="+s[0].target.getHealth());QaHarness.cleanup(server);});
        h.runAtTickTime(1020,()->s[0]=QaHarness.spawn(h.getLevel(),origin,QaHarness.Scene.WITHER_HOMING));
        h.runAtTickTime(1510,()->{h.assertTrue(ExtendedSkeletonState.runtime(s[0].mob).homingTicks>0&&ExtendedSkeletonState.runtime(s[0].mob).hits>0,"QA native tracking skull collision");proof("qa_homing",(AbstractSkeleton)s[0].mob,"hp="+s[0].target.getHealth());QaHarness.cleanup(server);h.succeed();});
    }
}
