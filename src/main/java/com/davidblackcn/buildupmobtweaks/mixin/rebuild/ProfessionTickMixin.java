package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.profession.ProfessionBehavior;
import net.minecraft.world.entity.monster.zombie.Zombie;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Zombie.class)
public abstract class ProfessionTickMixin {
    @Inject(method="tick()V",at=@At("TAIL"))
    private void buildup$bounded(CallbackInfo ci){var m=(Zombie)(Object)this;
        if(!m.level().isClientSide()&&ProfessionBehavior.instance()!=null)ProfessionBehavior.instance().tick(m);
    }
}
