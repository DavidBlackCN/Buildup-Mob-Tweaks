package com.davidblackcn.buildupmobtweaks.mixin.rebuild;

import com.davidblackcn.buildupmobtweaks.villager.farmer.*;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Defer invalidation until the next activity tick, including reentrant block updates during harvest. */
@Mixin(PathNavigation.class)
public abstract class FarmerRepathMixin {
    @Shadow @Final protected Mob mob;
    @Inject(method="recomputePath()V",at=@At("HEAD"),cancellable=true)
    private void buildup$revalidateFarmLayout(CallbackInfo ci){if(mob instanceof Villager v&&FarmNavigation.handlesRecompute(v))ci.cancel();}
}
