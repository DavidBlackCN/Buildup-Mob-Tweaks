package com.davidblackcn.buildupmobtweaks.rebuild.remaining;
import com.mojang.brigadier.Command;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Mob;
public final class RemainingCommands {
    public static void register(){CommandRegistrationCallback.EVENT.register((d,a,e)->d.register(Commands.literal("buildupmobtweaks").then(Commands.literal("remaining")
        .requires(s->s.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER))
        .then(Commands.argument("entity",EntityArgument.entity()).executes(ctx->{var ent=EntityArgument.getEntity(ctx,"entity");
            if(ent instanceof Mob m&&RemainingState.known(m)){var r=RemainingState.runtime(m);ctx.getSource().sendSuccess(()->Component.literal("Remaining uuid="+m.getUUID()+" state="+m.getAttached(RemainingState.DATA)+" target="+(m.getTarget()==null?"none":m.getTarget().getUUID())+" charge="+m.getAttachedOrCreate(RemainingState.CHARGE)+" shots="+r.shots+" bound="+r.bindings+" released="+r.releases+" hides="+r.hides+" help="+r.helpBlocks+" combos="+r.combos+" teleports="+r.teleports+" attacks="+r.attacks+" bursts="+r.bursts),false);}return Command.SINGLE_SUCCESS;})))));}
    private RemainingCommands(){}
}
