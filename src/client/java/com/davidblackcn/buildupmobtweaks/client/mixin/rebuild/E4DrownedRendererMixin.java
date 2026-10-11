package com.davidblackcn.buildupmobtweaks.client.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.client.rebuild.E4RenderState;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.entity.DrownedRenderer;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(DrownedRenderer.class)
public abstract class E4DrownedRendererMixin {
    @ModifyConstant(method="setupRotations(Lnet/minecraft/client/renderer/entity/state/ZombieRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;FF)V",constant=@Constant(floatValue=-10))
    private float buildup$swim(float original,@Local(argsOnly=true) ZombieRenderState s){return (((E4RenderState)s).buildup$e4()&4)!=0?-80:original;}
}
