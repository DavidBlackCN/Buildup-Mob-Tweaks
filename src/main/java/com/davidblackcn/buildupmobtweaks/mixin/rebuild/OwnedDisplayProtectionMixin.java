package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.villager.profession.OwnedWorkDisplays;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin({ArmorStand.class,ItemFrame.class})
public abstract class OwnedDisplayProtectionMixin {
    @Inject(method="hurtServer",at=@At("HEAD"),cancellable=true)
    private void buildup$protectDisplay(ServerLevel l,DamageSource source,float amount,CallbackInfoReturnable<Boolean> ci){if(OwnedWorkDisplays.protectedEntity((Entity)(Object)this))ci.setReturnValue(false);}
    @Inject(method="interact",at=@At("HEAD"),cancellable=true)
    private void buildup$protectEquipment(Player player,InteractionHand hand,Vec3 hit,CallbackInfoReturnable<InteractionResult> ci){if(OwnedWorkDisplays.protectedEntity((Entity)(Object)this))ci.setReturnValue(InteractionResult.FAIL);}
}
