/* Mob AI Tweaks adaptation: Copyright (c) 2024 N0t_UN_Owen, MIT. See NOTICE.md. */
package com.davidblackcn.buildupmobtweaks.client.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.remaining.RemainingState;
import net.minecraft.client.renderer.entity.GhastRenderer;
import net.minecraft.client.renderer.entity.state.GhastRenderState;
import net.minecraft.world.entity.monster.Ghast;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(GhastRenderer.class)
public abstract class GhastRebuildVisualMixin {
    @Inject(method="extractRenderState(Lnet/minecraft/world/entity/monster/Ghast;Lnet/minecraft/client/renderer/entity/state/GhastRenderState;F)V",at=@At("TAIL"))
    private void buildup$charge(Ghast m,GhastRenderState state,float partial,CallbackInfo ci){int charge=m.getAttachedOrCreate(RemainingState.CHARGE);if(charge>10){state.scale*=1+Math.min(40,charge)*.002f;state.hasRedOverlay|=(m.tickCount/3)%2==0;}}
}
