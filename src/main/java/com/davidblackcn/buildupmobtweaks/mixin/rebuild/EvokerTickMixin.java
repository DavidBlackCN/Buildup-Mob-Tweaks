package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.evoker.EvokerBehavior;
import com.davidblackcn.buildupmobtweaks.rebuild.raid.RaidState;
import net.minecraft.world.entity.monster.illager.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(SpellcasterIllager.class)
public abstract class EvokerTickMixin {
    @Inject(method="getArmPose()Lnet/minecraft/world/entity/monster/illager/AbstractIllager$IllagerArmPose;",at=@At("HEAD"),cancellable=true)
    private void buildup$pose(CallbackInfoReturnable<AbstractIllager.IllagerArmPose> ci){if((Object)this instanceof Evoker m&&Boolean.TRUE.equals(m.getAttached(RaidState.ACTIVE)))ci.setReturnValue(AbstractIllager.IllagerArmPose.BOW_AND_ARROW);}
}
