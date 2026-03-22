package dev.mrsnowy.teleport_commands.storage;

import com.google.gson.*;
import dev.mrsnowy.teleport_commands.Constants;
import dev.mrsnowy.teleport_commands.TeleportCommands;

import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class configManager {
    public Path configFile;
    public ConfigClass config;

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final int defaultVersion = new ConfigClass().getVersion();
    private final TeleportCommands teleportCommands;

    public configManager(TeleportCommands teleportCommands) {
        this.teleportCommands = teleportCommands;
        configFile = teleportCommands.configDir.resolve("teleport_commands.json");

        try {
            configLoader();

        } catch (Exception e) {
            // crashing is probably better here, otherwise the whole mod will be broken
            Constants.LOGGER.error("Error while initializing the config file! Exiting! => ", e);
            throw new RuntimeException("Error while initializing the config file! Exiting! => ", e);
        }
    }

    /// This function loads the config from disk
    public void configLoader() throws Exception {
        if (!configFile.toFile().exists() || configFile.toFile().length() == 0) {
            Files.createDirectories(teleportCommands.configDir);

            Constants.LOGGER.warn("Config file was not found or was empty! Initializing config");
            config = new ConfigClass();
            configSaver();
            Constants.LOGGER.info("Config created successfully!");
        }

        configMigrator();

        FileReader reader = new FileReader(configFile.toFile());
        config = gson.fromJson(reader, ConfigClass.class);
        if (config == null) {
            Constants.LOGGER.warn("Config file was empty! Loading defaults...");
            config = new ConfigClass();
            configSaver();
        }

        configSaver(); // Save it so any missing values get added to the file.
        Constants.LOGGER.info("Config loaded successfully!");
    }

    /// This function checks what version the config file is and migrates it to the current version of the mod.
    public void configMigrator() throws Exception {
        FileReader reader = new FileReader(configFile.toFile());
        JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);

        int version = jsonObject.has("version") ? jsonObject.get("version").getAsInt() : 0;

        if (version < defaultVersion) {
//            Constants.LOGGER.warn("Config file is v{}, migrating to v{}!", version, defaultVersion);
//
//            // Save the storage :3
//            byte[] json = GSON.toJson(jsonObject, JsonArray.class).getBytes();
//            Files.write(CONFIG_FILE, json, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
//
//            Constants.LOGGER.info("Config file migrated to v{} successfully!", defaultVersion);
        } else if (version > defaultVersion) {
            String message = String.format("Teleport Commands: The config file's version is newer than the supported version, found v%s, expected <= v%s.\n" +
                            "If you intentionally backported then you can attempt to downgrade the config file located at this location: \"%s\".\n",
                    version, defaultVersion, configFile.toAbsolutePath());

            throw new IllegalStateException(message);
        }
    }

    /// Saves the config to disk
    public void configSaver() throws Exception {
        // todo! maybe throttle saves?
        byte[] json = gson.toJson(config).getBytes();

        Files.write(configFile, json, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
    }

    public static class ConfigClass {
        private final int version = 0;
        public Teleporting teleporting = new Teleporting();
        public Back back = new Back();
        public Home home = new Home();
        public Tpa tpa = new Tpa();
        public Warp warp = new Warp();
        public WorldSpawn worldSpawn = new WorldSpawn();

        public int getVersion() {
            return version;
        }

        public static final class Teleporting {
            /// Delay before teleporting
            private int delay = 3;
            /// Cooldown before they can teleport again
            private int cooldown = 5;
            /// Allow moving while teleporting
            private boolean whileMoving = true;
            /// Allow fighting while teleporting
            private boolean whileFighting = false;
            /// Cooldown after fighting before they can teleport again
            private int fightCooldown = 10;

            public int getDelay() {
                return delay;
            }

            public void setDelay(int delay) {
                this.delay = delay;
            }

            public boolean isWhileMoving() {
                return whileMoving;
            }

            public void setWhileMoving(boolean whileMoving) {
                this.whileMoving = whileMoving;
            }

            public boolean isWhileFighting() {
                return whileFighting;
            }

            public void setWhileFighting(boolean whileFighting) {
                this.whileFighting = whileFighting;
            }

            public int getFightCooldown() {
                return fightCooldown;
            }

            public void setFightCooldown(int fightCooldown) {
                this.fightCooldown = fightCooldown;
            }

            public int getCooldown() {
                return cooldown;
            }

            public void setCooldown(int cooldown) {
                this.cooldown = cooldown;
            }
        }

        public static final class Back {
            private boolean enabled = true;
            /// Deletes the /back after teleporting, so you cant call /back twice.
            private boolean deleteAfterTeleport = false;

            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            public boolean isDeleteAfterTeleport() {
                return deleteAfterTeleport;
            }

            public void setDeleteAfterTeleport(boolean deleteAfterTeleport) {
                this.deleteAfterTeleport = deleteAfterTeleport;
            }
        }

        public static final class Home {
            private boolean enabled = true;
            /// The maximum amount of homes a player can have
            private int playerMaximum = 20;
            /// If a home with an invalid dimension should get automatically deleted
            private boolean deleteInvalid = false;

            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            public int getPlayerMaximum() {
                return playerMaximum;
            }

            public void setPlayerMaximum(int playerMaximum) {
                this.playerMaximum = playerMaximum;
            }

            public boolean isDeleteInvalid() {
                return deleteInvalid;
            }

            public void setDeleteInvalid(boolean deleteInvalid) {
                this.deleteInvalid = deleteInvalid;
            }
        }

        public static final class Tpa {
            private boolean enabled = true;

            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }
        }

        public static final class Warp {
            private boolean enabled = true;
            /// If a warp with an invalid dimension should get automatically deleted
            private boolean deleteInvalid = false;

            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            public boolean isDeleteInvalid() {
                return deleteInvalid;
            }

            public void setDeleteInvalid(boolean deleteInvalid) {
                this.deleteInvalid = deleteInvalid;
            }
        }

        public static final class WorldSpawn {
            private boolean enabled = true;
            private String world_id = "minecraft:overworld";

            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            public String getWorld_id() {
                return world_id;
            }

            public void setWorld_id(String world_id) {
                this.world_id = world_id;
            }
        }
    }

    // --- Ideas! ---
    // Make config options for disabling certain commands
    // Make config options for renaming certain commands
    // Make config option for changing required permission level for certain commands
    // Make config for setting max homes
    // Make config that adds a delay between teleports and when fighting. (in teleport function?)
    // Make config that automatically deletes namedLocations (warps/homes) with invalid world id's
    // Make config for setting the world_id for /worldspawn ?
}
