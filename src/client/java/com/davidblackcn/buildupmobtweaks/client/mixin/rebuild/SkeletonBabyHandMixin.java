package com.davidblackcn.buildupmobtweaks.client.mixin.rebuild;

import com.davidblackcn.buildupmobtweaks.client.rebuild.SkeletonBabyVisuals;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemInHandLayer.class)
public abstract class SkeletonBabyHandMixin {
    @ModifyReturnValue(method="useBabyOffset(Lnet/minecraft/client/renderer/entity/state/ArmedEntityRenderState;)Z",at=@At("RETURN"))
    private boolean buildup$sharedMeshGrip(boolean original,ArmedEntityRenderState state){
        // Uniformly scaled adult arms need their adult grip, not the short native baby-mesh offset.
        return original&&!SkeletonBabyVisuals.usesAdultMesh(state);
    }
}
