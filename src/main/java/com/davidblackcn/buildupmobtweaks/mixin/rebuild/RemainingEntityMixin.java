package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.remaining.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Blaze;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(Entity.class)
public abstract class RemainingEntityMixin {
    @Inject(method="positionRider(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity$MoveFunction;)V",at=@At("HEAD"),cancellable=true)
    private void buildup$position(Entity p,Entity.MoveFunction move,CallbackInfo ci){if((Object)this instanceof Blaze m&&RemainingBehavior.instance()!=null&&RemainingBehavior.instance().placeBall(m,p,move))ci.cancel();}
    @Inject(method="tick()V",at=@At("TAIL"))
    private void buildup$noAi(CallbackInfo ci){if((Object)this instanceof Mob m&&!m.level().isClientSide()&&m.isNoAi()&&RemainingBehavior.instance()!=null)RemainingBehavior.instance().beforeAi(m);}
}
