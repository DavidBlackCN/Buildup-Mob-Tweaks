package com.davidblackcn.buildupmobtweaks.client.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.client.rebuild.E4RenderState;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.zombie.*;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(DrownedModel.class)
public abstract class E4DrownedModelMixin extends ZombieModel<ZombieRenderState> {
    protected E4DrownedModelMixin(ModelPart p){super(p);}
    @Inject(method="setupSwimAnimation(Lnet/minecraft/client/renderer/entity/state/ZombieRenderState;FF)V",at=@At("TAIL"))
    private void buildup$head(ZombieRenderState s,float f,float g,CallbackInfo ci){if((((E4RenderState)s).buildup$e4()&4)!=0&&s.swimAmount>0)head.xRot-=s.swimAmount;}
}
