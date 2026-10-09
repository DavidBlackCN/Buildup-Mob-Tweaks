package com.davidblackcn.buildupmobtweaks;

import com.davidblackcn.buildupmobtweaks.rebuild.skeleton.*;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;

/** Fixtures arrange worlds and damage events; behavior is driven exclusively by server/GoalSelector ticks. */
public class SkeletonBehaviorTests {
    static final String ARENA="buildupmobtweaks:r2a_arena";
    static void floor(GameTestHelper h,boolean roof){
        for(int x=0;x<28;x++)for(int z=0;z<28;z++){h.setBlock(new BlockPos(x,1,z),Blocks.STONE);if(roof)h.setBlock(new BlockPos(x,6,z),Blocks.STONE);}
    }
    static AbstractSkeleton skeleton(GameTestHelper h,EntityType<? extends AbstractSkeleton> type,int x,int z){
        var m=type.create(h.getLevel(),EntitySpawnReason.COMMAND);m.snapTo(h.absolutePos(new BlockPos(x,2,z)),0,0);m.setPersistenceRequired();
        m.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.BOW));m.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.IRON_HELMET));
        h.getLevel().addFreshEntity(m);return m;
    }
    static IronGolem target(GameTestHelper h,int x,int z){var t=EntityTypes.IRON_GOLEM.create(h.getLevel(),EntitySpawnReason.COMMAND);
        t.snapTo(h.absolutePos(new BlockPos(x,2,z)),0,0);t.setNoAi(true);t.setPersistenceRequired();h.getLevel().addFreshEntity(t);return t;}
    static boolean[] arrows(GameTestHelper h,AbstractSkeleton m){return arrows(h,()->m);}
    static boolean[] arrows(GameTestHelper h,java.util.function.Supplier<AbstractSkeleton> supplier){boolean[] flags={false,false,false};h.onEachTick(()->{
        var m=supplier.get();if(m==null||m.isRemoved())return;
        if(m.isUsingItem()&&m.getTicksUsingItem()>=10)flags[2]=true;
        for(var a:h.getLevel().getEntitiesOfClass(AbstractArrow.class,m.getBoundingBox().inflate(32),a->a.getOwner()==m)){flags[0]=true;if(a.isCritArrow())flags[1]=true;}
    });return flags;}
    static void evidence(String scenario,AbstractSkeleton m,String detail){var r=SkeletonState.runtime(m);
        BuildupMobTweaks.LOGGER.info("R2B_EVIDENCE scenario={} entity={} ticks={} mode={} shots={} swaps={} flips={} dodges={} snowballs={} shelters={} {}",
                scenario,m.getUUID(),m.tickCount,r.mode,r.shots,r.swaps,r.flips,r.dodges,r.snowballs,r.shelters,detail);}
    @GameTest(structure=ARENA,maxTicks=185)
    public void actualMeleeSwapReturnsOwnedBowAndShoots(GameTestHelper h){
        floor(h,true);var m=skeleton(h,EntityTypes.SKELETON,8,8);var t=target(h,8,10);var bow=m.getMainHandItem();bow.setDamageValue(17);
        bow.set(DataComponents.CUSTOM_NAME,Component.literal("R2B owned bow"));m.setDropChance(EquipmentSlot.MAINHAND,.73f);var seen=arrows(h,m);
        h.runAtTickTime(65,()->{h.assertTrue(m.getTarget()==t&&m.getMainHandItem().is(Items.WOODEN_SWORD),"Natural target and real reserve sword");
            h.assertTrue(t.getHealth()<100,"Actual vanilla melee damage");evidence("S02_melee",m,"health="+t.getHealth());t.snapTo(h.absolutePos(new BlockPos(8,2,20)),0,0);});
        h.runAtTickTime(170,()->{h.assertTrue(seen[0],"Owned bow actually fired after melee");h.assertTrue(m.getMainHandItem()==bow,"Original object returned");
            h.assertTrue(bow.getDamageValue()>17&&bow.getHoverName().getString().equals("R2B owned bow"),"Durability used and name preserved");
            h.assertValueEqual(m.getDropChances().byEquipment(EquipmentSlot.MAINHAND),.73f,"Drop policy preserved");
            h.assertTrue(SkeletonState.reserve(m).is(Items.WOODEN_SWORD),"Exactly one reserve sword");evidence("S02_return",m,"arrow=true");m.discard();t.discard();h.succeed();});
    }
    @GameTest(structure=ARENA,maxTicks=185)
    public void roofSniperReallyDrawsAndFiresCriticalArrow(GameTestHelper h){
        floor(h,true);var m=skeleton(h,EntityTypes.SKELETON,8,8);var t=target(h,8,18);var seen=arrows(h,m);
        h.runAtTickTime(170,()->{h.assertTrue(seen[0]&&seen[1]&&seen[2],"Real draw, actual arrow and critical flag");
            h.assertTrue(SkeletonState.runtime(m).mode.equals("sniper"),"Roof selects sniper mode");evidence("S01_roof",m,"draw=true critical=true targetHealth="+t.getHealth());m.discard();t.discard();h.succeed();});
    }
    @GameTest(structure=ARENA,maxTicks=175)
    public void independentSniperDisableUsesWalkingBowGoal(GameTestHelper h){
        floor(h,true);var m=skeleton(h,EntityTypes.SKELETON,8,8);m.addTag("buildupmobtweaks:disable_skeleton_sniping");var t=target(h,8,18);
        var origin=m.position();var seen=arrows(h,m);double[] displacement={0};h.onEachTick(()->displacement[0]=Math.max(displacement[0],m.position().distanceTo(origin)));
        h.runAtTickTime(160,()->{h.assertTrue(seen[0]&&!seen[1],"Walking goal fires actual noncritical arrows");h.assertTrue(displacement[0]>.5,"Walking strafing visibly moved");
            h.assertTrue(SkeletonState.runtime(m).mode.equals("walk"),"Only sniper option disabled");evidence("S01_walk",m,"max_movement="+displacement[0]);m.discard();t.discard();h.succeed();});
    }
    @GameTest(structure=ARENA,maxTicks=180)
    public void offhandSubclassBowActuallyShoots(GameTestHelper h){
        floor(h,true);var m=skeleton(h,EntityTypes.SKELETON,8,8);m.setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);m.setItemSlot(EquipmentSlot.OFFHAND,new ItemStack(CombatTestItems.BOW));
        var t=target(h,8,18);var seen=arrows(h,m);
        h.runAtTickTime(165,()->{h.assertTrue(seen[0]&&seen[2],"Actual offhand subclass bow draw and arrow");h.assertTrue(m.getOffhandItem().getDamageValue()>0,"Actual held bow used");
            evidence("S02_offhand",m,"arrow=true");m.discard();t.discard();h.succeed();});
    }
    @GameTest(structure=ARENA,maxTicks=250)
    public void strayDefaultCooldownFlipMovesAndThrowsRealSnowballs(GameTestHelper h){
        floor(h,true);var m=skeleton(h,EntityTypes.STRAY,8,8);var t=target(h,8,18);double base=m.getY();double[] height={base};boolean[] balls={false};
        h.onEachTick(()->{height[0]=Math.max(height[0],m.getY());if(!h.getLevel().getEntitiesOfClass(Snowball.class,m.getBoundingBox().inflate(32),s->s.getOwner()==m).isEmpty())balls[0]=true;});
        h.runAtTickTime(100,()->h.assertValueEqual(SkeletonState.runtime(m).flips,0,"Default 120 tick cooldown not bypassed"));
        h.runAtTickTime(235,()->{h.assertTrue(SkeletonState.runtime(m).flips>0&&height[0]-base>.5,"Real default flip displacement");h.assertTrue(balls[0],"Real snowball entity during flip recovery");
            evidence("S03_flip",m,"height="+(height[0]-base)+" balls=true");m.discard();t.discard();h.succeed();});
    }
    @GameTest(structure=ARENA,maxTicks=245)
    public void boggedDodgeCreatesExpandingPoisonCloudWhenTargetApproaches(GameTestHelper h){
        floor(h,true);var m=skeleton(h,EntityTypes.BOGGED,8,8);m.addTag("buildupmobtweaks:disable_ranged_reposition");var t=target(h,8,13);
        var pig=EntityTypes.PIG.create(h.getLevel(),EntitySpawnReason.COMMAND);pig.snapTo(h.absolutePos(new BlockPos(8,2,8)),0,0);pig.setNoAi(true);h.getLevel().addFreshEntity(pig);
        boolean[] observed={false,false};float[] radius={0};net.minecraft.world.phys.Vec3[] dodgeStart={null};double[] displacement={0};
        h.onEachTick(()->{
            if(((SkeletonAccess)m).buildup$special()!=0){if(dodgeStart[0]==null)dodgeStart[0]=m.position();displacement[0]=Math.max(displacement[0],m.position().distanceTo(dodgeStart[0]));}
            for(var c:h.getLevel().getEntitiesOfClass(AreaEffectCloud.class,m.getBoundingBox().inflate(16),c->c.getOwner()==m)){observed[0]=true;radius[0]=Math.max(radius[0],c.getRadius());}
            if(pig.hasEffect(MobEffects.POISON))observed[1]=true;});
        h.runAtTickTime(140,()->{
            // Directed approach; random repositioning is independently disabled in this cloud fixture.
            t.snapTo(m.position().add(0,0,4),0,0);pig.snapTo(m.position(),0,0);
        });
        h.runAtTickTime(225,()->{h.assertTrue(SkeletonState.runtime(m).dodges>0&&displacement[0]>.5,"Real movement during the dodge phase");
            h.assertTrue(observed[0]&&radius[0]>.4f&&observed[1],"Real expanding cloud applied poison");
            evidence("S04_cloud",m,"maxRadius="+radius[0]+" dodgeDistance="+displacement[0]+" poison=true");m.discard();t.discard();pig.discard();h.succeed();});
    }
    @GameTest(structure=ARENA,maxTicks=140)
    public void meleeSaveLoadAndDeathConserveBothWeapons(GameTestHelper h){
        floor(h,true);var holder=new AbstractSkeleton[]{skeleton(h,EntityTypes.SKELETON,8,8)};var t=target(h,8,10);
        var external=new ItemStack(Items.WOODEN_SWORD);external.setDamageValue(9);external.set(DataComponents.CUSTOM_NAME,Component.literal("External owned sword"));
        holder[0].setAttached(SkeletonState.RESERVE,external);
        h.runAtTickTime(65,()->{var m=holder[0];h.assertTrue(m.getMainHandItem().is(Items.WOODEN_SWORD),"Actual melee before save");holder[0]=CombatTestWorld.reload(h,m);
            var loaded=holder[0];h.assertTrue(loaded.getMainHandItem().is(Items.BOW)&&SkeletonState.reserve(loaded).is(Items.WOODEN_SWORD),"Reload restores owned bow and one sword");
            loaded.addTag("buildupmobtweaks:vanilla_ai");loaded.setDropChance(EquipmentSlot.MAINHAND,2);loaded.getAttached(SkeletonState.DATA).putFloat("reserve_drop",2);t.discard();});
        h.runAtTickTime(85,()->holder[0].hurtServer(h.getLevel(),holder[0].damageSources().generic(),1000));
        h.runAtTickTime(100,()->{int bows=0,swords=0;for(var e:h.getLevel().getEntitiesOfClass(ItemEntity.class,holder[0].getBoundingBox().inflate(8))){if(e.getItem().is(Items.BOW))bows+=e.getItem().getCount();if(e.getItem().is(Items.WOODEN_SWORD))swords+=e.getItem().getCount();}
            h.assertTrue(bows==1&&swords==1,"Exactly one actual bow and externally supplied sword dropped after real death");
            h.assertTrue(h.getLevel().getEntitiesOfClass(ItemEntity.class,holder[0].getBoundingBox().inflate(8)).stream().anyMatch(e->e.getItem().is(Items.WOODEN_SWORD)&&e.getItem().getDamageValue()==9&&e.getItem().getHoverName().getString().equals("External owned sword")),"External sword components preserved");
            evidence("S02_reload_death",holder[0],"bow=1 external_sword=1 damage=9");h.succeed();});
    }
    @GameTest(structure=ARENA,maxTicks=110)
    public void generatedSwordsNeverDropAfterActualVariantMelee(GameTestHelper h){
        floor(h,true);var mobs=new AbstractSkeleton[]{skeleton(h,EntityTypes.SKELETON,6,8),skeleton(h,EntityTypes.STRAY,14,8),skeleton(h,EntityTypes.BOGGED,22,8)};
        var targets=new IronGolem[]{target(h,6,10),target(h,14,10),target(h,22,10)};
        h.runAtTickTime(70,()->{for(int i=0;i<mobs.length;i++){var m=mobs[i];h.assertTrue(m.getMainHandItem().is(Items.WOODEN_SWORD)&&SkeletonState.generatedSword(m.getMainHandItem()),"Actual Goal equipped generated sword");
            h.assertTrue(targets[i].getHealth()<100,"Actual melee hit before death");m.setDropChance(EquipmentSlot.MAINHAND,2);m.getAttached(SkeletonState.DATA).putFloat("reserve_drop",2);
            m.hurtServer(h.getLevel(),m.damageSources().generic(),1000);targets[i].discard();}});
        h.runAtTickTime(95,()->{int swords=0,bows=0;for(var item:h.getLevel().getEntitiesOfClass(ItemEntity.class,mobs[1].getBoundingBox().inflate(28))){if(item.getItem().is(Items.WOODEN_SWORD))swords+=item.getItem().getCount();if(item.getItem().is(Items.BOW))bows+=item.getItem().getCount();}
            h.assertTrue(swords==0&&bows==3,"All three generated swords suppressed while each original bow drops once");evidence("S02_generated_death",mobs[0],"variants=3 generated_swords=0 bows=3 forced_drop=2");h.succeed();});
    }
    @GameTest(structure=ARENA,maxTicks=130)
    public void generatedSwordReloadAndOptOutStillSuppressDeathDrop(GameTestHelper h){
        floor(h,true);var holder=new AbstractSkeleton[]{skeleton(h,EntityTypes.SKELETON,8,8)};var t=target(h,8,10);
        h.runAtTickTime(65,()->{h.assertTrue(SkeletonState.generatedSword(holder[0].getMainHandItem()),"Actual melee before serialization");holder[0]=CombatTestWorld.reload(h,holder[0]);
            h.assertTrue(holder[0].getMainHandItem().is(Items.BOW)&&SkeletonState.generatedSword(SkeletonState.reserve(holder[0])),"Provenance moves and persists with actual sword");holder[0].addTag("buildupmobtweaks:vanilla_ai");t.discard();});
        h.runAtTickTime(85,()->{var m=holder[0];m.setDropChance(EquipmentSlot.MAINHAND,2);m.getAttached(SkeletonState.DATA).putFloat("reserve_drop",2);m.hurtServer(h.getLevel(),m.damageSources().generic(),1000);});
        h.runAtTickTime(105,()->{var drops=h.getLevel().getEntitiesOfClass(ItemEntity.class,holder[0].getBoundingBox().inflate(8));h.assertTrue(drops.stream().noneMatch(e->e.getItem().is(Items.WOODEN_SWORD))&&drops.stream().filter(e->e.getItem().is(Items.BOW)).mapToInt(e->e.getItem().getCount()).sum()==1,"Reload and disable cannot turn generated sword into loot");evidence("S02_generated_reload_disable",holder[0],"generated_swords=0 bow=1");h.succeed();});
    }
    @GameTest(structure=ARENA,maxTicks=70)
    public void legacyPlainSwordMigratesOnceButExternalSwordRemainsLoot(GameTestHelper h){
        floor(h,true);var mobs=new AbstractSkeleton[]{skeleton(h,EntityTypes.SKELETON,6,8),skeleton(h,EntityTypes.SKELETON,14,8),skeleton(h,EntityTypes.SKELETON,22,8)};
        for(var m:mobs){m.setNoAi(true);m.getAttached(SkeletonState.DATA).remove("generated_sword_policy");m.setAttached(SkeletonState.RESERVE,new ItemStack(Items.WOODEN_SWORD));}
        // Legacy melee save has its plain sword in hand and the real bow in reserve.
        var oldBow=mobs[1].getMainHandItem();mobs[1].setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.WOODEN_SWORD));mobs[1].setAttached(SkeletonState.RESERVE,oldBow);
        SkeletonState.reserve(mobs[2]).set(DataComponents.CUSTOM_NAME,Component.literal("Legacy external sword"));SkeletonState.reserve(mobs[2]).setDamageValue(7);
        h.runAtTickTime(10,()->{for(int i=0;i<mobs.length;i++)mobs[i]=CombatTestWorld.reload(h,mobs[i]);
            h.assertTrue(SkeletonState.generatedSword(SkeletonState.reserve(mobs[0]))&&SkeletonState.generatedSword(SkeletonState.reserve(mobs[1])),"Both legacy ownership positions migrated through real save/load");
            h.assertTrue(!SkeletonState.generatedSword(SkeletonState.reserve(mobs[2])),"Named damaged external sword is not classified as generated");
            for(var m:mobs){m.addTag("buildupmobtweaks:vanilla_ai");m.setDropChance(EquipmentSlot.MAINHAND,2);m.getAttached(SkeletonState.DATA).putFloat("reserve_drop",2);m.hurtServer(h.getLevel(),m.damageSources().generic(),1000);}});
        h.runAtTickTime(40,()->{var drops=h.getLevel().getEntitiesOfClass(ItemEntity.class,mobs[1].getBoundingBox().inflate(28));
            h.assertTrue(drops.stream().filter(e->e.getItem().is(Items.WOODEN_SWORD)).mapToInt(e->e.getItem().getCount()).sum()==1&&drops.stream().anyMatch(e->e.getItem().getHoverName().getString().equals("Legacy external sword")&&e.getItem().getDamageValue()==7),"Only actual external sword drops with original components");evidence("S02_legacy_generated_death",mobs[0],"legacy_generated_swords=0 external_sword=1 damage=7");h.succeed();});
    }
    @GameTest(structure=ARENA,maxTicks=110)
    public void externalPlainSwordDropsButGeneratedSwordWithoutBowDoesNot(GameTestHelper h){
        floor(h,true);var external=skeleton(h,EntityTypes.SKELETON,6,8);var generated=skeleton(h,EntityTypes.SKELETON,18,8);
        external.setAttached(SkeletonState.RESERVE,new ItemStack(Items.WOODEN_SWORD));var t1=target(h,6,10);var t2=target(h,18,10);
        h.runAtTickTime(70,()->{h.assertTrue(external.getMainHandItem().is(Items.WOODEN_SWORD)&&!SkeletonState.generatedSword(external.getMainHandItem())&&SkeletonState.generatedSword(generated.getMainHandItem()),"Actual melee distinguishes even an unmodified external wooden sword");
            // The original bow is gone; death cleanup must also suppress the generated sword still in hand.
            generated.removeAttached(SkeletonState.RESERVE);
            for(var m:new AbstractSkeleton[]{external,generated}){m.setDropChance(EquipmentSlot.MAINHAND,2);m.getAttached(SkeletonState.DATA).putFloat("reserve_drop",2);m.hurtServer(h.getLevel(),m.damageSources().generic(),1000);}t1.discard();t2.discard();});
        h.runAtTickTime(95,()->{var drops=h.getLevel().getEntitiesOfClass(ItemEntity.class,external.getBoundingBox().inflate(28));
            h.assertTrue(drops.stream().filter(e->e.getItem().is(Items.WOODEN_SWORD)).mapToInt(e->e.getItem().getCount()).sum()==1&&drops.stream().filter(e->e.getItem().is(Items.BOW)).mapToInt(e->e.getItem().getCount()).sum()==1,"Only the external sword and its actual bow drop; no replacement bow or generated sword");evidence("S02_generated_orphan_external",external,"generated_swords=0 external_plain_sword=1 bow=1");h.succeed();});
    }
    @GameTest(structure=ARENA,maxTicks=210)
    public void liveOptOutRestoresOriginalVanillaGoalAndBow(GameTestHelper h){
        floor(h,true);var m=skeleton(h,EntityTypes.SKELETON,8,8);var t=target(h,8,10);var seen=arrows(h,m);
        h.runAtTickTime(60,()->{h.assertTrue(m.getMainHandItem().is(Items.WOODEN_SWORD),"New melee active before disable");m.addTag("buildupmobtweaks:vanilla_ai");t.snapTo(h.absolutePos(new BlockPos(8,2,20)),0,0);});
        h.runAtTickTime(195,()->{h.assertTrue(!SkeletonState.runtime(m).managed&&m.getMainHandItem().is(Items.BOW)&&seen[0],"Restored vanilla Goal really shoots owned bow");
            evidence("disabled_vanilla",m,"arrow=true managed=false");m.discard();t.discard();h.succeed();});
    }
    @GameTest(structure=ARENA,maxTicks=790)
    public void reachableRoofShelterThenAttackResumes(GameTestHelper h){
        floor(h,false);AbstractSkeleton[] refs=new AbstractSkeleton[1];IronGolem[] targets=new IronGolem[1];boolean[] reached={false},shotAfterRoof={false};int[] shotsAtRoof={0};
        h.onEachTick(()->{var m=refs[0];if(m==null||m.isRemoved())return;var rt=SkeletonState.runtime(m);
            if(!reached[0]&&rt.shelters>0&&!h.getLevel().canSeeSky(m.blockPosition())){reached[0]=true;shotsAtRoof[0]=rt.shots;}
            if(reached[0]&&rt.shots>shotsAtRoof[0])shotAfterRoof[0]=true;});
        h.runAtTickTime(550,()->{
            var clock=h.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.WORLD_CLOCK).getOrThrow(net.minecraft.world.clock.WorldClocks.OVERWORLD);
            h.getLevel().clockManager().setTotalTicks(clock,6000);
            for(int x=12;x<=20;x++)for(int z=4;z<=14;z++)h.setBlock(new BlockPos(x,5,z),Blocks.STONE);
            refs[0]=skeleton(h,EntityTypes.SKELETON,8,8);refs[0].addTag("buildupmobtweaks:disable_ranged_reposition");targets[0]=target(h,8,20);
        });
        h.runAtTickTime(770,()->{var m=refs[0];h.assertTrue(reached[0],"Reached real accessible roof");
            h.assertTrue(shotAfterRoof[0],"Attack resumes after shelter navigation");evidence("S06_shelter",m,"roof=true arrow=true");m.discard();targets[0].discard();h.succeed();});
    }
    @GameTest(structure=ARENA,maxTicks=1230)
    public void nightWalkingTransitionsToDaySniper(GameTestHelper h){
        floor(h,false);AbstractSkeleton[] refs=new AbstractSkeleton[1];IronGolem[] targets=new IronGolem[1];boolean[][] seen={arrows(h,()->refs[0])};
        h.runAtTickTime(800,()->{
            var clock=h.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.WORLD_CLOCK).getOrThrow(net.minecraft.world.clock.WorldClocks.OVERWORLD);
            h.getLevel().clockManager().setTotalTicks(clock,18000);
            refs[0]=skeleton(h,EntityTypes.SKELETON,8,8);refs[0].addTag("buildupmobtweaks:disable_skeleton_shelter");targets[0]=target(h,8,18);
        });
        h.runAtTickTime(980,()->{var m=refs[0];h.assertTrue(!h.getLevel().isBrightOutside()&&SkeletonState.runtime(m).mode.equals("walk")&&seen[0][0]&&!seen[0][1],"Night sky uses actual walking attack");
            evidence("S01_night",m,"arrow=true critical=false");
            var clock=h.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.WORLD_CLOCK).getOrThrow(net.minecraft.world.clock.WorldClocks.OVERWORLD);h.getLevel().clockManager().setTotalTicks(clock,6000);});
        h.runAtTickTime(1200,()->{var m=refs[0];h.assertTrue(h.getLevel().isBrightOutside()&&SkeletonState.runtime(m).mode.equals("sniper")&&seen[0][1],"Day transition really fires critical arrow");
            evidence("S01_day",m,"critical=true");m.discard();targets[0].discard();h.succeed();});
    }
    @GameTest(structure=ARENA,maxTicks=45)
    public void freshBirthOwnsSwordButReloadNeverRefillsLostReserve(GameTestHelper h){
        floor(h,true);var m=EntityTypes.SKELETON.spawn(h.getLevel(),h.absolutePos(new BlockPos(8,2,8)),EntitySpawnReason.SPAWN_ITEM_USE);
        h.runAtTickTime(10,()->{h.assertTrue(m.getMainHandItem().is(Items.BOW)&&SkeletonState.reserve(m).is(Items.WOODEN_SWORD),"Real default spawn owns bow and reserve");
            m.removeAttached(SkeletonState.RESERVE);var loaded=CombatTestWorld.reload(h,m);h.assertTrue(SkeletonState.reserve(loaded).isEmpty(),"Reload does not synthesize lost sword");
            evidence("S02_default_birth",loaded,"missing_reserve_not_refilled=true");loaded.discard();h.succeed();});
    }
    @GameTest(structure=ARENA,maxTicks=350)
    public void obstructionExitReacquisitionAndSpecialDisableCleanup(GameTestHelper h){
        floor(h,true);var m=skeleton(h,EntityTypes.BOGGED,8,8);m.addTag("buildupmobtweaks:disable_ranged_reposition");var t=target(h,8,13);
        h.runAtTickTime(125,()->t.snapTo(m.position().add(0,0,4),0,0));
        h.runAtTickTime(170,()->{
            h.assertTrue(SkeletonState.runtime(m).dodges>0,"Real special happened before independent disable");
            m.addTag("buildupmobtweaks:disable_bogged_spore_retreat");
            t.snapTo(h.absolutePos(new BlockPos(8,2,13)),0,0);
            for(int x=6;x<=10;x++)for(int z=11;z<=15;z++)for(int y=2;y<=6;y++)if(x==6||x==10||z==11||z==15||y==6)h.setBlock(new BlockPos(x,y,z),Blocks.STONE);
        });
        h.runAtTickTime(245,()->{
            h.assertTrue(m.getTarget()==null&&!m.isUsingItem(),"Real occlusion expires target and use");
            h.assertTrue(((SkeletonAccess)m).buildup$special()==0&&SkeletonState.runtime(m).clouds.isEmpty(),"Disabled special and clouds cleaned");
            for(int x=6;x<=10;x++)for(int z=11;z<=15;z++)for(int y=2;y<=6;y++)h.setBlock(new BlockPos(x,y,z),Blocks.AIR);
        });
        h.runAtTickTime(325,()->{h.assertTrue(m.getTarget()==t,"Original selector reacquires within 80 ticks after obstruction removed");evidence("lifecycle_obstruction",m,"target_reacquired=true clouds=0");m.discard();t.discard();h.succeed();});
    }
    record PlayerFixture(net.minecraft.server.level.ServerPlayer player,io.netty.channel.embedded.EmbeddedChannel channel,net.minecraft.network.Connection connection){
        void close(){player.level().getServer().getConnection().getConnections().remove(connection);player.level().getServer().getPlayerList().remove(player);player.discard();channel.finishAndReleaseAll();}
    }
    static PlayerFixture player(GameTestHelper h){
        var profile=new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"R2B_"+java.util.UUID.randomUUID().toString().substring(0,8));
        var cookie=net.minecraft.server.network.CommonListenerCookie.createInitial(profile,false);
        var p=new net.minecraft.server.level.ServerPlayer(h.getLevel().getServer(),h.getLevel(),profile,cookie.clientInformation()){@Override public boolean isClientAuthoritative(){return false;}};
        var connection=new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);var channel=new io.netty.channel.embedded.EmbeddedChannel(connection);
        h.getLevel().getServer().getConnection().getConnections().add(connection);h.getLevel().getServer().getPlayerList().placeNewPlayer(connection,p,cookie);
        p.connection.handleAcceptPlayerLoad(new net.minecraft.network.protocol.game.ServerboundPlayerLoadedPacket());p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        p.snapTo(h.absolutePos(new BlockPos(8,2,18)),0,0);return new PlayerFixture(p,channel,connection);
    }
    @GameTest(structure=ARENA,maxTicks=240)
    public void realPlayerCreativeSpectatorAndDistanceReleaseTarget(GameTestHelper h){
        floor(h,true);var m=skeleton(h,EntityTypes.SKELETON,8,8);var fixture=player(h);var p=fixture.player();
        m.addTag("buildupmobtweaks:disable_ranged_reposition");
        h.runAtTickTime(40,()->{h.assertTrue(m.getTarget()==p,"Natural survival aggro");p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);});
        h.runAtTickTime(60,()->{h.assertTrue(m.getTarget()==null&&!m.isUsingItem(),"Creative releases target and draw");p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);});
        h.runAtTickTime(105,()->{h.assertTrue(m.getTarget()==p,"Survival reacquires");p.setGameMode(net.minecraft.world.level.GameType.SPECTATOR);});
        h.runAtTickTime(125,()->{h.assertTrue(m.getTarget()==null&&!m.isUsingItem(),"Spectator releases target");p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);});
        h.runAtTickTime(170,()->{evidence("player_reacquire_fixture",m,"playerHealth="+p.getHealth()+" playerPos="+p.position()+" target="+m.getTarget());h.assertTrue(m.getTarget()==p,"Reacquires again");p.snapTo(m.position().add(45,0,0),0,0);});
        h.runAtTickTime(190,()->{h.assertTrue(m.getTarget()==null&&!m.isUsingItem(),"Beyond follow range releases");evidence("lifecycle_player_modes",m,"creative_spectator_distance=true");m.discard();fixture.close();h.succeed();});
    }
    @GameTest(structure=ARENA,maxTicks=55)
    public void excludedVariantsAndFutureStateKeepVanillaGoals(GameTestHelper h){
        floor(h,true);var wither=EntityTypes.WITHER_SKELETON.spawn(h.getLevel(),h.absolutePos(new BlockPos(8,2,8)),EntitySpawnReason.SPAWN_ITEM_USE);
        var parched=EntityTypes.PARCHED.spawn(h.getLevel(),h.absolutePos(new BlockPos(18,2,18)),EntitySpawnReason.SPAWN_ITEM_USE);
        var future=EntityTypes.SKELETON.create(h.getLevel(),EntitySpawnReason.LOAD);future.snapTo(h.absolutePos(new BlockPos(18,2,8)),0,0);
        var state=new net.minecraft.nbt.CompoundTag();state.putInt("version",99);state.putString("sentinel","unchanged");future.setAttached(SkeletonState.DATA,state);
        future.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.BOW));h.getLevel().addFreshEntity(future);
        h.runAtTickTime(35,()->{for(var m:new AbstractSkeleton[]{wither,parched,future})h.assertTrue(m.getGoalSelector().getAvailableGoals().stream().noneMatch(g->g.getGoal().getClass().getName().startsWith("com.davidblackcn.buildupmobtweaks.rebuild.skeleton")),"Excluded mob has no new skeleton Goals");
            h.assertTrue(future.getAttached(SkeletonState.DATA).getStringOr("sentinel","").equals("unchanged"),"Future data unchanged");wither.discard();parched.discard();future.discard();h.succeed();});
    }
    @GameTest(structure=ARENA,maxTicks=250)
    public void unarmedStraySnowballActuallyDamagesTarget(GameTestHelper h){
        floor(h,true);var m=skeleton(h,EntityTypes.STRAY,8,8);m.setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);m.removeAttached(SkeletonState.RESERVE);
        var t=target(h,8,16);boolean[] snowDamage={false};
        h.onEachTick(()->{if(t.getLastDamageSource()!=null&&t.getLastDamageSource().getDirectEntity() instanceof Snowball&&t.getHealth()<100)snowDamage[0]=true;});
        h.runAtTickTime(235,()->{h.assertTrue(SkeletonState.runtime(m).flips>0&&SkeletonState.runtime(m).snowHits>0&&snowDamage[0],"Actual thrown snowball collision damaged target");
            evidence("S03_snow_damage",m,"confirmed_hits="+SkeletonState.runtime(m).snowHits+" health="+t.getHealth());m.discard();t.discard();h.succeed();});
    }
    @GameTest(structure=ARENA,maxTicks=1550)
    public void realConfigDisableRestoresVanillaAndKeepsReserve(GameTestHelper h){
        floor(h,true);AbstractSkeleton[] refs=new AbstractSkeleton[1];IronGolem[] targets=new IronGolem[1];boolean[] seen=arrows(h,()->refs[0]);
        h.runAtTickTime(1300,()->{refs[0]=skeleton(h,EntityTypes.SKELETON,8,8);targets[0]=target(h,8,10);});
        h.runAtTickTime(1360,()->{h.assertTrue(refs[0].getMainHandItem().is(Items.WOODEN_SWORD),"Real melee before individual config change");BuildupMobTweaks.config().hostile.skeleton.weaponSwitching.accept(false);});
        h.runAtTickTime(1380,()->{h.assertTrue(refs[0].getMainHandItem().is(Items.BOW)&&SkeletonState.reserve(refs[0]).is(Items.WOODEN_SWORD),"Individual disable restores owned weapon");
            BuildupMobTweaks.config().general.enabled.accept(false);targets[0].snapTo(h.absolutePos(new BlockPos(8,2,20)),0,0);});
        h.runAtTickTime(1520,()->{var m=refs[0];h.assertTrue(!SkeletonState.runtime(m).managed&&seen[0],"Master disabled original vanilla Goal shoots");
            evidence("config_disable",m,"reserve=sword vanilla_arrow=true");m.discard();targets[0].discard();BuildupMobTweaks.config().general.enabled.accept(true);BuildupMobTweaks.config().hostile.skeleton.weaponSwitching.accept(true);h.succeed();});
    }

    @GameTest(structure=ARENA,maxTicks=510)
    public void powderSnowConversionTransfersOwnedReserve(GameTestHelper h){
        floor(h,true);var m=skeleton(h,EntityTypes.SKELETON,8,8);
        var reserve=SkeletonState.reserve(m);reserve.set(DataComponents.CUSTOM_NAME,Component.literal("conversion reserve"));
        m.getAttached(SkeletonState.DATA).putLong("special_ready",h.getLevel().getGameTime()+900);
        for(int x=7;x<=9;x++)for(int z=7;z<=9;z++)for(int y=2;y<=4;y++)
            h.setBlock(new BlockPos(x,y,z),x==8&&z==8?Blocks.POWDER_SNOW:Blocks.STONE);
        h.runAtTickTime(490,()->{
            var converted=h.getLevel().getEntitiesOfClass(AbstractSkeleton.class,m.getBoundingBox().inflate(3),s->s.getType()==EntityTypes.STRAY);
            h.assertTrue(m.isRemoved()&&converted.size()==1,"Actual powder snow conversion occurred");var s=converted.getFirst();
            h.assertTrue(SkeletonState.reserve(s)==reserve&&SkeletonState.reserve(m).isEmpty(),"Actual reserve transferred once");
            h.assertTrue(s.getMainHandItem().is(Items.BOW)&&s.getAttached(SkeletonState.DATA).getLongOr("special_ready",0)>h.getLevel().getGameTime(),"Equipment and cooldown retained");
            evidence("conversion",s,"reserve_owned=true cooldown_retained=true");s.discard();h.succeed();
        });
    }

    @GameTest(structure=ARENA,maxTicks=190)
    public void unreachableRoofDoesNotBlockAttackOrChangeBlocks(GameTestHelper h){
        floor(h,false);var m=skeleton(h,EntityTypes.SKELETON,8,8);m.addTag("buildupmobtweaks:disable_ranged_reposition");var t=target(h,8,18);var seen=arrows(h,m);
        for(int x=12;x<=20;x++)for(int z=4;z<=14;z++)for(int y=2;y<=6;y++)h.setBlock(new BlockPos(x,y,z),Blocks.STONE);
        h.runAtTickTime(180,()->{h.assertTrue(seen[0]&&SkeletonState.runtime(m).shelters==0,"Unreachable cover never claims MOVE and actual bow still fires");
            h.assertTrue(h.getLevel().getBlockState(h.absolutePos(new BlockPos(12,2,8))).is(Blocks.STONE),"Cover unchanged");
            evidence("S06_unreachable",m,"arrow=true blocks_unchanged=true");m.discard();t.discard();h.succeed();});
    }

    @GameTest(structure=ARENA,maxTicks=110)
    public void subclassReserveReturnsOnOptOutAndForeignWeaponYields(GameTestHelper h){
        floor(h,true);var m=skeleton(h,EntityTypes.SKELETON,8,8);var bow=new ItemStack(CombatTestItems.BOW);m.setItemSlot(EquipmentSlot.MAINHAND,bow);var t=target(h,8,10);
        h.runAtTickTime(60,()->{h.assertTrue(m.getMainHandItem().is(Items.WOODEN_SWORD)&&SkeletonState.reserve(m)==bow,"Actual subclass bow stored for melee");m.addTag("buildupmobtweaks:vanilla_ai");});
        h.runAtTickTime(80,()->{h.assertTrue(m.getMainHandItem()==bow,"Disabled compatibility still returns the owned subclass bow");m.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.CROSSBOW));m.removeTag("buildupmobtweaks:vanilla_ai");});
        h.runAtTickTime(100,()->{h.assertTrue(!SkeletonState.runtime(m).managed&&m.getMainHandItem().is(Items.CROSSBOW),"Unrecognized projectile weapon yields to original Goals");
            evidence("compatibility_exit",m,"owned_subclass_returned=true foreign_weapon_yield=true");m.discard();t.discard();h.succeed();});
    }

    @GameTest(structure=ARENA,maxTicks=210)
    public void reloadEndsDodgeAndDiscardsSavedTransientCloud(GameTestHelper h){
        floor(h,true);var m=skeleton(h,EntityTypes.BOGGED,8,8);m.addTag("buildupmobtweaks:disable_ranged_reposition");var t=target(h,8,13);
        AbstractSkeleton[] loaded={null};boolean[] checked={false};
        h.runAtTickTime(125,()->t.snapTo(m.position().add(0,0,4),0,0));
        h.onEachTick(()->{if(checked[0]||SkeletonState.runtime(m).clouds.isEmpty())return;
            var cloud=SkeletonState.runtime(m).clouds.getFirst();if(cloud.isRemoved())return;
            long ready=m.getAttached(SkeletonState.DATA).getLongOr("special_ready",0);
            var restoredCloud=CombatTestWorld.reload(h,cloud);loaded[0]=CombatTestWorld.reload(h,m);
            h.assertTrue(restoredCloud.isRemoved(),"Serialized in-flight cloud discarded on load");
            h.assertTrue(((SkeletonAccess)loaded[0]).buildup$special()==0&&loaded[0].getAttached(SkeletonState.DATA).getLongOr("special_ready",0)==ready,"Dodge phase cleared but cooldown retained");checked[0]=true;
        });
        h.runAtTickTime(195,()->{h.assertTrue(checked[0],"Observed actual dodge before reload");evidence("S04_reload",loaded[0],"cloud_discarded=true cooldown_preserved=true");loaded[0].discard();t.discard();h.succeed();});
    }

    @GameTest(structure=ARENA,maxTicks=260)
    public void walkingStrafeFiresWithoutSteppingOffNarrowPlatform(GameTestHelper h){
        for(int x=0;x<28;x++)for(int z=0;z<28;z++)h.setBlock(new BlockPos(x,6,z),Blocks.STONE);
        for(int x=10;x<=12;x++)for(int z=5;z<=23;z++)h.setBlock(new BlockPos(x,1,z),Blocks.STONE);
        var m=skeleton(h,EntityTypes.SKELETON,11,8);m.addTag("buildupmobtweaks:disable_skeleton_sniping");var t=target(h,11,18);var seen=arrows(h,m);double base=m.getY();double[] min={base};
        h.onEachTick(()->min[0]=Math.min(min[0],m.getY()));
        h.runAtTickTime(245,()->{h.assertTrue(seen[0]&&min[0]>=base-.15,"Actual walking bow goal fires while staying on the three-block platform");
            evidence("safe_strafe",m,"minY="+min[0]+" baseY="+base+" arrow=true");m.discard();t.discard();h.succeed();});
    }
}
