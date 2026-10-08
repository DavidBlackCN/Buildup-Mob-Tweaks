package com.davidblackcn.buildupmobtweaks.command;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import com.davidblackcn.buildupmobtweaks.traits.TraitService;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;

public final class TraitCommands {
    private TraitCommands() {}

    public static void register(TraitService traits) {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(Commands.literal(BuildupMobTweaks.MOD_ID)
                        .then(Commands.literal("traits").requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                                .then(Commands.argument("target", EntityArgument.entity()).executes(context -> {
                                    var entity = EntityArgument.getEntity(context, "target");
                                    var state = TraitService.read(entity);
                                    context.getSource().sendSuccess(() -> Component.translatable(
                                            "commands.buildupmobtweaks.traits", entity.getUUID().toString(),
                                            state.map(Object::toString).orElse(entity.hasAttached(TraitService.DATA)
                                                    ? "unsupported_or_invalid (preserved)" : "unassigned"),
                                            traits.activeTraits(entity).toString()), false);
                                    return 1;
                                })))));
    }
}
