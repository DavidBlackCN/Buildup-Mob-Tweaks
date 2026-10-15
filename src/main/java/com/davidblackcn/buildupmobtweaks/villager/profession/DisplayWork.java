package com.davidblackcn.buildupmobtweaks.villager.profession;
public final class DisplayWork extends ResourceWork {
    private final String kind;private OwnedWorkDisplays.Plan plan;
    public DisplayWork(String kind){super("display_"+kind);this.kind=kind;}
    public boolean canStart(Context c){plan=OwnedWorkDisplays.find(c,kind);return plan!=null;}
    public void start(Context c){super.start(c);WorkMotion.look(c,plan.pos());ProfessionWorkState.visual(c.villager(),plan.prop(),2);}
    public Result tick(Context c){if(!valid(c))return Result.FAILED;if(++workTicks<20)return Result.CONTINUE;if(!OwnedWorkDisplays.place(c,plan))return Result.FAILED;commit(c);return Result.COMPLETE;}
}
