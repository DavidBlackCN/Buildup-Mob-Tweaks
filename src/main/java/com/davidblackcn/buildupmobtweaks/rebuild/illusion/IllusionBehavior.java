/* Mob AI Tweaks behavioral adaptation, Copyright (c) 2024 N0t_UN_Owen, MIT. See NOTICE.md. */
package com.davidblackcn.buildupmobtweaks.rebuild.illusion;
import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import com.davidblackcn.buildupmobtweaks.feature.*;
import com.davidblackcn.buildupmobtweaks.mixin.rebuild.RemainingTargetAccess;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.illager.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import java.util.*;
public final class IllusionBehavior {
    private static IllusionBehavior instance; private final FeatureRegistry features;
    private final Set<AbstractArrow> arrows=Collections.newSetFromMap(new IdentityHashMap<>());
    private final Set<Entity> pending=Collections.newSetFromMap(new IdentityHashMap<>());
    private static final TagKey<EntityType<?>> EXCLUDED=TagKey.create(Registries.ENTITY_TYPE,BuildupMobTweaks.id("illusion_ai_excluded"));
    public IllusionBehavior(FeatureRegistry f){features=f;}
    public static IllusionBehavior instance(){return instance;}
    public static boolean supported(Entity m){return m.getType()==EntityTypes.ILLUSIONER||m.getType()==EntityTypes.VINDICATOR;}
    public boolean enabled(Mob m,FeatureId id){return supported(m)&&IllusionState.known(m)&&features.isEnabled(id)&&!m.getType().builtInRegistryHolder().is(EXCLUDED)&&!m.entityTags().contains("buildupmobtweaks:vanilla_ai")&&!m.entityTags().contains("buildupmobtweaks:disable_"+id.id().getPath());}
    public static boolean valid(Mob m,LivingEntity t){return t!=null&&t.isAlive()&&t.level()==m.level()&&!t.isSpectator()&&!(t instanceof Player p&&p.isCreative())&&!m.isAlliedTo(t)&&m.canAttack(t);}
    public static InteractionHand bow(Mob m){return m.getMainHandItem().is(Items.BOW)?InteractionHand.MAIN_HAND:m.getOffhandItem().is(Items.BOW)?InteractionHand.OFF_HAND:null;}
    public static void reserve(Mob m,String key,long delay){var d=m.getAttached(IllusionState.DATA).copy();d.putLong(key,m.level().getGameTime()+delay);m.setAttached(IllusionState.DATA,d);}
    public static boolean ready(Mob m,String key){return m.level().getGameTime()>=m.getAttached(IllusionState.DATA).getLongOr(key,0);}
    public void initialize(Mob m){if(!m.hasAttached(IllusionState.DATA)){var d=new CompoundTag();d.putInt("version",1);d.putString("birth","legacy_skipped");m.setAttached(IllusionState.DATA,d);}}
    public void born(Illusioner m,EntitySpawnReason reason){if(m.hasAttached(IllusionState.DATA))return;initialize(m);var d=m.getAttached(IllusionState.DATA).copy();d.putString("birth",reason.name());
        boolean fresh=reason!=EntitySpawnReason.LOAD&&reason!=EntitySpawnReason.CONVERSION&&reason!=EntitySpawnReason.DIMENSION_TRAVEL&&!m.isLoadedFromDisk();
        d.putBoolean("clone_trait",fresh&&enabled(m,FeatureId.ILLUSIONER_CLONE_ARROWS)&&m.getRandom().nextInt(1000)<features.majorChance(FeatureId.ILLUSIONER_CLONE_ARROWS));m.setAttached(IllusionState.DATA,d);}
    public void register(){instance=this;ServerEntityEvents.ENTITY_LOAD.register((e,l)->{if(e instanceof Mob m&&supported(m)){initialize(m);if(IllusionState.known(m)&&!IllusionState.runtime(m).installed){IllusionState.runtime(m).installed=true;if(m instanceof Vindicator v)((RemainingTargetAccess)v).buildup$targets().addGoal(5,new SupportGoal(v,this));}}
        if(e instanceof AbstractArrow a&&marked(a)&&!arrows.contains(a))pending.add(a);});
        ServerEntityEvents.ENTITY_UNLOAD.register((e,l)->{if(e instanceof Mob m&&supported(m)){cleanup(m);for(var a:arrows)if(a.getOwner()==m)pending.add(a);}if(e instanceof AbstractArrow a)arrows.remove(a);});
        ServerTickEvents.END_SERVER_TICK.register(s->{for(var a:List.copyOf(arrows)){var d=a.getAttached(IllusionState.ARROW);if(a.isRemoved()){arrows.remove(a);continue;}if(!(a.getOwner() instanceof Illusioner m)||!m.isAlive()||m.isNoAi()||!enabled(m,FeatureId.ILLUSIONER_CLONE_ARROWS)||a.level().getGameTime()>=d.getLongOr("expires",0))pending.add(a);}for(var e:List.copyOf(pending))if(!e.isRemoved())e.discard();pending.clear();});
        ServerLifecycleEvents.SERVER_STOPPED.register(s->{arrows.clear();pending.clear();});
    }
    public void cleanup(Mob m){var r=IllusionState.runtime(m);r.positions=new Vec3[0];r.anchor=null;r.visible=false;if(m.hasAttached(IllusionState.VISUAL))m.setAttached(IllusionState.VISUAL,new CompoundTag());if(r.supportTarget!=null&&m.getTarget()==r.supportTarget)m.setTarget(null);r.supportTarget=null;}
    private boolean combat(Illusioner m){return IllusionState.known(m)&&m.isAlive()&&!m.isNoAi()&&valid(m,m.getTarget())&&bow(m)!=null;}
    public void before(Illusioner m){if(!IllusionState.known(m))return;if(!combat(m)){if(m.getTarget()!=null&&!valid(m,m.getTarget()))m.setTarget(null);if(m.isUsingItem()&&bow(m)!=null)m.stopUsingItem();cleanup(m);}}
    /** Four bounded ground candidates; world coordinates are authoritative, never extra entities. */
    public void tick(Illusioner m){if(!combat(m)||!m.isInvisible()||!(enabled(m,FeatureId.ILLUSIONER_SWAP)||enabled(m,FeatureId.ILLUSIONER_CLONE_ARROWS))){cleanup(m);return;}
        var r=IllusionState.runtime(m);if(r.visible&&r.anchor!=null){var travel=m.position().subtract(r.anchor);for(int i=0;i<r.positions.length;i++)r.positions[i]=r.positions[i].add(travel);}
        long now=m.level().getGameTime();boolean refresh=!r.visible||m.hurtTime==1||now-r.lastRefresh>=1200;
        if(refresh&&!m.isCastingSpell()){r.visible=true;r.lastRefresh=now;var points=new ArrayList<Vec3>();int first=m.getRandom().nextInt(4);for(int i=0;i<4;i++){int k=(i+first)%4;Vec3 delta=switch(k){case 0->new Vec3(2.5,0,0);case 1->new Vec3(-2.5,0,0);case 2->new Vec3(0,0,2.5);default->new Vec3(0,0,-2.5);};var p=m.position().add(delta);if(safe(m,p))points.add(p);}
            r.positions=points.toArray(Vec3[]::new);
            if(enabled(m,FeatureId.ILLUSIONER_SWAP)&&!m.isPassenger()&&!m.isVehicle()&&ready(m,"swap_ready")){reserve(m,"swap_ready",200);if(r.positions.length>0){var p=r.positions[0];m.getNavigation().stop();m.getMoveControl().strafe(0,0);m.teleportTo(p.x,p.y,p.z);r.swaps++;if(enabled(m,FeatureId.ILLUSIONER_SHOOT_PAUSE))reserve(m,"pause_ready",10);}}
        }
        r.anchor=m.position();if(r.visible)sync(m);
    }
    private void sync(Illusioner m){var d=new CompoundTag();d.putInt("version",1);var positions=Arrays.stream(IllusionState.runtime(m).positions).filter(p->p.distanceToSqr(m.position())<=36).toList();d.putInt("count",positions.size());int i=0;for(var p:positions){var v=p.subtract(m.position());d.putDouble("x"+i,v.x);d.putDouble("y"+i,v.y);d.putDouble("z"+i,v.z);i++;}if(!d.equals(m.getAttached(IllusionState.VISUAL)))m.setAttached(IllusionState.VISUAL,d);}
    public static Vec3[] offsets(Illusioner m){var d=m.getAttached(IllusionState.VISUAL);if(d==null||d.getIntOr("version",0)!=1)return null;var result=new Vec3[4];int count=Math.min(4,Math.max(0,d.getIntOr("count",0)));for(int i=0;i<4;i++)result[i]=i<count?new Vec3(d.getDoubleOr("x"+i,0),d.getDoubleOr("y"+i,0),d.getDoubleOr("z"+i,0)):Vec3.ZERO;return result;}
    public static boolean safe(Illusioner m,Vec3 p){var level=(ServerLevel)m.level();var bp=BlockPos.containing(p);if(!level.hasChunkAt(bp)||!level.getWorldBorder().isWithinBounds(bp)||!level.getBlockState(bp.below()).isFaceSturdy(level,bp.below(),Direction.UP)||!level.getFluidState(bp).isEmpty()||!level.getFluidState(bp.above()).isEmpty())return false;
        for(var b:List.of(bp,bp.below(),bp.above())){var s=level.getBlockState(b);if(s.is(Blocks.FIRE)||s.is(Blocks.SOUL_FIRE)||s.is(Blocks.MAGMA_BLOCK)||s.is(Blocks.CAMPFIRE)||s.is(Blocks.SOUL_CAMPFIRE))return false;}return level.noCollision(m,m.getBoundingBox().move(p.subtract(m.position())));}
    public boolean pause(Illusioner m){return enabled(m,FeatureId.ILLUSIONER_SHOOT_PAUSE)&&(!ready(m,"pause_ready")||m.isCastingSpell());}
    public void shot(Illusioner m){IllusionState.runtime(m).shots++;if(enabled(m,FeatureId.ILLUSIONER_SHOOT_PAUSE))reserve(m,"pause_ready",20);}
    public boolean clones(Illusioner m,LivingEntity t,float power){if(!combat(m)||!m.isInvisible()||m.isCastingSpell()||m.getTarget()!=t||!enabled(m,FeatureId.ILLUSIONER_CLONE_ARROWS)||!m.getAttached(IllusionState.DATA).getBooleanOr("clone_trait",false)||!ready(m,"arrow_ready"))return false;
        var r=IllusionState.runtime(m);var points=new ArrayList<Vec3>();for(var p:r.positions){if(points.size()==2)break;var eye=p.add(0,m.getEyeHeight(),0);if(p.distanceToSqr(m.position())>.25&&p.distanceToSqr(m.position())<=36&&safe(m,p)&&m.level().clip(new ClipContext(eye,t.getEyePosition(),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,m)).getType()==HitResult.Type.MISS)points.add(eye);}if(points.isEmpty())return false;
        reserve(m,"arrow_ready",120);var stack=m.getItemInHand(bow(m));var projectile=m.getProjectile(stack);int made=0;
        for(var p:points){var a=ProjectileUtil.getMobArrow(m,projectile,power,stack);a.setPos(p);a.pickup=AbstractArrow.Pickup.DISALLOWED;var d=new CompoundTag();d.putInt("version",1);d.putString("owner",m.getUUID().toString());d.putLong("expires",m.level().getGameTime()+200);a.setAttached(IllusionState.ARROW,d);var delta=new Vec3(t.getX(),t.getY(1.0/3),t.getZ()).subtract(p);double horizontal=Math.sqrt(delta.x*delta.x+delta.z*delta.z);a.shoot(delta.x,delta.y+horizontal*.2,delta.z,1.6f,m.rangedAttackUncertainty((ServerLevel)m.level()));arrows.add(a);if(m.level().addFreshEntity(a)){a.applyOnProjectileSpawned((ServerLevel)m.level(),projectile);made++;pending.remove(a);}else arrows.remove(a);}
        if(made==0)return false;r.volleys++;r.arrows+=made;
        // Replacing the native shot must retain its audible release cue, once per volley.
        m.playSound(net.minecraft.sounds.SoundEvents.SKELETON_SHOOT,1,1/(m.getRandom().nextFloat()*.4f+.8f));return true;
    }
    public static boolean marked(AbstractArrow a){var d=a.getAttached(IllusionState.ARROW);return d!=null&&d.getIntOr("version",-1)==1;}
    public static boolean allyHit(AbstractArrow a,Entity t){return marked(a)&&a.getOwner() instanceof Illusioner m&&a.getAttached(IllusionState.ARROW).getStringOr("owner","").equals(m.getUUID().toString())&&(m==t||m.isAlliedTo(t));}
}
