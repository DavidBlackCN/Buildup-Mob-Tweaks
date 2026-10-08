package com.davidblackcn.buildupmobtweaks.command;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import com.davidblackcn.buildupmobtweaks.combat.*;
import com.davidblackcn.buildupmobtweaks.feature.*;
import java.util.Arrays;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;

public final class ZombieCommands {
    private ZombieCommands() {}
    public static void register(FeatureRegistry features) {
        CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) -> dispatcher.register(
                Commands.literal(BuildupMobTweaks.MOD_ID).then(Commands.literal("zombie")
                        .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                        .then(Commands.argument("target", EntityArgument.entity()).executes(context -> {
                            var entity = EntityArgument.getEntity(context, "target");
                            var gates = Arrays.stream(FeatureId.values()).filter(id -> id.name().startsWith("ZOMBIE_")
                                    || id.name().startsWith("DROWNED_") || id == FeatureId.HUSK_SAND_BURROW)
                                    .map(id -> id.id().getPath() + "=" + features.isEnabled(id)).toList();
                            context.getSource().sendSuccess(() -> Component.translatable("commands.buildupmobtweaks.zombie",
                                    entity.getUUID().toString(), String.valueOf(entity.getAttached(ZombieCombat.DATA)),
                                    String.valueOf(entity.getAttached(DrownedTridents.FLIGHT)),
                                    String.valueOf(entity.getAttached(DrownedTridents.OWNED)), gates.toString()), false);
                            return 1;
                        })))));
    }
}