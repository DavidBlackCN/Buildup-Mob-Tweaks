package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.ability.*;
import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(Mob.class)
public abstract class BabyConversionMixin {
    @WrapOperation(method="convertTo(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/ConversionParams;Lnet/minecraft/world/entity/EntitySpawnReason;Lnet/minecraft/world/entity/ConversionParams$AfterConversion;)Lnet/minecraft/world/entity/Mob;",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/ConversionParams$AfterConversion;finalizeConversion(Lnet/minecraft/world/entity/Mob;)V"))
    private void buildup$carryChoice(ConversionParams.AfterConversion<Mob> action,Mob to,Operation<Void> original){original.call(action,to);var from=(Mob)(Object)this;
        if(AbilityBehavior.skeleton(from)&&AbilityBehavior.skeleton(to)&&from.hasAttached(AbilityState.DATA)){
            to.setAttached(AbilityState.DATA,from.getAttached(AbilityState.DATA).copy());var b=AbilityBehavior.instance();
            if(to instanceof AbstractSkeleton s&&b!=null)s.setBaby(AbilityState.known(to)&&b.enabled(to,FeatureId.SKELETON_BABY_VARIANT)&&to.getAttached(AbilityState.DATA).getBooleanOr("baby_choice",false));
        }
    }
}
