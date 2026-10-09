package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.evoker.EvokerBehavior;
import com.davidblackcn.buildupmobtweaks.rebuild.raid.RaidState;
import net.minecraft.world.entity.projectile.hurtingprojectile.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(AbstractHurtingProjectile.class)
public abstract class EvokerBallMixin {
    @Inject(method="tick()V",at=@At("HEAD"),cancellable=true)
    private void buildup$orbit(CallbackInfo ci){if(!((Object)this instanceof SmallFireball b))return;
        if(!b.level().isClientSide()&&EvokerBehavior.instance()!=null)EvokerBehavior.instance().ballTick(b);
        if(Boolean.TRUE.equals(b.getAttached(RaidState.ORBIT))){b.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);b.baseTick();ci.cancel();}}
}
