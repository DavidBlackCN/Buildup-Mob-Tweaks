package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.remaining.*;
import net.minecraft.world.entity.Mob;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(Mob.class)
public abstract class RemainingAiMixin {
    @Inject(method="serverAiStep()V",at=@At("HEAD"))
    private void buildup$ai(CallbackInfo ci){if(RemainingBehavior.instance()!=null)RemainingBehavior.instance().beforeAi((Mob)(Object)this);}
    @Inject(method="finalizeSpawn(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/DifficultyInstance;Lnet/minecraft/world/entity/EntitySpawnReason;Lnet/minecraft/world/entity/SpawnGroupData;)Lnet/minecraft/world/entity/SpawnGroupData;",at=@At("TAIL"))
    private void buildup$birth(net.minecraft.world.level.ServerLevelAccessor l,net.minecraft.world.DifficultyInstance d,net.minecraft.world.entity.EntitySpawnReason reason,net.minecraft.world.entity.SpawnGroupData group,CallbackInfoReturnable<net.minecraft.world.entity.SpawnGroupData> ci){if(RemainingBehavior.instance()!=null)RemainingBehavior.instance().born((Mob)(Object)this,reason);}
}
