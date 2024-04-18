package dev.mrsnowy.teleport_commands.utils;

import net.fabricmc.fabric.api.dimension.v1.FabricDimensions;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;

import java.util.*;

import static dev.mrsnowy.teleport_commands.storage.StorageManager.*;
import static net.minecraft.sound.SoundEvents.ENTITY_ENDERMAN_TELEPORT;

public class tools {
    private static ArrayList<tpaArrayClass> tpaList = new ArrayList<>();

    private static class tpaArrayClass {
        private ServerPlayerEntity InitPlayer;
        private ServerPlayerEntity RecPlayer;
        private boolean here;
    }

    private static void Teleporter(ServerPlayerEntity player, ServerWorld world, Vec3d coords) {
        world.spawnParticles(ParticleTypes.SNOWFLAKE, player.getX(), player.getY() + 1, player.getZ(), 15, 0.0D, 0.0D, 0.0D, 0.01);
        world.spawnParticles(ParticleTypes.WHITE_SMOKE, player.getX(), player.getY(), player.getZ(), 10, 0.0D, 1.0D, 0.0D, 0.03);
        world.playSound(null, player.getBlockPos(), SoundEvent.of(ENTITY_ENDERMAN_TELEPORT.getId()), SoundCategory.PLAYERS, 0.4f, 1.0f);

        FabricDimensions.teleport(player, world, new TeleportTarget(coords, Vec3d.ZERO, player.getYaw(), player.getPitch()));
        
        new Timer().schedule(
            new TimerTask() {
                @Override
                public void run() {
                    world.playSound(null, player.getBlockPos(), SoundEvent.of(ENTITY_ENDERMAN_TELEPORT.getId()), SoundCategory.PLAYERS, 0.4f, 1.0f);
                    world.spawnParticles(ParticleTypes.SNOWFLAKE, player.getX(), player.getY() , player.getZ(), 15, 0.0D, 1.0D, 0.0D, 0.01);
                    world.spawnParticles(ParticleTypes.WHITE_SMOKE, player.getX(), player.getY(), player.getZ(), 10, 0.0D, 0.0D, 0.0D, 0.03);
                }
            }, 50 // 1 tick, i think?
        );
    }


    public static void SetHome(ServerPlayerEntity player, String homeName) throws Exception {
        homeName = homeName.toLowerCase();
        Vec3d pos = player.getPos();
        ServerWorld world = player.getServerWorld();

        PlayerStorageResult storages = GetPlayerStorage(player.getUuidAsString());
        StorageClass storage = storages.storage;
        StorageClass.Player playerStorage = storages.playerStorage;

        boolean homeNotFound = true;

        // check for duplicates
        for (StorageClass.Player.Home currentHome : playerStorage.Homes) {
            if (Objects.equals(currentHome.name, homeName)) {
                homeNotFound = false;
                break;
            }
        }

        if (homeNotFound) {
            // Create a new Home
            StorageClass.Player.Home homeLocation = new StorageClass.Player.Home();

            homeLocation.name = homeName;
            homeLocation.x = Double.parseDouble(String.format("%.1f", pos.getX()));
            homeLocation.y = Double.parseDouble(String.format("%.1f", pos.getY()));;
            homeLocation.z = Double.parseDouble(String.format("%.1f", pos.getZ()));;
            homeLocation.world = world.getRegistryKey().getValue().toString();

            playerStorage.Homes.add(homeLocation);

            if (playerStorage.Homes.size() == 1) {
                playerStorage.DefaultHome = homeName;
            }

            StorageSaver(storage);
        } else {
            player.sendMessage(Text.literal("Home Already Exists!"), true);
        }
    }

