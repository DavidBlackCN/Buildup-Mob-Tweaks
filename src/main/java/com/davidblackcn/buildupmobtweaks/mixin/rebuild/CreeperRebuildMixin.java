/* Mob AI Tweaks adaptation: Copyright (c) 2024 N0t_UN_Owen, MIT. See NOTICE.md. */
package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.remaining.*;
import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import net.minecraft.world.entity.monster.Creeper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(Creeper.class)
public abstract class CreeperRebuildMixin {
    @Inject(method="explodeCreeper()V",at=@At(value="INVOKE",target="Lnet/minecraft/server/level/ServerLevel;explode(Lnet/minecraft/world/entity/Entity;DDDFLnet/minecraft/world/level/Level$ExplosionInteraction;)V",shift=At.Shift.AFTER))
    private void buildup$arrows(CallbackInfo ci){RemainingExtras.explode((Creeper)(Object)this);}
    @ModifyVariable(method="setSwellDir(I)V",at=@At("HEAD"),argsOnly=true)
    private int buildup$delay(int dir){var m=(Creeper)(Object)this;return RemainingBehavior.instance()!=null&&RemainingBehavior.instance().enabled(m,FeatureId.CREEPER_HIT_DELAY)&&!m.isIgnited()&&m.hurtTime>0&&dir>0?-dir:dir;}
}
