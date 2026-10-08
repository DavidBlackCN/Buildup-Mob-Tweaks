package com.davidblackcn.buildupmobtweaks;
import com.davidblackcn.buildupmobtweaks.combat.*;
import com.davidblackcn.buildupmobtweaks.config.BuildupConfig;
import com.davidblackcn.buildupmobtweaks.feature.*;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.illager.Pillager;
import net.minecraft.world.entity.projectile.hurtingprojectile.*;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.Vec3;
import static com.davidblackcn.buildupmobtweaks.CombatTestWorld.*;
public class RangedExpansionTests {
    private static <T extends Mob> T placed(GameTestHelper h, EntityType<T> type, int x, int z) {
        T mob = mob(h, type, x, z); h.getLevel().addFreshEntity(mob); return mob;
    }
    private static Goal goal(Mob mob, String name) {
        return mob.getGoalSelector().getAvailableGoals().stream().map(WrappedGoal::getGoal)
                .filter(g -> g.getClass().getName().endsWith(name)).findFirst().orElseThrow();
    }
    @GameTest(structure = ARENA)
    public void actualGhastProjectileSlowsAndCooldownSurvivesReload(GameTestHelper h) {
        var mob = placed(h, EntityTypes.GHAST, 3, 3); var target = placed(h, EntityTypes.COW, 13, 3); mob.setTarget(target);
        var goal = goal(mob, "$GhastShootFireballGoal"); goal.start(); for(int i=0;i<20;i++) goal.tick();
        var balls = h.getEntities(EntityTypes.FIREBALL, new BlockPos(8,2,3), 16);
        h.assertValueEqual(balls.size(), 1, "Exactly one real vanilla projectile"); var ball=balls.getFirst();
        h.assertTrue(Math.abs(ball.accelerationPower - .06) < .00001 && Math.abs(ball.getDeltaMovement().length() - .06) < .00001, "Initial velocity and acceleration reduced together");
        var saved=mob.getAttached(HostileCombat.DATA).copy(); var loaded=reload(h,mob);
        h.assertValueEqual(loaded.getAttached(HostileCombat.DATA),saved,"Cooldown persists");
        loaded.setTarget(target); var again=goal(loaded,"$GhastShootFireballGoal"); again.start(); for(int i=0;i<100;i++) again.tick();
        h.assertValueEqual(h.getEntities(EntityTypes.FIREBALL,new BlockPos(8,2,3),16).size(),1,"Restarting Goal cannot reset server-time deadline");
        var savedBall=reload(h,ball); h.assertTrue(Math.abs(savedBall.accelerationPower-.06)<.00001,"Original projectile save format keeps acceleration");
        savedBall.discard(); loaded.discard(); target.discard(); h.succeed();
    }
    @GameTest(structure=ARENA)
    public void disabledGhastChangesPreserveVanillaProjectile(GameTestHelper h) {
        var cfg=new BuildupConfig(); cfg.hostile.extended.ghast_slow_fireball.accept(false); cfg.hostile.extended.ghast_cooldown.accept(false);
        var combat=new HostileCombat(new FeatureRegistry(cfg)); var mob=placed(h,EntityTypes.GHAST,3,3);
        var ball=new LargeFireball(h.getLevel(),mob,new Vec3(1,0,0),1); var velocity=ball.getDeltaMovement();
        combat.fireball(mob,ball); h.assertValueEqual(ball.accelerationPower,.1,"Vanilla acceleration untouched");
        h.assertValueEqual(ball.getDeltaMovement(),velocity,"Vanilla initial motion untouched"); h.assertTrue(combat.canGhastCharge(mob),"Cooldown independently disabled");
        mob.discard(); h.succeed();
    }
    @GameTest(structure=ARENA)
    public void actualBlazeVolleyMatchesDifficultyWithoutExtraEntities(GameTestHelper h) {
        var mob=placed(h,EntityTypes.BLAZE,3,3); var target=placed(h,EntityTypes.COW,13,3); mob.setTarget(target);
        var goal=goal(mob,"$BlazeAttackGoal"); goal.start(); for(int i=0;i<80;i++) goal.tick();
        int expected=switch(h.getLevel().getDifficulty()) {case PEACEFUL,EASY->1;case NORMAL->2;case HARD->3;};
        var balls=h.getEntities(EntityTypes.SMALL_FIREBALL,new BlockPos(8,2,3),16); h.assertValueEqual(balls.size(),expected,"Actual vanilla volley count, orbit particles spawn no extra fireballs");
        for(var ball:balls) ball.discard(); mob.discard(); target.discard(); h.succeed();
    }
    @GameTest(structure=ARENA)
    public void vexTakesRealSnowballDamageAndProjectileMultiplier(GameTestHelper h) {
        var vex=placed(h,EntityTypes.VEX,4,4); var shooter=placed(h,EntityTypes.COW,10,4);
        var snowball=new Snowball(h.getLevel(),shooter,new ItemStack(Items.SNOWBALL)); float before=vex.getHealth();
        vex.hurtServer(h.getLevel(),vex.damageSources().thrown(snowball,shooter),0);
        h.assertValueEqual(vex.getHealth(),before-2,"Zero vanilla snowball damage becomes two actual health damage");
        var cfg=new BuildupConfig(); cfg.hostile.extended.vex_projectile_weakness.accept(false);
        h.assertValueEqual(new HostileCombat(new FeatureRegistry(cfg)).damage(vex,vex.damageSources().thrown(snowball,shooter),0),0f,"Independent damage feature off");
        vex.discard(); shooter.discard(); h.succeed();
    }
    @GameTest(structure=ARENA,maxTicks=160)
    public void actualModCrossbowLoadsAndFiresFromMainHand(GameTestHelper h) {
        floor(h,net.minecraft.world.level.block.Blocks.STONE); var mob=placed(h,EntityTypes.PILLAGER,3,3);
        mob.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(CombatTestItems.CROSSBOW)); var target=placed(h,EntityTypes.IRON_GOLEM,12,3); mob.setTarget(target); mob.setNoAi(false);
        h.assertTrue(mob.canUseNonMeleeWeapon(mob.getMainHandItem()),"Standard subclass accepted");
        h.succeedWhen(()-> { h.assertTrue(!h.getEntities(EntityTypes.ARROW,new BlockPos(8,2,3),16).isEmpty() || target.getHealth()<target.getMaxHealth(),"Actual charge and arrow release through original Goal"); mob.discard(); target.discard(); });
    }
    @GameTest(structure=ARENA)
    public void extendedGatesAndUnknownDataLeaveOriginalBehavior(GameTestHelper h) {
        var cfg=new BuildupConfig(); var combat=new HostileCombat(new FeatureRegistry(cfg)); var blaze=placed(h,EntityTypes.BLAZE,3,3);
        cfg.hostile.extended.blaze_difficulty_volley.accept(false); h.assertValueEqual(combat.blazeVolley(blaze),3,"Original volley on disable");
        var data=blaze.getAttached(HostileCombat.DATA).copy(); data.putInt("version",99); data.putString("future","keep"); blaze.setAttached(HostileCombat.DATA,data);
        combat.reserve(blaze,"test",100); h.assertValueEqual(blaze.getAttached(HostileCombat.DATA),data,"Unknown schema never rewritten");
        h.assertFalse(combat.enabled(blaze,FeatureId.BLAZE_ORBIT),"Unknown schema disables extra policy"); blaze.discard(); h.succeed();
    }
}