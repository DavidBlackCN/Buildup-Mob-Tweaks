package com.davidblackcn.buildupmobtweaks.mixin;
import com.davidblackcn.buildupmobtweaks.combat.*;
import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(targets="net.minecraft.world.entity.animal.equine.SkeletonTrapGoal")
public abstract class SkeletonTrapMixin {
    @Unique private int buildup$index;
    @Inject(method="tick()V",at=@At("HEAD")) private void buildup$reset(CallbackInfo ci){buildup$index=0;}
    @WrapOperation(method="tick()V",at=@At(value="INVOKE",target="Lnet/minecraft/server/level/ServerLevel;addFreshEntityWithPassengers(Lnet/minecraft/world/entity/Entity;)V"))
    private void buildup$variants(ServerLevel level,Entity entity,Operation<Void> original){
        original.call(level,entity);
        Skeleton skeleton=entity instanceof Skeleton s?s:entity.getPassengers().stream().filter(Skeleton.class::isInstance).map(Skeleton.class::cast).findFirst().orElse(null);
        int index=buildup$index++;
        if(skeleton==null || !EnvironmentCombat.on(skeleton,FeatureId.SKELETON_TRAP_VARIANTS))return;
        EntityType<? extends Mob> type=switch(index){case 1->EntityTypes.STRAY;case 2->EntityTypes.BOGGED;case 3->EntityTypes.WITHER_SKELETON;default->null;};
        if(type!=null)skeleton.convertTo(type,ConversionParams.single(skeleton,true,true),EntitySpawnReason.CONVERSION,result->{});
    }
}