package com.davidblackcn.buildupmobtweaks.client.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.client.rebuild.ProfessionRenderStateAccess;
import com.davidblackcn.buildupmobtweaks.villager.profession.ProfessionWorkState;
import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(VillagerRenderer.class)
public abstract class VillagerWorkRendererMixin {

    @Inject(method="extractRenderState(Lnet/minecraft/world/entity/npc/villager/Villager;Lnet/minecraft/client/renderer/entity/state/VillagerRenderState;F)V",at=@At("TAIL"))
    private void buildup$visualOnlyProp(Villager v,VillagerRenderState state,float partial,CallbackInfo ci){
        int flags=v.hasAttached(ProfessionWorkState.VISUAL)?v.getAttached(ProfessionWorkState.VISUAL):0;((ProfessionRenderStateAccess)state).buildup$workVisual(flags);
        if(flags!=0&&v.getMainHandItem().isEmpty()){var item=net.minecraft.core.registries.BuiltInRegistries.ITEM.byId(flags>>4);((LivingItemModelAccess)this).buildup$itemModels().updateForLiving(state.heldItem,new ItemStack(item),ItemDisplayContext.GROUND,v);}
    }
}
