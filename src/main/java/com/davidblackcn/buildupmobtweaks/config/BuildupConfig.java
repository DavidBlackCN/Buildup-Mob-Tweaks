package com.davidblackcn.buildupmobtweaks.config;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import me.fzzyhmstrs.fzzy_config.annotations.Version;
import me.fzzyhmstrs.fzzy_config.config.Config;
import me.fzzyhmstrs.fzzy_config.config.ConfigSection;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedBoolean;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedInt;

@Version(version = 9)
public final class BuildupConfig extends Config {
    public General general = new General();
    // Reserved sections keep the SPEC layout without inventing gameplay switches.
    public Hostile hostile = new Hostile();
    public ConfigSection neutral = new ConfigSection();
    public ConfigSection passive = new ConfigSection();
    public ConfigSection bosses = new ConfigSection();
    public Equipment equipment = new Equipment();
    public Traits traits = new Traits();
    public ConfigSection fixes = new ConfigSection();
    public ConfigSection compatibility = new ConfigSection();
    public Performance performance = new Performance();

    public BuildupConfig() {
        super(BuildupMobTweaks.id("main"));
    }

    @Override
    public int defaultPermLevel() {
        return 4;
    }

    public static final class General extends ConfigSection {
        public ValidatedBoolean enabled = new ValidatedBoolean(true);
        public ValidatedBoolean diagnosticProbe = new ValidatedBoolean(true);
    }

    public static final class Hostile extends ConfigSection {
        public Skeleton skeleton = new Skeleton();
        public Zombie zombie = new Zombie();
        public Drowned drowned = new Drowned();
        public Raid raid = new Raid();
        public Vex vex = new Vex();
        public Extended extended = new Extended();
    }

    public static final class Skeleton extends ConfigSection {
        public ValidatedBoolean safeStrafing = new ValidatedBoolean(true);
        public ValidatedBoolean targetValidation = new ValidatedBoolean(true);
        public ValidatedBoolean weaponSwitching = new ValidatedBoolean(true);
        public ValidatedBoolean bowCompatibility = new ValidatedBoolean(true);
        public ValidatedBoolean skeletonSniping = new ValidatedBoolean(true);
        public ValidatedBoolean strayJumpShot = new ValidatedBoolean(true);
        public ValidatedBoolean boggedSporeRetreat = new ValidatedBoolean(true);
        public ValidatedInt skeletonChance = new ValidatedInt(70, 1000, 0);
        public ValidatedInt strayChance = new ValidatedInt(70, 1000, 0);
        public ValidatedInt boggedChance = new ValidatedInt(10, 1000, 0);
    }

    public static final class Zombie extends ConfigSection {
        public ValidatedBoolean shieldUse = new ValidatedBoolean(true);
        public ValidatedBoolean doorGuard = new ValidatedBoolean(true);
        public ValidatedBoolean activeGuard = new ValidatedBoolean(true);
        public ValidatedBoolean sandBurrow = new ValidatedBoolean(true);
        public ValidatedBoolean babyRider = new ValidatedBoolean(true);
        public ValidatedInt doorChance = new ValidatedInt(30, 1000, 0);
        public ValidatedInt guardChance = new ValidatedInt(70, 1000, 0);
        public ValidatedInt burrowChance = new ValidatedInt(30, 1000, 0);
        public ValidatedInt riderChance = new ValidatedInt(10, 1000, 0);
        public ValidatedInt guardCooldown = new ValidatedInt(100, 2400, 40);
        public ValidatedInt doorCooldown = new ValidatedInt(60, 2400, 20);
        public ValidatedInt burrowCooldown = new ValidatedInt(200, 2400, 60);
        public ValidatedInt riderCooldown = new ValidatedInt(200, 2400, 40);
    }
    public static final class Drowned extends ConfigSection {
        public ValidatedBoolean tridentConservation = new ValidatedBoolean(true);
        public ValidatedBoolean tridentRecovery = new ValidatedBoolean(true);
        public ValidatedBoolean tridentPlayerPickup = new ValidatedBoolean(true);
        public ValidatedInt recoveryTimeout = new ValidatedInt(200, 1200, 40);
    }

