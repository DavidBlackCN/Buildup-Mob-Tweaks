/* Mob AI Tweaks adaptation: Copyright (c) 2024 N0t_UN_Owen, MIT. See NOTICE.md. */
package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.remaining.*;
import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import com.davidblackcn.buildupmobtweaks.rebuild.zombie.ZombieBehavior;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(targets="net.minecraft.world.entity.monster.Ghast$GhastShootFireballGoal")
public abstract class GhastRebuildGoalMixin {
    @Shadow @Final private Ghast ghast;@Shadow public int chargeTime;
    @Inject(method="tick()V",at=@At("HEAD"),cancellable=true)
    private void buildup$wait(CallbackInfo ci){var b=RemainingBehavior.instance();if(b!=null&&RemainingState.known(ghast)&&RemainingState.runtime(ghast).paused){chargeTime=0;RemainingState.runtime(ghast).paused=false;}if(b!=null&&RemainingState.known(ghast)&&(b.ghastWait(ghast)||!ZombieBehavior.valid(ghast,ghast.getTarget()))){chargeTime=0;ghast.setCharging(false);ghast.setAttached(RemainingState.CHARGE,0);ci.cancel();}}
    @ModifyConstant(method="tick()V",constant=@Constant(intValue=20))
    private int buildup$windup(int original){return RemainingBehavior.instance()!=null&&RemainingBehavior.instance().enabled(ghast,FeatureId.GHAST_COOLDOWN)?40:original;}
    @Redirect(method="tick()V",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean buildup$fireball(Level level,Entity e){if(e instanceof LargeFireball b&&RemainingBehavior.instance()!=null)RemainingBehavior.instance().fireball(ghast,b);return level.addFreshEntity(e);}
    @Inject(method="tick()V",at=@At("TAIL"))
    private void buildup$sync(CallbackInfo ci){if(RemainingBehavior.instance()!=null)ghast.setAttached(RemainingState.CHARGE,RemainingBehavior.instance().enabled(ghast,FeatureId.GHAST_TELEGRAPH)?Math.max(0,chargeTime):0);}
    @Inject(method="stop()V",at=@At("TAIL"))
    private void buildup$stop(CallbackInfo ci){chargeTime=0;ghast.setAttached(RemainingState.CHARGE,0);}
}
