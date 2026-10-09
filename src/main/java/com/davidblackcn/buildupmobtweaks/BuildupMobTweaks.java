package com.davidblackcn.buildupmobtweaks;

import com.davidblackcn.buildupmobtweaks.command.DiagnosticCommands;
import com.davidblackcn.buildupmobtweaks.command.EquipmentCommands;
import com.davidblackcn.buildupmobtweaks.equipment.EquipmentPools;
import com.davidblackcn.buildupmobtweaks.config.BuildupConfig;
import com.davidblackcn.buildupmobtweaks.feature.FeatureRegistry;
import me.fzzyhmstrs.fzzy_config.api.ConfigApiJava;
import me.fzzyhmstrs.fzzy_config.api.RegisterType;
import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BuildupMobTweaks implements ModInitializer {
	public static final String MOD_ID = "buildupmobtweaks";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static BuildupConfig config;
    public static BuildupConfig config() { return config; }

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		config = ConfigApiJava.registerAndLoadConfig(BuildupConfig::new, RegisterType.BOTH);
		FeatureRegistry features = new FeatureRegistry(config);
        com.davidblackcn.buildupmobtweaks.rebuild.raid.RaidState.bootstrap();
        com.davidblackcn.buildupmobtweaks.rebuild.remaining.RemainingState.bootstrap();
		DiagnosticCommands.register(features);
        // R2-A: legacy S2 services stay unregistered until individually rebuilt.
        EquipmentPools.register();
        EquipmentCommands.register();
        new com.davidblackcn.buildupmobtweaks.rebuild.pillager.PillagerBehavior(features).register();
        new com.davidblackcn.buildupmobtweaks.rebuild.skeleton.SkeletonBehavior(features).register();
        new com.davidblackcn.buildupmobtweaks.rebuild.zombie.ZombieBehavior(features).register();
        new com.davidblackcn.buildupmobtweaks.rebuild.drowned.DrownedBehavior(features).register();
        new com.davidblackcn.buildupmobtweaks.rebuild.witch.WitchBehavior(features).register();
        new com.davidblackcn.buildupmobtweaks.rebuild.evoker.EvokerBehavior(features).register();
        new com.davidblackcn.buildupmobtweaks.rebuild.vex.VexBehavior(features).register();
        new com.davidblackcn.buildupmobtweaks.rebuild.remaining.RemainingBehavior(features).register();
        LOGGER.info("Buildup Mob Tweaks initialized: R3/R4-A/B/C/D behavior rebuild.");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
