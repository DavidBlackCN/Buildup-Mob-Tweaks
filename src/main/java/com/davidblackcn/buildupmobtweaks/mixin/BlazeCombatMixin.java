package com.davidblackcn.buildupmobtweaks.mixin;
import com.davidblackcn.buildupmobtweaks.combat.HostileCombat;
import net.minecraft.world.entity.monster.Blaze;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(targets = "net.minecraft.world.entity.monster.Blaze$BlazeAttackGoal")
public abstract class BlazeCombatMixin {
    @Shadow @Final private Blaze blaze;
    @Shadow private int attackStep;
    @Shadow private int attackTime;
    @ModifyConstant(method = "tick()V", constant = @Constant(intValue = 4))
    private int buildup$volley(int vanilla) { return HostileCombat.instance() == null ? vanilla : HostileCombat.instance().blazeVolley(blaze) + 1; }
    @Inject(method = "tick()V", at = @At("TAIL"))
    private void buildup$telegraph(CallbackInfo ci) { if (HostileCombat.instance() != null) HostileCombat.instance().blazeTick(blaze, attackStep, attackTime); }
}