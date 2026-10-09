package com.davidblackcn.buildupmobtweaks.mixin.rebuild;

import com.davidblackcn.buildupmobtweaks.rebuild.zombie.ZombieBehavior;
import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Vanilla 26.3 wears blocking items only for Player; charge the existing zombie shield on actual blocks. */
@Mixin(BlocksAttacks.class)
public abstract class ZombieShieldWearMixin {
    @Inject(method="hurtBlockingItem(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;F)V",at=@At("HEAD"))
    private void buildup$wear(Level level,ItemStack stack,LivingEntity user,InteractionHand hand,float blocked,CallbackInfo ci){
        if(level.isClientSide()||!(user instanceof Zombie m)||!stack.is(Items.SHIELD)||ZombieBehavior.instance()==null
                ||!ZombieBehavior.instance().enabled(m,FeatureId.ZOMBIE_SHIELD_USE))return;
        int amount=((BlocksAttacks)(Object)this).itemDamage().apply(blocked);
        if(amount>0)stack.hurtAndBreak(amount,user,hand.asEquipmentSlot());
    }
}
