package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.illusion.*;
import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.illager.Illusioner;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(RangedBowAttackGoal.class)
public abstract class IllusionBowGoalMixin {
    @Shadow @Final private Monster mob;
    @Inject(method="tick()V",at=@At("HEAD"),cancellable=true)
    private void buildup$pause(CallbackInfo ci){if(mob instanceof Illusioner m&&IllusionBehavior.instance()!=null&&IllusionState.known(m)&&IllusionBehavior.instance().pause(m)){m.getNavigation().stop();m.getMoveControl().strafe(0,0);ci.cancel();}}
    @Inject(method="tick()V",at=@At("TAIL"))
    private void buildup$draw(CallbackInfo ci){if(mob instanceof Illusioner m&&IllusionBehavior.instance()!=null&&IllusionBehavior.instance().enabled(m,FeatureId.ILLUSIONER_SHOOT_PAUSE)&&m.isUsingItem()){m.getNavigation().stop();m.getMoveControl().strafe(0,0);}}
}
