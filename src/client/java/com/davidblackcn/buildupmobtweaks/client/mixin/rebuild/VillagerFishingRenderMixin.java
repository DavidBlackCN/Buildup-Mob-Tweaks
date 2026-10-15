package com.davidblackcn.buildupmobtweaks.client.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.villager.profession.NpcFishingHooks;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.state.FishingHookRenderState;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.projectile.FishingHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(FishingHookRenderer.class)
public abstract class VillagerFishingRenderMixin extends EntityRenderer<FishingHook,FishingHookRenderState> {
    protected VillagerFishingRenderMixin(EntityRendererProvider.Context context){super(context);}
    @Inject(method="shouldRender(Lnet/minecraft/world/entity/projectile/FishingHook;Lnet/minecraft/client/renderer/culling/Frustum;DDDF)Z",at=@At("HEAD"),cancellable=true)
    private void buildup$npcVisibility(FishingHook h,Frustum f,double x,double y,double z,float partial,CallbackInfoReturnable<Boolean> ci){if(NpcFishingHooks.marked(h))ci.setReturnValue(super.shouldRender(h,f,x,y,z,partial));}
    @Inject(method="extractRenderState(Lnet/minecraft/world/entity/projectile/FishingHook;Lnet/minecraft/client/renderer/entity/state/FishingHookRenderState;F)V",at=@At("RETURN"))
    private void buildup$npcLine(FishingHook h,FishingHookRenderState state,float partial,CallbackInfo ci){if(NpcFishingHooks.marked(h)&&h.getOwner() instanceof Villager v){com.davidblackcn.buildupmobtweaks.client.rebuild.NpcFishingClientHooks.observe(v,h);double angle=Math.toRadians(net.minecraft.util.Mth.rotLerp(partial,v.yBodyRotO,v.yBodyRot));state.lineOriginOffset=v.getPosition(partial).add(-Math.sin(angle)*.35,1.15,Math.cos(angle)*.35).subtract(h.getPosition(partial).add(0,.25,0));}}
}
