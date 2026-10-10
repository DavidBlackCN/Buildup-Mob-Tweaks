package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.profession.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(FishingHook.class)
public abstract class MobFishingHookMixin implements FishingAccess {
    @Invoker("checkCollision") public abstract void buildup$collision();
    @Invoker("pullEntity") public abstract void buildup$pull(Entity target);
    @Inject(method="tick()V",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/projectile/Projectile;tick()V",shift=At.Shift.AFTER),cancellable=true)
    private void buildup$mobTick(CallbackInfo ci){var h=(FishingHook)(Object)this;if(ProfessionBehavior.mobHook(h)){
        if(ProfessionBehavior.instance()!=null)ProfessionBehavior.instance().tickHook(h);ci.cancel();}
        else if(h.level().isClientSide()&&ProfessionBehavior.mobOwner(h)&&h.tickCount<=3)ci.cancel();}
    @Inject(method="recreateFromPacket(Lnet/minecraft/network/protocol/game/ClientboundAddEntityPacket;)V",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/projectile/Projectile;recreateFromPacket(Lnet/minecraft/network/protocol/game/ClientboundAddEntityPacket;)V",shift=At.Shift.AFTER),cancellable=true)
    private void buildup$mobOwner(ClientboundAddEntityPacket packet,CallbackInfo ci){if(ProfessionBehavior.mobOwner((FishingHook)(Object)this))ci.cancel();}
    @Inject(method="setOwner(Lnet/minecraft/world/entity/Entity;)V",at=@At("TAIL"))
    private void buildup$remember(Entity owner,CallbackInfo ci){ProfessionBehavior.ownerChanged((FishingHook)(Object)this);}
    @Inject(method="canHitEntity(Lnet/minecraft/world/entity/Entity;)Z",at=@At("RETURN"),cancellable=true)
    private void buildup$target(Entity target,CallbackInfoReturnable<Boolean> ci){var h=(FishingHook)(Object)this;
        if(ProfessionBehavior.mobHook(h)&&!h.level().isClientSide()&&h.getOwner() instanceof Mob m)
            ci.setReturnValue(ci.getReturnValueZ()&&target instanceof LivingEntity living&&target==ProfessionState.runtime(m).target&&com.davidblackcn.buildupmobtweaks.rebuild.zombie.ZombieBehavior.valid(m,living));
    }
}
