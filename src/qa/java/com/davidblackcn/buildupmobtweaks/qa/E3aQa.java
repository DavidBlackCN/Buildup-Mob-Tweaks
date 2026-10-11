package com.davidblackcn.buildupmobtweaks.qa;
import com.davidblackcn.buildupmobtweaks.rebuild.relations.*;
import com.davidblackcn.buildupmobtweaks.rebuild.riding.*;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.equine.SkeletonHorse;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import java.util.*;
/** Independent E3 fixtures; commands change inputs only, all actions use normal world/Goal ticks. */
public final class E3aQa {
    private static final Map<MinecraftServer,Session> SESSIONS=new IdentityHashMap<>();
    public static final List<String> SCENES=List.of("relation_zombie","relation_villager","golem_misfire","chicken","boat","camel","horse","trap","conversion","rider");
    public static void register(){CommandRegistrationCallback.EVENT.register((d,a,e)->d.register(Commands.literal("buildupqa").requires(Commands.hasPermission(Commands.LEVEL_OWNERS)).then(Commands.literal("e3").requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
        .then(Commands.literal("spawn").then(Commands.argument("scene",StringArgumentType.word()).suggests((c,b)->net.minecraft.commands.SharedSuggestionProvider.suggest(SCENES,b)).executes(c->{String name=StringArgumentType.getString(c,"scene");if(!SCENES.contains(name)){c.getSource().sendFailure(Component.literal("Unknown E3 scene: "+name));return 0;}try{var s=spawn(c.getSource().getLevel(),BlockPos.containing(c.getSource().getPosition()).offset(0,0,8),name);c.getSource().sendSuccess(()->Component.literal(s.check()),false);return 1;}catch(IllegalStateException ex){c.getSource().sendFailure(Component.literal(ex.getMessage()));return 0;}})))
        .then(Commands.literal("check").executes(c->{var s=SESSIONS.get(c.getSource().getServer());c.getSource().sendSuccess(()->Component.literal(s==null?"No E3 session":s.check()),false);return 1;}))
        .then(Commands.literal("off").executes(c->{var s=SESSIONS.get(c.getSource().getServer());if(s!=null)s.off();return 1;}))
        .then(Commands.literal("cleanup").executes(c->{cleanup(c.getSource().getServer());return 1;})))));
        ServerTickEvents.END_SERVER_TICK.register(server->{var s=SESSIONS.get(server);if(s!=null){s.observe();if(s.elapsed>=1200)cleanup(server);}});
        ServerLifecycleEvents.SERVER_STOPPED.register(E3aQa::cleanup);
    }
    private static <T extends Mob> T create(ServerLevel l,EntityType<T> t,Vec3 p){var m=t.create(l,EntitySpawnReason.COMMAND);m.snapTo(p,0,0);m.setPersistenceRequired();m.addTag(QaHarness.TAG);m.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);m.setHealth(1000);m.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.IRON_HELMET));return m;}
    public static boolean hasSession(MinecraftServer s){return SESSIONS.containsKey(s);}
    public static Session spawn(ServerLevel l,BlockPos o,String scene){if(!SCENES.contains(scene))throw new IllegalStateException("Unknown E3 scene");if(E4Qa.hasSession(l.getServer())||SESSIONS.containsKey(l.getServer())||QaHarness.hasSession(l.getServer())||E3bQa.hasSession(l.getServer())||E3cQa.hasSession(l.getServer()))throw new IllegalStateException("Cleanup the current QA session first");
        if(l.getDifficulty()==net.minecraft.world.Difficulty.PEACEFUL)throw new IllegalStateException("Use non-peaceful difficulty");
        for(int x=-12;x<=12;x++)for(int z=-12;z<=12;z++)for(int y=-1;y<=6;y++){var p=o.offset(x,y,z);if(!l.hasChunkAt(p)||l.getBlockEntity(p)!=null||!l.getFluidState(p).isEmpty()||y>=0&&!l.getBlockState(p).isAir())throw new IllegalStateException("Need loaded empty 25x25x7 airspace; no block entities/fluids");}
        var s=new Session(l,o,scene);for(int x=-12;x<=12;x++)for(int z=-12;z<=12;z++){s.set(o.offset(x,-1,z),Blocks.STONE.defaultBlockState());s.set(o.offset(x,6,z),Blocks.STONE.defaultBlockState());}
        Vec3 p=Vec3.atBottomCenterOf(o);s.actor=switch(scene){case "relation_zombie","relation_villager"->create(l,EntityTypes.VINDICATOR,p);case "golem_misfire"->create(l,EntityTypes.SNOW_GOLEM,p);case "chicken","boat"->create(l,EntityTypes.ZOMBIE,p);case "camel"->create(l,EntityTypes.HUSK,p);case "trap"->create(l,EntityTypes.SKELETON_HORSE,p);default->create(l,EntityTypes.SKELETON,p);};
        s.actor.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(scene.startsWith("relation")?Items.IRON_AXE:scene.equals("camel")?Items.IRON_SPEAR:scene.equals("horse")||scene.equals("conversion")||scene.equals("rider")?Items.BOW:Items.IRON_SWORD));
        if(!scene.equals("trap")){s.target=scene.equals("relation_villager")?create(l,EntityTypes.ZOMBIE_VILLAGER,p.add(0,0,4)):scene.equals("relation_zombie")?create(l,EntityTypes.ZOMBIE,p.add(0,0,4)):create(l,EntityTypes.IRON_GOLEM,p.add(0,0,scene.equals("golem_misfire")?3:8));s.target.setNoAi(!scene.startsWith("relation"));s.add(s.target);}
        if(List.of("horse","chicken","camel","boat","rider").contains(scene)){Entity v=scene.equals("horse")||scene.equals("rider")?create(l,EntityTypes.SKELETON_HORSE,p):scene.equals("chicken")?create(l,EntityTypes.CHICKEN,p):scene.equals("camel")?create(l,EntityTypes.CAMEL_HUSK,p):EntityTypes.OAK_BOAT.create(l,EntitySpawnReason.COMMAND);
            v.snapTo(p,0,0);v.addTag(QaHarness.TAG);if(v instanceof Chicken c){c.setChickenJockey(true);((Zombie)s.actor).setBaby(true);s.target.snapTo(p.add(0,0,4),0,0);}if(v instanceof SkeletonHorse h)h.setTamed(true);s.add(v);s.actor.startRiding(v,true,false);s.vehicle=v;}
        if(scene.equals("golem_misfire")){s.target.setNoAi(false);var enemy=create(l,EntityTypes.ZOMBIE,p.add(0,0,8));enemy.setNoAi(true);s.add(enemy);s.actor.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);s.target.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);}
        if(scene.equals("trap"))((SkeletonHorse)s.actor).setTrap(true);
        if(scene.equals("boat"))s.actor.setItemSlot(EquipmentSlot.HEAD,ItemStack.EMPTY);
        s.add(s.actor);s.actor.getRandom().setSeed(640002);if(scene.equals("horse")||scene.equals("chicken"))s.actor.getAttached(RidingState.DATA).putBoolean(scene,true);
        if(scene.equals("rider")){((Mob)s.vehicle).setNoAi(true);s.actor.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);}
        // Keep the warning lane intact until the default earned cooldown. Native Goals still run;
        // the real charge impulse moves this fixture without relying on ordinary walking speed.
        if(scene.equals("horse")){s.actor.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);((Mob)s.vehicle).getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);}
        s.initial=s.vehicle==null?s.actor.position():s.vehicle.position();SESSIONS.put(l.getServer(),s);return s;
    }
    public static void cleanup(MinecraftServer server){var s=SESSIONS.remove(server);if(s!=null){s.capture();for(var e:List.copyOf(s.entities))if(!e.isRemoved())e.discard();s.blocks.forEach((p,b)->s.level.setBlockAndUpdate(p,b));}}
    public static final class Session {
        public final ServerLevel level;public final BlockPos origin;public final String scene;public Mob actor,target;public Entity vehicle;public int elapsed;public double moved;private Vec3 initial;private boolean off;
        private final Set<Entity> entities=Collections.newSetFromMap(new IdentityHashMap<>());private final Set<UUID> redirected=new HashSet<>();private final Map<BlockPos,BlockState> blocks=new LinkedHashMap<>();
        Session(ServerLevel l,BlockPos o,String s){level=l;origin=o;scene=s;}
        private void set(BlockPos p,BlockState b){blocks.putIfAbsent(p,level.getBlockState(p));level.setBlockAndUpdate(p,b);}
        private void add(Entity e){entities.add(e);level.addFreshEntity(e);}
        public void observe(){elapsed++;if(elapsed==15&&!off&&scene.equals("boat"))actor.hurtServer(level,actor.damageSources().mobAttack(target),999);
            if(elapsed==15&&!off&&scene.equals("conversion")){actor.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WITHER,100));actor.hurtServer(level,actor.damageSources().generic(),2000);}
            if(scene.equals("rider")){
                var arrows=new ArrayList<net.minecraft.world.entity.projectile.arrow.AbstractArrow>();
                level.getEntities(net.minecraft.world.level.entity.EntityTypeTest.forClass(net.minecraft.world.entity.projectile.arrow.AbstractArrow.class),new AABB(origin).inflate(24),a->a.getOwner()==actor,arrows,16);
                entities.addAll(arrows);
                for(var a:arrows)if(a.tickCount>=2&&a.getDeltaMovement().lengthSqr()>.01&&!redirected.contains(a.getUUID())){
                    var output=net.minecraft.world.level.storage.TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING,level.registryAccess());a.saveWithoutId(output);
                    if(!output.buildResult().getBooleanOr("LeftOwner",false))continue;
                    redirected.add(a.getUUID());a.setNoGravity(true);a.setPos(vehicle.position().add(0,.3,4));a.setDeltaMovement(0,0,-1);
                }
            }
            capture();
            if(initial!=null)moved=Math.max(moved,initial.distanceTo(vehicle==null?actor.position():vehicle.position()));}
        private void capture(){var list=new ArrayList<Mob>();level.getEntities(net.minecraft.world.level.entity.EntityTypeTest.forClass(Mob.class),new AABB(origin).inflate(24),m->actor.getUUID().toString().equals(m.getAttached(RidingState.TRAP_ORIGIN))||RidingState.known(m)&&actor.getUUID().toString().equals(m.getAttached(RidingState.DATA).getStringOr("converted_from","")),list,16);entities.addAll(list);if(off)for(var m:list)m.addTag("buildupmobtweaks:vanilla_ai");}
        public void off(){off=true;for(var e:entities)if(e instanceof Mob m)m.addTag("buildupmobtweaks:vanilla_ai");}
        public int returnedArrows(){return redirected.size();}
        public String check(){var r=RidingState.known(actor)?RidingState.runtime(actor):null;var relation=RelationBehavior.supported(actor)?RelationBehavior.state(actor):null;
            return "E3 scene="+scene+" ticks="+elapsed+" actor="+actor.getUUID()+" target="+(actor.getTarget()==null?"none":actor.getTarget().getUUID())+" vehicle="+(vehicle==null?"none":vehicle.getUUID())+" off="+off+" noAI="+actor.isNoAi()+" using="+actor.isUsingItem()+" selectionCooldown="+actor.getAttached(RidingState.DATA)+" nativeGoals="+actor.getGoalSelector().getAvailableGoals().stream().filter(g->g.isRunning()).map(g->g.getGoal().getClass().getSimpleName()).toList()+" phase="+(r==null?"native":r.phase)+" pounces="+(r==null?0:r.pounces)+" charges="+(r==null?0:r.charges)+" hits="+(r==null?0:r.hits)+" circles="+(r==null?0:r.circles)+" escapes="+(r==null?0:r.escapes)+" returnedArrows="+returnedArrows()+" projectileSkips="+(r==null?0:r.projectileSkips)+" vehicleHP="+(vehicle instanceof net.minecraft.world.entity.LivingEntity v?v.getHealth():"none")+" acquisitions="+(relation==null?0:relation.acquisitions)+" queries="+(relation==null?0:relation.queries)+" moved="+moved+" targetTarget="+(target==null||target.getTarget()==null?"none":target.getTarget().getUUID())+" targetLastHurt="+(target==null||target.getLastHurtByMob()==null?"none":target.getLastHurtByMob().getUUID())+" targetHP="+(target==null?"none":target.getHealth())+" ownedEntities="+entities.size()+" reason="+(scene.equals("trap")&&((SkeletonHorse)actor).isTrap()?"waiting_for_nearby_alive_player":actor.isRemoved()?"actor_removed_or_converted":"observe_native_ticks");
        }
    }
    private E3aQa(){}
}
