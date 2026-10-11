package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.general.DiscoveryContext;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(NearestAttackableTargetGoal.class)
public abstract class E4DiscoveryMixin extends TargetGoal {
    protected E4DiscoveryMixin(Mob m,boolean see,boolean reach){super(m,see,reach);}
    @ModifyReturnValue(method="getTargetConditions()Lnet/minecraft/world/entity/ai/targeting/TargetingConditions;",at=@At("RETURN"))
    private TargetingConditions buildup$discovery(TargetingConditions original){
        if(mob.getTarget()!=null||!com.davidblackcn.buildupmobtweaks.rebuild.general.E4Behavior.hasDiscovery(mob))return original;
        var copy=original.copy();((DiscoveryContext)(Object)copy).buildup$discoverer(mob);return copy;
    }
}
