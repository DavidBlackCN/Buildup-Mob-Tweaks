package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.skeleton.ExtendedSkeletonBehavior;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.projectile.hurtingprojectile.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(AbstractHurtingProjectile.class)
public abstract class ExtendedSkeletonTickMixin {
    @Inject(method="tick()V",at=@At("HEAD"),cancellable=true)
    private void buildup$skull(CallbackInfo ci){if((Object)this instanceof WitherSkull s&&ExtendedSkeletonBehavior.instance()!=null){ExtendedSkeletonBehavior.instance().skullTick(s);if(s.isRemoved())ci.cancel();}}
}
