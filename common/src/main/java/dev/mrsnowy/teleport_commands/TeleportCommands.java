package dev.mrsnowy.teleport_commands;

import com.mojang.brigadier.CommandDispatcher;
import dev.mrsnowy.teleport_commands.storage.StorageManager;
import dev.mrsnowy.teleport_commands.commands.*;
import dev.mrsnowy.teleport_commands.storage.DeathLocationStorage;
import dev.mrsnowy.teleport_commands.storage.configManager;
import dev.mrsnowy.teleport_commands.utils.teleporter;
import dev.mrsnowy.teleport_commands.utils.tools;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.core.BlockPos;

import java.nio.file.Path;
import java.nio.file.Paths;

public class TeleportCommands {
	public String modLoader;
	public Path saveDir;
	public Path configDir;
	public MinecraftServer server;
	public StorageManager storageManager;
	public configManager config;
	public DeathLocationStorage deathLocationStorage;
	public teleporter teleporter;

	// Gets ran when the server starts, initializes the mod :3
	public void initializeMod(MinecraftServer server) {
		Constants.LOGGER.info("Initializing Teleport Commands (V{})! Hello {}!", Constants.VERSION, modLoader);

		saveDir = Path.of(String.valueOf(server.getWorldPath(LevelResource.ROOT)));
		configDir = Paths.get(System.getProperty("user.dir")).resolve("config");
		this.server = server;

		storageManager = new StorageManager(this);
		config = new configManager(this);
		deathLocationStorage = new DeathLocationStorage();
		teleporter = new teleporter(this);
	}

    // initialize commands, also allows me to easily disable any when there is a config
    public void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        new back(dispatcher, this);
        home.register(dispatcher);
        tpa.register(dispatcher);
        warp.register(dispatcher);
        worldspawn.register(dispatcher);
        main.register(dispatcher);
    }

	// Runs when the playerDeath mixin calls it, updates the /back command position
	public void onPlayerDeath(ServerPlayer player) {
		BlockPos pos = new BlockPos(player.getBlockX(), player.getBlockY(), player.getBlockZ());
		String world = player.serverLevel().dimension().location().toString();
		String uuid = player.getStringUUID();

		this.deathLocationStorage.setDeathLocation(uuid, pos, world);
	}
}