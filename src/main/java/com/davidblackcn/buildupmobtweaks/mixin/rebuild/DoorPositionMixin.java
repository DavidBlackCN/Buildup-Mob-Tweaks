package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.zombie.ZombieBehavior;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.zombie.Zombie;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Entity.class)
public abstract class DoorPositionMixin {
    @Inject(method="positionRider(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity$MoveFunction;)V",at=@At("HEAD"),cancellable=true)
    private void buildup$door(Entity passenger,Entity.MoveFunction update,CallbackInfo ci){if((Object)this instanceof Zombie m&&ZombieBehavior.instance()!=null&&ZombieBehavior.instance().placeDisplay(m,passenger,update))ci.cancel();}
}
