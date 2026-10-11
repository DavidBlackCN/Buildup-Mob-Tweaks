/* Mob AI Tweaks adaptation: Copyright (c) 2024 N0t_UN_Owen, MIT. See NOTICE.md. */
package com.davidblackcn.buildupmobtweaks.rebuild.general;

import com.davidblackcn.buildupmobtweaks.feature.*;
import com.davidblackcn.buildupmobtweaks.rebuild.skeleton.*;
import com.davidblackcn.buildupmobtweaks.rebuild.zombie.*;
import com.davidblackcn.buildupmobtweaks.rebuild.pillager.PillagerBehavior;
import com.davidblackcn.buildupmobtweaks.rebuild.raid.*;
import com.davidblackcn.buildupmobtweaks.rebuild.ecology.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.feline.*;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.monster.zombie.Drowned;
import com.davidblackcn.buildupmobtweaks.rebuild.drowned.DrownedBehavior;
import net.minecraft.world.entity.monster.illager.Pillager;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.BowItem;
import java.util.UUID;
import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import net.fabricmc.fabric.api.attachment.v1.*;

/** E4 owns no equipment, persistent data or target lifecycle. */
public final class E4Behavior {
    private static FeatureRegistry features;
    private static final AttachmentType<Draw> DRAW=AttachmentRegistry.createDefaulted(BuildupMobTweaks.id("stagger_runtime"),Draw::new);
    public static void register(FeatureRegistry f){features=f;}
    public static boolean enabled(Mob m,FeatureId id){
        if(features==null||!features.isEnabled(id)||!m.isAlive()||m.isNoAi()||m.entityTags().contains("buildupmobtweaks:vanilla_ai")
                ||m.entityTags().contains("buildupmobtweaks:disable_"+id.id().getPath()))return false;
        if(m instanceof AbstractSkeleton s)return SkeletonBehavior.supported(s)?SkeletonBehavior.instance().enabled(s,id):ExtendedSkeletonBehavior.supported(s)&&ExtendedSkeletonBehavior.instance().enabled(s,id);
        if(m instanceof Drowned d)return DrownedBehavior.instance().enabled(d,id);
        if(m instanceof Zombie z)return ZombieBehavior.supported(z)&&ZombieBehavior.instance().enabled(z,id);
        if(m instanceof Pillager p)return PillagerBehavior.instance().enabled(p,id);
        if(m instanceof Witch)return RaidState.known(m,RaidState.WITCH)&&RaidGate.enabled(m,features,id);
        if(m instanceof net.minecraft.world.entity.monster.illager.Evoker e&&(id==FeatureId.EVOKER_FLEE_SPEED||id==FeatureId.EVOKER_AVOID_TARGET_FIX||id==FeatureId.BURN_FREEZE_VISUAL||id==FeatureId.BURNING_PROJECTILE_VISUAL))return com.davidblackcn.buildupmobtweaks.rebuild.evoker.EvokerBehavior.instance().enabled(e,id);
        if(m instanceof net.minecraft.world.entity.monster.Vex v&&(id==FeatureId.VEX_PROJECTILE_WEAKNESS||id==FeatureId.BURN_FREEZE_VISUAL))return com.davidblackcn.buildupmobtweaks.rebuild.vex.VexBehavior.instance().enabled(v,id);
        return false;
    }
    public static boolean sneak(Mob m,LivingEntity candidate){
        return features!=null&&features.isEnabled(FeatureId.ANIMAL_SNEAK_APPROACH)&&!m.isNoAi()&&m.isAlive()
            &&(m.getType()==EntityTypes.CAT||m.getType()==EntityTypes.OCELOT||m.getType()==EntityTypes.RABBIT)
            &&(!m.hasAttached(EcologyState.DATA)||EcologyState.known(m))
            &&!m.getType().builtInRegistryHolder().is(EcologyBehavior.EXCLUDED)
            &&!m.entityTags().contains("buildupmobtweaks:vanilla_ai")&&!m.entityTags().contains("buildupmobtweaks:disable_animal_sneak_approach")
            &&candidate instanceof Player&&candidate.isShiftKeyDown();
    }
    public static double discoveryRange(Mob m,LivingEntity t,double range){
        double r=range;
        if(t.isInvisible()&&enabled(m,FeatureId.SENSE_INVISIBILITY))r/=4;
        else if(t.isVisuallyCrawling()&&enabled(m,FeatureId.SENSE_CRAWLING))r/=3;
        else if(t.isShiftKeyDown()&&enabled(m,FeatureId.SENSE_CROUCHING))r/=2;
        if(m.hasEffect(MobEffects.BLINDNESS)&&enabled(m,FeatureId.SENSE_BLINDNESS))r/=3;
        else if(m.hasEffect(MobEffects.NAUSEA)&&enabled(m,FeatureId.SENSE_NAUSEA))r*=.8;
        if(r<range&&(t.isSwinging()||t.isUsingItem()||t.isAutoSpinAttack()))r*=1.2;
        return Math.max(2,Math.min(range,r));
    }
    public static boolean hasDiscovery(Mob m){
        return enabled(m,FeatureId.SENSE_BLINDNESS)||enabled(m,FeatureId.SENSE_CRAWLING)||enabled(m,FeatureId.SENSE_CROUCHING)
            ||enabled(m,FeatureId.SENSE_INVISIBILITY)||enabled(m,FeatureId.SENSE_GLOWING)||enabled(m,FeatureId.SENSE_NAUSEA);
    }
    /** Bounded, transient coordination; the vanilla draw and release remain untouched. */
    public static boolean mayDraw(AbstractSkeleton m){
        Draw d=m.getAttachedOrCreate(DRAW);var t=m.getTarget();long now=m.level().getGameTime();
        if(!enabled(m,FeatureId.SKELETON_STAGGERED_DRAW)||t==null||!t.isAlive()||!m.hasLineOfSight(t)||m.isPassenger()
                ||m.getMainHandItem().is(SkeletonBehavior.EXCLUDED_ITEMS)||m.getOffhandItem().is(SkeletonBehavior.EXCLUDED_ITEMS)
                ||!(m.getMainHandItem().getItem() instanceof BowItem||m.getOffhandItem().getItem() instanceof BowItem)) {d.reset();return true;}
        if(!t.getUUID().equals(d.target)){d.reset();d.target=t.getUUID();}
        if(d.since>=0&&now-d.since>=40){d.reset();return true;}
        if(now<d.poll)return !d.wait;
        d.poll=now+4;d.wait=false;
        var candidates=new java.util.ArrayList<AbstractSkeleton>();
        m.level().getEntities(net.minecraft.world.level.entity.EntityTypeTest.forClass(AbstractSkeleton.class),m.getBoundingBox().inflate(8),s->s!=m&&s.getType()==m.getType(),candidates,32);
        for(var s:candidates)if(s.isAlive()&&s.getTarget()==t&&s.isUsingItem()&&!s.getUseItem().is(SkeletonBehavior.EXCLUDED_ITEMS)&&s.getUseItem().getItem() instanceof BowItem&&enabled(s,FeatureId.SKELETON_STAGGERED_DRAW)){d.wait=true;break;}
        if(d.wait){if(d.since<0)d.since=now;return false;}
        d.since=-1;return true;
    }
    private static final class Draw {UUID target;long since=-1,poll;boolean wait;void reset(){target=null;since=-1;poll=0;wait=false;}}
    public static boolean obstacleJump(AbstractSkeleton m,net.minecraft.world.phys.Vec3 delta){
        if(!enabled(m,FeatureId.STRAFE_OBSTACLE_JUMP)||m.isPassenger()||m.isInWater()||((SkeletonAccess)m).buildup$special()!=0)return false;
        if(!m.onGround()&&(!enabled(m,FeatureId.MOVEMENT_JUMP_INPUT)||m.getDeltaMovement().y<-.3))return false;
        var projected=m.getBoundingBox().move(delta);boolean obstacle=false;
        for(var p:net.minecraft.core.BlockPos.betweenClosed(net.minecraft.util.Mth.floor(projected.minX),net.minecraft.util.Mth.floor(m.getY()),net.minecraft.util.Mth.floor(projected.minZ),net.minecraft.util.Mth.floor(projected.maxX-1.0e-6),net.minecraft.util.Mth.floor(m.getY()),net.minecraft.util.Mth.floor(projected.maxZ-1.0e-6))){
            if(!m.level().hasChunkAt(p))return false;var b=m.level().getBlockState(p);
            if(b.is(net.minecraft.tags.BlockTags.DOORS)||b.is(net.minecraft.tags.BlockTags.FENCES)||b.is(net.minecraft.tags.BlockTags.FENCE_GATES))return false;
            obstacle|=!b.getCollisionShape(m.level(),p).isEmpty();
        }
        double lift=m.onGround()?1:Math.ceil(m.getY())-m.getY();
        if(!obstacle||!m.level().noCollision(m,m.getBoundingBox().move(0,1,0)))return false;
        // The near edge can overlap a ledge before the destination centre reaches its support.
        var top=projected.move(0,lift,0);
        if(!SkeletonBehavior.hazardFree(m,top)||!m.level().noCollision(m,top))return false;
        boolean support=false;
        for(var p:net.minecraft.core.BlockPos.betweenClosed(net.minecraft.util.Mth.floor(top.minX),net.minecraft.util.Mth.floor(top.minY)-1,net.minecraft.util.Mth.floor(top.minZ),net.minecraft.util.Mth.floor(top.maxX-1.0e-6),net.minecraft.util.Mth.floor(top.minY)-1,net.minecraft.util.Mth.floor(top.maxZ-1.0e-6))){var b=m.level().getBlockState(p);if(!b.getFluidState().isEmpty()||b.is(net.minecraft.world.level.block.Blocks.MAGMA_BLOCK)||b.is(net.minecraft.world.level.block.Blocks.CAMPFIRE)||b.is(net.minecraft.world.level.block.Blocks.SOUL_CAMPFIRE))return false;support|=b.isFaceSturdy(m.level(),p,net.minecraft.core.Direction.UP);}
        return support;
    }
    private E4Behavior(){}
}
