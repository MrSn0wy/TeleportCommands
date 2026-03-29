package dev.mrsnowy.teleport_commands;

import com.google.gson.*;
import com.mojang.brigadier.CommandDispatcher;
import dev.mrsnowy.teleport_commands.storage.StorageManager;
import dev.mrsnowy.teleport_commands.commands.*;
import dev.mrsnowy.teleport_commands.storage.DeathLocationStorage;
import dev.mrsnowy.teleport_commands.storage.ConfigManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.core.BlockPos;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TeleportCommands {
	public static String MOD_LOADER;
	public static Path SAVE_DIR;
	public static Path CONFIG_DIR;
	public static MinecraftServer SERVER;


	// Gets ran when the server starts, initializes the mod :3
	public static void initializeMod(MinecraftServer server) {
		Constants.LOGGER.info("Initializing Teleport Commands (V{})! Hello {}!", Constants.VERSION, MOD_LOADER);

		SAVE_DIR = Path.of(String.valueOf(server.getWorldPath(LevelResource.ROOT)));
		CONFIG_DIR = Paths.get(System.getProperty("user.dir")).resolve("config"); // Construct the game directory path
		SERVER = server;

		StorageManager.StorageInit(); // Initialize the storage file
        ConfigManager.ConfigInit();
		DeathLocationStorage.clearDeathLocations(); // Clear data of death locations.
	}

    // initialize commands, also allows me to easily disable any when there is a config
    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        back.register(dispatcher);
        home.register(dispatcher);
        tpa.register(dispatcher);
        warp.register(dispatcher);
        worldspawn.register(dispatcher);
        main.register(dispatcher);
    }

	// Runs when the playerDeath mixin calls it, updates the /back command position
	public static void onPlayerDeath(ServerPlayer player) {
		BlockPos pos = new BlockPos(player.getBlockX(), player.getBlockY(), player.getBlockZ());
		String world = player.level().dimension().identifier().toString();
		String uuid = player.getStringUUID();

		DeathLocationStorage.setDeathLocation(uuid, pos, world);
	}
}