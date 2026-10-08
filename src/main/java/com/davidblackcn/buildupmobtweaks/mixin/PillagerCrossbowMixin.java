package com.davidblackcn.buildupmobtweaks.mixin;
import com.davidblackcn.buildupmobtweaks.combat.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.RangedCrossbowAttackGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.illager.Pillager;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(RangedCrossbowAttackGoal.class)
public abstract class PillagerCrossbowMixin {
    @Shadow @Final private Monster mob;
    @Inject(method = "isHoldingCrossbow()Z", at = @At("HEAD"), cancellable = true)
    private void buildup$meleeHand(CallbackInfoReturnable<Boolean> cir) {
        if (mob instanceof Pillager pillager && RaidCombat.instance() != null && RaidCombat.instance().melee(pillager)) cir.setReturnValue(false);
        else if (HostileCombat.instance() != null && HostileCombat.instance().crossbow(mob)) cir.setReturnValue(true);
    }
    @Redirect(method = "tick()V", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/ai/goal/RangedCrossbowAttackGoal;attackRadiusSqr:F"))
    private float buildup$range(RangedCrossbowAttackGoal<?> goal) {
        return HostileCombat.instance() != null && mob instanceof Pillager && HostileCombat.instance().enabled(mob,
                com.davidblackcn.buildupmobtweaks.feature.FeatureId.PILLAGER_RANGE) ? 15 * 15 : attackRadiusSqr;
    }
    @Shadow @Final private float attackRadiusSqr;
    @Redirect(method = "stop()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Monster;setTarget(Lnet/minecraft/world/entity/LivingEntity;)V"))
    private void buildup$retainSwitchTarget(Monster entity, LivingEntity target) {
        if (entity instanceof Pillager pillager && RaidCombat.instance() != null && RaidCombat.instance().melee(pillager)
                && SkeletonCombat.validTarget(pillager, pillager.getTarget())) return;
        entity.setTarget(target);
    }
    @Inject(method = "tick()V", at = @At("TAIL"))
    private void buildup$retreat(CallbackInfo ci) {
        if (mob instanceof Pillager pillager && RaidCombat.instance() != null) RaidCombat.instance().retreat(pillager);
    }
}
