package com.davidblackcn.buildupmobtweaks.command;
import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import com.davidblackcn.buildupmobtweaks.combat.RaidCombat;
import com.davidblackcn.buildupmobtweaks.feature.*;
import java.util.List;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.monster.Witch;
public final class RaidCommands {
    private RaidCommands() {}
    public static void register(FeatureRegistry features) {
        CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) -> dispatcher.register(
                Commands.literal(BuildupMobTweaks.MOD_ID).then(Commands.literal("raider")
                        .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                        .then(Commands.argument("target", EntityArgument.entity()).executes(context -> {
                            var entity = EntityArgument.getEntity(context, "target");
                            var gates = List.of(FeatureId.PILLAGER_RETREAT, FeatureId.PILLAGER_WEAPON_SWITCH, FeatureId.VINDICATOR_SUPPORT,
                                    FeatureId.EVOKER_VEX_LIMIT, FeatureId.EVOKER_SUMMON_COOLDOWN, FeatureId.WITCH_WINDUP, FeatureId.WITCH_THROW_COOLDOWN)
                                    .stream().map(id -> id.id().getPath() + "=" + features.isEnabled(id)).toList();
                            context.getSource().sendSuccess(() -> Component.translatable("commands.buildupmobtweaks.raider", entity.getUUID().toString(),
                                    String.valueOf(entity.getAttached(RaidCombat.DATA)), entity instanceof Witch witch ? RaidCombat.instance().pendingPotion(witch).toString() : "none", gates.toString()), false);
                            return 1;
                        })))));
    }
}
