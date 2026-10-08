package com.davidblackcn.buildupmobtweaks.mixin;

import com.davidblackcn.buildupmobtweaks.combat.*;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.arrow.*;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThrownTrident.class)
public abstract class TridentMixin extends AbstractArrow implements OwnedTrident {
    @Shadow @Final private static EntityDataAccessor<Byte> ID_LOYALTY;
    @Shadow private boolean dealtDamage;
    protected TridentMixin(EntityType<? extends AbstractArrow> type, Level level) { super(type, level); }
    @Override public boolean buildup$readyForRecovery() {
        return tickCount >= 10 && (isInGround() || dealtDamage || isInWater() && getDeltaMovement().lengthSqr() < .04);
    }
    @Inject(method = "tick()V", at = @At("HEAD"), cancellable = true)
    private void buildup$ownership(CallbackInfo ci) {
        var self = (ThrownTrident) (Object) this;
        if (!level().isClientSide() && hasAttached(DrownedTridents.OWNED)) {
            // Vanilla loyalty discards tridents reaching non-player owners instead of equipping them.
            // Keep the original item's enchantments; only the owned projectile's return AI is replaced.
            entityData.set(ID_LOYALTY, (byte) 0);
            if (DrownedTridents.instance() != null) DrownedTridents.instance().projectileTick(self);
            if (isRemoved()) ci.cancel();
        }
    }
}