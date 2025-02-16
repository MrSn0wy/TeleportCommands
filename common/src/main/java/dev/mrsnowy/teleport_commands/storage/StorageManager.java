package dev.mrsnowy.teleport_commands.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.mrsnowy.teleport_commands.TeleportCommands;
import dev.mrsnowy.teleport_commands.common.NamedLocation;
import dev.mrsnowy.teleport_commands.common.Player;
import net.minecraft.core.BlockPos;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

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
                STORAGE = new StorageClass();
                StorageSaver(); // todo! verify that it creates em correctly
            }

        } catch (Exception e) {
            TeleportCommands.LOGGER.error("Error while creating the storage file! Exiting! => ", e);
            // crashing is probably better here, otherwise the whole mod will be broken
            System.exit(1);
        }
    }

    public static void StorageSaver() throws Exception {
        Gson gson = new GsonBuilder().create();
        byte[] json = gson.toJson( STORAGE ).getBytes();

        Files.write(STORAGE_FILE, json, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    }


    public static class StorageClass {
        private static final ArrayList<NamedLocation> Warps = new ArrayList<>();
        private static final ArrayList<Player> Players = new ArrayList<>();

        // -----

        // returns all warps
        public ArrayList<NamedLocation> getWarps() {
            return Warps;
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

        // creates a new warp, if there already is a warp it will update the existing one
        public void setWarp(String name, BlockPos pos, String world) throws Exception {
            Optional<NamedLocation> OptionalWarp = getWarp(name);

            if (OptionalWarp.isEmpty()) {
                // create a new warp
                NamedLocation warp = new NamedLocation(name, pos, world);
                Warps.add(warp);
            } else {
                // modify existing warp
                NamedLocation warp = OptionalWarp.get();
                warp.setName(name);
            }

            StorageSaver();
        }

        // creates a new player, if there already is a player it will return the existing one. The player won't be saved unless they actually do something lol
        // todo! check if this works fully
        public Player addPlayer(String uuid) {
            final Optional<Player> OptionalPlayer = getPlayer(uuid);

            if (OptionalPlayer.isEmpty()) {
                // create new player
                Player player = new Player(uuid);
                Players.add(player);
//                TeleportCommands.LOGGER.info("Player '{}' added successfully in storage!", uuid); // todo! prob remove these loggers

                return player;
            } else {
                // return existing player
//                TeleportCommands.LOGGER.info("Player '{}' already exists!", uuid);
                return OptionalPlayer.get();
            }
        }

        // -----

        public void rmWarp(NamedLocation warp) throws Exception {
            Warps.remove(warp);
            StorageSaver();
        }
    }
}
