package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.skeleton.*;
import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Snowball.class)
public abstract class SkeletonSnowballMixin {
    @Inject(method="onHitEntity(Lnet/minecraft/world/phys/EntityHitResult;)V",at=@At("HEAD"),cancellable=true)
    private void buildup$damage(EntityHitResult hit,CallbackInfo ci){var ball=(Snowball)(Object)this;
        if(Boolean.TRUE.equals(ball.getAttached(SkeletonSpecificGoals.SNOW))&&ball.level() instanceof ServerLevel level
                &&ball.getOwner() instanceof AbstractSkeleton mob&&SkeletonBehavior.instance()!=null
                &&SkeletonBehavior.instance().enabled(mob,FeatureId.STRAY_JUMP_SHOT)){
            if(hit.getEntity().hurtServer(level,ball.damageSources().thrown(ball,mob),level.getDifficulty().getId()+1))SkeletonState.runtime(mob).snowHits++;
            ci.cancel();
        }
    }
}
