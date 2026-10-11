package com.davidblackcn.buildupmobtweaks.client.rebuild;
import net.minecraft.client.renderer.item.ItemStackRenderState;
public interface SnowVisualState {
    void buildup$snow(int ticks,boolean left,float partial);
    int buildup$throwTicks();boolean buildup$throwLeft();float buildup$throwPartial();
    ItemStackRenderState buildup$heldSnowball();
}
