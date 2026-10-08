package com.davidblackcn.buildupmobtweaks.mixin;
import com.davidblackcn.buildupmobtweaks.combat.CombatPerception;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(MeleeAttackGoal.class)
public abstract class MeleeRhythmMixin {
    @Shadow @Final protected PathfinderMob mob;
    @ModifyConstant(method={"resetAttackCooldown()V","getAttackInterval()I"},constant=@Constant(intValue=20))
    private int buildup$interval(int vanilla) { return CombatPerception.meleeTicks(mob,vanilla); }
    @Inject(method="tick()V",at=@At("TAIL"))
    private void buildup$jump(CallbackInfo ci) { CombatPerception.jump(mob); }
}