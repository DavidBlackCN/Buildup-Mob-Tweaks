package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.villager.profession.ProfessionModule;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.villager.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Villager.class)
public abstract class ProfessionBoundaryMixin {
    @Inject(method="tick()V",at=@At("HEAD"))
    private void buildup$workBoundary(CallbackInfo ci){var v=(Villager)(Object)this;if(v.level() instanceof ServerLevel l)ProfessionModule.boundary(v,l);}
}
