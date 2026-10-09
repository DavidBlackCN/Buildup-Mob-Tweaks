package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.drowned.*;
import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.zombie.Drowned;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(Drowned.class)
public abstract class DrownedLifecycleMixin {
    @Inject(method="performRangedAttack(Lnet/minecraft/world/entity/LivingEntity;F)V",at=@At("HEAD"),cancellable=true)
    private void buildup$throw(LivingEntity target,float power,CallbackInfo ci){var m=(Drowned)(Object)this;var b=DrownedBehavior.instance();
        if(b!=null&&b.enabled(m,FeatureId.DROWNED_TRIDENT_CONSERVATION)){b.throwHeld(m,target);ci.cancel();}}
    @Inject(method="wantsToSwim()Z",at=@At("HEAD"),cancellable=true)
    private void buildup$water(CallbackInfoReturnable<Boolean> ci){if(DrownedBehavior.instance()!=null&&DrownedBehavior.instance().seekingWater((Drowned)(Object)this))ci.setReturnValue(true);}
}
