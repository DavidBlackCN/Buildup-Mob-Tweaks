package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.villager.profession.OwnedWorkDisplays;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ItemFrame.class)
public abstract class OwnedFrameProtectionMixin {
    @Inject(method="dropItem",at=@At("HEAD"),cancellable=true)
    private void buildup$noSecondDisplayDrop(CallbackInfo ci){if(OwnedWorkDisplays.protectedEntity((Entity)(Object)this))ci.cancel();}
}
