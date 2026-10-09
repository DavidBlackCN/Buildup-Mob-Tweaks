package com.davidblackcn.buildupmobtweaks.rebuild.raid;
import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import com.davidblackcn.buildupmobtweaks.rebuild.evoker.VexLedger;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.monster.illager.Evoker;
/** Operator-only, read-only state inspection; does not change traits, tick AI or resolve unloaded minions. */
public final class RaidCommands {
    public static void register(){CommandRegistrationCallback.EVENT.register((dispatcher,access,environment)->dispatcher.register(
        Commands.literal(BuildupMobTweaks.MOD_ID).then(Commands.literal("raid").requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
            .then(Commands.argument("target",EntityArgument.entity()).executes(c->{if(!(EntityArgument.getEntity(c,"target") instanceof Mob m)||!RaidGate.supported(m))return 0;
                var r=RaidState.runtime(m);var type=m instanceof Witch?RaidState.WITCH:m instanceof Evoker?RaidState.EVOKER:RaidState.VEX;
                c.getSource().sendSuccess(()->Component.literal("Raid uuid="+m.getUUID()+" target="+(m.getTarget()==null?"none":m.getTarget().getUUID())
                    +" state="+m.getAttached(type)+" preview="+r.preview+" charging="+r.charging+" active="+m.getAttached(RaidState.ACTIVE)
                    +" throws="+r.throwsMade+" casts="+r.casts+" released="+r.releases+" clutch="+r.totems+" charges="+r.charges
                    +(m instanceof Evoker e?" owned_vex="+VexLedger.count(e):m instanceof Vex v&&v.getOwnerReference()!=null?" owner="+v.getOwnerReference().getUUID():"")),false);return 1;
            })))));}
    private RaidCommands(){}
}
