package com.davidblackcn.buildupmobtweaks;

import com.davidblackcn.buildupmobtweaks.rebuild.riding.*;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.SkeletonHorse;
import net.minecraft.world.entity.projectile.arrow.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;

/** Real native projectile collision ticks; never invokes canHitEntity/onHit/Goal/entity.tick. */
public class RiderProjectileTests {
    private static final String ENV="buildupmobtweaks:r4e3a",ARENA="buildupmobtweaks:r2a_arena";
    private static SkeletonHorse horse(GameTestHelper h,int x,int z){var v=RidingRelationTests.mob(h,EntityTypes.SKELETON_HORSE,x,z);v.setNoAi(true);v.setTamed(true);return v;}
    private static boolean leftOwner(GameTestHelper h,AbstractArrow a){var o=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,h.getLevel().registryAccess());a.saveWithoutId(o);return o.buildResult().getBooleanOr("LeftOwner",false);}
    private static void assertLeftOwner(GameTestHelper h,AbstractArrow a){h.assertTrue(leftOwner(h,a),"Native projectile really left its owner before returning");}
    private void collision(GameTestHelper h,boolean trident,String mode){
        SkeletonBehaviorTests.floor(h,true);
        Mob owner=trident?RidingRelationTests.mob(h,EntityTypes.DROWNED,8,8):RidingRelationTests.mob(h,EntityTypes.SKELETON,8,8);
        owner.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);
        var mount=horse(h,8,8);owner.startRiding(mount,true,false);
        var other=horse(h,18,14);
        AbstractArrow a=trident?new ThrownTrident(h.getLevel(),owner,Items.TRIDENT.getDefaultInstance()):new Arrow(h.getLevel(),owner,Items.ARROW.getDefaultInstance(),null);
        a.pickup=AbstractArrow.Pickup.DISALLOWED;a.setNoGravity(true);a.setPos(owner.getEyePosition());a.setDeltaMovement(0,0,.45);h.getLevel().addFreshEntity(a);
        h.runAtTickTime(8,()->{assertLeftOwner(h,a);
            if(mode.equals("off"))owner.addTag("buildupmobtweaks:disable_rider_friendly_fire");
            if(mode.equals("vanilla"))owner.addTag("buildupmobtweaks:vanilla_ai");
            if(mode.equals("noai"))owner.setNoAi(true);
            if(mode.equals("ownerless"))a.setOwner(null);
            if(mode.equals("detached")||mode.equals("changed")){owner.stopRiding();owner.snapTo(other.position(),0,0);if(mode.equals("changed"))owner.startRiding(other,true,false);}
            var candidate=mode.equals("enemy")?other:mount;
            a.setPos(candidate.position().add(0,.3,4));a.setDeltaMovement(0,0,-1);
        });
        h.runAtTickTime(30,()->{var candidate=mode.equals("enemy")?other:mount;boolean protect=mode.equals("same");
            BuildupMobTweaks.LOGGER.info("R4E3A_RIDER_PROOF mode={} trident={} owner={} candidate={} hp={} skips={} removed={}",mode,trident,owner.getUUID(),candidate.getUUID(),candidate.getHealth(),RidingState.runtime(owner).projectileSkips,a.isRemoved());
            h.assertTrue(protect?candidate.getHealth()==1000&&RidingState.runtime(owner).projectileSkips>0:candidate.getHealth()<1000,"Actual collision protects only current enabled riding tree");
            RidingRelationTests.clean(a,owner,mount,other);h.succeed();
        });
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=45) public void returningArrowProtectsCurrentMount(GameTestHelper h){collision(h,false,"same");}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=45) public void returningTridentProtectsCurrentMount(GameTestHelper h){collision(h,true,"same");}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=45) public void arrowDisabledHitsCurrentMount(GameTestHelper h){collision(h,false,"off");}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=45) public void tridentDisabledHitsCurrentMount(GameTestHelper h){collision(h,true,"off");}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=45) public void vanillaAiArrowHitsCurrentMount(GameTestHelper h){collision(h,false,"vanilla");}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=45) public void noAiOwnerHasNoAddedProtection(GameTestHelper h){collision(h,false,"noai");}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=45) public void ownerlessArrowKeepsNativeCollision(GameTestHelper h){collision(h,false,"ownerless");}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=45) public void dismountedArrowHitsOldMount(GameTestHelper h){collision(h,false,"detached");}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=45) public void dismountedTridentHitsOldMount(GameTestHelper h){collision(h,true,"detached");}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=45) public void changedMountReleasesOldTree(GameTestHelper h){collision(h,false,"changed");}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=45) public void enemyMountArrowStillHits(GameTestHelper h){collision(h,false,"enemy");}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=45) public void enemyMountTridentStillHits(GameTestHelper h){collision(h,true,"enemy");}

    @GameTest(environment=ENV,structure=ARENA,maxTicks=60) public void playerOwnedArrowStillHitsOwnMount(GameTestHelper h){
        SkeletonBehaviorTests.floor(h,true);var f=SkeletonBehaviorTests.player(h);f.player().setGameMode(GameType.SPECTATOR);var mount=horse(h,8,8);f.player().startRiding(mount,true,false);
        var a=new Arrow(h.getLevel(),f.player(),Items.ARROW.getDefaultInstance(),null);a.setNoGravity(true);a.setPos(f.player().getEyePosition());a.setDeltaMovement(0,0,.45);h.getLevel().addFreshEntity(a);
        h.runAtTickTime(8,()->{assertLeftOwner(h,a);a.setPos(mount.position().add(0,.3,4));a.setDeltaMovement(0,0,-1);});
        h.runAtTickTime(30,()->{h.assertTrue(mount.getHealth()<1000,"Player shooter retains native own-mount collision");BuildupMobTweaks.LOGGER.info("R4E3A_RIDER_PROOF mode=player hp={}",mount.getHealth());RidingRelationTests.clean(a,mount);f.close();h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=240) public void nativeBowGoalArrowReallyReturnsPastOwnMount(GameTestHelper h){
        SkeletonBehaviorTests.floor(h,true);var rider=SkeletonBehaviorTests.skeleton(h,EntityTypes.SKELETON,8,8);rider.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);var mount=horse(h,8,8);rider.startRiding(mount,true,false);var target=RidingRelationTests.mob(h,EntityTypes.IRON_GOLEM,8,19);target.setNoAi(true);AbstractArrow[] redirected={null};
        h.onEachTick(()->{if(redirected[0]==null)for(var a:h.getLevel().getEntitiesOfClass(AbstractArrow.class,rider.getBoundingBox().inflate(24),p->p.getOwner()==rider&&p.tickCount>=2)){
            if(!leftOwner(h,a))continue;assertLeftOwner(h,a);redirected[0]=a;a.setNoGravity(true);a.setPos(mount.position().add(0,.3,4));a.setDeltaMovement(0,0,-1);break;}});
        h.runAtTickTime(220,()->{h.assertTrue(redirected[0]!=null&&RidingState.runtime(rider).projectileSkips>0&&mount.getHealth()==1000,"Original bow Goal releases a real arrow; native return collision skips own mount");BuildupMobTweaks.LOGGER.info("R4E3A_RIDER_PROOF mode=native_goal skips={} mountHP={} target={}",RidingState.runtime(rider).projectileSkips,mount.getHealth(),rider.getTarget());if(redirected[0]!=null)redirected[0].discard();RidingRelationTests.clean(rider,mount,target);h.succeed();});
    }
}
