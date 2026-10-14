package com.davidblackcn.buildupmobtweaks.villager.farmer;

import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.gamerules.GameRules;
import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import java.util.*;

/** Explicit protection adapters only. Missing required coverage and adapter exceptions fail closed. */
public final class InteractionPolicy {
    public enum Operation { HARVEST, REPLANT }
    public enum Decision { ALLOW, DENY, UNKNOWN }
    @FunctionalInterface public interface Provider { Decision check(ServerLevel level,Villager villager,BlockPos pos,Operation operation); }
    private static final Map<Identifier,Provider> PROVIDERS=new LinkedHashMap<>();
    private static final Set<Identifier> FAILED=new HashSet<>();
    public static void register(Identifier id,Provider provider){if(PROVIDERS.putIfAbsent(id,Objects.requireNonNull(provider))!=null)throw new IllegalArgumentException("Duplicate interaction policy: "+id);}
    public static boolean allows(ServerLevel level,Villager v,BlockPos pos,Operation operation){
        if(!level.hasChunkAt(pos)||!level.getGameRules().get(GameRules.MOB_GRIEFING))return false;
        if(BuildupMobTweaks.config().villagers.farmer.protection_required.get()&&PROVIDERS.isEmpty())return false;
        for(var entry:PROVIDERS.entrySet()){
            if(FAILED.contains(entry.getKey()))return false;
            try{if(entry.getValue().check(level,v,pos,operation)!=Decision.ALLOW)return false;}
            catch(RuntimeException error){FAILED.add(entry.getKey());BuildupMobTweaks.LOGGER.error("Farmer protection adapter paused: {}",entry.getKey(),error);return false;}
        }
        return true;
    }
    static void resetFailures(){FAILED.clear();}
    private InteractionPolicy(){}
}
