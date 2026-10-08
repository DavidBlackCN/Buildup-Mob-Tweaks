package com.davidblackcn.buildupmobtweaks;
import com.davidblackcn.buildupmobtweaks.combat.*;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.gameevent.GameEvent;
import static com.davidblackcn.buildupmobtweaks.CombatTestWorld.*;
public class EnvironmentExpansionTests {
    private static <T extends Mob> T placed(GameTestHelper h,EntityType<T> type,int x,int z) {var mob=mob(h,type,x,z);h.getLevel().addFreshEntity(mob);return mob;}
    @GameTest(structure=ARENA)
    public void visibilityUsesStrongestEffectAndKeepsBlindRange(GameTestHelper h) {
        floor(h,Blocks.STONE);var observer=placed(h,EntityTypes.SKELETON,3,3);var target=placed(h,EntityTypes.COW,12,3);
        observer.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,200));
        h.assertValueEqual(target.getVisibilityPercent(h.getLevel(),observer),.25,"Actual visibility hook reduces blind perception");
        observer.addEffect(new MobEffectInstance(MobEffects.NAUSEA,200));
        h.assertValueEqual(target.getVisibilityPercent(h.getLevel(),observer),.25,"Effects use strongest, not compounded multipliers");
        h.assertFalse(observer.hasLineOfSight(target),"Blind distant firing gate");target.snapTo(observer.position().add(2,0,0),0,0);
        h.assertTrue(observer.hasLineOfSight(target),"Blind close combat remains possible");observer.discard();target.discard();h.succeed();
    }
    @GameTest(structure=ARENA)
    public void glowSeesThroughWallWithoutBypassingTargetSafety(GameTestHelper h) {
        floor(h,Blocks.STONE);var observer=placed(h,EntityTypes.SKELETON,3,3);var target=placed(h,EntityTypes.COW,10,3);
        for(int y=2;y<5;y++) for(int z=1;z<6;z++)h.setBlock(new BlockPos(6,y,z),Blocks.STONE);
        h.assertFalse(observer.getSensing().hasLineOfSight(target),"Wall blocks ordinary target");target.setGlowingTag(true);
        h.assertTrue(observer.getSensing().hasLineOfSight(target),"Glowing overrides only sight result");
        target.setPermanentlyInvulnerable(true);h.assertFalse(TargetingConditions.forCombat().range(16).test(h.getLevel(),observer,target),"Original target invulnerability validation remains");
        observer.discard();target.discard();h.succeed();
    }
    @GameTest(structure=ARENA)
    public void invisibleAttackerRevealsWithinCombatWindow(GameTestHelper h) {
        var observer=placed(h,EntityTypes.ZOMBIE,3,3);var target=placed(h,EntityTypes.COW,10,3);target.setInvisible(true);
        h.assertTrue(target.getVisibilityPercent(h.getLevel(),observer)<.4,"Invisible before combat"); observer.setLastHurtByMob(target);
        h.assertValueEqual(target.getVisibilityPercent(h.getLevel(),observer),1.0,"Recent attacker reveals to victim only"); observer.discard();target.discard();h.succeed();
    }
    @GameTest(structure=ARENA)
    public void meleeEffectsBoundedAndFatigueTakesPriority(GameTestHelper h) {
        var mob=placed(h,EntityTypes.ZOMBIE,3,3);mob.addEffect(new MobEffectInstance(MobEffects.HASTE,200,255));
        h.assertValueEqual(CombatPerception.meleeTicks(mob,20),11,"Haste amplifier capped at three");mob.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE,200,255));
        h.assertValueEqual(CombatPerception.meleeTicks(mob,20),35,"Fatigue priority and cap");mob.discard();h.succeed();
    }
    @GameTest(structure=ARENA)
    public void hurtSilverfishBurrowsOnlyWhenGriefingAllowed(GameTestHelper h) {
        floor(h,Blocks.STONE);var mob=placed(h,EntityTypes.SILVERFISH,5,5);mob.setHealth(2);var goal=EnvironmentCombat.burrowGoal(mob);
        var rules=h.getLevel().getGameRules();boolean old=rules.get(GameRules.MOB_GRIEFING);
        try { rules.set(GameRules.MOB_GRIEFING,false,h.getLevel().getServer());h.assertFalse(goal.canUse(),"Griefing false blocks conversion");
            rules.set(GameRules.MOB_GRIEFING,true,h.getLevel().getServer());h.assertTrue(goal.canUse(),"Adjacent stone offers bounded escape");goal.start();
            for(int i=0;i<19;i++)goal.tick();h.assertTrue(mob.isAlive(),"Windup leaves damage window");goal.tick();
            h.assertTrue(mob.isRemoved(),"Original mob consumed once after successful infestation");
            h.assertTrue(h.getBlockState(new BlockPos(5,1,5)).getBlock() instanceof InfestedBlock,"Actual adjacent block converted");
        } finally {rules.set(GameRules.MOB_GRIEFING,old,h.getLevel().getServer());mob.discard();}h.succeed();
    }
    @GameTest(structure=ARENA)
    public void silverfishCallGoalClaimsMovementDuringCountdown(GameTestHelper h) {
        floor(h,Blocks.STONE);var mob=placed(h,EntityTypes.SILVERFISH,5,5);var attacker=placed(h,EntityTypes.COW,9,5);
        mob.hurtServer(h.getLevel(),mob.damageSources().mobAttack(attacker),1);
        var goal=mob.getGoalSelector().getAvailableGoals().stream().map(WrappedGoal::getGoal).filter(g->g.getClass().getName().endsWith("$SilverfishWakeUpFriendsGoal")).findFirst().orElseThrow();
        h.assertTrue(goal.canUse() && goal.getFlags().contains(Goal.Flag.MOVE),"Actual original call countdown owns movement mutex");mob.discard();attacker.discard();h.succeed();
    }
    @GameTest(structure=ARENA)
    public void restUsesActualGameEventWakeAndDamageWake(GameTestHelper h) {
        floor(h,Blocks.STONE);var mob=placed(h,EntityTypes.ZOMBIE,5,5);var target=placed(h,EntityTypes.COW,8,5);
        var goal=mob.getGoalSelector().getAvailableGoals().stream().map(WrappedGoal::getGoal).filter(g->g.getClass().getName().endsWith("$RestGoal")).findFirst().orElseThrow();goal.start();
        mob.setTarget(target);h.assertTrue(mob.getTarget()==null,"Rest temporarily refuses new aggression");
        target.gameEvent(GameEvent.BLOCK_DESTROY);h.assertFalse(RestingHostiles.resting(mob),"Real nearby game-event dispatcher wakes resting mob");
        goal.start();mob.hurtServer(h.getLevel(),mob.damageSources().mobAttack(target),1);h.assertFalse(RestingHostiles.resting(mob),"Actual damage independently wakes");
        goal.stop();mob.discard();target.discard();h.succeed();
    }
    @GameTest(structure=ARENA)
    public void witchPreviewIsNotEquipmentAndClearsOnCancel(GameTestHelper h) {
        floor(h,Blocks.STONE);var witch=placed(h,EntityTypes.WITCH,3,3);var target=placed(h,EntityTypes.COW,10,3);witch.setTarget(target);
        witch.performRangedAttack(target,1);h.assertTrue(witch.hasAttached(RaidCombat.POTION_PREVIEW),"Actual attack writes synchronized preview");
        h.assertTrue(witch.getMainHandItem().isEmpty() && witch.getOffhandItem().isEmpty(),"Preview cannot enter equipment drops");
        witch.setTarget(null);RaidCombat.instance().advancePotion(witch);h.assertFalse(witch.hasAttached(RaidCombat.POTION_PREVIEW),"Cancelled preview removed");witch.discard();target.discard();h.succeed();
    }
}