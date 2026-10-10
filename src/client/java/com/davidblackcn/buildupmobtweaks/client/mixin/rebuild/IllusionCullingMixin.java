package com.davidblackcn.buildupmobtweaks.client.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.illusion.IllusionBehavior;
import net.minecraft.client.renderer.entity.IllusionerRenderer;
import net.minecraft.world.entity.monster.illager.Illusioner;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(IllusionerRenderer.class)
public abstract class IllusionCullingMixin {
    @Inject(method="extractRenderState(Lnet/minecraft/world/entity/monster/illager/Illusioner;Lnet/minecraft/client/renderer/entity/state/IllusionerRenderState;F)V",at=@At("RETURN"))
    private void buildup$bowHand(Illusioner m,net.minecraft.client.renderer.entity.state.IllusionerRenderState state,float f,org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci){var hand=IllusionBehavior.bow(m);boolean managed=hand!=null&&IllusionBehavior.offsets(m)!=null;((com.davidblackcn.buildupmobtweaks.client.rebuild.IllusionRenderAccess)state).buildup$managedBow(managed);if(managed)state.mainArm=hand==net.minecraft.world.InteractionHand.MAIN_HAND?m.getMainArm():m.getMainArm().getOpposite();}
    @Inject(method="getBoundingBoxForCulling(Lnet/minecraft/world/entity/monster/illager/Illusioner;F)Lnet/minecraft/world/phys/AABB;",at=@At("RETURN"),cancellable=true)
    private void buildup$clones(Illusioner m,float f,CallbackInfoReturnable<AABB> ci){if(IllusionBehavior.offsets(m)!=null)ci.setReturnValue(ci.getReturnValue().inflate(3,0,3));}
}
