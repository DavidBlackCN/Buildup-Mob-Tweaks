package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.general.E4Behavior;
import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.tags.DamageTypeTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(LivingEntity.class)
public abstract class E4VexDamageMixin {
    @ModifyVariable(method="hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z",at=@At("HEAD"),argsOnly=true)
    private float buildup$weakness(float amount,ServerLevel l,DamageSource source,float original){return (Object)this instanceof Vex v&&amount>0&&Float.isFinite(amount)
        &&source.is(DamageTypeTags.IS_PROJECTILE)&&E4Behavior.enabled(v,FeatureId.VEX_PROJECTILE_WEAKNESS)?(float)Math.min(Float.MAX_VALUE,(double)amount*3):amount;}
}
