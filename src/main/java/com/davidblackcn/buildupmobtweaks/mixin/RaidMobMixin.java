package com.davidblackcn.buildupmobtweaks.mixin;
import com.davidblackcn.buildupmobtweaks.combat.RaidCombat;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Mob.class)
public abstract class RaidMobMixin {
    @Inject(method = "serverAiStep()V", at = @At("HEAD"))
    private void buildup$raidTick(CallbackInfo ci) {
        if (RaidCombat.instance() != null) RaidCombat.instance().tick((Mob) (Object) this);
    }
}
