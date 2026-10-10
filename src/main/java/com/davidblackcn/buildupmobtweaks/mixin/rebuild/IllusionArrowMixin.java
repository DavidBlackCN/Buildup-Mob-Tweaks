package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.illusion.IllusionBehavior;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(Projectile.class)
public abstract class IllusionArrowMixin {
    @Inject(method="canHitEntity(Lnet/minecraft/world/entity/Entity;)Z",at=@At("HEAD"),cancellable=true)
    private void buildup$allies(Entity t,CallbackInfoReturnable<Boolean> ci){if((Object)this instanceof AbstractArrow a&&IllusionBehavior.allyHit(a,t))ci.setReturnValue(false);}
}
