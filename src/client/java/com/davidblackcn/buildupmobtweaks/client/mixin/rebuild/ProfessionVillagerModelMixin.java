package com.davidblackcn.buildupmobtweaks.client.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.client.rebuild.ProfessionVisuals;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.monster.zombie.ZombieVillagerModel;
import net.minecraft.client.renderer.entity.state.ZombieVillagerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ZombieVillagerModel.class)
public abstract class ProfessionVillagerModelMixin {
    @Inject(method="setupAttackAnimation(Lnet/minecraft/client/renderer/entity/state/ZombieVillagerRenderState;)V",at=@At("TAIL"))
    private void buildup$actualCrossbow(ZombieVillagerRenderState state,CallbackInfo ci){ProfessionVisuals.crossbow((HumanoidModel<?>)(Object)this,state);}
}
