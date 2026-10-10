package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.illusion.*;
import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.illager.Vindicator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Mob.class)
public abstract class VindicatorSupportExitMixin {
    @Inject(method="tick()V",at=@At("TAIL"))
    private void buildup$exit(CallbackInfo ci){if((Object)this instanceof Vindicator m&&!m.level().isClientSide()&&IllusionState.known(m)&&IllusionBehavior.instance()!=null&&(m.isNoAi()||!m.isAlive()||!IllusionBehavior.instance().enabled(m,FeatureId.VINDICATOR_SUPPORT)))IllusionBehavior.instance().cleanup(m);}
}
