package com.davidblackcn.buildupmobtweaks;

import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.*;
import net.minecraft.resources.*;
import net.minecraft.world.item.*;

/** Registered only by the test mod; exercises a non-minecraft BowItem through real vanilla goals. */
public final class CombatTestItems implements ModInitializer {
    public static Item BOW;
    @Override public void onInitialize() {
        var id = Identifier.fromNamespaceAndPath("buildupmobtweaks-test", "bow");
        BOW = Registry.register(BuiltInRegistries.ITEM, id,
                new BowItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)).durability(384)));
    }
}