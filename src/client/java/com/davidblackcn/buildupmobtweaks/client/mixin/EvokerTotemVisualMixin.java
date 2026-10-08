package com.davidblackcn.buildupmobtweaks.client.mixin;
import com.davidblackcn.buildupmobtweaks.combat.AdvancedHostiles;
import net.minecraft.client.renderer.entity.EvokerRenderer;
import net.minecraft.client.renderer.entity.state.EvokerRenderState;
import net.minecraft.world.entity.monster.illager.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(EvokerRenderer.class)
public abstract class EvokerTotemVisualMixin {
    @Inject(method="extractRenderState(Lnet/minecraft/world/entity/monster/illager/SpellcasterIllager;Lnet/minecraft/client/renderer/entity/state/EvokerRenderState;F)V",at=@At("TAIL"))
    private void buildup$totem(SpellcasterIllager mob,EvokerRenderState state,float partial,CallbackInfo ci){
        if(mob instanceof Evoker && AdvancedHostiles.marked(mob.getOffhandItem())){state.isCastingSpell=true;state.armPose=AbstractIllager.IllagerArmPose.SPELLCASTING;}
    }
}