    public static final class Raid extends ConfigSection {
        public ValidatedBoolean pillagerTargetLifecycle = new ValidatedBoolean(true);
        public ValidatedBoolean pillagerSpawnSupplies = new ValidatedBoolean(true);
        public ValidatedBoolean pillagerRetreat = new ValidatedBoolean(true);
        public ValidatedBoolean pillagerWeaponSwitch = new ValidatedBoolean(true);
        public ValidatedBoolean vindicatorSupport = new ValidatedBoolean(true);
        public ValidatedBoolean evokerVexLimit = new ValidatedBoolean(true);
        public ValidatedBoolean evokerSummonCooldown = new ValidatedBoolean(true);
        public ValidatedBoolean witchWindup = new ValidatedBoolean(true);
        public ValidatedBoolean witchThrowCooldown = new ValidatedBoolean(true);
        public ValidatedInt vexLimit = new ValidatedInt(6, 24, 3);
        public ValidatedInt summonCooldown = new ValidatedInt(680, 2400, 340);
        public ValidatedInt witchWindupTicks = new ValidatedInt(20, 60, 10);
        public ValidatedInt witchCooldownTicks = new ValidatedInt(100, 400, 60);
    }

    public static final class Vex extends ConfigSection {
        public ValidatedBoolean fixedCharge = new ValidatedBoolean(true);
        public ValidatedBoolean recoveryPause = new ValidatedBoolean(true);
        public ValidatedBoolean closeRangeGuard = new ValidatedBoolean(true);
        public ValidatedInt recoveryTicks = new ValidatedInt(20, 60, 10);
        public ValidatedInt minimumChargeDistance = new ValidatedInt(3, 6, 2);
    }

