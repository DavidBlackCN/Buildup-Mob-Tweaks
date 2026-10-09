package com.davidblackcn.buildupmobtweaks.rebuild.drowned;
import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.monster.zombie.Drowned;
final class DrownedCommands {
    static void register(){CommandRegistrationCallback.EVENT.register((d,a,e)->d.register(Commands.literal(BuildupMobTweaks.MOD_ID)
            .then(Commands.literal("drowned").requires(Commands.hasPermission(Commands.LEVEL_OWNERS)).then(Commands.argument("target",EntityArgument.entity()).executes(c->{
                if(!(EntityArgument.getEntity(c,"target") instanceof Drowned m))return 0;var r=DrownedBehavior.runtime(m);
                c.getSource().sendSuccess(()->Component.literal("Drowned "+m.getUUID()+" throws="+r.throwsCount+" hits="+r.hits+" recoveries="+r.recoveries+" releases="+r.releases+" hand="+m.getMainHandItem()+" flight="+m.getAttached(DrownedBehavior.FLIGHT)),false);return 1;
            })))));}
}
