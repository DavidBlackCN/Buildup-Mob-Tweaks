package com.davidblackcn.buildupmobtweaks.config;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import me.fzzyhmstrs.fzzy_config.annotations.Version;
import me.fzzyhmstrs.fzzy_config.config.Config;
import me.fzzyhmstrs.fzzy_config.config.ConfigSection;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedBoolean;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedInt;

@Version(version = 1)
public final class BuildupConfig extends Config {
    public General general = new General();
    // Reserved sections keep the SPEC layout without inventing gameplay switches.
    public ConfigSection hostile = new ConfigSection();
    public ConfigSection neutral = new ConfigSection();
    public ConfigSection passive = new ConfigSection();
    public ConfigSection bosses = new ConfigSection();
    public ConfigSection equipment = new ConfigSection();
    public ConfigSection traits = new ConfigSection();
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

    public static final class Performance extends ConfigSection {
        // Fzzy's constructor order is default, maximum, minimum.
        public ValidatedInt diagnosticLines = new ValidatedInt(1, 5, 1);
    }
}
