package com.davidblackcn.buildupmobtweaks.mixin;
import net.minecraft.world.entity.monster.Creeper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(Creeper.class)
public interface CreeperFuseAccess {
    @Accessor("swell") int buildup$getSwell();
    @Accessor("swell") void buildup$setSwell(int value);
}