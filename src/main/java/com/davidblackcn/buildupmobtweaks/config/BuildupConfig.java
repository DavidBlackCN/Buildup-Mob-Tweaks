package com.davidblackcn.buildupmobtweaks.config;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import me.fzzyhmstrs.fzzy_config.annotations.Version;
import me.fzzyhmstrs.fzzy_config.annotations.Comment;
import me.fzzyhmstrs.fzzy_config.config.Config;
import me.fzzyhmstrs.fzzy_config.config.ConfigSection;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedBoolean;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedInt;

@Version(version = 13)
public final class BuildupConfig extends Config {
    public General general = new General();
    public Villagers villagers = new Villagers();
    // Reserved sections keep the SPEC layout without inventing gameplay switches.
    public Hostile hostile = new Hostile();
    public ConfigSection neutral = new ConfigSection();
    public Passive passive = new Passive();
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

    public static final class Passive extends ConfigSection {
        public ValidatedBoolean animal_sneak_approach = new ValidatedBoolean(true);
        public ValidatedBoolean snow_golem_pumpkin_return = new ValidatedBoolean(true);
        public ValidatedBoolean snow_golem_pumpkin_defense = new ValidatedBoolean(true);
        public ValidatedBoolean snow_golem_ranged_behavior = new ValidatedBoolean(true);
        public ValidatedBoolean snow_golem_throw_visual = new ValidatedBoolean(true);

        public ValidatedBoolean squid_ink_defense = new ValidatedBoolean(true);
        public ValidatedBoolean glow_squid_gaze = new ValidatedBoolean(true);
        public ValidatedBoolean chicken_threat_escape = new ValidatedBoolean(true);
        public ValidatedBoolean animal_group_panic = new ValidatedBoolean(true);
        public ValidatedBoolean tameable_recovery = new ValidatedBoolean(true);
        public ValidatedBoolean cat_projectile_dodge = new ValidatedBoolean(true);
        public ValidatedBoolean goat_horn_aggro = new ValidatedBoolean(true);
        public ValidatedBoolean trader_self_defense = new ValidatedBoolean(true);
        public ValidatedBoolean sniffer_search = new ValidatedBoolean(true);
    }

    public static final class Villagers extends ConfigSection {
        @Comment("Experimental Villager Core; enable for review. Native Brain, professions and trades remain authoritative.")
        public ValidatedBoolean enabled = new ValidatedBoolean(false);
        public Farmer farmer = new Farmer();
        public Professions professions = new Professions();
        public ValidatedBoolean poi_checks = new ValidatedBoolean(true);
        public ValidatedBoolean navigation_safety = new ValidatedBoolean(true);
        public ValidatedBoolean schedule_coordination = new ValidatedBoolean(true);
        public ValidatedInt poi_interval = new ValidatedInt(100, 1200, 20);
        public ValidatedInt navigation_interval = new ValidatedInt(10, 40, 5);
        public ValidatedInt stuck_ticks = new ValidatedInt(80, 600, 40);
        public ValidatedInt retry_ticks = new ValidatedInt(100, 1200, 20);
        public ValidatedInt max_retries = new ValidatedInt(2, 5, 1);
    }

    public static final class Professions extends ConfigSection {
        public ValidatedBoolean enabled = new ValidatedBoolean(false);
        public ValidatedBoolean external_control = new ValidatedBoolean(false);
        public ValidatedBoolean protection_required = new ValidatedBoolean(false);
        public ValidatedBoolean self_heal = new ValidatedBoolean(true);
        public ProfessionSwitches profession_switches = new ProfessionSwitches();
        public ValidatedBoolean daily_routines = new ValidatedBoolean(true);
        public ValidatedBoolean fishing = new ValidatedBoolean(true);
        public ValidatedBoolean shearing = new ValidatedBoolean(true);
        public ValidatedBoolean cleric_healing = new ValidatedBoolean(true);
        public ValidatedBoolean golem_repair = new ValidatedBoolean(true);
        public ValidatedBoolean baby_feeding = new ValidatedBoolean(false);
        public ValidatedBoolean wool_pickup = new ValidatedBoolean(false);
        public ValidatedBoolean archery_training = new ValidatedBoolean(false);
        public ValidatedBoolean decoration_frames = new ValidatedBoolean(false);
        public ValidatedBoolean decoration_stands = new ValidatedBoolean(false);
        public ValidatedBoolean decoration_blocks = new ValidatedBoolean(false);
        public ValidatedInt scan_interval = new ValidatedInt(60,1200,20);
        public ValidatedInt action_cooldown = new ValidatedInt(200,2400,20);
        public ValidatedInt world_scan_budget = new ValidatedInt(512,4096,64);
    }

