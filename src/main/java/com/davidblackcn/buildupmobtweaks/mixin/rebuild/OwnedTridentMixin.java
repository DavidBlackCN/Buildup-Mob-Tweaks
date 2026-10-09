package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.drowned.*;
import net.minecraft.world.entity.monster.zombie.Drowned;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ThrownTrident.class)
public abstract class OwnedTridentMixin implements TridentAccess {
    @Shadow private boolean dealtDamage;
    @Override public boolean buildup$ready(){return dealtDamage;}
    @Inject(method="tick()V",at=@At("TAIL"))
    private void buildup$tick(CallbackInfo ci){if(DrownedBehavior.instance()!=null)DrownedBehavior.instance().tickProjectile((ThrownTrident)(Object)this);}
    @Inject(method="onHitEntity(Lnet/minecraft/world/phys/EntityHitResult;)V",at=@At("TAIL"))
    private void buildup$hit(EntityHitResult hit,CallbackInfo ci){var p=(ThrownTrident)(Object)this;if(DrownedBehavior.known(p.getAttached(DrownedBehavior.OWNED))&&p.getOwner() instanceof Drowned m)DrownedBehavior.runtime(m).hits++;}
}
