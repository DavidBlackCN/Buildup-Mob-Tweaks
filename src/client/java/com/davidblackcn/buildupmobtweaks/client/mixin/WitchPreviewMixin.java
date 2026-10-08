package com.davidblackcn.buildupmobtweaks.client.mixin;
import com.davidblackcn.buildupmobtweaks.combat.RaidCombat;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import net.minecraft.client.renderer.entity.WitchRenderer;
import net.minecraft.client.renderer.entity.state.*;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.item.ItemDisplayContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(WitchRenderer.class)
public abstract class WitchPreviewMixin {
    @WrapOperation(method="extractRenderState(Lnet/minecraft/world/entity/monster/Witch;Lnet/minecraft/client/renderer/entity/state/WitchRenderState;F)V",
            at=@At(value="INVOKE",target="Lnet/minecraft/client/renderer/entity/state/HoldingEntityRenderState;extractHoldingEntityRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/HoldingEntityRenderState;Lnet/minecraft/client/renderer/item/ItemModelResolver;)V"))
    private void buildup$preview(LivingEntity mob,HoldingEntityRenderState state,ItemModelResolver resolver,Operation<Void> original) {
        var stack=mob.getAttached(RaidCombat.POTION_PREVIEW);
        if(stack!=null && !stack.isEmpty() && mob.getMainHandItem().isEmpty()) resolver.updateForLiving(state.heldItem,stack,ItemDisplayContext.GROUND,mob);
        else original.call(mob,state,resolver);
    }
    @Inject(method="extractRenderState(Lnet/minecraft/world/entity/monster/Witch;Lnet/minecraft/client/renderer/entity/state/WitchRenderState;F)V",at=@At("TAIL"))
    private void buildup$pose(Witch mob,WitchRenderState state,float partial,CallbackInfo ci) {
        var stack=mob.getAttached(RaidCombat.POTION_PREVIEW);
        if(stack!=null && !stack.isEmpty() && mob.getMainHandItem().isEmpty()) {state.isHoldingItem=true; state.isHoldingPotion=false;}
    }
}