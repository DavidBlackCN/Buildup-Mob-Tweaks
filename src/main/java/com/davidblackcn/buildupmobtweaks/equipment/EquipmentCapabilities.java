package com.davidblackcn.buildupmobtweaks.equipment;

import java.util.Set;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Recognition only. Unknown items retain vanilla AI; tags do not grant executable weapon behavior. */
public final class EquipmentCapabilities {
    public enum Ability { BOW, CROSSBOW, TRIDENT, SHIELD, MELEE }
    private static final Set<Item> SWORDS = Set.of(Items.WOODEN_SWORD, Items.STONE_SWORD, Items.COPPER_SWORD,
            Items.IRON_SWORD, Items.GOLDEN_SWORD, Items.DIAMOND_SWORD, Items.NETHERITE_SWORD);
    private EquipmentCapabilities() {}

    public static Set<Ability> identify(ItemStack stack) {
        if (stack.isEmpty()) return Set.of();
        Item item = stack.getItem();
        if (item == Items.BOW) return Set.of(Ability.BOW);
        if (item == Items.CROSSBOW) return Set.of(Ability.CROSSBOW);
        if (item == Items.TRIDENT) return Set.of(Ability.TRIDENT);
        if (item == Items.SHIELD) return Set.of(Ability.SHIELD);
        if (SWORDS.contains(item)) return Set.of(Ability.MELEE);
        return Set.of();
    }
}