package com.davidblackcn.buildupmobtweaks.villager.profession;

import com.davidblackcn.buildupmobtweaks.villager.*;
import net.minecraft.resources.Identifier;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/** Internal extension point; optional-mod code belongs in compat/<mod_id>, never Core. */
public record ProfessionAdapter(Identifier id,ProfessionProfile profile,List<Entry> activities,boolean existingFarmer) {
    public record Entry(Identifier id,int priority,Supplier<? extends WorkActivity> factory) {
        public Entry {Objects.requireNonNull(id);Objects.requireNonNull(factory);}
    }
    public ProfessionAdapter {Objects.requireNonNull(id);Objects.requireNonNull(profile);activities=List.copyOf(activities);}
}
