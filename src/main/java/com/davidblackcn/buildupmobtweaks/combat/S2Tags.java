package com.davidblackcn.buildupmobtweaks.combat;
import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.gameevent.GameEvent;
public final class S2Tags {
    public static final TagKey<EntityType<?>> GOLEM_EXCLUDED=entity("golems_never_target");
    public static final TagKey<EntityType<?>> ILLAGER_EXCLUDED=entity("zombies_not_attackable_by_illagers");
    public static final TagKey<EntityType<?>> VANILLA_BOW=entity("skeletons_use_vanilla_bow_checks");
    public static final TagKey<EntityType<?>> RANGED_EXCLUDED=entity("ranged_extensions_disabled");
    public static final TagKey<EntityType<?>> ESCAPABLE_SEATS=entity("escapable_seats");
    public static final TagKey<EntityType<?>> ZOMBIE_SPECIALS=entity("zombie_specials");
    public static final TagKey<net.minecraft.world.item.Item> RANGED_ITEMS_EXCLUDED=TagKey.create(Registries.ITEM,BuildupMobTweaks.id("ranged_items_excluded"));
    public static final TagKey<GameEvent> REST_IGNORED=TagKey.create(Registries.GAME_EVENT,BuildupMobTweaks.id("rest_ignored_events"));
    private static TagKey<EntityType<?>> entity(String path){return TagKey.create(Registries.ENTITY_TYPE,BuildupMobTweaks.id(path));}
    private S2Tags(){}
}