    public static final class Extended extends ConfigSection {
        public ValidatedBoolean skeleton_aim_fix = new ValidatedBoolean(true);
        public ValidatedBoolean golem_friendly_fire_fix = new ValidatedBoolean(true);
        public ValidatedBoolean zombified_piglin_anger_fix = new ValidatedBoolean(true);
        public ValidatedBoolean strafe_hazard_check = new ValidatedBoolean(true);
        public ValidatedBoolean movement_jump_input = new ValidatedBoolean(true);
        public ValidatedBoolean movement_clear_strafe = new ValidatedBoolean(true);
        public ValidatedBoolean strafe_obstacle_jump = new ValidatedBoolean(true);
        public ValidatedBoolean piglin_shot_swing = new ValidatedBoolean(true);
        public ValidatedBoolean piglin_item_dodge = new ValidatedBoolean(true);
        public ValidatedBoolean skeleton_offhand_visual = new ValidatedBoolean(true);
        public ValidatedBoolean drowned_swim_visual = new ValidatedBoolean(true);
        public ValidatedBoolean burning_projectile_visual = new ValidatedBoolean(true);
        public ValidatedBoolean burn_freeze_visual = new ValidatedBoolean(true);
        public ValidatedBoolean undead_horse_sunburn = new ValidatedBoolean(true);
        public ValidatedBoolean hostile_escape_seat = new ValidatedBoolean(true);
        public ValidatedBoolean hostile_spawn_effect = new ValidatedBoolean(true);
        public ValidatedBoolean spider_hunts_pests = new ValidatedBoolean(true);
        public ValidatedBoolean creeper_fire_visual = new ValidatedBoolean(true);
        public ValidatedBoolean creeper_hit_delay = new ValidatedBoolean(true);
        public ValidatedBoolean creeper_embedded_arrows = new ValidatedBoolean(true);
        public ValidatedInt skeleton_horse_charge_chance = new ValidatedInt(10, 1000, 0);
        public ValidatedInt chicken_jockey_charge_chance = new ValidatedInt(10, 1000, 0);
        public ValidatedInt zombie_slime_carrier_chance = new ValidatedInt(10, 1000, 0);
        public ValidatedInt zombie_horse_leader_chance = new ValidatedInt(10, 1000, 0);
        public ValidatedBoolean illusioner_omen_spawn = new ValidatedBoolean(true);
        public ValidatedBoolean illager_zombie_conflict = new ValidatedBoolean(true);
        public ValidatedBoolean illager_zombie_villager = new ValidatedBoolean(true);
        public ValidatedBoolean illager_boats = new ValidatedBoolean(true);
        public ValidatedBoolean husk_spear_charge = new ValidatedBoolean(true);
        public ValidatedBoolean husk_camel_circle = new ValidatedBoolean(true);
        public ValidatedBoolean skeleton_horse_charge = new ValidatedBoolean(true);
        public ValidatedBoolean chicken_jockey_charge = new ValidatedBoolean(true);
        public ValidatedBoolean zombie_slime_carrier = new ValidatedBoolean(true);
        public ValidatedBoolean zombie_horse_leader = new ValidatedBoolean(true);
        public ValidatedBoolean piglin_crossbow_compatibility = new ValidatedBoolean(true);
        public ValidatedBoolean pillager_food_heal = new ValidatedBoolean(true);
        public ValidatedBoolean pillager_shield_break = new ValidatedBoolean(true);
        public ValidatedBoolean drowned_fishing_pull = new ValidatedBoolean(true);
        public ValidatedBoolean zombified_piglin_spawn_crossbow = new ValidatedBoolean(true);
        public ValidatedBoolean zombified_piglin_crossbow = new ValidatedBoolean(true);
        public ValidatedBoolean zombie_fisherman_rod = new ValidatedBoolean(true);
        public ValidatedBoolean zombie_cleric_potions = new ValidatedBoolean(true);
        public ValidatedBoolean zombie_fletcher_crossbow = new ValidatedBoolean(true);
        public ValidatedInt wither_skeleton_skull_chance = new ValidatedInt(70, 1000, 0);
        public ValidatedInt parched_dodge_chance = new ValidatedInt(70, 1000, 0);
        public ValidatedInt stray_snow_barrage_chance = new ValidatedInt(70, 1000, 0);
        public ValidatedBoolean rider_friendly_fire = new ValidatedBoolean(true);
        public ValidatedBoolean skeleton_trap_variants = new ValidatedBoolean(true);
        public ValidatedBoolean skeleton_wither_conversion = new ValidatedBoolean(true);
        public ValidatedBoolean wither_skeleton_bow = new ValidatedBoolean(true);
        public ValidatedBoolean wither_skeleton_homing = new ValidatedBoolean(true);
        public ValidatedBoolean wither_skeleton_skull = new ValidatedBoolean(true);
        public ValidatedBoolean parched_dodge = new ValidatedBoolean(true);
        public ValidatedBoolean bogged_omen_mushrooms = new ValidatedBoolean(true);
        public ValidatedBoolean stray_omen_snow = new ValidatedBoolean(true);
        public ValidatedBoolean stray_snow_barrage = new ValidatedBoolean(true);
        public ValidatedBoolean skeleton_shelter = new ValidatedBoolean(true);
        public ValidatedInt evoker_fireball_chance = new ValidatedInt(70, 1000, 0);
        public ValidatedInt evoker_totem_chance = new ValidatedInt(10, 1000, 0);
        public ValidatedInt enderman_combo_chance = new ValidatedInt(70, 1000, 0);
        public ValidatedInt illusioner_clone_chance = new ValidatedInt(70, 1000, 0);
        public ValidatedInt special_cooldown = new ValidatedInt(400, 2400, 200);
        public ValidatedBoolean illusioner_swap = new ValidatedBoolean(true);
        public ValidatedBoolean illusioner_shoot_pause = new ValidatedBoolean(true);
        public ValidatedBoolean illusioner_clone_arrows = new ValidatedBoolean(true);
        public ValidatedBoolean enderman_combo_animation = new ValidatedBoolean(true);
        public ValidatedBoolean enderman_combo = new ValidatedBoolean(true);
        public ValidatedBoolean enderman_speed = new ValidatedBoolean(true);
        public ValidatedBoolean evoker_fireball_no_fire = new ValidatedBoolean(true);
        public ValidatedBoolean evoker_totem = new ValidatedBoolean(true);
        public ValidatedBoolean evoker_fireball = new ValidatedBoolean(true);
        public ValidatedBoolean hostile_rest = new ValidatedBoolean(true);
        public ValidatedBoolean rest_sound_wake = new ValidatedBoolean(true);
        public ValidatedBoolean witch_leaping_potion = new ValidatedBoolean(true);
        public ValidatedBoolean witch_jump_throw = new ValidatedBoolean(true);
        public ValidatedBoolean silverfish_burrow = new ValidatedBoolean(true);
        public ValidatedBoolean silverfish_call_pause = new ValidatedBoolean(true);
        public ValidatedBoolean silverfish_call_particles = new ValidatedBoolean(true);
        public ValidatedBoolean silverfish_shovel_weakness = new ValidatedBoolean(true);
        public ValidatedBoolean breeze_takeoff_burst = new ValidatedBoolean(true);
        public ValidatedBoolean breeze_landing_burst = new ValidatedBoolean(true);
        public ValidatedBoolean evoker_flee_speed = new ValidatedBoolean(true);
        public ValidatedBoolean evoker_avoid_target_fix = new ValidatedBoolean(true);
        public ValidatedBoolean sense_glowing = new ValidatedBoolean(true);
        public ValidatedBoolean sense_blindness = new ValidatedBoolean(true);
        public ValidatedBoolean sense_darkness = new ValidatedBoolean(true);
        public ValidatedBoolean sense_nausea = new ValidatedBoolean(true);
        public ValidatedBoolean sense_invisibility = new ValidatedBoolean(true);
        public ValidatedBoolean sense_crawling = new ValidatedBoolean(true);
        public ValidatedBoolean sense_crouching = new ValidatedBoolean(true);
        public ValidatedBoolean melee_effect_interval = new ValidatedBoolean(true);
        public ValidatedBoolean melee_high_target_jump = new ValidatedBoolean(true);
        public ValidatedBoolean ranged_reposition = new ValidatedBoolean(true);
        public ValidatedBoolean omen_pressure = new ValidatedBoolean(true);
        public ValidatedBoolean ghast_slow_fireball = new ValidatedBoolean(true);
        public ValidatedBoolean ghast_cooldown = new ValidatedBoolean(true);
        public ValidatedBoolean ghast_telegraph = new ValidatedBoolean(true);
        public ValidatedBoolean blaze_orbit = new ValidatedBoolean(true);
        public ValidatedBoolean blaze_difficulty_volley = new ValidatedBoolean(true);
        public ValidatedBoolean blaze_strafe = new ValidatedBoolean(true);
        public ValidatedBoolean vex_projectile_weakness = new ValidatedBoolean(true);
        public ValidatedBoolean pillager_crossbow_compatibility = new ValidatedBoolean(true);
        public ValidatedBoolean pillager_range = new ValidatedBoolean(true);
    }

