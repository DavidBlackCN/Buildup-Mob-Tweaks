package com.davidblackcn.buildupmobtweaks.client.mixin;
import com.davidblackcn.buildupmobtweaks.combat.HostileCombat;
import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import net.minecraft.client.model.monster.zombie.DrownedModel;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(DrownedModel.class)
public abstract class DrownedSwimMixin {
    @Inject(method="setupSwimAnimation(Lnet/minecraft/client/renderer/entity/state/ZombieRenderState;FF)V",at=@At("TAIL"))
    private void buildup$stroke(ZombieRenderState state,float limbSwing,float amount,CallbackInfo ci){
        if(HostileCombat.instance()==null||!HostileCombat.instance().gate(FeatureId.DROWNED_SWIM_VISUAL)||state.isUsingItem||state.swimAmount<=0)return;
        var model=(DrownedModel)(Object)this;float stroke=(float)Math.sin(state.ageInTicks*.2)*.25f*state.swimAmount;
        model.rightArm.xRot+=stroke;model.leftArm.xRot-=stroke;
    }
}