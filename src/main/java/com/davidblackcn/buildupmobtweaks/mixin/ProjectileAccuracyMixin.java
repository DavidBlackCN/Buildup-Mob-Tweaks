package com.davidblackcn.buildupmobtweaks.mixin;
import com.davidblackcn.buildupmobtweaks.combat.CombatPerception;
import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(Projectile.class)
public abstract class ProjectileAccuracyMixin {
    @ModifyVariable(method="shoot(DDDFF)V",at=@At("HEAD"),argsOnly=true,ordinal=1)
    private float buildup$nausea(float vanilla) { return CombatPerception.uncertainty((Projectile)(Object)this,vanilla); }
}