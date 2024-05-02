package dev.mrsnowy.teleport_commands;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;

public class fabricInit implements ModInitializer {

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		TeleportCommands.LOGGER.info("Teleport Commands loaded! Hello Fabric!");

		// initialize the mod
		ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
			TeleportCommands.initializeMod(server, "Fabric");
		});


		// check if it is a player and check if the player died
		ServerEntityEvents.ENTITY_UNLOAD.register((entity,  world) -> {
			if (entity instanceof ServerPlayer player) {
				if (player.getRemovalReason() != null && (Objects.equals(player.getRemovalReason().toString(), "KILLED") || Objects.equals(player.getRemovalReason().toString(), "DISCARDED"))) {
					TeleportCommands.onPlayerDeath(player);
				}
			}
		});
	}
}