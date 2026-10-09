package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.vex.VexBehavior;
import net.minecraft.world.entity.monster.Vex;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(targets="net.minecraft.world.entity.monster.Vex$VexMoveControl")
public abstract class VexMoveMixin {
    @Shadow @Final private Vex this$0;
    @Inject(method="tick()V",at=@At("HEAD"),cancellable=true)
    private void buildup$fixed(CallbackInfo ci){if(VexBehavior.instance()!=null&&VexBehavior.instance().move(this$0))ci.cancel();}
}
