package com.davidblackcn.buildupmobtweaks.mixin.rebuild;

import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import com.davidblackcn.buildupmobtweaks.rebuild.relations.RelationBehavior;
import com.davidblackcn.buildupmobtweaks.rebuild.riding.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Buildup safety rule, not an upstream restoration or a general damage immunity. */
@Mixin(Projectile.class)
public abstract class RiderProjectileMixin {
    @Inject(method="canHitEntity(Lnet/minecraft/world/entity/Entity;)Z",at=@At("HEAD"),cancellable=true)
    private void buildup$currentRidingTree(Entity candidate,CallbackInfoReturnable<Boolean> ci){
        if(!((Object)this instanceof AbstractArrow arrow)||!(arrow.level() instanceof ServerLevel))return;
        if(!(arrow.getOwner() instanceof Mob owner)||!owner.isAlive()||owner.isRemoved()||owner.isNoAi()
                ||owner.level()!=candidate.level()||(!owner.isPassenger()&&!owner.isVehicle()))return;
        var behavior=RidingBehavior.instance();
        if(behavior==null||!behavior.gate(owner,FeatureId.RIDER_FRIENDLY_FIRE)||RelationBehavior.unknownState(owner)
                ||owner.hasAttached(RidingState.DATA)&&!RidingState.known(owner))return;
        if(owner.isPassengerOfSameVehicle(candidate)){
            RidingState.runtime(owner).projectileSkips++;
            ci.setReturnValue(false);
        }
    }
}
