package com.davidblackcn.buildupmobtweaks.rebuild.raid;
import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import com.davidblackcn.buildupmobtweaks.feature.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.illager.Evoker;
public final class RaidGate {
    private static final TagKey<EntityType<?>> RAID=TagKey.create(Registries.ENTITY_TYPE,BuildupMobTweaks.id("raid_ai_excluded"));
    private static final TagKey<EntityType<?>> VEX=TagKey.create(Registries.ENTITY_TYPE,BuildupMobTweaks.id("vex_ai_excluded"));
    public static boolean supported(Mob m){return m instanceof Witch&&m.getType()==EntityTypes.WITCH||m instanceof Evoker&&m.getType()==EntityTypes.EVOKER||m instanceof Vex&&m.getType()==EntityTypes.VEX;}
    public static boolean enabled(Mob m,FeatureRegistry f,FeatureId id){return supported(m)&&f.isEnabled(id)&&!m.getType().builtInRegistryHolder().is(m instanceof Vex?VEX:RAID)
        &&!m.entityTags().contains("buildupmobtweaks:vanilla_ai")&&!m.entityTags().contains("buildupmobtweaks:disable_"+id.id().getPath());}
    private RaidGate(){}
}
