package com.davidblackcn.buildupmobtweaks.client.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.client.rebuild.IllusionRenderAccess;
import net.minecraft.client.renderer.entity.state.IllusionerRenderState;
import org.spongepowered.asm.mixin.*;
@Mixin(IllusionerRenderState.class)
public abstract class IllusionRenderStateMixin implements IllusionRenderAccess {
    @Unique private boolean buildup$bow;
    public boolean buildup$managedBow(){return buildup$bow;}
    public void buildup$managedBow(boolean value){buildup$bow=value;}
}