    public static final class ProfessionSwitches extends ConfigSection {
        public ValidatedBoolean armorer = new ValidatedBoolean(true);
        public ValidatedBoolean butcher = new ValidatedBoolean(true);
        public ValidatedBoolean cartographer = new ValidatedBoolean(true);
        public ValidatedBoolean cleric = new ValidatedBoolean(true);
        public ValidatedBoolean farmer = new ValidatedBoolean(true);
        public ValidatedBoolean fisherman = new ValidatedBoolean(true);
        public ValidatedBoolean fletcher = new ValidatedBoolean(true);
        public ValidatedBoolean leatherworker = new ValidatedBoolean(true);
        public ValidatedBoolean librarian = new ValidatedBoolean(true);
        public ValidatedBoolean mason = new ValidatedBoolean(true);
        public ValidatedBoolean shepherd = new ValidatedBoolean(true);
        public ValidatedBoolean toolsmith = new ValidatedBoolean(true);
        public ValidatedBoolean weaponsmith = new ValidatedBoolean(true);
    }

    public static final class Farmer extends ConfigSection {
        public ValidatedBoolean enabled = new ValidatedBoolean(false);
        @Comment("Select external farming control; both Buildup and vanilla harvesting/bonemeal yield. No external AI is disabled.")
        public ValidatedBoolean external_control = new ValidatedBoolean(false);
        @Comment("Require a registered protection adapter. Without one, harvest and replant are paused.")
        public ValidatedBoolean protection_required = new ValidatedBoolean(false);
        public ValidatedInt radius = new ValidatedInt(8, 16, 2);
        public ValidatedInt scan_interval = new ValidatedInt(60, 1200, 20);
        public ValidatedInt scan_budget = new ValidatedInt(64, 256, 16);
        public ValidatedInt world_scan_budget = new ValidatedInt(512, 4096, 64);
        public ValidatedInt action_interval = new ValidatedInt(40, 600, 20);
        public ValidatedInt target_timeout = new ValidatedInt(180, 600, 60);
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
        @Comment("Deferred outside Phase 1; retained for old configuration compatibility. This value has no effect.")
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
        public ValidatedBoolean skeleton_staggered_draw = new ValidatedBoolean(true);
        public ValidatedBoolean skeleton_baby_variant = new ValidatedBoolean(true);
        public ValidatedInt skeleton_baby_chance = new ValidatedInt(100, 1000, 0);
        public ValidatedBoolean breeze_shotgun = new ValidatedBoolean(true);
        @Comment("Inactive legacy alias; use the implemented native/rebuilt feature. This value has no effect.")
        public ValidatedBoolean skeleton_aim_fix = new ValidatedBoolean(true);
        public ValidatedBoolean golem_friendly_fire_fix = new ValidatedBoolean(true);
        public ValidatedBoolean zombified_piglin_anger_fix = new ValidatedBoolean(true);
        @Comment("Inactive legacy alias; use the implemented native/rebuilt feature. This value has no effect.")
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
        @Comment("Deferred outside Phase 1; retained for old configuration compatibility. This value has no effect.")
        public ValidatedBoolean undead_horse_sunburn = new ValidatedBoolean(true);
        public ValidatedBoolean hostile_escape_seat = new ValidatedBoolean(true);
        @Comment("Deferred outside Phase 1; retained for old configuration compatibility. This value has no effect.")
        public ValidatedBoolean hostile_spawn_effect = new ValidatedBoolean(true);
        public ValidatedBoolean spider_hunts_pests = new ValidatedBoolean(true);
        public ValidatedBoolean creeper_fire_visual = new ValidatedBoolean(true);
        public ValidatedBoolean creeper_hit_delay = new ValidatedBoolean(true);
        public ValidatedBoolean creeper_embedded_arrows = new ValidatedBoolean(true);
        public ValidatedInt skeleton_horse_charge_chance = new ValidatedInt(10, 1000, 0);
        public ValidatedInt chicken_jockey_charge_chance = new ValidatedInt(10, 1000, 0);
        public ValidatedInt zombie_slime_carrier_chance = new ValidatedInt(10, 1000, 0);
        public ValidatedInt zombie_horse_leader_chance = new ValidatedInt(10, 1000, 0);
        @Comment("Deferred outside Phase 1; retained for old configuration compatibility. This value has no effect.")
        public ValidatedBoolean illusioner_omen_spawn = new ValidatedBoolean(true);
        public ValidatedBoolean illager_zombie_conflict = new ValidatedBoolean(true);
        public ValidatedBoolean illager_zombie_villager = new ValidatedBoolean(true);
        @Comment("Deferred outside Phase 1; retained for old configuration compatibility. This value has no effect.")
        public ValidatedBoolean illager_boats = new ValidatedBoolean(true);
        @Comment("Inactive legacy alias; use the implemented native/rebuilt feature. This value has no effect.")
        public ValidatedBoolean husk_spear_charge = new ValidatedBoolean(true);
        public ValidatedBoolean husk_camel_circle = new ValidatedBoolean(true);
        public ValidatedBoolean skeleton_horse_charge = new ValidatedBoolean(true);
        public ValidatedBoolean chicken_jockey_charge = new ValidatedBoolean(true);
        @Comment("Deferred outside Phase 1; retained for old configuration compatibility. This value has no effect.")
        public ValidatedBoolean zombie_slime_carrier = new ValidatedBoolean(true);
        @Comment("Deferred outside Phase 1; retained for old configuration compatibility. This value has no effect.")
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
        @Comment("Deferred outside Phase 1; retained for old configuration compatibility. This value has no effect.")
        public ValidatedBoolean stray_omen_snow = new ValidatedBoolean(true);
        @Comment("Deferred outside Phase 1; retained for old configuration compatibility. This value has no effect.")
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
        @Comment("Deferred outside Phase 1; retained for old configuration compatibility. This value has no effect.")
        public ValidatedBoolean hostile_rest = new ValidatedBoolean(true);
        @Comment("Deferred outside Phase 1; retained for old configuration compatibility. This value has no effect.")
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
        @Comment("Deferred outside Phase 1; retained for old configuration compatibility. This value has no effect.")
        public ValidatedBoolean sense_darkness = new ValidatedBoolean(true);
        public ValidatedBoolean sense_nausea = new ValidatedBoolean(true);
        public ValidatedBoolean sense_invisibility = new ValidatedBoolean(true);
        public ValidatedBoolean sense_crawling = new ValidatedBoolean(true);
        public ValidatedBoolean sense_crouching = new ValidatedBoolean(true);
        public ValidatedBoolean melee_effect_interval = new ValidatedBoolean(true);
        public ValidatedBoolean melee_high_target_jump = new ValidatedBoolean(true);
        public ValidatedBoolean ranged_reposition = new ValidatedBoolean(true);
        @Comment("Deferred outside Phase 1; retained for old configuration compatibility. This value has no effect.")
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
        public ValidatedInt witherBowChance = new ValidatedInt(10, 1000, 0);
        public ValidatedInt zombifiedCrossbowChance = new ValidatedInt(10, 1000, 0);
    }

