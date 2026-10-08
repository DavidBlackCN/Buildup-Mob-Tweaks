package com.davidblackcn.buildupmobtweaks;
import com.davidblackcn.buildupmobtweaks.combat.*;
import com.davidblackcn.buildupmobtweaks.config.BuildupConfig;
import com.davidblackcn.buildupmobtweaks.feature.*;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import static com.davidblackcn.buildupmobtweaks.CombatTestWorld.*;
public class AdvancedHostileTests {
    private static <T extends Mob> T placed(GameTestHelper h, EntityType<T> type,int x,int z) { var m=mob(h,type,x,z);h.getLevel().addFreshEntity(m);return m; }
    private static void force(Mob m,FeatureId id) { var tag=m.getAttached(AdvancedHostiles.DATA).copy(); tag.putString("trait",id.id().toString()); m.setAttached(AdvancedHostiles.DATA,tag); }
    @GameTest(structure=ARENA)
    public void evokerSkillHasWindupThreeShotsAndPersistentReservation(GameTestHelper h) {
        floor(h,Blocks.STONE);var m=placed(h,EntityTypes.EVOKER,3,3);var t=placed(h,EntityTypes.IRON_GOLEM,10,3); m.setTarget(t);force(m,FeatureId.EVOKER_FIREBALL);
        var goal=AdvancedHostiles.instance().skill(m);h.assertTrue(goal.canUse(),"Visible target allows skill");goal.start();
        for(int i=0;i<39;i++)goal.tick();h.assertTrue(h.getEntities(EntityTypes.SMALL_FIREBALL,new BlockPos(3,2,3),16).isEmpty(),"No shot before forty-tick warning");
        for(int i=39;i<66;i++)goal.tick();goal.stop();
        var balls=h.getEntities(EntityTypes.SMALL_FIREBALL,new BlockPos(3,2,3),16);h.assertValueEqual(balls.size(),3,"Strict three-projectile cap");
        h.assertFalse(goal.canUse(),"Cooldown prevents recast");var tag=m.getAttached(HostileCombat.DATA).copy();m=reload(h,m);h.assertValueEqual(m.getAttached(HostileCombat.DATA),tag,"Cooldown survives serialization");
        for(var ball:balls)ball.discard();m.discard();t.discard();h.succeed();
    }
    @GameTest(structure=ARENA)
    public void actualTotemConsumesOnceAndNeverMakesEvokerInvulnerable(GameTestHelper h) {
        var m=placed(h,EntityTypes.EVOKER,3,3);force(m,FeatureId.EVOKER_TOTEM);m.setHealth(5);AdvancedHostiles.instance().totem(m);
        h.assertTrue(m.getOffhandItem().is(Items.TOTEM_OF_UNDYING),"Real item telegraphs death protection");
        m.hurtServer(h.getLevel(),m.damageSources().generic(),100);h.assertTrue(m.isAlive(),"Vanilla totem protects once");h.assertTrue(m.getOffhandItem().isEmpty(),"Vanilla consumes exactly one item");
        AdvancedHostiles.instance().totem(m);h.assertTrue(m.getOffhandItem().isEmpty(),"No refill at low health");
        m.damageCooldownTime=0;m.hurtServer(h.getLevel(),m.damageSources().generic(),100);h.assertFalse(m.isAlive(),"Next lethal hit kills normally");m.discard();h.succeed();
    }
    @GameTest(structure=ARENA)
    public void rareChoicesAreExclusiveAndReloadDoesNotRollAgain(GameTestHelper h) {
        var cfg=new BuildupConfig();cfg.hostile.extended.evoker_fireball_chance.accept(1000);cfg.hostile.extended.evoker_totem_chance.accept(1000);
        var combat=new AdvancedHostiles(new FeatureRegistry(cfg));var m=mob(h,EntityTypes.EVOKER,3,3);combat.initialize(m);var tag=m.getAttached(AdvancedHostiles.DATA).copy();
        var trait=tag.getStringOr("trait","");h.assertTrue(trait.equals(FeatureId.EVOKER_FIREBALL.id().toString())||trait.equals(FeatureId.EVOKER_TOTEM.id().toString()),"Normalized draw chooses exactly one ability");
        combat.initialize(m);h.assertValueEqual(m.getAttached(AdvancedHostiles.DATA),tag,"No duplicate draw");
        h.getLevel().addFreshEntity(m);tag=m.getAttached(AdvancedHostiles.DATA).copy();m=reload(h,m);h.assertValueEqual(m.getAttached(AdvancedHostiles.DATA),tag,"Saved draw retained");m.discard();h.succeed();
    }
    @GameTest(structure=ARENA)
    public void cloneArrowsHaveBoundedCountAndCannotBePickedUp(GameTestHelper h) {
        floor(h,Blocks.STONE);var m=placed(h,EntityTypes.ILLUSIONER,5,5);var t=placed(h,EntityTypes.IRON_GOLEM,12,5);m.setTarget(t);m.setInvisible(true);m.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.BOW));force(m,FeatureId.ILLUSIONER_CLONE_ARROWS);
        AdvancedHostiles.instance().illusionShot(m,t,1);AdvancedHostiles.instance().illusionShot(m,t,1);
        var arrows=h.getEntities(EntityTypes.ARROW,new BlockPos(5,2,5),16);h.assertValueEqual(arrows.size(),2,"Only two clone arrows per cooldown");
        for(var arrow:arrows){h.assertValueEqual(arrow.pickup,net.minecraft.world.entity.projectile.arrow.AbstractArrow.Pickup.DISALLOWED,"No bonus ammunition drops");arrow.discard();}m.discard();t.discard();h.succeed();
    }
    @GameTest(structure=ARENA)
    public void vexLedgerKeepsUnloadedSlotsAndReleasesDestroyedMinions(GameTestHelper h) {
        var e=placed(h,EntityTypes.EVOKER,3,3);var v=placed(h,EntityTypes.VEX,4,3);v.setOwner(e);h.assertValueEqual(VexOwnership.count(e),1,"Registered on ownership change");
        v.remove(Entity.RemovalReason.UNLOADED_TO_CHUNK);h.assertValueEqual(VexOwnership.count(e),1,"Unloading does not release slot");
        VexOwnership.release(v);h.assertValueEqual(VexOwnership.count(e),0,"Explicit destroyed ownership release");e.discard();h.succeed();
    }
}