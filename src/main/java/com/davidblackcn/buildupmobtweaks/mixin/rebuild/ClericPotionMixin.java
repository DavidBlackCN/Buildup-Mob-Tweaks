package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.profession.ProfessionBehavior;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import java.util.List;
@Mixin(ThrownSplashPotion.class)
public abstract class ClericPotionMixin {
    @WrapOperation(method="onHitAsPotion(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/phys/HitResult;)V",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;getEntitiesOfClass(Ljava/lang/Class;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;"))
    private List<LivingEntity> buildup$supportOnly(Level level,Class<LivingEntity> type,AABB box,Operation<List<LivingEntity>> original){var targets=original.call(level,type,box);
        return ProfessionBehavior.instance()==null?targets:ProfessionBehavior.instance().potionTargets((ThrownSplashPotion)(Object)this,targets);
    }
}
