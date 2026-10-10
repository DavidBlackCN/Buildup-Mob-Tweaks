package com.davidblackcn.buildupmobtweaks;

import com.davidblackcn.buildupmobtweaks.rebuild.ecology.*;
import com.davidblackcn.buildupmobtweaks.qa.*;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.commands.arguments.EntityAnchorArgument.Anchor;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.entity.projectile.arrow.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import java.util.*;

/** Inputs/observations only: actual native server ticks, GoalSelector and Brain. */
public class EcologyBehaviorTests {
    private static final String ENV="buildupmobtweaks:r4e3b",ARENA="buildupmobtweaks:r2a_arena";
    private static <T extends Mob> T mob(GameTestHelper h,EntityType<T> t,int x,int z){return RidingRelationTests.mob(h,t,x,z);}
    private static void off(Mob m,String id){m.addTag("buildupmobtweaks:disable_"+id);}
    private static void proof(String name,Mob m,String detail){var r=EcologyState.runtime(m);BuildupMobTweaks.LOGGER.info("R4E3B_PROOF scenario={} uuid={} ticks={} hp={} ink={} gazes={} escapes={} shares={} heals={} dodges={} defenses={} horns={} queries={} search={} searchQueries={} {}",name,m.getUUID(),m.tickCount,m.getHealth(),r.ink,r.gazes,r.escapes,r.shares,r.heals,r.dodges,r.defenses,r.horns,r.queries,r.search,r.searchQueries,detail);}
    private static void water(GameTestHelper h){SkeletonBehaviorTests.floor(h,true);for(int x=5;x<=11;x++)for(int z=5;z<=11;z++)for(int y=2;y<=4;y++)h.setBlock(new BlockPos(x,y,z),x==5||x==11||z==5||z==11?Blocks.GLASS:Blocks.WATER);}
    private void ink(GameTestHelper h,boolean glow,boolean disabled){water(h);var s=glow?mob(h,EntityTypes.GLOW_SQUID,8,8):mob(h,EntityTypes.SQUID,8,8);var a=mob(h,EntityTypes.IRON_GOLEM,8,13);a.setNoAi(true);if(disabled)off(s,"squid_ink_defense");
        h.runAtTickTime(5,()->s.hurtServer(h.getLevel(),s.damageSources().mobAttack(a),2));
        h.runAtTickTime(10,()->{var effect=glow?MobEffects.GLOWING:MobEffects.BLINDNESS;proof("ink_"+glow+"_"+disabled,s,"attackerEffect="+a.hasEffect(effect));h.assertTrue(s.getHealth()<1000,"Real native damage");h.assertTrue(disabled?!a.hasEffect(effect):a.hasEffect(effect),"Native ink actual effect obeys gate");h.assertTrue(EcologyState.runtime(s).ink==(disabled?0:1),"One cooldown-limited burst");RidingRelationTests.clean(s,a);h.succeed();});}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=25) public void squidRealInkBlinds(GameTestHelper h){ink(h,false,false);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=25) public void glowInkGlows(GameTestHelper h){ink(h,true,false);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=25) public void inkDisabledPreservesNativeDamage(GameTestHelper h){ink(h,false,true);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=35) public void inkCooldownPersistsOnActualReload(GameTestHelper h){water(h);var actors=new Mob[]{mob(h,EntityTypes.SQUID,8,8)};var a=mob(h,EntityTypes.IRON_GOLEM,8,13);a.setNoAi(true);
        h.runAtTickTime(3,()->actors[0].hurtServer(h.getLevel(),actors[0].damageSources().mobAttack(a),2));
        h.runAtTickTime(8,()->actors[0]=CombatTestWorld.reload(h,actors[0]));
        h.runAtTickTime(10,()->{a.removeAllEffects();actors[0].hurtServer(h.getLevel(),actors[0].damageSources().mobAttack(a),2);});
        h.runAtTickTime(15,()->{proof("ink_reload",actors[0],"attackerEffect="+a.hasEffect(MobEffects.BLINDNESS));h.assertTrue(!a.hasEffect(MobEffects.BLINDNESS),"Persistent ink cooldown prevents reload burst");RidingRelationTests.clean(actors[0],a);h.succeed();});}
    private void gaze(GameTestHelper h,String mode){water(h);var s=mob(h,EntityTypes.GLOW_SQUID,8,8);var f=SkeletonBehaviorTests.player(h);var p=f.player();p.snapTo(h.absolutePos(new BlockPos(8,3,9)),0,0);if(mode.equals("off"))off(s,"glow_squid_gaze");if(mode.equals("creative"))p.setGameMode(GameType.CREATIVE);if(mode.equals("spectator"))p.setGameMode(GameType.SPECTATOR);
        if(mode.equals("wall"))for(int x=5;x<12;x++)for(int y=2;y<6;y++)h.setBlock(new BlockPos(x,y,9),Blocks.STONE);
        if(mode.equals("far"))p.snapTo(s.position().add(0,0,20),0,0);
        h.onEachTick(()->{if(!p.isRemoved())p.lookAt(Anchor.EYES,mode.equals("away")?p.getEyePosition().add(0,0,10):s.getEyePosition());});
        h.runAtTickTime(75,()->{boolean yes=mode.equals("on");proof("gaze_"+mode,s,"actualNightVision="+p.hasEffect(MobEffects.NIGHT_VISION));h.assertTrue(p.hasEffect(MobEffects.NIGHT_VISION)==yes,"Actual sight effect obeys range LOS gaze player mode and gate");s.discard();f.close();h.succeed();});}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=95) public void actualPlayerGazeReceivesNightVision(GameTestHelper h){gaze(h,"on");}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=95) public void gazeGateOff(GameTestHelper h){gaze(h,"off");}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=95) public void gazeBlockedByWall(GameTestHelper h){gaze(h,"wall");}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=95) public void gazeFacingAway(GameTestHelper h){gaze(h,"away");}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=95) public void gazeDistanceLimit(GameTestHelper h){gaze(h,"far");}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=95) public void gazeCreativeExcluded(GameTestHelper h){gaze(h,"creative");}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=95) public void gazeSpectatorExcluded(GameTestHelper h){gaze(h,"spectator");}
    private void chicken(GameTestHelper h,String mode){SkeletonBehaviorTests.floor(h,true);var c=mob(h,EntityTypes.CHICKEN,12,12);c.getRandom().setSeed(80012);var start=c.position();var f=SkeletonBehaviorTests.player(h);var p=f.player();p.snapTo(c.position().add(0,0,3),0,0);
        if(mode.equals("food"))p.setItemInHand(InteractionHand.MAIN_HAND,Items.WHEAT_SEEDS.getDefaultInstance());if(mode.equals("off"))off(c,"chicken_threat_escape");if(mode.equals("creative"))p.setGameMode(GameType.CREATIVE);
        h.runAtTickTime(55,()->{var r=EcologyState.runtime(c);proof("chicken_"+mode,c,"moved="+c.position().distanceTo(start));h.assertTrue(mode.equals("on")?r.escapes>0&&c.position().distanceTo(start)>.5:r.escapes==0,"Real native escape/feeding/player exclusion");c.discard();f.close();h.succeed();});}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=70) public void chickenNativeGoalMovesFromPlayer(GameTestHelper h){chicken(h,"on");}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=70) public void chickenFoodPlayerIsNotThreat(GameTestHelper h){chicken(h,"food");}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=70) public void chickenGateOff(GameTestHelper h){chicken(h,"off");}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=70) public void chickenCreativeIsNotThreat(GameTestHelper h){chicken(h,"creative");}
    private void group(GameTestHelper h,boolean disabled){SkeletonBehaviorTests.floor(h,true);var c=mob(h,EntityTypes.COW,8,8);var other=mob(h,EntityTypes.COW,10,8);var a=mob(h,EntityTypes.IRON_GOLEM,8,6);a.setNoAi(true);var initial=other.position();c.getRandom().setSeed(81000);other.getRandom().setSeed(81001);if(disabled)off(c,"animal_group_panic");
        h.runAtTickTime(5,()->c.hurtServer(h.getLevel(),c.damageSources().mobAttack(a),2));
        h.runAtTickTime(70,()->{proof("herd_"+disabled,c,"otherEscapes="+EcologyState.runtime(other).escapes+" otherMoved="+other.position().distanceTo(initial));h.assertTrue(disabled?EcologyState.runtime(c).shares==0:EcologyState.runtime(c).shares>0&&EcologyState.runtime(other).escapes>0&&other.position().distanceTo(initial)>.5,"Native panic start propagates one real movement signal");h.assertTrue(EcologyState.runtime(other).shares==0,"No recursive repropagation");RidingRelationTests.clean(c,other,a);h.succeed();});}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=90) public void realPanicMakesHerdMove(GameTestHelper h){group(h,false);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=90) public void panicShareOffKeepsOwnPanic(GameTestHelper h){group(h,true);}
    private void recovery(GameTestHelper h,String mode){SkeletonBehaviorTests.floor(h,true);var w=new Mob[]{mode.startsWith("horse")?mob(h,EntityTypes.HORSE,8,8):mob(h,EntityTypes.WOLF,8,8)};var f=SkeletonBehaviorTests.player(h);var p=f.player();p.setGameMode(GameType.CREATIVE);if(w[0] instanceof TamableAnimal pet){pet.setTame(true,false);pet.setOwner(p);pet.setOrderedToSit(true);}else{var horse=(net.minecraft.world.entity.animal.equine.AbstractHorse)w[0];horse.setTamed(true);horse.setOwner(p);horse.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);}if(mode.equals("off"))off(w[0],"tameable_recovery");if(mode.equals("wild"))((TamableAnimal)w[0]).setTame(false,false);
        h.runAtTickTime(5,()->w[0].hurtServer(h.getLevel(),w[0].damageSources().generic(),mode.startsWith("horse")?20:2));
        h.runAtTickTime(30,()->{if(mode.equals("reload")||mode.equals("horse_reload"))w[0]=CombatTestWorld.reload(h,w[0]);if(mode.equals("horse_ridden"))p.startRiding(w[0],true,false);if(mode.equals("unknown")){var d=new CompoundTag();d.putInt("version",99);d.putString("future","keep");w[0].setAttached(EcologyState.DATA,d);}});
        final float[] hurt={0};h.runAtTickTime(80,()->{hurt[0]=w[0].getHealth();h.assertTrue(EcologyState.runtime(w[0]).heals==0,"Damage grace period survives normal ticks and reload");});
        h.runAtTickTime(250,()->{boolean yes=mode.equals("on")||mode.equals("reload")||mode.equals("horse")||mode.equals("horse_reload");proof("recovery_"+mode,w[0],"hurtHP="+hurt[0]);h.assertTrue(yes?w[0].getHealth()>hurt[0]&&EcologyState.runtime(w[0]).heals>0:EcologyState.runtime(w[0]).heals==0&&(mode.startsWith("horse")||w[0].getHealth()==hurt[0]),"Actual pet healing obeys owner tame gate and unknown version");if(mode.equals("unknown"))h.assertTrue(w[0].getAttached(EcologyState.DATA).getStringOr("future","").equals("keep"),"Unknown data unchanged");w[0].discard();f.close();h.succeed();});}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=270) public void petActualHealAfterGrace(GameTestHelper h){recovery(h,"on");}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=270) public void petReloadCannotEraseGrace(GameTestHelper h){recovery(h,"reload");}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=270) public void petRecoveryOff(GameTestHelper h){recovery(h,"off");}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=270) public void wildPetDoesNotRecover(GameTestHelper h){recovery(h,"wild");}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=270) public void unknownEcologyDataUntouched(GameTestHelper h){recovery(h,"unknown");}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=270) public void ownedTamedHorseActuallyRecovers(GameTestHelper h){recovery(h,"horse");}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=270) public void horseReloadKeepsRecoveryGrace(GameTestHelper h){recovery(h,"horse_reload");}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=270) public void horseRidingPausesAddedRecovery(GameTestHelper h){recovery(h,"horse_ridden");}
    private void dodge(GameTestHelper h,String mode){SkeletonBehaviorTests.floor(h,true);var c=mob(h,EntityTypes.CAT,8,8);var a=mob(h,EntityTypes.IRON_GOLEM,8,18);a.setNoAi(true);var initial=c.position();if(mode.equals("off"))off(c,"cat_projectile_dodge");if(mode.equals("sit")){c.setTame(true,false);c.setOrderedToSit(true);}c.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);
        final AbstractArrow[] arrow={null};h.runAtTickTime(8,()->{arrow[0]=new Arrow(h.getLevel(),a,Items.ARROW.getDefaultInstance(),null);arrow[0].setNoGravity(true);arrow[0].setPos(c.getBoundingBox().getCenter().add(0,0,4));arrow[0].setDeltaMovement(0,0,-.5);h.getLevel().addFreshEntity(arrow[0]);});
        h.runAtTickTime(30,()->{var r=EcologyState.runtime(c);proof("dodge_"+mode,c,"displacement="+c.position().distanceTo(initial)+" projectileRemoved="+arrow[0].isRemoved());h.assertTrue(mode.equals("on")?r.dodges>0&&c.position().distanceTo(initial)>.1:r.dodges==0&&c.getHealth()<1000,"Actual trajectory triggers movement; disabled/sitting retain native projectile damage");RidingRelationTests.clean(c,a,arrow[0]);h.succeed();});}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=45) public void catActuallyMovesFromIncomingArrow(GameTestHelper h){dodge(h,"on");}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=45) public void catGateOffArrowReallyHurts(GameTestHelper h){dodge(h,"off");}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=45) public void sittingCatArrowReallyHurts(GameTestHelper h){dodge(h,"sit");}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=45) public void catDoesNotDodgeOntoHotFloor(GameTestHelper h){
        SkeletonBehaviorTests.floor(h,true);for(int x=4;x<=12;x++)for(int z=5;z<=11;z++)if(x!=8)h.setBlock(new BlockPos(x,1,z),Blocks.MAGMA_BLOCK);
        var c=mob(h,EntityTypes.CAT,8,8);var owner=mob(h,EntityTypes.IRON_GOLEM,8,18);owner.setNoAi(true);c.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);AbstractArrow[] arrows={null};
        h.runAtTickTime(8,()->{var a=new Arrow(h.getLevel(),owner,Items.ARROW.getDefaultInstance(),null);a.setNoGravity(true);a.setPos(c.getBoundingBox().getCenter().add(0,0,4));a.setDeltaMovement(0,0,-.5);arrows[0]=a;h.getLevel().addFreshEntity(a);});
        h.runAtTickTime(30,()->{proof("hot_floor_rejected",c,"arrowRemoved="+arrows[0].isRemoved());h.assertTrue(EcologyState.runtime(c).dodges==0&&c.getHealth()<1000,"Actual unsafe floor rejects lateral dodge while native projectile damage remains");RidingRelationTests.clean(c,owner,arrows[0]);h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=45) public void dodgingCatStillTakesRealArrowDamage(GameTestHelper h){
        SkeletonBehaviorTests.floor(h,true);var c=mob(h,EntityTypes.CAT,8,8);var owner=mob(h,EntityTypes.IRON_GOLEM,8,18);owner.setNoAi(true);c.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);AbstractArrow[] arrows={null};
        h.runAtTickTime(8,()->{var a=new Arrow(h.getLevel(),owner,Items.ARROW.getDefaultInstance(),null);a.setNoGravity(true);a.setPos(c.getBoundingBox().getCenter().add(0,0,4));a.setDeltaMovement(0,0,-.5);arrows[0]=a;h.getLevel().addFreshEntity(a);});
        h.runAtTickTime(12,()->{h.assertTrue(EcologyState.runtime(c).dodges>0,"Native dodge has actually started");arrows[0].setPos(c.getBoundingBox().getCenter().add(0,0,1));arrows[0].setDeltaMovement(0,0,-1);});
        h.runAtTickTime(20,()->{proof("no_dodge_immunity",c,"arrowRemoved="+arrows[0].isRemoved());h.assertTrue(c.getHealth()<1000,"Actual projectile collision still damages during dodge window");RidingRelationTests.clean(c,owner,arrows[0]);h.succeed();});
    }
    private void trader(GameTestHelper h,boolean disabled){SkeletonBehaviorTests.floor(h,true);var t=mob(h,EntityTypes.WANDERING_TRADER,8,8);var a=mob(h,EntityTypes.IRON_GOLEM,8,11);a.setNoAi(true);t.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);if(disabled)off(t,"trader_self_defense");
        h.runAtTickTime(5,()->t.hurtServer(h.getLevel(),t.damageSources().mobAttack(a),2));h.runAtTickTime(15,()->h.assertTrue(!a.hasEffect(MobEffects.BLINDNESS),"Actual windup precedes effect"));
        h.runAtTickTime(38,()->{proof("trader_"+disabled,t,"attackerBlind="+a.hasEffect(MobEffects.BLINDNESS)+" nativeHand="+t.getMainHandItem()+" nativeEffects="+t.getActiveEffects());h.assertTrue(a.hasEffect(MobEffects.BLINDNESS)!=disabled,"Actual scheduled defense honors gate");h.assertTrue(t.getMainHandItem().isEmpty()||t.getMainHandItem().is(Items.POTION)||t.getMainHandItem().is(Items.MILK_BUCKET),"Only native drinking items may occupy the main hand");h.assertTrue(h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion.class,t.getBoundingBox().inflate(16),p->p.getOwner()==t).isEmpty(),"No generated defense splash projectile");RidingRelationTests.clean(t,a);h.succeed();});}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=55) public void traderNativeGoalBlindsAfterWindup(GameTestHelper h){trader(h,false);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=55) public void traderDisabledNativeHurt(GameTestHelper h){trader(h,true);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=60) public void traderMidWindupOffNeverReplaysOldDamage(GameTestHelper h){
        SkeletonBehaviorTests.floor(h,true);var t=mob(h,EntityTypes.WANDERING_TRADER,8,8);var a=mob(h,EntityTypes.IRON_GOLEM,8,11);a.setNoAi(true);t.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);
        h.runAtTickTime(5,()->t.hurtServer(h.getLevel(),t.damageSources().mobAttack(a),2));h.runAtTickTime(12,()->off(t,"trader_self_defense"));h.runAtTickTime(18,()->t.removeTag("buildupmobtweaks:disable_trader_self_defense"));
        h.runAtTickTime(45,()->{h.assertTrue(!a.hasEffect(MobEffects.BLINDNESS)&&EcologyState.runtime(t).defenses==0,"Actual disable cancels windup; re-enable does not replay old hit");RidingRelationTests.clean(t,a);h.succeed();});
    }
    @GameTest(environment="buildupmobtweaks:r4e3b_config",structure=ARENA,maxTicks=60) public void ecologyActualMasterAndIndependentConfigOff(GameTestHelper h){
        SkeletonBehaviorTests.floor(h,true);var cfg=BuildupMobTweaks.config();boolean master=cfg.general.enabled.get(),ink=cfg.passive.squid_ink_defense.get();cfg.general.enabled.setAndUpdate(false);
        var t=mob(h,EntityTypes.WANDERING_TRADER,8,8);var a=mob(h,EntityTypes.IRON_GOLEM,8,11);a.setNoAi(true);t.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);
        h.runAtTickTime(5,()->t.hurtServer(h.getLevel(),t.damageSources().mobAttack(a),2));
        h.runAtTickTime(20,()->{cfg.general.enabled.setAndUpdate(true);cfg.passive.squid_ink_defense.setAndUpdate(false);});
        h.runAtTickTime(42,()->{try{h.assertTrue(t.getHealth()<1000&&!a.hasEffect(MobEffects.BLINDNESS)&&EcologyState.runtime(t).defenses==0,"Master off keeps native damage and no stale defense on resume");var f=new com.davidblackcn.buildupmobtweaks.feature.FeatureRegistry(cfg);h.assertTrue(!f.isEnabled(com.davidblackcn.buildupmobtweaks.feature.FeatureId.SQUID_INK_DEFENSE)&&f.isEnabled(com.davidblackcn.buildupmobtweaks.feature.FeatureId.GLOW_SQUID_GAZE),"Independent synced config switch does not disable gaze");proof("actual_master_config",t,"nativeDamage=true noReplay=true independentGate=true");RidingRelationTests.clean(t,a);h.succeed();}finally{cfg.general.enabled.setAndUpdate(master);cfg.passive.squid_ink_defense.setAndUpdate(ink);}});
    }
    private void horn(GameTestHelper h,String mode){SkeletonBehaviorTests.floor(h,true);var z=mob(h,EntityTypes.ZOMBIE,8,8);z.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);var f=SkeletonBehaviorTests.player(h);var p=f.player();p.snapTo(z.position().add(0,0,20),0,0);p.setItemInHand(InteractionHand.MAIN_HAND,Items.GOAT_HORN.getDefaultInstance());if(mode.equals("off"))off(z,"goat_horn_aggro");if(mode.equals("creative"))p.setGameMode(GameType.CREATIVE);if(mode.equals("far"))p.snapTo(z.position().add(0,0,30),0,0);
        p.setGameMode(GameType.SPECTATOR);
        h.runAtTickTime(5,()->{p.setGameMode(mode.equals("creative")?GameType.CREATIVE:GameType.SURVIVAL);p.getMainHandItem().use(h.getLevel(),p,InteractionHand.MAIN_HAND);});
        h.runAtTickTime(8,()->{proof("horn_"+mode,z,"actualUsing="+p.isUsingItem()+" target="+z.getTarget());h.assertTrue(p.isUsingItem(),"Actual native instrument use started");h.assertTrue(mode.equals("on")?EcologyState.runtime(z).horns>0&&z.getTarget()==p:EcologyState.runtime(z).horns==0,"Bounded actual horn aggro honors range mode gate");off(z,"goat_horn_aggro");p.setGameMode(GameType.SPECTATOR);});
        h.runAtTickTime(12,()->{h.assertTrue(EcologyState.runtime(z).hornTarget==null,"Owned target released on disable and spectator");z.discard();f.close();h.succeed();});}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=25) public void hornActualNativeUseAttracts(GameTestHelper h){horn(h,"on");}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=25) public void hornGateOff(GameTestHelper h){horn(h,"off");}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=25) public void hornCreativeExcluded(GameTestHelper h){horn(h,"creative");}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=25) public void hornOutOfRange(GameTestHelper h){horn(h,"far");}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=55) public void hornPreservesNativeNonPlayerTarget(GameTestHelper h){
        SkeletonBehaviorTests.floor(h,true);var z=mob(h,EntityTypes.ZOMBIE,8,8);var g=mob(h,EntityTypes.IRON_GOLEM,8,11);z.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);g.setNoAi(true);
        h.runAtTickTime(30,()->{h.assertTrue(z.getTarget()==g,"Original selector has naturally acquired non-player target before player arrives");var f=SkeletonBehaviorTests.player(h);var p=f.player();p.snapTo(z.position().add(0,0,20),0,0);p.setItemInHand(InteractionHand.MAIN_HAND,Items.GOAT_HORN.getDefaultInstance());p.getMainHandItem().use(h.getLevel(),p,InteractionHand.MAIN_HAND);
            h.assertTrue(z.getTarget()==g&&EcologyState.runtime(z).horns==0,"Actual instrument use preserves native non-player target; no synthetic setTarget fixture");RidingRelationTests.clean(z,g);f.close();h.succeed();});
    }
    private static void fixturePlains(GameTestHelper h,Mob m){
        var chunk=h.getLevel().getChunkSource().getChunkNow(m.blockPosition().getX()>>4,m.blockPosition().getZ()>>4);
        var plains=h.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.BIOME).getOrThrow(net.minecraft.world.level.biome.Biomes.PLAINS);
        // Native helper.setBiome uses a larger fillbiome box containing unavailable border chunks.
        // Fixture writes only an already loaded chunk; production remains strictly read-only.
        chunk.fillBiomesFromNoise((x,y,z)->plains);
    }
    private void search(GameTestHelper h,String mode){SkeletonBehaviorTests.floor(h,true);var s=new Sniffer[]{mob(h,EntityTypes.SNIFFER,8,8)};s[0].getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);var f=SkeletonBehaviorTests.player(h);var p=f.player();p.snapTo(s[0].position().add(0,0,4),0,0);p.setShiftKeyDown(true);p.setItemInHand(InteractionHand.MAIN_HAND,(mode.equals("biome")?Items.SUNFLOWER:Items.IRON_INGOT).getDefaultInstance());
        h.setBlock(new BlockPos(8,1,8),Blocks.IRON_ORE);
        if(mode.equals("biome"))fixturePlains(h,s[0]);
        if(mode.equals("off"))off(s[0],"sniffer_search");
        h.runAtTickTime(5,()->s[0].interact(p,InteractionHand.MAIN_HAND,Vec3.ZERO));
        h.runAtTickTime(8,()->{if(mode.equals("cancel"))p.setShiftKeyDown(false);if(mode.equals("reload"))s[0]=CombatTestWorld.reload(h,s[0]);});
        final int[] before={0};h.onEachTick(()->{int queries=EcologyState.runtime(s[0]).searchQueries;h.assertTrue(queries-before[0]<=32,"Bounded per-request query delta");before[0]=queries;});
        h.runAtTickTime(98,()->{var r=EcologyState.runtime(s[0]);proof("sniffer_"+mode,s[0],"itemCount="+p.getMainHandItem().getCount()+" active="+SnifferSearch.active(h.getLevel().getServer()));h.assertTrue(mode.equals("off")?r.searchQueries==0:mode.equals("cancel")?r.search.equals("cancelled"):mode.equals("reload")?!r.search.equals("searching"):r.search.equals("found"),"Actual interaction produces bounded loaded-world result or cancellation");h.assertTrue(p.getMainHandItem().getCount()==1,"Search never consumes or creates items");s[0].discard();f.close();h.succeed();});}
    @GameTest(environment="buildupmobtweaks:r4e3b_search",structure=ARENA,maxTicks=120) public void snifferActualInteractionFindsOre(GameTestHelper h){search(h,"on");}
    @GameTest(environment="buildupmobtweaks:r4e3b_search",structure=ARENA,maxTicks=120) public void snifferActualBiomeSearchLoadedOnly(GameTestHelper h){search(h,"biome");}
    @GameTest(environment="buildupmobtweaks:r4e3b_search",structure=ARENA,maxTicks=120) public void snifferStopsWhenPlayerStopsSneaking(GameTestHelper h){search(h,"cancel");}
    @GameTest(environment="buildupmobtweaks:r4e3b_search",structure=ARENA,maxTicks=120) public void snifferReloadDropsPendingRequest(GameTestHelper h){search(h,"reload");}
    @GameTest(environment="buildupmobtweaks:r4e3b_search",structure=ARENA,maxTicks=120) public void snifferGateOffKeepsInteraction(GameTestHelper h){search(h,"off");}

    @GameTest(environment="buildupmobtweaks:r4e3b_budget",structure=ARENA,maxTicks=45)
    public void snifferEightRequestsGlobalBudgetAndCancellation(GameTestHelper h){
        SkeletonBehaviorTests.floor(h,true);var f=SkeletonBehaviorTests.player(h);var p=f.player();p.snapTo(h.absolutePos(new BlockPos(12,2,12)),0,0);p.setShiftKeyDown(true);p.setItemInHand(InteractionHand.MAIN_HAND,Items.IRON_INGOT.getDefaultInstance());
        var actors=new ArrayList<Sniffer>();for(int i=0;i<9;i++){var s=mob(h,EntityTypes.SNIFFER,12+i%3,12+i/3);s.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);actors.add(s);}
        h.runAtTickTime(5,()->{for(var s:actors)s.interact(p,InteractionHand.MAIN_HAND,Vec3.ZERO);h.assertTrue(SnifferSearch.active(h.getLevel().getServer())==8,"Exactly eight native interaction requests admitted");h.assertTrue(EcologyState.runtime(actors.get(8)).search.equals("queue_full"),"Ninth rejected without world query");});
        int[] previous={0};h.onEachTick(()->{int current=actors.stream().mapToInt(s->EcologyState.runtime(s).searchQueries).sum();h.assertTrue(current-previous[0]<=64,"Actual global query delta at most 64/tick");previous[0]=current;});
        h.runAtTickTime(16,()->p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY));
        h.runAtTickTime(20,()->{h.assertTrue(SnifferSearch.active(h.getLevel().getServer())==0,"All requests cancelled on actual held-item change");h.assertTrue(previous[0]>0,"Real requests performed loaded-world queries");for(var s:actors){proof("global_budget_cancel",s,"totalQueries="+previous[0]);s.discard();}f.close();h.succeed();});
    }
    @GameTest(environment="buildupmobtweaks:r4e3b_timeout",structure=ARENA,maxTicks=135)
    public void snifferUnavailableBiomeNeverLoadsChunksAndFinishes(GameTestHelper h){
        SkeletonBehaviorTests.floor(h,true);var s=mob(h,EntityTypes.SNIFFER,8,8);s.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);fixturePlains(h,s);var p=h.makeMockPlayer(GameType.SURVIVAL);p.snapTo(s.position().add(0,0,4),0,0);p.setShiftKeyDown(true);p.setItemInHand(InteractionHand.MAIN_HAND,Items.BAMBOO.getDefaultInstance());
        h.runAtTickTime(5,()->s.interact(p,InteractionHand.MAIN_HAND,Vec3.ZERO));
        var missing=new ArrayList<BlockPos>();h.runAtTickTime(3,()->{for(int x=-8;x<=8;x++)for(int z=-8;z<=8;z++){var pos=s.blockPosition().offset(x*16,0,z*16);if(h.getLevel().getChunkSource().getChunkNow(pos.getX()>>4,pos.getZ()>>4)==null)missing.add(pos);}});
        h.runAtTickTime(115,()->{var r=EcologyState.runtime(s);long loaded=missing.stream().filter(pos->h.getLevel().getChunkSource().getChunkNow(pos.getX()>>4,pos.getZ()>>4)!=null).count();proof("loaded_only_no_biome",s,"initiallyUnloaded="+missing.size()+" newlyLoaded="+loaded+" playerFixture=native_mock_without_view_tickets");h.assertTrue(!r.search.equals("searching")&&r.searchQueries<=289&&r.searchQueries>0,"Finite loaded-only not-found result");h.assertTrue(!missing.isEmpty()&&loaded==0,"Previously unavailable queried chunks stayed unloaded; no player view tickets");s.discard();p.discard();h.succeed();});
    }
    @GameTest(environment="buildupmobtweaks:r4e3b_queue_timeout",structure=ARENA,maxTicks=135) public void snifferEightRequestsActuallyTimeOut(GameTestHelper h){
        SkeletonBehaviorTests.floor(h,true);var f=SkeletonBehaviorTests.player(h);var p=f.player();p.snapTo(h.absolutePos(new BlockPos(12,2,12)),0,0);p.setShiftKeyDown(true);p.setItemInHand(InteractionHand.MAIN_HAND,Items.COAL.getDefaultInstance());var actors=new ArrayList<Sniffer>();
        for(int i=0;i<8;i++){var s=mob(h,EntityTypes.SNIFFER,12+i%3,12+i/3);s.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);actors.add(s);}
        h.runAtTickTime(5,()->{for(var s:actors)s.interact(p,InteractionHand.MAIN_HAND,Vec3.ZERO);});
        h.runAtTickTime(112,()->{h.assertTrue(SnifferSearch.active(h.getLevel().getServer())==0,"All timed requests retired without more world access");for(var s:actors){var r=EcologyState.runtime(s);proof("real_queue_timeout",s,"queryLimit=2601");h.assertTrue(r.search.equals("timeout")&&r.searchQueries>0&&r.searchQueries<2601,"Real queued request times out before full scan");s.discard();}f.close();h.succeed();});
    }
}
