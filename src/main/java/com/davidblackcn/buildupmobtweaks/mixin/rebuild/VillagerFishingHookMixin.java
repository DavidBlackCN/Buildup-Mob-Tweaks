package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.villager.profession.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(FishingHook.class)
public abstract class VillagerFishingHookMixin implements NpcFishingAccess {
    @Invoker("catchingFish") public abstract void buildup$naturalBite(BlockPos pos);
    @Accessor("nibble") public abstract int buildup$nibble();
    @Invoker("checkCollision") public abstract void buildup$workCollision();
    @Inject(method="tick()V",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/projectile/Projectile;tick()V",shift=At.Shift.AFTER),cancellable=true)
    private void buildup$npcFishing(CallbackInfo ci){var h=(FishingHook)(Object)this;if(NpcFishingHooks.marked(h)){NpcFishingHooks.tick(h);ci.cancel();}else if(h.level().isClientSide()&&h.getOwner() instanceof Villager&&h.tickCount<=3)ci.cancel();}
    @Inject(method="recreateFromPacket",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/projectile/Projectile;recreateFromPacket(Lnet/minecraft/network/protocol/game/ClientboundAddEntityPacket;)V",shift=At.Shift.AFTER),cancellable=true)
    private void buildup$npcOwner(ClientboundAddEntityPacket packet,CallbackInfo ci){if(((FishingHook)(Object)this).getOwner() instanceof Villager)ci.cancel();}
    @Inject(method="canHitEntity",at=@At("RETURN"),cancellable=true)
    private void buildup$noWorkHookAttack(Entity target,CallbackInfoReturnable<Boolean> ci){if(NpcFishingHooks.marked((FishingHook)(Object)this))ci.setReturnValue(false);}
}
