package com.davidblackcn.buildupmobtweaks.client.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.client.rebuild.*;
import com.davidblackcn.buildupmobtweaks.rebuild.ability.AbilityState;
import net.minecraft.client.model.animal.golem.SnowGolemModel;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.state.SnowGolemRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.world.entity.animal.golem.SnowGolem;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(SnowGolemRenderer.class)
public abstract class SnowRendererMixin extends MobRenderer<SnowGolem,SnowGolemRenderState,SnowGolemModel> {
    @Unique private ItemModelResolver buildup$items;
    protected SnowRendererMixin(EntityRendererProvider.Context context,SnowGolemModel model,float shadow){super(context,model,shadow);}
    @Inject(method="<init>",at=@At("TAIL")) private void buildup$layer(EntityRendererProvider.Context context,CallbackInfo ci){buildup$items=context.getItemModelResolver();addLayer(new SnowballHandLayer(this));}
    @Inject(method="extractRenderState(Lnet/minecraft/world/entity/animal/golem/SnowGolem;Lnet/minecraft/client/renderer/entity/state/SnowGolemRenderState;F)V",at=@At("TAIL"))
    private void buildup$state(SnowGolem m,SnowGolemRenderState s,float partial,CallbackInfo ci){var visual=(SnowVisualState)s;
        visual.buildup$snow(m.getAttachedOrCreate(AbilityState.THROW),m.getAttachedOrCreate(AbilityState.LEFT),partial);
        if(visual.buildup$throwTicks()>4)buildup$items.updateForLiving(visual.buildup$heldSnowball(),Items.SNOWBALL.getDefaultInstance(),ItemDisplayContext.GROUND,m);else visual.buildup$heldSnowball().clear();
    }
}
