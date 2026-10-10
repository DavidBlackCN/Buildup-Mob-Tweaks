package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.illusion.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.illager.Illusioner;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(Illusioner.class)
public abstract class IllusionRebuildMixin {
    @Inject(method="finalizeSpawn(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/DifficultyInstance;Lnet/minecraft/world/entity/EntitySpawnReason;Lnet/minecraft/world/entity/SpawnGroupData;)Lnet/minecraft/world/entity/SpawnGroupData;",at=@At("RETURN"))
    private void buildup$birth(ServerLevelAccessor l,DifficultyInstance d,EntitySpawnReason reason,SpawnGroupData g,CallbackInfoReturnable<SpawnGroupData> ci){if(IllusionBehavior.instance()!=null)IllusionBehavior.instance().born((Illusioner)(Object)this,reason);}
    @Inject(method="aiStep()V",at=@At("HEAD"))
    private void buildup$exit(CallbackInfo ci){var m=(Illusioner)(Object)this;if(!m.level().isClientSide()&&IllusionBehavior.instance()!=null)IllusionBehavior.instance().before(m);}
    @Inject(method="aiStep()V",at=@At("TAIL"))
    private void buildup$clones(CallbackInfo ci){var m=(Illusioner)(Object)this;if(!m.level().isClientSide()&&IllusionBehavior.instance()!=null)IllusionBehavior.instance().tick(m);}
    @Inject(method="getIllusionOffsets(F)[Lnet/minecraft/world/phys/Vec3;",at=@At("HEAD"),cancellable=true)
    private void buildup$sync(float f,CallbackInfoReturnable<Vec3[]> ci){var offsets=IllusionBehavior.offsets((Illusioner)(Object)this);if(offsets!=null)ci.setReturnValue(offsets);}
    @Inject(method="performRangedAttack(Lnet/minecraft/world/entity/LivingEntity;F)V",at=@At("HEAD"),cancellable=true)
    private void buildup$arrows(LivingEntity t,float power,CallbackInfo ci){var m=(Illusioner)(Object)this;var b=IllusionBehavior.instance();if(b!=null&&IllusionState.known(m)){if(!IllusionBehavior.valid(m,t)){ci.cancel();return;}if(b.clones(m,t,power)){b.shot(m);ci.cancel();}}}
    @Inject(method="performRangedAttack(Lnet/minecraft/world/entity/LivingEntity;F)V",at=@At("TAIL"))
    private void buildup$shot(LivingEntity t,float power,CallbackInfo ci){var m=(Illusioner)(Object)this;if(IllusionBehavior.instance()!=null&&IllusionState.known(m))IllusionBehavior.instance().shot(m);}
}
