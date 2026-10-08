package com.davidblackcn.buildupmobtweaks.command;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import com.davidblackcn.buildupmobtweaks.combat.VexCombat;
import com.davidblackcn.buildupmobtweaks.feature.*;
import java.util.List;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;

public final class VexCommands {
    private VexCommands() {}
    public static void register(FeatureRegistry features) {
        CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) -> dispatcher.register(
                Commands.literal(BuildupMobTweaks.MOD_ID).then(Commands.literal("vex")
                        .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                        .then(Commands.argument("target", EntityArgument.entity()).executes(context -> {
                            var entity = EntityArgument.getEntity(context, "target");
                            var gates = List.of(FeatureId.VEX_FIXED_CHARGE, FeatureId.VEX_RECOVERY_PAUSE, FeatureId.VEX_CLOSE_RANGE_GUARD)
                                    .stream().map(id -> id.id().getPath() + "=" + features.isEnabled(id)).toList();
                            context.getSource().sendSuccess(() -> Component.translatable("commands.buildupmobtweaks.vex", entity.getUUID().toString(),
                                    String.valueOf(entity.getAttached(VexCombat.DATA)), gates.toString()), false);
                            return 1;
                        })))));
    }
}