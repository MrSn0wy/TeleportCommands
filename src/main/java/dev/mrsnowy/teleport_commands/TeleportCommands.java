package dev.mrsnowy.teleport_commands;

import dev.mrsnowy.teleport_commands.storage.StorageManager;
import dev.mrsnowy.teleport_commands.utils.commands;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Objects;

import static dev.mrsnowy.teleport_commands.utils.tools.DeathLocationUpdater;

public class TeleportCommands implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final String MOD_ID = "teleport_commands";
    public static final Logger LOGGER = LoggerFactory.getLogger("teleport_commands");

	public static Path SAVE_DIR;
	public static Path CONFIG_DIR;
	public static StorageManager Storage;
	public static MinecraftServer Server;

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		ServerWorldEvents.LOAD.register(this::onWorldLoad);

		ServerEntityEvents.ENTITY_UNLOAD.register(this::onPlayerUnload);

		// todo: /back  /tpa /tpahere  /home /homes /sethome /delhome /renamehome /defaulthome   /spawn  /worldspawn
		commands.registerCommands();

	}



	private void onPlayerUnload(Entity entity, ServerWorld world) {
		if (entity instanceof ServerPlayerEntity player) {
//			LOGGER.info(String.valueOf(entity.getRemovalReason()));
            if (player.getRemovalReason() != null && Objects.equals(player.getRemovalReason().toString(), "KILLED") || Objects.equals(player.getRemovalReason().toString(), "DISCARDED")) {
				try {
					// /back command position
					LOGGER.info(player.getPos().toString());
					DeathLocationUpdater(player.getPos(), player.getServerWorld(), player.getUuidAsString());
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	private void onWorldLoad(MinecraftServer minecraftServer, ServerWorld serverWorld) {
		// make it run only once
		if (serverWorld.getRegistryKey() == World.OVERWORLD) {
			// initialize da variables
			SAVE_DIR = Path.of(String.valueOf(minecraftServer.getSavePath(WorldSavePath.ROOT)));
			CONFIG_DIR = FabricLoader.getInstance().getConfigDir();

			LOGGER.error(String.valueOf(SAVE_DIR));
			LOGGER.error(String.valueOf(CONFIG_DIR));

			Server = minecraftServer;
			StorageManager.StorageInit();
		}
	}
}