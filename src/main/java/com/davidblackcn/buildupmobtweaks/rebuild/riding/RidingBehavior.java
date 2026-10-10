/* Mob AI Tweaks riding adaptation: Copyright (c) 2024 N0t_UN_Owen, MIT. See NOTICE.md. */
package com.davidblackcn.buildupmobtweaks.rebuild.riding;
import com.davidblackcn.buildupmobtweaks.feature.*;
import com.davidblackcn.buildupmobtweaks.rebuild.relations.RelationBehavior;
import com.davidblackcn.buildupmobtweaks.rebuild.skeleton.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.animal.camel.CamelHusk;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.equine.SkeletonHorse;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import java.util.*;
public final class RidingBehavior {
    private static RidingBehavior instance;private final FeatureRegistry features;
    private static final net.minecraft.tags.TagKey<EntityType<?>> EXCLUDED=net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE,com.davidblackcn.buildupmobtweaks.BuildupMobTweaks.id("riding_ai_excluded"));
    public RidingBehavior(FeatureRegistry features){this.features=features;}
    public static RidingBehavior instance(){return instance;}
    public boolean gate(Mob m,FeatureId id){return features.isEnabled(id)&&!m.getType().builtInRegistryHolder().is(EXCLUDED)&&!m.entityTags().contains("buildupmobtweaks:vanilla_ai")&&!m.entityTags().contains("buildupmobtweaks:disable_"+id.id().getPath());}
    public boolean enabled(Mob m,FeatureId id){return RidingState.known(m)&&gate(m,id);}
    private static boolean actor(Mob m){return m instanceof Monster&&net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(m.getType()).getNamespace().equals("minecraft");}
    public void register(){instance=this;
        ServerEntityEvents.ENTITY_LOAD.register((e,l)->{if(e instanceof Mob m&&actor(m))install(m);});
        ServerEntityEvents.ENTITY_UNLOAD.register((e,l)->{if(e instanceof Mob m&&RidingState.known(m))cancel(m);});
        ServerLivingEntityEvents.AFTER_DAMAGE.register((e,source,base,taken,blocked)->{if(e instanceof Mob m&&taken>0&&enabled(m,FeatureId.HOSTILE_ESCAPE_SEAT)&&m.getVehicle() instanceof AbstractBoat){var r=RidingState.runtime(m);r.hurtPending=true;r.hurtAmount=taken;r.hurtExpires=m.level().getGameTime()+20;}});
        ServerLivingEntityEvents.AFTER_DEATH.register((e,s)->{if(e instanceof Mob m&&m.hasAttached(RidingState.RESERVE)&&m.level() instanceof ServerLevel l){var item=m.removeAttached(RidingState.RESERVE);float chance=RidingState.known(m)?m.getAttached(RidingState.DATA).getFloatOr("reserve_drop",.085f):.085f;
            if(item!=null&&!item.isEmpty()&&!SkeletonState.generatedSword(item)&&!EnchantmentHelper.has(item,EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP)&&l.getGameRules().get(GameRules.MOB_DROPS)&&m.getRandom().nextFloat()<chance)m.spawnAtLocation(l,item);}});
    }
    private void install(Mob m){if(RelationBehavior.unknownState(m))return;var r=RidingState.runtime(m);if(r.installed)return;r.installed=true;
        if(!m.hasAttached(RidingState.DATA)){var d=new CompoundTag();d.putInt("version",1);m.setAttached(RidingState.DATA,d);
            boolean fresh=!m.isLoadedFromDisk()&&m.spawnReason()!=null&&m.spawnReason()!=EntitySpawnReason.LOAD&&m.spawnReason()!=EntitySpawnReason.CONVERSION&&m.spawnReason()!=EntitySpawnReason.DIMENSION_TRAVEL;
            d.putBoolean("chicken",fresh&&m instanceof Zombie&&enabled(m,FeatureId.CHICKEN_JOCKEY_CHARGE)&&m.getRandom().nextInt(1000)<features.majorChance(FeatureId.CHICKEN_JOCKEY_CHARGE));
            d.putBoolean("horse",fresh&&m instanceof AbstractSkeleton&&enabled(m,FeatureId.SKELETON_HORSE_CHARGE)&&m.getRandom().nextInt(1000)<features.majorChance(FeatureId.SKELETON_HORSE_CHARGE));
            d.putLong("chicken_ready",m.level().getGameTime()+100);d.putLong("horse_ready",m.level().getGameTime()+200);
        }
        if(!RidingState.known(m))return;
        r.action=new RidingGoal(m,this);m.getGoalSelector().addGoal(8,r.action);
        m.getGoalSelector().addGoal(3,new BoatEscapeGoal(m,this));
    }
    public void cancel(Mob m){var r=RidingState.runtime(m);if(r.action!=null)r.action.cancel();r.hurtPending=false;}
    public void boundary(Mob m){if(!RidingState.known(m))return;var r=RidingState.runtime(m);if(r.action!=null&&r.action.active()&&!r.action.permitted())r.action.cancel();if(m.isNoAi()||!m.isAlive())r.hurtPending=false;}
    public static boolean safe(Mob m,Vec3 point){var pos=BlockPos.containing(point);var l=m.level();var box=m.getBoundingBox().move(point.subtract(m.position()));
        int x0=net.minecraft.util.Mth.floor(box.minX+.001),x1=net.minecraft.util.Mth.floor(box.maxX-.001),z0=net.minecraft.util.Mth.floor(box.minZ+.001),z1=net.minecraft.util.Mth.floor(box.maxZ-.001);
        if(x1-x0>3||z1-z0>3)return false;
        for(int x=x0;x<=x1;x++)for(int z=z0;z<=z1;z++){var feet=new BlockPos(x,pos.getY(),z);var floor=feet.below();if(!l.hasChunkAt(feet))return false;var s=l.getBlockState(floor);
            if(!s.isFaceSturdy(l,floor,Direction.UP)||s.is(Blocks.MAGMA_BLOCK)||s.is(Blocks.CAMPFIRE)||s.is(Blocks.SOUL_CAMPFIRE)||s.is(Blocks.FIRE)||l.getBlockState(feet).is(Blocks.FIRE)||!l.getFluidState(feet).isEmpty())return false;}
        return l.noCollision(m,box);
    }
    public static Mob vehicle(Mob m){if(!(m.getVehicle() instanceof Mob v)||v.getFirstPassenger()!=m||v.isNoAi()||!v.isAlive()||v.getPassengers().stream().anyMatch(e->e instanceof net.minecraft.world.entity.player.Player))return null;return v;}
    public static boolean valid(Mob m){return m.isAlive()&&!m.isNoAi()&&RelationBehavior.valid(m,m.getTarget())&&m.hasLineOfSight(m.getTarget());}
    static final class BoatEscapeGoal extends net.minecraft.world.entity.ai.goal.Goal {
        private final Mob mob;private final RidingBehavior behavior;private Vec3 landing;
        BoatEscapeGoal(Mob m,RidingBehavior b){mob=m;behavior=b;}
        public boolean canUse(){var r=RidingState.runtime(mob);long now=mob.level().getGameTime();if(!r.hurtPending)return false;
            if(!behavior.enabled(mob,FeatureId.HOSTILE_ESCAPE_SEAT)||!valid(mob)||!(mob.getVehicle() instanceof AbstractBoat)||now>r.hurtExpires){r.hurtPending=false;return false;}
            r.hurtPending=false;var d=mob.getAttached(RidingState.DATA);if(now<d.getLongOr("escape_ready",0))return false;d.putLong("escape_ready",now+100);
            if(mob.getRandom().nextFloat()*mob.getMaxHealth()>=r.hurtAmount)return false;
            var base=mob.getVehicle().blockPosition();for(int y=1;y>=-1;y--)for(var dir:Direction.Plane.HORIZONTAL){var p=Vec3.atBottomCenterOf(base.relative(dir,2).offset(0,y,0));if(safe(mob,p)){landing=p;return true;}}return false;
        }
        public void start(){if(landing!=null&&mob.getVehicle() instanceof AbstractBoat&&safe(mob,landing)){mob.stopRiding();mob.teleportTo(landing.x,landing.y,landing.z);RidingState.runtime(mob).escapes++;}landing=null;}
        public boolean canContinueToUse(){return false;}
    }
}
