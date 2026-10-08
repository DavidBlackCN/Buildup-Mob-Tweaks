package com.davidblackcn.buildupmobtweaks.mixin;
import com.davidblackcn.buildupmobtweaks.combat.VexOwnership;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Vex;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Vex.class)
public abstract class VexOwnerMixin {
    @Inject(method="setOwner(Lnet/minecraft/world/entity/Mob;)V", at=@At("HEAD"))
    private void buildup$release(Mob owner, CallbackInfo ci) { VexOwnership.release((Vex)(Object)this); }
    @Inject(method="setOwner(Lnet/minecraft/world/entity/Mob;)V", at=@At("TAIL"))
    private void buildup$track(Mob owner, CallbackInfo ci) { VexOwnership.track((Vex)(Object)this); }
}