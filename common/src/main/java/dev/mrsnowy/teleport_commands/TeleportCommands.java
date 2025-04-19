package dev.mrsnowy.teleport_commands;

import com.google.gson.*;
import dev.mrsnowy.teleport_commands.storage.StorageManager;
import dev.mrsnowy.teleport_commands.commands.*;
import dev.mrsnowy.teleport_commands.storage.DeathLocationStorage;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import net.minecraft.core.BlockPos;

import static dev.mrsnowy.teleport_commands.storage.StorageManager.*;

public class TeleportCommands {
	public static String MOD_LOADER;
	public static Path SAVE_DIR;
	public static Path CONFIG_DIR;
	public static MinecraftServer SERVER;


	// Gets ran when the server starts
	public static void initializeMod(MinecraftServer server) {

//		InputStream stream = TeleportCommands.class.getResourceAsStream("/version");
//		try {
//			BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(stream, "Couldn't find the version file!"), StandardCharsets.UTF_8));
//			Constants.VERSION = reader.readLine();
//
//		} catch (Exception e) {
//			Constants.LOGGER.error("Couldn't find the version file!");
//		}

		// initialize da variables
		Constants.LOGGER.info("Initializing Teleport Commands (V{})! Hello {}!", Constants.VERSION, MOD_LOADER);

		SAVE_DIR = Path.of(String.valueOf(server.getWorldPath(LevelResource.ROOT)));

		// Construct the game directory path
		CONFIG_DIR = Paths.get(System.getProperty("user.dir")).resolve("config");

		SERVER = server;

		StorageManager.STORAGE = storageValidator();

		// initialize commands, also allows me to easily disable any when there is a config
		Commands commandManager = server.getCommands();
		back.register(commandManager);
		home.register(commandManager);
		tpa.register(commandManager);
		warp.register(commandManager);
		worldspawn.register(commandManager);
	}


	// Runs when the playerDeath mixin calls it, updates the /back command position
	public static void onPlayerDeath(ServerPlayer player) {
		BlockPos pos = new BlockPos(player.getBlockX(), player.getBlockY(), player.getBlockZ());
		String world = player.serverLevel().dimension().location().toString();
		String uuid = player.getStringUUID();

		DeathLocationStorage.setDeathLocation(uuid, pos, world);
	}

