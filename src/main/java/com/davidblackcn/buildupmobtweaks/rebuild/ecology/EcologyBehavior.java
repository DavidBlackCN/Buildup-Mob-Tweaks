/* Mob AI Tweaks ecology adaptation: Copyright (c) 2024 N0t_UN_Owen, MIT. See NOTICE.md. */
package com.davidblackcn.buildupmobtweaks.rebuild.ecology;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import com.davidblackcn.buildupmobtweaks.feature.*;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.squid.Squid;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.entity.EntityTypeTest;
import java.util.*;

public final class EcologyBehavior {
    private static EcologyBehavior instance;
    private final FeatureRegistry features;
    public static final TagKey<EntityType<?>> EXCLUDED = TagKey.create(Registries.ENTITY_TYPE, BuildupMobTweaks.id("ecology_ai_excluded"));
    public static final TagKey<EntityType<?>> NO_RECOVERY = TagKey.create(Registries.ENTITY_TYPE, BuildupMobTweaks.id("tameable_no_recovery"));
    public EcologyBehavior(FeatureRegistry f) { features = f; }
    public static EcologyBehavior instance() { return instance; }
    public static boolean herd(Mob m) { return m.getType() == EntityTypes.CHICKEN || m.getType() == EntityTypes.COW || m.getType() == EntityTypes.SHEEP || m.getType() == EntityTypes.PIG || m.getType() == EntityTypes.RABBIT; }
    public static boolean supported(Mob m) { return herd(m) || m instanceof Squid || m instanceof TamableAnimal || m instanceof AbstractHorse || m.getType() == EntityTypes.SNIFFER || m.getType() == EntityTypes.WANDERING_TRADER; }
    private static boolean tamed(Mob m) { return m instanceof TamableAnimal a && a.isTame() || m instanceof AbstractHorse h && h.isTamed(); }
    public boolean enabled(Mob m, FeatureId id) {
        return m.isAlive() && !m.isRemoved() && !m.isNoAi() && EcologyState.known(m) && features.isEnabled(id)
            && !m.getType().builtInRegistryHolder().is(EXCLUDED) && !m.entityTags().contains("buildupmobtweaks:vanilla_ai")
            && !m.entityTags().contains("buildupmobtweaks:disable_" + id.id().getPath());
    }
    public static boolean playerValid(Player p) { return p.isAlive() && !p.isRemoved() && !p.isCreative() && !p.isSpectator(); }
    public static boolean livingValid(LivingEntity e) { return e.isAlive() && !e.isRemoved() && (!(e instanceof Player p) || playerValid(p)); }
    public static <T extends Entity> List<T> nearby(Mob m, Class<T> type, double radius, java.util.function.Predicate<T> predicate, int cap) {
        var out = new ArrayList<T>();
        // Cap raw candidates before expensive LOS/trajectory/feature checks, including rejected candidates.
        m.level().getEntities(EntityTypeTest.forClass(type), m.getBoundingBox().inflate(radius), e -> e != m && m.distanceToSqr(e) <= radius * radius, out, cap);
        out.removeIf(predicate.negate());
        EcologyState.runtime(m).queries++;
        return out;
    }
    public void register() {
        instance = this;
        // Register attachments before the first entity can be loaded.
        Objects.requireNonNull(EcologyState.DATA);
        ServerEntityEvents.ENTITY_LOAD.register((e, l) -> { if (e instanceof Mob m && supported(m) && EcologyState.initialize(m)) install(m); });
        ServerEntityEvents.ENTITY_UNLOAD.register((e, l) -> { if (e instanceof Mob m && m.hasAttached(EcologyState.DATA)) { SnifferSearch.cancel(m, "unloaded"); clear(m); } });
        ServerLivingEntityEvents.AFTER_DAMAGE.register((e, source, base, taken, blocked) -> {
            if (!(e instanceof Mob m) || !supported(m) || taken <= 0 || !EcologyState.initialize(m)) return;
            EcologyState.cooldown(m, "recovery_after", 200);
            if (m.getType() == EntityTypes.WANDERING_TRADER && enabled(m, FeatureId.TRADER_SELF_DEFENSE) && source.getEntity() instanceof LivingEntity a && livingValid(a)) {
                var r = EcologyState.runtime(m); r.attacker = a; r.defenseUntil = m.level().getGameTime() + 40;
            }
        });
        ServerTickEvents.END_SERVER_TICK.register(SnifferSearch::tick);
        ServerLifecycleEvents.SERVER_STOPPED.register(SnifferSearch::stop);
    }
    private void install(Mob m) {
        var r = EcologyState.runtime(m); if (r.installed) return; r.installed = true;
        if (m instanceof Chicken c) {
            c.getGoalSelector().addGoal(5, new EcologyGoals.Escape(c, this, true));
            c.getGoalSelector().addGoal(6, new EcologyGoals.Escape(c, this, false));
        }
        if (herd(m)) m.getGoalSelector().addGoal(2, new EcologyGoals.GroupPanic((PathfinderMob)m, this));
        if (m instanceof Cat c) c.getGoalSelector().addGoal(0, new EcologyGoals.Dodge(c, this));
        if (m.getType() == EntityTypes.WANDERING_TRADER) m.getGoalSelector().addGoal(0, new EcologyGoals.Defense(m, this));
    }
    public void ink(Squid squid, net.minecraft.world.damagesource.DamageSource source) {
        if (!(squid.level() instanceof ServerLevel) || !enabled(squid, FeatureId.SQUID_INK_DEFENSE) || !EcologyState.ready(squid, "ink_after")) return;
        EcologyState.cooldown(squid, "ink_after", 100); EcologyState.runtime(squid).ink++;
        var effect = squid.getType() == EntityTypes.GLOW_SQUID ? MobEffects.GLOWING : MobEffects.BLINDNESS;
        LivingEntity direct = source != null && source.isDirect() && source.getEntity() instanceof LivingEntity a && !(a instanceof Squid) && livingValid(a) && a.level() == squid.level() && squid.distanceToSqr(a) <= 64 ? a : null;
        if (direct != null) direct.addEffect(new MobEffectInstance(effect, 100));
        for (var e : nearby(squid, LivingEntity.class, 8, e -> e != direct && !(e instanceof Squid) && livingValid(e), direct == null ? 16 : 15)) {
            int duration = Math.max(1, (int)(100 * (1 - squid.distanceTo(e) / 8)));
            e.addEffect(new MobEffectInstance(effect, duration));
        }
    }
    public void gaze(Squid squid) {
        if (!(squid.level() instanceof ServerLevel) || squid.getType() != EntityTypes.GLOW_SQUID || squid.tickCount % 60 != 0 || !enabled(squid, FeatureId.GLOW_SQUID_GAZE)) return;
        for (var p : nearby(squid, Player.class, 16, EcologyBehavior::playerValid, 8)) {
            var direction = squid.getEyePosition().subtract(p.getEyePosition()); double distance = direction.length();
            if (distance > .01 && p.getViewVector(1).normalize().dot(direction.normalize()) > 1 - .025 / distance && p.hasLineOfSight(squid)) {
                p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 600)); EcologyState.runtime(squid).gazes++;
            }
        }
    }
    public void spread(PathfinderMob origin) {
        if (!herd(origin) || !enabled(origin, FeatureId.ANIMAL_GROUP_PANIC) || !EcologyState.ready(origin, "panic_after") || !livingValidNullable(origin.getLastHurtByMob())) return;
        var threat = origin.getLastHurtByMob();
        if (threat.level() != origin.level() || origin.tickCount - origin.getLastHurtByMobTimestamp() > 60) return;
        EcologyState.cooldown(origin, "panic_after", 200);
        for (var other : nearby(origin, Mob.class, 16, m -> m != origin && m.getType() == origin.getType() && enabled(m, FeatureId.ANIMAL_GROUP_PANIC) && EcologyState.ready(m, "panic_after"), 8)) {
            var r = EcologyState.runtime(other); r.threat = threat.position(); r.panicUntil = origin.level().getGameTime() + 60;
            EcologyState.cooldown(other, "panic_after", 200); EcologyState.runtime(origin).shares++;
        }
    }
    private static boolean livingValidNullable(LivingEntity e) { return e != null && livingValid(e); }
    private static boolean nonPlayerTarget(Mob m) {
        if (m.getTarget() != null && !(m.getTarget() instanceof Player)) return true;
        var memory = net.minecraft.world.entity.ai.memory.MemoryModuleType.ATTACK_TARGET;
        return m.getBrain().hasMemoryValue(memory) && !(m.getBrain().getMemory(memory).orElse(null) instanceof Player);
    }
    public void horn(Player p) {
        if (!(p.level() instanceof ServerLevel l) || !playerValid(p) || !features.isEnabled(FeatureId.GOAT_HORN_AGGRO) || !p.isUsingItem() || !p.getUseItem().has(DataComponents.INSTRUMENT) || !EcologyState.initialize(p) || !EcologyState.ready(p, "horn_after")) return;
        EcologyState.cooldown(p, "horn_after", 100);
        var found = new ArrayList<Monster>();
        l.getEntities(EntityTypeTest.forClass(Monster.class), p.getBoundingBox().inflate(24), m -> m.distanceToSqr(p) <= 576, found, 16);
        for (var m : found) if (m.isAlive() && !m.isNoAi() && !m.getType().builtInRegistryHolder().is(EXCLUDED) && !m.entityTags().contains("buildupmobtweaks:vanilla_ai") && !m.entityTags().contains("buildupmobtweaks:disable_goat_horn_aggro") && m.canAttack(p) && m.hasLineOfSight(p) && !nonPlayerTarget(m) && EcologyState.initialize(m)) {
            var r = EcologyState.runtime(m); if (m.getTarget() == p) continue;
            r.hornTarget = p; r.hornUntil = l.getGameTime() + 60; m.setTarget(p); r.horns++;
        }
    }
    public void boundary(Mob m) {
        if (!EcologyState.known(m)) return;
        var r = EcologyState.runtime(m);
        if (m.getType() == EntityTypes.WANDERING_TRADER && !enabled(m, FeatureId.TRADER_SELF_DEFENSE)) r.attacker = null;
        if (r.hornTarget != null && (m.getTarget() != r.hornTarget || !enabled(m, FeatureId.GOAT_HORN_AGGRO) || !livingValid(r.hornTarget) || r.hornTarget.level() != m.level() || !(r.hornTarget instanceof Player p) || !p.isUsingItem() || !p.getUseItem().has(DataComponents.INSTRUMENT) || m.level().getGameTime() >= r.hornUntil || m.distanceToSqr(r.hornTarget) > 576)) {
            if (m.getTarget() == r.hornTarget) m.setTarget(null); r.hornTarget = null;
        }
        if (m instanceof OwnableEntity pet && tamed(m) && m.tickCount % 20 == 0 && enabled(m, FeatureId.TAMEABLE_RECOVERY) && !m.getType().builtInRegistryHolder().is(NO_RECOVERY)
            && pet.getOwner() != null && pet.getOwner().isAlive() && pet.getOwner().level() == m.level() && !(pet.getOwner() instanceof Player p && p.isSpectator())
            && m.getHealth() < m.getMaxHealth() && m.getTarget() == null && !m.isOnFire() && !m.isPassenger() && !m.isVehicle() && EcologyState.ready(m, "recovery_after") && EcologyState.ready(m, "heal_after")) {
            m.heal(1); EcologyState.cooldown(m, "heal_after", 100); r.heals++;
        }
        if (!m.isAlive() || m.isNoAi()) clear(m);
    }
    private static void clear(Mob m) {
        var r = EcologyState.runtime(m); r.panicUntil = 0; r.threat = null; r.attacker = null;
        if (m.getTarget() == r.hornTarget && r.hornTarget != null) m.setTarget(null); r.hornTarget = null;
    }
}
