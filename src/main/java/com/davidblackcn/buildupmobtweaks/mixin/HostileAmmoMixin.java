package com.davidblackcn.buildupmobtweaks.mixin;
import com.davidblackcn.buildupmobtweaks.combat.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(LivingEntity.class)
public abstract class HostileAmmoMixin {
    @Inject(method="getProjectile(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;",at=@At("RETURN"),cancellable=true)
    private void buildup$ammunition(ItemStack weapon,CallbackInfoReturnable<ItemStack> cir){
        if((Object)this instanceof Mob mob&&HostileEquipment.crossbowEnabled(mob)&&weapon.getItem() instanceof CrossbowItem&&cir.getReturnValue().isEmpty())cir.setReturnValue(new ItemStack(Items.ARROW));
    }
    @Inject(method="isHolding(Lnet/minecraft/world/item/Item;)Z",at=@At("RETURN"),cancellable=true)
    private void buildup$crossbow(Item item,CallbackInfoReturnable<Boolean> cir){
        if(item==Items.CROSSBOW && (Object)this instanceof net.minecraft.world.entity.monster.piglin.Piglin mob && HostileCombat.instance()!=null && HostileCombat.instance().crossbow(mob))cir.setReturnValue(true);
    }
}