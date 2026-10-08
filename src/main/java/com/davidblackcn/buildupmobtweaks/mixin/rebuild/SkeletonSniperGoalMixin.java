package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.skeleton.*;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(RangedAttackGoal.class)
public abstract class SkeletonSniperGoalMixin {
    @Shadow @Final private Mob mob;
    @Shadow private int attackTime;
    @Inject(method="tick()V",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/monster/RangedAttackMob;performRangedAttack(Lnet/minecraft/world/entity/LivingEntity;F)V"),cancellable=true)
    private void buildup$realDraw(CallbackInfo ci){
        if(mob instanceof AbstractSkeleton m&&SkeletonBehavior.supported(m)&&SkeletonState.runtime(m).managed&&SkeletonState.runtime(m).mode.equals("sniper")){
            var b=SkeletonBehavior.instance();if(!m.isUsingItem())m.startUsingItem(b.hand(m));
            if(m.getTicksUsingItem()<20){attackTime++;ci.cancel();}else m.stopUsingItem();
        }
    }
}
