package com.davidblackcn.buildupmobtweaks.qa;

import com.davidblackcn.buildupmobtweaks.rebuild.ecology.*;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import java.util.*;

/** Separate developer mod: fixture inputs only; no production action/Goal/tick invocation. */
public final class E3bQa {
    public static final List<String> SCENES=List.of("ink","gaze","chicken_escape","group_panic","pet_recovery","cat_dodge","horn","trader_defense","sniffer");
    private static final Map<MinecraftServer,Session> SESSIONS=new IdentityHashMap<>();
    public static boolean hasSession(MinecraftServer s){return SESSIONS.containsKey(s);}
    public static void register(){
        CommandRegistrationCallback.EVENT.register((d,a,e)->d.register(Commands.literal("buildupqa").requires(Commands.hasPermission(Commands.LEVEL_OWNERS)).then(Commands.literal("e3b").requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
            .then(Commands.literal("spawn").then(Commands.argument("scene",StringArgumentType.word()).suggests((c,b)->net.minecraft.commands.SharedSuggestionProvider.suggest(SCENES,b)).executes(c->{try{var s=spawn(c.getSource().getLevel(),BlockPos.containing(c.getSource().getPosition()).offset(0,0,8),StringArgumentType.getString(c,"scene"),c.getSource().getPlayer());c.getSource().sendSuccess(()->Component.literal(s.check()),false);return 1;}catch(IllegalStateException ex){c.getSource().sendFailure(Component.literal(ex.getMessage()));return 0;}})))
            .then(Commands.literal("check").executes(c->{var s=SESSIONS.get(c.getSource().getServer());c.getSource().sendSuccess(()->Component.literal(s==null?"No E3-b session":s.check()),false);return 1;}))
            .then(Commands.literal("off").executes(c->{var s=SESSIONS.get(c.getSource().getServer());if(s!=null)for(var entity:s.entities)if(entity instanceof Mob m)m.addTag("buildupmobtweaks:vanilla_ai");return 1;}))
            .then(Commands.literal("cleanup").executes(c->{cleanup(c.getSource().getServer());return 1;})))));
        ServerTickEvents.END_SERVER_TICK.register(server->{var s=SESSIONS.get(server);if(s!=null){s.observe();if(s.elapsed>=1200)cleanup(server);}});
        ServerLifecycleEvents.SERVER_STOPPED.register(E3bQa::cleanup);
    }
    public static Session spawn(ServerLevel l,BlockPos o,String scene,ServerPlayer owner){
        if(!SCENES.contains(scene))throw new IllegalStateException("Unknown E3-b scene");
        if(hasSession(l.getServer())||QaHarness.hasSession(l.getServer())||E3aQa.hasSession(l.getServer())||E3cQa.hasSession(l.getServer()))throw new IllegalStateException("One session/server; cleanup first");
        for(int x=-12;x<=12;x++)for(int z=-12;z<=12;z++)for(int y=-1;y<=6;y++){var p=o.offset(x,y,z);if(!l.hasChunkAt(p)||l.getBlockEntity(p)!=null||!l.getFluidState(p).isEmpty()||y>=0&&!l.getBlockState(p).isAir())throw new IllegalStateException("Need loaded empty 25x25x7 airspace without fluids/block entities");}
        var s=new Session(l,o,scene);for(int x=-12;x<=12;x++)for(int z=-12;z<=12;z++){s.set(o.offset(x,-1,z),Blocks.STONE.defaultBlockState());s.set(o.offset(x,6,z),Blocks.STONE.defaultBlockState());}
        if(scene.equals("ink")||scene.equals("gaze")){for(int x=-3;x<=3;x++)for(int z=-3;z<=3;z++)for(int y=0;y<=2;y++)s.set(o.offset(x,y,z),Math.abs(x)==3||Math.abs(z)==3?Blocks.GLASS.defaultBlockState():Blocks.WATER.defaultBlockState());}
        EntityType<? extends Mob> type=switch(scene){case "ink"->EntityTypes.SQUID;case "gaze"->EntityTypes.GLOW_SQUID;case "chicken_escape"->EntityTypes.CHICKEN;case "group_panic"->EntityTypes.COW;case "pet_recovery"->EntityTypes.WOLF;case "cat_dodge"->EntityTypes.CAT;case "horn"->EntityTypes.ZOMBIE;case "trader_defense"->EntityTypes.WANDERING_TRADER;default->EntityTypes.SNIFFER;};
        s.actor=s.add(type,Vec3.atBottomCenterOf(o));s.initial=s.actor.position();
        s.target=scene.equals("chicken_escape")?s.add(EntityTypes.ZOMBIE,Vec3.atBottomCenterOf(o.offset(0,0,4))):(scene.equals("group_panic")||scene.equals("horn"))?s.add(EntityTypes.COW,Vec3.atBottomCenterOf(o.offset(0,0,4))):s.add(EntityTypes.IRON_GOLEM,Vec3.atBottomCenterOf(o.offset(0,0,4)));s.target.setNoAi(!scene.equals("group_panic"));
        if(s.actor instanceof TamableAnimal pet&&owner!=null){pet.setTame(true,false);pet.setOwner(owner);pet.setOrderedToSit(scene.equals("pet_recovery"));}
        if(scene.equals("sniffer"))s.set(o.below(),Blocks.IRON_ORE.defaultBlockState());
        SESSIONS.put(l.getServer(),s);return s;
    }
    public static void cleanup(MinecraftServer server){var s=SESSIONS.remove(server);if(s!=null){for(var e:s.entities)if(!e.isRemoved())e.discard();s.blocks.forEach((p,b)->s.level.setBlockAndUpdate(p,b));}}
    public static final class Session {
        public final ServerLevel level;public final BlockPos origin;public final String scene;public Mob actor,target;public int elapsed;private Vec3 initial;private double moved;private boolean fired;
        private final Set<Entity> entities=Collections.newSetFromMap(new IdentityHashMap<>());private final Map<BlockPos,BlockState> blocks=new LinkedHashMap<>();
        Session(ServerLevel l,BlockPos o,String s){level=l;origin=o;scene=s;}
        private void set(BlockPos p,BlockState b){blocks.putIfAbsent(p,level.getBlockState(p));level.setBlockAndUpdate(p,b);}
        private <T extends Mob> T add(EntityType<T> type,Vec3 p){var m=type.create(level,EntitySpawnReason.COMMAND);m.snapTo(p,0,0);m.setPersistenceRequired();m.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);m.setHealth(1000);m.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.IRON_HELMET));m.addTag(QaHarness.TAG);entities.add(m);level.addFreshEntity(m);return m;}
        public void observe(){elapsed++;moved=Math.max(moved,actor.position().distanceTo(initial));
            if(elapsed==15&&(scene.equals("ink")||scene.equals("group_panic")||scene.equals("pet_recovery")||scene.equals("trader_defense")))actor.hurtServer(level,scene.equals("pet_recovery")?actor.damageSources().generic():actor.damageSources().mobAttack(target),2);
            if(elapsed==15&&scene.equals("cat_dodge")&&!fired){fired=true;var a=new Arrow(level,target,Items.ARROW.getDefaultInstance(),null);a.setNoGravity(true);a.setPos(actor.getBoundingBox().getCenter().add(0,0,4));a.setDeltaMovement(0,0,-.5);a.pickup=net.minecraft.world.entity.projectile.arrow.AbstractArrow.Pickup.DISALLOWED;entities.add(a);level.addFreshEntity(a);}
        }
        public String check(){var r=EcologyState.runtime(actor);return "E3B scene="+scene+" ticks="+elapsed+" actor="+actor.getUUID()+" hp="+actor.getHealth()+" targetHP="+target.getHealth()+" targetEscapes="+EcologyState.runtime(target).escapes+" actualEffects="+actor.getActiveEffects()+" targetEffects="+target.getActiveEffects()+" moved="+moved+" ink="+r.ink+" gazes="+r.gazes+" escapes="+r.escapes+" shares="+r.shares+" heals="+r.heals+" dodges="+r.dodges+" defenses="+r.defenses+" horns="+r.horns+" queries="+r.queries+" search="+r.search+" searchQueries="+r.searchQueries+" cooldowns="+actor.getAttached(EcologyState.DATA)+" nativeGoals="+actor.getGoalSelector().getAvailableGoals().stream().filter(g->g.isRunning()).map(g->g.getGoal().getClass().getSimpleName()).toList()+" input="+(scene.equals("gaze")?"survival_player_look_at_squid":scene.equals("horn")?"survival_player_blow_real_horn":scene.equals("sniffer")?"survival_player_sneak_iron_ingot_right_click":scene.equals("pet_recovery")?"spawn_from_player_for_owner":"fixture_native_ticks");}
    }
    private E3bQa(){}
}