    public static final class Equipment extends ConfigSection {
        public ValidatedBoolean poolAssignment = new ValidatedBoolean(true);
        public ValidatedInt assignmentChance = new ValidatedInt(100, 1000, 0);
    }

    public static final class Traits extends ConfigSection {
        public ValidatedBoolean enabled = new ValidatedBoolean(true);
        public ValidatedBoolean commonMarker = new ValidatedBoolean(true);
        public ValidatedBoolean advancedMarker = new ValidatedBoolean(true);
        public ValidatedBoolean rareMarker = new ValidatedBoolean(true);
        public Chances cow = new Chances(200, 70, 10);
        public Chances zombieFamily = new Chances(100, 30, 5);
    }

    public static final class Chances extends ConfigSection {
        public ValidatedInt common;
        public ValidatedInt advanced;
        public ValidatedInt rare;

        public Chances(int common, int advanced, int rare) {
            this.common = new ValidatedInt(common, 1000, 0);
            this.advanced = new ValidatedInt(advanced, 1000, 0);
            this.rare = new ValidatedInt(rare, 1000, 0);
        }
    }

    public static final class Performance extends ConfigSection {
        // Fzzy's constructor order is default, maximum, minimum.
        public ValidatedInt diagnosticLines = new ValidatedInt(1, 5, 1);
    }
}
