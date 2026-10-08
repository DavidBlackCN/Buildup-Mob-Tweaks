package com.davidblackcn.buildupmobtweaks.rebuild.skeleton;
import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
final class SkeletonCommands {
    static void register(){CommandRegistrationCallback.EVENT.register((dispatcher,access,environment)->dispatcher.register(
            Commands.literal(BuildupMobTweaks.MOD_ID).then(Commands.literal("skeleton").requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                    .then(Commands.argument("target",EntityArgument.entity()).executes(c->{
                        if(!(EntityArgument.getEntity(c,"target") instanceof AbstractSkeleton m)||!SkeletonBehavior.supported(m))return 0;
                        var r=SkeletonState.runtime(m);c.getSource().sendSuccess(()->Component.literal("Skeleton "+m.getUUID()+" mode="+r.mode+" managed="+r.managed
                                +" target="+(m.getTarget()==null?"none":m.getTarget().getUUID())+" swaps="+r.swaps+" shots="+r.shots+" flips="+r.flips+" dodges="+r.dodges
                                +" snowballs="+r.snowballs+" shelters="+r.shelters+" reserve="+SkeletonState.reserve(m)+" state="+m.getAttached(SkeletonState.DATA)),false);return 1;
                    })))));}
}
