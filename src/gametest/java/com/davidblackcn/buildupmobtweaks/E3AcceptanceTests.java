package com.davidblackcn.buildupmobtweaks;

import com.davidblackcn.buildupmobtweaks.rebuild.riding.RidingState;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.SkeletonHorse;
import net.minecraft.world.entity.monster.skeleton.*;
import net.minecraft.world.entity.projectile.arrow.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import java.util.*;

/** Actual world ticks and native Goals; no direct skill, Goal or entity tick invocation. */
public class E3AcceptanceTests {
    private static final String ENV="buildupmobtweaks:r4e3_acceptance",ARENA="buildupmobtweaks:r2a_arena";
    private void horseExit(GameTestHelper h,String exit){
        SkeletonBehaviorTests.floor(h,true);
        var rider=RidingRelationTests.mob(h,EntityTypes.SKELETON,8,8);
        var horse=RidingRelationTests.mob(h,EntityTypes.SKELETON_HORSE,8,8);horse.setTamed(true);
        rider.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);
        horse.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);
        rider.addTag("buildupmobtweaks:disable_skeleton_weapon_switching");
        rider.addTag("buildupmobtweaks:disable_skeleton_sniping");
        rider.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.BOW));rider.startRiding(horse,true,false);
        rider.getAttached(RidingState.DATA).putBoolean("horse",true);
        var target=RidingRelationTests.mob(h,EntityTypes.IRON_GOLEM,8,16);target.setNoAi(true);
        boolean[] warning={false},input={false};double[] moved={0};Vec3 initial=horse.position();
        h.onEachTick(()->{
            // This scenario isolates mounted contact and pose cleanup. Native arrows are
            // covered by normalNativeMountedArrowDoesNotTurnBack; intercept these fixture
            // arrows so a random arrow hit cannot occupy the target's native damage cooldown.
            if(exit.equals("contact"))for(var a:h.getLevel().getEntitiesOfClass(AbstractArrow.class,rider.getBoundingBox().inflate(32),a->a.getOwner()==rider))a.discard();
            moved[0]=Math.max(moved[0],horse.position().distanceTo(initial));
            if(!input[0]&&RidingState.runtime(rider).phase.equals("warning")){
                warning[0]=horse.isStanding();input[0]=true;
                switch(exit){
                    case "contact"->target.snapTo(horse.position().add(0,0,1),0,0);
                    case "off"->rider.addTag("buildupmobtweaks:disable_skeleton_horse_charge");
                    case "noai"->rider.setNoAi(true);
                    case "dismount"->rider.stopRiding();
                    case "target_exit"->target.discard();
                    case "rider_death"->rider.hurtServer(h.getLevel(),rider.damageSources().generic(),2000);
                    default->{}
                }
            }
        });
        h.runAtTickTime(285,()->{
            var r=RidingState.runtime(rider);
            BuildupMobTweaks.LOGGER.info("E3_ACCEPT_HORSE exit={} warning={} standing={} standAnim={} phase={} charges={} hits={} moved={}",exit,warning[0],horse.isStanding(),horse.getStandAnim(1),r.phase,r.charges,r.hits,moved[0]);
            h.assertTrue(input[0]&&warning[0],"Actual native scheduling reached the real standing warning");
            if(exit.equals("timeout"))h.assertTrue(r.charges==1&&moved[0]>.3,"A real charge moved the horse before completion");
            if(exit.equals("contact"))h.assertTrue(r.hits==1&&target.getHealth()<1000,"Actual world collision caused one bounded hit");
            h.assertTrue(!horse.isStanding()&&horse.getStandAnim(1)<.01,"The native standing flag and interpolated animation clear after this exit");
            RidingRelationTests.clean(rider,horse,target);h.succeed();
        });
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=310) public void horseChargeCompletionClearsStanding(GameTestHelper h){horseExit(h,"timeout");}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=310) public void horseEarlyCollisionClearsStanding(GameTestHelper h){horseExit(h,"contact");}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=310) public void horseWarningDisableClearsStanding(GameTestHelper h){horseExit(h,"off");}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=310) public void horseWarningNoAiClearsStanding(GameTestHelper h){horseExit(h,"noai");}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=310) public void horseWarningDismountClearsStanding(GameTestHelper h){horseExit(h,"dismount");}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=310) public void horseWarningTargetExitClearsStanding(GameTestHelper h){horseExit(h,"target_exit");}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=310) public void horseWarningRiderDeathClearsStanding(GameTestHelper h){horseExit(h,"rider_death");}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=65) public void disabledSkillDoesNotEraseUnrelatedNativeStanding(GameTestHelper h){
        SkeletonBehaviorTests.floor(h,true);var horse=RidingRelationTests.mob(h,EntityTypes.SKELETON_HORSE,8,8);
        var rider=RidingRelationTests.mob(h,EntityTypes.SKELETON,8,8);rider.addTag("buildupmobtweaks:disable_skeleton_horse_charge");rider.startRiding(horse,true,false);
        horse.setStanding(60);
        h.runAtTickTime(25,()->{h.assertTrue(horse.isStanding(),"A never-started charge must not clear unrelated native standing");RidingRelationTests.clean(rider,horse);h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=245) public void normalNativeMountedArrowDoesNotTurnBack(GameTestHelper h){
        SkeletonBehaviorTests.floor(h,true);var rider=SkeletonBehaviorTests.skeleton(h,EntityTypes.SKELETON,8,8);
        rider.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);
        var horse=RidingRelationTests.mob(h,EntityTypes.SKELETON_HORSE,8,8);horse.setNoAi(true);horse.setTamed(true);rider.startRiding(horse,true,false);
        var target=RidingRelationTests.mob(h,EntityTypes.IRON_GOLEM,8,19);target.setNoAi(true);
        AbstractArrow[] first={null};int[] forward={0};boolean[] reversed={false};
        h.onEachTick(()->{
            if(first[0]==null)first[0]=h.getLevel().getEntitiesOfClass(AbstractArrow.class,rider.getBoundingBox().inflate(24),a->a.getOwner()==rider).stream().findFirst().orElse(null);
            var arrow=first[0];if(arrow!=null&&!arrow.isRemoved()&&arrow.tickCount<=6&&arrow.getDeltaMovement().lengthSqr()>.01){forward[0]++;reversed[0]|=arrow.getDeltaMovement().z<=0;}
        });
        h.runAtTickTime(220,()->{BuildupMobTweaks.LOGGER.info("E3_ACCEPT_FORWARD samples={} reversed={} hp={} horseHP={}",forward[0],reversed[0],target.getHealth(),horse.getHealth());
            h.assertTrue(first[0]!=null&&forward[0]>=2&&!reversed[0]&&target.getHealth()<1000&&horse.getHealth()==1000,"Original mounted bow Goal sends a real forward arrow into the enemy without a QA redirect");
            h.getLevel().getEntitiesOfClass(AbstractArrow.class,rider.getBoundingBox().inflate(24),a->a.getOwner()==rider).forEach(Entity::discard);
            RidingRelationTests.clean(rider,horse,target);h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=75) public void activeCooldownCancellationPreservesUnownedStanding(GameTestHelper h){
        SkeletonBehaviorTests.floor(h,true);var rider=RidingRelationTests.mob(h,EntityTypes.SKELETON,8,8);var horse=RidingRelationTests.mob(h,EntityTypes.SKELETON_HORSE,8,8);
        // Fix only the fixture's random target-selector input, retaining native scheduling and deadlines.
        rider.getRandom().setSeed(441100);
        horse.setTamed(true);rider.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);horse.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);
        rider.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.BOW));rider.addTag("buildupmobtweaks:disable_skeleton_weapon_switching");rider.addTag("buildupmobtweaks:disable_skeleton_sniping");
        rider.startRiding(horse,true,false);rider.getAttached(RidingState.DATA).putBoolean("horse",true);var target=RidingRelationTests.mob(h,EntityTypes.IRON_GOLEM,8,16);target.setNoAi(true);
        h.runAtTickTime(30,()->{BuildupMobTweaks.LOGGER.info("E3_ACCEPT_COOLDOWN tick={} target={} fixture={} active={} phase={} standing={}",rider.tickCount,rider.getTarget()==null?"none":rider.getTarget().getUUID(),target.getUUID(),RidingState.runtime(rider).action.active(),RidingState.runtime(rider).phase,horse.isStanding());h.assertTrue(RidingState.runtime(rider).action.active()&&RidingState.runtime(rider).phase.equals("cooldown"),"Actual scheduled Goal is waiting, without owning a warning");horse.setStanding(60);});
        h.runAtTickTime(40,()->rider.setNoAi(true));
        h.runAtTickTime(55,()->{h.assertTrue(horse.isStanding()&&horse.getStandAnim(1)>.9,"Cancelling a cooldown does not erase an unrelated native standing input");RidingRelationTests.clean(rider,horse,target);h.succeed();});
    }
    private void trapRevenge(GameTestHelper h,boolean variants){
        SkeletonBehaviorTests.floor(h,true);var fixture=SkeletonBehaviorTests.player(h);var player=fixture.player();
        var root=RidingRelationTests.mob(h,EntityTypes.SKELETON_HORSE,8,8);root.setTrap(true);
        if(!variants)root.addTag("buildupmobtweaks:disable_skeleton_trap_variants");
        player.setGameMode(GameType.SURVIVAL);player.snapTo(root.position().add(0,0,5),0,0);
        List<Mob> event=new ArrayList<>();Mob[] pair=new Mob[2];Arrow[] projectile={null};boolean[] revenge={false};
        h.runAtTickTime(10,()->{
            player.setGameMode(GameType.SPECTATOR);
            event.addAll(h.getLevel().getEntitiesOfClass(Mob.class,root.getBoundingBox().inflate(24),m->root.getUUID().toString().equals(m.getAttached(RidingState.TRAP_ORIGIN))));
            var riders=event.stream().filter(m->m instanceof AbstractSkeleton).toList();
            h.assertTrue(riders.size()==4,"Actual native trap produced exactly four riders");
            if(variants){h.assertTrue(riders.stream().map(Entity::getType).distinct().count()==4,"Native trap event really produced the four variant types");
                pair[0]=riders.stream().filter(m->m.getType()==EntityTypes.STRAY).findFirst().orElseThrow();pair[1]=riders.stream().filter(m->m.getType()==EntityTypes.WITHER_SKELETON).findFirst().orElseThrow();
            }else{h.assertTrue(riders.stream().allMatch(m->m.getType()==EntityTypes.SKELETON),"Disabled variant gate leaves four original native skeletons");pair[0]=riders.get(0);pair[1]=riders.get(1);}
            for(var m:event){m.setNoAi(true);m.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);m.setHealth(1000);m.setInvulnerableTime(0);}
            pair[0].getVehicle().teleportTo(root.position().add(0,0,-4).x,root.getY(),root.getZ()-4);
            pair[1].getVehicle().teleportTo(root.getX()+10,root.getY(),root.getZ()-4);
        });
        h.runAtTickTime(75,()->{
            var victim=pair[0];var attacker=pair[1];victim.setNoAi(false);victim.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);
            // One real projectile is a controlled misfire input; retaliation is acquired by the native target selector.
            // Keep one native flight tick so post-teleport passenger settling cannot move the target out of the fixture line.
            // Native trap variants may roll a baby body. Aim inside the actual upper
            // rider bounds rather than a fixed adult offset that can hit its mount.
            var aim=new Vec3(victim.getX(),victim.getY(.8),victim.getZ());
            var a=new Arrow(h.getLevel(),attacker,new ItemStack(Items.ARROW),null);a.setNoGravity(true);a.setPos(aim.add(0,0,1));a.setDeltaMovement(0,0,-1);projectile[0]=a;h.getLevel().addFreshEntity(a);
            BuildupMobTweaks.LOGGER.info("R5_TRAP_INPUT baby={} bounds={} arrow={}",victim.isBaby(),victim.getBoundingBox(),a.position());
        });
        h.onEachTick(()->{if(pair[0]!=null)revenge[0]|=pair[0].getTarget()==pair[1];});
        h.runAtTickTime(115,()->{
            BuildupMobTweaks.LOGGER.info("E3_ACCEPT_TRAP variants={} victim={} attacker={} hp={} revenge={} sameTree={}",variants,pair[0].getType(),pair[1].getType(),pair[0].getHealth(),revenge[0],pair[0].isPassengerOfSameVehicle(pair[1]));
            h.assertTrue(pair[0].getHealth()<1000&&pair[0].getLastHurtByMob()==pair[1]&&revenge[0],"Real arrow damage naturally invokes original revenge between riders on different mounts");
            h.assertTrue(!pair[0].isPassengerOfSameVehicle(pair[1]),"A trap-origin marker is not a shared riding tree or an alliance");
            event.forEach(Entity::discard);RidingRelationTests.clean(root,projectile[0]);fixture.close();h.succeed();
        });
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=140) public void mixedNativeTrapRidersKeepOriginalMisfireRevenge(GameTestHelper h){trapRevenge(h,true);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=140) public void originalFourSkeletonTrapAlsoHasMisfireRevenge(GameTestHelper h){trapRevenge(h,false);}
}
