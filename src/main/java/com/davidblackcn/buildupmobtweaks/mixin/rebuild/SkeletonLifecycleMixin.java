package com.davidblackcn.buildupmobtweaks.mixin.rebuild;

import com.davidblackcn.buildupmobtweaks.rebuild.skeleton.*;
import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import net.minecraft.network.syncher.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(AbstractSkeleton.class)
public abstract class SkeletonLifecycleMixin extends Monster implements SkeletonAccess {
    protected SkeletonLifecycleMixin(EntityType<? extends Monster> type,Level level){super(type,level);}
    @Shadow @Final private RangedBowAttackGoal<AbstractSkeleton> bowGoal;
    @Shadow @Final private MeleeAttackGoal meleeGoal;
    @Shadow protected abstract int getHardAttackInterval();
    @Shadow protected abstract int getAttackInterval();
    @Unique private static final EntityDataAccessor<Integer> BUILDUP_SPECIAL=SynchedEntityData.defineId(AbstractSkeleton.class,EntityDataSerializers.INT);
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder){super.defineSynchedData(builder);builder.define(BUILDUP_SPECIAL,0);}
    @Override public int buildup$special(){return ((AbstractSkeleton)(Object)this).getEntityData().get(BUILDUP_SPECIAL);}
    @Override public void buildup$special(int ticks){((AbstractSkeleton)(Object)this).getEntityData().set(BUILDUP_SPECIAL,ticks);}
    @Override public int buildup$attackInterval(boolean hard){return hard?getHardAttackInterval():getAttackInterval();}
    @Override public void buildup$removeOriginalAttacks(){var m=(AbstractSkeleton)(Object)this;m.getGoalSelector().removeGoal(bowGoal);m.getGoalSelector().removeGoal(meleeGoal);}
    @Inject(method="reassessWeaponGoal()V",at=@At("HEAD"),cancellable=true)
    private void buildup$managedWeapons(CallbackInfo ci){var m=(AbstractSkeleton)(Object)this;if(SkeletonBehavior.supported(m)&&SkeletonState.runtime(m).managed)ci.cancel();}
    @Inject(method="canUseNonMeleeWeapon(Lnet/minecraft/world/item/ItemStack;)Z",at=@At("HEAD"),cancellable=true)
    private void buildup$bow(ItemStack item,CallbackInfoReturnable<Boolean> ci){var m=(AbstractSkeleton)(Object)this;var b=SkeletonBehavior.instance();
        if(b!=null&&b.enabled(m,FeatureId.SKELETON_BOW_COMPATIBILITY)&&b.bow(m,item))ci.setReturnValue(true);}
    @Inject(method="getArrow(Lnet/minecraft/world/item/ItemStack;FLnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/entity/projectile/arrow/AbstractArrow;",at=@At("RETURN"))
    private void buildup$critical(ItemStack ammo,float power,ItemStack bow,CallbackInfoReturnable<AbstractArrow> ci){var m=(AbstractSkeleton)(Object)this;
        if(SkeletonBehavior.instance()!=null&&SkeletonBehavior.instance().sniper(m)&&!m.isBaby())ci.getReturnValue().setCritArrow(true);}
    @Inject(method="performRangedAttack(Lnet/minecraft/world/entity/LivingEntity;F)V",at=@At("RETURN"))
    private void buildup$shot(LivingEntity target,float power,CallbackInfo ci){var m=(AbstractSkeleton)(Object)this;var b=SkeletonBehavior.instance();
        if(b!=null&&SkeletonBehavior.supported(m)&&SkeletonState.runtime(m).managed){var hand=b.hand(m);m.getItemInHand(hand).hurtAndBreak(1,m,hand==net.minecraft.world.InteractionHand.MAIN_HAND?EquipmentSlot.MAINHAND:EquipmentSlot.OFFHAND);SkeletonState.runtime(m).shots++;}}
}
