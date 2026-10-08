package com.davidblackcn.buildupmobtweaks.mixin;
import com.davidblackcn.buildupmobtweaks.combat.EnvironmentCombat;
import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.illager.Evoker;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(SmallFireball.class)
public abstract class EvokerFireballMixin {
    private boolean buildup$protected() {
        var self=(SmallFireball)(Object)this;var tag=self.getAttached(com.davidblackcn.buildupmobtweaks.combat.SkeletonExtras.PROJECTILE);
        if(tag!=null && tag.getIntOr("version",-1)==1 && tag.getStringOr("role","").equals("evoker_fireball") && com.davidblackcn.buildupmobtweaks.combat.HostileCombat.instance()!=null)
            return com.davidblackcn.buildupmobtweaks.combat.HostileCombat.instance().gate(FeatureId.EVOKER_FIREBALL_NO_FIRE);
        return ((SmallFireball)(Object)this).getOwner() instanceof Evoker owner && EnvironmentCombat.on(owner,FeatureId.EVOKER_FIREBALL_NO_FIRE); }
    @Inject(method="onHitBlock(Lnet/minecraft/world/phys/BlockHitResult;)V",at=@At("HEAD"),cancellable=true)
    private void buildup$blocks(BlockHitResult hit,CallbackInfo ci) { if(buildup$protected()) ci.cancel(); }
    @Redirect(method="onHitEntity(Lnet/minecraft/world/phys/EntityHitResult;)V",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/Entity;igniteForSeconds(F)V"))
    private void buildup$entities(Entity target,float seconds) { if(!buildup$protected()) target.igniteForSeconds(seconds); }
}