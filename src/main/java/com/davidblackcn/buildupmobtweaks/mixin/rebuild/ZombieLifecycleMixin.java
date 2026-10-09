package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.zombie.*;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(Zombie.class)
public abstract class ZombieLifecycleMixin {
    @Inject(method="tick()V",at=@At("TAIL"))
    private void buildup$finish(CallbackInfo ci){var m=(Zombie)(Object)this;if(!m.level().isClientSide()&&ZombieBehavior.instance()!=null)ZombieBehavior.instance().afterTick(m);}
    @Inject(method="finalizeSpawn(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/DifficultyInstance;Lnet/minecraft/world/entity/EntitySpawnReason;Lnet/minecraft/world/entity/SpawnGroupData;)Lnet/minecraft/world/entity/SpawnGroupData;",at=@At("TAIL"))
    private void buildup$born(ServerLevelAccessor level,DifficultyInstance difficulty,EntitySpawnReason reason,SpawnGroupData data,CallbackInfoReturnable<SpawnGroupData> ci){
        if(ZombieBehavior.instance()!=null)ZombieBehavior.instance().born((Zombie)(Object)this,level,difficulty,reason);
    }
}
