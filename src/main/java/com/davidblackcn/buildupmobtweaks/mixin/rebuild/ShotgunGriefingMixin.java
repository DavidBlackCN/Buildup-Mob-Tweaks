package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.ability.AbilityState;
import net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.BreezeWindCharge;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.gamerules.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(BreezeWindCharge.class)
public abstract class ShotgunGriefingMixin {
    @ModifyArg(method="explode(Lnet/minecraft/world/phys/Vec3;)V",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;explode(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/world/level/ExplosionDamageCalculator;DDDFZLnet/minecraft/world/level/Level$ExplosionInteraction;Lnet/minecraft/core/particles/ParticleOptions;Lnet/minecraft/core/particles/ParticleOptions;Lnet/minecraft/util/random/WeightedList;Lnet/minecraft/core/Holder;)V"),index=8)
    private Level.ExplosionInteraction buildup$protectExtra(Level.ExplosionInteraction original){var p=(BreezeWindCharge)(Object)this;return AbilityState.known(p)&&p.getAttached(AbilityState.DATA).getStringOr("role","").equals("shotgun")&&p.level() instanceof ServerLevel l&&!l.getGameRules().get(GameRules.MOB_GRIEFING)?Level.ExplosionInteraction.NONE:original;}
}
