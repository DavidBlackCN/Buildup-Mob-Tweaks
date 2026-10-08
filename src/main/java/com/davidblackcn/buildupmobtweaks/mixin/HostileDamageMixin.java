package com.davidblackcn.buildupmobtweaks.mixin;
import com.davidblackcn.buildupmobtweaks.combat.HostileCombat;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(LivingEntity.class)
public abstract class HostileDamageMixin {
    @ModifyVariable(method = "hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z", at = @At("HEAD"), argsOnly = true)
    private float buildup$damage(float amount, ServerLevel level, DamageSource source, float original) {
        return HostileCombat.instance() == null ? amount : HostileCombat.instance().damage((LivingEntity) (Object) this, source, amount);
    }
}