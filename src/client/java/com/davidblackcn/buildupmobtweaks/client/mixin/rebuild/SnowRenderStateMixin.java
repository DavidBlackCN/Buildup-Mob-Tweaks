package com.davidblackcn.buildupmobtweaks.client.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.client.rebuild.SnowVisualState;
import net.minecraft.client.renderer.entity.state.SnowGolemRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.spongepowered.asm.mixin.*;
@Mixin(SnowGolemRenderState.class)
public abstract class SnowRenderStateMixin implements SnowVisualState {
    @Unique private int buildup$ticks;@Unique private boolean buildup$left;@Unique private float buildup$partial;
    @Unique private final ItemStackRenderState buildup$ball=new ItemStackRenderState();
    public void buildup$snow(int ticks,boolean left,float partial){buildup$ticks=ticks;buildup$left=left;buildup$partial=partial;}
    public int buildup$throwTicks(){return buildup$ticks;}public boolean buildup$throwLeft(){return buildup$left;}public float buildup$throwPartial(){return buildup$partial;}
    public ItemStackRenderState buildup$heldSnowball(){return buildup$ball;}
}
