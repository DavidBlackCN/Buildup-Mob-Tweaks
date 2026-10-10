package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.ecology.SnifferSearch;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(Sniffer.class)
public abstract class EcologySnifferMixin {
    @Inject(method="mobInteract(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;",at=@At("HEAD"),cancellable=true)
    private void buildup$search(Player player,InteractionHand hand,CallbackInfoReturnable<InteractionResult> cir){if(SnifferSearch.request((Sniffer)(Object)this,player,hand))cir.setReturnValue(InteractionResult.SUCCESS);}
}
