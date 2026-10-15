package com.davidblackcn.buildupmobtweaks.mixin.rebuild;

import com.davidblackcn.buildupmobtweaks.villager.profession.ProfessionWorkChoice;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.npc.villager.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import java.util.*;

@Mixin(VillagerGoalPackages.class)
public abstract class ProfessionWorkPackageMixin {
    @ModifyArg(method="getWorkPackage",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/ai/behavior/RunOne;<init>(Ljava/util/List;)V"),index=0)
    private static List<Pair<? extends BehaviorControl<? super Villager>,Integer>> buildup$singleProfessionChoice(List<Pair<? extends BehaviorControl<? super Villager>,Integer>> list){
        var out=new ArrayList<Pair<? extends BehaviorControl<? super Villager>,Integer>>();
        for(var e:list)out.add(e.getFirst() instanceof HarvestFarmland?Pair.of(new ProfessionWorkChoice(e.getFirst()),e.getSecond()):e);
        return List.copyOf(out);
    }
}
