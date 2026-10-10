/* Mob AI Tweaks search intention adaptation: Copyright (c) 2024 N0t_UN_Owen, MIT. See NOTICE.md. */
package com.davidblackcn.buildupmobtweaks.rebuild.ecology;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import java.util.*;

/** Main-thread bounded requests. getChunkNow is the sole world-query gateway. */
public final class SnifferSearch {
    private record Rule(TagKey<Item> items, TagKey<Block> blocks, String biome) {}
    private static final List<Rule> RULES = rules();
    private static final Map<MinecraftServer,ArrayDeque<Request>> REQUESTS = new IdentityHashMap<>();
    public static final int GLOBAL_BUDGET=64, MAX_REQUESTS=8, TIMEOUT=100, MAX_BLOCK_QUERIES=2601;
    private static List<Rule> rules(){
        var rules=new ArrayList<Rule>();
        for(String ore:List.of("iron","gold","copper","coal","diamond","emerald","redstone","lapis"))rules.add(new Rule(TagKey.create(Registries.ITEM,BuildupMobTweaks.id("sniffer_"+ore+"_items")),TagKey.create(Registries.BLOCK,BuildupMobTweaks.id("sniffer_"+ore+"_targets")),null));
        String[][] biomes={{"acacia_sapling","#is_savanna"},{"bamboo","bamboo_jungle"},{"birch_sapling","birch_forest"},{"brown_mushroom","swamp"},{"cherry_sapling","cherry_grove"},{"dark_oak_sapling","dark_forest"},{"mangrove_roots","mangrove_swamp"},{"oak_sapling","forest"},{"red_mushroom","mushroom_fields"},{"red_sand","#is_badlands"},{"sand","desert"},{"spruce_sapling","#is_taiga"},{"sunflower","plains"}};
        for(var pair:biomes)rules.add(new Rule(TagKey.create(Registries.ITEM,BuildupMobTweaks.id("sniffer_"+pair[0]+"_items")),null,pair[1]));
        return List.copyOf(rules);
    }
    public static boolean request(Sniffer s,Player p,InteractionHand hand){
        var behavior=EcologyBehavior.instance();
        if(!(s.level() instanceof ServerLevel l)||behavior==null||!behavior.enabled(s,FeatureId.SNIFFER_SEARCH)||!EcologyBehavior.playerValid(p)||!p.isShiftKeyDown()||s.isPassenger()||s.isBaby())return false;
        var rule=RULES.stream().filter(r->p.getItemInHand(hand).is(r.items)).findFirst().orElse(null);if(rule==null)return false;
        cancel(s,"replaced");
        if(!EcologyState.ready(s,"search_after")){p.sendSystemMessage(Component.translatable("buildupmobtweaks.sniffer.cooldown"));return true;}
        var queue=REQUESTS.computeIfAbsent(l.getServer(),k->new ArrayDeque<>());
        if(queue.size()>=MAX_REQUESTS){p.sendSystemMessage(Component.translatable("buildupmobtweaks.sniffer.busy"));EcologyState.runtime(s).search="queue_full";return true;}
        EcologyState.cooldown(s,"search_after",100);EcologyState.runtime(s).search="searching";EcologyState.runtime(s).searchQueries=0;
        queue.addLast(new Request(s,p,hand,p.getItemInHand(hand).getItem(),rule,s.blockPosition(),l.getGameTime()));
        p.sendSystemMessage(Component.translatable("buildupmobtweaks.sniffer.started"));return true;
    }
    public static void cancel(Mob m,String reason){
        if(m.level().getServer()!=null){var q=REQUESTS.get(m.level().getServer());if(q!=null)q.removeIf(r->r.sniffer==m);}
        if(EcologyState.known(m)){var r=EcologyState.runtime(m);if(r.search.equals("searching")||r.pointer!=null)r.search=reason;r.pointer=null;}
    }
    public static void stop(MinecraftServer server){REQUESTS.remove(server);}
    public static int active(MinecraftServer server){var q=REQUESTS.get(server);return q==null?0:q.size();}
    public static void tick(MinecraftServer server){
        var q=REQUESTS.get(server);if(q==null||q.isEmpty())return;
        int budget=GLOBAL_BUDGET,turns=q.size();
        while(budget>0&&turns-->0&&!q.isEmpty()){
            var r=q.removeFirst();var s=r.sniffer;var state=EcologyState.runtime(s);
            if(!r.valid()){state.search="cancelled";continue;}
            if(s.level().getGameTime()-r.started>=TIMEOUT){finish(r,null,"timeout");continue;}
            int count=Math.min(budget,r.rule.blocks==null?8:32);boolean complete=false;
            for(int i=0;i<count;i++){
                if(r.index>=r.limit()){finish(r,null,"not_found_loaded_area");complete=true;break;}
                var pos=r.next();r.index++;budget--;state.searchQueries++;
                var level=(ServerLevel)s.level();var chunk=level.getChunkSource().getChunkNow(pos.getX()>>4,pos.getZ()>>4);
                if(chunk==null||pos.getY()<level.getMinY()||pos.getY()>=level.getMaxY())continue;
                boolean found;
                if(r.rule.blocks!=null)found=chunk.getBlockState(pos).is(r.rule.blocks);
                else {var biome=chunk.getNoiseBiome(pos.getX()>>2,pos.getY()>>2,pos.getZ()>>2);String name=r.rule.biome;
                    found=name.startsWith("#")?biome.is(TagKey.create(Registries.BIOME,Identifier.withDefaultNamespace(name.substring(1)))):biome.is(Identifier.withDefaultNamespace(name));}
                if(found){finish(r,pos,"found");complete=true;break;}
            }
            if(!complete)q.addLast(r);
        }
    }
    private static void finish(Request r,BlockPos found,String result){
        var state=EcologyState.runtime(r.sniffer);state.search=result;
        if(found!=null){state.pointer=net.minecraft.world.phys.Vec3.atCenterOf(found);state.pointerUntil=r.sniffer.level().getGameTime()+60;}
        r.player.sendSystemMessage(Component.translatable("buildupmobtweaks.sniffer."+(found!=null?"found":"not_found")));
    }
    public static void point(Sniffer s){var r=EcologyState.runtime(s);if(r.pointer==null)return;
        if(!EcologyBehavior.instance().enabled(s,FeatureId.SNIFFER_SEARCH)||s.level().getGameTime()>=r.pointerUntil){r.pointer=null;return;}
        s.getLookControl().setLookAt(r.pointer.x,r.pointer.y,r.pointer.z);
        if(s.tickCount%5==0&&s.level() instanceof ServerLevel l){var direction=r.pointer.subtract(s.getEyePosition()).normalize();var p=s.getEyePosition().add(direction.scale(2));l.sendParticles(ParticleTypes.HAPPY_VILLAGER,p.x,p.y,p.z,2,.1,.1,.1,0);}
    }
    private static final class Request {
        final Sniffer sniffer;final Player player;final InteractionHand hand;final Item item;final Rule rule;final BlockPos origin;final long started;int index;
        Request(Sniffer s,Player p,InteractionHand h,Item i,Rule r,BlockPos o,long t){sniffer=s;player=p;hand=h;item=i;rule=r;origin=o;started=t;}
        boolean valid(){return EcologyBehavior.instance().enabled(sniffer,FeatureId.SNIFFER_SEARCH)&&EcologyBehavior.playerValid(player)&&player.level()==sniffer.level()&&player.isShiftKeyDown()&&player.getItemInHand(hand).is(item)&&player.distanceToSqr(sniffer)<=64&&!sniffer.isPassenger()&&sniffer.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(origin))<=16;}
        int limit(){return rule.blocks==null?289:MAX_BLOCK_QUERIES;}
        BlockPos next(){
            // Center-out square rings for each depth; at most the 2601 samples (8 blocks horizontally, 8 below).
            int depth=rule.blocks==null?0:index/289,n=index%289,spacing=rule.blocks==null?16:1;if(n==0)return origin.below(depth);
            int ring=(int)Math.ceil((Math.sqrt(n+1)-1)/2),base=(2*ring-1)*(2*ring-1),edge=(n-base)/(2*ring),step=(n-base)%(2*ring);
            int x=0,z=0;switch(edge){case 0->{x=-ring+step;z=-ring;}case 1->{x=ring;z=-ring+step;}case 2->{x=ring-step;z=ring;}default->{x=-ring;z=ring-step;}}
            return origin.offset(x*spacing,-depth,z*spacing);
        }
    }
    private SnifferSearch(){}
}
