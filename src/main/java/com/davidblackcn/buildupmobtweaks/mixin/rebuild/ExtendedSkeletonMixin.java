package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.skeleton.ExtendedSkeletonBehavior;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(AbstractSkeleton.class)
public abstract class ExtendedSkeletonMixin {
    @Inject(method="performRangedAttack(Lnet/minecraft/world/entity/LivingEntity;F)V",at=@At("RETURN"))
    private void buildup$ownedBow(LivingEntity target,float power,CallbackInfo ci){if(ExtendedSkeletonBehavior.instance()!=null)ExtendedSkeletonBehavior.instance().bowShot((AbstractSkeleton)(Object)this);}
}
