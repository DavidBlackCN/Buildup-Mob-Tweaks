/* Mob AI Tweaks adaptation: Copyright (c) 2024 N0t_UN_Owen, MIT. See NOTICE.md. */
package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.general.E4Behavior;
import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.Difficulty;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(MeleeAttackGoal.class)
public abstract class E4MeleeMixin {
    @Shadow @Final protected PathfinderMob mob;
    @Shadow private int ticksUntilNextAttack;
    @Unique private long buildup$attackReady;
    @Unique private int buildup$interval(int original){
        if(!E4Behavior.enabled(mob,FeatureId.MELEE_EFFECT_INTERVAL))return original;
        var slow=mob.getEffect(MobEffects.MINING_FATIGUE);var fast=mob.getEffect(MobEffects.HASTE);
        int ticks=original+(slow==null?0:2*(Math.clamp(slow.getAmplifier(),0,255)+1))-(fast==null?0:2*(Math.clamp(fast.getAmplifier(),0,255)+1));
        return Math.clamp(ticks,6,200);
    }
    @Inject(method="resetAttackCooldown()V",at=@At("TAIL")) private void buildup$reset(CallbackInfo ci){
        ticksUntilNextAttack=buildup$interval(ticksUntilNextAttack);
        buildup$attackReady=E4Behavior.enabled(mob,FeatureId.MELEE_EFFECT_INTERVAL)?mob.level().getGameTime()+ticksUntilNextAttack:0;
    }
    // Native start resets its counter to zero. A path/target restart must not
    // bypass the still-active effect interval; expiry and disabled use stay native.
    @Inject(method="start()V",at=@At("TAIL")) private void buildup$restart(CallbackInfo ci){
        if(!E4Behavior.enabled(mob,FeatureId.MELEE_EFFECT_INTERVAL)){buildup$attackReady=0;return;}
        long remaining=buildup$attackReady-mob.level().getGameTime();
        if(remaining>0)ticksUntilNextAttack=Math.max(ticksUntilNextAttack,(int)Math.min(200,remaining+1));
    }
    @ModifyReturnValue(method="getAttackInterval()I",at=@At("RETURN")) private int buildup$get(int original){return buildup$interval(original);}
    @Unique private long buildup$jumpReady;
    @ModifyReturnValue(method="canUse()Z",at=@At("RETURN")) private boolean buildup$unreachable(boolean original){if(!original)buildup$tryJump();return original;}
    @Inject(method="tick()V",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/ai/goal/MeleeAttackGoal;checkAndPerformAttack(Lnet/minecraft/world/entity/LivingEntity;)V")) private void buildup$jump(CallbackInfo ci){buildup$tryJump();}
    @Unique private void buildup$tryJump(){
        var t=mob.getTarget();var box=mob.getBoundingBox();
        if(ticksUntilNextAttack==0&&mob.level().getGameTime()>=buildup$jumpReady&&com.davidblackcn.buildupmobtweaks.rebuild.zombie.ZombieBehavior.valid(mob,t)&&E4Behavior.enabled(mob,FeatureId.MELEE_HIGH_TARGET_JUMP)&&mob.level().getDifficulty()==Difficulty.HARD
            &&mob.onGround()&&!mob.isInWater()&&!mob.isPassenger()&&box.getXsize()<.8&&box.getZsize()<.8&&box.getYsize()>1&&mob.hasLineOfSight(t)){
            double dy=t.getY()-mob.getEyeY(),dx=t.getX()-mob.getX(),dz=t.getZ()-mob.getZ();
            if(dx*dx+dz*dz<4&&dy>.2&&dy<.9*mob.getScale()*mob.getAgeScale()+mob.getJumpBoostPower()&&mob.level().noCollision(mob,box.move(0,.6,0))){mob.getJumpControl().jump();buildup$jumpReady=mob.level().getGameTime()+20;}
        }
    }
}
