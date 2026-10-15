package com.davidblackcn.buildupmobtweaks.villager.profession;

import com.davidblackcn.buildupmobtweaks.villager.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.*;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import java.util.*;
import java.util.function.Supplier;

/** Equal-priority implementations of the same activity pause that activity, regardless of order. */
public final class ProfessionRegistry {
    public record Binding(Identifier adapter,ProfessionProfile profile,ProfessionAdapter.Entry entry){}
    private static final Map<Identifier,ProfessionAdapter> ADAPTERS=new LinkedHashMap<>();
    private static final Map<ResourceKey<VillagerProfession>,List<Binding>> CACHE=new HashMap<>();
    public static void register(ProfessionAdapter adapter){
        if(ADAPTERS.putIfAbsent(adapter.id(),adapter)!=null)throw new IllegalArgumentException("Duplicate profession adapter: "+adapter.id());
        CACHE.clear();
    }
    public static boolean optional(String modId,String exactVersion,Supplier<ProfessionAdapter> factory){
        var mod=FabricLoader.getInstance().getModContainer(modId);
        if(mod.isEmpty()||!mod.get().getMetadata().getVersion().getFriendlyString().equals(exactVersion))return false;
        register(factory.get());return true;
    }
    public static List<Binding> activities(ResourceKey<VillagerProfession> profession){return CACHE.computeIfAbsent(profession,ProfessionRegistry::resolve);}
    private static List<Binding> resolve(ResourceKey<VillagerProfession> profession){
        var groups=new TreeMap<String,List<Binding>>();
        for(var a:ADAPTERS.values())if(a.profile().profession().equals(profession))for(var e:a.activities())
            groups.computeIfAbsent(e.id().toString(),k->new ArrayList<>()).add(new Binding(a.id(),a.profile(),e));
        var out=new ArrayList<Binding>();
        for(var group:groups.values()){
            int priority=group.stream().mapToInt(b->b.entry().priority()).max().orElseThrow();
            var best=group.stream().filter(b->b.entry().priority()==priority).toList();
            if(best.size()==1)out.add(best.getFirst());
        }
        out.sort(Comparator.comparingInt((Binding b)->b.entry().priority()).reversed().thenComparing(b->b.entry().id().toString()));
        return List.copyOf(out);
    }
    public static List<ProfessionAdapter> profiles(ResourceKey<VillagerProfession> profession){return ADAPTERS.values().stream().filter(a->a.profile().profession().equals(profession)).toList();}
    public static boolean ambiguous(ResourceKey<VillagerProfession> profession){
        var groups=new HashMap<Identifier,List<Integer>>();for(var a:profiles(profession))for(var e:a.activities())groups.computeIfAbsent(e.id(),k->new ArrayList<>()).add(e.priority());return groups.values().stream().anyMatch(g->{int max=Collections.max(g);return g.stream().filter(i->i==max).count()>1;});
    }
    private ProfessionRegistry(){}
}
