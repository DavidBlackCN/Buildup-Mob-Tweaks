package com.davidblackcn.buildupmobtweaks;

import com.davidblackcn.buildupmobtweaks.combat.SkeletonCombat;
import com.davidblackcn.buildupmobtweaks.combat.ZombieCombat;
import com.davidblackcn.buildupmobtweaks.combat.RaidCombat;
import com.davidblackcn.buildupmobtweaks.combat.VexCombat;
import com.davidblackcn.buildupmobtweaks.combat.DrownedTridents;
import com.davidblackcn.buildupmobtweaks.command.ZombieCommands;
import com.davidblackcn.buildupmobtweaks.command.RaidCommands;
import com.davidblackcn.buildupmobtweaks.command.VexCommands;
import com.davidblackcn.buildupmobtweaks.command.SkeletonCommands;
import com.davidblackcn.buildupmobtweaks.command.DiagnosticCommands;
import com.davidblackcn.buildupmobtweaks.command.TraitCommands;
import com.davidblackcn.buildupmobtweaks.command.EquipmentCommands;
import com.davidblackcn.buildupmobtweaks.equipment.EquipmentPools;
import com.davidblackcn.buildupmobtweaks.equipment.EquipmentService;
import com.davidblackcn.buildupmobtweaks.traits.TraitService;
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

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		BuildupConfig config = ConfigApiJava.registerAndLoadConfig(BuildupConfig::new, RegisterType.BOTH);
		FeatureRegistry features = new FeatureRegistry(config);
		DiagnosticCommands.register(features);
		TraitService traits = new TraitService(features);
		traits.register();
		TraitCommands.register(traits);
        EquipmentPools.register();
        new EquipmentService(features).register();
        EquipmentCommands.register();
        new SkeletonCombat(features).register();
        SkeletonCommands.register(features);
        new ZombieCombat(features).register();
        new DrownedTridents(features).register();
        ZombieCommands.register(features);
        new VexCombat(features).register();
        VexCommands.register(features);
        new RaidCombat(features).register();
        RaidCommands.register(features);
		LOGGER.info("Buildup Mob Tweaks configuration initialized (S2-C2).");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
