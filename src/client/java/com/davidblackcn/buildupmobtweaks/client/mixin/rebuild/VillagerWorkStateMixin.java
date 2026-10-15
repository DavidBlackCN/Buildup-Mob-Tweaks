package com.davidblackcn.buildupmobtweaks.client.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.client.rebuild.ProfessionRenderStateAccess;
import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import org.spongepowered.asm.mixin.*;
@Mixin(VillagerRenderState.class)
public abstract class VillagerWorkStateMixin implements ProfessionRenderStateAccess {
    @Unique private int buildup$visual;
    public int buildup$workVisual(){return buildup$visual;}
    public void buildup$workVisual(int flags){buildup$visual=flags;}
}
