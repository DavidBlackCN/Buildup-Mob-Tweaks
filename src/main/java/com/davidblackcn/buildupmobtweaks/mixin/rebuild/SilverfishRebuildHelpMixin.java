/* Mob AI Tweaks adaptation: Copyright (c) 2024 N0t_UN_Owen, MIT. See NOTICE.md. */
package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.remaining.*;
import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import java.util.EnumSet;
@Mixin(targets="net.minecraft.world.entity.monster.Silverfish$SilverfishWakeUpFriendsGoal")
public abstract class SilverfishRebuildHelpMixin extends Goal {
    @Shadow @Final private Silverfish silverfish;
    @Inject(method="canUse()Z",at=@At("HEAD"),cancellable=true)
    private void buildup$flags(CallbackInfoReturnable<Boolean> ci){var b=RemainingBehavior.instance();if(b==null)return;
        setFlags(b.enabled(silverfish,FeatureId.SILVERFISH_CALL_PAUSE)?EnumSet.of(Flag.MOVE):EnumSet.noneOf(Flag.class));
        if(b.enabled(silverfish,FeatureId.SILVERFISH_BURROW)){var r=RemainingState.runtime(silverfish);long age=silverfish.tickCount-silverfish.getLastHurtByMobTimestamp();
            if(r.hidden||silverfish.getLastHurtByMob()!=null&&age<220&&silverfish.level().getGameTime()>=silverfish.getAttached(RemainingState.DATA).getLongOr("hide_ready",0))ci.setReturnValue(false);}}
    @Inject(method="tick()V",at=@At("HEAD"))
    private void buildup$pause(CallbackInfo ci){var b=RemainingBehavior.instance();if(b!=null&&b.enabled(silverfish,FeatureId.SILVERFISH_CALL_PAUSE)){silverfish.getNavigation().stop();silverfish.getMoveControl().setWait();RemainingState.runtime(silverfish).paused=true;}}
    @WrapOperation(method="tick()V",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;destroyBlock(Lnet/minecraft/core/BlockPos;ZLnet/minecraft/world/entity/Entity;)Z"))
    private boolean buildup$count(Level l,BlockPos pos,boolean drops,Entity owner,Operation<Boolean> original){boolean ok=original.call(l,pos,drops,owner);if(ok&&RemainingBehavior.instance()!=null&&RemainingState.known(silverfish)){
        var r=RemainingState.runtime(silverfish);r.helpBlocks++;if(RemainingBehavior.instance().enabled(silverfish,FeatureId.SILVERFISH_CALL_PARTICLES)&&l instanceof ServerLevel s){s.sendParticles(ParticleTypes.INFESTED,silverfish.getX(),silverfish.getY(),silverfish.getZ(),1,.1,.1,.1,.1);r.particles++;}}return ok;}
}