    public static void GoHome(ServerPlayerEntity player, String homeName) {
        homeName = homeName.toLowerCase();
        StorageClass.Player playerStorage = GetPlayerStorage(player.getUuidAsString()).playerStorage;

        if (homeName.isEmpty()) {
            homeName = playerStorage.DefaultHome;
            // todo : can cause error if home doesnt exist
        }

        boolean foundHome = false;
        boolean foundWorld = false;

        // find correct home
        for (StorageClass.Player.Home currentHome : playerStorage.Homes) {
            if (Objects.equals(currentHome.name, homeName)){
                foundHome = true;

                // find correct world
                for (ServerWorld currentWorld : Objects.requireNonNull(player.getServer()).getWorlds()) {
                    if (Objects.equals(currentWorld.getRegistryKey().getValue().toString(), currentHome.world)) {
                        Teleporter(player, currentWorld, new Vec3d(currentHome.x, currentHome.y, currentHome.z));
                        foundWorld = true;
                        break;
                    }
                }
            }
        }

        if (!foundHome) {
            player.sendMessage(Text.literal("Home Not Found!"), true);
        } else if (!foundWorld) {
            player.sendMessage(Text.literal("World Not Found!"), true);
        }

    }

    public static void DeleteHome(ServerPlayerEntity player, String homeName) throws Exception {
        homeName = homeName.toLowerCase();
        PlayerStorageResult storages = GetPlayerStorage(player.getUuidAsString());
        StorageClass storage = storages.storage;
        StorageClass.Player playerStorage = storages.playerStorage;

        StorageClass.Player.Home homeToDelete = null;

        // get correct home
        for (StorageClass.Player.Home currentHome : playerStorage.Homes) {
            if (Objects.equals(currentHome.name, homeName)){
                homeToDelete = currentHome;
                break;
            }
        }

        if (Objects.nonNull(homeToDelete)) {
            playerStorage.Homes.remove(homeToDelete);
            StorageSaver(storage);
        } else {
            player.sendMessage(Text.literal("Home Not Found!"), true);
        }
    }

    public static void RenameHome(ServerPlayerEntity player, String homeName, String newHomeName) throws Exception {
        homeName = homeName.toLowerCase();
        newHomeName = newHomeName.toLowerCase();

        PlayerStorageResult storages = GetPlayerStorage(player.getUuidAsString());
        StorageClass storage = storages.storage;
        StorageClass.Player playerStorage = storages.playerStorage;

        StorageClass.Player.Home homeToRename = null;
        boolean newNameNotFound = true;

        // check for duplicates
        for (StorageClass.Player.Home currentHome : playerStorage.Homes) {
            if (Objects.equals(currentHome.name, newHomeName)) {
                newNameNotFound = false;
                break;
            }
        }

        if (newNameNotFound) {
            // get correct home
            for (StorageClass.Player.Home currentHome : playerStorage.Homes) {
                if (Objects.equals(currentHome.name, homeName)){
                    homeToRename = currentHome;
                    break;
                }
            }

            if (Objects.nonNull(homeToRename)) {
                if (Objects.equals(playerStorage.DefaultHome, homeToRename.name)) {
                    playerStorage.DefaultHome = newHomeName;
                }

                homeToRename.name = newHomeName;
                StorageSaver(storage);
            } else {
                player.sendMessage(Text.literal("Home Not Found!"), true);
            }
        } else {
            player.sendMessage(Text.literal("Home Already Exists!"), true);
        }

    }

