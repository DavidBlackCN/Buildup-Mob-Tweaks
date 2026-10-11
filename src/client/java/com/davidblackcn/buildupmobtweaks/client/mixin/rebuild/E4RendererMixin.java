package com.davidblackcn.buildupmobtweaks.client.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.client.rebuild.E4RenderState;
import com.davidblackcn.buildupmobtweaks.rebuild.general.E4VisualState;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.util.ARGB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(LivingEntityRenderer.class)
public abstract class E4RendererMixin {
    @Inject(method="extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",at=@At("TAIL"))
    private void buildup$state(LivingEntity e,LivingEntityRenderState s,float f,CallbackInfo ci){((E4RenderState)s).buildup$e4(e.getAttachedOrCreate(E4VisualState.FLAGS));}
    @ModifyReturnValue(method="getModelTint(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;)I",at=@At("RETURN"))
    private int buildup$tint(int original,LivingEntityRenderState s){int flags=((E4RenderState)s).buildup$e4();return (flags&1)!=0?ARGB.multiply(original,ARGB.color(255,255,s.hasRedOverlay?102:165,0)):(flags&2)!=0?ARGB.multiply(original,ARGB.color(255,173,216,230)):original;}
}
