package com.davidblackcn.buildupmobtweaks.rebuild.pillager;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.monster.illager.Pillager;

/** Read-only diagnostics; arranging a scenario never drives AI methods. */
public final class PillagerCommands {
    private PillagerCommands() {}
    static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) -> dispatcher.register(
                Commands.literal(BuildupMobTweaks.MOD_ID).then(Commands.literal("pillager")
                        .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                        .then(Commands.argument("target", EntityArgument.entity()).executes(context -> {
                            var entity = EntityArgument.getEntity(context, "target");
                            if (!(entity instanceof Pillager p)) return 0;
                            var rt = PillagerBehavior.runtime(p);
                            String text = "Pillager " + p.getUUID() + " phase=" + rt.phase + " target="
                                    + (p.getTarget() == null ? "none" : p.getTarget().getUUID()) + " swaps=" + rt.swaps
                                    + " retreatTicks=" + rt.retreats + " meals=" + rt.meals + " targetExits=" + rt.targetClears
                                    + " inventory=" + p.getInventory() + " state=" + p.getAttached(PillagerBehavior.DATA);
                            context.getSource().sendSuccess(() -> Component.literal(text), false); return 1;
                        })))));
    }
}
