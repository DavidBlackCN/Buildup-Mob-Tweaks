package com.davidblackcn.buildupmobtweaks.villager.profession;
import net.minecraft.world.entity.npc.villager.Villager;
public final class ProfessionDiagnostics {
    public static String describe(Villager v){var key=v.getVillagerData().profession().unwrapKey();var profiles=key.map(ProfessionRegistry::profiles).orElse(java.util.List.of());int level=profiles.isEmpty()?0:profiles.stream().anyMatch(a->a.existingFarmer()||!a.activities().isEmpty())?2:1;return "level="+level+", known="+ProfessionWorkState.known(v)+", conflict="+key.map(ProfessionRegistry::ambiguous).orElse(false)+", "+ProfessionWorkState.peek(v).map(r->r.reason+", adapter="+r.adapter+", starts="+r.starts+", completed="+r.completed+", operations="+r.operations+", scans="+r.scans).orElse("inactive")+", saved="+v.getAttached(ProfessionWorkState.DATA);}
    private ProfessionDiagnostics(){}
}
