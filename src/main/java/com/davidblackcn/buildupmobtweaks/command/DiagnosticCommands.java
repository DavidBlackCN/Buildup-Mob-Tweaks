package com.davidblackcn.buildupmobtweaks.command;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import com.davidblackcn.buildupmobtweaks.feature.FeatureRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.commands.arguments.EntityArgument;
import com.davidblackcn.buildupmobtweaks.traits.core.TraitStorage;
import com.davidblackcn.buildupmobtweaks.traits.core.TraitRegistry;
import com.davidblackcn.buildupmobtweaks.traits.core.SpawnStorage;

/** Opt-in diagnostics only: no ticking, entities, items, or world mutations. */
public final class DiagnosticCommands {
    private DiagnosticCommands() {}

    public static void register(FeatureRegistry features) {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(Commands.literal(BuildupMobTweaks.MOD_ID)
                        .then(Commands.literal("status").executes(context -> status(context.getSource(), features)))
                        .then(Commands.literal("probe")
                                .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                                .executes(context -> probe(context.getSource(), features)))
                        .then(Commands.literal("villager").requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                                .then(Commands.argument("target", EntityArgument.entity()).executes(context -> {
                                    if (!features.isEnabled(FeatureId.DIAGNOSTIC_PROBE)) return 0;
                                    var entity=EntityArgument.getEntity(context,"target");
                                    if (!(entity instanceof net.minecraft.world.entity.npc.villager.Villager v)) return 0;
                                    var level=(net.minecraft.server.level.ServerLevel)v.level();
                                    var scope=com.davidblackcn.buildupmobtweaks.villager.ControlPolicy.Scope.class;
                                    String control=java.util.Arrays.stream(scope.getEnumConstants()).map(s -> s + "="
                                            + com.davidblackcn.buildupmobtweaks.villager.ControlPolicy.query(v,level,s)).toList().toString();
                                    String runtime=com.davidblackcn.buildupmobtweaks.villager.VillagerState.peekRuntime(v)
                                            .map(s -> "checks="+s.poiChecks+", invalid="+s.invalidPois+", stuck="+s.stuckEvents+", retries="+s.recomputes+", deferred="+s.deferrals+", hazards="+s.hazardStops)
                                            .orElse("inactive");
                                    context.getSource().sendSuccess(() -> Component.translatable("commands.buildupmobtweaks.villager_core",
                                            v.getUUID().toString(), control, v.getBrain().getActiveActivities().toString(), runtime,
                                            String.valueOf(v.getAttached(com.davidblackcn.buildupmobtweaks.villager.VillagerState.DATA))),false);
                                    return 1;
                                })))
                        .then(Commands.literal("traits").requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                                .then(Commands.argument("target", EntityArgument.entity()).executes(context -> {
                                    if (!features.isEnabled(FeatureId.DIAGNOSTIC_PROBE)) return 0;
                                    var entity = EntityArgument.getEntity(context, "target");
                                    // Read only: no getAttachedOrCreate, migration, equipment identification or selection.
                                    var state = com.davidblackcn.buildupmobtweaks.traits.core.TraitRuntime.readRecord(entity);
                                    String summary = state.map(s -> s.mode() + " / " + s.outcome() + " / "
                                            + s.entries().stream().map(i -> {
                                                var d = TraitRegistry.find(i.id()).orElseThrow();
                                                return i.id() + " [v" + i.version() + ", " + i.source() + ", Tier "
                                                        + d.tier() + ", " + d.category() + ", " + d.polarity() + "]";
                                            }).toList()).orElse(entity.hasAttached(TraitStorage.DATA)
                                                    ? "unknown data; paused" : "no record");
                                    context.getSource().sendSuccess(() -> Component.translatable(
                                            "commands.buildupmobtweaks.trait_core", entity.getUUID().toString(), summary,
                                            SpawnStorage.read(entity).map(Object::toString).orElse("unknown source")), false);
                                    return 1;
                                })))));
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
