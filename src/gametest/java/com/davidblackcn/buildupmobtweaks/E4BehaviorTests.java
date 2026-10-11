package com.davidblackcn.buildupmobtweaks;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import java.util.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.projectile.arrow.*;
import net.minecraft.world.level.block.Blocks;
import com.davidblackcn.buildupmobtweaks.rebuild.general.E4VisualState;

/** Only world inputs and observations; native selectors and actual ticks perform the work. */
public class E4BehaviorTests {
    private static final Map<Entity,java.util.function.Consumer<AbstractArrow>> OBSERVERS=new IdentityHashMap<>();
    static {net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents.ENTITY_LOAD.register((e,l)->{if(e instanceof AbstractArrow a){var observer=OBSERVERS.get(a.getOwner());if(observer!=null)observer.accept(a);}});}
    static final String ENV="buildupmobtweaks:r4e4",ARENA="buildupmobtweaks:r2a_arena";
    static boolean fleeing(Mob m){return m.getGoalSelector().getAvailableGoals().stream().anyMatch(g->g.isRunning()&&g.getGoal() instanceof AvoidEntityGoal);}
    private <T extends Mob> void sneak(GameTestHelper h,EntityType<T> type,String mode){
        SkeletonBehaviorTests.floor(h,true);var m=RidingRelationTests.mob(h,type,12,12);m.getRandom().setSeed(90401);
        var f=SkeletonBehaviorTests.player(h);var p=f.player();p.snapTo(h.absolutePos(new BlockPos(12,2,15)),0,0);p.setGameMode(GameType.SPECTATOR);
        if(mode.equals("off"))m.addTag("buildupmobtweaks:disable_animal_sneak_approach");
        var start=m.position();boolean[] ran={false};double[] moved={0};
        h.runAtTickTime(3,()->{p.setShiftKeyDown(!mode.equals("stand"));p.setGameMode(GameType.SURVIVAL);});
        h.onEachTick(()->{ran[0]|=fleeing(m);moved[0]=Math.max(moved[0],m.position().distanceTo(start));});
        h.runAtTickTime(45,()->{
            boolean expected=!mode.equals("sneak");BuildupMobTweaks.LOGGER.info("R4E4_PROOF sneak type={} mode={} goal={} moved={}",type,mode,ran[0],moved[0]);
            h.assertTrue(expected?ran[0]&&moved[0]>.5:!ran[0],"Actual native avoid goal and escape movement obey sneaking gate");m.discard();f.close();h.succeed();
        });
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=65) public void catStandFlees(GameTestHelper h){sneak(h,EntityTypes.CAT,"stand");}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=65) public void catSneakApproach(GameTestHelper h){sneak(h,EntityTypes.CAT,"sneak");}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=65) public void catOffNativeFlee(GameTestHelper h){sneak(h,EntityTypes.CAT,"off");}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=65) public void ocelotStandFlees(GameTestHelper h){sneak(h,EntityTypes.OCELOT,"stand");}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=65) public void ocelotSneakApproach(GameTestHelper h){sneak(h,EntityTypes.OCELOT,"sneak");}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=65) public void ocelotOffNativeFlee(GameTestHelper h){sneak(h,EntityTypes.OCELOT,"off");}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=65) public void rabbitStandFlees(GameTestHelper h){sneak(h,EntityTypes.RABBIT,"stand");}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=65) public void rabbitSneakApproach(GameTestHelper h){sneak(h,EntityTypes.RABBIT,"sneak");}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=65) public void rabbitOffNativeFlee(GameTestHelper h){sneak(h,EntityTypes.RABBIT,"off");}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=90) public void nearerSneakerCannotHideStandingThreat(GameTestHelper h){
        SkeletonBehaviorTests.floor(h,true);var m=RidingRelationTests.mob(h,EntityTypes.OCELOT,12,12);m.getRandom().setSeed(90401);var a=SkeletonBehaviorTests.player(h);var b=SkeletonBehaviorTests.player(h);
        a.player().setShiftKeyDown(true);a.player().snapTo(m.position().add(0,0,2),0,0);b.player().snapTo(m.position().add(0,0,5),0,0);var start=m.position();boolean[] ran={false};h.onEachTick(()->ran[0]|=fleeing(m));
        h.runAtTickTime(60,()->{h.assertTrue(ran[0]&&m.position().distanceTo(start)>1,"Second standing candidate is selected and native escape moves");m.discard();a.close();b.close();h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=85) public void sneakingPlayerDamageStillStartsNativePanic(GameTestHelper h){
        SkeletonBehaviorTests.floor(h,true);var m=RidingRelationTests.mob(h,EntityTypes.RABBIT,12,12);m.getRandom().setSeed(90401);var f=SkeletonBehaviorTests.player(h);f.player().setShiftKeyDown(true);f.player().snapTo(m.position().add(0,0,2),0,0);var start=m.position();
        h.runAtTickTime(5,()->m.hurtServer(h.getLevel(),m.damageSources().playerAttack(f.player()),2));
        h.runAtTickTime(55,()->{h.assertTrue(m.getHealth()<1000&&m.position().distanceTo(start)>.5,"Native hurt panic moves despite sneaking attacker");m.discard();f.close();h.succeed();});
    }
    void sense(GameTestHelper h,String kind,boolean off){
        SkeletonBehaviorTests.floor(h,true);var m=SkeletonBehaviorTests.skeleton(h,EntityTypes.SKELETON,8,8);m.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);m.addTag("buildupmobtweaks:disable_skeleton_sniping");m.addTag("buildupmobtweaks:disable_skeleton_weapon_switching");
        if(off)m.addTag("buildupmobtweaks:disable_sense_"+kind);var f=SkeletonBehaviorTests.player(h);var p=f.player();p.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);p.setHealth(1000);p.snapTo(m.position().add(0,0,kind.equals("crouching")?12:9),0,0);
        if(kind.equals("blindness"))m.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,100));
        if(kind.equals("crouching"))p.setShiftKeyDown(true);
        if(kind.equals("crawling")){for(int x=-1;x<=0;x++)for(int z=-1;z<=0;z++)h.setBlock(h.relativePos(p.blockPosition().above().offset(x,0,z)),Blocks.STONE);p.setPose(Pose.SWIMMING);h.onEachTick(()->p.setPose(Pose.SWIMMING));}
        if(kind.equals("invisibility")){p.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY,100));for(var slot:List.of(EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET))p.setItemSlot(slot,(slot==EquipmentSlot.HEAD?Items.IRON_HELMET:slot==EquipmentSlot.CHEST?Items.IRON_CHESTPLATE:slot==EquipmentSlot.LEGS?Items.IRON_LEGGINGS:Items.IRON_BOOTS).getDefaultInstance());}
        boolean[] acquired={false};h.onEachTick(()->acquired[0]|=m.getTarget()==p);
        h.runAtTickTime(55,()->{BuildupMobTweaks.LOGGER.info("R4E4_PROOF sense={} off={} acquired={} distance={}",kind,off,acquired[0],m.distanceTo(p));h.assertTrue(acquired[0]==off,"Native active target selection respects absolute range and off fallback");m.discard();f.close();h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=75) public void blindnessDiscovery(GameTestHelper h){sense(h,"blindness",false);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=75) public void blindnessOff(GameTestHelper h){sense(h,"blindness",true);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=75) public void crouchingDiscovery(GameTestHelper h){sense(h,"crouching",false);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=75) public void crouchingOff(GameTestHelper h){sense(h,"crouching",true);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=75) public void armoredInvisibleDiscovery(GameTestHelper h){sense(h,"invisibility",false);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=75) public void armoredInvisibleOff(GameTestHelper h){sense(h,"invisibility",true);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=75) public void physicalCrawlingDiscovery(GameTestHelper h){sense(h,"crawling",false);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=75) public void physicalCrawlingOff(GameTestHelper h){sense(h,"crawling",true);}
    void stagger(GameTestHelper h,int count,boolean off,boolean sniper,EntityType<? extends AbstractSkeleton> type){
        SkeletonBehaviorTests.floor(h,true);var shooters=new ArrayList<AbstractSkeleton>();var t=SkeletonBehaviorTests.target(h,12,19);var arrows=new HashSet<UUID>();var times=new ArrayList<String>();var fired=new HashSet<UUID>();var began=new HashMap<UUID,Long>();var minima=new ArrayList<Long>();
        for(int i=0;i<count;i++){var m=SkeletonBehaviorTests.skeleton(h,type,10+i%3*2,8+i/3*2);m.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);m.addTag("buildupmobtweaks:disable_skeleton_weapon_switching");if(!sniper)m.addTag("buildupmobtweaks:disable_skeleton_sniping");if(off)m.addTag("buildupmobtweaks:disable_skeleton_staggered_draw");shooters.add(m);}
        h.onEachTick(()->{long tick=h.getLevel().getGameTime();for(var m:shooters)if(m.isUsingItem())began.putIfAbsent(m.getUUID(),tick);
            for(var a:h.getLevel().getEntitiesOfClass(AbstractArrow.class,t.getBoundingBox().inflate(30),a->shooters.contains(a.getOwner())))if(arrows.add(a.getUUID())){var owner=a.getOwner().getUUID();fired.add(owner);times.add(tick+":"+owner);if(began.containsKey(owner))minima.add(tick-began.remove(owner));}
        });
        h.runAtTickTime(260,()->{BuildupMobTweaks.LOGGER.info("R4E4_PROOF stagger count={} off={} sniper={} type={} times={} draws={}",count,off,sniper,type,times,minima);h.assertTrue(fired.size()==count,"Every shooter actually releases; bounded wait avoids starvation");h.assertTrue(!minima.isEmpty()&&minima.stream().allMatch(n->n>=19),"Native twenty tick draw preserved");if(count==2&&!off)h.assertTrue(times.size()>1&&!times.get(0).split(":")[0].equals(times.get(1).split(":")[0]),"Paired releases are staggered");shooters.forEach(Entity::discard);t.discard();h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=285) public void twoNativeBowShootersStagger(GameTestHelper h){stagger(h,2,false,false,EntityTypes.SKELETON);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=285) public void sixShootersNeverStarve(GameTestHelper h){stagger(h,6,false,false,EntityTypes.SKELETON);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=285) public void staggerOffPreservesArrows(GameTestHelper h){stagger(h,2,true,false,EntityTypes.SKELETON);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=285) public void sniperUsesSameCoordination(GameTestHelper h){stagger(h,2,false,true,EntityTypes.SKELETON);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=285) public void strayStagger(GameTestHelper h){stagger(h,2,false,false,EntityTypes.STRAY);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=285) public void boggedStagger(GameTestHelper h){stagger(h,2,false,false,EntityTypes.BOGGED);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=285) public void parchedStagger(GameTestHelper h){stagger(h,2,false,false,EntityTypes.PARCHED);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=285) public void witherStagger(GameTestHelper h){stagger(h,2,false,false,EntityTypes.WITHER_SKELETON);}
    void melee(GameTestHelper h,String mode){
        SkeletonBehaviorTests.floor(h,true);var m=RidingRelationTests.mob(h,EntityTypes.ZOMBIE,8,8);m.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);var t=SkeletonBehaviorTests.target(h,8,9);t.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);t.setHealth(1000);
        if(mode.equals("slow"))m.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE,400,4));else m.addEffect(new MobEffectInstance(MobEffects.HASTE,400,1));if(mode.equals("off"))m.addTag("buildupmobtweaks:disable_melee_effect_interval");
        var hits=new ArrayList<Long>();float[] hp={t.getHealth()};h.onEachTick(()->{if(t.getHealth()<hp[0])hits.add(h.getLevel().getGameTime());hp[0]=t.getHealth();});
        h.runAtTickTime(170,()->{BuildupMobTweaks.LOGGER.info("R4E4_PROOF melee mode={} realDamageTimes={}",mode,hits);h.assertTrue(hits.size()>=3,"Native melee goal actually damages repeatedly");for(int i=1;i<hits.size();i++)h.assertTrue(hits.get(i)-hits.get(i-1)>=(mode.equals("slow")?30:mode.equals("off")?20:16),"Cooldown and native invulnerability preserved");m.discard();t.discard();h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=195) public void fatigueSlowsActualMeleeHits(GameTestHelper h){melee(h,"slow");}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=195) public void hastePreservesNativeInvulnerability(GameTestHelper h){melee(h,"fast");}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=195) public void meleeOffNativeTiming(GameTestHelper h){melee(h,"off");}
    private void meleeRestart(GameTestHelper h,boolean off){
        SkeletonBehaviorTests.floor(h,true);var m=RidingRelationTests.mob(h,EntityTypes.ZOMBIE,8,8);m.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);m.getRandom().setSeed(51660);
        m.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE,500,89));if(off)m.addTag("buildupmobtweaks:disable_melee_effect_interval");
        var first=SkeletonBehaviorTests.target(h,8,9);first.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);first.setHealth(1000);
        Mob[] second={null};long[] hits={-1,-1};boolean[] gap={false};
        h.onEachTick(()->{
            if(hits[0]<0&&first.getHealth()<1000){hits[0]=h.getLevel().getGameTime();first.discard();}
            if(hits[0]>=0&&second[0]==null&&h.getLevel().getGameTime()>hits[0]+3){second[0]=SkeletonBehaviorTests.target(h,8,9);second[0].getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);second[0].setHealth(1000);}
            if(hits[0]>=0&&second[0]==null)gap[0]|=m.getGoalSelector().getAvailableGoals().stream().noneMatch(g->g.isRunning()&&g.getGoal() instanceof net.minecraft.world.entity.ai.goal.MeleeAttackGoal);
            if(second[0]!=null&&hits[1]<0&&second[0].getHealth()<1000)hits[1]=h.getLevel().getGameTime();
        });
        h.runAtTickTime(300,()->{long elapsed=hits[1]-hits[0];BuildupMobTweaks.LOGGER.info("R5_MELEE_RESTART off={} first={} second={} elapsed={} nativeGoalStopped={}",off,hits[0],hits[1],elapsed,gap[0]);
            h.assertTrue(hits[0]>=0&&hits[1]>hits[0]&&gap[0],"Actual first damage, target removal, native Goal stop and second target damage");
            h.assertTrue(off?elapsed<200:elapsed>=200,"Enabled effect deadline survives native Goal restart; off retains original restart behavior");m.discard();first.discard();second[0].discard();h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=325) public void fatigueDeadlineSurvivesNativeGoalRestart(GameTestHelper h){meleeRestart(h,false);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=325) public void fatigueDeadlineOffPreservesNativeRestart(GameTestHelper h){meleeRestart(h,true);}
    void vex(GameTestHelper h,boolean off){SkeletonBehaviorTests.floor(h,true);var m=RidingRelationTests.mob(h,EntityTypes.VEX,12,12);m.setNoGravity(true);if(off)m.addTag("buildupmobtweaks:disable_vex_projectile_weakness");var f=SkeletonBehaviorTests.player(h);f.player().setGameMode(GameType.SPECTATOR);
        h.runAtTickTime(5,()->{var a=new Arrow(h.getLevel(),f.player(),Items.ARROW.getDefaultInstance(),null);a.setBaseDamage(2);a.setNoGravity(true);a.snapTo(m.getX()-3,m.getY()+.4,m.getZ(),0,0);a.shoot(1,0,0,1,0);h.getLevel().addFreshEntity(a);});
        h.runAtTickTime(16,()->{float damage=1000-m.getHealth();BuildupMobTweaks.LOGGER.info("R4E4_PROOF vex off={} collisionDamage={}",off,damage);h.assertTrue(Math.abs(damage-(off?1.92:5.904))<.002,"Actual arrow collision triples incoming damage before native armor reduction (fixture helmet grants 2 armor)");m.discard();f.close();h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=30) public void vexActualArrowTriples(GameTestHelper h){vex(h,false);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=30) public void vexOffActualArrow(GameTestHelper h){vex(h,true);}
    void glow(GameTestHelper h,String mode){SkeletonBehaviorTests.floor(h,true);var m=SkeletonBehaviorTests.skeleton(h,EntityTypes.SKELETON,8,8);m.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);m.addTag("buildupmobtweaks:disable_skeleton_sniping");var t=SkeletonBehaviorTests.target(h,8,17);t.addEffect(new MobEffectInstance(MobEffects.GLOWING,250));if(mode.equals("off"))m.addTag("buildupmobtweaks:disable_sense_glowing");if(mode.equals("blind"))m.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,250));
        for(int x=4;x<=12;x++)for(int y=2;y<=5;y++)h.setBlock(new BlockPos(x,y,13),Blocks.STONE);boolean[] acquired={false};int[] shots={0};var ids=new HashSet<UUID>();
        h.onEachTick(()->{acquired[0]|=m.getTarget()==t;for(var a:h.getLevel().getEntitiesOfClass(AbstractArrow.class,m.getBoundingBox().inflate(24),a->a.getOwner()==m))if(ids.add(a.getUUID()))shots[0]++;});
        h.runAtTickTime(65,()->{BuildupMobTweaks.LOGGER.info("R4E4_PROOF glow mode={} discovered={} wallShots={}",mode,acquired[0],shots[0]);h.assertTrue(acquired[0]==mode.equals("on"),"Bounded glowing discovery obeys blindness priority and gate");h.assertTrue(shots[0]==0,"No arrow fired through physical wall");for(int x=4;x<=12;x++)for(int y=2;y<=5;y++)h.setBlock(new BlockPos(x,y,13),Blocks.AIR);m.removeEffect(MobEffects.BLINDNESS);});
        h.runAtTickTime(155,()->{h.assertTrue(shots[0]>0,"Original goal fires only after physical LOS restored");m.discard();t.discard();h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=175) public void glowDiscoveryDoesNotAllowWallArrows(GameTestHelper h){glow(h,"on");}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=175) public void blindnessWinsOverGlow(GameTestHelper h){glow(h,"blind");}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=175) public void glowingOffOriginalLos(GameTestHelper h){glow(h,"off");}
    void nausea(GameTestHelper h,boolean off){SkeletonBehaviorTests.floor(h,true);var t=SkeletonBehaviorTests.target(h,12,19);t.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100000);t.setHealth(100000);var ms=new ArrayList<AbstractSkeleton>();var ids=new HashSet<UUID>();var errors=new ArrayList<Double>();
        for(int i=0;i<6;i++){var m=SkeletonBehaviorTests.skeleton(h,EntityTypes.SKELETON,2+i*4,8);m.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(32);m.getRandom().setSeed(34000+i);m.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);m.addTag("buildupmobtweaks:disable_skeleton_sniping");m.addTag("buildupmobtweaks:disable_skeleton_weapon_switching");m.addTag("buildupmobtweaks:disable_skeleton_staggered_draw");m.addEffect(new MobEffectInstance(MobEffects.NAUSEA,400,255));if(off)m.addTag("buildupmobtweaks:disable_sense_nausea");ms.add(m);}
        for(var m:ms)OBSERVERS.put(m,a->{if(ids.add(a.getUUID())){var aim=t.position().add(0,t.getBbHeight()/3,0).subtract(a.position()).normalize();errors.add(1-a.getDeltaMovement().normalize().dot(aim));}});
        h.runAtTickTime(260,()->{double mean=errors.stream().mapToDouble(Double::doubleValue).average().orElseThrow();BuildupMobTweaks.LOGGER.info("R4E4_PROOF nausea off={} n={} errors={} mean={}",off,errors.size(),errors,mean);for(var m:ms)OBSERVERS.remove(m);h.assertTrue(errors.size()>=15,"Native goals produce actual trajectory samples");h.assertTrue(off?mean<.06:mean>.12,"Actual initial directional distribution distinguishes nausea from native gate off");ms.forEach(Entity::discard);t.discard();h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=285) public void nauseaActualNativeArrowDistribution(GameTestHelper h){nausea(h,false);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=285) public void nauseaOffActualNativeArrowDistribution(GameTestHelper h){nausea(h,true);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=80) public void invisibleNearestDoesNotHideOtherCandidate(GameTestHelper h){SkeletonBehaviorTests.floor(h,true);var m=SkeletonBehaviorTests.skeleton(h,EntityTypes.SKELETON,8,8);m.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);var a=SkeletonBehaviorTests.player(h);var b=SkeletonBehaviorTests.player(h);a.player().snapTo(m.position().add(0,0,5),0,0);a.player().addEffect(new MobEffectInstance(MobEffects.INVISIBILITY,100));a.player().setItemSlot(EquipmentSlot.HEAD,Items.IRON_HELMET.getDefaultInstance());a.player().setItemSlot(EquipmentSlot.CHEST,Items.IRON_CHESTPLATE.getDefaultInstance());a.player().setItemSlot(EquipmentSlot.LEGS,Items.IRON_LEGGINGS.getDefaultInstance());a.player().setItemSlot(EquipmentSlot.FEET,Items.IRON_BOOTS.getDefaultInstance());b.player().snapTo(m.position().add(0,0,9),0,0);boolean[] saw={false};h.onEachTick(()->saw[0]|=m.getTarget()==b.player());h.runAtTickTime(55,()->{h.assertTrue(saw[0],"Valid farther player selected instead of post-filtering nearest player");m.discard();a.close();b.close();h.succeed();});}
    void high(GameTestHelper h,boolean off){
        SkeletonBehaviorTests.floor(h,true);var f=SkeletonBehaviorTests.player(h);var t=f.player();var server=h.getLevel().getServer();var previous=h.getLevel().getDifficulty();server.setDifficulty(net.minecraft.world.Difficulty.HARD,true);
        var m=RidingRelationTests.mob(h,EntityTypes.ZOMBIE,8,8);m.getRandom().setSeed(321);m.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);var elevated=m.position().add(0,2,1.5);t.snapTo(elevated,0,0);t.setNoGravity(true);h.setBlock(new BlockPos(8,3,9),Blocks.STONE);
        if(off)m.addTag("buildupmobtweaks:disable_melee_high_target_jump");double[] y={m.getY()};boolean[] acquired={false};double initial=m.getY();h.onEachTick(()->{t.snapTo(elevated,0,0);y[0]=Math.max(y[0],m.getY());acquired[0]|=m.getTarget()==t;});
        h.runAtTickTime(60,()->{BuildupMobTweaks.LOGGER.info("R4E4_PROOF high off={} acquired={} dy={} targetHp={}",off,acquired[0],y[0]-initial,t.getHealth());m.discard();f.close();server.setDifficulty(previous,true);h.assertTrue(acquired[0]&&(off?y[0]-initial<.1:y[0]-initial>.5),"Native melee scheduler triggers physical high-target jump only when enabled");h.succeed();});
    }
    @GameTest(environment="buildupmobtweaks:r4e4_hard",structure=ARENA,maxTicks=80) public void hardHighTargetActualJump(GameTestHelper h){high(h,false);}
    @GameTest(environment="buildupmobtweaks:r4e4_hard_off",structure=ARENA,maxTicks=80) public void highTargetJumpOff(GameTestHelper h){high(h,true);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=40) public void runtimeVisualGatesPreserveDamage(GameTestHelper h){SkeletonBehaviorTests.floor(h,true);var m=RidingRelationTests.mob(h,EntityTypes.ZOMBIE,8,8);m.setRemainingFireTicks(300);
        h.runAtTickTime(8,()->{BuildupMobTweaks.LOGGER.info("R4E4_DEBUG visual fire={} frozen={} noai={} flags={} known={} gate={} tags={}",m.isOnFire(),m.isFullyFrozen(),m.isNoAi(),m.getAttachedOrCreate(E4VisualState.FLAGS),com.davidblackcn.buildupmobtweaks.rebuild.zombie.ZombieState.known(m),com.davidblackcn.buildupmobtweaks.rebuild.general.E4Behavior.enabled(m,com.davidblackcn.buildupmobtweaks.feature.FeatureId.BURN_FREEZE_VISUAL),m.entityTags());h.assertTrue(m.getAttachedOrCreate(E4VisualState.FLAGS)==1,"Real burning state synced");h.assertTrue(m.getHealth()<1000,"Native fire damage remains");m.addTag("buildupmobtweaks:disable_burn_freeze_visual");});
        h.runAtTickTime(15,()->{h.assertTrue(m.getAttachedOrCreate(E4VisualState.FLAGS)==0&&m.isOnFire(),"Disabling only clears own visual");m.discard();h.succeed();});}
    void motion(GameTestHelper h,String kind,boolean off){SkeletonBehaviorTests.floor(h,true);var m=SkeletonBehaviorTests.skeleton(h,EntityTypes.SKELETON,12,12);var t=SkeletonBehaviorTests.target(h,12,21);t.getAttribute(Attributes.MAX_HEALTH).setBaseValue(10000);t.setHealth(10000);m.getRandom().setSeed(61321);m.addTag("buildupmobtweaks:disable_skeleton_sniping");m.addTag("buildupmobtweaks:disable_skeleton_weapon_switching");m.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);
        if(off)m.addTag("buildupmobtweaks:disable_strafe_obstacle_jump");
        for(int x=10;x<=14;x++)for(int z=10;z<=14;z++)if(x==10||x==14||z==10||z==14){var p=new BlockPos(x,2,z);switch(kind){case "wall"->h.setBlock(p,Blocks.STONE);case "wall_fire"->{h.setBlock(p,Blocks.STONE);h.setBlock(p.above(),Blocks.FIRE);}case "fire"->h.setBlock(p,Blocks.FIRE);case "lava"->{h.setBlock(p.below(),Blocks.LAVA);h.setBlock(p.below().below(),Blocks.STONE);}case "powder"->h.setBlock(p,Blocks.POWDER_SNOW);case "edge"->h.setBlock(p.below(),Blocks.AIR);case "fence"->h.setBlock(p,Blocks.OAK_FENCE);}}
        double[] height={0},moved={0};int[] air={0};var initial=m.position();float health=m.getHealth();var arrows=SkeletonBehaviorTests.arrows(h,m);
        h.runAtTickTime(35,()->m.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(.25));
        h.onEachTick(()->{if(m.tickCount>=35){height[0]=Math.max(height[0],m.getY()-initial.y);moved[0]=Math.max(moved[0],m.position().subtract(initial).multiply(1,0,1).length());if(!m.onGround())air[0]++;}});
        h.runAtTickTime(180,()->{BuildupMobTweaks.LOGGER.info("R4E4_PROOF movement kind={} off={} height={} xz={} airTicks={} hp={} arrows={}",kind,off,height[0],moved[0],air[0],m.getHealth(),arrows[0]);h.assertTrue(arrows[0]&&moved[0]>.2,"Real native walk-bow scheduling and horizontal motion");if(kind.equals("wall"))h.assertTrue(off?height[0]<.2:height[0]>.5,"Obstacle strafe physically jumps only on gate");else {h.assertTrue(m.getHealth()==health&&m.getY()>=initial.y-.1,"Native strafe avoids real hazard/fall damage");if(kind.equals("wall_fire"))h.assertTrue(height[0]<.2,"Fire on top of ledge prevents extra jump");}t.discard();});
        h.runAtTickTime(220,()->{double last=m.position().distanceTo(initial);h.assertTrue(m.getTarget()==null&&Math.abs(m.xxa)<.001&&Math.abs(m.zza)<.001,"Target exit clears actual movement inputs");BuildupMobTweaks.LOGGER.info("R4E4_PROOF movement exit xxa={} zza={} distance={}",m.xxa,m.zza,last);m.discard();h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=240) public void nativeStrafeActualOneBlockJump(GameTestHelper h){motion(h,"wall",false);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=240) public void obstacleJumpOffNativeFallback(GameTestHelper h){motion(h,"wall",true);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=240) public void actualFireSafeStrafe(GameTestHelper h){motion(h,"fire",false);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=240) public void actualLavaSafeStrafe(GameTestHelper h){motion(h,"lava",false);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=240) public void actualPowderSnowSafeStrafe(GameTestHelper h){motion(h,"powder",false);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=240) public void actualDropSafeStrafe(GameTestHelper h){motion(h,"edge",false);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=240) public void fenceDoesNotTriggerJump(GameTestHelper h){motion(h,"fence",false);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=240) public void fireOnLedgePreventsActualJump(GameTestHelper h){motion(h,"wall_fire",false);}
    void evoker(GameTestHelper h,boolean off){SkeletonBehaviorTests.floor(h,true);var m=RidingRelationTests.mob(h,EntityTypes.EVOKER,12,12);m.getRandom().setSeed(90401);if(off)m.addTag("buildupmobtweaks:disable_evoker_flee_speed");var f=SkeletonBehaviorTests.player(h);f.player().snapTo(m.position().add(0,0,3),0,0);f.player().getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);f.player().setHealth(1000);var speeds=new ArrayList<Double>();boolean[] cast={false},badCast={false};var start=m.position();
        h.onEachTick(()->{if(fleeing(m))speeds.add(m.getMoveControl().getSpeedModifier());if(m.isCastingSpell()){cast[0]=true;badCast[0]|=fleeing(m)&&!m.getNavigation().isDone();}});
        h.runAtTickTime(100,()->{BuildupMobTweaks.LOGGER.info("R4E4_PROOF evoker off={} speeds={} moved={} casting={} badCast={}",off,speeds,m.position().distanceTo(start),cast[0],badCast[0]);h.assertTrue(speeds.stream().anyMatch(s->Math.abs(s-(off?1:.8))<.001)&&m.position().distanceTo(start)>.3,"Actual native avoid navigation applies independent speed style");h.assertTrue(!badCast[0],"Native spell and avoid do not compete");f.player().setGameMode(GameType.CREATIVE);});
        h.runAtTickTime(115,()->{h.assertTrue(!fleeing(m),"Creative threat exits native avoid lifecycle");m.discard();f.close();h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=135) public void evokerActualFleeAndCastingArbitration(GameTestHelper h){evoker(h,false);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=135) public void evokerSpeedOffOriginalNavigation(GameTestHelper h){evoker(h,true);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=50) public void vexNonProjectileDamageUnchanged(GameTestHelper h){SkeletonBehaviorTests.floor(h,true);var m=RidingRelationTests.mob(h,EntityTypes.VEX,8,8);h.runAtTickTime(5,()->m.hurtServer(h.getLevel(),m.damageSources().generic(),2));h.runAtTickTime(8,()->{h.assertTrue(Math.abs(1000-m.getHealth()-(m.damageSources().generic().is(net.minecraft.tags.DamageTypeTags.BYPASSES_ARMOR)?2:1.92))<.002,"Native armor and ordinary damage unchanged");m.discard();h.succeed();});}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=150) public void blindnessPreservesNativeHurtRetaliation(GameTestHelper h){
        SkeletonBehaviorTests.floor(h,true);var m=SkeletonBehaviorTests.skeleton(h,EntityTypes.SKELETON,8,8);m.addTag("buildupmobtweaks:disable_skeleton_sniping");m.addTag("buildupmobtweaks:disable_skeleton_weapon_switching");m.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);m.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,400));
        var f=SkeletonBehaviorTests.player(h);var p=f.player();p.snapTo(m.position().add(0,0,10),0,0);p.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);p.setHealth(1000);var arrows=SkeletonBehaviorTests.arrows(h,m);boolean[] retaliated={false};
        h.onEachTick(()->retaliated[0]|=m.getTarget()==p);
        h.runAtTickTime(40,()->{h.assertTrue(m.getTarget()==null,"Blind discovery range excludes distant player before damage");m.hurtServer(h.getLevel(),m.damageSources().playerAttack(p),1);});
        h.runAtTickTime(130,()->{BuildupMobTweaks.LOGGER.info("R4E4_PROOF blind retaliation={} actualArrows={} targetHp={}",retaliated[0],arrows[0],p.getHealth());m.discard();f.close();h.assertTrue(retaliated[0]&&arrows[0],"Native hurt target goal and actual bow attacks survive discovery-only limit");h.succeed();});
    }
}
