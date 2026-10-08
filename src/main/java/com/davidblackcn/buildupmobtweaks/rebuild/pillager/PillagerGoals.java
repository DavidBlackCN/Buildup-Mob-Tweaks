/* Mob AI Tweaks behavioral reference: Copyright (c) 2024 N0t_UN_Owen, MIT.
 * Buildup safe inventory/Goal adaptations, MIT. See NOTICE.md. */
package com.davidblackcn.buildupmobtweaks.rebuild.pillager;

import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import java.util.EnumSet;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.monster.illager.Pillager;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;

public final class PillagerGoals {
    private PillagerGoals() {}
    static final class Melee extends MeleeAttackGoal {
        private final Pillager pillager;
        private final PillagerBehavior behavior;
        Melee(Pillager mob, PillagerBehavior behavior) { super(mob, 1, false); this.pillager = mob; this.behavior = behavior; }
        @Override public boolean canUse() { return behavior.melee(pillager) && PillagerBehavior.validTarget(pillager, pillager.getTarget()) && super.canUse(); }
        @Override public boolean canContinueToUse() { return behavior.melee(pillager) && PillagerBehavior.validTarget(pillager, pillager.getTarget()) && super.canContinueToUse(); }
        @Override public void start() {
            super.start();
            var damage = pillager.getAttribute(Attributes.ATTACK_DAMAGE);
            var id = BuildupMobTweaks.id("pillager_melee_balance");
            // Upstream changes vanilla base damage 5 -> 1. Scope the -4 to our active melee Goal.
            if (damage != null && !damage.hasModifier(id)) damage.addTransientModifier(new AttributeModifier(id, -4, AttributeModifier.Operation.ADD_VALUE));
        }
        @Override public void stop() {
            super.stop();
            var damage = pillager.getAttribute(Attributes.ATTACK_DAMAGE);
            if (damage != null) damage.removeModifier(BuildupMobTweaks.id("pillager_melee_balance"));
        }
    }
    static final class Switch extends Goal {
        private final Pillager mob;
        private final PillagerBehavior behavior;
        Switch(Pillager mob, PillagerBehavior behavior) { this.mob = mob; this.behavior = behavior; }
        @Override public boolean canUse() { return behavior.enabled(mob, FeatureId.PILLAGER_WEAPON_SWITCH) && PillagerBehavior.validTarget(mob, mob.getTarget()); }
        @Override public boolean requiresUpdateEveryTick() { return true; }
        @Override public void tick() { behavior.switchWeapon(mob); }
    }
    static final class Eat extends Goal {
        private final Pillager mob;
        private final PillagerBehavior behavior;
        private int wait;
        private boolean started;
        Eat(Pillager mob, PillagerBehavior behavior) { this.mob = mob; this.behavior = behavior; setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK)); }
        private boolean allowed() { return behavior.enabled(mob, FeatureId.PILLAGER_FOOD_HEAL) && mob.getTarget() == null && mob.isAlive() && mob.getHealth() < mob.getMaxHealth(); }
        @Override public boolean canUse() { return allowed() && !mob.isUsingItem() && PillagerBehavior.find(mob, PillagerBehavior::food) >= 0; }
        @Override public boolean canContinueToUse() { return allowed() && (!started || behavior.mealSlot(mob) >= 0 && mob.isUsingItem()); }
        @Override public boolean requiresUpdateEveryTick() { return true; }
        @Override public void start() { wait = 0; started = false; mob.getNavigation().stop(); }
        @Override public void tick() {
            mob.getNavigation().stop();
            if (!started && ++wait >= 60) { started = true; behavior.beginMeal(mob); }
        }
        @Override public void stop() { behavior.restoreMeal(mob); wait = 0; started = false; }
    }
}
