package com.davidblackcn.buildupmobtweaks.qa;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import com.davidblackcn.buildupmobtweaks.feature.*;
import com.davidblackcn.buildupmobtweaks.rebuild.pillager.PillagerBehavior;
import com.davidblackcn.buildupmobtweaks.rebuild.skeleton.*;
import com.davidblackcn.buildupmobtweaks.rebuild.zombie.*;
import com.davidblackcn.buildupmobtweaks.rebuild.raid.*;
import com.davidblackcn.buildupmobtweaks.rebuild.remaining.*;
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
        WITHER_HOMING("wither_homing",FeatureId.WITHER_SKELETON_HOMING);
        public final String id; public final FeatureId feature;
        Scene(String id,FeatureId feature){this.id=id;this.feature=feature;}
        public static Scene parse(String name){return Arrays.stream(values()).filter(s->s.id.equals(name)).findFirst().orElseThrow(()->new IllegalArgumentException("Scene: pillager/skeleton/enderman/evoker/door/parched/wither_bow/wither_skull/wither_homing"));}
        boolean matches(Mob m){return switch(this){case PILLAGER->m.getType()==EntityTypes.PILLAGER;case SKELETON->m.getType()==EntityTypes.SKELETON;case ENDERMAN->m.getType()==EntityTypes.ENDERMAN;case EVOKER->m.getType()==EntityTypes.EVOKER;case DOOR->m.getType()==EntityTypes.ZOMBIE;case PARCHED->m.getType()==EntityTypes.PARCHED;case WITHER_BOW,WITHER_SKULL,WITHER_HOMING->m.getType()==EntityTypes.WITHER_SKELETON;};}
    }
    @Override public void onInitialize(){
        CommandRegistrationCallback.EVENT.register((d,a,e)->{
            var root=Commands.literal("buildupqa").requires(Commands.hasPermission(Commands.LEVEL_OWNERS));
            root.then(Commands.literal("spawn").then(Commands.argument("scene",StringArgumentType.word()).suggests((c,b)->{for(var s:Scene.values())b.suggest(s.id);return b.buildFuture();}).executes(c->attempt(c.getSource(),()->{
                var session=spawn(c.getSource().getLevel(),BlockPos.containing(c.getSource().getPosition()).offset(0,0,8),Scene.parse(StringArgumentType.getString(c,"scene")));
                reply(c.getSource(),"QA uuid="+session.mob.getUUID()+" target="+session.target.getUUID()+"; inspect/check/off/cleanup; auto-cleanup after 1200 ticks");return 1;}))));
            root.then(Commands.literal("inspect").then(Commands.argument("entity",EntityArgument.entity()).executes(c->{var ent=EntityArgument.getEntity(c,"entity");if(!(ent instanceof Mob m))return 0;reply(c.getSource(),inspect(m));return 1;})));
            root.then(Commands.literal("force").then(Commands.argument("scene",StringArgumentType.word()).then(Commands.argument("entity",EntityArgument.entity()).executes(c->attempt(c.getSource(),()->{
                if(!(EntityArgument.getEntity(c,"entity") instanceof Mob m))throw new IllegalArgumentException("Expected Mob");select(m,Scene.parse(StringArgumentType.getString(c,"scene")));reply(c.getSource(),"Only birth/trait forced; existing equipment is not replaced. "+inspect(m));return 1;})))));
            root.then(Commands.literal("target").then(Commands.argument("entity",EntityArgument.entity()).executes(c->attempt(c.getSource(),()->{
                var s=require(c.getSource().getServer());if(!(EntityArgument.getEntity(c,"entity") instanceof LivingEntity t)||!ZombieBehavior.valid(s.mob,t))throw new IllegalArgumentException("Target dead/allied/creative/spectator/wrong dimension");s.mob.setTarget(t);reply(c.getSource(),inspect(s.mob));return 1;}))));
            root.then(Commands.literal("check").executes(c->attempt(c.getSource(),()->{reply(c.getSource(),check(require(c.getSource().getServer())));return 1;})));
            root.then(Commands.literal("off").executes(c->attempt(c.getSource(),()->{var s=require(c.getSource().getServer());s.mob.addTag("buildupmobtweaks:disable_"+s.scene.feature.id().getPath());reply(c.getSource(),"Single gate off; allow real ticks to retire its in-flight state. "+inspect(s.mob));return 1;})));
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
        var mob=switch(scene){case PILLAGER->EntityTypes.PILLAGER.create(level,EntitySpawnReason.COMMAND);case SKELETON->EntityTypes.SKELETON.create(level,EntitySpawnReason.COMMAND);case ENDERMAN->EntityTypes.ENDERMAN.create(level,EntitySpawnReason.COMMAND);case EVOKER->EntityTypes.EVOKER.create(level,EntitySpawnReason.COMMAND);case DOOR->EntityTypes.ZOMBIE.create(level,EntitySpawnReason.COMMAND);case PARCHED->EntityTypes.PARCHED.create(level,EntitySpawnReason.COMMAND);case WITHER_BOW,WITHER_SKULL,WITHER_HOMING->EntityTypes.WITHER_SKELETON.create(level,EntitySpawnReason.COMMAND);};
        var target=EntityTypes.IRON_GOLEM.create(level,EntitySpawnReason.COMMAND);
        if(mob==null||target==null)throw new IllegalArgumentException("Entity creation failed");
        for(int x=-14;x<=14;x++)for(int z=-14;z<=14;z++)level.setBlockAndUpdate(origin.offset(x,-1,z),Blocks.STONE.defaultBlockState());
        // Roof fixes sunlight/light mode and makes Enderman teleports testable within a safe solid floor.
        for(int x=-14;x<=14;x++)for(int z=-14;z<=14;z++)level.setBlockAndUpdate(origin.offset(x,4,z),Blocks.STONE.defaultBlockState());
        mob.snapTo(Vec3.atBottomCenterOf(origin),0,0);mob.setPersistenceRequired();mob.addTag(TAG);mob.getRandom().setSeed(480000+scene.ordinal());
        target.snapTo(Vec3.atBottomCenterOf(origin.offset(0,0,scene==Scene.PILLAGER?2:9)),180,0);target.setNoAi(true);target.setPersistenceRequired();target.addTag(TAG);target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);target.setHealth(1000);
        mob.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.IRON_HELMET));mob.setDropChance(EquipmentSlot.HEAD,0);
        if(scene==Scene.PILLAGER){mob.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.CROSSBOW));mob.addTag("buildupmobtweaks:disable_pillager_spawn_supplies");((Pillager)mob).getInventory().setItem(0,new ItemStack(Items.STONE_AXE));}
        if(scene==Scene.SKELETON){mob.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.BOW));mob.addTag("buildupmobtweaks:disable_skeleton_sniping");}
        if(scene==Scene.EVOKER||scene==Scene.ENDERMAN)mob.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);
        if(scene==Scene.PARCHED||scene==Scene.WITHER_BOW)mob.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.BOW));
        if(scene==Scene.WITHER_SKULL||scene==Scene.WITHER_HOMING){mob.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.STONE_SWORD));mob.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);}
        if(scene==Scene.WITHER_HOMING)target.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.TRIAL_OMEN,1200));
        assign(mob,scene);
        var s=new Session(level,mob,target,scene,origin,saved,true);SESSIONS.put(level.getServer(),s);
        if(!level.addFreshEntity(mob)||!level.addFreshEntity(target)){cleanup(level.getServer());throw new IllegalArgumentException("Entity add rejected; scene cleaned");}
        mob.setTarget(target);if(mob instanceof Enderman)mob.hurtServer(level,mob.damageSources().mobAttack(target),1);
        return s;
    }
    /** Selected existing entities are never given/copied equipment. Only an inactive known birth field is changed. */
    public static Session select(Mob mob,Scene scene){
        if(!(mob.level() instanceof ServerLevel level)||!scene.matches(mob))throw new IllegalArgumentException("Exact supported entity type required");ensureFree(level);assertEnabled(scene);
        var type=stateType(scene);if(!mob.isAlive()||mob.isNoAi()||mob.getTarget()!=null||mob.isUsingItem()||mob.isPassenger()||mob.isVehicle())throw new IllegalArgumentException("Select idle, living, AI-enabled entity without target/passengers/use");
        if(type!=null&&(!mob.hasAttached(type)||mob.getAttached(type).getIntOr("version",-1)!=1))throw new IllegalArgumentException("Unknown/missing state; not rewritten");
        var s=new Session(level,mob,null,scene,mob.blockPosition(),Map.of(),false);SESSIONS.put(level.getServer(),s);assign(mob,scene);return s;
    }
    private static AttachmentType<CompoundTag> stateType(Scene scene){return switch(scene){case DOOR->ZombieState.DATA;case EVOKER->RaidState.EVOKER;case ENDERMAN->RemainingState.DATA;case PARCHED,WITHER_BOW,WITHER_SKULL,WITHER_HOMING->ExtendedSkeletonState.DATA;default->null;};}
    private static void assign(Mob mob,Scene scene){
        var type=stateType(scene);if(type==null)return;
        var data=mob.hasAttached(type)?mob.getAttached(type).copy():new CompoundTag();data.putInt("version",1);
        data.putString("birth",switch(scene){case DOOR->"door";case EVOKER->"fireball";case ENDERMAN->"combo";case PARCHED->"spin";case WITHER_SKULL,WITHER_HOMING->"skull";default->"none";});
        if(scene==Scene.DOOR){data.putFloat("door_health",40);data.putString("material","minecraft:oak_door");}mob.setAttached(type,data);
    }
    public static String inspect(Mob m){
        var goals=m.getGoalSelector().getAvailableGoals().stream().map(g->g.getPriority()+":"+g.getGoal().getClass().getSimpleName()+"["+g.isRunning()+","+g.getGoal().getFlags()+"]").toList();
        String state="";
        if(m instanceof Pillager p){var r=PillagerBehavior.runtime(p);state=" phase="+r.phase+" swaps="+r.swaps+" shots=native inventory="+p.getInventory()+" data="+p.getAttached(PillagerBehavior.DATA);}
        if(m instanceof AbstractSkeleton s&&SkeletonBehavior.supported(s)){var r=SkeletonState.runtime(s);state=" phase="+r.mode+" shots="+r.shots+" swaps="+r.swaps+" reserve="+SkeletonState.reserve(s)+" data="+s.getAttached(SkeletonState.DATA);}
        if(m instanceof AbstractSkeleton s&&ExtendedSkeletonBehavior.supported(s)){var r=ExtendedSkeletonState.runtime(s);state=" phase="+r.phase+" spins="+r.spins+" skulls="+r.skulls+" homing="+r.homingTicks+" hits="+r.hits+" shots="+r.shots+" inFlight="+r.projectiles.size()+" data="+s.getAttached(ExtendedSkeletonState.DATA);}
        if(m instanceof Zombie z&&ZombieState.known(z)){var r=ZombieState.runtime(z);state=" phase="+r.mode+" blocks="+r.blocks+" displays="+r.displays.size()+" data="+z.getAttached(ZombieState.DATA);}
        if(m instanceof Evoker){var r=RaidState.runtime(m);state=" active="+m.getAttached(RaidState.ACTIVE)+" start="+r.start+" end="+r.end+" casts="+r.casts+" releases="+r.releases+" data="+m.getAttached(RaidState.EVOKER);}
        if(m instanceof Enderman){var r=RemainingState.runtime(m);state=" start="+r.start+" end="+r.end+" combos="+r.combos+" teleports="+r.teleports+" attacks="+r.attacks+" data="+m.getAttached(RemainingState.DATA);}
        return "QA MC=26.3 uuid="+m.getUUID()+" source="+m.spawnReason()+" fromDisk="+m.isLoadedFromDisk()+" noAI="+m.isNoAi()+" target="+(m.getTarget()==null?"none":m.getTarget().getUUID()+" valid="+ZombieBehavior.valid(m,m.getTarget()))+" hands="+m.getMainHandItem()+"/"+m.getOffhandItem()+" tags="+m.entityTags()+state+" goals="+goals+" brain="+m.getBrain();
    }
    public static String check(Session s){
        var reasons=new ArrayList<String>();var m=s.mob;
        if(!new FeatureRegistry(BuildupMobTweaks.config()).isEnabled(s.scene.feature))reasons.add("server config disabled");
        if(m.entityTags().contains("buildupmobtweaks:vanilla_ai")||m.entityTags().contains("buildupmobtweaks:disable_"+s.scene.feature.id().getPath()))reasons.add("entity gate disabled");
        if(m.isNoAi()||!m.isAlive())reasons.add("NoAI/dead");if(!ZombieBehavior.valid(m,m.getTarget()))reasons.add("no attackable target");
        if(m.getTarget()!=null&&!m.hasLineOfSight(m.getTarget()))reasons.add("no line of sight");
        if(s.scene==Scene.PILLAGER){var p=(Pillager)m;boolean axe=p.getMainHandItem().is(Items.STONE_AXE),bow=p.getMainHandItem().is(Items.CROSSBOW);for(int i=0;i<p.getInventory().getContainerSize();i++){axe|=p.getInventory().getItem(i).is(Items.STONE_AXE);bow|=p.getInventory().getItem(i).is(Items.CROSSBOW);}if(!axe||!bow)reasons.add("requires actual crossbow + stone axe in hand/container");}
        if(m instanceof AbstractSkeleton z&&ExtendedSkeletonBehavior.supported(z)){
            if(!ExtendedSkeletonBehavior.instance().enabled(z,s.scene.feature))reasons.add("extended entity/config/exclusion gate disabled");
            if((s.scene==Scene.PARCHED||s.scene==Scene.WITHER_BOW)&&!z.getMainHandItem().is(Items.BOW)&&!z.getOffhandItem().is(Items.BOW))reasons.add("requires an actual vanilla bow");
            if(s.scene==Scene.WITHER_HOMING){if(!ExtendedSkeletonBehavior.instance().enabled(z,FeatureId.WITHER_SKELETON_SKULL))reasons.add("skull generation gate disabled");if(z.getTarget()!=null&&!ExtendedSkeletonBehavior.ominous(z.getTarget()))reasons.add("tracking requires ominous target; skull remains ballistic");}
        }
        if(s.scene==Scene.SKELETON){var z=(AbstractSkeleton)m;if(!z.getMainHandItem().is(Items.BOW)&&!SkeletonState.reserve(z).is(Items.BOW))reasons.add("missing owned bow");}
        var type=stateType(s.scene);if(type!=null){var data=m.getAttached(type);if(data==null)reasons.add("missing state");else for(String key:List.of("special_ready","combo_ready","guard_ready")){long left=data.getLongOr(key,0)-s.level.getGameTime();if(left>0)reasons.add(key+" remaining="+left);}}
        return "QA prerequisites="+(reasons.isEmpty()?"satisfied; wait for native Goal arbitration/distance/phase":reasons)+" elapsed="+s.elapsed+" "+inspect(m);
    }
    public static void cleanup(MinecraftServer server){var s=SESSIONS.get(server);if(s==null)return;s.retire();if(s.generated){SESSIONS.remove(server);s.restoreBlocks();}else{s.restoring=true;s.restoreAt=s.level.getGameTime()+2;}}
    private static void shutdown(MinecraftServer server){var s=SESSIONS.remove(server);if(s!=null){s.retire();s.restoreSelection();s.restoreBlocks();}}
    public static final class Session {
        public final ServerLevel level;public final Mob mob;public final IronGolem target;public final Scene scene;public final BlockPos origin;public int elapsed;
        public boolean sawAxe,sawBow,sawSword,sawWalk,sawProjectile,sawDoor,sawSpin;public double maxMove;private final Vec3 initial;
        private final Map<BlockPos,BlockState> blocks;private final boolean generated;private final Set<Entity> children=Collections.newSetFromMap(new IdentityHashMap<>());
        private final Set<String> oldTags;private final Set<UUID> previousChildren=new HashSet<>();private final CompoundTag oldState;private final LivingEntity oldTarget;private boolean restoring;private long restoreAt;
        private Session(ServerLevel l,Mob m,IronGolem t,Scene s,BlockPos o,Map<BlockPos,BlockState>b,boolean g){level=l;mob=m;target=t;scene=s;origin=o;blocks=b;generated=g;initial=m.position();oldTags=Set.copyOf(m.entityTags());oldTarget=m.getTarget();var type=stateType(s);oldState=type!=null&&m.hasAttached(type)?m.getAttached(type).copy():null;
            for(var e:l.getEntitiesOfClass(Entity.class,m.getBoundingBox().inflate(96)))if(owned(e))previousChildren.add(e.getUUID());}
        private boolean owned(Entity e){return e instanceof Projectile p&&p.getOwner()==mob||e instanceof Vex v&&v.getOwnerReference()!=null&&v.getOwnerReference().matches(mob);}
        private void observe(){
            if(restoring){if(level.getGameTime()>=restoreAt){restoreSelection();SESSIONS.remove(level.getServer());}return;}
            elapsed++;
            // Only owned spawned projectiles/minions; no broad deletion of neighboring entities or drops.
            for(var e:level.getEntitiesOfClass(Entity.class,mob.getBoundingBox().inflate(96)))if(owned(e)&&!previousChildren.contains(e.getUUID()))children.add(e);
            if(generated&&target!=null){if(scene==Scene.PILLAGER&&elapsed==65)target.snapTo(mob.position().add(0,0,12),0,0);
                if(scene==Scene.SKELETON&&elapsed==140)target.snapTo(mob.position().add(0,0,2),0,0);if(scene==Scene.SKELETON&&elapsed==230)target.snapTo(mob.position().add(0,0,12),0,0);}
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
            else {mob.addTag("buildupmobtweaks:vanilla_ai");mob.setTarget(null);mob.stopUsingItem();mob.getNavigation().stop();}
        }
        private void restoreSelection(){if(generated)return;
            // Restore only QA-owned birth/door fields; never rewind earned cooldowns or restore/copy used equipment.
            var type=stateType(scene);if(type!=null&&oldState!=null&&mob.hasAttached(type)){var d=mob.getAttached(type);for(String key:List.of("birth","door_health","material")){d.remove(key);var saved=oldState.get(key);if(saved!=null)d.put(key,saved.copy());}}
            for(String tag:List.of("buildupmobtweaks:vanilla_ai","buildupmobtweaks:disable_"+scene.feature.id().getPath()))if(!oldTags.contains(tag))mob.removeTag(tag);mob.setTarget(oldTarget);
        }
        private void restoreBlocks(){for(var entry:blocks.entrySet())if(level.hasChunkAt(entry.getKey())&&level.getBlockEntity(entry.getKey())==null&&level.getBlockState(entry.getKey()).is(Blocks.STONE))level.setBlockAndUpdate(entry.getKey(),entry.getValue());}
    }
}
