package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.villager.profession.WorkProjectiles;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import java.util.List;
@Mixin(ThrownSplashPotion.class)
public abstract class VillagerHealingPotionMixin {
    @WrapOperation(method="onHitAsPotion",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;getEntitiesOfClass(Ljava/lang/Class;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;"))
    private List<LivingEntity> buildup$patientOnly(Level l,Class<LivingEntity> type,AABB box,Operation<List<LivingEntity>> original){return WorkProjectiles.patients((ThrownSplashPotion)(Object)this,original.call(l,type,box));}
}
