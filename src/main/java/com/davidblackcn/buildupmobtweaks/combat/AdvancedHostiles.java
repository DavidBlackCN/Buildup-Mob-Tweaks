package com.davidblackcn.buildupmobtweaks.combat;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import com.davidblackcn.buildupmobtweaks.feature.*;
import com.davidblackcn.buildupmobtweaks.mixin.SpellTimerAccess;
import java.util.EnumSet;
import net.fabricmc.fabric.api.attachment.v1.*;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.entity.monster.illager.*;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.Vec3;

/** Rare attacks have one saved choice, a reserved cooldown, and a cancellable Goal. */
public final class AdvancedHostiles {
    public static final AttachmentType<CompoundTag> DATA = AttachmentRegistry.createPersistent(BuildupMobTweaks.id("major_trait"), CompoundTag.CODEC);
    private static final AttachmentType<Boolean> INSTALLED = AttachmentRegistry.create(BuildupMobTweaks.id("major_goals"));
    public static final AttachmentType<Boolean> CLONE_VISUAL = AttachmentRegistry.create(BuildupMobTweaks.id("clone_visual"), builder -> builder.syncWith(net.minecraft.network.codec.ByteBufCodecs.BOOL, AttachmentSyncPredicate.all()));
    private static final String TOTEM_MARKER = "buildupmobtweaks:evoker_totem";
    private static AdvancedHostiles instance;
    private final FeatureRegistry features;
    public AdvancedHostiles(FeatureRegistry features) { this.features = features; }
    public static AdvancedHostiles instance() { return instance; }
    public void register() {
        instance = this;
        SkeletonExtras.register();
        IllagerRelations.register();
        GeneralHostileRules.register();
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (!(entity instanceof Mob mob) || !HostileCombat.eligible(mob)) return;
            initialize(mob);
            if (!mob.hasAttached(INSTALLED)) {
                mob.setAttached(INSTALLED, true);
                SkeletonExtras.install(mob);
                HostileEquipment.install(mob);
                RidingCombat.install(mob);
                IllagerRelations.install(mob);
                GeneralHostileRules.initialize(mob);
                if (mob instanceof Evoker || mob instanceof Enderman) mob.getGoalSelector().addGoal(0, skill(mob));
            }
        });
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, base, taken, blocked) -> {
            if (entity instanceof Illusioner mob && taken > 0) swap(mob);
        });
    }
    public void initialize(Mob mob) {
        if (mob.hasAttached(DATA)) return;
        var data = new CompoundTag(); data.putInt("version", 1); data.putString("trait", "none");
        var reason = mob.spawnReason();
        boolean fresh = !mob.isLoadedFromDisk() && reason != null && reason != EntitySpawnReason.LOAD
                && reason != EntitySpawnReason.CONVERSION && reason != EntitySpawnReason.DIMENSION_TRAVEL;
        var candidates = mob instanceof Evoker ? new FeatureId[]{FeatureId.EVOKER_TOTEM, FeatureId.EVOKER_FIREBALL}
                : mob instanceof Enderman ? new FeatureId[]{FeatureId.ENDERMAN_COMBO}
                : mob instanceof Illusioner ? new FeatureId[]{FeatureId.ILLUSIONER_CLONE_ARROWS}
                : mob.getType()==EntityTypes.STRAY ? new FeatureId[]{FeatureId.STRAY_SNOW_BARRAGE}
                : mob.getType()==EntityTypes.PARCHED ? new FeatureId[]{FeatureId.PARCHED_DODGE}
                : mob.getType()==EntityTypes.WITHER_SKELETON ? new FeatureId[]{FeatureId.WITHER_SKELETON_SKULL}
                : mob.getType()==EntityTypes.SKELETON ? new FeatureId[]{FeatureId.SKELETON_HORSE_CHARGE}
                : mob.getType()==EntityTypes.PIGLIN ? new FeatureId[]{FeatureId.PIGLIN_ITEM_DODGE}
                : mob.getType()==EntityTypes.ZOMBIE ? (mob.isBaby()?new FeatureId[]{FeatureId.CHICKEN_JOCKEY_CHARGE}:new FeatureId[]{FeatureId.ZOMBIE_HORSE_LEADER,FeatureId.ZOMBIE_SLIME_CARRIER}) : new FeatureId[0];
        if (mob.hasAttached(ZombieCombat.DATA) && !mob.getAttached(ZombieCombat.DATA).getStringOr("trait","none").equals("none")) fresh=false;
        if (mob.hasAttached(SkeletonCombat.DATA) && !mob.getAttached(SkeletonCombat.DATA).getStringOr("trait","none").equals("none")) fresh=false;
        if (fresh) {
            int total = 0; for (var id : candidates) total += features.majorChance(id);
            int roll = mob.getRandom().nextInt(Math.max(1000, total));
            for (var id : candidates) { roll -= features.majorChance(id); if (roll < 0) { data.putString("trait", id.id().toString()); break; } }
        }
        data.putString("origin", fresh ? "rolled" : "legacy_or_source_skipped");
        data.putString("exclusive_group", "major_attack"); mob.setAttached(DATA, data);
    }
    public boolean active(Mob mob, FeatureId id) {
        var data = mob.getAttached(DATA);
        if(mob instanceof net.minecraft.world.entity.monster.zombie.Zombie && !mob.getType().builtInRegistryHolder().is(S2Tags.ZOMBIE_SPECIALS))return false;
        if (mob.getType().builtInRegistryHolder().is(S2Tags.RANGED_EXCLUDED) && (id==FeatureId.EVOKER_FIREBALL || id==FeatureId.ILLUSIONER_CLONE_ARROWS || id==FeatureId.STRAY_SNOW_BARRAGE || id==FeatureId.WITHER_SKELETON_SKULL))return false;
        return EnvironmentCombat.on(mob, id) && data != null && data.getIntOr("version", -1) == 1
                && data.getStringOr("trait", "").equals(id.id().toString());
    }
    public void tick(Mob mob) {
        GeneralHostileRules.tick(mob);
        if (mob instanceof Illusioner) {
            boolean visual = active(mob, FeatureId.ILLUSIONER_CLONE_ARROWS) || EnvironmentCombat.on(mob, FeatureId.ILLUSIONER_SWAP);
            if (mob.getAttachedOrElse(CLONE_VISUAL,false) != visual) mob.setAttached(CLONE_VISUAL,visual);
        }
        if (mob instanceof Enderman enderman) {
            var speed = enderman.getAttribute(Attributes.MOVEMENT_SPEED); var id = BuildupMobTweaks.id("enderman_balance");
            if (speed != null) {
                if (EnvironmentCombat.on(mob, FeatureId.ENDERMAN_SPEED)) {
                    if (!speed.hasModifier(id)) speed.addTransientModifier(new AttributeModifier(id, -.15, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
                } else speed.removeModifier(id);
            }
        }
        if (mob instanceof Evoker evoker) totem(evoker);
        SkeletonExtras.tick(mob);
        RidingCombat.tick(mob);
        IllagerRelations.tick(mob);
        if(mob instanceof Pillager pillager) HostileEquipment.shieldBreak(pillager);
    }
    public static boolean marked(ItemStack stack) {
        return stack.is(Items.TOTEM_OF_UNDYING) && stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getBooleanOr(TOTEM_MARKER, false);
    }
    public void totem(Evoker mob) {
        var data = mob.getAttached(DATA); if (data == null || data.getIntOr("version", -1) != 1) return;
        long now = mob.level().getGameTime();
        if (data.getBooleanOr("totem_drop_owned",false) && !marked(mob.getOffhandItem())) {
            if (mob.getDropChances().byEquipment(EquipmentSlot.OFFHAND) == 0) mob.setDropChance(EquipmentSlot.OFFHAND,data.getFloatOr("old_offhand_drop", .085f));
            ((SpellTimerAccess)mob).buildup$setSpellTimer(0);
            data=data.copy(); data.putBoolean("totem_drop_owned",false); mob.setAttached(DATA,data);
        }
        if (marked(mob.getOffhandItem()) && (!active(mob, FeatureId.EVOKER_TOTEM) || now >= data.getLongOr("totem_until", 0))) {
            mob.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY); ((SpellTimerAccess)mob).buildup$setSpellTimer(0); return;
        }
        if (!active(mob, FeatureId.EVOKER_TOTEM) || data.getBooleanOr("totem_spent", false)
                || !mob.isAlive() || mob.isCastingSpell() || mob.getHealth() > mob.getMaxHealth() * .3 || !mob.getOffhandItem().isEmpty()) return;
        data = data.copy(); data.putBoolean("totem_spent", true); data.putLong("totem_until", now + 40); data.putFloat("old_offhand_drop", mob.getDropChances().byEquipment(EquipmentSlot.OFFHAND)); data.putBoolean("totem_drop_owned",true); mob.setAttached(DATA, data);
        var stack = new ItemStack(Items.TOTEM_OF_UNDYING);
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putBoolean(TOTEM_MARKER, true));
        mob.setItemSlot(EquipmentSlot.OFFHAND, stack); mob.setDropChance(EquipmentSlot.OFFHAND, 0);
        ((SpellTimerAccess)mob).buildup$setSpellTimer(40);
        mob.swingForAttack(InteractionHand.OFF_HAND);
        ((ServerLevel)mob.level()).sendParticles(ParticleTypes.TOTEM_OF_UNDYING, mob.getX(), mob.getEyeY(), mob.getZ(), 12, .3, .3, .3, .02);
        // Vanilla consumes this real item. No invulnerability or second resurrection is added.
    }
    public Goal skill(Mob mob) { return new SpecialGoal(mob); }
    private final class SpecialGoal extends Goal {
        private final Mob mob; private LivingEntity target; private int ticks; private boolean running;
        private final FeatureId id;
        private SpecialGoal(Mob mob) {
            this.mob = mob; id = mob instanceof Evoker ? FeatureId.EVOKER_FIREBALL : FeatureId.ENDERMAN_COMBO;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }
        @Override public boolean canUse() {
            return active(mob, id) && SkeletonCombat.validTarget(mob, mob.getTarget()) && mob.hasLineOfSight(mob.getTarget())
                    && mob.distanceToSqr(mob.getTarget()) <= 144 && !mob.isPassenger() && !mob.isInWater()
                    && (!(mob instanceof Evoker evoker) || !evoker.isCastingSpell()) && HostileCombat.instance().ready(mob, "major_next");
        }
        @Override public void start() {
            target = mob.getTarget(); ticks = 0; running = true; mob.getNavigation().stop();
            HostileCombat.instance().reserve(mob, "major_next", features.specialCooldown());
            if (mob instanceof Evoker evoker) { ((SpellTimerAccess)evoker).buildup$setSpellTimer(70); }
        }
        @Override public boolean canContinueToUse() {
            return running && ticks < 66 && active(mob, id) && mob.getTarget() == target
                    && SkeletonCombat.validTarget(mob, target) && mob.hasLineOfSight(target) && mob.distanceToSqr(target) <= 256;
        }
        @Override public boolean requiresUpdateEveryTick() { return true; }
        @Override public void tick() {
            if (!canContinueToUse()) { running = false; return; }
            ticks++; mob.getNavigation().stop(); mob.getLookControl().setLookAt(target, 30, 30);
            var level = (ServerLevel)mob.level();
            if (ticks < 40 && ticks % 4 == 0) for (int i=0; i<3; i++) {
                double angle = ticks * .15 + i * Math.PI * 2 / 3;
                level.sendParticles(mob instanceof Evoker ? ParticleTypes.FLAME : ParticleTypes.PORTAL,
                        mob.getX()+Math.cos(angle),mob.getY()+1,mob.getZ()+Math.sin(angle),1,0,0,0,0);
            }
            if (ticks == 40 || ticks == 52 || ticks == 64) {
                if (mob instanceof Evoker evoker) {
                    Vec3 origin = mob.getEyePosition(); var ball = new SmallFireball(level, evoker, target.getEyePosition().subtract(origin).normalize());
                    ball.setPos(origin); SkeletonExtras.tag(ball,"evoker_fireball",target); level.addFreshEntity(ball);
                } else if (mob instanceof Enderman enderman) {
                    Vec3 away = mob.position().subtract(target.position()).multiply(1,0,1).normalize();
                    if (away.lengthSqr() < .01) away = new Vec3(1,0,0);
                    Vec3 point = target.position().add(away.scale(2));
                    if (level.hasChunkAt(net.minecraft.core.BlockPos.containing(point)) && enderman.canRandomlyTeleportTo(point.x,point.y,point.z))
                        enderman.teleport(point.x,point.y,point.z);
                    if (mob.distanceToSqr(target) < 9 && mob.hasLineOfSight(target)) {
                        if (EnvironmentCombat.on(mob, FeatureId.ENDERMAN_COMBO_ANIMATION)) mob.swingForAttack(InteractionHand.MAIN_HAND);
                        mob.doHurtTarget(level, target);
                    }
                }
            }
        }
        @Override public void stop() {
            running = false; target = null;
            if (mob instanceof Evoker evoker) { ((SpellTimerAccess)evoker).buildup$setSpellTimer(0); }
        }
    }
    public static Vec3[] cloneOffsets() { return new Vec3[]{new Vec3(2.5,0,0),new Vec3(-2.5,0,0),new Vec3(0,0,2.5),new Vec3(0,0,-2.5)}; }
    public void illusionShot(Illusioner mob, LivingEntity target, float power) {
        if (EnvironmentCombat.on(mob, FeatureId.ILLUSIONER_SHOOT_PAUSE)) { mob.getNavigation().stop(); mob.getMoveControl().setWait(); }
        if (!active(mob, FeatureId.ILLUSIONER_CLONE_ARROWS) || !mob.isInvisible() || !SkeletonCombat.validTarget(mob,target)
                || !mob.hasLineOfSight(target) || !HostileCombat.instance().ready(mob,"clone_shot")) return;
        HostileCombat.instance().reserve(mob,"clone_shot",120);
        for (int i=0; i<2; i++) {
            Vec3 position = mob.getEyePosition().add(cloneOffsets()[i]);
            if (!mob.level().noCollision(mob,mob.getBoundingBox().move(cloneOffsets()[i]))) continue;
            var arrow = ProjectileUtil.getMobArrow(mob,new ItemStack(Items.ARROW),power,mob.getMainHandItem());
            arrow.setPos(position); arrow.pickup=AbstractArrow.Pickup.DISALLOWED;
            Vec3 delta=target.getEyePosition().subtract(position); arrow.shoot(delta.x,delta.y,delta.z,1.3f,10);
            mob.level().addFreshEntity(arrow);
        }
    }
    public void swap(Illusioner mob) {
        if (!EnvironmentCombat.on(mob,FeatureId.ILLUSIONER_SWAP) || !mob.isInvisible() || !mob.isAlive()
                || !HostileCombat.instance().ready(mob,"clone_swap")) return;
        HostileCombat.instance().reserve(mob,"clone_swap",200);
        for (Vec3 offset:cloneOffsets()) {
            if (!SkeletonCombat.safeStep(mob,offset.x,offset.z)) continue;
            Vec3 point=mob.position().add(offset); var floor=net.minecraft.core.BlockPos.containing(point).below();
            if (!mob.level().getBlockState(floor).isFaceSturdy(mob.level(),floor,net.minecraft.core.Direction.UP)
                    || !mob.level().noCollision(mob,mob.getBoundingBox().move(offset))) continue;
            mob.teleportTo(point.x,point.y,point.z); break;
        }
    }
}