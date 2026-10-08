package com.davidblackcn.buildupmobtweaks.mixin;
import com.davidblackcn.buildupmobtweaks.combat.AdvancedHostiles;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.illager.Illusioner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Illusioner.class)
public abstract class IllusionCombatMixin {
    @Inject(method="getIllusionOffsets(F)[Lnet/minecraft/world/phys/Vec3;",at=@At("RETURN"),cancellable=true)
    private void buildup$offsets(float partialTick,org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<net.minecraft.world.phys.Vec3[]> cir) {
        if (((Illusioner)(Object)this).getAttachedOrElse(AdvancedHostiles.CLONE_VISUAL,false)) cir.setReturnValue(AdvancedHostiles.cloneOffsets());
    }
    @Inject(method="performRangedAttack(Lnet/minecraft/world/entity/LivingEntity;F)V",at=@At("TAIL"))
    private void buildup$clones(LivingEntity target,float power,CallbackInfo ci) {
        if(AdvancedHostiles.instance()!=null) AdvancedHostiles.instance().illusionShot((Illusioner)(Object)this,target,power);
    }
}