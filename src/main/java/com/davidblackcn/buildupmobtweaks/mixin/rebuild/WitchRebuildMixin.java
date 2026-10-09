/* Mob AI Tweaks adaptation: Copyright (c) 2024 N0t_UN_Owen, MIT. See NOTICE.md. */
package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.witch.*;
import com.davidblackcn.buildupmobtweaks.rebuild.raid.*;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.resources.Identifier;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(Witch.class)
public abstract class WitchRebuildMixin implements WitchAccess {
    @Shadow private int usingTime;
    @Shadow @Final private static Identifier SPEED_MODIFIER_DRINKING_ID;
    public void buildup$cancelDrink(){var m=(Witch)(Object)this;usingTime=0;m.setUsingItem(false);m.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(SPEED_MODIFIER_DRINKING_ID);}
    @Inject(method="performRangedAttack(Lnet/minecraft/world/entity/LivingEntity;F)V",at=@At("HEAD"),cancellable=true)
    private void buildup$cooldown(LivingEntity t,float p,CallbackInfo ci){var b=WitchBehavior.instance();if(b!=null&&b.blocked((Witch)(Object)this,t))ci.cancel();}
    @Inject(method="performRangedAttack(Lnet/minecraft/world/entity/LivingEntity;F)V",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/projectile/Projectile;spawnProjectileUsingShoot(Lnet/minecraft/world/entity/projectile/Projectile$ProjectileFactory;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;DDDFF)Lnet/minecraft/world/entity/projectile/Projectile;"),cancellable=true)
    private void buildup$preview(LivingEntity t,float p,CallbackInfo ci,@Local ItemStack stack){var b=WitchBehavior.instance();if(b==null)return;var m=(Witch)(Object)this;if(b.defer(m,t,stack))ci.cancel();else{b.jump(m,t);b.thrown(m);}}
    @Inject(method="aiStep()V",at=@At(value="INVOKE",target="Lnet/minecraft/util/RandomSource;nextFloat()F",ordinal=0))
    private void buildup$leapChoice(CallbackInfo ci,@Local LocalRef<Holder<Potion>> potion){var m=(Witch)(Object)this;var b=WitchBehavior.instance();if(b!=null&&b.leap(m)&&m.getRandom().nextFloat()<.05f)potion.set(Potions.LEAPING);}
    @ModifyExpressionValue(method="aiStep()V",at=@At(value="INVOKE",target="Lnet/minecraft/world/item/alchemy/PotionContents;createItemStack(Lnet/minecraft/world/item/Item;Lnet/minecraft/core/Holder;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack buildup$ownedLeap(ItemStack stack){var contents=stack.get(net.minecraft.core.component.DataComponents.POTION_CONTENTS);if(contents!=null&&contents.is(Potions.LEAPING)){RaidState.runtime((Witch)(Object)this).leaps++;TemporaryItem.mark(stack,"leap");}return stack;}
    @Inject(method="aiStep()V",at=@At("HEAD"))
    private void buildup$lifecycle(CallbackInfo ci){var m=(Witch)(Object)this;if(!m.level().isClientSide()&&WitchBehavior.instance()!=null)WitchBehavior.instance().beforeAi(m);}
}
