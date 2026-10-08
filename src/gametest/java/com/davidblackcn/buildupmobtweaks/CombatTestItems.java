package com.davidblackcn.buildupmobtweaks;

import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.*;
import net.minecraft.resources.*;
import net.minecraft.world.item.*;

/** Registered only by the test mod; exercises a non-minecraft BowItem through real vanilla goals. */
public final class CombatTestItems implements ModInitializer {
    public static Item BOW;
    public static Item CROSSBOW;
    @Override public void onInitialize() {
        var id = Identifier.fromNamespaceAndPath("buildupmobtweaks-test", "bow");
        var crossbowId = Identifier.fromNamespaceAndPath("buildupmobtweaks-test", "crossbow");
        CROSSBOW = Registry.register(BuiltInRegistries.ITEM, crossbowId,
                new CrossbowItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, crossbowId)).durability(465)));
        BOW = Registry.register(BuiltInRegistries.ITEM, id,
                new BowItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)).durability(384)));
    }
}