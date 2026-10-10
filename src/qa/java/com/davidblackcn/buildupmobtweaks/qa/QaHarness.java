package com.davidblackcn.buildupmobtweaks.qa;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import com.davidblackcn.buildupmobtweaks.feature.*;
import com.davidblackcn.buildupmobtweaks.equipment.EquipmentService;
import com.davidblackcn.buildupmobtweaks.rebuild.profession.*;
import com.davidblackcn.buildupmobtweaks.rebuild.illusion.*;
import net.minecraft.world.entity.monster.zombie.ZombieVillager;
import net.minecraft.world.entity.monster.zombie.Drowned;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import com.davidblackcn.buildupmobtweaks.rebuild.pillager.PillagerBehavior;
import com.davidblackcn.buildupmobtweaks.rebuild.skeleton.*;
import com.davidblackcn.buildupmobtweaks.rebuild.zombie.*;
import com.davidblackcn.buildupmobtweaks.rebuild.raid.*;
import com.davidblackcn.buildupmobtweaks.rebuild.remaining.*;
import com.davidblackcn.buildupmobtweaks.rebuild.piglin.*;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.minecraft.commands.*;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.entity.monster.illager.*;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import java.util.*;

/** Development-only fixtures. Never calls a production Goal/tick or changes global config. */
public final class QaHarness implements ModInitializer {
    // 26.3 selector tag= uses readUnquotedString; a colon would not be selector-safe.
    public static final String TAG="buildupmobtweaks_qa_fixture";
    private static final Map<MinecraftServer,Session> SESSIONS=new IdentityHashMap<>();
    public enum Scene {
        PILLAGER("pillager",FeatureId.PILLAGER_WEAPON_SWITCH),
        SKELETON("skeleton",FeatureId.SKELETON_WEAPON_SWITCHING),
        ENDERMAN("enderman",FeatureId.ENDERMAN_COMBO),
        EVOKER("evoker",FeatureId.EVOKER_FIREBALL),
        DOOR("door",FeatureId.ZOMBIE_DOOR_GUARD),
        PARCHED("parched",FeatureId.PARCHED_DODGE),
        WITHER_BOW("wither_bow",FeatureId.WITHER_SKELETON_BOW),
        WITHER_SKULL("wither_skull",FeatureId.WITHER_SKELETON_SKULL),
        WITHER_HOMING("wither_homing",FeatureId.WITHER_SKELETON_HOMING),
        PIGLIN("piglin",FeatureId.PIGLIN_CROSSBOW_COMPATIBILITY),
        PIGLIN_DODGE("piglin_dodge",FeatureId.PIGLIN_ITEM_DODGE),
        PIGLIN_ANGER("piglin_anger",FeatureId.ZOMBIFIED_PIGLIN_ANGER_FIX),
        SKELETON_DRAW("skeleton_draw",FeatureId.SKELETON_OFFHAND_VISUAL),
        BOGGED_OMEN("bogged_omen",FeatureId.BOGGED_OMEN_MUSHROOMS),
        STRAY_FLIP("stray_flip",FeatureId.STRAY_JUMP_SHOT),
        WITHER_BIRTH("wither_birth",FeatureId.WITHER_SKELETON_BOW),
        ZOMBIFIED_BIRTH("zombified_birth",FeatureId.ZOMBIFIED_PIGLIN_SPAWN_CROSSBOW),
        ZOMBIFIED_CROSSBOW("zombified_crossbow",FeatureId.ZOMBIFIED_PIGLIN_CROSSBOW),
        FLETCHER("fletcher",FeatureId.ZOMBIE_FLETCHER_CROSSBOW),
        FISHERMAN("fisherman",FeatureId.ZOMBIE_FISHERMAN_ROD),
        CLERIC("cleric",FeatureId.ZOMBIE_CLERIC_POTIONS),
        DROWNED_FISHING("drowned_fishing",FeatureId.DROWNED_FISHING_PULL),
        ILLUSION("illusion",FeatureId.ILLUSIONER_SWAP),
        ILLUSION_ARROWS("illusion_arrows",FeatureId.ILLUSIONER_CLONE_ARROWS),
        VINDICATOR("vindicator",FeatureId.VINDICATOR_SUPPORT);
        public final String id; public final FeatureId feature;
        Scene(String id,FeatureId feature){this.id=id;this.feature=feature;}
        public static Scene parse(String name){return Arrays.stream(values()).filter(s->s.id.equals(name)).findFirst().orElseThrow(()->new IllegalArgumentException("Scene: "+Arrays.stream(values()).map(s->s.id).toList()));}
        boolean matches(Mob m){return switch(this){case ILLUSION,ILLUSION_ARROWS->m.getType()==EntityTypes.ILLUSIONER;case VINDICATOR->m.getType()==EntityTypes.VINDICATOR;case PILLAGER->m.getType()==EntityTypes.PILLAGER;case SKELETON,SKELETON_DRAW->m.getType()==EntityTypes.SKELETON;case BOGGED_OMEN->m.getType()==EntityTypes.BOGGED;case STRAY_FLIP->m.getType()==EntityTypes.STRAY;case ENDERMAN->m.getType()==EntityTypes.ENDERMAN;case EVOKER->m.getType()==EntityTypes.EVOKER;case DOOR->m.getType()==EntityTypes.ZOMBIE;case PARCHED->m.getType()==EntityTypes.PARCHED;case WITHER_BIRTH,WITHER_BOW,WITHER_SKULL,WITHER_HOMING->m.getType()==EntityTypes.WITHER_SKELETON;case PIGLIN,PIGLIN_DODGE->m.getType()==EntityTypes.PIGLIN;case PIGLIN_ANGER,ZOMBIFIED_BIRTH,ZOMBIFIED_CROSSBOW->m.getType()==EntityTypes.ZOMBIFIED_PIGLIN;case DROWNED_FISHING->m.getType()==EntityTypes.DROWNED;case FLETCHER,FISHERMAN,CLERIC->m instanceof ZombieVillager z&&ProfessionBehavior.profession(z,this==FLETCHER?VillagerProfession.FLETCHER:this==FISHERMAN?VillagerProfession.FISHERMAN:VillagerProfession.CLERIC);};}
    }
    @Override public void onInitialize(){
        CommandRegistrationCallback.EVENT.register((d,a,e)->{
            var root=Commands.literal("buildupqa").requires(Commands.hasPermission(Commands.LEVEL_OWNERS));
            root.then(Commands.literal("spawn").then(Commands.argument("scene",StringArgumentType.word()).suggests((c,b)->{for(var s:Scene.values())b.suggest(s.id);return b.buildFuture();}).executes(c->attempt(c.getSource(),()->{
                var session=spawn(c.getSource().getLevel(),BlockPos.containing(c.getSource().getPosition()).offset(0,0,8),Scene.parse(StringArgumentType.getString(c,"scene")));
                reply(c.getSource(),"QA uuid="+session.mob.getUUID()+" target="+session.target.getUUID()+"; inspect/check/off/cleanup; auto-cleanup after 1200 ticks");return 1;}))));
            root.then(Commands.literal("inspect").then(Commands.argument("entity",EntityArgument.entity()).executes(c->{var ent=EntityArgument.getEntity(c,"entity");if(!(ent instanceof Mob m))return 0;reply(c.getSource(),inspect(m));return 1;})));
            root.then(Commands.literal("force").then(Commands.argument("scene",StringArgumentType.word()).then(Commands.argument("entity",EntityArgument.entity()).executes(c->attempt(c.getSource(),()->{
                if(!(EntityArgument.getEntity(c,"entity") instanceof Mob m))throw new IllegalArgumentException("Expected Mob");select(m,Scene.parse(StringArgumentType.getString(c,"scene")));reply(c.getSource(),"QA session selected; existing equipment and earned cooldowns preserved. "+inspect(m));return 1;})))));
            root.then(Commands.literal("target").then(Commands.argument("entity",EntityArgument.entity()).executes(c->attempt(c.getSource(),()->{
                var s=require(c.getSource().getServer());if(s.scene==Scene.VINDICATOR)throw new IllegalArgumentException("Support target comes from an allied caster being hurt; no direct target assignment");if(s.scene==Scene.CLERIC)throw new IllegalArgumentException("Cleric Goal selects nearby injured undead; native hostile target is independent");if(!(EntityArgument.getEntity(c,"entity") instanceof LivingEntity t)||!ZombieBehavior.valid(s.mob,t))throw new IllegalArgumentException("Target dead/allied/creative/spectator/wrong dimension");setTarget(s.mob,t);reply(c.getSource(),inspect(s.mob));return 1;}))));
            root.then(Commands.literal("check").executes(c->attempt(c.getSource(),()->{reply(c.getSource(),check(require(c.getSource().getServer())));return 1;})));
            root.then(Commands.literal("off").executes(c->attempt(c.getSource(),()->{var s=require(c.getSource().getServer());s.mob.addTag("buildupmobtweaks:disable_"+s.scene.feature.id().getPath());if(s.scene==Scene.PIGLIN_ANGER&&s.target!=null)s.mob.hurtServer(s.level,s.mob.damageSources().mobAttack(s.target),1);reply(c.getSource(),"Single gate off; allow real ticks to retire its in-flight state. "+inspect(s.mob));return 1;})));
            root.then(Commands.literal("cleanup").executes(c->{cleanup(c.getSource().getServer());reply(c.getSource(),"Generated entities/projectiles and arena retired; selected entity restoration after 2 real ticks.");return 1;}));
            d.register(root);
        });
        ServerTickEvents.END_SERVER_TICK.register(server->{var s=SESSIONS.get(server);if(s!=null)s.observe();});
        ServerLifecycleEvents.SERVER_STOPPING.register(QaHarness::shutdown);
        BuildupMobTweaks.LOGGER.warn("Development QA commands enabled; use a disposable world. Production mod does not contain this entrypoint.");
    }
    @FunctionalInterface private interface Action {int run() throws com.mojang.brigadier.exceptions.CommandSyntaxException;}
    private static int attempt(CommandSourceStack source,Action action) throws com.mojang.brigadier.exceptions.CommandSyntaxException {try{return action.run();}catch(IllegalArgumentException error){source.sendFailure(Component.literal(error.getMessage()));return 0;}}
    private static void reply(CommandSourceStack s,String text){s.sendSuccess(()->Component.literal(text),false);}
    private static Session require(MinecraftServer server){var s=SESSIONS.get(server);if(s==null||s.restoring)throw new IllegalArgumentException("No active QA session");return s;}
    private static void ensureFree(ServerLevel level){if(SESSIONS.containsKey(level.getServer()))throw new IllegalArgumentException("One session/server; cleanup first");if(level.getDifficulty()==net.minecraft.world.Difficulty.PEACEFUL)throw new IllegalArgumentException("Use non-peaceful difficulty; QA does not change world rules");}
    private static void assertEnabled(Scene scene){if(scene==Scene.WITHER_HOMING&&!new FeatureRegistry(BuildupMobTweaks.config()).isEnabled(FeatureId.WITHER_SKELETON_SKULL))throw new IllegalArgumentException("Skull feature must also be enabled");if(!new FeatureRegistry(BuildupMobTweaks.config()).isEnabled(scene.feature))throw new IllegalArgumentException("Feature disabled in server config: "+scene.feature.id()+"; QA never changes global gates");}
    public static Session spawn(ServerLevel level,BlockPos origin,Scene scene){
        ensureFree(level);assertEnabled(scene);
        // Preflight all blocks before any mutation. Never touches block entities, liquids or occupied airspace.
        Map<BlockPos,BlockState> saved=new LinkedHashMap<>();
        for(int x=-14;x<=14;x++)for(int z=-14;z<=14;z++)for(int y=-1;y<=4;y++){
            var p=origin.offset(x,y,z);if(!level.hasChunkAt(p)||level.getBlockEntity(p)!=null||!level.getFluidState(p).isEmpty()||(y>=0&&!level.getBlockState(p).isAir()))throw new IllegalArgumentException("Need loaded empty 29x29x5 airspace; block entities/fluids prohibited: "+p);
            if(y==-1||y==4)saved.put(p,level.getBlockState(p));
        }
        Mob mob=switch(scene){case ILLUSION,ILLUSION_ARROWS->EntityTypes.ILLUSIONER.create(level,EntitySpawnReason.SPAWN_ITEM_USE);case VINDICATOR->EntityTypes.VINDICATOR.create(level,EntitySpawnReason.SPAWN_ITEM_USE);case PILLAGER->EntityTypes.PILLAGER.create(level,EntitySpawnReason.COMMAND);case SKELETON,SKELETON_DRAW->EntityTypes.SKELETON.create(level,EntitySpawnReason.COMMAND);case BOGGED_OMEN->EntityTypes.BOGGED.create(level,EntitySpawnReason.COMMAND);case STRAY_FLIP->EntityTypes.STRAY.create(level,EntitySpawnReason.COMMAND);case ENDERMAN->EntityTypes.ENDERMAN.create(level,EntitySpawnReason.COMMAND);case EVOKER->EntityTypes.EVOKER.create(level,EntitySpawnReason.COMMAND);case DOOR->EntityTypes.ZOMBIE.create(level,EntitySpawnReason.COMMAND);case PARCHED->EntityTypes.PARCHED.create(level,EntitySpawnReason.COMMAND);case WITHER_BIRTH,WITHER_BOW,WITHER_SKULL,WITHER_HOMING->EntityTypes.WITHER_SKELETON.create(level,EntitySpawnReason.COMMAND);case PIGLIN,PIGLIN_DODGE->EntityTypes.PIGLIN.create(level,EntitySpawnReason.COMMAND);case PIGLIN_ANGER,ZOMBIFIED_BIRTH,ZOMBIFIED_CROSSBOW->EntityTypes.ZOMBIFIED_PIGLIN.create(level,EntitySpawnReason.COMMAND);case FLETCHER,FISHERMAN,CLERIC->EntityTypes.ZOMBIE_VILLAGER.create(level,EntitySpawnReason.COMMAND);case DROWNED_FISHING->EntityTypes.DROWNED.create(level,EntitySpawnReason.COMMAND);};
        Mob target=scene==Scene.VINDICATOR?EntityTypes.ZOMBIE.create(level,EntitySpawnReason.COMMAND):scene==Scene.CLERIC?EntityTypes.ZOMBIE.create(level,EntitySpawnReason.COMMAND):scene==Scene.PIGLIN_ANGER?EntityTypes.ZOMBIFIED_PIGLIN.create(level,EntitySpawnReason.COMMAND):EntityTypes.IRON_GOLEM.create(level,EntitySpawnReason.COMMAND);
        if(mob==null||target==null)throw new IllegalArgumentException("Entity creation failed");
        // A disabled/missing birth rule must fail before any terrain mutation.
        if(scene==Scene.WITHER_BIRTH||scene==Scene.ZOMBIFIED_BIRTH){mob.discard();mob=birth(level,origin,scene);}
        if(professionScene(scene)){mob.discard();mob=professionBirth(level,origin,scene);}
        if(scene==Scene.ILLUSION||scene==Scene.ILLUSION_ARROWS||scene==Scene.VINDICATOR){mob.finalizeSpawn(level,level.getCurrentDifficultyAt(origin),EntitySpawnReason.SPAWN_ITEM_USE,null);if(mob instanceof Illusioner i)IllusionBehavior.instance().initialize(i);}
        for(int x=-14;x<=14;x++)for(int z=-14;z<=14;z++)level.setBlockAndUpdate(origin.offset(x,-1,z),Blocks.STONE.defaultBlockState());
        // Roof fixes sunlight/light mode and makes Enderman teleports testable within a safe solid floor.
        for(int x=-14;x<=14;x++)for(int z=-14;z<=14;z++)level.setBlockAndUpdate(origin.offset(x,4,z),Blocks.STONE.defaultBlockState());
        mob.snapTo(Vec3.atBottomCenterOf(origin),0,0);mob.setPersistenceRequired();mob.addTag(TAG);mob.getRandom().setSeed(480000+scene.ordinal());
        target.snapTo(Vec3.atBottomCenterOf(origin.offset(0,0,scene==Scene.VINDICATOR?2:scene==Scene.FLETCHER||scene==Scene.ZOMBIFIED_CROSSBOW?6:scene==Scene.CLERIC?5:scene==Scene.PILLAGER||scene==Scene.PIGLIN_ANGER?2:scene==Scene.PIGLIN?6:scene==Scene.BOGGED_OMEN?3:9)),180,0);target.setNoAi(true);target.setPersistenceRequired();target.addTag(TAG);target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);target.setHealth(scene==Scene.CLERIC?900:1000);
        if(professionScene(scene)){mob.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);target.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.IRON_HELMET));target.addTag("buildupmobtweaks:disable_equipment_assignment");}
        if(scene==Scene.FISHERMAN||scene==Scene.DROWNED_FISHING){target.setNoAi(false);target.getGoalSelector().removeAllGoals(g->true);((com.davidblackcn.buildupmobtweaks.mixin.rebuild.RemainingTargetAccess)target).buildup$targets().removeAllGoals(g->true);target.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);}
        mob.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.IRON_HELMET));mob.setDropChance(EquipmentSlot.HEAD,0);
        if(mob instanceof Piglin p){p.setImmuneToZombification(true);mob.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.CROSSBOW));if(scene==Scene.PIGLIN)mob.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);}
        if(scene==Scene.PILLAGER){mob.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.CROSSBOW));mob.addTag("buildupmobtweaks:disable_pillager_spawn_supplies");((Pillager)mob).getInventory().setItem(0,new ItemStack(Items.STONE_AXE));}
        if(scene==Scene.SKELETON){mob.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.BOW));mob.addTag("buildupmobtweaks:disable_skeleton_sniping");}
        if(scene==Scene.SKELETON_DRAW||scene==Scene.BOGGED_OMEN||scene==Scene.STRAY_FLIP){
            mob.addTag("buildupmobtweaks:disable_skeleton_weapon_switching");mob.addTag("buildupmobtweaks:disable_ranged_reposition");mob.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);
            mob.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(scene==Scene.SKELETON_DRAW?Items.BOW:Items.STONE_SWORD));
            if(scene==Scene.BOGGED_OMEN)target.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.TRIAL_OMEN,1200));
        }
        if(scene==Scene.ILLUSION||scene==Scene.ILLUSION_ARROWS)mob.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);
        if(scene==Scene.VINDICATOR){target.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.IRON_HELMET));target.addTag("buildupmobtweaks:disable_equipment_assignment");}
        if(scene==Scene.EVOKER||scene==Scene.ENDERMAN)mob.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);
        if(scene==Scene.PARCHED||scene==Scene.WITHER_BOW)mob.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.BOW));
        if(scene==Scene.WITHER_SKULL||scene==Scene.WITHER_HOMING){mob.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.STONE_SWORD));mob.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);}
        if(scene==Scene.WITHER_HOMING)target.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.TRIAL_OMEN,1200));
        assign(mob,scene);
        var s=new Session(level,mob,target,scene,origin,saved,true);SESSIONS.put(level.getServer(),s);
        if(!level.addFreshEntity(mob)||!level.addFreshEntity(target)){cleanup(level.getServer());throw new IllegalArgumentException("Entity add rejected; scene cleaned");}
        if(scene==Scene.BOGGED_OMEN||scene==Scene.STRAY_FLIP)mob.getAttached(SkeletonState.DATA).putLong("special_ready",level.getGameTime()+40);
        if(scene==Scene.PIGLIN_ANGER){var peer=EntityTypes.ZOMBIFIED_PIGLIN.create(level,EntitySpawnReason.COMMAND);peer.snapTo(mob.position().add(4,0,0),0,0);peer.addTag(TAG);peer.setPersistenceRequired();s.children.add(peer);if(!level.addFreshEntity(peer)){cleanup(level.getServer());throw new IllegalArgumentException("Peer add rejected");}}
        if(scene==Scene.VINDICATOR){var donor=EntityTypes.EVOKER.create(level,EntitySpawnReason.COMMAND);donor.snapTo(mob.position().add(3,0,0),0,0);donor.setNoAi(true);donor.setPersistenceRequired();donor.addTag(TAG);s.children.add(donor);if(!level.addFreshEntity(donor)){cleanup(level.getServer());throw new IllegalArgumentException("Donor add rejected");}}
        if(scene!=Scene.VINDICATOR&&scene!=Scene.PIGLIN_ANGER&&scene!=Scene.CLERIC)setTarget(mob,target);if(mob instanceof Enderman)mob.hurtServer(level,mob.damageSources().mobAttack(target),1);
        return s;
    }
    /** Selected existing entities are never given/copied equipment. Only an inactive known birth field is changed. */
    public static Session select(Mob mob,Scene scene){
        if(!(mob.level() instanceof ServerLevel level)||!scene.matches(mob))throw new IllegalArgumentException("Exact supported entity type required");ensureFree(level);assertEnabled(scene);
        if(scene==Scene.WITHER_BIRTH||scene==Scene.ZOMBIFIED_BIRTH)throw new IllegalArgumentException("Birth scenes generate native candidates only; existing entities are never re-finalized");
        if(mob instanceof Piglin&&mob.getBrain().hasMemoryValue(MemoryModuleType.ANGRY_AT))throw new IllegalArgumentException("Select a Piglin without existing anger memory");
        var type=stateType(scene);if(!mob.isAlive()||mob.isNoAi()||mob.getTarget()!=null||mob.isUsingItem()||mob.isPassenger()||mob.isVehicle())throw new IllegalArgumentException("Select idle, living, AI-enabled entity without target/passengers/use");
        if(type!=null&&(!mob.hasAttached(type)||mob.getAttached(type).getIntOr("version",-1)!=1))throw new IllegalArgumentException("Unknown/missing state; not rewritten");
        var s=new Session(level,mob,null,scene,mob.blockPosition(),Map.of(),false);SESSIONS.put(level.getServer(),s);assign(mob,scene);return s;
    }
    /** Only generated candidates: real native finalize and actual configured probability, never an equipment grant. */
    private static Mob birth(ServerLevel level,BlockPos pos,Scene scene){
        var c=BuildupMobTweaks.config();int chance=scene==Scene.WITHER_BIRTH?c.equipment.witherBowChance.get():c.equipment.zombifiedCrossbowChance.get();
        if(!new FeatureRegistry(c).isEnabled(FeatureId.EQUIPMENT_ASSIGNMENT)||chance==0)throw new IllegalArgumentException("Birth gate/probability disabled; not overridden by QA");
        for(int attempt=0;attempt<512;attempt++){
            Mob m=(scene==Scene.WITHER_BIRTH?EntityTypes.WITHER_SKELETON:EntityTypes.ZOMBIFIED_PIGLIN).create(level,EntitySpawnReason.SPAWN_ITEM_USE);
            m.snapTo(Vec3.atBottomCenterOf(pos),0,0);m.getRandom().setSeed(520001+attempt);
            // Adult QA candidates suppress native chicken-jockey side spawns before a Session can own them.
            m.finalizeSpawn(level,level.getCurrentDifficultyAt(pos),EntitySpawnReason.SPAWN_ITEM_USE,
                    scene==Scene.ZOMBIFIED_BIRTH?new Zombie.ZombieGroupData(false,false):null);
            var d=m.getAttached(EquipmentService.DATA);String outcome=d==null?"missing":d.getStringOr("outcome","");
            if(outcome.equals("equipped")&&m.getMainHandItem().is(scene==Scene.WITHER_BIRTH?Items.BOW:Items.CROSSBOW)){
                BuildupMobTweaks.LOGGER.info("R4E2A_QA_NATIVE_BIRTH scene={} attempts={} state={}",scene.id,attempt+1,d);return m;
            }
            m.discard();if(!outcome.equals("missed")&&!outcome.equals("no_rule"))throw new IllegalArgumentException("Native birth blocked: "+outcome);
        }
        throw new IllegalArgumentException("No matching native birth in 512 bounded candidates; rules/chance need checking, no forced equipment");
    }
    public static boolean professionScene(Scene s){return s==Scene.FLETCHER||s==Scene.FISHERMAN||s==Scene.CLERIC||s==Scene.DROWNED_FISHING||s==Scene.ZOMBIFIED_CROSSBOW;}
    private static Mob professionBirth(ServerLevel level,BlockPos pos,Scene scene){
        if(scene==Scene.ZOMBIFIED_CROSSBOW){assertEnabled(Scene.ZOMBIFIED_BIRTH);return birth(level,pos,Scene.ZOMBIFIED_BIRTH);}
        for(int i=0;i<512;i++){
            Mob m=scene==Scene.DROWNED_FISHING?EntityTypes.DROWNED.create(level,EntitySpawnReason.SPAWN_ITEM_USE):EntityTypes.ZOMBIE_VILLAGER.create(level,EntitySpawnReason.SPAWN_ITEM_USE);
            m.snapTo(Vec3.atBottomCenterOf(pos),0,0);
            if(m instanceof ZombieVillager z)z.setVillagerData(z.getVillagerData().withProfession(level.registryAccess(),scene==Scene.FLETCHER?VillagerProfession.FLETCHER:scene==Scene.FISHERMAN?VillagerProfession.FISHERMAN:VillagerProfession.CLERIC));
            m.finalizeSpawn(level,level.getCurrentDifficultyAt(pos),EntitySpawnReason.SPAWN_ITEM_USE,new Zombie.ZombieGroupData(false,false));
            if(ProfessionBehavior.hand(m,scene==Scene.FLETCHER?Items.CROSSBOW:scene==Scene.CLERIC?Items.SPLASH_POTION:Items.FISHING_ROD)!=null){
                BuildupMobTweaks.LOGGER.info("R4E2B_QA_NATIVE_BIRTH scene={} attempts={} state={}",scene.id,i+1,m.getAttached(ProfessionState.DATA));return m;}
            m.discard();if(scene!=Scene.DROWNED_FISHING)throw new IllegalArgumentException("Native profession birth blocked; equipment is not forced");
        }throw new IllegalArgumentException("No native drowned rod in 512 candidates; not forced");
    }
    private static AttachmentType<CompoundTag> stateType(Scene scene){return switch(scene){case ILLUSION,ILLUSION_ARROWS,VINDICATOR->IllusionState.DATA;case FLETCHER,FISHERMAN,CLERIC,DROWNED_FISHING,ZOMBIFIED_CROSSBOW->ProfessionState.DATA;case SKELETON_DRAW,BOGGED_OMEN,STRAY_FLIP->SkeletonState.DATA;case DOOR->ZombieState.DATA;case EVOKER->RaidState.EVOKER;case ENDERMAN->RemainingState.DATA;case PARCHED,WITHER_BOW,WITHER_SKULL,WITHER_HOMING->ExtendedSkeletonState.DATA;case PIGLIN,PIGLIN_DODGE->PiglinState.DATA;default->null;};}
    private static void setTarget(Mob mob,LivingEntity target){if(mob instanceof Piglin p){if(target!=null)p.hurtServer((ServerLevel)p.level(),p.damageSources().mobAttack(target),1);else {p.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);p.getBrain().eraseMemory(MemoryModuleType.ANGRY_AT);p.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);}}else mob.setTarget(target);}
    private static void assign(Mob mob,Scene scene){
        if(scene==Scene.VINDICATOR)return;
        if(scene==Scene.ILLUSION||scene==Scene.ILLUSION_ARROWS){var d=mob.getAttached(IllusionState.DATA).copy();d.putBoolean("clone_trait",scene==Scene.ILLUSION_ARROWS);mob.setAttached(IllusionState.DATA,d);return;}
        if(professionScene(scene)||scene==Scene.SKELETON_DRAW||scene==Scene.BOGGED_OMEN||scene==Scene.STRAY_FLIP)return;
        var type=stateType(scene);if(type==null)return;
        var data=mob.hasAttached(type)?mob.getAttached(type).copy():new CompoundTag();data.putInt("version",1);
        data.putString("birth",switch(scene){case DOOR->"door";case EVOKER->"fireball";case ENDERMAN->"combo";case PARCHED->"spin";case WITHER_SKULL,WITHER_HOMING->"skull";case PIGLIN_DODGE->"dodge";default->"none";});
        if(scene==Scene.DOOR){data.putFloat("door_health",40);data.putString("material","minecraft:oak_door");}mob.setAttached(type,data);
    }
    public static String inspect(Mob m){
        var goals=m.getGoalSelector().getAvailableGoals().stream().map(g->g.getPriority()+":"+g.getGoal().getClass().getSimpleName()+"["+g.isRunning()+","+g.getGoal().getFlags()+"]").toList();
        String state="";
        if(m instanceof Piglin){var r=PiglinState.runtime(m);state=" shots="+r.shots+" swings="+r.swings+" dodges="+r.dodges+" cancels="+r.cancels+" dodgeUntil="+r.dodgeUntil+" pose="+((Piglin)m).getArmPose()+" data="+m.getAttached(PiglinState.DATA)+" attackMemory="+m.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET)+" angerMemory="+m.getBrain().getMemory(MemoryModuleType.ANGRY_AT)+" activity="+m.getBrain().getActiveNonCoreActivity();}
        if(m instanceof ZombifiedPiglin z)state=" anger="+z.getPersistentAngerTarget()+" angryUntil="+z.getPersistentAngerEndTime();
        if(m instanceof Pillager p){var r=PillagerBehavior.runtime(p);state=" phase="+r.phase+" swaps="+r.swaps+" shots=native inventory="+p.getInventory()+" data="+p.getAttached(PillagerBehavior.DATA);}
        if(m instanceof AbstractSkeleton s&&SkeletonBehavior.supported(s)){var r=SkeletonState.runtime(s);state=" phase="+r.mode+" shots="+r.shots+" swings="+r.drawSwings+" flips="+r.flips+" snow="+r.snowballs+" dodges="+r.dodges+" mushrooms="+r.mushrooms+" mushroomHits="+r.mushroomHits+" mushroomClouds="+r.mushroomClouds+" visual="+s.getAttachedOrCreate(SkeletonDifferences.VISUAL)+" swaps="+r.swaps+" reserve="+SkeletonState.reserve(s)+" data="+s.getAttached(SkeletonState.DATA);}
        if(m instanceof AbstractSkeleton s&&ExtendedSkeletonBehavior.supported(s)){var r=ExtendedSkeletonState.runtime(s);state=" phase="+r.phase+" spins="+r.spins+" skulls="+r.skulls+" homing="+r.homingTicks+" hits="+r.hits+" shots="+r.shots+" inFlight="+r.projectiles.size()+" data="+s.getAttached(ExtendedSkeletonState.DATA);}
        if(m instanceof Zombie z&&ZombieState.known(z)){var r=ZombieState.runtime(z);state=" phase="+r.mode+" blocks="+r.blocks+" displays="+r.displays.size()+" data="+z.getAttached(ZombieState.DATA);}
        if(m instanceof Evoker){var r=RaidState.runtime(m);state=" active="+m.getAttached(RaidState.ACTIVE)+" start="+r.start+" end="+r.end+" casts="+r.casts+" releases="+r.releases+" data="+m.getAttached(RaidState.EVOKER);}
        if(m instanceof Enderman){var r=RemainingState.runtime(m);state=" start="+r.start+" end="+r.end+" combos="+r.combos+" teleports="+r.teleports+" attacks="+r.attacks+" data="+m.getAttached(RemainingState.DATA);}
        if(IllusionBehavior.supported(m)){var r=IllusionState.runtime(m);state+=" illusionShots="+r.shots+" swaps="+r.swaps+" volleys="+r.volleys+" cloneArrows="+r.arrows+" supports="+r.supports+" positions="+r.positions.length+" targetHealth="+(m.getTarget()==null?"none":m.getTarget().getHealth())+" illusionData="+m.getAttached(IllusionState.DATA);}
        if(ProfessionBehavior.supported(m)){var r=ProfessionState.runtime(m);state+=" professionPhase="+r.phase+" professionShots="+r.shots+" casts="+r.casts+" pulls="+r.pulls+" potions="+r.potions+" hook="+(ProfessionBehavior.hook(m)==null?"none":ProfessionBehavior.hook(m).getUUID())+" professionData="+m.getAttached(ProfessionState.DATA);}
        return "QA MC=26.3 uuid="+m.getUUID()+" source="+m.spawnReason()+" fromDisk="+m.isLoadedFromDisk()+" noAI="+m.isNoAi()+" target="+(m.getTarget()==null?"none":m.getTarget().getUUID()+" valid="+ZombieBehavior.valid(m,m.getTarget()))+" hands="+m.getMainHandItem()+"/"+m.getOffhandItem()+" tags="+m.entityTags()+state+" equipmentLedger="+m.getAttached(EquipmentService.DATA)+" goals="+goals+" brain="+m.getBrain();
    }
    public static String check(Session s){
        var reasons=new ArrayList<String>();var m=s.mob;
        if(!new FeatureRegistry(BuildupMobTweaks.config()).isEnabled(s.scene.feature))reasons.add("server config disabled");
        if(m.entityTags().contains("buildupmobtweaks:vanilla_ai")||m.entityTags().contains("buildupmobtweaks:disable_"+s.scene.feature.id().getPath()))reasons.add("entity gate disabled");
        if(m instanceof Piglin p){if(PiglinBehavior.hand(p)==null)reasons.add("requires an actual non-excluded crossbow");if(p.isBaby())reasons.add("baby Piglin native Brain does not fight");if(s.scene==Scene.PIGLIN_DODGE&&p.getTarget()!=null&&p.distanceToSqr(p.getTarget())>=4)reasons.add("dodge requires charging at distance <2");}
        if(s.scene==Scene.PIGLIN_ANGER)reasons.add("protected same-species target absence is expected; off repeats real hurt input");
        if(m.isNoAi()||!m.isAlive())reasons.add("NoAI/dead");if(s.scene!=Scene.CLERIC&&!ZombieBehavior.valid(m,m.getTarget()))reasons.add("no attackable target");
        if(s.scene==Scene.CLERIC){if(!ProfessionBehavior.instance().enabled(m,FeatureId.ZOMBIE_CLERIC_POTIONS))reasons.add("cleric gate/profession disabled");if(ProfessionBehavior.hand(m,Items.SPLASH_POTION)==null)reasons.add("actual potion consumed or missing; no automatic refill");if(!ProfessionBehavior.healable(m,s.target))reasons.add("requires nearby injured undead support recipient, independent of hostile target");}
        if(m.getTarget()!=null&&!m.hasLineOfSight(m.getTarget()))reasons.add("no line of sight");
        if(s.scene==Scene.PILLAGER){var p=(Pillager)m;boolean axe=p.getMainHandItem().is(Items.STONE_AXE),bow=p.getMainHandItem().is(Items.CROSSBOW);for(int i=0;i<p.getInventory().getContainerSize();i++){axe|=p.getInventory().getItem(i).is(Items.STONE_AXE);bow|=p.getInventory().getItem(i).is(Items.CROSSBOW);}if(!axe||!bow)reasons.add("requires actual crossbow + stone axe in hand/container");}
        if(m instanceof AbstractSkeleton z&&ExtendedSkeletonBehavior.supported(z)){
            if(!ExtendedSkeletonBehavior.instance().enabled(z,s.scene.feature))reasons.add("extended entity/config/exclusion gate disabled");
            if((s.scene==Scene.PARCHED||s.scene==Scene.WITHER_BOW)&&!z.getMainHandItem().is(Items.BOW)&&!z.getOffhandItem().is(Items.BOW))reasons.add("requires an actual vanilla bow");
            if(s.scene==Scene.WITHER_HOMING){if(!ExtendedSkeletonBehavior.instance().enabled(z,FeatureId.WITHER_SKELETON_SKULL))reasons.add("skull generation gate disabled");if(z.getTarget()!=null&&!ExtendedSkeletonBehavior.ominous(z.getTarget()))reasons.add("tracking requires ominous target; skull remains ballistic");}
        }
        if(m instanceof AbstractSkeleton z&&(s.scene==Scene.SKELETON_DRAW||s.scene==Scene.BOGGED_OMEN||s.scene==Scene.STRAY_FLIP)){
            if(!SkeletonBehavior.instance().enabled(z,s.scene.feature))reasons.add("skeleton entity/config/exclusion gate disabled");
            if(s.scene==Scene.SKELETON_DRAW&&!SkeletonBehavior.instance().hasBow(z))reasons.add("requires actual non-excluded bow");
            if(s.scene==Scene.BOGGED_OMEN){if(!SkeletonBehavior.instance().enabled(z,FeatureId.BOGGED_SPORE_RETREAT))reasons.add("base spore phase disabled");if(!SkeletonDifferences.ominous(z.getTarget()))reasons.add("requires BAD_OMEN or TRIAL_OMEN target");}
        }
        if(s.scene==Scene.SKELETON){var z=(AbstractSkeleton)m;if(!z.getMainHandItem().is(Items.BOW)&&!SkeletonState.reserve(z).is(Items.BOW))reasons.add("missing owned bow");}
        var type=stateType(s.scene);if(type!=null){var data=m.getAttached(type);if(data==null)reasons.add("missing state");else for(String key:List.of("special_ready","combo_ready","guard_ready","dodge_ready","rod_ready","potion_ready","swap_ready","arrow_ready","pause_ready","support_ready")){long left=data.getLongOr(key,0)-s.level.getGameTime();if(left>0)reasons.add(key+" remaining="+left);}}
        if(s.scene==Scene.WITHER_BIRTH||s.scene==Scene.ZOMBIFIED_BIRTH)reasons.add("birth ledger="+m.getAttached(EquipmentService.DATA)+"; off affects future births, actual item is retained; ZP ranged AI is available in E2-b; this gate controls future birth only");
        return "QA prerequisites="+(reasons.isEmpty()?"satisfied; wait for native Brain/Goal arbitration/distance/phase":reasons)+" elapsed="+s.elapsed+" "+inspect(m);
    }
    public static void cleanup(MinecraftServer server){var s=SESSIONS.get(server);if(s==null)return;s.retire();if(s.generated){SESSIONS.remove(server);s.restoreBlocks();}else{s.restoring=true;s.restoreAt=s.level.getGameTime()+2;}}
    private static void shutdown(MinecraftServer server){var s=SESSIONS.remove(server);if(s!=null){s.retire();s.restoreSelection();s.restoreBlocks();}}
    public static final class Session {
        public final ServerLevel level;public final Mob mob;public final Mob target;public final Scene scene;public final BlockPos origin;public int elapsed;
        public boolean sawAxe,sawBow,sawSword,sawWalk,sawProjectile,sawDoor,sawSpin;public double maxMove;private final Vec3 initial;
        private final Map<BlockPos,BlockState> blocks;private final boolean generated;private final Set<Entity> children=Collections.newSetFromMap(new IdentityHashMap<>());
        private final Set<String> oldTags;private final Set<UUID> previousChildren=new HashSet<>();private final CompoundTag oldState;private final LivingEntity oldTarget;private boolean restoring,piglinNear;private long restoreAt;
        private Session(ServerLevel l,Mob m,Mob t,Scene s,BlockPos o,Map<BlockPos,BlockState>b,boolean g){level=l;mob=m;target=t;scene=s;origin=o;blocks=b;generated=g;initial=m.position();oldTags=Set.copyOf(m.entityTags());oldTarget=m.getTarget();var type=stateType(s);oldState=type!=null&&m.hasAttached(type)?m.getAttached(type).copy():null;
            for(var e:l.getEntitiesOfClass(Entity.class,m.getBoundingBox().inflate(96)))if(owned(e))previousChildren.add(e.getUUID());}
        private boolean owned(Entity e){return e instanceof AreaEffectCloud c&&c.getOwner()==mob||e instanceof Projectile p&&p.getOwner()==mob||e instanceof Vex v&&v.getOwnerReference()!=null&&v.getOwnerReference().matches(mob);}
        private void observe(){
            if(restoring){if(level.getGameTime()>=restoreAt){restoreSelection();SESSIONS.remove(level.getServer());}return;}
            elapsed++;
            if(generated&&scene==Scene.VINDICATOR&&elapsed==5)for(var child:children)if(child instanceof Evoker e)e.hurtServer(level,e.damageSources().mobAttack(target),1);
            if(generated&&scene==Scene.PIGLIN_ANGER&&elapsed==5)mob.hurtServer(level,mob.damageSources().mobAttack(target),1);
            if(generated&&mob instanceof Piglin p&&target!=null){if(scene==Scene.PIGLIN&&elapsed==140&&!p.getMainHandItem().isEmpty()&&p.getOffhandItem().isEmpty()){p.stopUsingItem();var actual=p.getMainHandItem();p.setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);p.setItemSlot(EquipmentSlot.OFFHAND,actual);}
                if(scene==Scene.PIGLIN_DODGE&&!piglinNear&&elapsed>=205&&p.onGround()&&p.isUsingItem()&&p.getTicksUsingItem()>=5&&p.getTicksUsingItem()<=12){piglinNear=true;target.snapTo(p.position().add(0,0,1.3),0,0);}}
            // Only owned spawned projectiles/minions; no broad deletion of neighboring entities or drops.
            for(var e:level.getEntitiesOfClass(Entity.class,mob.getBoundingBox().inflate(96)))if(owned(e)&&!previousChildren.contains(e.getUUID()))children.add(e);
            if(generated&&(scene==Scene.FLETCHER||scene==Scene.ZOMBIFIED_CROSSBOW)&&(elapsed==140||elapsed==280||elapsed==420)){
                mob.stopUsingItem();if(elapsed==280)mob.setLeftHanded(true);else {var from=elapsed==140?EquipmentSlot.MAINHAND:EquipmentSlot.OFFHAND;var to=elapsed==140?EquipmentSlot.OFFHAND:EquipmentSlot.MAINHAND;var actual=mob.getItemBySlot(from);if(actual.is(Items.CROSSBOW)&&mob.getItemBySlot(to).isEmpty()){mob.setItemSlot(from,ItemStack.EMPTY);mob.setItemSlot(to,actual);}}
            }
        if(generated&&target!=null){if(scene==Scene.PILLAGER&&elapsed==65)target.snapTo(mob.position().add(0,0,12),0,0);
                if(scene==Scene.SKELETON&&elapsed==140)target.snapTo(mob.position().add(0,0,2),0,0);if(scene==Scene.SKELETON&&elapsed==230)target.snapTo(mob.position().add(0,0,12),0,0);}
            if(generated&&scene==Scene.SKELETON_DRAW&&mob instanceof AbstractSkeleton z&&(elapsed==140||elapsed==280||elapsed==420)){
                z.stopUsingItem();if(elapsed==280)z.setLeftHanded(true);else {var from=elapsed==140?EquipmentSlot.MAINHAND:EquipmentSlot.OFFHAND;var to=elapsed==140?EquipmentSlot.OFFHAND:EquipmentSlot.MAINHAND;var actual=z.getItemBySlot(from);z.setItemSlot(from,ItemStack.EMPTY);z.setItemSlot(to,actual);}
            }
            sawAxe|=mob.getMainHandItem().is(Items.STONE_AXE);sawSword|=mob.getMainHandItem().is(Items.WOODEN_SWORD);sawBow|=mob.getMainHandItem().is(Items.BOW)||mob.getMainHandItem().is(Items.CROSSBOW);sawProjectile|=!children.isEmpty();maxMove=Math.max(maxMove,mob.position().distanceTo(initial));
            if(mob instanceof AbstractSkeleton z)sawWalk|=SkeletonState.runtime(z).mode.equals("walk")&&SkeletonState.runtime(z).shots>0;
            if(mob.getType()==EntityTypes.PARCHED)sawSpin|=mob.getAttachedOrCreate(ExtendedSkeletonState.SPIN)!=0;
            if(mob instanceof Zombie z)sawDoor|=ZombieState.runtime(z).displays.size()==2;
            if(elapsed%40==0)BuildupMobTweaks.LOGGER.info("R4E0_QA scene={} elapsed={} {}",scene.id,elapsed,inspect(mob));
            if(mob.isRemoved()||elapsed>=1200)cleanup(level.getServer());
        }
        private void retire(){
            for(var e:children)if(!e.isRemoved())e.discard();children.clear();
            if(generated){if(mob instanceof Zombie z)for(var e:List.copyOf(ZombieState.runtime(z).displays))e.discard();mob.discard();if(target!=null)target.discard();}
            else {mob.addTag("buildupmobtweaks:vanilla_ai");setTarget(mob,null);mob.stopUsingItem();mob.getNavigation().stop();}
        }
        private void restoreSelection(){if(generated)return;
            // Restore only QA-owned birth/door fields; never rewind earned cooldowns or restore/copy used equipment.
            var type=stateType(scene);if(type!=null&&oldState!=null&&mob.hasAttached(type)){var d=mob.getAttached(type);for(String key:List.of("birth","door_health","material","clone_trait")){d.remove(key);var saved=oldState.get(key);if(saved!=null)d.put(key,saved.copy());}}
            for(String tag:List.of("buildupmobtweaks:vanilla_ai","buildupmobtweaks:disable_"+scene.feature.id().getPath()))if(!oldTags.contains(tag))mob.removeTag(tag);setTarget(mob,oldTarget);
        }
        private void restoreBlocks(){for(var entry:blocks.entrySet())if(level.hasChunkAt(entry.getKey())&&level.getBlockEntity(entry.getKey())==null&&level.getBlockState(entry.getKey()).is(Blocks.STONE))level.setBlockAndUpdate(entry.getKey(),entry.getValue());}
    }
}
