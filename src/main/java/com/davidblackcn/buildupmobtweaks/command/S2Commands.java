package com.davidblackcn.buildupmobtweaks.command;
import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import com.davidblackcn.buildupmobtweaks.combat.*;
import com.davidblackcn.buildupmobtweaks.feature.*;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.monster.illager.Evoker;
public final class S2Commands {
    private S2Commands(){}
    public static void register(FeatureRegistry features){
        CommandRegistrationCallback.EVENT.register((dispatcher,access,environment)->dispatcher.register(Commands.literal(BuildupMobTweaks.MOD_ID)
                .then(Commands.literal("s2").requires(Commands.hasPermission(Commands.LEVEL_OWNERS)).then(Commands.argument("target",EntityArgument.entity()).executes(context->{
                    var entity=EntityArgument.getEntity(context,"target");
                    context.getSource().sendSuccess(()->Component.translatable("commands.buildupmobtweaks.s2",entity.getStringUUID(),
                            String.valueOf(entity.getAttached(HostileCombat.DATA)),String.valueOf(entity.getAttached(AdvancedHostiles.DATA)),
                            entity instanceof Evoker evoker?VexOwnership.count(evoker):0),false);return 1;
                }))).then(Commands.literal("feature").requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                        .then(Commands.argument("id",com.mojang.brigadier.arguments.StringArgumentType.word()).executes(context->{
                            String name=com.mojang.brigadier.arguments.StringArgumentType.getString(context,"id");
                            for(var id:FeatureId.values())if(id.id().getPath().equals(name)){
                                context.getSource().sendSuccess(()->Component.literal(id.id()+"="+features.isEnabled(id)),false);return 1;
                            }context.getSource().sendFailure(Component.literal("Unknown Buildup feature: "+name));return 0;
                        })))));
    }
}