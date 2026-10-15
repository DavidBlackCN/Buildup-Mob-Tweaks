package com.davidblackcn.buildupmobtweaks.villager;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.ai.village.poi.PoiType;

/** Existing profession/POI description shared by the farmer and registered A3 activities. */
public record ProfessionProfile(Identifier id, ResourceKey<VillagerProfession> profession,
                                ResourceKey<PoiType> workPoi, int workRadius) {
    public ProfessionProfile {
        java.util.Objects.requireNonNull(id);
        java.util.Objects.requireNonNull(profession);
        java.util.Objects.requireNonNull(workPoi);
        if (workRadius < 1 || workRadius > 48) throw new IllegalArgumentException("workRadius must be 1..48");
    }
}
