package dev.mrsnowy.teleport_commands;

import dev.mrsnowy.teleport_commands.storage.StorageManager;
import dev.mrsnowy.teleport_commands.commands.*;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static dev.mrsnowy.teleport_commands.utils.tools.DeathLocationUpdater;

public class TeleportCommands {

	public static final String MOD_ID = "teleport_commands";
	public static final String MOD_NAME = "Teleport Commands";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);
	public static String MOD_LOADER;
	public static Path SAVE_DIR;
	public static Path CONFIG_DIR;
	public static MinecraftServer Server;

	public static void initializeMod(MinecraftServer server, String ModLoader) {
		// initialize da variables

		MOD_LOADER = ModLoader;

		SAVE_DIR = Path.of(String.valueOf(server.getWorldPath(LevelResource.ROOT)));

		// Construct the game directory path
		CONFIG_DIR = Paths.get(System.getProperty("user.dir")).resolve("config");

		Server = server;

		// initialize commands, also allows me to easily disable any when there is a config
		Commands commandManager = server.getCommands();
		back.register(commandManager);
		home.register(commandManager);
		tpa.register(commandManager);

		StorageManager.StorageInit();
	}

	public static void onPlayerDeath(ServerPlayer player) {
		try {
			// update /back command position
			DeathLocationUpdater(player.position(), player.serverLevel(), player.getStringUUID());

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}