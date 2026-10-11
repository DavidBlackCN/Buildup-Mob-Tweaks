package com.davidblackcn.buildupmobtweaks.qa;
import com.davidblackcn.buildupmobtweaks.rebuild.general.E4VisualState;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.minecraft.commands.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.monster.zombie.Drowned;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.effect.*;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import java.util.*;

/** Separate QA artifact. Never changes spawn probabilities, player inventory or world rules. */
public final class E4Qa {
    public static final List<String> SCENES=List.of("sneak_cat","sneak_ocelot","sneak_rabbit","stagger","blindness","crawling","invisibility","crouching","glowing","nausea","movement","obstacle","hazard","melee","high_target","evoker","swim","burn","freeze","burn_arrow","vex");
    private static final Map<MinecraftServer,Session> SESSIONS=new IdentityHashMap<>();
    public static boolean hasSession(MinecraftServer s){return SESSIONS.containsKey(s);}
    public static void register(){
        CommandRegistrationCallback.EVENT.register((d,a,e)->d.register(Commands.literal("buildupqa").requires(Commands.hasPermission(Commands.LEVEL_OWNERS)).then(Commands.literal("e4")
            .then(Commands.literal("spawn").then(Commands.argument("scene",StringArgumentType.word()).suggests((c,b)->SharedSuggestionProvider.suggest(SCENES,b)).executes(c->{try{var s=spawn(c.getSource().getLevel(),BlockPos.containing(c.getSource().getPosition()).offset(0,0,8),StringArgumentType.getString(c,"scene"));c.getSource().sendSuccess(()->Component.literal(s.check()),false);return 1;}catch(IllegalStateException ex){c.getSource().sendFailure(Component.literal(ex.getMessage()));return 0;}})))
            .then(Commands.literal("check").executes(c->{var s=SESSIONS.get(c.getSource().getServer());c.getSource().sendSuccess(()->Component.literal(s==null?"No E4 session":s.check()),false);return 1;}))
            .then(Commands.literal("off").executes(c->{var s=SESSIONS.get(c.getSource().getServer());if(s!=null)for(var m:s.actors)m.addTag("buildupmobtweaks:vanilla_ai");return 1;}))
            .then(Commands.literal("cleanup").executes(c->{cleanup(c.getSource().getServer());return 1;})))));
        ServerTickEvents.END_SERVER_TICK.register(server->{var s=SESSIONS.get(server);if(s!=null){s.observe();if(s.elapsed>=1200)cleanup(server);}});
        ServerEntityEvents.ENTITY_LOAD.register((e,l)->{var s=SESSIONS.get(l.getServer());if(s!=null&&e instanceof Projectile p&&s.actors.contains(p.getOwner()))s.projectile(p);});
        ServerLifecycleEvents.SERVER_STOPPED.register(E4Qa::cleanup);
    }
    public static Session spawn(ServerLevel l,BlockPos o,String scene){
        if(!SCENES.contains(scene))throw new IllegalStateException("Unknown E4 scene");
        if(hasSession(l.getServer())||QaHarness.hasSession(l.getServer())||E3aQa.hasSession(l.getServer())||E3bQa.hasSession(l.getServer())||E3cQa.hasSession(l.getServer()))throw new IllegalStateException("One session/server; cleanup first");
        if(l.getDifficulty()==net.minecraft.world.Difficulty.PEACEFUL)throw new IllegalStateException("Use non-peaceful difficulty");
        for(int x=-12;x<=12;x++)for(int z=-12;z<=12;z++)for(int y=-1;y<=6;y++){var p=o.offset(x,y,z);if(!l.hasChunkAt(p)||l.getBlockEntity(p)!=null||!l.getFluidState(p).isEmpty()||y>=0&&!l.getBlockState(p).isAir())throw new IllegalStateException("Need loaded empty 25x25x7 airspace without fluids/block entities");}
        var s=new Session(l,o,scene);SESSIONS.put(l.getServer(),s);try{for(int x=-12;x<=12;x++)for(int z=-12;z<=12;z++){s.set(o.offset(x,-1,z),Blocks.STONE.defaultBlockState());s.set(o.offset(x,6,z),Blocks.STONE.defaultBlockState());}
        EntityType<? extends Mob> type=switch(scene){case "sneak_cat"->EntityTypes.CAT;case "sneak_ocelot"->EntityTypes.OCELOT;case "sneak_rabbit"->EntityTypes.RABBIT;case "evoker"->EntityTypes.EVOKER;case "swim"->EntityTypes.DROWNED;case "vex"->EntityTypes.VEX;case "melee","high_target","burn","freeze"->EntityTypes.ZOMBIE;default->EntityTypes.SKELETON;};
        var m=s.add(type,o);s.actor=m;
        if(m instanceof AbstractSkeleton){m.setItemSlot(EquipmentSlot.MAINHAND,Items.BOW.getDefaultInstance());m.addTag("buildupmobtweaks:disable_skeleton_weapon_switching");m.addTag("buildupmobtweaks:disable_skeleton_sniping");}
        if(scene.equals("blindness"))m.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,1200));
        if(scene.equals("nausea"))m.addEffect(new MobEffectInstance(MobEffects.NAUSEA,1200,64));
        if(scene.equals("melee"))m.addEffect(new MobEffectInstance(MobEffects.HASTE,1200,4));
        if(scene.equals("burn")||scene.equals("burn_arrow"))m.setRemainingFireTicks(1200);
        if(scene.equals("freeze"))m.setTicksFrozen(1200);
        if(scene.equals("burn_arrow"))m.getMainHandItem().enchant(l.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).getOrThrow(net.minecraft.world.item.enchantment.Enchantments.FLAME),1);
        if(scene.equals("swim")){for(int x=-3;x<=3;x++)for(int z=-3;z<=9;z++)for(int y=0;y<=3;y++)s.set(o.offset(x,y,z),(x==-3||x==3||z==-3||z==9?Blocks.GLASS:Blocks.WATER).defaultBlockState());((Drowned)m).setItemSlot(EquipmentSlot.MAINHAND,Items.TRIDENT.getDefaultInstance());}
        boolean manual=scene.startsWith("sneak_")||List.of("blindness","crawling","invisibility","crouching","glowing","vex","high_target").contains(scene);
        if(!manual){var t=s.add(EntityTypes.IRON_GOLEM,o.offset(0,0,8));t.setNoAi(true);s.target=t;}
        if(scene.equals("stagger")){
            // Freeze ordinary walking only, retaining native selectors/use/release. This keeps
            // the shared-target demonstration from turning into vanilla friendly-fire retaliation.
            m.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);
            m.snapTo(net.minecraft.world.phys.Vec3.atBottomCenterOf(o.offset(-7,0,0)),0,0);
            for(int i=1;i<6;i++){var a=s.add(EntityTypes.SKELETON,o.offset(i*3-7,0,0));a.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);a.setItemSlot(EquipmentSlot.MAINHAND,Items.BOW.getDefaultInstance());a.addTag("buildupmobtweaks:disable_skeleton_weapon_switching");a.addTag("buildupmobtweaks:disable_skeleton_sniping");}
        }
        if(scene.equals("high_target"))s.set(o.offset(0,1,1),Blocks.STONE.defaultBlockState());
        if(scene.equals("obstacle"))for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++)if(Math.abs(x)==2||Math.abs(z)==2)s.set(o.offset(x,0,z),Blocks.STONE.defaultBlockState());
        if(scene.equals("hazard"))for(int x=-4;x<=4;x++)s.set(o.offset(x,0,-3),Blocks.FIRE.defaultBlockState());
        return s;}catch(RuntimeException ex){cleanup(l.getServer());throw ex;}
    }
    public static void cleanup(MinecraftServer server){var s=SESSIONS.remove(server);if(s!=null){for(var e:s.entities)if(!e.isRemoved())e.discard();s.blocks.forEach((p,b)->s.level.setBlockAndUpdate(p,b));}}
    public static final class Session {
        public final ServerLevel level;public final BlockPos origin;public final String scene;public Mob actor,target;public int elapsed;
        private final List<Mob> actors=new ArrayList<>();private final Set<Entity> entities=Collections.newSetFromMap(new IdentityHashMap<>());private final Set<UUID> shots=new HashSet<>();private final Map<BlockPos,BlockState> blocks=new LinkedHashMap<>();
        private final List<String> releases=new ArrayList<>();
        Session(ServerLevel l,BlockPos p,String s){level=l;origin=p;scene=s;}
        private void set(BlockPos p,BlockState b){blocks.putIfAbsent(p,level.getBlockState(p));level.setBlockAndUpdate(p,b);}
        private <T extends Mob> T add(EntityType<T> type,BlockPos p){var m=type.create(level,EntitySpawnReason.COMMAND);m.snapTo(net.minecraft.world.phys.Vec3.atBottomCenterOf(p),0,0);m.setPersistenceRequired();m.setItemSlot(EquipmentSlot.HEAD,Items.IRON_HELMET.getDefaultInstance());m.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);m.setHealth(1000);if(!level.addFreshEntity(m))throw new IllegalStateException("Actor rejected");actors.add(m);entities.add(m);return m;}
        void observe(){elapsed++;if(scene.equals("freeze"))actor.setTicksFrozen(1200);
            var found=new ArrayList<Projectile>();level.getEntities(net.minecraft.world.level.entity.EntityTypeTest.forClass(Projectile.class),actor.getBoundingBox().inflate(24),p->actors.contains(p.getOwner()),found,128);
            for(var p:found)entities.add(p);
        }
        void projectile(Projectile p){entities.add(p);if(shots.add(p.getUUID())&&releases.size()<128)releases.add(elapsed+":"+p.getOwner().getUUID()+":"+p.getDeltaMovement());}
        public String check(){return "E4 scene="+scene+" elapsed="+elapsed+" actual releases="+releases+" actors="+actors.stream().map(m->m.getUUID()+" hp="+m.getHealth()+" pos="+m.position()+" target="+(m.getTarget()==null?"none":m.getTarget().getUUID())+" draw="+m.getTicksUsingItem()+" visual="+m.getAttachedOrCreate(E4VisualState.FLAGS)+" goals="+m.getGoalSelector().getAvailableGoals().stream().filter(g->g.isRunning()).map(g->g.getGoal().getClass().getSimpleName()).toList()).toList()+"; manual scenes: use survival, crouch/stand/effects and bow yourself; check is observation, not PASS";}
    }
    private E4Qa(){}
}
