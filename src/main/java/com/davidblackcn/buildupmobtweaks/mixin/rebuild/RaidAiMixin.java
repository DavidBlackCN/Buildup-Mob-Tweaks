package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.evoker.EvokerBehavior;
import com.davidblackcn.buildupmobtweaks.rebuild.vex.VexBehavior;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.illager.Evoker;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.DifficultyInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(Mob.class)
public abstract class RaidAiMixin {
    @Inject(method="finalizeSpawn(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/DifficultyInstance;Lnet/minecraft/world/entity/EntitySpawnReason;Lnet/minecraft/world/entity/SpawnGroupData;)Lnet/minecraft/world/entity/SpawnGroupData;",at=@At("TAIL"))
    private void buildup$born(ServerLevelAccessor l,DifficultyInstance d,EntitySpawnReason reason,SpawnGroupData group,CallbackInfoReturnable<SpawnGroupData> ci){if((Object)this instanceof Evoker m&&EvokerBehavior.instance()!=null)EvokerBehavior.instance().born(m,reason);}
    @Inject(method="serverAiStep()V",at=@At("HEAD"))
    private void buildup$ai(CallbackInfo ci){if((Object)this instanceof Evoker m&&EvokerBehavior.instance()!=null)EvokerBehavior.instance().beforeAi(m);
        if((Object)this instanceof Vex m&&VexBehavior.instance()!=null)VexBehavior.instance().tick(m);}
}
