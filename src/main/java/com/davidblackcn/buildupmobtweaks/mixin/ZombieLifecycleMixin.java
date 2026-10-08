package com.davidblackcn.buildupmobtweaks.mixin;

import com.davidblackcn.buildupmobtweaks.combat.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.zombie.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class ZombieLifecycleMixin {
    @Inject(method = "serverAiStep()V", at = @At("HEAD"))
    private void buildup$zombieTick(CallbackInfo ci) {
        if ((Object) this instanceof Zombie zombie && ZombieCombat.instance() != null) ZombieCombat.instance().tick(zombie);
    }
    @Inject(method = "dropCustomDeathLoot(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;Z)V", at = @At("HEAD"))
    private void buildup$restoreFlightForVanillaLoot(ServerLevel level, DamageSource source, boolean recentlyHit, CallbackInfo ci) {
        if ((Object) this instanceof Drowned drowned && DrownedTridents.instance() != null) DrownedTridents.instance().beforeDeathLoot(drowned);
    }
}