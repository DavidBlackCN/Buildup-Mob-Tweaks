package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.ecology.EcologyBehavior;
import net.minecraft.world.entity.animal.squid.Squid;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(Squid.class)
public abstract class EcologySquidMixin {
    @Unique private DamageSource buildup$inkSource;
    @Inject(method="hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/animal/squid/Squid;spawnInk()V")) private void buildup$source(ServerLevel l,DamageSource source,float damage,CallbackInfoReturnable<Boolean> cir){buildup$inkSource=source;}
    @Inject(method="spawnInk()V",at=@At("TAIL")) private void buildup$ink(CallbackInfo ci){if(EcologyBehavior.instance()!=null)EcologyBehavior.instance().ink((Squid)(Object)this,buildup$inkSource);buildup$inkSource=null;}
    @Inject(method="aiStep()V",at=@At("TAIL")) private void buildup$gaze(CallbackInfo ci){if(EcologyBehavior.instance()!=null)EcologyBehavior.instance().gaze((Squid)(Object)this);}
}
