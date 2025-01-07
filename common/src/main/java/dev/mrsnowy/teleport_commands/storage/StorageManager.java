package dev.mrsnowy.teleport_commands.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

//    public static StorageClass.Player PlayerAdd(String UUID) {
//
//        // try to find an exising storage for this player
//        Optional<StorageClass.Player> playerStorage = STORAGE.Players.stream()
//                .filter(player -> Objects.equals(UUID, player.UUID))
//                .findFirst();
//
//        if (playerStorage.isEmpty()) {
//            StorageClass.Player newPlayer = new StorageClass.Player(UUID); // TODO! verify that it creates the player proper
//
//            List<StorageClass.Player> playerList = STORAGE.Players;
//            playerList.add(newPlayer);
//
////            StorageSaver(); // no need to save since no data is actually set yet!
//            TeleportCommands.LOGGER.info("Player '{}' added successfully in storage!", UUID);
//            return newPlayer;
//        } else {
//            TeleportCommands.LOGGER.info("Player '{}' already exists!", UUID);
//            return playerStorage.get();
//        }
//    }

    public static void StorageSaver() throws Exception {
        Gson gson = new GsonBuilder().create();
        byte[] json = gson.toJson( STORAGE ).getBytes();

        TeleportCommands.LOGGER.info(STORAGE.toString());

        Files.write(STORAGE_FILE, json, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    }

//    public static Pair<StorageClass, List<StorageClass.NamedLocation>> getWarpStorage() {
//        return new Pair<>(STORAGE, STORAGE.Warps);
//    }

//    public static Pair<StorageClass, StorageClass.Player> GetPlayerStorage(String UUID) {
//
//        // try to find an exising storage for this player
//        Optional<StorageClass.Player> playerStorage = STORAGE.Players.stream()
//                .filter(player -> Objects.equals(UUID, player.UUID))
//                .findFirst();
//
//        if (playerStorage.isEmpty()) {
//            StorageClass.Player player = PlayerAdd(UUID); // create a new player
//
//            return new Pair<>(STORAGE, player);
//        }
//
//        return new Pair<>(STORAGE, playerStorage.get());
//    }


    public static class StorageClass {
        public static warpList Warps = new warpList();
        public static playerList Players = new playerList();

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

        public static class warpList {
            private final ArrayList<NamedLocation> warpList = new ArrayList<>();

            // filters the warpList and finds the one with the name (if there is one)
            public Optional<NamedLocation> getWarp(String name) {
                return warpList.stream()
                        .filter( warp -> Objects.equals( warp.name, name ))
                        .findFirst();
            }

            // returns all warps
            public ArrayList<NamedLocation> getWarps() {
                return warpList;
            }

            // creates a new warp, if there already is a warp it will update the existing one
            public void setWarp(String name, BlockPos pos, String world) throws Exception {
                Optional<NamedLocation> OptionalWarp = getWarp(name);

                if (OptionalWarp.isEmpty()) {
                    // create a new warp
                    NamedLocation warp = new NamedLocation(name, pos, world);
                    warpList.add(warp);
                    StorageSaver();
                } else {
                    // modify existing warp
                    NamedLocation warp = OptionalWarp.get();
                    warp.name = name;
                }
            }
        }

        public static class playerList {
            private final ArrayList<Player> playerList = new ArrayList<>();

            // filters the playerList and finds the one with the uuid (if there is one)
            public Optional<Player> getPlayer(String uuid) {
                return playerList.stream()
                        .filter( player -> Objects.equals( player.UUID, uuid ))
                        .findFirst();
            }

            // creates a new player, if there already is a player it will return the existing one
            public Player addPlayer(String uuid, BlockPos pos, String world) {
                Optional<Player> OptionalPlayer = getPlayer(uuid);

                if (OptionalPlayer.isEmpty()) {
                    // create new player
                    Player player = new Player(uuid);
                    playerList.add(player);
                    TeleportCommands.LOGGER.info("Player '{}' added successfully in storage!", uuid);

                    return player;
                } else {
                    // return existing player
                    TeleportCommands.LOGGER.info("Player '{}' already exists!", uuid);
                    return OptionalPlayer.get();
                }
            }
        }


        public static class Player {
            public final String UUID;
            public String DefaultHome = "";
            public homeList Homes = new homeList();

            public Player(String uuid) {
                this.UUID = uuid;
            }

            public static class homeList {
                private final List<NamedLocation> Homes = new ArrayList<>();

                // filters the Homes and finds the one with the name (if there is one)
                public Optional<NamedLocation> getHome(String name)  {
                    return Homes.stream()
                            .filter( home -> Objects.equals( home.name, name ))
                            .findFirst();
                }

                // returns all homes
                public List<NamedLocation> getHomes() {
                    return Homes;
                }

                // creates a new home, if there already is a home it will update the existing one
                public void setHome(String name, BlockPos pos, String world) throws Exception {
                    Optional<NamedLocation> OptionalHome = getHome(name);

                    if (OptionalHome.isEmpty()) {
                        NamedLocation home = new NamedLocation(name, pos, world);

                        Homes.add(home);
                        StorageSaver();
                    } else {
                        NamedLocation home = OptionalHome.get();

                        home.name = name;
                        StorageSaver();
                    }
                }
            }
        }
    }
}
