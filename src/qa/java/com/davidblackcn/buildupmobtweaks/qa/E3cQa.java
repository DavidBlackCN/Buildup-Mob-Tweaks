package com.davidblackcn.buildupmobtweaks.qa;
import com.davidblackcn.buildupmobtweaks.rebuild.ability.*;
import com.davidblackcn.buildupmobtweaks.rebuild.remaining.*;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.entity.animal.golem.SnowGolem;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import java.util.*;

/** Independent deterministic inputs, never a direct skill/Goal/tick call. */
public final class E3cQa {
    public static final List<String> SCENES=List.of("pumpkin","snow","snow_sweep","baby","baby_wither","shotgun");
    private static final Map<MinecraftServer,Session> SESSIONS=new IdentityHashMap<>();
    public static boolean hasSession(MinecraftServer s){return SESSIONS.containsKey(s);}
    public static void register(){
        CommandRegistrationCallback.EVENT.register((d,a,e)->d.register(Commands.literal("buildupqa").requires(Commands.hasPermission(Commands.LEVEL_OWNERS)).then(Commands.literal("e3c")
            .then(Commands.literal("spawn").then(Commands.argument("scene",StringArgumentType.word()).suggests((c,b)->net.minecraft.commands.SharedSuggestionProvider.suggest(SCENES,b)).executes(c->{try{var s=spawn(c.getSource().getLevel(),BlockPos.containing(c.getSource().getPosition()).offset(0,0,8),StringArgumentType.getString(c,"scene"));c.getSource().sendSuccess(()->Component.literal(s.check()),false);return 1;}catch(IllegalStateException ex){c.getSource().sendFailure(Component.literal(ex.getMessage()));return 0;}})))
            .then(Commands.literal("check").executes(c->{var s=SESSIONS.get(c.getSource().getServer());c.getSource().sendSuccess(()->Component.literal(s==null?"No E3-c session":s.check()),false);return 1;}))
            .then(Commands.literal("off").executes(c->{var s=SESSIONS.get(c.getSource().getServer());if(s!=null)s.actor.addTag("buildupmobtweaks:vanilla_ai");return 1;}))
            .then(Commands.literal("cleanup").executes(c->{cleanup(c.getSource().getServer());return 1;})))));
        ServerTickEvents.END_SERVER_TICK.register(server->{var s=SESSIONS.get(server);if(s!=null){s.observe();if(s.elapsed>=1200)cleanup(server);}});
        ServerLifecycleEvents.SERVER_STOPPED.register(E3cQa::cleanup);
    }
    public static Session spawn(ServerLevel l,BlockPos o,String scene){
        if(!SCENES.contains(scene))throw new IllegalStateException("Unknown E3-c scene");
        if(E4Qa.hasSession(l.getServer())||hasSession(l.getServer())||QaHarness.hasSession(l.getServer())||E3aQa.hasSession(l.getServer())||E3bQa.hasSession(l.getServer()))throw new IllegalStateException("One session/server; cleanup first");
        if(l.getDifficulty()==net.minecraft.world.Difficulty.PEACEFUL)throw new IllegalStateException("Use non-peaceful difficulty; QA does not change world rules");
        for(int x=-12;x<=12;x++)for(int z=-12;z<=12;z++)for(int y=-1;y<=6;y++){var p=o.offset(x,y,z);if(!l.hasChunkAt(p)||l.getBlockEntity(p)!=null||!l.getFluidState(p).isEmpty()||y>=0&&!l.getBlockState(p).isAir())throw new IllegalStateException("Need loaded empty 25x25x7 airspace without fluids/block entities");}
        var s=new Session(l,o,scene);for(int x=-12;x<=12;x++)for(int z=-12;z<=12;z++){s.set(o.offset(x,-1,z),Blocks.STONE.defaultBlockState());s.set(o.offset(x,6,z),Blocks.STONE.defaultBlockState());}
        var type=scene.equals("shotgun")?EntityTypes.BREEZE:scene.equals("baby")?EntityTypes.SKELETON:scene.equals("baby_wither")?EntityTypes.WITHER_SKELETON:EntityTypes.SNOW_GOLEM;
        s.actor=s.add(type,Vec3.atBottomCenterOf(o),scene.startsWith("baby"));s.actor.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);
        s.actor.addTag("buildupmobtweaks:disable_skeleton_weapon_switching");s.actor.addTag("buildupmobtweaks:disable_skeleton_sniping");
        if(s.actor instanceof SnowGolem snow){snow.setPumpkin(scene.equals("pumpkin"));if(scene.equals("pumpkin"))snow.setNoAi(true);}
        if(scene.startsWith("baby"))s.actor.setItemSlot(EquipmentSlot.MAINHAND,Items.BOW.getDefaultInstance());
        if(!scene.equals("pumpkin")){EntityType<? extends Mob> targetType=scene.startsWith("snow")?EntityTypes.BLAZE:EntityTypes.IRON_GOLEM;s.target=s.add(targetType,Vec3.atBottomCenterOf(o.offset(0,0,7)),false);s.target.setNoAi(true);s.target.setNoGravity(true);
            if(scene.equals("snow_sweep"))for(int i=0;i<3;i++){var a=s.add(EntityTypes.ZOMBIE,Vec3.atBottomCenterOf(o.offset(-3+3*i,0,8)),false);a.setNoAi(true);a.setTarget(s.actor);}
        }
        SESSIONS.put(l.getServer(),s);return s;
    }
    public static void cleanup(MinecraftServer server){var s=SESSIONS.remove(server);if(s!=null){for(var e:s.entities)if(!e.isRemoved())e.discard();s.blocks.forEach((p,b)->s.level.setBlockAndUpdate(p,b));}}
    public static final class Session {
        public final ServerLevel level;public final BlockPos origin;public final String scene;public Mob actor,target;public int elapsed;
        private final Set<Entity> entities=Collections.newSetFromMap(new IdentityHashMap<>());private final Set<UUID> projectileIds=new HashSet<>();private final Map<BlockPos,BlockState> blocks=new LinkedHashMap<>();
        Session(ServerLevel l,BlockPos o,String s){level=l;origin=o;scene=s;}
        private void set(BlockPos p,BlockState b){blocks.putIfAbsent(p,level.getBlockState(p));level.setBlockAndUpdate(p,b);}
        private <T extends Mob>T add(EntityType<T> t,Vec3 p,boolean baby){var m=t.create(level,EntitySpawnReason.COMMAND);m.snapTo(p,0,0);m.setPersistenceRequired();m.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);m.setHealth(1000);m.setItemSlot(EquipmentSlot.HEAD,Items.IRON_HELMET.getDefaultInstance());m.addTag(QaHarness.TAG);
            if(baby){var d=new CompoundTag();d.putInt("version",1);d.putBoolean("baby_choice",true);m.setAttached(AbilityState.DATA,d);}entities.add(m);level.addFreshEntity(m);return m;}
        public void observe(){elapsed++;for(var p:level.getEntitiesOfClass(Projectile.class,actor.getBoundingBox().inflate(40),p->p.getOwner()==actor)){entities.add(p);projectileIds.add(p.getUUID());}}
        public String check(){var r=AbilityState.runtime(actor);return "E3C scene="+scene+" ticks="+elapsed+" actor="+actor.getUUID()+" target="+(target==null?"none":target.getUUID())+" targetHP="+(target==null?0:target.getHealth())+" baby="+actor.isBaby()+" width="+actor.getBbWidth()+" eye="+actor.getEyeHeight()+" armor="+actor.getArmorValue()+" prepared="+r.prepared+" released="+r.released+" cancelled="+r.cancelled+" scans="+r.scans+" animation="+actor.getAttachedOrCreate(AbilityState.THROW)+" volleys="+r.volleys+" extra="+r.extra+" actualProjectileIds="+projectileIds.size()+" data="+actor.getAttached(AbilityState.DATA)+" shared="+(RemainingState.known(actor)?actor.getAttached(RemainingState.DATA).getLongOr("breeze_special_ready",0):0)+" native="+(actor instanceof Breeze b?b.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET):actor.getGoalSelector().getAvailableGoals().stream().filter(g->g.isRunning()).map(g->g.getGoal().getClass().getSimpleName()).toList())+" input="+(scene.equals("pumpkin")?"player_shears_then_carved_pumpkin":"fixture_native_ticks");}
    }
    private E3cQa(){}
}
