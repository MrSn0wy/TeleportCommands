package dev.mrsnowy.teleport_commands.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.datafixers.util.Pair;
import dev.mrsnowy.teleport_commands.TeleportCommands;
import net.minecraft.core.BlockPos;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
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

    public static StorageClass.Player PlayerAdd(String UUID) {

        // try to find an exising storage for this player
        Optional<StorageClass.Player> playerStorage = STORAGE.Players.stream()
                .filter(player -> Objects.equals(UUID, player.UUID))
                .findFirst();

        if (playerStorage.isEmpty()) {
            StorageClass.Player newPlayer = new StorageClass.Player(UUID); // TODO! verify that it creates the player proper

            List<StorageClass.Player> playerList = STORAGE.Players;
            playerList.add(newPlayer);

//            StorageSaver(); // no need to save since no data is actually set yet!
            TeleportCommands.LOGGER.info("Player '{}' added successfully in storage!", UUID);
            return newPlayer;
        } else {
            TeleportCommands.LOGGER.info("Player '{}' already exists!", UUID);
            return playerStorage.get();
        }
    }

    public static void StorageSaver() throws Exception {
        Gson gson = new GsonBuilder().create();
        byte[] json = gson.toJson( STORAGE ).getBytes();

        Files.write(STORAGE_FILE, json, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public static Pair<StorageClass, List<StorageClass.NamedLocation>> getWarpStorage() {
        return new Pair<>(STORAGE, STORAGE.Warps);
    }

    public static Pair<StorageClass, StorageClass.Player> GetPlayerStorage(String UUID) {

        // try to find an exising storage for this player
        Optional<StorageClass.Player> playerStorage = STORAGE.Players.stream()
                .filter(player -> Objects.equals(UUID, player.UUID))
                .findFirst();

        if (playerStorage.isEmpty()) {
            StorageClass.Player player = PlayerAdd(UUID); // create a new player

            return new Pair<>(STORAGE, player);
        }

        return new Pair<>(STORAGE, playerStorage.get());
    }


    public static class StorageClass {
        public static List<NamedLocation> Warps = new ArrayList<>();
        public static List<Player> Players = new ArrayList<>();

        public static class NamedLocation {
            public String name;
            public final int x;
            public final int y;
            public final int z;
            public final String world;

            public NamedLocation(String name, BlockPos pos, String world) {
                this.name = name;
                this.x = pos.getX();
                this.y = pos.getY();
                this.z = pos.getZ();
                this.world = world;
            }
        }

        public static class Location {
            public int x = 0;
            public int y = 0;
            public int z = 0;
            public String world = "";
        }

        public static class Player {
            public final String UUID;
            public String DefaultHome = "";
            public Location deathLocation; // todo! deprecate
            public List<NamedLocation> Homes = new ArrayList<>();

            public Player(String uuid) {
                this.UUID = uuid;
            }
        }
    }
}
