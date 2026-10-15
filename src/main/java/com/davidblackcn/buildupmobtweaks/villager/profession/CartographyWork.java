package com.davidblackcn.buildupmobtweaks.villager.profession;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Items;

/** One loaded, short exploration leg followed by an actual return to the original feet position. */
public final class CartographyWork extends WorkCycle {
    private BlockPos origin,outbound;private int ticks,pause;private boolean returning;
    public CartographyWork(){super("cartographer_work",Items.MAP);}
    public boolean canStart(Context c){if(!super.canStart(c))return false;origin=c.villager().blockPosition();for(var d:net.minecraft.core.Direction.Plane.HORIZONTAL){var p=origin.relative(d,3);if(WorkMotion.clearFoot(c.level(),p)){outbound=p;return true;}}return false;}
    public void start(Context c){super.start(c);ticks=pause=0;returning=false;}
    public Result tick(Context c){if(++ticks>500)return Result.FAILED;if(!returning){if(!WorkMotion.reach(c,outbound,.8))return Result.CONTINUE;WorkMotion.look(c,outbound.relative(net.minecraft.core.Direction.NORTH,2));if(++pause>=40)returning=true;return Result.CONTINUE;}return WorkMotion.reach(c,origin,.8)?Result.COMPLETE:Result.CONTINUE;}
}
