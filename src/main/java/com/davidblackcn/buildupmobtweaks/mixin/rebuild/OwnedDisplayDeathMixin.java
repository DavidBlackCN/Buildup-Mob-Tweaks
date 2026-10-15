package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.villager.profession.OwnedWorkDisplays;
import net.minecraft.world.entity.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(LivingEntity.class)
public abstract class OwnedDisplayDeathMixin {
    @Inject(method="dropAllDeathLoot",at=@At("HEAD"),cancellable=true)
    private void buildup$noSecondDisplayDeathDrop(CallbackInfo ci){if(OwnedWorkDisplays.protectedEntity((Entity)(Object)this))ci.cancel();}
}
