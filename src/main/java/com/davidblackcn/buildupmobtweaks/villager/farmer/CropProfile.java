package com.davidblackcn.buildupmobtweaks.villager.farmer;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.item.Item;

/** Resolved immutable rule. Signature prevents applying changed rules to an old replant record. */
public record CropProfile(Identifier id, Block crop, IntegerProperty age, int matureAge,
                          Identifier handler, Item seed, int count, BlockState replant, Block substrate, String signature) {}
