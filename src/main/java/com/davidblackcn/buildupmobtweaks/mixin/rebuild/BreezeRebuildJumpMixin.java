/* Mob AI Tweaks adaptation: Copyright (c) 2024 N0t_UN_Owen, MIT. See NOTICE.md. */
package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.remaining.RemainingExtras;
import net.minecraft.world.entity.monster.breeze.*;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(LongJump.class)
public abstract class BreezeRebuildJumpMixin {
    @Inject(method="tick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/monster/breeze/Breeze;J)V",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/monster/breeze/Breeze;setDiscardFriction(Z)V",ordinal=0))
    private void buildup$takeoff(ServerLevel l,Breeze m,long time,CallbackInfo ci){RemainingExtras.breeze(m,true);}
    @Inject(method="tick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/monster/breeze/Breeze;J)V",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/monster/breeze/Breeze;setDiscardFriction(Z)V",ordinal=1))
    private void buildup$land(ServerLevel l,Breeze m,long time,CallbackInfo ci){RemainingExtras.breeze(m,false);}
}
