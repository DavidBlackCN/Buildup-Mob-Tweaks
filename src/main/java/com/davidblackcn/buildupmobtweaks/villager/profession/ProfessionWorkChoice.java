package com.davidblackcn.buildupmobtweaks.villager.profession;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.npc.villager.Villager;

/** Preserve the old choice and weight. Farmer's existing implementation gets the first attempt. */
public final class ProfessionWorkChoice implements BehaviorControl<Villager> {
    private final BehaviorControl<? super Villager> original;
    private final ProfessionWorkBehavior work=new ProfessionWorkBehavior();
    private BehaviorControl<? super Villager> selected;
    public ProfessionWorkChoice(BehaviorControl<? super Villager> original){this.original=original;}
    public java.util.Set<net.minecraft.world.entity.ai.memory.MemoryModuleType<?>> getRequiredMemories(){var out=new java.util.HashSet<>(original.getRequiredMemories());out.addAll(work.getRequiredMemories());return java.util.Set.copyOf(out);}
    public Behavior.Status getStatus(){return selected==null?Behavior.Status.STOPPED:selected.getStatus();}
    public boolean tryStart(ServerLevel l,Villager v,long time){
        if(original.tryStart(l,v,time)){selected=original;return true;}
        if(work.tryStart(l,v,time)){selected=work;return true;}return false;
    }
    public void tickOrStop(ServerLevel l,Villager v,long time){if(selected!=null)selected.tickOrStop(l,v,time);}
    public void doStop(ServerLevel l,Villager v,long time){if(selected!=null)selected.doStop(l,v,time);selected=null;}
    public String debugString(){return "ProfessionWorkChoice["+(selected==null?"idle":selected.debugString())+"]";}
}
