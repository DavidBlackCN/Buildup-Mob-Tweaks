package com.davidblackcn.buildupmobtweaks.client.mixin;
import com.davidblackcn.buildupmobtweaks.combat.HostileCombat;
import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import net.minecraft.client.renderer.entity.GhastRenderer;
import net.minecraft.client.renderer.entity.state.GhastRenderState;
import net.minecraft.world.entity.monster.Ghast;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(GhastRenderer.class)
public abstract class GhastVisualMixin {
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/monster/Ghast;Lnet/minecraft/client/renderer/entity/state/GhastRenderState;F)V", at = @At("TAIL"))
    private void buildup$charge(Ghast mob, GhastRenderState state, float partial, CallbackInfo ci) {
        if (HostileCombat.instance() != null && HostileCombat.instance().gate(FeatureId.GHAST_TELEGRAPH) && state.isCharging) {
            state.scale *= 1.08f; state.hasRedOverlay |= (mob.tickCount / 3) % 2 == 0;
        }
    }
}