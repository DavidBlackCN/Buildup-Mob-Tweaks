package com.davidblackcn.buildupmobtweaks.command;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import com.davidblackcn.buildupmobtweaks.feature.FeatureRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/** Opt-in diagnostics only: no ticking, entities, items, or world mutations. */
public final class DiagnosticCommands {
    private DiagnosticCommands() {}

    public static void register(FeatureRegistry features) {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(Commands.literal(BuildupMobTweaks.MOD_ID)
                        .then(Commands.literal("status").executes(context -> status(context.getSource(), features)))
                        .then(Commands.literal("probe")
                                .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                                .executes(context -> probe(context.getSource(), features)))));
    }

    private static int status(CommandSourceStack source, FeatureRegistry features) {
        source.sendSuccess(() -> Component.translatable("commands.buildupmobtweaks.status",
                FeatureId.DIAGNOSTIC_PROBE.id().toString(),
                features.isEnabled(FeatureId.DIAGNOSTIC_PROBE), features.diagnosticLines()), false);
        return 1;
    }

    private static int probe(CommandSourceStack source, FeatureRegistry features) {
        if (!features.isEnabled(FeatureId.DIAGNOSTIC_PROBE)) {
            source.sendFailure(Component.translatable("commands.buildupmobtweaks.disabled"));
            return 0;
        }
        int lines = features.diagnosticLines();
        for (int index = 1; index <= lines; index++) {
            int line = index;
            source.sendSuccess(() -> Component.translatable("commands.buildupmobtweaks.probe", line, lines), false);
        }
        return lines;
    }
}