    public static void PrintHomes(ServerPlayerEntity player) {
        StorageClass.Player playerStorage = GetPlayerStorage(player.getUuidAsString()).playerStorage;
        boolean anyHomes = false;

        for (StorageClass.Player.Home currenthome : playerStorage.Homes) {
            if (!anyHomes) {
                player.sendMessage(Text.literal("Homes: \n").formatted(Formatting.YELLOW, Formatting.BOLD), false);
                anyHomes = true;
            }

            String name = String.format("  - %s", currenthome.name);
            String nameDefault = " (Default)";


            String coords = String.format(" [X%.1f Y%.1f Z%.1f]", currenthome.x, currenthome.y, currenthome.z);
            String dimension = String.format(" [%s]", currenthome.world);

            if (Objects.equals(currenthome.name, playerStorage.DefaultHome)) {
                player.sendMessage(Text.literal(name).formatted(Formatting.AQUA)
                        .append(Text.literal(nameDefault).formatted(Formatting.AQUA, Formatting.BOLD)),
                        false
                );
            } else {
                player.sendMessage(Text.literal(name).formatted(Formatting.AQUA), false);
            }


            player.sendMessage(Text.literal("     |").formatted(Formatting.AQUA)
                    .append(Text.literal(coords).formatted(Formatting.LIGHT_PURPLE).styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, String.format("X%.2f Y%.2f Z%.2f", currenthome.x, currenthome.y, currenthome.z)))))
                    .append(Text.literal(dimension).formatted(Formatting.DARK_PURPLE).styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, currenthome.world)))),
                    false
            );

            player.sendMessage(Text.literal("     |").formatted(Formatting.AQUA)
                    .append(Text.literal(" [Tp]").formatted(Formatting.GREEN).styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/home %s", currenthome.name)))))
                    .append(Text.literal(" [Rename]").formatted(Formatting.BLUE).styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, String.format("/renamehome %s ", currenthome.name)))))
                    .append(Text.literal(" [Delete]\n").formatted(Formatting.RED).styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, String.format("/delhome %s", currenthome.name))))),
                    false
            );
        }

        if (!anyHomes) {
            player.sendMessage(Text.literal("No homes set"), true);
        }
    }

    public static void SetDefaultHome(ServerPlayerEntity player, String homeName) throws Exception {
        homeName = homeName.toLowerCase();
        PlayerStorageResult storages = GetPlayerStorage(player.getUuidAsString());
        StorageClass storage = storages.storage;
        StorageClass.Player playerStorage = storages.playerStorage;

        boolean homeExists = false;

        // check if home exists
        for (StorageClass.Player.Home currentHome : playerStorage.Homes) {
            if (Objects.equals(currentHome.name, homeName)){
                homeExists = true;
                break;
            }
        }

        if (homeExists) {
            if (Objects.equals(playerStorage.DefaultHome, homeName)) {
                player.sendMessage(Text.literal("Home is already set as default!"), true);

            } else {
                playerStorage.DefaultHome = homeName;
                StorageSaver(storage);
            }
        } else {
            player.sendMessage(Text.literal("Home not found!"), true);
        }
    }


    public static void ToDeathLocation(ServerPlayerEntity player) {
        StorageClass.Player playerStorage = GetPlayerStorage(player.getUuidAsString()).playerStorage;

        Vec3d pos = new Vec3d(playerStorage.deathLocation.x, playerStorage.deathLocation.y, playerStorage.deathLocation.z);

        boolean found = false;
        for (ServerWorld currentWorld : Objects.requireNonNull(player.getServer()).getWorlds()) {
            if (Objects.equals(currentWorld.getRegistryKey().getValue().toString(), playerStorage.deathLocation.world)) {
                Teleporter(player, currentWorld, pos);
                found = true;
                break;
            }
        }

        if (!found) {
            player.sendMessage(Text.literal("No Location Found!"), true);
        }
    }

    public static void tpaCommandHandler(ServerPlayerEntity FromPlayer, ServerPlayerEntity ToPlayer,  boolean here) {

        if (FromPlayer == ToPlayer) {
            FromPlayer.sendMessage(Text.literal("Well, that was easy").formatted(Formatting.AQUA),true);

        } else {
            String ToMessage;
            String FromMessage;

            // Store da request
            tpaArrayClass tpaRequest = new tpaArrayClass();
            tpaRequest.InitPlayer = FromPlayer;
            tpaRequest.RecPlayer = ToPlayer;
            tpaRequest.here = here;
            tpaList.add(tpaRequest);

            if (here) {
                ToMessage = "TpaHere Request Received From ";
                FromMessage = "TpaHere Request Send to ";

            } else {
                ToMessage = "Tpa Request Received From ";
                FromMessage = "Tpa Request Send to ";
            }

            String ToPlayerString = Objects.requireNonNull(ToPlayer.getName().getLiteralString());
            String FromPlayerString = Objects.requireNonNull(FromPlayer.getName().getLiteralString());

            FromPlayer.sendMessage(Text.literal(FromMessage).formatted(Formatting.AQUA)
                            .append(Text.literal(ToPlayerString).formatted(Formatting.AQUA, Formatting.BOLD))
//                            .append(Text.literal("\n[Cancel]").formatted(Formatting.BLUE, Formatting.BOLD))
                    ,true
            );

            ToPlayer.sendMessage(Text.literal(ToMessage).formatted(Formatting.AQUA)
                            .append(Text.literal(FromPlayerString).formatted(Formatting.AQUA, Formatting.BOLD))
                            .append(Text.literal("\n[Accept]").formatted(Formatting.GREEN, Formatting.BOLD).styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/tpaaccept %s", FromPlayerString)))))
                            .append(Text.literal(" [Deny]").formatted(Formatting.RED, Formatting.BOLD).styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/tpadeny %s", FromPlayerString))))),
                    false
            );

            Timer timer = new Timer();
            timer.schedule(
                    new TimerTask() {
                        @Override
                        public void run() {
                            boolean successful = tpaList.remove(tpaRequest);
                            if (successful) {
                                if (here) {
                                    FromPlayer.sendMessage(Text.literal("TpaHere Request Expired").formatted(Formatting.AQUA),true);
                                } else {
                                    FromPlayer.sendMessage(Text.literal("Tpa Request Expired").formatted(Formatting.AQUA),true);
                                }
                            }
                        }
                    }, 30 * 1000
            );
        }
    }

    public static void tpaAccept(ServerPlayerEntity FromPlayer, ServerPlayerEntity ToPlayer) {
        if (FromPlayer == ToPlayer) {
            FromPlayer.sendMessage(Text.literal("No").formatted(Formatting.AQUA),true);
        } else {
            Optional<tpaArrayClass> tpaStorage = tpaList.stream()
                    .filter(tpa -> Objects.equals(ToPlayer, tpa.InitPlayer))
                    .filter(tpa -> Objects.equals(FromPlayer, tpa.RecPlayer))
                    .findFirst();

            if (tpaStorage.isPresent()) {
                if (tpaStorage.get().here) {
                    FromPlayer.sendMessage(Text.literal("Teleporting"),true);
                    ToPlayer.sendMessage(Text.literal("Request Accepted"),true);

                    Teleporter(FromPlayer, ToPlayer.getServerWorld(), ToPlayer.getPos());
                } else {
                    ToPlayer.sendMessage(Text.literal("Teleporting"),true);
                    FromPlayer.sendMessage(Text.literal("Request Accepted"),true);

                    Teleporter(ToPlayer, FromPlayer.getServerWorld(), FromPlayer.getPos());
                }

                tpaList.remove(tpaStorage.get());
            } else {
                FromPlayer.sendMessage(Text.literal("No Requests found!").formatted(Formatting.AQUA),true);
            }
        }
    }

    public static void tpaDeny(ServerPlayerEntity FromPlayer, ServerPlayerEntity ToPlayer) {
        if (FromPlayer == ToPlayer) {
            FromPlayer.sendMessage(Text.literal("No").formatted(Formatting.AQUA),true);
        } else {
            Optional<tpaArrayClass> tpaStorage = tpaList.stream()
                    .filter(tpa -> Objects.equals(ToPlayer, tpa.InitPlayer))
                    .filter(tpa -> Objects.equals(FromPlayer, tpa.RecPlayer))
                    .findFirst();

            if (tpaStorage.isPresent()) {
                tpaList.remove(tpaStorage.get());

                if (tpaStorage.get().here) {
                    ToPlayer.sendMessage(Text.literal("Request Denied").formatted(Formatting.AQUA),true);
                } else {
                    FromPlayer.sendMessage(Text.literal("Request Denied").formatted(Formatting.AQUA),true);
                }
            } else {
                FromPlayer.sendMessage(Text.literal("No Requests found!").formatted(Formatting.AQUA),true);
            }
        }
    }


    public static void DeathLocationUpdater(Vec3d pos, ServerWorld world, String UUID) throws Exception {
        PlayerStorageResult storages = GetPlayerStorage(UUID);

        StorageClass storage = storages.storage;
        StorageClass.Player playerStorage = storages.playerStorage;

        playerStorage.deathLocation.x = Double.parseDouble(String.format("%.1f", pos.getX()));;
        playerStorage.deathLocation.y = Double.parseDouble(String.format("%.1f", pos.getY()));;
        playerStorage.deathLocation.z = Double.parseDouble(String.format("%.1f", pos.getZ()));;
        playerStorage.deathLocation.world = world.getRegistryKey().getValue().toString();

        StorageSaver(storage);
    }
}
