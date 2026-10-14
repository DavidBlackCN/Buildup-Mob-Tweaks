package com.davidblackcn.buildupmobtweaks.villager.farmer;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.villager.Villager;
import java.util.*;

public final class CropHandlers {
    private static final Map<Identifier,CropHandler> HANDLERS=new LinkedHashMap<>();
    public static void register(Identifier id,CropHandler handler) {
        Objects.requireNonNull(id);Objects.requireNonNull(handler);
        if(handler.version()<1 || handler.interactionTier()!=1 || HANDLERS.putIfAbsent(id,handler)!=null) throw new IllegalArgumentException("Invalid/duplicate crop handler: "+id);
    }
    public static Optional<CropHandler> find(Identifier id){return Optional.ofNullable(HANDLERS.get(id));}
    public static void bootstrap(){register(BuildupMobTweaks.id("break_and_replant"),new AgeCrop());}
    private static final class AgeCrop implements CropHandler {
        public int interactionTier(){return 1;}
        public boolean mature(ServerLevel l,BlockPos p,CropProfile c){var s=l.getBlockState(p);return s.is(c.crop())&&s.getValue(c.age())==c.matureAge();}
        public boolean harvest(ServerLevel l,Villager v,BlockPos p,CropProfile c){return mature(l,p,c)&&l.destroyBlock(p,true,v);}
        public boolean plant(ServerLevel l,Villager v,BlockPos p,CropProfile c){return l.getBlockState(p).isAir()&&l.getBlockState(p.below()).is(c.substrate())&&c.replant().canSurvive(l,p)&&l.setBlock(p,c.replant(),3);}
    }
    private CropHandlers(){}
}
