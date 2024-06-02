package dev.mrsnowy.teleport_commands;

import com.google.gson.*;
import dev.mrsnowy.teleport_commands.storage.StorageManager;
import dev.mrsnowy.teleport_commands.commands.*;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static dev.mrsnowy.teleport_commands.utils.tools.DeathLocationUpdater;

public class TeleportCommands {
	public static final String MOD_ID = "teleport_commands";
	public static final String MOD_NAME = "Teleport Commands";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);
	public static String MOD_LOADER;
	public static Path SAVE_DIR;
	public static Path CONFIG_DIR;
//	public static MinecraftServer Server;


	// Gets ran when the server starts
	public static void initializeMod(MinecraftServer server) {
		// initialize da variables
		LOGGER.info("Initializing Teleport Commands! Hello {}!", MOD_LOADER);

		SAVE_DIR = Path.of(String.valueOf(server.getWorldPath(LevelResource.ROOT)));

		// Construct the game directory path
		CONFIG_DIR = Paths.get(System.getProperty("user.dir")).resolve("config");

//		Server = server;

		cleanStorage();

		// initialize commands, also allows me to easily disable any when there is a config
		Commands commandManager = server.getCommands();
		back.register(commandManager);
		home.register(commandManager);
		tpa.register(commandManager);
	}

	public static void onPlayerDeath(ServerPlayer player) {
		try {
			// update /back command position
			DeathLocationUpdater(player.position(), player.serverLevel(), player.getStringUUID());

		} catch (Exception e) {
			LOGGER.error(e.toString());
		}
	}

	// cleans and updates Storage to the newest "version"
	private static void cleanStorage() {
		LOGGER.info("Cleaning and updating Storage!");
		try {
			StorageManager.StorageInit();
			FileReader reader = new FileReader(StorageManager.STORAGE_FILE.toString());
			JsonElement jsonElement = JsonParser.parseReader(reader);

			boolean done = false;

			if (jsonElement.isJsonObject()) {
				JsonObject mainJsonObject = jsonElement.getAsJsonObject();

				// get the Players list
				if (mainJsonObject.has("Players") && mainJsonObject.get("Players").isJsonArray()) {

					JsonArray newPlayersArray = new JsonArray();

					// players
					for (JsonElement playerElement : mainJsonObject.get("Players").getAsJsonArray()) {
						System.out.println("Element: " + playerElement);

						// player
						if (playerElement.isJsonObject()) {

							JsonObject player = playerElement.getAsJsonObject();
							boolean hasInformation = false;

							String UUID = player.has("Player_UUID")
									? player.get("Player_UUID").getAsString() : (player.has("UUID")
									? player.get("UUID").getAsString() : null);

							String DefaultHome = player.has("DefaultHome")
									? player.get("DefaultHome").getAsString() : "";


							// Clean death location after server restart
							JsonObject deathLocation = new JsonObject();

							deathLocation.addProperty("x", 0);
							deathLocation.addProperty("y", 0);
							deathLocation.addProperty("z", 0);
							deathLocation.addProperty("world", "");


							JsonArray homes = new JsonArray();

							if (player.has("Homes") && player.get("Homes").isJsonArray() ) {
								JsonArray tempHomes = player.get("Homes").getAsJsonArray();
								boolean defaultFound = false;


								for (JsonElement homeElement : tempHomes) {
									if (homeElement.isJsonObject()) {
										JsonObject home = homeElement.getAsJsonObject();

										String homeName = home.has("name")
												? home.get("name").getAsString() : "";

										Double homeX = home.has("x")
												? home.get("x").getAsDouble() : null;

										Double homeY = home.has("y")
												? home.get("y").getAsDouble() : null;

										Double homeZ = home.has("z")
												? home.get("z").getAsDouble() : null;

										String homeWorld = home.has("world")
												? home.get("world").getAsString() : "";


										// check if it is valid
										if (!homeName.isBlank() && !homeWorld.isBlank() && homeX != null && homeY != null && homeZ != null) {

											// check if it is the default home
											if (!DefaultHome.isBlank() && homeName.equals(DefaultHome)) {
												defaultFound = true;
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
								if (!defaultFound) {
									DefaultHome = "";
								}
							}

							// if it isn't empty it gets added to the newPlayersArray
							if ((UUID != null && !UUID.isBlank()) && hasInformation) {

								JsonObject newPlayer = new JsonObject();

								newPlayer.addProperty("UUID", UUID);
								newPlayer.addProperty("DefaultHome", DefaultHome);
								newPlayer.add("deathLocation", deathLocation);
								newPlayer.add("Homes", homes);

								newPlayersArray.add(newPlayer);
							}
						}
					}

					// save the cleaned and updated file
					mainJsonObject.remove("Players");
					mainJsonObject.add("Players", newPlayersArray);

					Gson gson = new GsonBuilder().create();
					byte[] json = gson.toJson(mainJsonObject).getBytes();
					Files.write(StorageManager.STORAGE_FILE, json, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
				}
			}

		} catch (IOException e) {
			LOGGER.error(e.toString());
		}
	}
}