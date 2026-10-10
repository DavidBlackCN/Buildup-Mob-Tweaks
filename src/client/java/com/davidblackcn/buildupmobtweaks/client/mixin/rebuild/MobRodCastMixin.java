package com.davidblackcn.buildupmobtweaks.client.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.profession.*;
import net.minecraft.client.renderer.item.properties.conditional.FishingRodCast;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(FishingRodCast.class)
public abstract class MobRodCastMixin {
    @Inject(method="get(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/world/entity/LivingEntity;ILnet/minecraft/world/item/ItemDisplayContext;)Z",at=@At("RETURN"),cancellable=true)
    private void buildup$cast(ItemStack stack,ClientLevel level,LivingEntity owner,int seed,ItemDisplayContext context,CallbackInfoReturnable<Boolean> ci){
        if(owner instanceof Mob m&&m.getAttachedOrCreate(ProfessionState.ROD_VISUAL)&&ProfessionBehavior.hook(m)!=null){
            var hand=ProfessionBehavior.hand(m,Items.FISHING_ROD);if(hand!=null&&m.getItemInHand(hand)==stack)ci.setReturnValue(true);
        }
    }
}
