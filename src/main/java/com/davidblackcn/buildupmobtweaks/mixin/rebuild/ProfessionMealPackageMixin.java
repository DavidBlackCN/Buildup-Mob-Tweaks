package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.villager.profession.EatMealBehavior;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.npc.villager.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(VillagerGoalPackages.class)
public abstract class ProfessionMealPackageMixin {
    @Inject(method="getCorePackage",at=@At("RETURN"),cancellable=true)
    private static void buildup$meal(net.minecraft.core.Holder<net.minecraft.world.entity.npc.villager.VillagerProfession> profession,float speed,CallbackInfoReturnable<ImmutableList<Pair<Integer,? extends BehaviorControl<? super Villager>>>> cir){
        var out=ImmutableList.<Pair<Integer,? extends BehaviorControl<? super Villager>>>builder();out.addAll(cir.getReturnValue());out.add(Pair.of(4,new EatMealBehavior()));cir.setReturnValue(out.build());
    }
}
