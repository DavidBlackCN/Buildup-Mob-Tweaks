package com.davidblackcn.buildupmobtweaks.client.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.client.rebuild.SkeletonAnimationState;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import org.spongepowered.asm.mixin.*;
@Mixin(SkeletonRenderState.class)
public abstract class SkeletonRenderStateMixin implements SkeletonAnimationState {
    @Unique private int buildup$kind,buildup$ticks;
    @Unique private float buildup$partial;
    public void buildup$animation(int kind,int ticks,float partial){buildup$kind=kind;buildup$ticks=ticks;buildup$partial=partial;}
    public int buildup$kind(){return buildup$kind;}
    public int buildup$ticks(){return buildup$ticks;}
    public float buildup$partial(){return buildup$partial;}
}
