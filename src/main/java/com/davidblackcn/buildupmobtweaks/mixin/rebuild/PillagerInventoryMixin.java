package com.davidblackcn.buildupmobtweaks.mixin.rebuild;

import com.davidblackcn.buildupmobtweaks.rebuild.pillager.PillagerBehavior;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.IntStream;
import net.minecraft.world.entity.monster.illager.Pillager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Preserve slot identity: vanilla Inventory omits empty slots and merges stacks when loading. */
@Mixin(Pillager.class)
public abstract class PillagerInventoryMixin {
    @Unique private static final String BUILDUP_LAYOUT = "buildupmobtweaks:inventory_slots_v1";

    @Inject(method = "addAdditionalSaveData(Lnet/minecraft/world/level/storage/ValueOutput;)V", at = @At("TAIL"))
    private void buildup$saveLayout(ValueOutput output, CallbackInfo ci) {
        var mob = (Pillager) (Object) this;
        if (!PillagerBehavior.known(mob)) return;
        var inventory = mob.getInventory();
        output.putIntArray(BUILDUP_LAYOUT, IntStream.range(0, inventory.getContainerSize())
                .filter(slot -> !inventory.getItem(slot).isEmpty()).toArray());
    }

    @Inject(method = "readAdditionalSaveData(Lnet/minecraft/world/level/storage/ValueInput;)V", at = @At("TAIL"))
    private void buildup$loadLayout(ValueInput input, CallbackInfo ci) {
        var layout = input.getIntArray(BUILDUP_LAYOUT);
        if (layout.isEmpty()) return;
        var inventory = ((Pillager) (Object) this).getInventory();
        int[] slots = layout.get();
        var items = new ArrayList<ItemStack>();
        input.listOrEmpty("Inventory", ItemStack.CODEC).forEach(items::add);
        var unique = new HashSet<Integer>();
        if (slots.length != items.size() || slots.length > inventory.getContainerSize()) return;
        for (int slot : slots) if (slot < 0 || slot >= inventory.getContainerSize() || !unique.add(slot)) return;
        // Read the single authoritative vanilla item list, replacing the compacted view; no backup copies are saved.
        inventory.clearContent();
        for (int i = 0; i < slots.length; i++) inventory.setItem(slots[i], items.get(i));
    }
}
