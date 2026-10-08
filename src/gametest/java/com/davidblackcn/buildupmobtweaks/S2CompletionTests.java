package com.davidblackcn.buildupmobtweaks;
import com.davidblackcn.buildupmobtweaks.combat.*;
import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import static com.davidblackcn.buildupmobtweaks.CombatTestWorld.*;
public class S2CompletionTests {
    private static <T extends Mob>T placed(GameTestHelper h,EntityType<T> type,int x,int z){var m=mob(h,type,x,z);h.getLevel().addFreshEntity(m);return m;}
    private static void force(Mob m,FeatureId id){var tag=m.getAttached(AdvancedHostiles.DATA).copy();tag.putString("trait",id.id().toString());m.setAttached(AdvancedHostiles.DATA,tag);}
    @GameTest(structure=ARENA,maxTicks=140)
    public void zombifiedPiglinActuallyLoadsAndFiresRetainedCrossbow(GameTestHelper h){
        floor(h,Blocks.STONE);var m=placed(h,EntityTypes.ZOMBIFIED_PIGLIN,3,3);var t=placed(h,EntityTypes.IRON_GOLEM,11,3);
        var stack=new ItemStack(CombatTestItems.CROSSBOW);m.setItemSlot(EquipmentSlot.MAINHAND,stack);m.setTarget(t);m.setNoAi(false);
        h.runAtTickTime(90,()->{h.assertTrue(t.getHealth()<t.getMaxHealth()||!h.getEntities(EntityTypes.ARROW,new BlockPos(7,2,3),16).isEmpty(),"Real Goal charged and fired custom standard crossbow");
            h.assertTrue(m.getMainHandItem()==stack,"Weapon not replaced");m.discard();t.discard();h.succeed();});
    }
    @GameTest(structure=ARENA)
    public void fletcherEquipmentNeverReplacesExistingWeaponOrRerolls(GameTestHelper h){
        var m=mob(h,EntityTypes.ZOMBIE_VILLAGER,3,3);m.setVillagerData(m.getVillagerData().withProfession(h.getLevel().registryAccess(),VillagerProfession.FLETCHER));
        var sword=new ItemStack(Items.DIAMOND_SWORD);m.setItemSlot(EquipmentSlot.MAINHAND,sword);h.getLevel().addFreshEntity(m);
        h.assertTrue(m.getMainHandItem()==sword,"External equipment retained");m.setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);var loaded=reload(h,m);
        h.assertTrue(loaded.getMainHandItem().isEmpty(),"No second equipment assignment after reload");loaded.discard();h.succeed();
    }
    @GameTest(structure=ARENA)
    public void fishingPullRequiresWindupVisibilityAndConsumesDurability(GameTestHelper h){
        floor(h,Blocks.STONE);var m=placed(h,EntityTypes.DROWNED,3,3);var t=placed(h,EntityTypes.COW,9,3);m.setTarget(t);m.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.FISHING_ROD));
        var goal=HostileEquipment.rodGoal(m);h.assertTrue(goal.canUse(),"Rod with visible target");goal.start();for(int i=0;i<19;i++)goal.tick();h.assertTrue(t.getDeltaMovement().lengthSqr()==0,"Windup does not pull early");
        goal.tick();h.assertTrue(t.getDeltaMovement().x<0,"Pull points toward holder");h.assertValueEqual(m.getMainHandItem().getDamageValue(),1,"Actual rod durability spent");goal.stop();h.assertFalse(goal.canUse(),"Persistent cooldown blocks immediate reuse");m.discard();t.discard();h.succeed();
    }
    @GameTest(structure=ARENA)
    public void slowFoodConsumesOneActualStackOnlyOutsideCombat(GameTestHelper h){
        var m=placed(h,EntityTypes.PILLAGER,3,3);m.tickCount=200;m.setHealth(10);m.setItemSlot(EquipmentSlot.OFFHAND,new ItemStack(Items.APPLE,2));
        var goal=HostileEquipment.foodGoal(m);h.assertTrue(goal.canUse(),"Hurt idle pillager can eat owned food");goal.start();for(int i=0;i<79;i++)goal.tick();h.assertValueEqual(m.getHealth(),10f,"Slow telegraph does not heal early");goal.tick();
        h.assertValueEqual(m.getHealth(),12f,"Two health restored");h.assertValueEqual(m.getOffhandItem().getCount(),1,"One apple consumed");goal.stop();m.discard();h.succeed();
    }
    @GameTest(structure=ARENA)
    public void clericSelectsHurtUndeadAndThrowsOnePotion(GameTestHelper h){
        floor(h,Blocks.STONE);var m=mob(h,EntityTypes.ZOMBIE_VILLAGER,3,3);m.setVillagerData(m.getVillagerData().withProfession(h.getLevel().registryAccess(),VillagerProfession.CLERIC));h.getLevel().addFreshEntity(m);
        var t=placed(h,EntityTypes.ZOMBIE,7,3);t.setHealth(5);var goal=HostileEquipment.potionGoal(m);h.assertTrue(goal.canUse(),"Cleric locates wounded undead in bounded scan");goal.start();for(int i=0;i<20;i++)goal.tick();
        var potions=h.getEntities(EntityTypes.SPLASH_POTION,new BlockPos(3,2,3),16);h.assertValueEqual(potions.size(),1,"Exactly one thrown potion");h.assertTrue(potions.getFirst().getOwner()==m,"Correct owner");potions.getFirst().discard();m.discard();t.discard();h.succeed();
    }
    @GameTest(structure=ARENA)
    public void skeletonWitherDeathConvertsOnceAndTransfersWeapon(GameTestHelper h){
        floor(h,Blocks.STONE);var m=placed(h,EntityTypes.SKELETON,3,3);m.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.BOW));m.addEffect(new MobEffectInstance(MobEffects.WITHER,100));
        m.hurtServer(h.getLevel(),m.damageSources().generic(),100);var result=h.getEntities(EntityTypes.WITHER_SKELETON,new BlockPos(3,2,3),4);
        h.assertValueEqual(result.size(),1,"One conversion replaces original skeleton");h.assertTrue(m.isRemoved(),"Old entity removed");h.assertTrue(result.getFirst().getMainHandItem().is(Items.BOW),"Equipment transferred");result.getFirst().discard();h.succeed();
    }
    @GameTest(structure=ARENA)
    public void actualSkeletonTrapCreatesFourDifferentMountedRiders(GameTestHelper h){
        floor(h,Blocks.STONE);var horse=placed(h,EntityTypes.SKELETON_HORSE,8,8);horse.setTrap(true);
        var goal=horse.getGoalSelector().getAvailableGoals().stream().map(WrappedGoal::getGoal).filter(g->g.getClass().getSimpleName().equals("SkeletonTrapGoal")).findFirst().orElseThrow();goal.tick();
        for(var type:java.util.List.of(EntityTypes.SKELETON,EntityTypes.STRAY,EntityTypes.BOGGED,EntityTypes.WITHER_SKELETON)){
            var riders=h.getEntities(type,new BlockPos(8,2,8),14);h.assertValueEqual(riders.size(),1,"One of each rider type");h.assertTrue(riders.getFirst().isPassenger(),"Conversion keeps mount");riders.getFirst().discard();}
        for(var mount:h.getEntities(EntityTypes.SKELETON_HORSE,new BlockPos(8,2,8),14))mount.discard();h.succeed();
    }
    @GameTest(structure=ARENA)
    public void witherSkullHasWarningCountLimitAndSavedExpiry(GameTestHelper h){
        floor(h,Blocks.STONE);var m=placed(h,EntityTypes.WITHER_SKELETON,3,3);var t=placed(h,EntityTypes.IRON_GOLEM,11,3);m.setTarget(t);m.setOnGround(true);force(m,FeatureId.WITHER_SKELETON_SKULL);
        var goal=SkeletonExtras.goal(m);h.assertTrue(goal.canUse(),"Long-range skill available");goal.start();for(int i=0;i<29;i++)goal.tick();h.assertTrue(h.getEntities(EntityTypes.WITHER_SKULL,new BlockPos(3,2,3),16).isEmpty(),"Warning first");goal.tick();
        var balls=h.getEntities(EntityTypes.WITHER_SKULL,new BlockPos(3,2,3),16);h.assertValueEqual(balls.size(),1,"One weak skull per skill");var saved=balls.getFirst().getAttached(SkeletonExtras.PROJECTILE).copy();var loaded=reload(h,balls.getFirst());h.assertValueEqual(loaded.getAttached(SkeletonExtras.PROJECTILE),saved,"Lifespan metadata survives save");
        goal.stop();loaded.discard();m.discard();t.discard();h.succeed();
    }
    @GameTest(structure=ARENA)
    public void illagerTargetsZombieVillagerWithoutStealingCurrentCombat(GameTestHelper h){
        floor(h,Blocks.STONE);var m=placed(h,EntityTypes.PILLAGER,3,3);var z=placed(h,EntityTypes.ZOMBIE_VILLAGER,8,3);IllagerRelations.chooseTarget(m);h.assertTrue(m.getTarget()==z,"Zombie villager is a valid new target");
        var golem=placed(h,EntityTypes.IRON_GOLEM,10,3);m.setTarget(golem);IllagerRelations.chooseTarget(m);h.assertTrue(m.getTarget()==golem,"Existing combat target retained");m.discard();z.discard();golem.discard();h.succeed();
    }
    @GameTest(structure=ARENA)
    public void ominousEncounterHasBoundedLocalCap(GameTestHelper h){
        floor(h,Blocks.STONE);var player=h.makeMockServerPlayerInLevel();player.snapTo(h.absolutePos(new BlockPos(8,2,8)),0,0);
        h.assertTrue(IllagerRelations.spawnEncounter(player),"Safe floor permits encounter");h.assertFalse(IllagerRelations.spawnEncounter(player),"Another loaded illusioner prevents duplication");
        for(var m:h.getEntities(EntityTypes.ILLUSIONER,new BlockPos(8,2,8),20))m.discard();player.discard();h.succeed();
    }
}