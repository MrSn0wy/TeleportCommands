package dev.mrsnowy.teleport_commands.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.mrsnowy.teleport_commands.TeleportCommands;

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

    public static void StorageInit() {
        STORAGE_FOLDER = TeleportCommands.SAVE_DIR.resolve("TeleportCommands/");
        STORAGE_FILE = STORAGE_FOLDER.resolve("storage.json");

        try {
            if (!Files.exists(STORAGE_FOLDER)) {
                Files.createDirectories(STORAGE_FOLDER);
            }

            if (!Files.exists(STORAGE_FILE)) {
                Files.createFile(STORAGE_FILE);
            }

            // create the storage
            if (new File(String.valueOf(STORAGE_FILE)).length() == 0) {
                StorageClass root = new StorageClass();
                root.Players = new ArrayList<>();
                StorageSaver(root);
            }

        } catch (Exception e) {
            TeleportCommands.LOGGER.error(e.getMessage());
            // crashing is probably better here, otherwise the whole mod will be broken
            System.exit(1);
        }
    }

    public static void StorageAdd(String UUID) throws Exception {
        StorageClass storage = StorageRetriever();
//        Optional<StorageClass.Player> playerStorage = GetPlayerStorage(UUID, storage);
        Optional<StorageClass.Player> playerStorage = storage.Players.stream()
                .filter(player -> Objects.equals(UUID, player.Player_UUID))
                .findFirst();

        if (playerStorage.isEmpty()) {
            StorageClass.Player newPlayer = new StorageClass.Player();

            newPlayer.Player_UUID = UUID;
            newPlayer.DefaultHome = "";
            newPlayer.deathLocation = new StorageClass.Player.Location();
            newPlayer.deathLocation.x = new StorageClass.Player.Location().x;
            newPlayer.deathLocation.y = new StorageClass.Player.Location().y;
            newPlayer.deathLocation.z = new StorageClass.Player.Location().z;
            newPlayer.deathLocation.world = "";

            newPlayer.Homes = new ArrayList<>();

            List<StorageClass.Player> playerList = storage.Players;
            playerList.add(newPlayer);

            StorageSaver(storage);
            TeleportCommands.LOGGER.info("Player '" + UUID + "' added successfully in storage!");
        } else {
            TeleportCommands.LOGGER.info("Player '" + UUID + "' already exists!");
        }
    }

    public static void StorageSaver(StorageClass storage) throws Exception {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        byte[] json = gson.toJson(storage).getBytes();
        Files.write(STORAGE_FILE, json, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private static StorageClass StorageRetriever() throws Exception {
        // double check that the storage file is intact
        if (new File(String.valueOf(STORAGE_FILE)).length() == 0) {
            StorageInit();
        }
        String jsonContent = Files.readString(STORAGE_FILE);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.fromJson(jsonContent, StorageClass.class);
    }

    public static PlayerStorageResult GetPlayerStorage(String UUID) throws Exception {
        StorageClass storage = StorageRetriever();

        Optional<StorageClass.Player> playerStorage = storage.Players.stream()
                .filter(player -> Objects.equals(UUID, player.Player_UUID))
                .findFirst();

        if (playerStorage.isEmpty()) {
            StorageAdd(UUID);

            storage = StorageRetriever();

            playerStorage = storage.Players.stream()
                    .filter(player -> Objects.equals(UUID, player.Player_UUID))
                    .findFirst();

            if (playerStorage.isEmpty()) {
                throw new Exception("No Player found?!");
            }
        }

        return new PlayerStorageResult(storage, playerStorage.get());
    }

    public static class PlayerStorageResult {
        public StorageClass storage;
        public StorageClass.Player playerStorage;

        public PlayerStorageResult(StorageClass storage, StorageClass.Player playerStorage) {
            this.storage = storage;
            this.playerStorage = playerStorage;
        }
    }

    public static class StorageClass {
        public List<Player> Players;

        public static class Player {
            public String Player_UUID;
            public String DefaultHome;
            public Location deathLocation;
            public List<Home> Homes;

            public static class Location {
                public double x;
                public double y;
                public double z;
                public String world;
            }

            public static class Home {
                public String name;
                public double x;
                public double y;
                public double z;
                public String world;
            }
        }
    }
}
