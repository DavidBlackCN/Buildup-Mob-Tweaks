package com.davidblackcn.buildupmobtweaks.client.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.client.rebuild.ProfessionVisuals;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.monster.piglin.ZombifiedPiglinModel;
import net.minecraft.client.renderer.entity.state.ZombifiedPiglinRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ZombifiedPiglinModel.class)
public abstract class ProfessionPiglinModelMixin {
    @Inject(method="setupAttackAnimation(Lnet/minecraft/client/renderer/entity/state/ZombifiedPiglinRenderState;)V",at=@At("TAIL"))
    private void buildup$actualCrossbow(ZombifiedPiglinRenderState state,CallbackInfo ci){ProfessionVisuals.crossbow((HumanoidModel<?>)(Object)this,state);}
}
