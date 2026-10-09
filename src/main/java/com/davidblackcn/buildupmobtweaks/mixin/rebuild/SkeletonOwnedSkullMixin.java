package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.skeleton.*;
import net.minecraft.world.entity.projectile.hurtingprojectile.WitherSkull;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(WitherSkull.class)
public abstract class SkeletonOwnedSkullMixin {
    @ModifyArg(method="onHit(Lnet/minecraft/world/phys/HitResult;)V",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;explode(Lnet/minecraft/world/entity/Entity;DDDFZLnet/minecraft/world/level/Level$ExplosionInteraction;)V"),index=6)
    private ExplosionInteraction buildup$noTerrain(ExplosionInteraction original){return ((WitherSkull)(Object)this).hasAttached(ExtendedSkeletonState.SKULL)?ExplosionInteraction.NONE:original;}
    @Inject(method="onHitEntity(Lnet/minecraft/world/phys/EntityHitResult;)V",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/LivingEntity;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z"),cancellable=true)
    private void buildup$noLongWither(EntityHitResult hit,CallbackInfo ci){if(((WitherSkull)(Object)this).hasAttached(ExtendedSkeletonState.SKULL))ci.cancel();}
    @Inject(method="onHitEntity(Lnet/minecraft/world/phys/EntityHitResult;)V",at=@At("HEAD"))
    private void buildup$hitCount(EntityHitResult hit,CallbackInfo ci){var s=(WitherSkull)(Object)this;if(ExtendedSkeletonState.known(s,ExtendedSkeletonState.SKULL)&&s.getOwner()!=null)ExtendedSkeletonState.runtime(s.getOwner()).hits++;}
}
