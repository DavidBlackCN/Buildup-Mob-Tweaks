package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.villager.profession.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ArmorStand.class)
public abstract class OwnedStandProtectionMixin implements OwnedStandAccess {
    @Invoker("setMarker") public abstract void buildup$displayMarker(boolean marker);
    @Inject(method={"brokenByPlayer","brokenByAnything"},at=@At("HEAD"),cancellable=true)
    private void buildup$noSecondDisplayDrop(CallbackInfo ci){if(OwnedWorkDisplays.protectedEntity((Entity)(Object)this))ci.cancel();}
}
