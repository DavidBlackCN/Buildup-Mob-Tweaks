package com.davidblackcn.buildupmobtweaks.mixin;
import com.davidblackcn.buildupmobtweaks.combat.EnvironmentCombat;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.monster.breeze.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(LongJump.class)
public abstract class BreezeBurstMixin {
    @WrapOperation(method="tick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/monster/breeze/Breeze;J)V",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/monster/breeze/Breeze;setPose(Lnet/minecraft/world/entity/Pose;)V"))
    private void buildup$burst(Breeze mob,Pose pose,Operation<Void> original) { var old=mob.getPose(); original.call(mob,pose); EnvironmentCombat.breezeTransition(mob,old,pose); }
}