	// cleans and updates Storage to the newest "version". This is painful
	private static StorageClass storageValidator() {
		Constants.LOGGER.info("Cleaning and updating Storage!");

		try {
			StorageInit();

			long startFileSize = Files.size(StorageManager.STORAGE_FILE);

			FileReader reader = new FileReader(StorageManager.STORAGE_FILE.toString());
			JsonElement jsonElement = JsonParser.parseReader(reader);

			if (jsonElement.isJsonObject()) {

				JsonObject mainJsonObject = jsonElement.getAsJsonObject();
				JsonArray newWarpsArray = new JsonArray();
				JsonArray newPlayersArray = new JsonArray();

				// get the Warps list
				if (mainJsonObject.has("Warps") && mainJsonObject.get("Warps").isJsonArray()) {

					// Warps
					for (JsonElement warpElement : mainJsonObject.get("Warps").getAsJsonArray()) {

						// Warp
						if (warpElement.isJsonObject()) {
							JsonObject warp = warpElement.getAsJsonObject();

							String warpName = warp.has("name") ? warp.get("name").getAsString()  : "";
							Integer warpX = warp.has("x") ? warp.get("x").getAsInt() : null;
							Integer warpY = warp.has("y") ? warp.get("y").getAsInt() : null;
							Integer warpZ = warp.has("z") ? warp.get("z").getAsInt() : null;
							String warpWorld = warp.has("world") ? warp.get("world").getAsString() : "";

							// check if it is valid
							if (!warpName.isBlank() && !warpWorld.isBlank() && warpX != null && warpY != null && warpZ != null) {
								JsonObject newWarp = new JsonObject();

								newWarp.addProperty("name", warpName);
								newWarp.addProperty("x", warpX);
								newWarp.addProperty("y", warpY);
								newWarp.addProperty("z", warpZ);
								newWarp.addProperty("world", warpWorld);

								newWarpsArray.add(newWarp);
							}
						}
					}
				}


				// get the Players list
				if (mainJsonObject.has("Players") && mainJsonObject.get("Players").isJsonArray()) {

					// players
					for (JsonElement playerElement : mainJsonObject.get("Players").getAsJsonArray()) {

						// player
						if (playerElement.isJsonObject()) {

							JsonObject player = playerElement.getAsJsonObject();
							boolean hasInformation = false;

							String UUID = player.has("Player_UUID")
									? player.get("Player_UUID").getAsString() : (player.has("UUID")
									? player.get("UUID").getAsString() : null);

							String DefaultHome = player.has("DefaultHome")
									? player.get("DefaultHome").getAsString() : "";

							JsonArray homes = new JsonArray();

							if (player.has("Homes") && player.get("Homes").isJsonArray() ) {
								JsonArray tempHomes = player.get("Homes").getAsJsonArray();
								boolean defaultHomeFound = false;


								for (JsonElement homeElement : tempHomes) {
									if (homeElement.isJsonObject()) {
										JsonObject home = homeElement.getAsJsonObject();

										String homeName = home.has("name")
												? home.get("name").getAsString() : "";

										// upgrade doubles to int
										Integer homeX = home.has("x") && home.get("x").isJsonPrimitive() && home.get("x").getAsJsonPrimitive().isNumber()
												? (int) Math.floor(home.get("x").getAsDouble()) : null;

										Integer homeY = home.has("y") && home.get("y").isJsonPrimitive() && home.get("y").getAsJsonPrimitive().isNumber()
												? (int) Math.floor(home.get("y").getAsDouble()) : null;

										Integer homeZ = home.has("z") && home.get("z").isJsonPrimitive() && home.get("z").getAsJsonPrimitive().isNumber()
												? (int) Math.floor(home.get("z").getAsDouble()) : null;

										String homeWorld = home.has("world")
												? home.get("world").getAsString() : "";

										// check if it is valid
										if (!homeName.isBlank() && !homeWorld.isBlank() && homeX != null && homeY != null && homeZ != null) {

											// check if it is the default home
											if (!DefaultHome.isBlank() && homeName.equals(DefaultHome)) {
												defaultHomeFound = true;
											}

											JsonObject newHome = new JsonObject();

											newHome.addProperty("name", homeName);
											newHome.addProperty("x", homeX);
											newHome.addProperty("y", homeY);
											newHome.addProperty("z", homeZ);
											newHome.addProperty("world", homeWorld);

											homes.add(newHome);
											hasInformation = true;
										}
									}
								}

								// clean DefaultHome if there is no home with the name
								if (!defaultHomeFound) {
									DefaultHome = "";
								}
							}

							// if it isn't empty it gets added to the newPlayersArray
							if ((UUID != null && !UUID.isBlank()) && hasInformation) {

								JsonObject newPlayer = new JsonObject();

								newPlayer.addProperty("UUID", UUID);
								newPlayer.addProperty("DefaultHome", DefaultHome);
								newPlayer.add("Homes", homes);

								newPlayersArray.add(newPlayer);
							}
						}
					}
				}

				// save the cleaned and updated file
				mainJsonObject.remove("Warps");
				mainJsonObject.add("Warps", newWarpsArray);

				mainJsonObject.remove("Players");
				mainJsonObject.add("Players", newPlayersArray);

				// save the cleaned database
				Gson gson = new GsonBuilder().create();
				byte[] json = gson.toJson(mainJsonObject).getBytes();
				Files.write(StorageManager.STORAGE_FILE, json, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);

				// Only show amount cleaned when it isn't 0B lool
				int diff = Math.round(( startFileSize - Files.size(StorageManager.STORAGE_FILE) ));

				if (diff > 0) {
					Constants.LOGGER.info("Success! Cleaned: {}B", diff);
				} else {
					Constants.LOGGER.info("Success!");
				}

				return gson.fromJson(mainJsonObject, StorageManager.StorageClass.class);
			}

		} catch (IOException e) {
			Constants.LOGGER.error("Error while cleaning the database!", e);
		}

        return null;
    }
}