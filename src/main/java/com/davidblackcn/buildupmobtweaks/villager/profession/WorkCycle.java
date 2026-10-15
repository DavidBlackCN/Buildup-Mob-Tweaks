package com.davidblackcn.buildupmobtweaks.villager.profession;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import com.davidblackcn.buildupmobtweaks.villager.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.item.Item;

/** A real, interruptible workstation action. No production or inventory writes. */
public class WorkCycle implements WorkActivity {
    private final Identifier id;private final Item prop;private int ticks;
    public WorkCycle(String name,Item prop){this.id=BuildupMobTweaks.id(name);this.prop=prop;}
    public Identifier id(){return id;}public int interactionTier(){return 0;}
    public boolean canStart(Context c){return BuildupMobTweaks.config().villagers.professions.daily_routines.get()
            &&c.villager().getBrain().getMemory(MemoryModuleType.JOB_SITE).filter(s->s.pos().closerToCenterThan(c.villager().position(),1.73)).isPresent();}
    public void start(Context c){ticks=0;var v=c.villager();var r=ProfessionWorkState.runtime(v);r.look=new BlockPosTracker(v.getBrain().getMemory(MemoryModuleType.JOB_SITE).orElseThrow().pos());v.getBrain().setMemory(MemoryModuleType.LOOK_TARGET,r.look);ProfessionWorkState.visual(v,prop,1);v.playWorkSound();}
    public Result tick(Context c){return ++ticks>=60?Result.COMPLETE:Result.CONTINUE;}
    public void stop(Context c,StopReason reason){}
}
