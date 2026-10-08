package com.davidblackcn.buildupmobtweaks.mixin;
import com.davidblackcn.buildupmobtweaks.combat.HostileCombat;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(targets = "net.minecraft.world.entity.monster.Ghast$GhastShootFireballGoal")
public abstract class GhastCombatMixin {
    @Shadow @Final private Ghast ghast;
    @Shadow public int chargeTime;
    @Inject(method = "tick()V", at = @At("HEAD"), cancellable = true)
    private void buildup$cooldown(CallbackInfo ci) {
        if (HostileCombat.instance() != null && !HostileCombat.instance().canGhastCharge(ghast)) {
            chargeTime = 0; ghast.setCharging(false); ci.cancel();
        }
    }
    @Redirect(method = "tick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean buildup$projectile(Level level, Entity entity) {
        if (entity instanceof LargeFireball ball && HostileCombat.instance() != null) HostileCombat.instance().fireball(ghast, ball);
        return level.addFreshEntity(entity);
    }
}