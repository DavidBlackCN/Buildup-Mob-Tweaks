package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.evoker.EvokerBehavior;
import com.davidblackcn.buildupmobtweaks.rebuild.raid.RaidState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.illager.Evoker;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(LivingEntity.class)
public abstract class RaidDeathMixin {
    @Inject(method="die(Lnet/minecraft/world/damagesource/DamageSource;)V",at=@At("HEAD"))
    private void buildup$death(DamageSource source,CallbackInfo ci){if((Object)this instanceof Evoker m&&RaidState.known(m,RaidState.EVOKER)&&EvokerBehavior.instance()!=null)EvokerBehavior.instance().cleanup(m,true);}
}
