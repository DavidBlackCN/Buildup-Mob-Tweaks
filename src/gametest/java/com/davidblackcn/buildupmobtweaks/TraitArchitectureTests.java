package com.davidblackcn.buildupmobtweaks;

import com.davidblackcn.buildupmobtweaks.traits.core.*;
import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import com.davidblackcn.buildupmobtweaks.equipment.*;
import com.davidblackcn.buildupmobtweaks.rebuild.zombie.ZombieState;
import com.davidblackcn.buildupmobtweaks.rebuild.remaining.RemainingState;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedInt;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.*;
import java.util.*;

/** Birth/finalize, native entity serialization, conversion and server ticks; no manual Goal calls. */
public final class TraitArchitectureTests {
    static final String ENV="buildupmobtweaks:phase2_traits", ARENA="buildupmobtweaks:r2a_arena";
    static final class Settings implements AutoCloseable {
        final boolean unified=BuildupMobTweaks.config().traits.unified_selection.get();
        final boolean traits=BuildupMobTweaks.config().traits.enabled.get();
        final boolean general=BuildupMobTweaks.config().general.enabled.get();
        final Map<ValidatedInt,Integer> values=new LinkedHashMap<>();
        Settings(){
            var c=BuildupMobTweaks.config();
            for(var field:c.traits.selection.getClass().getFields())try{
                if(field.get(c.traits.selection) instanceof ValidatedInt v) values.put(v,v.get());
            }catch(IllegalAccessException e){throw new AssertionError(e);}
            c.general.enabled.accept(true);c.traits.enabled.accept(true);c.traits.unified_selection.accept(true);
        }
        public void close(){values.forEach(ValidatedInt::accept);var c=BuildupMobTweaks.config();c.general.enabled.accept(general);c.traits.enabled.accept(traits);c.traits.unified_selection.accept(unified);}
    }
    static <T extends Mob> T born(GameTestHelper h,EntityType<T> type,EntitySpawnReason reason,int x){
        var m=type.create(h.getLevel(),reason);m.snapTo(h.absolutePos(new BlockPos(x,2,8)),0,0);
        m.setPersistenceRequired();m.setNoAi(true);
        if(m instanceof Piglin p)p.setImmuneToZombification(true);
        m.finalizeSpawn(h.getLevel(),h.getLevel().getCurrentDifficultyAt(m.blockPosition()),reason,
                m instanceof Zombie?new Zombie.ZombieGroupData(false,false):null);
        h.getLevel().addFreshEntity(m);return m;
    }
    static void proof(String name,Entity entity){BuildupMobTweaks.LOGGER.info("PHASE2_TRAIT_PROOF scenario={} ticks={} core={} source={}",name,entity.tickCount,entity.getAttached(TraitStorage.DATA),entity.getAttached(SpawnStorage.DATA));}
    static void floor(GameTestHelper h){SkeletonBehaviorTests.floor(h,true);}
    static boolean owns(Mob m,FeatureId id){return TraitRuntime.has(m,id);}
    static void after(GameTestHelper h,Mob m,String name){h.runAtTickTime(20,()->{proof(name,m);m.discard();h.succeed();});}

