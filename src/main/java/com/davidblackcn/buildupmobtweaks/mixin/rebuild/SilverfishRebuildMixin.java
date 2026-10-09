/* Mob AI Tweaks adaptation: Copyright (c) 2024 N0t_UN_Owen, MIT. See NOTICE.md. */
package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.remaining.*;
import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(Silverfish.class)
public abstract class SilverfishRebuildMixin {
    @ModifyVariable(method="hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z",at=@At("HEAD"),argsOnly=true)
    private float buildup$shovel(float amount,ServerLevel l,DamageSource s,float original){return RemainingBehavior.instance()!=null&&RemainingBehavior.instance().enabled((Silverfish)(Object)this,FeatureId.SILVERFISH_SHOVEL_WEAKNESS)
        &&s.getEntity()==s.getDirectEntity()&&s.getEntity() instanceof LivingEntity a&&a.getMainHandItem().is(ItemTags.SHOVELS)?amount*2:amount;}
}
