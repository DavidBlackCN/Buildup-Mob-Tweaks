package com.davidblackcn.buildupmobtweaks.rebuild.raid;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
public final class TemporaryItem {
    private static final String KEY="buildupmobtweaks:raid_temporary";
    public static ItemStack mark(ItemStack stack,String kind){CustomData.update(DataComponents.CUSTOM_DATA,stack,d->d.putString(KEY,kind));return stack;}
    public static boolean is(ItemStack stack,String kind){var d=stack.get(DataComponents.CUSTOM_DATA);return d!=null&&d.copyTag().getStringOr(KEY,"").equals(kind);}
    private TemporaryItem(){}
}
