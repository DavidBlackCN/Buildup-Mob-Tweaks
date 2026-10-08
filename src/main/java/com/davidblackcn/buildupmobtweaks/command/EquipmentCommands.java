package com.davidblackcn.buildupmobtweaks.command;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import com.davidblackcn.buildupmobtweaks.equipment.EquipmentCapabilities;
import com.davidblackcn.buildupmobtweaks.equipment.EquipmentPools;
import com.davidblackcn.buildupmobtweaks.equipment.EquipmentService;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

public final class EquipmentCommands {
    private EquipmentCommands() {}
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) -> dispatcher.register(
                Commands.literal(BuildupMobTweaks.MOD_ID)
                        .then(Commands.literal("equipment_pools").requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                                .executes(context -> {
                                    var pools = EquipmentPools.get(context.getSource().getServer());
                                    context.getSource().sendSuccess(() -> Component.translatable("commands.buildupmobtweaks.equipment_pools",
                                            pools.rules().stream().map(rule -> rule.id().toString()).toList().toString(), pools.rejected()), false);
                                    return pools.rules().size();
                                }))
                        .then(Commands.literal("equipment").requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                                .then(Commands.argument("target", EntityArgument.entity()).executes(context -> {
                                    var entity = EntityArgument.getEntity(context, "target");
                                    var capabilities = entity instanceof LivingEntity living
                                            ? "main=" + EquipmentCapabilities.identify(living.getMainHandItem())
                                                + ", off=" + EquipmentCapabilities.identify(living.getOffhandItem()) : "[]";
                                    context.getSource().sendSuccess(() -> Component.translatable("commands.buildupmobtweaks.equipment",
                                            entity.getUUID().toString(), String.valueOf(entity.getAttached(EquipmentService.DATA)), capabilities), false);
                                    return 1;
                                })))));
    }
}