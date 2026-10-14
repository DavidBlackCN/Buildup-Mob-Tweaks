package com.davidblackcn.buildupmobtweaks.villager.farmer;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import com.google.gson.*;
import net.fabricmc.fabric.api.resource.v1.*;
import net.fabricmc.fabric.api.resource.v1.reloader.SimpleReloadListener;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.resources.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.item.*;
import java.util.*;
import java.io.IOException;

/** Each server owns an immutable resolved resource snapshot. Same-crop ambiguity disables all contenders. */
public final class CropProfiles extends SimpleReloadListener<List<CropProfiles.Raw>> {
    private static final DataResourceStore.Key<Loaded> KEY=new DataResourceStore.Key<>();
    private static final FileToIdConverter FILES=FileToIdConverter.json("buildupmobtweaks/crop_profile");
    public record Raw(Identifier id,JsonObject json,String source){}
    public record Snapshot(Map<Identifier,CropProfile> rules,Map<Block,CropProfile> byCrop,int rejected,UUID generation){
        public Snapshot{rules=Map.copyOf(rules);byCrop=Map.copyOf(byCrop);}
    }
    public static void register(){
        DataResourceLoader.get().registerReloadListener(BuildupMobTweaks.id("crop_profiles"),new CropProfiles());
        ServerLifecycleEvents.SERVER_STARTING.register(CropProfiles::get);
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((s,m,success)->{if(success){FarmerModule.stopForReload(s);get(s);}});
    }
    public static Snapshot get(MinecraftServer server){return server.getOrThrow(KEY).snapshot();}
    protected List<Raw> prepare(SharedState state){
        var out=new ArrayList<Raw>();
        for(var e:FILES.listMatchingResources(state.resourceManager()).entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()){
            try(var reader=e.getValue().openAsReader()){out.add(new Raw(FILES.fileToId(e.getKey()),JsonParser.parseReader(reader).getAsJsonObject(),e.getValue().sourcePackId()));}
            catch(IOException|IllegalStateException|JsonParseException error){BuildupMobTweaks.LOGGER.error("Crop profile parse rejected: {}",e.getKey(),error);}
        }return List.copyOf(out);
    }
    protected void apply(List<Raw> prepared,SharedState state){state.get(DataResourceLoader.DATA_RESOURCE_STORE_KEY).put(KEY,new Loaded(prepared));}
    private static final class Loaded{
        final List<Raw> raw;Snapshot cached;Loaded(List<Raw> raw){this.raw=raw;}
        Snapshot snapshot(){if(cached==null)cached=resolve(raw);return cached;}
    }
    public static Snapshot resolve(List<Raw> raw){
        var rules=new LinkedHashMap<Identifier,CropProfile>();int rejected=0;
        for(var r:raw)try{var c=decode(r.id(),r.json());if(rules.putIfAbsent(c.id(),c)!=null)throw new IllegalArgumentException("Duplicate rule ID");}
        catch(IllegalArgumentException|IllegalStateException error){rejected++;BuildupMobTweaks.LOGGER.error("Crop profile rejected: {} ({}): {}",r.id(),r.source(),error.getMessage());}
        var grouped=new HashMap<Block,List<CropProfile>>();for(var c:rules.values())grouped.computeIfAbsent(c.crop(),b->new ArrayList<>()).add(c);
        var byCrop=new HashMap<Block,CropProfile>();
        for(var group:grouped.values())if(group.size()==1)byCrop.put(group.getFirst().crop(),group.getFirst());else{for(var c:group){rules.remove(c.id());rejected++;}BuildupMobTweaks.LOGGER.error("Ambiguous crop profiles paused: {}",group.stream().map(CropProfile::id).toList());}
        return new Snapshot(rules,byCrop,rejected,UUID.randomUUID());
    }
    public static CropProfile decode(Identifier id,JsonObject j){
        try{return decodeRule(id,j);}catch(NullPointerException error){throw new IllegalArgumentException("Missing required crop field",error);}
    }
    private static CropProfile decodeRule(Identifier id,JsonObject j){
        fields(j,Set.of("version","crop","maturity","harvest_handler","replant","category"));if(integer(j,"version")!=1)throw new IllegalArgumentException("Unsupported crop version");
        var crop=BuiltInRegistries.BLOCK.getOptional(Identifier.parse(j.get("crop").getAsString())).orElseThrow(()->new IllegalArgumentException("Unknown crop"));
        var maturity=j.getAsJsonObject("maturity");fields(maturity,Set.of("type","property","value"));if(!maturity.get("type").getAsString().equals("age"))throw new IllegalArgumentException("Unknown maturity type");
        var prop=crop.getStateDefinition().getProperty(maturity.get("property").getAsString());
        int mature=integer(maturity,"value");if(!(crop instanceof CropBlock)||!(prop instanceof IntegerProperty age)||age.getPossibleValues().stream().max(Integer::compare).orElseThrow()!=mature)throw new IllegalArgumentException("Expected age-based crop maximum");
        var handler=Identifier.parse(j.get("harvest_handler").getAsString());if(CropHandlers.find(handler).isEmpty())throw new IllegalArgumentException("Unregistered handler");
        var replant=j.getAsJsonObject("replant");fields(replant,Set.of("item","count","block","state","substrate"));int count=integer(replant,"count");if(count<1||count>16)throw new IllegalArgumentException("Seed count 1..16 required");
        var seed=BuiltInRegistries.ITEM.getOptional(Identifier.parse(replant.get("item").getAsString())).orElseThrow(()->new IllegalArgumentException("Unknown seed"));
        if(!(seed instanceof BlockItem bi)||bi.getBlock()!=crop||!replant.get("block").getAsString().equals(BuiltInRegistries.BLOCK.getKey(crop).toString()))throw new IllegalArgumentException("Seed must plant the described crop");
        BlockState initial=crop.defaultBlockState();for(var e:replant.getAsJsonObject("state").entrySet())initial=property(initial,e.getKey(),e.getValue().getAsString());
        if(initial.getValue(age)==mature)throw new IllegalArgumentException("Replant must be immature");
        var substrate=BuiltInRegistries.BLOCK.getOptional(Identifier.parse(replant.get("substrate").getAsString())).orElseThrow(()->new IllegalArgumentException("Unknown substrate"));
        if(substrate!=Blocks.FARMLAND)throw new IllegalArgumentException("A2 supports existing farmland only");
        String signature=BuiltInRegistries.BLOCK.getKey(crop)+"|"+age.getName()+"|"+mature+"|"+handler+":"+CropHandlers.find(handler).orElseThrow().version()+"|"+BuiltInRegistries.ITEM.getKey(seed)+"|"+count+"|"+initial;
        return new CropProfile(id,crop,age,mature,handler,seed,count,initial,substrate,signature);
    }
    private static <T extends Comparable<T>> BlockState value(BlockState s,Property<T> p,String v){return s.setValue(p,p.getValue(v).orElseThrow(()->new IllegalArgumentException("Invalid property value")));}
    private static BlockState property(BlockState s,String key,String v){var p=s.getBlock().getStateDefinition().getProperty(key);if(p==null)throw new IllegalArgumentException("Unknown state property");return value(s,p,v);}
    private static int integer(JsonObject j,String name){try{if(j.get(name)==null||!j.get(name).isJsonPrimitive()||!j.get(name).getAsJsonPrimitive().isNumber())throw new IllegalArgumentException("Expected numeric integer: "+name);return j.get(name).getAsBigDecimal().intValueExact();}catch(ArithmeticException|IllegalStateException|NullPointerException e){throw new IllegalArgumentException("Expected integer: "+name,e);}}
    private static void fields(JsonObject j,Set<String> allowed){if(j==null)throw new IllegalArgumentException("Missing object");for(var key:j.keySet())if(!allowed.contains(key))throw new IllegalArgumentException("Unknown field: "+key);}
    private CropProfiles(){}
}
