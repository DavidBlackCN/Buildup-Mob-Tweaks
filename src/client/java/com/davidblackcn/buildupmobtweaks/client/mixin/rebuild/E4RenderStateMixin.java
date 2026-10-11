package com.davidblackcn.buildupmobtweaks.client.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.client.rebuild.E4RenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.*;
@Mixin(LivingEntityRenderState.class)
public abstract class E4RenderStateMixin implements E4RenderState {
    @Unique private int buildup$flags;
    public int buildup$e4(){return buildup$flags;}public void buildup$e4(int flags){buildup$flags=flags;}
}
