package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.ability.AbilityBehavior;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.monster.breeze.Shoot;
import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(Shoot.class)
public abstract class BreezeShotgunMixin {
    @ModifyExpressionValue(method="tick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/monster/breeze/Breeze;J)V",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/projectile/Projectile;spawnProjectileUsingShoot(Lnet/minecraft/world/entity/projectile/Projectile;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;DDDFF)Lnet/minecraft/world/entity/projectile/Projectile;"))
    private Projectile buildup$spread(Projectile p){var b=AbilityBehavior.instance();if(b!=null)b.shotgun(p);return p;}
}
