package dev.mrsnowy.teleport_commands;

import net.fabricmc.api.ModInitializer;

public class fabricInit implements ModInitializer {

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		TeleportCommands.MOD_LOADER = "Fabric";
	}
}