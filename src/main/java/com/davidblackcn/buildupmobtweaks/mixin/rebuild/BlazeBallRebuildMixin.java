package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.remaining.*;
import net.minecraft.world.entity.projectile.hurtingprojectile.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(AbstractHurtingProjectile.class)
public abstract class BlazeBallRebuildMixin {
    @Inject(method="tick()V",at=@At("HEAD"),cancellable=true)
    private void buildup$orbit(CallbackInfo ci){if((Object)this instanceof SmallFireball b){if(RemainingBehavior.instance()!=null)RemainingBehavior.instance().ballTick(b);
        if(Boolean.TRUE.equals(b.getAttached(RemainingState.ORBIT))&&(b.level().isClientSide()||RemainingState.known(b))){b.baseTick();ci.cancel();}}}
}
