package com.davidblackcn.buildupmobtweaks.mixin;
import com.davidblackcn.buildupmobtweaks.combat.EnvironmentCombat;
import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(targets="net.minecraft.world.entity.monster.Silverfish$SilverfishWakeUpFriendsGoal")
public abstract class SilverfishCallMixin {
    @Shadow @Final private Silverfish silverfish;
    @Inject(method="canUse()Z",at=@At("HEAD"))
    private void buildup$flags(org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> cir) {
        ((net.minecraft.world.entity.ai.goal.Goal)(Object)this).setFlags(EnvironmentCombat.on(silverfish,FeatureId.SILVERFISH_CALL_PAUSE)
                ? java.util.EnumSet.of(net.minecraft.world.entity.ai.goal.Goal.Flag.MOVE) : java.util.EnumSet.noneOf(net.minecraft.world.entity.ai.goal.Goal.Flag.class));
    }
    @Inject(method="tick()V",at=@At("HEAD"))
    private void buildup$pause(CallbackInfo ci) {
        if(EnvironmentCombat.on(silverfish,FeatureId.SILVERFISH_CALL_PAUSE)) {silverfish.getNavigation().stop(); silverfish.getMoveControl().setWait();}
    }
    @Redirect(method="tick()V",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;destroyBlock(Lnet/minecraft/core/BlockPos;ZLnet/minecraft/world/entity/Entity;)Z"))
    private boolean buildup$count(Level level,BlockPos pos,boolean drops,Entity entity) {
        boolean changed=level.destroyBlock(pos,drops,entity);
        if(changed && EnvironmentCombat.on(silverfish,FeatureId.SILVERFISH_CALL_PARTICLES))
            ((ServerLevel)level).sendParticles(ParticleTypes.HAPPY_VILLAGER,pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5,1,0,0,0,0);
        return changed;
    }
}