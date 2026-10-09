/* Mob AI Tweaks adaptation: Copyright (c) 2024 N0t_UN_Owen, MIT. See NOTICE.md. */
package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.evoker.EvokerBehavior;
import com.davidblackcn.buildupmobtweaks.rebuild.raid.RaidState;
import com.davidblackcn.buildupmobtweaks.rebuild.zombie.ZombieBehavior;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.illager.Evoker;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.phys.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(SmallFireball.class)
public abstract class EvokerFireImpactMixin {
    @Inject(method="onHitEntity(Lnet/minecraft/world/phys/EntityHitResult;)V",at=@At("HEAD"),cancellable=true)
    private void buildup$valid(EntityHitResult hit,CallbackInfo ci){var b=(SmallFireball)(Object)this;if(RaidState.known(b,RaidState.BALL)&&b.getOwner() instanceof Evoker m&&hit.getEntity() instanceof net.minecraft.world.entity.LivingEntity t&&!ZombieBehavior.valid(m,t))ci.cancel();}
    @WrapOperation(method="onHitEntity(Lnet/minecraft/world/phys/EntityHitResult;)V",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/Entity;igniteForSeconds(F)V"))
    private void buildup$noFire(Entity entity,float seconds,Operation<Void> original){if(EvokerBehavior.instance()==null||!EvokerBehavior.instance().noFire((SmallFireball)(Object)this))original.call(entity,seconds);}
    @Inject(method="onHitBlock(Lnet/minecraft/world/phys/BlockHitResult;)V",at=@At("HEAD"),cancellable=true)
    private void buildup$noBlockFire(BlockHitResult hit,CallbackInfo ci){if(EvokerBehavior.instance()!=null&&EvokerBehavior.instance().noFire((SmallFireball)(Object)this))ci.cancel();}
}
