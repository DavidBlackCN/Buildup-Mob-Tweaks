package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.ecology.EcologyBehavior;
import net.minecraft.world.item.InstrumentItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(InstrumentItem.class)
public abstract class EcologyHornMixin {
    @Inject(method="use(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;",at=@At("RETURN"))
    private void buildup$horn(Level level,Player player,InteractionHand hand,CallbackInfoReturnable<InteractionResult> cir){if(!level.isClientSide()&&EcologyBehavior.instance()!=null)EcologyBehavior.instance().horn(player);}
}