    @GameTest(environment=ENV,structure=ARENA,maxTicks=30)
    public void registryMatchesSevenFrozenTraitsAndExcludesBaseEquipment(GameTestHelper h){
        h.assertValueEqual(TraitRegistry.definitions().size(),7,"Seven definitions only");
        for(var id:List.of(FeatureId.SKELETON_SNIPING,FeatureId.BLAZE_ORBIT,FeatureId.ZOMBIE_SHIELD_USE,FeatureId.EQUIPMENT_ASSIGNMENT,FeatureId.ENDERMAN_COMBO_ANIMATION))
            h.assertTrue(TraitRegistry.find(id).isEmpty(),"Base/equipment/visual not converted into Trait");
        h.assertTrue(TraitRegistry.find(FeatureId.ZOMBIE_DOOR_GUARD).orElseThrow().polarity()==TraitDefinition.Polarity.MIXED,"Door Mixed");
        h.assertTrue(TraitRegistry.find(FeatureId.HUSK_SAND_BURROW).orElseThrow().polarity()==TraitDefinition.Polarity.POSITIVE,"Burrow Positive");h.succeed();
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=30)
    public void storedSchemaDoesNotContainActionEquipmentOrDuplicateMetadata(GameTestHelper h){
        var d=TraitRegistry.find(FeatureId.ENDERMAN_COMBO).orElseThrow();
        var raw=TraitStorage.encode(new TraitStorage.Snapshot(TraitStorage.Mode.UNIFIED,"selected",List.of(new TraitInstance(d.id(),1,SpawnSource.NATURAL))));
        h.assertTrue(raw.keySet().equals(Set.of("version","mode","outcome","entries")),"Only selection record fields");
        h.assertTrue(raw.getCompoundOrEmpty("entries").getCompoundOrEmpty(d.id().toString()).keySet().equals(Set.of("version","source")),"Entitlement only; metadata definition remains immutable");
        h.assertTrue(TraitStorage.decode(raw).isPresent(),"Current schema round trip");h.succeed();
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=30)
    public void legacyProbabilityMappingPreservesEveryDrawAndNextRandomValue(GameTestHelper h){
        for(int[] pair:List.of(new int[]{30,10},new int[]{70,10},new int[]{0,0},new int[]{1000,1000},new int[]{0,70})){
            var entries=new ArrayList<TraitPool.Weighted<String>>();if(pair[0]>0)entries.add(new TraitPool.Weighted<>("a",pair[0]));if(pair[1]>0)entries.add(new TraitPool.Weighted<>("b",pair[1]));
            var old=RandomSource.create(31297);var now=RandomSource.create(31297);
            for(int i=0;i<10000;i++){int r=old.nextInt(Math.max(1000,pair[0]+pair[1]));String expected=r<pair[0]?"a":r<pair[0]+pair[1]?"b":"none";
                h.assertTrue(expected.equals(TraitPool.legacyEquivalent(now,entries).orElse("none")),"Same final entitlement per seeded draw");}
            h.assertTrue(old.nextLong()==now.nextLong(),"Same subsequent random stream");
        }
        for(int p:new int[]{0,70,1000}){var old=RandomSource.create(77);var now=RandomSource.create(77);for(int i=0;i<1000;i++)h.assertTrue((old.nextInt(1000)<p)==TraitPool.legacySingle(now,p),"Single exact mapping");}
        h.succeed();
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=30)
    public void totalProbabilityAndWeightsHaveIndependentDistributions(GameTestHelper h){
        var pool=new TraitPool<String>(400,List.of(new TraitPool.Weighted<>("a",3),new TraitPool.Weighted<>("b",1)));
        int a=0,b=0;var random=RandomSource.create(301011);for(int i=0;i<20000;i++){var r=pool.sample(random);if(r.isPresent()){if(r.get().equals("a"))a++;else b++;}}
        h.assertTrue(a>5600&&a<6400&&b>1800&&b<2200,"P=40%, conditional 3:1 in a test-only pool");
        h.assertTrue(pool.atWeight(0).equals("a")&&pool.atWeight(2).equals("a")&&pool.atWeight(3).equals("b"),"Exact weight boundaries");
        h.assertTrue(new TraitPool<String>(0,pool.entries()).sample(random).isEmpty(),"Zero P");
        h.assertTrue(new TraitPool<String>(1000,List.of()).sample(random).isEmpty(),"Empty pool");h.succeed();
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=30)
    public void nativeNaturalAndEggBirthsReceiveOneCombo(GameTestHelper h){floor(h);var mobs=new ArrayList<Mob>();
        try(var settings=new Settings()){BuildupMobTweaks.config().traits.selection.enderman_chance_per_mille.accept(1000);
            for(var reason:List.of(EntitySpawnReason.NATURAL,EntitySpawnReason.SPAWN_ITEM_USE,EntitySpawnReason.DISPENSER)){
                var m=born(h,EntityTypes.ENDERMAN,reason,6+mobs.size()*5);mobs.add(m);
                h.assertTrue(owns(m,FeatureId.ENDERMAN_COMBO)&&TraitStorage.read(m).orElseThrow().entries().size()==1,"Actual finalization grants exactly one");
                h.assertTrue(SpawnStorage.read(m).orElseThrow().source()==SpawnSource.classify(reason,false),"Recorded actual source");
            }}
        h.runAtTickTime(20,()->{mobs.forEach(m->{proof("natural_egg",m);m.discard();});h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=30)
    public void nativeSpawnerStructureEventAndCommandBirthsSkip(GameTestHelper h){floor(h);var mobs=new ArrayList<Mob>();
        try(var settings=new Settings()){BuildupMobTweaks.config().traits.selection.wither_chance_per_mille.accept(1000);
            for(var reason:List.of(EntitySpawnReason.SPAWNER,EntitySpawnReason.TRIAL_SPAWNER,EntitySpawnReason.STRUCTURE,EntitySpawnReason.EVENT,EntitySpawnReason.COMMAND)){
                var m=born(h,EntityTypes.WITHER_SKELETON,reason,8);mobs.add(m);
                h.assertTrue(TraitStorage.read(m).orElseThrow().entries().isEmpty()&&TraitStorage.read(m).orElseThrow().outcome().equals("source_skipped"),"Default skipped source");
                h.assertTrue(!owns(m,FeatureId.WITHER_SKELETON_SKULL),"No second local roll");
            }}
        h.runAtTickTime(20,()->{mobs.forEach(m->{proof("skip_sources",m);m.discard();});h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=30)
    public void raidClassificationRequiresRaidContextAndExternalIsNeverInferred(GameTestHelper h){
        h.assertTrue(SpawnSource.classify(EntitySpawnReason.EVENT,true)==SpawnSource.RAID,"Native event with raid membership");
        h.assertTrue(SpawnSource.classify(EntitySpawnReason.EVENT,false)==SpawnSource.UNKNOWN,"Other events remain unknown");
        for(var r:List.of(EntitySpawnReason.COMMAND,EntitySpawnReason.JOCKEY,EntitySpawnReason.CHUNK_GENERATION,EntitySpawnReason.CONVERSION,EntitySpawnReason.LOAD,EntitySpawnReason.DIMENSION_TRAVEL))
            h.assertTrue(SpawnSource.classify(r,false)==SpawnSource.UNKNOWN,"Ambiguous or continuing source is not inferred");
        h.assertTrue(!SpawnSource.EXTERNAL.automaticallySelects()&&!SpawnSource.RAID.automaticallySelects(),"External/raid never auto roll");h.succeed();
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=45)
    public void missedResultAndRepeatedFinalizationReloadNeverReroll(GameTestHelper h){floor(h);Mob[] ref={null};
        try(var settings=new Settings()){BuildupMobTweaks.config().traits.selection.enderman_chance_per_mille.accept(0);ref[0]=born(h,EntityTypes.ENDERMAN,EntitySpawnReason.NATURAL,8);
            var raw=ref[0].getAttached(TraitStorage.DATA).copy();BuildupMobTweaks.config().traits.selection.enderman_chance_per_mille.accept(1000);
            ref[0].finalizeSpawn(h.getLevel(),h.getLevel().getCurrentDifficultyAt(ref[0].blockPosition()),EntitySpawnReason.NATURAL,null);
            h.assertTrue(raw.equals(ref[0].getAttached(TraitStorage.DATA)),"Repeated finalization stable");ref[0]=CombatTestWorld.reload(h,ref[0]);
            h.assertTrue(raw.equals(ref[0].getAttached(TraitStorage.DATA))&&!owns(ref[0],FeatureId.ENDERMAN_COMBO),"Reload keeps missed result");}
        after(h,ref[0],"missed_repeated_reload");
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=45)
    public void disabledAtBirthAndEmptyPoolAreFinal(GameTestHelper h){floor(h);var mobs=new ArrayList<Mob>();
        try(var settings=new Settings()){var c=BuildupMobTweaks.config();c.traits.enabled.accept(false);var disabled=born(h,EntityTypes.ENDERMAN,EntitySpawnReason.NATURAL,6);mobs.add(disabled);
            c.traits.enabled.accept(true);c.traits.selection.enderman_chance_per_mille.accept(1000);h.assertTrue(!owns(disabled,FeatureId.ENDERMAN_COMBO)&&TraitStorage.read(disabled).orElseThrow().outcome().equals("disabled"),"No birth backfill");
            c.traits.selection.combo_weight.accept(0);var empty=born(h,EntityTypes.ENDERMAN,EntitySpawnReason.NATURAL,12);mobs.add(empty);c.traits.selection.combo_weight.accept(1);
            h.assertTrue(!owns(empty,FeatureId.ENDERMAN_COMBO)&&TraitStorage.read(empty).orElseThrow().outcome().equals("empty_pool"),"No pool reload backfill");}
        h.runAtTickTime(20,()->{mobs.forEach(Entity::discard);h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=30)
    public void scenePoolsHaveNoInventedDefaultProbability(GameTestHelper h){floor(h);var mobs=new ArrayList<Mob>();
        try(var settings=new Settings()){var c=BuildupMobTweaks.config().traits.selection;c.husk_chance_per_mille.accept(-1);c.bogged_chance_per_mille.accept(-1);
            mobs.add(born(h,EntityTypes.HUSK,EntitySpawnReason.NATURAL,6));mobs.add(born(h,EntityTypes.BOGGED,EntitySpawnReason.NATURAL,12));
            for(var m:mobs)h.assertTrue(TraitStorage.read(m).orElseThrow().outcome().equals("unconfigured")&&TraitStorage.read(m).orElseThrow().entries().isEmpty(),"Explicitly unconfigured, not 50% or unused old chance");}
        h.runAtTickTime(20,()->{mobs.forEach(Entity::discard);h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=30)
    public void explicitlyConfiguredSceneTraitsArePersistentQualifications(GameTestHelper h){floor(h);var mobs=new ArrayList<Mob>();
        try(var settings=new Settings()){var c=BuildupMobTweaks.config().traits.selection;c.husk_chance_per_mille.accept(1000);c.burrow_weight.accept(1);c.door_weight.accept(0);c.bogged_chance_per_mille.accept(1000);c.bogged_weight.accept(1);
            var husk=born(h,EntityTypes.HUSK,EntitySpawnReason.NATURAL,6);var bogged=born(h,EntityTypes.BOGGED,EntitySpawnReason.NATURAL,12);mobs.add(husk);mobs.add(bogged);
            h.assertTrue(owns(husk,FeatureId.HUSK_SAND_BURROW)&&owns(bogged,FeatureId.BOGGED_SPORE_RETREAT),"Explicit test pool qualifies each scene trait");
            h.assertTrue(!owns(husk,FeatureId.ZOMBIE_DOOR_GUARD),"Husk has one combat qualification");}
        h.runAtTickTime(20,()->{mobs.forEach(m->{h.assertTrue(TraitStorage.read(m).orElseThrow().entries().size()==1,"Config restoration does not change an individual's mode");proof("explicit_scene",m);m.discard();});h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=30)
    public void firstBatchFireballAndSkullUseCoreAndDoNotCreateUnmigratedTraits(GameTestHelper h){floor(h);var mobs=new ArrayList<Mob>();
        try(var settings=new Settings()){var c=BuildupMobTweaks.config().traits.selection;c.evoker_chance_per_mille.accept(1000);c.wither_chance_per_mille.accept(1000);
            var evoker=born(h,EntityTypes.EVOKER,EntitySpawnReason.SPAWN_ITEM_USE,6);var skull=born(h,EntityTypes.WITHER_SKELETON,EntitySpawnReason.SPAWN_ITEM_USE,12);mobs.add(evoker);mobs.add(skull);
            h.assertTrue(owns(evoker,FeatureId.EVOKER_FIREBALL)&&owns(skull,FeatureId.WITHER_SKELETON_SKULL),"Core qualification selects once");
            h.assertTrue(!TraitRuntime.permits(evoker,FeatureId.EVOKER_TOTEM)&&!TraitRuntime.permits(skull,FeatureId.SKELETON_HORSE_CHARGE),"Unmigrated candidates do not add a second main trait in opt-in mode");
            h.assertTrue(TraitRuntime.permits(skull,FeatureId.WITHER_SKELETON_BOW),"Equipment gate independent");}
        h.runAtTickTime(20,()->{mobs.forEach(Entity::discard);h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=45)
    public void equipmentReplacementAndFeatureDisableKeepDodgeQualification(GameTestHelper h){floor(h);Piglin[] ref={null};
        try(var settings=new Settings()){BuildupMobTweaks.config().traits.selection.piglin_chance_per_mille.accept(1000);
            for(int i=0;i<30;i++){var p=born(h,EntityTypes.PIGLIN,EntitySpawnReason.SPAWN_ITEM_USE,8);if(!p.isBaby()){ref[0]=p;break;}p.discard();}
            h.assertTrue(ref[0]!=null&&owns(ref[0],FeatureId.PIGLIN_ITEM_DODGE),"Adult piglin selected");}
        var raw=ref[0].getAttached(TraitStorage.DATA).copy();ref[0].setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.DIAMOND_SWORD));ref[0].addTag("buildupmobtweaks:disable_piglin_item_dodge");
        h.runAtTickTime(10,()->{h.assertTrue(raw.equals(ref[0].getAttached(TraitStorage.DATA))&&owns(ref[0],FeatureId.PIGLIN_ITEM_DODGE),"Disable/equipment only affect execution");ref[0].removeTag("buildupmobtweaks:disable_piglin_item_dodge");});
        h.runAtTickTime(25,()->{h.assertTrue(raw.equals(ref[0].getAttached(TraitStorage.DATA))&&ref[0].getMainHandItem().is(Items.DIAMOND_SWORD),"Reenable does not refill crossbow or reroll");ref[0].discard();h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=45)
    public void modeSwitchOnlyAffectsNewIndividuals(GameTestHelper h){floor(h);Mob[] pair=new Mob[2];
        try(var settings=new Settings()){var c=BuildupMobTweaks.config();c.traits.unified_selection.accept(false);pair[0]=born(h,EntityTypes.HUSK,EntitySpawnReason.SPAWN_ITEM_USE,6);h.assertTrue(owns(pair[0],FeatureId.HUSK_SAND_BURROW),"Legacy scene behavior retained");
            c.traits.unified_selection.accept(true);c.traits.selection.husk_chance_per_mille.accept(0);pair[1]=born(h,EntityTypes.HUSK,EntitySpawnReason.SPAWN_ITEM_USE,12);
            h.assertTrue(owns(pair[0],FeatureId.HUSK_SAND_BURROW)&&!owns(pair[1],FeatureId.HUSK_SAND_BURROW),"No reinterpretation of prior mode");
            pair[0]=CombatTestWorld.reload(h,pair[0]);h.assertTrue(owns(pair[0],FeatureId.HUSK_SAND_BURROW),"Saved compatibility mode survives restart-like reload");}
        h.runAtTickTime(25,()->{Arrays.stream(pair).forEach(Entity::discard);h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=45)
    public void unknownCoreAndSourceVersionsArePreservedAndDoNotRoll(GameTestHelper h){floor(h);var m=EntityTypes.ENDERMAN.create(h.getLevel(),EntitySpawnReason.NATURAL);m.snapTo(h.absolutePos(new BlockPos(8,2,8)),0,0);m.setNoAi(true);m.setPersistenceRequired();
        var core=new CompoundTag();core.putInt("version",99);core.putString("opaque","future core");var source=new CompoundTag();source.putInt("version",99);source.putString("opaque","future source");m.setAttached(TraitStorage.DATA,core);m.setAttached(SpawnStorage.DATA,source);
        try(var settings=new Settings()){BuildupMobTweaks.config().traits.selection.enderman_chance_per_mille.accept(1000);m.finalizeSpawn(h.getLevel(),h.getLevel().getCurrentDifficultyAt(m.blockPosition()),EntitySpawnReason.NATURAL,null);h.getLevel().addFreshEntity(m);
            h.assertTrue(!owns(m,FeatureId.ENDERMAN_COMBO),"Unknown record does not become empty/rerolled");}
        var loaded=CombatTestWorld.reload(h,m);h.assertTrue(core.equals(loaded.getAttached(TraitStorage.DATA))&&source.equals(loaded.getAttached(SpawnStorage.DATA)),"Opaque NBT preserved exactly");after(h,loaded,"future_data");
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=30)
    public void malformedUnknownIdsVersionsAndMultipleNaturalEntriesPause(GameTestHelper h){
        var d=TraitRegistry.find(FeatureId.ENDERMAN_COMBO).orElseThrow();var raw=TraitStorage.encode(new TraitStorage.Snapshot(TraitStorage.Mode.UNIFIED,"selected",List.of(new TraitInstance(d.id(),1,SpawnSource.NATURAL))));
        var unknown=raw.copy();unknown.getCompoundOrEmpty("entries").put("other:future",new CompoundTag());h.assertTrue(TraitStorage.decode(unknown).isEmpty(),"Unknown ID pauses complete record");
        var version=raw.copy();version.getCompoundOrEmpty("entries").getCompoundOrEmpty(d.id().toString()).putInt("version",99);h.assertTrue(TraitStorage.decode(version).isEmpty(),"Unknown definition version");
        var multiple=raw.copy();multiple.getCompoundOrEmpty("entries").put(FeatureId.PIGLIN_ITEM_DODGE.id().toString(),raw.getCompoundOrEmpty("entries").getCompoundOrEmpty(d.id().toString()).copy());h.assertTrue(TraitStorage.decode(multiple).isEmpty(),"Two unified qualifications rejected");
        var malformed=raw.copy();malformed.putString("entries","wrong type");h.assertTrue(TraitStorage.decode(malformed).isEmpty(),"Malformed record pauses");h.succeed();
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=45)
    public void conversionPreservesRawQualifierAndSuspendsInapplicableTrait(GameTestHelper h){floor(h);Mob from;
        try(var settings=new Settings()){BuildupMobTweaks.config().traits.selection.enderman_chance_per_mille.accept(1000);from=born(h,EntityTypes.ENDERMAN,EntitySpawnReason.NATURAL,8);}
        var core=from.getAttached(TraitStorage.DATA).copy();var source=from.getAttached(SpawnStorage.DATA).copy();
        var to=from.convertTo(EntityTypes.ZOMBIE,ConversionParams.single(from,true,true),m->{m.setPersistenceRequired();m.setNoAi(true);});
        h.assertTrue(core.equals(to.getAttached(TraitStorage.DATA))&&source.equals(to.getAttached(SpawnStorage.DATA)),"Raw source/qualification transferred once");
        h.assertTrue(!TraitStorage.has(to,TraitRegistry.find(FeatureId.ENDERMAN_COMBO).orElseThrow()),"Inapplicable target retains paused entitlement, not executing it");after(h,to,"native_conversion");
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=45)
    public void legacyDoorDurabilityAndMultipleKnownAbilitiesSurviveCompatibilityReload(GameTestHelper h){floor(h);Zombie[] ref={null};
        try(var settings=new Settings()){BuildupMobTweaks.config().traits.unified_selection.accept(false);ref[0]=born(h,EntityTypes.HUSK,EntitySpawnReason.SPAWN_ITEM_USE,8);var d=ref[0].getAttached(ZombieState.DATA);d.putString("birth","door");d.putFloat("door_health",17.5f);d.putString("material","minecraft:oak_door");
            h.assertTrue(owns(ref[0],FeatureId.ZOMBIE_DOOR_GUARD)&&owns(ref[0],FeatureId.HUSK_SAND_BURROW),"Legacy combination preserved with explicit compatibility provenance");ref[0]=CombatTestWorld.reload(h,ref[0]);}
        h.runAtTickTime(25,()->{h.assertTrue(ref[0].getAttached(ZombieState.DATA).getFloatOr("door_health",0)==17.5f&&TraitStorage.read(ref[0]).orElseThrow().entries().size()==2,"No refill or deletion of legacy combination");proof("legacy_multitrait",ref[0]);ref[0].discard();h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=45)
    public void equipmentAssignmentAndReadOnlyTraitQueryNeverGrantTrait(GameTestHelper h){floor(h);var cow=born(h,EntityTypes.COW,EntitySpawnReason.SPAWN_ITEM_USE,8);
        h.assertTrue(!cow.hasAttached(TraitStorage.DATA),"Unrelated species has no core record");
        EquipmentCapabilities.identify(new ItemStack(Items.CROSSBOW));
        h.getLevel().getServer().getCommands().performPrefixedCommand(h.getLevel().getServer().createCommandSourceStack(),"buildupmobtweaks traits "+cow.getUUID());
        h.assertTrue(!cow.hasAttached(TraitStorage.DATA),"Query is read-only, no selection or backfill");
        Mob zombie;try(var settings=new Settings()){zombie=born(h,EntityTypes.ZOMBIE,EntitySpawnReason.SPAWN_ITEM_USE,12);}
        var core=zombie.getAttached(TraitStorage.DATA).copy();var sword=new ItemStack(Items.DIAMOND_SWORD);zombie.setItemSlot(EquipmentSlot.MAINHAND,sword);
        h.runAtTickTime(25,()->{h.assertTrue(core.equals(zombie.getAttached(TraitStorage.DATA))&&zombie.getMainHandItem()==sword,"Equipment independent of Trait state");zombie.discard();cow.discard();h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=45)
    public void newUnknownLegacyAttachmentDoesNotProduceBackfilledEntitlement(GameTestHelper h){floor(h);var m=EntityTypes.HUSK.create(h.getLevel(),EntitySpawnReason.COMMAND);m.snapTo(h.absolutePos(new BlockPos(8,2,8)),0,0);m.setNoAi(true);m.setPersistenceRequired();
        var raw=new CompoundTag();raw.putInt("version",99);raw.putString("birth","door");raw.putFloat("door_health",9);m.setAttached(ZombieState.DATA,raw);h.getLevel().addFreshEntity(m);
        h.assertTrue(!owns(m,FeatureId.ZOMBIE_DOOR_GUARD)&&!owns(m,FeatureId.HUSK_SAND_BURROW),"Unrecognized legacy format has no inferred qualification");
        var loaded=CombatTestWorld.reload(h,m);h.assertTrue(raw.equals(loaded.getAttached(ZombieState.DATA)),"Unknown legacy attachment unchanged");after(h,loaded,"unknown_legacy");
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=40)
    public void naturalDoorBurrowPoolUsesOneMainSlotAndKeepsDoorDamage(GameTestHelper h){floor(h);
        var server=h.getLevel().getServer();var previous=h.getLevel().getDifficulty();var pos=h.absolutePos(new BlockPos(8,2,8));var chunk=h.getLevel().getChunkAt(pos);long oldAge=chunk.getInhabitedTime();Zombie[] door={null};
        try(var settings=new Settings()){
            server.setDifficulty(net.minecraft.world.Difficulty.HARD,true);chunk.setInhabitedTime(3600000);
            h.assertTrue(h.getLevel().getCurrentDifficultyAt(pos).getEffectiveDifficulty()>2.5,"Actual native local difficulty satisfies door condition");
            var c=BuildupMobTweaks.config().traits.selection;c.husk_chance_per_mille.accept(1000);c.door_weight.accept(1);c.burrow_weight.accept(1);
            int doors=0,burrows=0;for(int i=0;i<160;i++){
                var m=born(h,EntityTypes.HUSK,EntitySpawnReason.NATURAL,8);var state=TraitStorage.read(m).orElseThrow();h.assertTrue(state.entries().size()==1,"Exactly one natural main qualification");
                if(owns(m,FeatureId.ZOMBIE_DOOR_GUARD)){doors++;if(door[0]==null){door[0]=m;m.getAttached(ZombieState.DATA).putFloat("door_health",11.5f);}else m.discard();}
                else{h.assertTrue(owns(m,FeatureId.HUSK_SAND_BURROW),"Other member is burrow");burrows++;m.discard();}}
            h.assertTrue(doors>30&&burrows>30,"Both configured candidates encountered, not a frozen release balance");
        }finally{server.setDifficulty(previous,true);chunk.setInhabitedTime(oldAge);}
        h.runAtTickTime(25,()->{h.assertTrue(door[0]!=null&&door[0].getAttached(ZombieState.DATA).getFloatOr("door_health",0)==11.5f,"No durability refill from core or mode restoration");proof("single_natural_main",door[0]);door[0].discard();h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=185)
    public void optInBurrowRunsNativeGoalAndReloadRestoresPhysics(GameTestHelper h){ZombieBehaviorTests.sand(h,3);Zombie[] ref={null};
        try(var settings=new Settings()){var c=BuildupMobTweaks.config().traits.selection;c.husk_chance_per_mille.accept(1000);c.door_weight.accept(0);c.burrow_weight.accept(1);ref[0]=born(h,EntityTypes.HUSK,EntitySpawnReason.NATURAL,8);}
        ref[0].snapTo(h.absolutePos(new BlockPos(8,4,6)),0,0);ref[0].setNoAi(false);var target=ZombieBehaviorTests.target(h,22,4);ref[0].setTarget(target);boolean[] reloaded={false};
        h.onEachTick(()->{if(!reloaded[0]&&ref[0].getAttached(ZombieState.DATA).getBooleanOr("burrow_active",false)){
            var core=ref[0].getAttached(TraitStorage.DATA).copy();long ready=ref[0].getAttached(ZombieState.DATA).getLongOr("burrow_ready",0);ref[0]=CombatTestWorld.reload(h,ref[0]);ref[0].setNoAi(true);
            h.assertTrue(core.equals(ref[0].getAttached(TraitStorage.DATA))&&ready==ref[0].getAttached(ZombieState.DATA).getLongOr("burrow_ready",0),"Real mid-action serialization preserves core and cooldown");reloaded[0]=true;}});
        h.runAtTickTime(160,()->{h.assertTrue(reloaded[0]&&!ref[0].noPhysics&&!ref[0].isNoGravity()&&!ref[0].getAttached(ZombieState.DATA).getBooleanOr("burrow_active",true),"Selected native Goal ran, interrupted reload safely restores collision/gravity");proof("unified_burrow_reload",ref[0]);ref[0].discard();target.discard();h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=210)
    public void optInComboRunsNativeGoalAndDisablePreservesQualification(GameTestHelper h){floor(h);net.minecraft.world.entity.monster.Enderman m;
        try(var settings=new Settings()){BuildupMobTweaks.config().traits.selection.enderman_chance_per_mille.accept(1000);m=born(h,EntityTypes.ENDERMAN,EntitySpawnReason.NATURAL,8);}
        m.setNoAi(false);var fixture=SkeletonBehaviorTests.player(h);var target=fixture.player();target.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(1000);target.setHealth(1000);target.snapTo(h.absolutePos(new BlockPos(8,2,13)),0,0);m.setTarget(target);var raw=m.getAttached(TraitStorage.DATA).copy();long[] ready={0};
        h.runAtTickTime(140,()->{var runtime=RemainingState.runtime(m);h.assertTrue(runtime.combos>0&&runtime.teleports>0&&runtime.attacks>0&&target.getHealth()<1000,"Native scheduler performs selected combo and real melee damage");ready[0]=m.getAttached(RemainingState.DATA).getLongOr("combo_ready",0);m.addTag("buildupmobtweaks:disable_enderman_combo");});
        h.runAtTickTime(155,()->{h.assertTrue(raw.equals(m.getAttached(TraitStorage.DATA))&&m.getAttached(RemainingState.DATA).getLongOr("combo_ready",0)==ready[0],"Disable retains qualification and earned cooldown");m.removeTag("buildupmobtweaks:disable_enderman_combo");});
        h.runAtTickTime(180,()->{h.assertTrue(raw.equals(m.getAttached(TraitStorage.DATA)),"Reenable does not reroll");proof("unified_native_combo",m);m.discard();fixture.close();h.succeed();});
    }
    @GameTest(environment=ENV,structure=ARENA,maxTicks=30)
    public void mismatchedSourceAndQualificationModesPauseWithoutRewriting(GameTestHelper h){floor(h);Mob m;
        try(var settings=new Settings()){BuildupMobTweaks.config().traits.selection.enderman_chance_per_mille.accept(1000);m=born(h,EntityTypes.ENDERMAN,EntitySpawnReason.NATURAL,8);}
        var source=m.getAttached(SpawnStorage.DATA).copy();source.putString("mode",TraitStorage.Mode.COMPATIBILITY.name());m.setAttached(SpawnStorage.DATA,source);var core=m.getAttached(TraitStorage.DATA).copy();
        h.assertTrue(!owns(m,FeatureId.ENDERMAN_COMBO)&&!TraitRuntime.permits(m,FeatureId.ENDERMAN_COMBO)&&TraitRuntime.readRecord(m).isEmpty(),"Inconsistent record pauses execution");
        h.assertTrue(core.equals(m.getAttached(TraitStorage.DATA))&&source.equals(m.getAttached(SpawnStorage.DATA)),"Both original records retained");after(h,m,"inconsistent_modes");
    }
    @GameTest(environment="buildupmobtweaks:phase2_dispenser",structure=ARENA,maxTicks=55)
    public void realRedstoneDispenserEggUsesEggPoolAndOneQualification(GameTestHelper h){floor(h);var settings=new Settings();BuildupMobTweaks.config().traits.selection.enderman_chance_per_mille.accept(1000);
        var pos=new BlockPos(8,2,6);h.setBlock(pos,net.minecraft.world.level.block.Blocks.DISPENSER.defaultBlockState().setValue(net.minecraft.world.level.block.DispenserBlock.FACING,net.minecraft.core.Direction.SOUTH));
        var block=(net.minecraft.world.level.block.entity.DispenserBlockEntity)h.getLevel().getBlockEntity(h.absolutePos(pos));block.setItem(0,new ItemStack(Items.ENDERMAN_SPAWN_EGG));
        h.setBlock(pos.west(),net.minecraft.world.level.block.Blocks.REDSTONE_BLOCK);
        h.runAtTickTime(25,()->{try{
            var spawned=h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.monster.Enderman.class,new net.minecraft.world.phys.AABB(h.absolutePos(pos)).inflate(16));
            h.assertTrue(spawned.size()==1&&block.getItem(0).isEmpty(),"Native redstone dispenser consumed one real egg and created one mob");var m=spawned.getFirst();
            h.assertTrue(m.spawnReason()==EntitySpawnReason.DISPENSER&&SpawnStorage.read(m).orElseThrow().source()==SpawnSource.SPAWN_EGG,"Actual dispenser reason mapped to egg source");
            h.assertTrue(owns(m,FeatureId.ENDERMAN_COMBO)&&TraitStorage.read(m).orElseThrow().entries().size()==1,"Actual egg path receives one core qualification");proof("real_dispenser_egg",m);m.discard();h.succeed();
        }finally{settings.close();}});
    }
}
