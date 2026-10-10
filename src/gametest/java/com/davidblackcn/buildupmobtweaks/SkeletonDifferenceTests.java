package com.davidblackcn.buildupmobtweaks;

import com.davidblackcn.buildupmobtweaks.rebuild.skeleton.*;
import com.davidblackcn.buildupmobtweaks.feature.*;
import com.davidblackcn.buildupmobtweaks.qa.QaHarness;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import java.util.*;

/** Fixtures only: actual Server Tick, vanilla Goal arbitration and native projectile collisions. */
public class SkeletonDifferenceTests {
    private static final Map<UUID,Integer> actualCloudDamage=new HashMap<>();
    static {net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.AFTER_DAMAGE.register((victim,source,base,taken,blocked)->{
        if(taken>0&&actualCloudDamage.containsKey(victim.getUUID())&&source.getDirectEntity() instanceof AreaEffectCloud c&&c.hasAttached(SkeletonDifferences.MUSHROOM))actualCloudDamage.merge(victim.getUUID(),1,Integer::sum);
    });}
    static boolean harming(AreaEffectCloud c){try{var f=AreaEffectCloud.class.getDeclaredField("potionContents");f.setAccessible(true);return ((net.minecraft.world.item.alchemy.PotionContents)f.get(c)).potion().orElseThrow().is(Potions.HARMING);}catch(ReflectiveOperationException e){throw new AssertionError(e);}}
    static final String ARENA="buildupmobtweaks:r2a_arena",ENV="buildupmobtweaks:r4e1c";
    static void proof(String scenario,AbstractSkeleton m,String detail){var r=SkeletonState.runtime(m);BuildupMobTweaks.LOGGER.info("R4E1C_PROOF scenario={} tick={} shots={} swings={} dodges={} mushrooms={} mushroomHits={} clouds={} flips={} snow={} {}",scenario,m.tickCount,r.shots,r.drawSwings,r.dodges,r.mushrooms,r.mushroomHits,r.mushroomClouds,r.flips,r.snowballs,detail);}
    static void clean(Entity... entities){for(var e:entities)if(e!=null)e.discard();}
    static AbstractSkeleton mob(GameTestHelper h,EntityType<? extends AbstractSkeleton> type){
        SkeletonBehaviorTests.floor(h,true);var m=SkeletonBehaviorTests.skeleton(h,type,8,8);m.getRandom().setSeed(601003);
        m.addTag("buildupmobtweaks:disable_skeleton_weapon_switching");m.addTag("buildupmobtweaks:disable_ranged_reposition");
        m.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);m.getAttached(SkeletonState.DATA).putLong("special_ready",0);return m;
    }
    static net.minecraft.world.entity.animal.golem.IronGolem target(GameTestHelper h,AbstractSkeleton m,boolean omen){
        var t=SkeletonBehaviorTests.target(h,8,m.getType()==EntityTypes.BOGGED?11:18);t.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);t.setHealth(1000);
        if(omen)t.addEffect(new MobEffectInstance(MobEffects.TRIAL_OMEN,1000));m.setTarget(t);return t;
    }
    static List<Snowball> balls(GameTestHelper h,AbstractSkeleton m){return h.getLevel().getEntitiesOfClass(Snowball.class,m.getBoundingBox().inflate(256),b->b.getOwner()==m&&b.hasAttached(SkeletonDifferences.MUSHROOM));}
    static List<AreaEffectCloud> clouds(GameTestHelper h,AbstractSkeleton m){return h.getLevel().getEntitiesOfClass(AreaEffectCloud.class,m.getBoundingBox().inflate(256),c->c.getOwner()==m&&c.hasAttached(SkeletonDifferences.MUSHROOM));}
    private void hands(GameTestHelper h,boolean left,boolean off,boolean enabled){
        var m=mob(h,EntityTypes.SKELETON);m.setLeftHanded(left);var bow=m.getMainHandItem();bow.setDamageValue(17);bow.set(DataComponents.CUSTOM_NAME,Component.literal("E1c owned bow"));
        if(off){m.setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);m.setItemSlot(EquipmentSlot.OFFHAND,bow);}
        if(!enabled)m.addTag("buildupmobtweaks:disable_skeleton_offhand_visual");var t=target(h,m,false);
        // Hand/pose compatibility fixture, not a long-range accuracy benchmark: keep native spread/collision.
        t.snapTo(m.position().add(0,0,6),0,0);boolean[] seen={false,false};
        h.onEachTick(()->{if(m.isUsingItem()){seen[0]=true;h.assertTrue(m.getUsedItemHand()==(off?InteractionHand.OFF_HAND:InteractionHand.MAIN_HAND),"Native draw uses actual owned bow hand");}
            if(m.isSwinging()){h.assertTrue(m.getCurrentSwing().hand()==(off?InteractionHand.MAIN_HAND:InteractionHand.OFF_HAND),"Actual swing is opposite current bow hand");seen[1]=true;}});
        h.runAtTickTime(175,()->{var r=SkeletonState.runtime(m);h.assertTrue(seen[0]&&r.shots>0&&t.getHealth()<1000,"Real native draw, arrow and damage");h.assertTrue(enabled?seen[1]&&r.drawSwings>0:!seen[1]&&r.drawSwings==0,"Independent swing gate keeps native fire");
            h.assertTrue(m.getItemInHand(off?InteractionHand.OFF_HAND:InteractionHand.MAIN_HAND)==bow&&bow.getDamageValue()>17&&bow.getHoverName().getString().equals("E1c owned bow"),"One real bow preserves name/durability");
            proof("hands_"+(left?"left":"right")+"_"+(off?"off":"main")+"_"+enabled,m,"native_damage="+t.getHealth());clean(m,t);h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=185) public void rightMainDraw(GameTestHelper h){hands(h,false,false,true);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=185) public void rightOffDraw(GameTestHelper h){hands(h,false,true,true);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=185) public void leftMainDraw(GameTestHelper h){hands(h,true,false,true);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=185) public void leftOffDraw(GameTestHelper h){hands(h,true,true,true);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=185) public void offhandVisualOffKeepsBowFire(GameTestHelper h){hands(h,false,true,false);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=185)
    public void visualAloneWorksWithOriginalBowGoalWithoutAiReplacement(GameTestHelper h){
        var m=mob(h,EntityTypes.SKELETON);for(String id:List.of("skeleton_sniping","skeleton_weapon_switching","skeleton_bow_compatibility","skeleton_safe_strafing"))m.addTag("buildupmobtweaks:disable_"+id);var t=target(h,m,false);var seen=SkeletonBehaviorTests.arrows(h,m);
        h.runAtTickTime(175,()->{h.assertTrue(!SkeletonState.runtime(m).managed&&seen[0]&&t.getHealth()<1000&&SkeletonState.runtime(m).drawSwings>0,"Independent visual still swings on original vanilla Bow Goal and real arrows");proof("visual_native_goal",m,"original_goal=true arrow=true");clean(m,t);h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=195)
    public void toggleAndNoAiRetireSyncedVisualWithoutMoreSwings(GameTestHelper h){
        var m=mob(h,EntityTypes.SKELETON);var t=target(h,m,false);int[] initial={-1};h.onEachTick(()->{if(initial[0]<0&&SkeletonState.runtime(m).drawSwings>0){initial[0]=SkeletonState.runtime(m).drawSwings;m.addTag("buildupmobtweaks:disable_skeleton_offhand_visual");}});
        h.runAtTickTime(175,()->{h.assertTrue(initial[0]>0&&SkeletonState.runtime(m).drawSwings==initial[0]&&SkeletonState.runtime(m).shots>0&&!m.getAttachedOrCreate(SkeletonDifferences.VISUAL),"Disable during actual draw: no later custom swings, fire continues, synced gate false");m.removeTag("buildupmobtweaks:disable_skeleton_offhand_visual");m.setNoAi(true);});
        h.runAtTickTime(185,()->{h.assertTrue(!m.getAttachedOrCreate(SkeletonDifferences.VISUAL)&&!m.getAttachedOrCreate(SkeletonDifferences.BOW_VISUAL),"NoAI clears server visual gates");proof("visual_toggle",m,"native_fire=true NoAI=true");clean(m,t);h.succeed();});
    }
    private void boggedGate(GameTestHelper h,String gate,boolean omen){
        var m=mob(h,EntityTypes.BOGGED);m.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.STONE_SWORD));if(gate!=null)m.addTag("buildupmobtweaks:"+gate);var t=target(h,m,omen);
        h.runAtTickTime(80,()->{var r=SkeletonState.runtime(m);h.assertTrue(r.mushrooms==0,"No unsupported mushroom release");if(gate==null||gate.equals("disable_bogged_omen_mushrooms"))h.assertTrue(r.dodges==1,"Existing spore phase still runs once");else h.assertTrue(r.dodges==0,"Global entity/base gate suppresses stage");proof("bogged_gate_"+(gate==null?"normal":gate),m,"omen="+omen);clean(m,t);h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=90) public void normalBoggedKeepsOnlySporePhase(GameTestHelper h){boggedGate(h,null,false);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=90) public void mushroomOffKeepsSporePhase(GameTestHelper h){boggedGate(h,"disable_bogged_omen_mushrooms",true);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=90) public void sporeOffHasNoMushrooms(GameTestHelper h){boggedGate(h,"disable_bogged_spore_retreat",true);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=90) public void vanillaTagHasNoExtraStages(GameTestHelper h){boggedGate(h,"vanilla_ai",true);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=90)
    public void twoColorsAndNativeBlockImpactsCreateBoundedHarmingClouds(GameTestHelper h){
        var m=mob(h,EntityTypes.BOGGED);m.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.STONE_SWORD));var t=target(h,m,true);Set<Item> colors=new HashSet<>();boolean[] cloud={false};
        h.onEachTick(()->{for(var b:balls(h,m))colors.add(b.getItem().getItem());for(var c:clouds(h,m)){cloud[0]=true;h.assertTrue(c.getDuration()==20&&c.getWaitTime()==10&&harming(c),"Actual impact cloud keeps upstream potion/duration/wait");}h.assertTrue(balls(h,m).size()<=2&&clouds(h,m).size()<=2,"Bounded in-flight count");});
        h.runAtTickTime(80,()->{var r=SkeletonState.runtime(m);h.assertTrue(r.dodges==1&&r.mushrooms==2&&colors.equals(Set.of(Items.RED_MUSHROOM,Items.BROWN_MUSHROOM)),"One existing stage releases exactly two colors");h.assertTrue(r.mushroomClouds==2&&cloud[0]&&balls(h,m).isEmpty()&&clouds(h,m).isEmpty(),"Both actual ground collisions burst once and clouds expire");proof("bogged_block_impacts",m,"red/brown=true impact_once=true expired=true");clean(m,t);h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=90)
    public void nativeEntityImpactHurtsAndBurstsOnce(GameTestHelper h){
        var m=mob(h,EntityTypes.BOGGED);m.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.STONE_SWORD));var t=target(h,m,true);boolean[] arranged={false};
        h.onEachTick(()->{if(!arranged[0]&&!balls(h,m).isEmpty()){arranged[0]=true;var ball=balls(h,m).getFirst();ball.setPos(t.position().add(0,1,2));ball.setDeltaMovement(0,0,-1);}});
        h.runAtTickTime(80,()->{h.assertTrue(arranged[0]&&SkeletonState.runtime(m).mushroomHits>0&&SkeletonState.runtime(m).mushroomClouds==2&&t.getHealth()<1000,"Native flight entity collision damages and generates a single burst");proof("bogged_entity_impact",m,"health="+t.getHealth());clean(m,t);h.succeed();});
    }
    private void exit(GameTestHelper h,String mode){
        var m=mob(h,EntityTypes.BOGGED);m.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.STONE_SWORD));var t=target(h,m,true);boolean[] changed={false};
        h.onEachTick(()->{if(!changed[0]&&SkeletonState.runtime(m).mushrooms==2){changed[0]=true;switch(mode){case "off"->m.addTag("buildupmobtweaks:disable_bogged_omen_mushrooms");case "noai"->m.setNoAi(true);case "target"->t.discard();case "death"->m.kill(h.getLevel());}}});
        h.runAtTickTime(65,()->{h.assertTrue(changed[0]&&balls(h,m).isEmpty()&&clouds(h,m).isEmpty()&&SkeletonState.runtime(m).mushroomClouds==0,"Active flight retires before a burst on exit");proof("bogged_exit_"+mode,m,"inflight=0");clean(m,t);h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=75) public void mushroomDisableRetiresFlight(GameTestHelper h){exit(h,"off");}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=75) public void noAiRetiresFlight(GameTestHelper h){exit(h,"noai");}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=75) public void targetDeathRetiresFlight(GameTestHelper h){exit(h,"target");}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=75) public void ownerDeathRetiresFlight(GameTestHelper h){exit(h,"death");}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=135)
    public void nativeLifetimeBudgetAndReloadDoNotResumeFlight(GameTestHelper h){
        AbstractSkeleton[] m={mob(h,EntityTypes.BOGGED)};m[0].setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.STONE_SWORD));var t=target(h,m[0],true);Snowball[] expired={null},loaded={null},control={null};long[] ready={0};int[] ages={0,0};double[] drops={Double.NaN,Double.NaN};
        h.onEachTick(()->{if(expired[0]==null&&!balls(h,m[0]).isEmpty()){
            var list=balls(h,m[0]);expired[0]=list.getFirst();expired[0].setPos(m[0].position().add(0,200,0));expired[0].setDeltaMovement(Vec3.ZERO);ages[0]=expired[0].tickCount;
            control[0]=new Snowball(h.getLevel(),m[0],Items.SNOWBALL.getDefaultInstance());control[0].setPos(m[0].position().add(1,200,0));control[0].setDeltaMovement(Vec3.ZERO);h.getLevel().addFreshEntity(control[0]);ages[1]=control[0].tickCount;
            loaded[0]=CombatTestWorld.reload(h,list.getLast());ready[0]=m[0].getAttached(SkeletonState.DATA).getLongOr("special_ready",0);
        }
            if(expired[0]!=null&&Double.isNaN(drops[0])&&expired[0].tickCount==ages[0]+1)drops[0]=expired[0].getDeltaMovement().y;
            if(control[0]!=null&&Double.isNaN(drops[1])&&control[0].tickCount==ages[1]+1)drops[1]=control[0].getDeltaMovement().y;
        });
        h.runAtTickTime(115,()->{h.assertTrue(expired[0]!=null&&expired[0].isRemoved()&&loaded[0].isRemoved()&&SkeletonState.runtime(m[0]).mushroomClouds==0,"Lifetime retires live projectile; serialized projectile never resumes");h.assertTrue(Math.abs(drops[0]+.0495)<.00001&&Math.abs(drops[1]+.0297)<.00001,"One actual air tick: source mushroom gravity .05, vanilla snowball .03 untouched");m[0]=CombatTestWorld.reload(h,m[0]);h.assertTrue(m[0].getAttached(SkeletonState.DATA).getLongOr("special_ready",0)==ready[0],"Earned stage cooldown preserved on owner serialization");m[0].getAttached(SkeletonState.DATA).putInt("version",99);m[0]=CombatTestWorld.reload(h,m[0]);});
        h.runAtTickTime(125,()->{h.assertTrue(m[0].getAttached(SkeletonState.DATA).getIntOr("version",0)==99&&SkeletonState.runtime(m[0]).mushrooms==0,"Unknown owner version preserved/inactive");proof("bogged_lifetime_reload",m[0],"lifetime=100 unknown=99 mushroom_drop="+drops[0]+" vanilla_drop="+drops[1]);clean(m[0],t,control[0]);h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=90)
    public void playerModesRejectTargetsAndRetireExistingMushrooms(GameTestHelper h){
        var m=mob(h,EntityTypes.BOGGED);m.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.STONE_SWORD));var f=SkeletonBehaviorTests.player(h);var p=f.player();p.snapTo(h.absolutePos(new BlockPos(8,2,11)),0,0);p.addEffect(new MobEffectInstance(MobEffects.BAD_OMEN,1000));m.setTarget(p);boolean[] switched={false};
        h.onEachTick(()->{if(!switched[0]&&SkeletonState.runtime(m).mushrooms==2){switched[0]=true;p.setGameMode(GameType.CREATIVE);}});
        h.runAtTickTime(60,()->{h.assertTrue(switched[0]&&m.getTarget()==null&&balls(h,m).isEmpty()&&clouds(h,m).isEmpty(),"Real in-flight creative exit");p.setGameMode(GameType.SPECTATOR);m.setTarget(p);});
        h.runAtTickTime(80,()->{h.assertTrue(m.getTarget()==null&&SkeletonState.runtime(m).mushrooms==2,"Spectator rejected without another release");proof("bogged_player_exit",m,"creative/spectator=true");clean(m);f.close();h.succeed();});
    }
    private void stray(GameTestHelper h,boolean omen){
        var m=mob(h,EntityTypes.STRAY);m.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.STONE_SWORD));var t=target(h,m,omen);double[] height={m.getY()};h.onEachTick(()->height[0]=Math.max(height[0],m.getY()));
        h.runAtTickTime(70,()->{var r=SkeletonState.runtime(m);h.assertTrue(r.flips==1&&r.snowballs==3&&height[0]>h.absolutePos(new BlockPos(8,2,8)).getY()+.5,"One actual flip releases exactly three native snowballs");h.assertTrue(!FeatureRegistry.supported(FeatureId.STRAY_OMEN_SNOW)&&!FeatureRegistry.supported(FeatureId.STRAY_SNOW_BARRAGE),"Old duplicate/custom omen IDs remain inactive");proof("stray_replace_"+omen,m,"height="+height[0]+" oldIDs=inactive");clean(m,t);h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=80) public void normalStrayUsesOneExistingThreeSnowChain(GameTestHelper h){stray(h,false);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=80) public void ominousStrayUsesSameExistingThreeSnowChain(GameTestHelper h){stray(h,true);}
    @GameTest(environment=ENV,structure=ARENA,maxTicks=340)
    public void replacedAimTracksRaisedMovingTargetAndActualArrowHits(GameTestHelper h){
        var m=mob(h,EntityTypes.SKELETON);var t=target(h,m,false);boolean[] raised={false};float[] before={0};Set<UUID> arrows=new HashSet<>();
        h.onEachTick(()->{for(var a:h.getLevel().getEntitiesOfClass(AbstractArrow.class,m.getBoundingBox().inflate(32),a->a.getOwner()==m)){arrows.add(a.getUUID());if(a.getDeltaMovement().y>.05)raised[0]=true;}});
        h.runAtTickTime(65,()->{before[0]=t.getHealth();for(int x=7;x<15;x++)for(int z=10;z<21;z++)h.setBlock(new BlockPos(x,6,z),Blocks.AIR);t.setNoGravity(true);t.snapTo(h.absolutePos(new BlockPos(11,3,16)),0,0);});
        // Keep the raised target in place without ticking or directly aiming the skeleton.
        h.onEachTick(()->{if(h.getTick()>=65)t.setDeltaMovement(Vec3.ZERO);});
        h.runAtTickTime(330,()->{proof("aim_replace",m,"arrows="+arrows.size()+" raised="+raised[0]+" health="+t.getHealth()+" before_move="+before[0]);h.assertTrue(arrows.size()>=2&&raised[0]&&t.getHealth()<before[0],"Native look control reacquires changed height/lateral target and arrows actually damage it after movement");h.assertTrue(!FeatureRegistry.supported(FeatureId.SKELETON_AIM_FIX),"Duplicate S2 aim ID remains inactive");clean(m,t);h.succeed();});
    }
    @GameTest(environment="buildupmobtweaks:r4e1c_qa",structure=ARENA,maxTicks=840)
    public void independentQaSkeletonDifferencesUseRealPhasesAndCleanup(GameTestHelper h){
        var origin=h.absolutePos(new BlockPos(14,2,14));var server=h.getLevel().getServer();QaHarness.Session[] s={null};
        h.runAtTickTime(5,()->s[0]=QaHarness.spawn(h.getLevel(),origin,QaHarness.Scene.SKELETON_DRAW));
        h.runAtTickTime(545,()->{var r=SkeletonState.runtime((AbstractSkeleton)s[0].mob);h.assertTrue(r.shots>=4&&r.drawSwings>=4&&s[0].target.getHealth()<1000,"QA real draw/fire after all four hand combinations");proof("qa_draw",(AbstractSkeleton)s[0].mob,"four_combinations=true");QaHarness.cleanup(server);});
        h.runAtTickTime(555,()->s[0]=QaHarness.spawn(h.getLevel(),origin,QaHarness.Scene.BOGGED_OMEN));
        h.runAtTickTime(675,()->{var r=SkeletonState.runtime((AbstractSkeleton)s[0].mob);h.assertTrue(r.dodges==1&&r.mushrooms==2&&r.mushroomClouds==2,"QA one spore phase, two real mushrooms and impacts");proof("qa_bogged",(AbstractSkeleton)s[0].mob,"two_colors=true");s[0].mob.addTag("buildupmobtweaks:disable_bogged_omen_mushrooms");});
        h.runAtTickTime(705,()->{h.assertTrue(SkeletonState.runtime((AbstractSkeleton)s[0].mob).mushrooms==2,"QA independent off prevents next additions");QaHarness.cleanup(server);});
        h.runAtTickTime(715,()->s[0]=QaHarness.spawn(h.getLevel(),origin,QaHarness.Scene.STRAY_FLIP));
        h.runAtTickTime(815,()->{var r=SkeletonState.runtime((AbstractSkeleton)s[0].mob);h.assertTrue(r.flips==1&&r.snowballs==3,"QA old stage retains one jump and three snowballs");proof("qa_stray",(AbstractSkeleton)s[0].mob,"oldIDs=inactive");QaHarness.cleanup(server);h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=95)
    public void staleOmenAndBabyCannotReleaseMushroomDifference(GameTestHelper h){
        var adult=mob(h,EntityTypes.BOGGED);adult.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.STONE_SWORD));var t=target(h,adult,true);
        adult.getAttached(SkeletonState.DATA).putLong("special_ready",h.getLevel().getGameTime()+35);h.runAtTickTime(20,()->t.removeEffect(MobEffects.TRIAL_OMEN));
        // Native Bogged has no baby state. This test-only subtype exposes a third-party baby flag.
        var baby=new net.minecraft.world.entity.monster.skeleton.Bogged(EntityTypes.BOGGED,h.getLevel()){@Override public boolean isBaby(){return true;}};
        baby.snapTo(h.absolutePos(new BlockPos(17,2,8)),0,0);baby.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.BOW));h.getLevel().addFreshEntity(baby);baby.getAttached(SkeletonState.DATA).putLong("special_ready",0);baby.addTag("buildupmobtweaks:disable_skeleton_weapon_switching");var bt=SkeletonBehaviorTests.target(h,17,11);bt.addEffect(new MobEffectInstance(MobEffects.BAD_OMEN,1000));baby.setTarget(bt);
        h.runAtTickTime(85,()->{h.assertTrue(SkeletonState.runtime(adult).dodges==1&&SkeletonState.runtime(adult).mushrooms==0,"Recheck omen at release, not a stale Goal.start snapshot");h.assertTrue(SkeletonState.runtime(baby).dodges==0&&SkeletonState.runtime(baby).mushrooms==0,"Baby never enters adult spore stage");proof("omen_recheck_baby",adult,"test_subtype_baby_mushrooms=0 native_baby=NOT_RUN");clean(adult,baby,t,bt);h.succeed();});
    }
    @GameTest(environment="buildupmobtweaks:r4e1c_config",structure=ARENA,maxTicks=215)
    public void realConfigMasterAndIndependentFeatureGates(GameTestHelper h){
        var cfg=BuildupMobTweaks.config();boolean master=cfg.general.enabled.get(),visual=cfg.hostile.extended.skeleton_offhand_visual.get(),mush=cfg.hostile.extended.bogged_omen_mushrooms.get();
        cfg.general.enabled.setAndUpdate(false);var m=mob(h,EntityTypes.SKELETON);var t=target(h,m,false);var bog=SkeletonBehaviorTests.skeleton(h,EntityTypes.BOGGED,18,8);bog.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.STONE_SWORD));bog.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);bog.getAttached(SkeletonState.DATA).putLong("special_ready",0);var bt=SkeletonBehaviorTests.target(h,18,11);bt.addEffect(new MobEffectInstance(MobEffects.TRIAL_OMEN,1000));bog.setTarget(bt);
        h.runAtTickTime(55,()->{try{h.assertTrue(!m.getAttachedOrCreate(SkeletonDifferences.BOW_VISUAL)&&SkeletonState.runtime(m).drawSwings==0&&SkeletonState.runtime(bog).dodges==0,"Actual master gate disables additions");cfg.hostile.extended.skeleton_offhand_visual.setAndUpdate(false);cfg.hostile.extended.bogged_omen_mushrooms.setAndUpdate(false);cfg.general.enabled.setAndUpdate(true);}catch(RuntimeException|AssertionError e){cfg.general.enabled.setAndUpdate(master);cfg.hostile.extended.skeleton_offhand_visual.setAndUpdate(visual);cfg.hostile.extended.bogged_omen_mushrooms.setAndUpdate(mush);throw e;}});
        h.runAtTickTime(205,()->{try{h.assertTrue(SkeletonState.runtime(m).shots>0&&SkeletonState.runtime(m).drawSwings==0&&SkeletonState.runtime(bog).dodges>0&&SkeletonState.runtime(bog).mushrooms==0,"Independent real config off keeps native shots/base spore phase");proof("config_gates",m,"bogged_base=true mushroom=0");clean(m,t,bog,bt);h.succeed();}finally{cfg.general.enabled.setAndUpdate(master);cfg.hostile.extended.skeleton_offhand_visual.setAndUpdate(visual);cfg.hostile.extended.bogged_omen_mushrooms.setAndUpdate(mush);}});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=95)
    public void nativeHarmingCloudDamagesEnemyAndProtectsTeamAlly(GameTestHelper h){
        var m=mob(h,EntityTypes.BOGGED);m.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.STONE_SWORD));for(int x=0;x<28;x++)for(int z=0;z<28;z++)h.setBlock(new BlockPos(x,6,z),Blocks.AIR);
        var t=target(h,m,true);var enemy=EntityTypes.VILLAGER.create(h.getLevel(),EntitySpawnReason.COMMAND);var ally=EntityTypes.VILLAGER.create(h.getLevel(),EntitySpawnReason.COMMAND);
        for(var v:List.of(enemy,ally)){v.snapTo(h.absolutePos(new BlockPos(14,2,8)),0,0);v.setNoAi(true);v.setNoGravity(true);v.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);v.setHealth(1000);h.getLevel().addFreshEntity(v);}
        var board=h.getLevel().getScoreboard();var team=board.addPlayerTeam("e1c_"+m.getUUID().toString().substring(0,8));board.addPlayerToTeam(m.getScoreboardName(),team);board.addPlayerToTeam(ally.getScoreboardName(),team);
        var undead=EntityTypes.ZOMBIE.create(h.getLevel(),EntitySpawnReason.COMMAND);undead.snapTo(h.absolutePos(new BlockPos(14,2,8)),0,0);undead.setNoAi(true);undead.setNoGravity(true);undead.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.IRON_HELMET));undead.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);undead.setHealth(900);h.getLevel().addFreshEntity(undead);board.addPlayerToTeam(undead.getScoreboardName(),team);
        actualCloudDamage.put(enemy.getUUID(),0);actualCloudDamage.put(ally.getUUID(),0);
        boolean[] positioned={false},deflected={false};float[] before={0};
        h.onEachTick(()->{
            // Isolate new HARMING clouds from the pre-existing vanilla POISON cloud: only move already-fired native projectiles.
            if(!deflected[0]&&!balls(h,m).isEmpty()){deflected[0]=true;for(var ball:balls(h,m)){ball.snapTo(h.absolutePos(new BlockPos(20,3,8)),0,0);ball.setDeltaMovement(0,-.1,0);}}
            if(!positioned[0]&&!clouds(h,m).isEmpty()){positioned[0]=true;var p=clouds(h,m).getFirst().position();before[0]=enemy.getHealth();enemy.snapTo(p,0,0);ally.snapTo(p.add(.1,0,0),0,0);undead.snapTo(p.add(0,0,.1),0,0);}});
        h.runAtTickTime(85,()->{try{proof("cloud_ally",m,"positioned="+positioned[0]+" enemy_before="+before[0]+" enemy="+enemy.getHealth()+" ally="+ally.getHealth()+" undead_ally="+undead.getHealth()+" native_cloud_damage="+actualCloudDamage.get(enemy.getUUID()));h.assertTrue(positioned[0]&&enemy.getHealth()<before[0]&&ally.getHealth()==1000&&undead.getHealth()==900&&actualCloudDamage.get(enemy.getUUID())>0&&actualCloudDamage.get(ally.getUUID())==0,"Actual native HARMING cloud damages enemy but neither damages living nor heals undead allied collateral");clean(m,t,enemy,ally,undead);h.succeed();}finally{board.removePlayerTeam(team);actualCloudDamage.remove(enemy.getUUID());actualCloudDamage.remove(ally.getUUID());}});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=85)
    public void replacingLivingTargetRetiresOldProjectilesAndCloudReloadDoesNotResume(GameTestHelper h){
        var m=mob(h,EntityTypes.BOGGED);m.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.STONE_SWORD));var t=target(h,m,true);var next=SkeletonBehaviorTests.target(h,14,14);AreaEffectCloud[] loaded={null};boolean[] replaced={false};
        h.onEachTick(()->{if(!replaced[0]&&!clouds(h,m).isEmpty()){replaced[0]=true;loaded[0]=CombatTestWorld.reload(h,clouds(h,m).getFirst());m.setTarget(next);}});
        h.runAtTickTime(75,()->{h.assertTrue(replaced[0]&&loaded[0].isRemoved()&&balls(h,m).isEmpty()&&clouds(h,m).isEmpty(),"Native cloud serialization and living target replacement retire old owned effects");h.assertTrue(SkeletonState.runtime(m).mushrooms==2&&next.getHealth()==100,"No replay at replacement target or cooldown refund");proof("living_target_cloud_reload",m,"loaded_cloud=retired");clean(m,t,next);h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=30)
    public void unknownMobStateAndUnexpectedAttachmentCarrierRemainUntouched(GameTestHelper h){
        var m=mob(h,EntityTypes.SKELETON);m.setNoAi(true);m.getAttached(SkeletonState.DATA).putInt("version",99);var d=new net.minecraft.nbt.CompoundTag();d.putInt("version",99);d.putString("future_payload","keep");m.setAttached(SkeletonDifferences.MUSHROOM,d);
        var loaded=CombatTestWorld.reload(h,m);
        h.runAtTickTime(20,()->{h.assertTrue(loaded.isAlive()&&!loaded.isRemoved()&&loaded.getAttached(SkeletonState.DATA).getIntOr("version",0)==99&&d.equals(loaded.getAttached(SkeletonDifferences.MUSHROOM)),"Unknown Mob data and a non-projectile carrier are preserved; no orphan retirement of mobs");h.assertTrue(!loaded.getAttachedOrCreate(SkeletonDifferences.VISUAL)&&!loaded.getAttachedOrCreate(SkeletonDifferences.BOW_VISUAL),"Unknown owner visual gates inactive");proof("unknown_carrier",loaded,"version=99 preserved=true");clean(loaded);h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=35)
    public void unknownProjectileAndCloudVersionsPreservePayloadAndNativeLifetime(GameTestHelper h){
        var m=mob(h,EntityTypes.BOGGED);m.setNoAi(true);var d=new net.minecraft.nbt.CompoundTag();d.putInt("version",99);d.putString("future_payload","keep");d.putLong("expires",0);
        var ball=new Snowball(h.getLevel(),m,Items.RED_MUSHROOM.getDefaultInstance());ball.setPos(m.position().add(0,200,0));ball.setAttached(SkeletonDifferences.MUSHROOM,d.copy());h.getLevel().addFreshEntity(ball);
        var cloud=new AreaEffectCloud(h.getLevel(),m.getX(),m.getY()+200,m.getZ());cloud.setOwner(m);cloud.setDuration(200);cloud.setPotionContents(new net.minecraft.world.item.alchemy.PotionContents(Potions.SWIFTNESS));cloud.setAttached(SkeletonDifferences.MUSHROOM,d.copy());h.getLevel().addFreshEntity(cloud);
        var loadedBall=CombatTestWorld.reload(h,ball);var loadedCloud=CombatTestWorld.reload(h,cloud);
        h.runAtTickTime(25,()->{h.assertTrue(!loadedBall.isRemoved()&&!loadedCloud.isRemoved()&&d.equals(loadedBall.getAttached(SkeletonDifferences.MUSHROOM))&&d.equals(loadedCloud.getAttached(SkeletonDifferences.MUSHROOM)),"Unknown skill payload preserved across native save/load; not deleted by orphan/expiry policy");h.assertTrue(!SkeletonDifferences.known(loadedBall)&&!SkeletonDifferences.known(loadedCloud)&&SkeletonState.runtime(m).mushrooms==0,"Unknown carriers are never managed or replayed by this module");proof("unknown_projectile_cloud",m,"version=99 native_fallback=true preserved=true");clean(m,loadedBall,loadedCloud);h.succeed();});
    }
}
