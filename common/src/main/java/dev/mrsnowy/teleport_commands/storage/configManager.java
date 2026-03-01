package dev.mrsnowy.teleport_commands.storage;

import com.google.gson.*;
import dev.mrsnowy.teleport_commands.Constants;
import dev.mrsnowy.teleport_commands.TeleportCommands;

import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class configManager {
    public Path CONFIG_FILE;
    public ConfigClass CONFIG;

    private final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final int defaultVersion = new ConfigClass().getVersion();
    private final TeleportCommands teleportCommands;

    public configManager(TeleportCommands teleportCommands) {
        this.teleportCommands = teleportCommands;
        CONFIG_FILE = teleportCommands.configDir.resolve("teleport_commands.json");

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
        if (!CONFIG_FILE.toFile().exists() || CONFIG_FILE.toFile().length() == 0) {
            Files.createDirectories(teleportCommands.configDir);

            Constants.LOGGER.warn("Config file was not found or was empty! Initializing config");
            CONFIG = new ConfigClass();
            configSaver();
            Constants.LOGGER.info("Config created successfully!");
        }

        configMigrator();

        FileReader reader = new FileReader(CONFIG_FILE.toFile());
        CONFIG = GSON.fromJson(reader, ConfigClass.class);
        if (CONFIG == null) {
            Constants.LOGGER.warn("Config file was empty! Loading defaults...");
            CONFIG = new ConfigClass();
            configSaver();
        }

        configSaver(); // Save it so any missing values get added to the file.
        Constants.LOGGER.info("Config loaded successfully!");
    }

    /// This function checks what version the config file is and migrates it to the current version of the mod.
    public void configMigrator() throws Exception {
        FileReader reader = new FileReader(CONFIG_FILE.toFile());
        JsonObject jsonObject = GSON.fromJson(reader, JsonObject.class);

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
                    version, defaultVersion, CONFIG_FILE.toAbsolutePath());

            throw new IllegalStateException(message);
        }
    }

    /// Saves the config to disk
    public void configSaver() throws Exception {
        // todo! maybe throttle saves?
        byte[] json = GSON.toJson(CONFIG).getBytes();

        Files.write(CONFIG_FILE, json, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
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
            private int delay = 5;
            private boolean whileMoving = true;
            private boolean whileFighting = false;
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
        }

        public static final class Back {
            private boolean enabled = true;
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
            private int playerMaximum = 20;
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
