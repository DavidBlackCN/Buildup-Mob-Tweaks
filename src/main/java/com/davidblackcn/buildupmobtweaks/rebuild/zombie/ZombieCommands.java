package com.davidblackcn.buildupmobtweaks.rebuild.zombie;
import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.monster.zombie.Zombie;
final class ZombieCommands {
    static void register(){CommandRegistrationCallback.EVENT.register((d,a,e)->d.register(Commands.literal(BuildupMobTweaks.MOD_ID)
            .then(Commands.literal("zombie").requires(Commands.hasPermission(Commands.LEVEL_OWNERS)).then(Commands.argument("target",EntityArgument.entity()).executes(c->{
                if(!(EntityArgument.getEntity(c,"target") instanceof Zombie m)||!ZombieBehavior.supported(m))return 0;
                var r=ZombieState.runtime(m);c.getSource().sendSuccess(()->Component.literal("Zombie "+m.getUUID()+" mode="+r.mode+" blocks="+r.blocks+" guards="+r.guards+" burrows="+r.burrows
                        +" displays="+r.displays.size()+" state="+m.getAttached(ZombieState.DATA)),false);return 1;
            })))));}
}
