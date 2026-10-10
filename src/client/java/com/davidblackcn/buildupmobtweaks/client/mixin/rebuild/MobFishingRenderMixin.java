package com.davidblackcn.buildupmobtweaks.client.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.client.rebuild.ProfessionVisuals;
import com.davidblackcn.buildupmobtweaks.rebuild.profession.ProfessionBehavior;
import net.minecraft.client.renderer.entity.FishingHookRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.state.FishingHookRenderState;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.FishingHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(FishingHookRenderer.class)
public abstract class MobFishingRenderMixin extends EntityRenderer<FishingHook,FishingHookRenderState> {
    protected MobFishingRenderMixin(EntityRendererProvider.Context context){super(context);}
    @Inject(method="shouldRender(Lnet/minecraft/world/entity/projectile/FishingHook;Lnet/minecraft/client/renderer/culling/Frustum;DDDF)Z",at=@At("RETURN"),cancellable=true)
    private void buildup$mobVisibility(FishingHook h,Frustum frustum,double camX,double camY,double camZ,float partial,CallbackInfoReturnable<Boolean> ci){
        // Native FishingHookRenderer rejects non-player owners before extracting or submitting the line.
        // Keep its superclass distance/culling checks and only admit our marked mob hooks.
        if(!ci.getReturnValueZ()&&ProfessionBehavior.mobHook(h))ci.setReturnValue(super.shouldRender(h,frustum,camX,camY,camZ,partial));
    }
    @Inject(method="extractRenderState(Lnet/minecraft/world/entity/projectile/FishingHook;Lnet/minecraft/client/renderer/entity/state/FishingHookRenderState;F)V",at=@At("RETURN"))
    private void buildup$line(FishingHook h,FishingHookRenderState state,float partial,CallbackInfo ci){
        if(ProfessionBehavior.mobHook(h)&&h.getOwner() instanceof Mob m)state.lineOriginOffset=ProfessionVisuals.rodHand(m,partial).subtract(h.getPosition(partial).add(0,.25,0));
    }
}
