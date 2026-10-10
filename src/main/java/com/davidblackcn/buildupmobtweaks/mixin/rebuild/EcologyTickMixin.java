package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.ecology.*;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Mob.class)
public abstract class EcologyTickMixin {
    @Inject(method="tick()V",at=@At("TAIL")) private void buildup$boundary(CallbackInfo ci){var m=(Mob)(Object)this;if(!m.level().isClientSide()&&EcologyBehavior.instance()!=null){EcologyBehavior.instance().boundary(m);if(m instanceof Sniffer s&&EcologyState.known(s))SnifferSearch.point(s);}}
}
