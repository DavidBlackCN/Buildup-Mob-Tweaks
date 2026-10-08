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
    @ModifyArg(method="aiStep()V",at=@At(value="INVOKE",target="Lnet/minecraft/world/item/alchemy/PotionContents;createItemStack(Lnet/minecraft/world/item/Item;Lnet/minecraft/core/Holder;)Lnet/minecraft/world/item/ItemStack;"),index=1)
    private net.minecraft.core.Holder<net.minecraft.world.item.alchemy.Potion> buildup$leaping(net.minecraft.core.Holder<net.minecraft.world.item.alchemy.Potion> potion) {
        var mob=(Witch)(Object)this;
        return potion==net.minecraft.world.item.alchemy.Potions.SWIFTNESS && mob.getTarget()!=null && mob.getTarget().getY()>mob.getY()+1
                && !mob.hasEffect(net.minecraft.world.effect.MobEffects.JUMP_BOOST)
                && com.davidblackcn.buildupmobtweaks.combat.EnvironmentCombat.on(mob,com.davidblackcn.buildupmobtweaks.feature.FeatureId.WITCH_LEAPING_POTION)
                ? net.minecraft.world.item.alchemy.Potions.LEAPING : potion;
    }
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
