package com.davidblackcn.buildupmobtweaks;
import com.davidblackcn.buildupmobtweaks.combat.*;
import com.davidblackcn.buildupmobtweaks.config.BuildupConfig;
import com.davidblackcn.buildupmobtweaks.feature.*;
import com.davidblackcn.buildupmobtweaks.mixin.SpellTimerAccess;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gamerules.GameRules;
import static com.davidblackcn.buildupmobtweaks.CombatTestWorld.*;
public class S2RegressionTests {
    private static <T extends Mob>T placed(GameTestHelper h,EntityType<T> type,int x,int z){var m=mob(h,type,x,z);h.getLevel().addFreshEntity(m);return m;}
    private static void force(Mob m,FeatureId id){var tag=m.getAttached(AdvancedHostiles.DATA).copy();tag.putString("trait",id.id().toString());m.setAttached(AdvancedHostiles.DATA,tag);}
    @GameTest(structure=ARENA)
    public void everyExtendedSwitchDisablesOnlyItsOwnDeclaration(GameTestHelper h)throws Exception{
        var cfg=new BuildupConfig();var registry=new FeatureRegistry(cfg);int count=0;
        for(var field:BuildupConfig.Extended.class.getFields()){
            if(!(field.get(cfg.hostile.extended) instanceof me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedBoolean value))continue;
            var id=FeatureId.valueOf(field.getName().toUpperCase(java.util.Locale.ROOT));h.assertTrue(registry.isEnabled(id),"Default enabled "+id);
            value.accept(false);h.assertFalse(registry.isEnabled(id),"Own gate disabled "+id);
            for(var other:BuildupConfig.Extended.class.getFields())if(!other.equals(field)&&other.get(cfg.hostile.extended) instanceof me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedBoolean)
                h.assertTrue(registry.isEnabled(FeatureId.valueOf(other.getName().toUpperCase(java.util.Locale.ROOT))),"Independent gate "+other.getName());
            value.accept(true);count++;
        }h.assertTrue(count>=80,"All extended switches exercised");h.succeed();
    }
    @GameTest(structure=ARENA)
    public void idleGroundMoveControlClearsResidualStrafeWithoutChangingCow(GameTestHelper h){
        floor(h,Blocks.STONE);var cow=placed(h,EntityTypes.COW,3,3);var zombie=placed(h,EntityTypes.ZOMBIE,7,3);
        for(var m:java.util.List.of(cow,zombie)){m.setOnGround(true);m.getMoveControl().strafe(0,.5f);m.getMoveControl().tick();h.assertTrue(m.xxa!=0,"Strafe sets lateral input");m.getMoveControl().tick();}
        h.assertTrue(cow.xxa!=0,"Vanilla WAIT retains lateral input, reproducing the bug");h.assertValueEqual(zombie.xxa,0f,"Enabled hostile fix clears input");cow.discard();zombie.discard();h.succeed();
    }
    @GameTest(structure=ARENA)
    public void strafingSkeletonKeepsVerticalLookControlTarget(GameTestHelper h){
        floor(h,Blocks.STONE);var m=placed(h,EntityTypes.SKELETON,3,3);var target=placed(h,EntityTypes.COW,8,3);target.setPos(target.getX(),target.getY()+1,target.getZ());m.setTarget(target);m.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.BOW));
        m.lookAt(target,30,30);m.getLookControl().tick();h.assertValueEqual(m.getXRot(),0f,"Vanilla direct look is reset by idle LookControl");
        var goal=new RangedBowAttackGoal<>(m,1,40,15);for(int i=0;i<25;i++){goal.tick();m.getLookControl().tick();}
        h.assertTrue(m.getXRot() < -1,"Real bow Goal maintains upward pitch while strafing");goal.stop();m.discard();target.discard();h.succeed();
    }
    @GameTest(structure=ARENA)
    public void sameSpeciesDamageStillHurtsButRetaliationDoesNotStart(GameTestHelper h){
        floor(h,Blocks.STONE);var a=placed(h,EntityTypes.ZOMBIFIED_PIGLIN,3,3);var b=placed(h,EntityTypes.ZOMBIFIED_PIGLIN,5,3);
        a.tickCount=10;a.hurtServer(h.getLevel(),a.damageSources().mobAttack(b),2);h.assertTrue(a.getHealth()<a.getMaxHealth(),"Damage is not cancelled");var goal=new HurtByTargetGoal(a);
        h.assertTrue(goal.canUse(),"26.3 HurtByTargetGoal accepts friendly aggressor before target filtering");goal.start();h.assertTrue(a.getTarget()==null,"Piglin anger target filtered");
        var golem=placed(h,EntityTypes.IRON_GOLEM,9,3);var snow=placed(h,EntityTypes.SNOW_GOLEM,12,3);golem.tickCount=10;golem.setLastHurtByMob(snow);var retaliate=new HurtByTargetGoal(golem);
        h.assertTrue(retaliate.canUse(),"Vanilla golem retaliation accepts another golem");retaliate.start();h.assertTrue(golem.getTarget()==null,"Golem friendly target filtered");a.discard();b.discard();golem.discard();snow.discard();h.succeed();
    }
    @GameTest(structure=ARENA)
    public void consumedStewReturnsItsActualContainer(GameTestHelper h){
        var m=placed(h,EntityTypes.PILLAGER,3,3);m.tickCount=200;m.setHealth(10);m.setItemSlot(EquipmentSlot.OFFHAND,new ItemStack(Items.MUSHROOM_STEW));var goal=HostileEquipment.foodGoal(m);
        h.assertTrue(goal.canUse(),"Owned stew is edible");goal.start();for(int i=0;i<80;i++)goal.tick();goal.stop();h.assertTrue(m.getOffhandItem().is(Items.BOWL),"Vanilla consumption returns bowl");h.assertValueEqual(m.getHealth(),12f,"Single heal");m.discard();h.succeed();
    }
    @GameTest(structure=ARENA)
    public void burrowReloadRestoresSurfacePhysicsAndKeepsCooldown(GameTestHelper h){
        floor(h,Blocks.STONE);var m=placed(h,EntityTypes.HUSK,3,3);var surface=m.position();var data=m.getAttached(ZombieCombat.DATA).copy();data.putBoolean("burrow_active",true);data.putDouble("burrow_x",surface.x);data.putDouble("burrow_y",surface.y);data.putDouble("burrow_z",surface.z);data.putLong("next",1234567);m.setAttached(ZombieCombat.DATA,data);m.noPhysics=true;m.setNoGravity(true);m.setPos(surface.add(0,-2,0));
        var loaded=reload(h,m);h.assertTrue(loaded.position().distanceTo(surface)<.01,"Reload restores saved surface");h.assertFalse(loaded.noPhysics||loaded.isNoGravity(),"Ordinary physics restored");h.assertFalse(loaded.getAttached(ZombieCombat.DATA).getBooleanOr("burrow_active",true),"Interrupted state cleared");h.assertValueEqual(loaded.getAttached(ZombieCombat.DATA).getLongOr("next",0),1234567L,"Existing cooldown preserved");loaded.discard();h.succeed();
    }
    @GameTest(structure=ARENA)
    public void totemCannotStartDuringAnotherSpellAndExpiresWithoutRefill(GameTestHelper h){
        var m=placed(h,EntityTypes.EVOKER,3,3);force(m,FeatureId.EVOKER_TOTEM);m.setHealth(5);((SpellTimerAccess)m).buildup$setSpellTimer(20);AdvancedHostiles.instance().totem(m);h.assertTrue(m.getOffhandItem().isEmpty(),"Other casting is not protected");
        ((SpellTimerAccess)m).buildup$setSpellTimer(0);AdvancedHostiles.instance().totem(m);h.assertTrue(AdvancedHostiles.marked(m.getOffhandItem()),"Own visible window starts");
        var data=m.getAttached(AdvancedHostiles.DATA).copy();data.putLong("totem_until",0);m.setAttached(AdvancedHostiles.DATA,data);var loaded=reload(h,m);AdvancedHostiles.instance().totem(loaded);AdvancedHostiles.instance().totem(loaded);
        h.assertTrue(loaded.getOffhandItem().isEmpty(),"Expired item removed after reload without refill");h.assertFalse(loaded.isCastingSpell(),"Owned spell state released");loaded.discard();h.succeed();
    }
    @GameTest(structure=ARENA)
    public void endermanComboCancelsOnTargetChangeAndKeepsCooldown(GameTestHelper h){
        floor(h,Blocks.STONE);var m=placed(h,EntityTypes.ENDERMAN,3,3);var t=placed(h,EntityTypes.COW,8,3);m.setTarget(t);force(m,FeatureId.ENDERMAN_COMBO);var goal=AdvancedHostiles.instance().skill(m);h.assertTrue(goal.canUse(),"Eligible combo");goal.start();for(int i=0;i<39;i++)goal.tick();h.assertValueEqual(t.getHealth(),t.getMaxHealth(),"Telegraph cannot hit early");m.setTarget(null);h.assertFalse(goal.canContinueToUse(),"Target loss cancels");goal.stop();m.setTarget(t);h.assertFalse(goal.canUse(),"Cancelled skill still spends cooldown");m.discard();t.discard();h.succeed();
    }
    @GameTest(structure=ARENA)
    public void boatEscapeChecksDestinationGroundAtVehicleHeight(GameTestHelper h){
        floor(h,Blocks.STONE);var m=placed(h,EntityTypes.ZOMBIE,5,5);var t=placed(h,EntityTypes.COW,10,5);var boat=EntityTypes.OAK_BOAT.create(h.getLevel(),EntitySpawnReason.COMMAND);boat.snapTo(m.position(),0,0);h.getLevel().addFreshEntity(boat);m.startRiding(boat);m.setTarget(t);
        GeneralHostileRules.tick(m);h.assertFalse(m.isPassenger(),"Safe nearby floor allows escape");h.assertTrue(h.getLevel().noCollision(m,m.getBoundingBox()),"Exit is unobstructed");h.assertFalse(HostileCombat.instance().ready(m,"seat_escape"),"Escape attempt rate limited");m.discard();t.discard();boat.discard();h.succeed();
    }
    @GameTest(structure=ARENA)
    public void slimeThrowWarnsAndReleasesOnlyOriginalPassenger(GameTestHelper h){
        floor(h,Blocks.STONE);var m=placed(h,EntityTypes.ZOMBIE,3,3);var t=placed(h,EntityTypes.COW,9,3);var slime=placed(h,EntityTypes.SLIME,3,3);slime.setSize(2,true);slime.startRiding(m);force(m,FeatureId.ZOMBIE_SLIME_CARRIER);var data=m.getAttached(AdvancedHostiles.DATA).copy();data.putString("companion",slime.getStringUUID());m.setAttached(AdvancedHostiles.DATA,data);m.setTarget(t);
        var goal=RidingCombat.slimeGoal(m);h.assertTrue(goal.canUse(),"Existing owned passenger ready");goal.start();for(int i=0;i<19;i++)goal.tick();h.assertTrue(slime.isPassenger(),"Warning retains slime");goal.tick();goal.stop();h.assertFalse(slime.isPassenger(),"Single original slime thrown");h.assertTrue(slime.getDeltaMovement().x>0,"Throw toward target");h.assertFalse(goal.canUse(),"No replacement manufactured");m.discard();t.discard();slime.discard();h.succeed();
    }
    @GameTest(structure=ARENA)
    public void parchedDodgeStopsResidualVelocityAfterFiniteSkill(GameTestHelper h){
        floor(h,Blocks.STONE);var m=placed(h,EntityTypes.PARCHED,3,3);var t=placed(h,EntityTypes.COW,11,3);m.setOnGround(true);m.setTarget(t);force(m,FeatureId.PARCHED_DODGE);var goal=SkeletonExtras.goal(m);h.assertTrue(goal.canUse(),"Dodge eligible");goal.start();for(int i=0;i<40;i++)goal.tick();h.assertFalse(goal.canContinueToUse(),"Dodge ends");goal.stop();h.assertValueEqual(m.getDeltaMovement().horizontalDistanceSqr(),0.0,"No perpetual lateral slide");m.discard();t.discard();h.succeed();
    }
    @GameTest(structure=ARENA)
    public void breezeBurstsRespectDifficultyAndBlockInteractionRule(GameTestHelper h){
        floor(h,Blocks.STONE);var m=placed(h,EntityTypes.BREEZE,5,5);var level=h.getLevel();var difficulty=level.getDifficulty();boolean grief=level.getGameRules().get(GameRules.MOB_GRIEFING);
        var lever=new BlockPos(6,2,5);h.setBlock(lever,Blocks.LEVER);var state=h.getBlockState(lever);
        try{
            level.getServer().setDifficulty(net.minecraft.world.Difficulty.NORMAL,true);EnvironmentCombat.breezeTransition(m,Pose.STANDING,Pose.LONG_JUMPING);
            h.assertTrue(HostileCombat.instance().ready(m,"breeze_takeoff_burst"),"Normal difficulty does not spend burst cooldown");
            level.getServer().setDifficulty(net.minecraft.world.Difficulty.HARD,true);level.getGameRules().set(GameRules.MOB_GRIEFING,false,level.getServer());
            EnvironmentCombat.breezeTransition(m,Pose.STANDING,Pose.LONG_JUMPING);h.assertFalse(HostileCombat.instance().ready(m,"breeze_takeoff_burst"),"Hard takeoff burst reserves cooldown");
            h.assertValueEqual(h.getBlockState(lever),state,"Protected lever unchanged by wind burst");
            h.assertTrue(HostileCombat.instance().ready(m,"breeze_landing_burst"),"Landing gate has separate budget");EnvironmentCombat.breezeTransition(m,Pose.LONG_JUMPING,Pose.STANDING);
            h.assertFalse(HostileCombat.instance().ready(m,"breeze_landing_burst"),"Landing burst reserved");
        }finally{level.getServer().setDifficulty(difficulty,true);level.getGameRules().set(GameRules.MOB_GRIEFING,grief,level.getServer());m.discard();}h.succeed();
    }
    @GameTest(structure=ARENA)
    public void evokerFireballBlockHitSuppressesIgnitionButVanillaBlazeStillIgnites(GameTestHelper h){
        floor(h,Blocks.STONE);var e=placed(h,EntityTypes.EVOKER,3,3);var b=placed(h,EntityTypes.BLAZE,4,3);var pos=new BlockPos(8,2,8);h.setBlock(pos,Blocks.OAK_PLANKS);
        var absolute=h.absolutePos(pos);var hit=new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(absolute),net.minecraft.core.Direction.UP,absolute,false);
        boolean grief=h.getLevel().getGameRules().get(GameRules.MOB_GRIEFING);h.getLevel().getGameRules().set(GameRules.MOB_GRIEFING,true,h.getLevel().getServer());
        try{
            var protectedBall=new net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball(h.getLevel(),e,new net.minecraft.world.phys.Vec3(1,0,0)){void hit(){super.onHitBlock(hit);}};
            SkeletonExtras.tag(protectedBall,"evoker_fireball",b);protectedBall.hit();h.assertTrue(h.getBlockState(pos.above()).isAir(),"Evoker block hit never ignites");
            var vanillaBall=new net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball(h.getLevel(),b,new net.minecraft.world.phys.Vec3(1,0,0)){void hit(){super.onHitBlock(hit);}};
            vanillaBall.hit();h.assertTrue(h.getBlockState(pos.above()).is(Blocks.FIRE),"Unrelated vanilla fireball still ignites");protectedBall.discard();vanillaBall.discard();
        }finally{h.getLevel().getGameRules().set(GameRules.MOB_GRIEFING,grief,h.getLevel().getServer());e.discard();b.discard();}h.succeed();
    }
    @GameTest(structure=ARENA)
    public void piglinStandardCrossbowActivityChecksUseActualOffhand(GameTestHelper h){
        var m=placed(h,EntityTypes.PIGLIN,3,3);var stack=new ItemStack(CombatTestItems.CROSSBOW);m.setItemSlot(EquipmentSlot.OFFHAND,stack);
        h.assertTrue(m.canUseNonMeleeWeapon(stack),"Piglin weapon check recognizes standard subclass");h.assertTrue(m.isHolding(Items.CROSSBOW),"Vanilla Brain crossbow item test recognizes subclass");
        h.assertValueEqual(net.minecraft.world.entity.projectile.ProjectileUtil.getWeaponHoldingHand(m,Items.CROSSBOW),net.minecraft.world.InteractionHand.OFF_HAND,"Actual offhand selected");m.discard();h.succeed();
    }
    @GameTest(structure=ARENA,maxTicks=100)
    public void sniperActuallyNavigatesToCoverWithoutBowGoalCancellingPath(GameTestHelper h){
        // GameTest chooses world positions; clear terrain above this fixture before requiring an open sky.
        for(int x=0;x<16;x++)for(int z=0;z<16;z++){
            var bottom=h.absolutePos(new BlockPos(x,2,z));int top=h.getLevel().getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE,bottom.getX(),bottom.getZ());
            for(int y=bottom.getY();y<=top;y++){var pos=new BlockPos(bottom.getX(),y,bottom.getZ());if(!h.getLevel().getBlockState(pos).isAir())h.getLevel().setBlockAndUpdate(pos,Blocks.AIR.defaultBlockState());}
        }
        floor(h,Blocks.STONE);for(int x=0;x<16;x++)for(int z=0;z<16;z++)h.setBlock(new BlockPos(x,5,z),Blocks.AIR);for(int x=6;x<=8;x++)for(int z=2;z<=4;z++)h.setBlock(new BlockPos(x,5,z),Blocks.STONE);
        var m=placed(h,EntityTypes.SKELETON,3,3);var t=placed(h,EntityTypes.COW,12,3);m.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.BOW));m.setTarget(t);var data=m.getAttached(SkeletonCombat.DATA).copy();data.putString("trait",FeatureId.SKELETON_SNIPING.id().toString());m.setAttached(SkeletonCombat.DATA,data);
        h.runAtTickTime(5,()->{h.assertTrue(h.getLevel().canSeeSky(m.blockPosition()),"Starts exposed after light updates");m.setNoAi(false);});
        var reached=new java.util.concurrent.atomic.AtomicBoolean();for(int tick=6;tick<80;tick++)h.runAtTickTime(tick,()->{if(!h.getLevel().canSeeSky(m.blockPosition()))reached.set(true);});
        h.runAtTickTime(80,()->{h.assertTrue(reached.get(),"Actual AI reached roof; final="+m.position()+"; saved="+m.getAttached(HostileCombat.DATA)+"; running="+m.getGoalSelector().getAvailableGoals().stream().filter(g->g.isRunning()).map(g->g.getGoal().getClass().getSimpleName()).toList());h.assertTrue(m.getTarget()==t,"Target retained");m.discard();t.discard();h.succeed();});
    }
    @GameTest(structure=ARENA)
    public void strayFallingBarrageHasThreeSnowballsAndPersistentExpiry(GameTestHelper h){
        floor(h,Blocks.STONE);var m=placed(h,EntityTypes.STRAY,3,3);var t=placed(h,EntityTypes.COW,11,3);m.setOnGround(true);m.setTarget(t);force(m,FeatureId.STRAY_SNOW_BARRAGE);var goal=SkeletonExtras.goal(m);h.assertTrue(goal.canUse(),"Unarmed stray may use rare barrage");goal.start();for(int i=0;i<20;i++)goal.tick();
        h.assertTrue(h.getEntities(EntityTypes.SNOWBALL,new BlockPos(3,2,3),16).isEmpty(),"No shots before falling");m.setOnGround(false);m.setDeltaMovement(0,-.1,0);goal.tick();goal.stop();var balls=h.getEntities(EntityTypes.SNOWBALL,new BlockPos(3,2,3),16);h.assertValueEqual(balls.size(),3,"Three snowballs, once");
        for(var ball:balls){var data=ball.getAttached(SkeletonExtras.PROJECTILE).copy();data.putLong("expires",0);ball.setAttached(SkeletonExtras.PROJECTILE,data);SkeletonExtras.projectileTick(ball);h.assertTrue(ball.isRemoved(),"Expired projectile discarded");}m.discard();t.discard();h.succeed();
    }
    @GameTest(structure=ARENA)
    public void unknownMajorSchemaSurvivesAllEntityLoadInitializers(GameTestHelper h){
        for(var type:java.util.List.of(EntityTypes.WITHER_SKELETON,EntityTypes.ZOMBIE_VILLAGER,EntityTypes.ZOMBIE)){
            var m=mob(h,type,3,3);var tag=new net.minecraft.nbt.CompoundTag();tag.putInt("version",99);tag.putString("origin","rolled");tag.putString("future","keep");m.setAttached(AdvancedHostiles.DATA,tag.copy());h.getLevel().addFreshEntity(m);
            h.assertValueEqual(m.getAttached(AdvancedHostiles.DATA),tag,"Unknown major data preserved exactly through load initializers");m.discard();
        }h.succeed();
    }
    @GameTest(structure=ARENA)
    public void firedWeakSkullKeepsDamageBudgetWithoutLoadedOwner(GameTestHelper h){
        var target=placed(h,EntityTypes.COW,3,3);var skull=EntityTypes.WITHER_SKULL.create(h.getLevel(),EntitySpawnReason.COMMAND);SkeletonExtras.tag(skull,"skull",target);
        var config=new BuildupConfig();config.general.enabled.accept(false);var combat=new HostileCombat(new FeatureRegistry(config));var source=target.damageSources().thrown(skull,null);
        h.assertValueEqual(combat.damage(target,source,8),4f,"Already fired weak projectile keeps its budget without owner or active gate");
        var future=skull.getAttached(SkeletonExtras.PROJECTILE).copy();future.putInt("version",99);skull.setAttached(SkeletonExtras.PROJECTILE,future);
        h.assertValueEqual(combat.damage(target,source,8),8f,"Unknown schema is not interpreted");skull.discard();target.discard();h.succeed();
    }
    @GameTest(structure=ARENA)
    public void evokerAddsAvoidanceForItsCurrentNonPlayerTarget(GameTestHelper h){
        floor(h,Blocks.STONE);var mob=placed(h,EntityTypes.EVOKER,7,7);var target=placed(h,EntityTypes.IRON_GOLEM,3,7);mob.setTarget(target);mob.setOnGround(true);mob.getRandom().setSeed(1234);
        var goal=mob.getGoalSelector().getAvailableGoals().stream().map(g->g.getGoal()).filter(g->g.getClass().getName().startsWith(HostileCombat.class.getName()+"$")).findFirst().orElseThrow();
        boolean found=false;for(int attempt=0;attempt<32&&!found;attempt++)found=goal.canUse();h.assertTrue(found,"Current non-player attacker can produce a safe avoidance path");
        goal.start();h.assertFalse(mob.getNavigation().isDone(),"Actual avoidance goal starts navigation");goal.stop();mob.setTarget(null);h.assertFalse(goal.canUse(),"No unrelated entity is chosen after target loss");mob.discard();target.discard();h.succeed();
    }
}