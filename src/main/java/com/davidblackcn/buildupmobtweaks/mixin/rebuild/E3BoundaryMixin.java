package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.relations.RelationBehavior;
import com.davidblackcn.buildupmobtweaks.rebuild.riding.RidingBehavior;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Mob.class)
public abstract class E3BoundaryMixin {
    @Inject(method="tick()V",at=@At("TAIL"))
    private void buildup$e3Boundary(CallbackInfo ci){var m=(Mob)(Object)this;if(!m.level().isClientSide()){if(RelationBehavior.instance()!=null)RelationBehavior.instance().boundary(m);if(RidingBehavior.instance()!=null)RidingBehavior.instance().boundary(m);}}
}
