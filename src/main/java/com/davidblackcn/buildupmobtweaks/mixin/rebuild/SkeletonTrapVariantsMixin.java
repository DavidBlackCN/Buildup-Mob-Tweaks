package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.riding.RidingEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.equine.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(SkeletonTrapGoal.class)
public abstract class SkeletonTrapVariantsMixin {
    @Shadow @Final private SkeletonHorse horse;
    @Unique private int buildup$index;
    @Inject(method="tick()V",at=@At("HEAD")) private void buildup$begin(CallbackInfo ci){buildup$index=0;}
    @ModifyArg(method="tick()V",at=@At(value="INVOKE",target="Lnet/minecraft/server/level/ServerLevel;addFreshEntityWithPassengers(Lnet/minecraft/world/entity/Entity;)V"),index=0)
    private Entity buildup$variant(Entity root){return RidingEvents.trap(root,buildup$index++,horse);}
}
