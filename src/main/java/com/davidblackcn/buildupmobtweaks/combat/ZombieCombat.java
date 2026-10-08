package com.davidblackcn.buildupmobtweaks.combat;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import com.davidblackcn.buildupmobtweaks.feature.*;
import java.util.*;
import net.fabricmc.fabric.api.attachment.v1.*;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.*;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.Vec3;

public final class ZombieCombat {
    public static final AttachmentType<CompoundTag> DATA = AttachmentRegistry.createPersistent(BuildupMobTweaks.id("zombie_trait"), CompoundTag.CODEC);
    private static final AttachmentType<Boolean> GOALS = AttachmentRegistry.create(BuildupMobTweaks.id("zombie_goals"));
    private static final List<FeatureId> SPECIALS = List.of(FeatureId.ZOMBIE_DOOR_GUARD, FeatureId.ZOMBIE_ACTIVE_GUARD,
            FeatureId.HUSK_SAND_BURROW, FeatureId.ZOMBIE_BABY_RIDER);
    private static ZombieCombat instance;
    private final FeatureRegistry features;
    public ZombieCombat(FeatureRegistry features) { this.features = features; }
    public static ZombieCombat instance() { return instance; }
    public static boolean eligible(Entity mob) { return mob instanceof Zombie && (mob.getType() == EntityTypes.ZOMBIE || mob.getType() == EntityTypes.HUSK); }
    private static boolean applicable(Zombie mob, FeatureId id) {
        if (mob.isBaby()) return id == FeatureId.ZOMBIE_BABY_RIDER && mob.getType() == EntityTypes.ZOMBIE;
        return id != FeatureId.ZOMBIE_BABY_RIDER && (id != FeatureId.HUSK_SAND_BURROW || mob.getType() == EntityTypes.HUSK);
    }
    public void register() {
        instance = this;
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (!eligible(entity)) return;
            var mob = (Zombie) entity; initialize(mob); restoreBurrow(mob);
            if (!mob.hasAttached(GOALS)) {
                mob.setAttached(GOALS, true);
                mob.getGoalSelector().addGoal(0, burrowGoal(mob));
                mob.getGoalSelector().addGoal(1, guardGoal(mob));
            }
        });
        ServerLivingEntityEvents.MOB_CONVERSION.register((old, converted, context) -> {
            if (old.hasAttached(DATA)) converted.setAttached(DATA, old.getAttached(DATA).copy());
        });
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> !(entity instanceof Zombie mob && eligible(mob)
                && mob.getAttached(DATA) != null && mob.getAttached(DATA).getIntOr("version",-1)==1
                && mob.getAttached(DATA).getBooleanOr("burrow_active",false) && source.is(net.minecraft.world.damagesource.DamageTypes.IN_WALL)));
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> !(eligible(entity)
                && blockWithDoor((Zombie) entity, source, amount)));
    }
    public void initialize(Zombie mob) {
        if (!eligible(mob) || mob.level().isClientSide() || mob.hasAttached(DATA)) return;
        var reason = mob.spawnReason();
        boolean fresh = !mob.isLoadedFromDisk() && reason != null && reason != EntitySpawnReason.LOAD
                && reason != EntitySpawnReason.CONVERSION && reason != EntitySpawnReason.DIMENSION_TRAVEL;
        int total = SPECIALS.stream().filter(id -> applicable(mob, id) && features.isEnabled(id)).mapToInt(features::zombieChance).sum();
        String selected = "none"; int level = 0;
        if (fresh) {
            int point = mob.getRandom().nextInt(Math.max(1000, total));
            for (var id : SPECIALS) if (applicable(mob, id) && features.isEnabled(id)) {
                point -= features.zombieChance(id);
                if (point < 0) { selected = id.id().toString(); level = id == FeatureId.ZOMBIE_BABY_RIDER ? 3 : 2; break; }
            }
        }
        var data = new CompoundTag(); data.putInt("version", 1); data.putString("trait", selected); data.putInt("level", level);
        data.putString("exclusive_group", "zombie_special"); data.putString("origin", reason == null ? "UNKNOWN" : reason.name());
        data.putString("status", fresh ? "rolled" : "legacy_or_source_skipped"); data.putLong("next_special_at", 0);
        data.putInt("door_uses", 0); data.putInt("ride_attempts", 0); mob.setAttached(DATA, data);
    }
    public boolean active(Zombie mob, FeatureId id) {
        var data = mob.getAttached(DATA);
        return eligible(mob) && mob.getType().builtInRegistryHolder().is(S2Tags.ZOMBIE_SPECIALS) && !mob.level().isClientSide() && applicable(mob, id) && features.isEnabled(id)
                && data != null && data.getIntOr("version", -1) == 1 && data.getStringOr("trait", "").equals(id.id().toString());
    }
    private boolean ready(Zombie mob) { return mob.level().getGameTime() >= mob.getAttached(DATA).getLongOr("next_special_at", Long.MAX_VALUE); }
    private CompoundTag cooldown(Zombie mob, FeatureId id) {
        var data = mob.getAttached(DATA).copy(); data.putLong("next_special_at", mob.level().getGameTime() + features.zombieCooldown(id));
        mob.setAttached(DATA, data); return data;
    }
    public boolean blockWithDoor(Zombie mob, DamageSource source, float amount) {
        if (mob.isNoAi() || amount <= 0 || !active(mob, FeatureId.ZOMBIE_DOOR_GUARD) || !ready(mob)
                || !mob.getOffhandItem().is(ItemTags.WOODEN_DOORS) || mob.getOffhandItem().getCount() != 1
                || source.is(DamageTypeTags.BYPASSES_SHIELD) || source.getDirectEntity() == null || source.getSourcePosition() == null) return false;
        var toward = source.getSourcePosition().subtract(mob.position()).multiply(1, 0, 1).normalize();
        if (mob.getViewVector(1).multiply(1, 0, 1).normalize().dot(toward) < .5) return false;
        var data = cooldown(mob, FeatureId.ZOMBIE_DOOR_GUARD);
        int uses = data.getIntOr("door_uses", 0) + 1;
        if (uses >= 3) { mob.getOffhandItem().shrink(1); uses = 0; }
        data.putInt("door_uses", uses);
        particles(mob); return true;
    }
    private static void particles(Zombie mob) {
        ((ServerLevel) mob.level()).sendParticles(ParticleTypes.CLOUD, mob.getX(), mob.getY() + .4, mob.getZ(), 12, .4, .2, .4, .03);
    }
    public void tick(Zombie mob) {
        if (!eligible(mob) || mob.level().isClientSide() || !mob.hasAttached(DATA)) return;
        var data = mob.getAttached(DATA);
        if (data.getIntOr("version", -1) != 1) return;
        if (!active(mob, FeatureId.ZOMBIE_BABY_RIDER)) {
            if (mob.isPassenger() && mob.getVehicle().getUUID().toString().equals(data.getStringOr("mount", ""))) mob.stopRiding();
            return;
        }
        if (mob.isPassenger() || !ready(mob) || data.getIntOr("ride_attempts", 3) >= 3 || (mob.tickCount + mob.getId()) % 20 != 0) return;
        attemptRide(mob);
    }
    public boolean attemptRide(Zombie mob) {
        if (!active(mob, FeatureId.ZOMBIE_BABY_RIDER) || mob.isPassenger() || !ready(mob) || mob.getAttached(DATA).getIntOr("ride_attempts", 3) >= 3) return false;
        var data = cooldown(mob, FeatureId.ZOMBIE_BABY_RIDER); data.putInt("ride_attempts", data.getIntOr("ride_attempts", 0) + 1);
        var candidates = new ArrayList<Zombie>();
        mob.level().getEntities(EntityTypeTest.forClass(Zombie.class), mob.getBoundingBox().inflate(4),
                adult -> adult != mob && adult.getType() == EntityTypes.ZOMBIE && !adult.isBaby() && adult.isAlive()
                        && !adult.isPassenger() && !adult.isVehicle() && mob.hasLineOfSight(adult), candidates, 8);
        for (var adult : candidates) if (mob.startRiding(adult)) {
            data.putString("mount", adult.getUUID().toString()); data.putInt("ride_attempts", 3); return true;
        }
        return false;
    }
    public Vec3 burrowDestination(Zombie mob) {
        var target = mob.getTarget();
        if (!active(mob, FeatureId.HUSK_SAND_BURROW) || !SkeletonCombat.validTarget(mob, target) || !mob.onGround()
                || mob.isPassenger() || mob.isVehicle() || mob.isInWater() || !mob.hasLineOfSight(target)
                || !mob.level().getBlockState(mob.blockPosition().below()).is(BlockTags.SAND)
                || !mob.level().getBlockState(target.blockPosition().below()).is(BlockTags.SAND)) return null;
        var offset = target.position().subtract(mob.position()); double distance = offset.horizontalDistance();
        if (distance < 5 || distance > 12 || Math.abs(offset.y) > .5) return null;
        var step = offset.multiply(1, 0, 1).normalize().scale(Math.min(3, distance - 2));
        for (int i = 1; i <= 6; i++) {
            var delta = step.scale(i / 6.0); var pos = BlockPos.containing(mob.position().add(delta));
            if (!mob.level().getBlockState(pos.below()).is(BlockTags.SAND)
                    || !mob.level().getBlockState(pos).isAir() || !mob.level().getBlockState(pos.above()).isAir()
                    || !mob.level().noCollision(mob, mob.getBoundingBox().move(delta))) return null;
        }
        return mob.position().add(step);
    }
    public Goal guardGoal(Zombie mob) { return new GuardGoal(mob); }
    public Goal burrowGoal(Zombie mob) { return new BurrowGoal(mob); }
    private final class GuardGoal extends Goal {
        private final Zombie mob; private long until, nextBasic; private boolean advanced; private ItemStack held;
        GuardGoal(Zombie mob) { this.mob = mob; setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK)); }
        @Override public boolean canUse() {
            advanced = active(mob, FeatureId.ZOMBIE_ACTIVE_GUARD);
            return !mob.isBaby() && !mob.isUsingItem() && mob.getOffhandItem().getItem() == Items.SHIELD
                    && SkeletonCombat.validTarget(mob, mob.getTarget()) && mob.hasLineOfSight(mob.getTarget())
                    && mob.distanceToSqr(mob.getTarget()) <= (advanced ? 64 : 9)
                    && (advanced ? ready(mob) : features.isEnabled(FeatureId.ZOMBIE_SHIELD_USE) && mob.level().getGameTime() >= nextBasic);
        }
        @Override public void start() {
            held = mob.getOffhandItem(); mob.startUsingItem(InteractionHand.OFF_HAND);
            until = mob.level().getGameTime() + (advanced ? 30 : 15);
            if (advanced) cooldown(mob, FeatureId.ZOMBIE_ACTIVE_GUARD); else nextBasic = mob.level().getGameTime() + 80;
        }
        @Override public boolean canContinueToUse() {
            return mob.level().getGameTime() < until && mob.getUseItem() == held && !held.isEmpty()
                    && (advanced ? active(mob, FeatureId.ZOMBIE_ACTIVE_GUARD) : features.isEnabled(FeatureId.ZOMBIE_SHIELD_USE))
                    && SkeletonCombat.validTarget(mob, mob.getTarget()) && mob.hasLineOfSight(mob.getTarget());
        }
        @Override public void tick() { mob.getNavigation().stop(); mob.getLookControl().setLookAt(mob.getTarget(), 30, 30); }
        @Override public void stop() { if (mob.getUseItem() == held) mob.stopUsingItem(); }
        @Override public boolean requiresUpdateEveryTick() { return true; }
    }
    public static void restoreBurrow(Zombie mob) {
        var data=mob.getAttached(DATA);
        if(data==null||data.getIntOr("version",-1)!=1||!data.getBooleanOr("burrow_active",false))return;
        double x=data.getDoubleOr("burrow_x",mob.getX()),y=data.getDoubleOr("burrow_y",mob.getY()),z=data.getDoubleOr("burrow_z",mob.getZ());
        // Prefer the original surface. A bounded upward search handles a block placed over the exit while unloaded.
        for(int offset=0;offset<=8;offset++){
            Vec3 point=new Vec3(x,y+offset,z);
            if(mob.level().noCollision(mob,mob.getBoundingBox().move(point.subtract(mob.position())))){y+=offset;break;}
        }
        mob.teleportTo(x,y,z);mob.noPhysics=false;mob.setNoGravity(false);mob.setDeltaMovement(Vec3.ZERO);
        data=data.copy();data.putBoolean("burrow_active",false);mob.setAttached(DATA,data);
    }
    private boolean sandTunnel(Zombie mob,Vec3 start,Vec3 end){
        if(!((ServerLevel)mob.level()).getGameRules().get(net.minecraft.world.level.gamerules.GameRules.MOB_GRIEFING))return false;
        for(int i=0;i<=6;i++){
            var pos=BlockPos.containing(start.lerp(end,i/6.0));
            if(!mob.level().getBlockState(pos).isAir()||!mob.level().getBlockState(pos.above()).isAir())return false;
            for(int depth=1;depth<=3;depth++)if(!mob.level().getBlockState(pos.below(depth)).is(BlockTags.SAND))return false;
        }
        return true;
    }
    private final class BurrowGoal extends Goal {
        private final Zombie mob;private UUID target;private int ticks;private Vec3 start,end;private boolean finished;
        BurrowGoal(Zombie mob){this.mob=mob;setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK,Flag.JUMP));}
        @Override public boolean canUse(){
            end=burrowDestination(mob);return end!=null&&ready(mob)&&!mob.noPhysics&&!mob.isNoGravity()&&sandTunnel(mob,mob.position(),end);
        }
        @Override public void start(){
            start=mob.position();target=mob.getTarget().getUUID();ticks=0;finished=false;cooldown(mob,FeatureId.HUSK_SAND_BURROW);particles(mob);
            var data=mob.getAttached(DATA).copy();data.putBoolean("burrow_active",true);data.putDouble("burrow_x",start.x);data.putDouble("burrow_y",start.y);data.putDouble("burrow_z",start.z);mob.setAttached(DATA,data);
        }
        @Override public boolean canContinueToUse(){return !finished&&mob.isAlive()&&active(mob,FeatureId.HUSK_SAND_BURROW)&&mob.getTarget()!=null&&mob.getTarget().isAlive()
                &&mob.getTarget().getUUID().equals(target)&&sandTunnel(mob,start,end);}
        @Override public void tick(){
            if(!canContinueToUse()){finished=true;restoreBurrow(mob);return;}
            ticks++;mob.getNavigation().stop();mob.getMoveControl().setWait();mob.setDeltaMovement(Vec3.ZERO);
            if(ticks<=15){if(ticks%5==0)particles(mob);return;}
            mob.noPhysics=true;mob.setNoGravity(true);
            double progress=Math.min(1,(ticks-15)/20.0);Vec3 point=start.lerp(end,progress).add(0,-Math.sin(progress*Math.PI)*2.2,0);
            mob.teleportTo(point.x,point.y,point.z);
            if(ticks%4==0)((ServerLevel)mob.level()).sendParticles(ParticleTypes.CLOUD,point.x,start.y,point.z,4,.2,0,.2,0);
            if(progress>=1){
                var data=mob.getAttached(DATA).copy();data.putDouble("burrow_x",end.x);data.putDouble("burrow_y",end.y);data.putDouble("burrow_z",end.z);mob.setAttached(DATA,data);
                finished=true;restoreBurrow(mob);particles(mob);
            }
        }
        @Override public void stop(){restoreBurrow(mob);finished=true;}
        @Override public boolean requiresUpdateEveryTick(){return true;}
    }
}