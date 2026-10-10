package com.davidblackcn.buildupmobtweaks.client.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.client.rebuild.ProfessionVisuals;
import com.davidblackcn.buildupmobtweaks.rebuild.profession.ProfessionBehavior;
import net.minecraft.client.renderer.entity.FishingHookRenderer;
import net.minecraft.client.renderer.entity.state.FishingHookRenderState;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.FishingHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(FishingHookRenderer.class)
public abstract class MobFishingRenderMixin {
    @Inject(method="extractRenderState(Lnet/minecraft/world/entity/projectile/FishingHook;Lnet/minecraft/client/renderer/entity/state/FishingHookRenderState;F)V",at=@At("RETURN"))
    private void buildup$line(FishingHook h,FishingHookRenderState state,float partial,CallbackInfo ci){
        if(ProfessionBehavior.mobHook(h)&&h.getOwner() instanceof Mob m)state.lineOriginOffset=ProfessionVisuals.rodHand(m,partial).subtract(h.getPosition(partial).add(0,.25,0));
    }
}
