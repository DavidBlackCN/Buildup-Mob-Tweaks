package com.davidblackcn.buildupmobtweaks.mixin;
import com.davidblackcn.buildupmobtweaks.combat.RaidCombat;
import net.minecraft.world.entity.monster.illager.Evoker;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(targets = "net.minecraft.world.entity.monster.illager.Evoker$EvokerSummonSpellGoal")
public abstract class EvokerSummonMixin {
    @Shadow @Final private Evoker this$0;
    @Inject(method = "canUse()Z", at = @At("HEAD"), cancellable = true)
    private void buildup$limit(CallbackInfoReturnable<Boolean> cir) {
        if (RaidCombat.instance() != null && !RaidCombat.instance().canSummon(this$0, false)) cir.setReturnValue(false);
    }
    @Inject(method = "performSpellCasting()V", at = @At("HEAD"), cancellable = true)
    private void buildup$commit(CallbackInfo ci) {
        if (RaidCombat.instance() != null && !RaidCombat.instance().beginSummon(this$0)) ci.cancel();
    }
    @Inject(method = "getCastingInterval()I", at = @At("RETURN"), cancellable = true)
    private void buildup$cooldown(CallbackInfoReturnable<Integer> cir) {
        if (RaidCombat.instance() != null) cir.setReturnValue(RaidCombat.instance().summonInterval(this$0, cir.getReturnValue()));
    }
}
