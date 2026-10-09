/* Mob AI Tweaks adaptation: Copyright (c) 2024 N0t_UN_Owen, MIT. See NOTICE.md. */
package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.evoker.EvokerBehavior;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.illager.Evoker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(Entity.class)
public abstract class EvokerBallPositionMixin {
    @Inject(method="positionRider(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity$MoveFunction;)V",at=@At("HEAD"),cancellable=true)
    private void buildup$position(Entity p,Entity.MoveFunction move,CallbackInfo ci){if((Object)this instanceof Evoker m&&EvokerBehavior.instance()!=null&&EvokerBehavior.instance().placeBall(m,p,move))ci.cancel();}
    @Inject(method="tick()V",at=@At("TAIL"))
    private void buildup$noAi(CallbackInfo ci){if((Object)this instanceof Evoker m&&!m.level().isClientSide()&&m.isNoAi()&&EvokerBehavior.instance()!=null)EvokerBehavior.instance().beforeAi(m);}
}
