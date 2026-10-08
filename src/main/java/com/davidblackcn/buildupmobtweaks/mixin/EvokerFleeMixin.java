package com.davidblackcn.buildupmobtweaks.mixin;
import com.davidblackcn.buildupmobtweaks.combat.EnvironmentCombat;
import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.monster.illager.Evoker;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(AvoidEntityGoal.class)
public abstract class EvokerFleeMixin {
    @Shadow @Final protected PathfinderMob mob;
    @ModifyArg(method="tick()V",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/ai/navigation/PathNavigation;setSpeedModifier(D)V"),index=0)
    private double buildup$speed(double original) { return mob instanceof Evoker && EnvironmentCombat.on(mob,FeatureId.EVOKER_FLEE_SPEED)?Math.min(original,.8):original; }
    @ModifyArg(method="start()V",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/ai/navigation/PathNavigation;moveTo(Lnet/minecraft/world/level/pathfinder/Path;D)Z"),index=1)
    private double buildup$startSpeed(double original) { return mob instanceof Evoker && EnvironmentCombat.on(mob,FeatureId.EVOKER_FLEE_SPEED)?Math.min(original,.8):original; }
}