package com.davidblackcn.buildupmobtweaks.client.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.client.rebuild.NpcFishingClientHooks;
import net.minecraft.client.renderer.item.properties.conditional.FishingRodCast;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(FishingRodCast.class)
public abstract class VillagerRodCastMixin {
    @Inject(method="get(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/world/entity/LivingEntity;ILnet/minecraft/world/item/ItemDisplayContext;)Z",at=@At("RETURN"),cancellable=true)
    private void buildup$actualNpcCast(ItemStack stack,ClientLevel level,LivingEntity owner,int seed,ItemDisplayContext context,CallbackInfoReturnable<Boolean> ci){if(owner instanceof Villager v&&v.getMainHandItem()==stack&&NpcFishingClientHooks.cast(v))ci.setReturnValue(true);}
}
