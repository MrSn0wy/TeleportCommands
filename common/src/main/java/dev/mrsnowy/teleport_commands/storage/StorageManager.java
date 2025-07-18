package dev.mrsnowy.teleport_commands.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.mrsnowy.teleport_commands.Constants;
import dev.mrsnowy.teleport_commands.TeleportCommands;
import dev.mrsnowy.teleport_commands.common.NamedLocation;
import dev.mrsnowy.teleport_commands.common.Player;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Collections.unmodifiableList;

public class StorageManager {
    public static Path STORAGE_FOLDER;
    public static Path STORAGE_FILE;
    public static StorageClass STORAGE;

    public static void StorageInit() {
        STORAGE_FOLDER = TeleportCommands.SAVE_DIR.resolve("TeleportCommands/");
        STORAGE_FILE = STORAGE_FOLDER.resolve("storage.json");

        try {
            // check if the folder exists and create it
            if (!Files.exists(STORAGE_FOLDER)) {
                Files.createDirectories(STORAGE_FOLDER);
            }

            // check if the file exists and create it
            if (!Files.exists(STORAGE_FILE)) {
                Files.createFile(STORAGE_FILE);
            }

            // create the basic storage if it is empty
            if (new File(String.valueOf(STORAGE_FILE)).length() == 0) {
                StorageManager.STORAGE = new StorageClass();
                StorageSaver(); // todo! verify that it creates em correctly
            }

        } catch (Exception e) {
            Constants.LOGGER.error("Error while creating the storage file! Exiting! => ", e);
            // crashing is probably better here, otherwise the whole mod will be broken
            System.exit(1);
        }
    }

    public static void StorageSaver() throws Exception {
        // todo! maybe throttle saves?
        Gson gson = new GsonBuilder().create();
        byte[] json = gson.toJson( StorageManager.STORAGE ).getBytes();

        Files.write(STORAGE_FILE, json, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    }


    public static class StorageClass {
        private final ArrayList<NamedLocation> Warps = new ArrayList<>();
        private final ArrayList<Player> Players = new ArrayList<>();

        // -----

        // returns all warps
        public List<NamedLocation> getWarps() {
            return unmodifiableList(Warps);
        }

        // filters the warpList and finds the one with the name (if there is one)
        public Optional<NamedLocation> getWarp(String name) {
            return Warps.stream()
                    .filter(warp -> Objects.equals(warp.getName(), name))
                    .findFirst();
        }

        // filters the playerList and finds the one with the uuid (if there is one)
        public Optional<Player> getPlayer(String uuid) {
            return Players.stream()
                    .filter( player -> Objects.equals( player.getUUID(), uuid ))
                    .findFirst();
        }

        // -----

        // Adds a NamedLocation to the warp list, returns true if a warp with the same name already exists
        public boolean addWarp(NamedLocation warp) throws Exception {
            if (getWarp(warp.getName()).isPresent()) {
                // Warp with same name found!
                return true;

            } else {
                Warps.add(warp);
                StorageSaver();
                return false;
            }
        }

        // Creates a new player, if there already is a player it will return the existing one. The player won't be saved unless they actually do something lol
        // The name of this function is wack but whatever kewk
        public Player addPlayer(String uuid) {
            final Optional<Player> OptionalPlayer = getPlayer(uuid);

            if (OptionalPlayer.isEmpty()) {
                // create and return new player
                Player player = new Player(uuid);
                Players.add(player);

                return player;
            } else {
                // return existing player
                return OptionalPlayer.get();
            }
        }

        // -----

        // Remove a warp, if the warp isn't found then nothing will happen
        public void removeWarp(NamedLocation warp) throws Exception {
            Warps.remove(warp);
            StorageSaver();
        }
    }
}
