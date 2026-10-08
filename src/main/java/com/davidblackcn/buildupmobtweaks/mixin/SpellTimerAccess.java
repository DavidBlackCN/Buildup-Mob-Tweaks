package com.davidblackcn.buildupmobtweaks.mixin;
import net.minecraft.world.entity.monster.illager.SpellcasterIllager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(SpellcasterIllager.class)
public interface SpellTimerAccess {
    @Accessor("spellCastingTickCount") void buildup$setSpellTimer(int ticks);
}