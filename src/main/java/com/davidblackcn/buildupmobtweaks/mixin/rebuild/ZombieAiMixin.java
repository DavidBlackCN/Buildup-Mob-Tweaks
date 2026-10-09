package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.zombie.*;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.zombie.Zombie;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Mob.class)
public abstract class ZombieAiMixin {
    @Inject(method="serverAiStep()V",at=@At("HEAD"))
    private void buildup$ai(CallbackInfo ci){
        if((Object)this instanceof Zombie m&&ZombieBehavior.instance()!=null)ZombieBehavior.instance().beforeAi(m);
        if((Object)this instanceof net.minecraft.world.entity.monster.zombie.Drowned m&&com.davidblackcn.buildupmobtweaks.rebuild.drowned.DrownedBehavior.instance()!=null)
            com.davidblackcn.buildupmobtweaks.rebuild.drowned.DrownedBehavior.instance().beforeAi(m);
    }
}
