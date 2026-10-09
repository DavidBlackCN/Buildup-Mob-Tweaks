/* Mob AI Tweaks adaptation: Copyright (c) 2024 N0t_UN_Owen, MIT. See NOTICE.md. */
package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.remaining.*;
import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(targets="net.minecraft.world.entity.monster.Blaze$BlazeAttackGoal")
public abstract class BlazeRebuildGoalMixin {
    @Shadow @Final private Blaze blaze;@Shadow private int attackStep;
    @ModifyConstant(method="tick()V",constant=@Constant(intValue=4))
    private int buildup$count(int original){return RemainingBehavior.instance()==null?original:RemainingBehavior.instance().volley(blaze)+1;}
    @Inject(method="tick()V",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/monster/Blaze;setCharged(Z)V",ordinal=0))
    private void buildup$start(CallbackInfo ci){if(RemainingBehavior.instance()!=null)RemainingBehavior.instance().blazeStep(blaze,attackStep);}
    @Redirect(method="tick()V",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean buildup$bind(Level l,Entity e){if(RemainingBehavior.instance()!=null)RemainingBehavior.instance().bind(blaze,e);return !e.isRemoved()&&l.addFreshEntity(e);}
    @Inject(method="tick()V",at=@At("TAIL"))
    private void buildup$strafe(CallbackInfo ci){if(RemainingBehavior.instance()!=null)RemainingBehavior.instance().strafe(blaze,attackStep);}
    @Inject(method="stop()V",at=@At("TAIL"))
    private void buildup$stop(CallbackInfo ci){var b=RemainingBehavior.instance();if(b!=null){if(b.enabled(blaze,FeatureId.BLAZE_ORBIT)||b.enabled(blaze,FeatureId.BLAZE_STRAFE)||b.enabled(blaze,FeatureId.BLAZE_DIFFICULTY_VOLLEY))attackStep=0;b.clearBalls(blaze,false);}}
}
