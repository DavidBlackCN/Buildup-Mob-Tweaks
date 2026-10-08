package com.davidblackcn.buildupmobtweaks.mixin;
import com.davidblackcn.buildupmobtweaks.combat.RaidCombat;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Witch.class)
public abstract class WitchPotionMixin {
    @Inject(method = "performRangedAttack(Lnet/minecraft/world/entity/LivingEntity;F)V", at = @At("HEAD"), cancellable = true)
    private void buildup$gate(LivingEntity target, float power, CallbackInfo ci) {
        if (RaidCombat.instance() != null && !RaidCombat.instance().allowPotion((Witch) (Object) this, target)) ci.cancel();
    }
    @Redirect(method = "performRangedAttack(Lnet/minecraft/world/entity/LivingEntity;F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/Projectile;spawnProjectileUsingShoot(Lnet/minecraft/world/entity/projectile/Projectile$ProjectileFactory;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;DDDFF)Lnet/minecraft/world/entity/projectile/Projectile;"))
    private Projectile buildup$telegraph(Projectile.ProjectileFactory<?> factory, ServerLevel level, ItemStack stack, LivingEntity shooter, double dx, double dy, double dz, float speed, float uncertainty) {
        return RaidCombat.instance() == null ? Projectile.spawnProjectileUsingShoot(factory, level, stack, shooter, dx, dy, dz, speed, uncertainty)
                : RaidCombat.instance().potion(factory, level, stack, (Witch) shooter, dx, dy, dz, speed, uncertainty);
    }
}
