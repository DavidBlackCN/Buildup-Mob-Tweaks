package com.davidblackcn.buildupmobtweaks.config;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import me.fzzyhmstrs.fzzy_config.annotations.Version;
import me.fzzyhmstrs.fzzy_config.config.Config;
import me.fzzyhmstrs.fzzy_config.config.ConfigSection;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedBoolean;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedInt;

@Version(version = 5)
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