    public static final class Traits extends ConfigSection {
        public ValidatedBoolean enabled = new ValidatedBoolean(true);
        @Comment("Opt-in unified selection for NEW individuals. Default false preserves Phase 1. Existing individuals keep their recorded mode. / 仅新个体采用统一抽取，默认保留 Phase 1 行为。")
        public ValidatedBoolean unified_selection = new ValidatedBoolean(false);
        public Selection selection = new Selection();
        @Comment("Inactive legacy diagnostic marker; no gameplay or probability effect in this rebuild.")
        public ValidatedBoolean commonMarker = new ValidatedBoolean(true);
        @Comment("Inactive legacy diagnostic marker; no gameplay or probability effect in this rebuild.")
        public ValidatedBoolean advancedMarker = new ValidatedBoolean(true);
        @Comment("Inactive legacy diagnostic marker; no gameplay or probability effect in this rebuild.")
        public ValidatedBoolean rareMarker = new ValidatedBoolean(true);
        public Chances cow = new Chances(200, 70, 10);
        public Chances zombieFamily = new Chances(100, 30, 5);
    }

    public static final class Selection extends ConfigSection {
        @Comment("-1 uses the effective old door chance; does not assign 50%. / -1 沿用有效旧门盾概率。")
        public ValidatedInt zombie_chance_per_mille = new ValidatedInt(-1, 1000, -1);
        @Comment("-1 leaves the new Husk pool unconfigured. Its old burrowChance was inactive. / -1 表示新尸壳池未配置。")
        public ValidatedInt husk_chance_per_mille = new ValidatedInt(-1, 1000, -1);
        @Comment("-1 leaves the new Bogged pool unconfigured. Its old boggedChance was inactive. / -1 表示新沼骸池未配置。")
        public ValidatedInt bogged_chance_per_mille = new ValidatedInt(-1, 1000, -1);
        public ValidatedInt enderman_chance_per_mille = new ValidatedInt(-1, 1000, -1);
        public ValidatedInt evoker_chance_per_mille = new ValidatedInt(-1, 1000, -1);
        public ValidatedInt wither_chance_per_mille = new ValidatedInt(-1, 1000, -1);
        public ValidatedInt piglin_chance_per_mille = new ValidatedInt(-1, 1000, -1);
        public ValidatedInt door_weight = new ValidatedInt(1, 10000, 0);
        @Comment("0 excludes this candidate until a new pool is explicitly configured; not a published default weight. / 0 暂不纳入新池。")
        public ValidatedInt burrow_weight = new ValidatedInt(0, 10000, 0);
        public ValidatedInt bogged_weight = new ValidatedInt(0, 10000, 0);
        public ValidatedInt combo_weight = new ValidatedInt(1, 10000, 0);
        public ValidatedInt fireball_weight = new ValidatedInt(1, 10000, 0);
        public ValidatedInt skull_weight = new ValidatedInt(1, 10000, 0);
        public ValidatedInt dodge_weight = new ValidatedInt(1, 10000, 0);
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
