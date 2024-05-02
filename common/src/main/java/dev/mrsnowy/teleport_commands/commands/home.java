package dev.mrsnowy.teleport_commands.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import dev.mrsnowy.teleport_commands.TeleportCommands;
import dev.mrsnowy.teleport_commands.storage.StorageManager;
import dev.mrsnowy.teleport_commands.suggestions.HomesuggestionProvider;
import java.util.Objects;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import static net.minecraft.commands.Commands.argument;
import static dev.mrsnowy.teleport_commands.storage.StorageManager.GetPlayerStorage;
import static dev.mrsnowy.teleport_commands.storage.StorageManager.StorageSaver;
import static dev.mrsnowy.teleport_commands.utils.tools.Teleporter;

public class home {
    public static void register(Commands commandManager) {

        commandManager.getDispatcher().register(Commands.literal("sethome")
                .then(argument("name", StringArgumentType.string())
                        .executes(context -> {
                            final String name = StringArgumentType.getString(context, "name");
                            ServerPlayer player = context.getSource().getPlayer();

                            if (player == null) {
                                TeleportCommands.LOGGER.error("Error while executing the command, No player found!");
                                return 1;
                            }

                            try {
                                player.displayClientMessage(Component.literal("Home Set"), true);
                                SetHome(player, name);

                            } catch (Exception e) {
                                TeleportCommands.LOGGER.error(String.valueOf(e));
                                player.displayClientMessage(Component.literal("Error Setting Home!").withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true);
                                return 1;
                            }
                            return 0;
                        })));


        commandManager.getDispatcher().register(Commands.literal("home")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayer();

                    if (player == null) {
                        TeleportCommands.LOGGER.error("Error while executing the command, No player found!");
                        return 1;
                    }

                    try {
                        player.displayClientMessage(Component.literal("Going Home"), true);
                        GoHome(player, "");

                    } catch (Exception e) {
                        TeleportCommands.LOGGER.error(String.valueOf(e));
                        player.displayClientMessage(Component.literal("Error Going Home!").withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true);
                        return 1;
                    }
                    return 0;
                })
                .then(argument("name", StringArgumentType.string()).suggests(new HomesuggestionProvider())
                        .executes(context -> {
                            final String name = StringArgumentType.getString(context, "name");
                            ServerPlayer player = context.getSource().getPlayer();

                            if (player == null) {
                                TeleportCommands.LOGGER.error("Error while executing the command, No player found!");
                                return 1;
                            }

                            try {
                                player.displayClientMessage(Component.literal("Going Home"), true);
                                GoHome(player, name);

                            } catch (Exception e) {
                                player.displayClientMessage(Component.literal("Error Going Home!").withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true);
                                TeleportCommands.LOGGER.error(String.valueOf(e));
                                return 1;
                            }
                            return 0;
                        })));

        commandManager.getDispatcher().register(Commands.literal("delhome")
                .then(argument("name", StringArgumentType.string()).suggests(new HomesuggestionProvider())
                        .executes(context -> {
                            final String name = StringArgumentType.getString(context, "name");
                            ServerPlayer player = context.getSource().getPlayer();

                            if (player == null) {
                                TeleportCommands.LOGGER.error("Error while executing the command, No player found!");
                                return 1;
                            }

                            try {
                                player.displayClientMessage(Component.literal("Home Deleted"), true);
                                DeleteHome(player, name);

                            } catch (Exception e) {
                                TeleportCommands.LOGGER.error(String.valueOf(e));
                                player.displayClientMessage(Component.literal("Error Deleting Home!").withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true);
                                return 1;
                            }
                            return 0;
                        })));

        commandManager.getDispatcher().register(Commands.literal("renamehome")
                .then(argument("name", StringArgumentType.string()).suggests(new HomesuggestionProvider())
                        .then(argument("newName", StringArgumentType.string())
                                .executes(context -> {
                                    final String name = StringArgumentType.getString(context, "name");
                                    final String newName = StringArgumentType.getString(context, "newName");
                                    ServerPlayer player = context.getSource().getPlayer();

                                    if (player == null) {
                                        TeleportCommands.LOGGER.error("Error while executing the command, No player found!");
                                        return 1;
                                    }

                                    try {
                                        player.displayClientMessage(Component.literal("Home Renamed"), true);
                                        RenameHome(player, name, newName);
                                    } catch (Exception e) {
                                        TeleportCommands.LOGGER.error(String.valueOf(e));
                                        player.displayClientMessage(Component.literal("Error Renaming Home!").withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true);
                                        return 1;
                                    }
                                    return 0;
                                }))));


        commandManager.getDispatcher().register(Commands.literal("homes")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayer();

                    if (player == null) {
                        TeleportCommands.LOGGER.error("Error while executing the command, No player found!");
                        return 1;
                    }

                    try {
                        PrintHomes(player);
                    } catch (Exception e) {
                        TeleportCommands.LOGGER.error(String.valueOf(e));
                        player.displayClientMessage(Component.literal("Error Getting Homes!").withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true);
                        return 1;
                    }
                    return 0;
                }));

        commandManager.getDispatcher().register(Commands.literal("defaulthome")
                .then(argument("name", StringArgumentType.string()).suggests(new HomesuggestionProvider())
                        .executes(context -> {
                            final String name = StringArgumentType.getString(context, "name");
                            ServerPlayer player = context.getSource().getPlayer();

                            if (player == null) {
                                TeleportCommands.LOGGER.error("Error while executing the command, No player found!");
                                return 1;
                            }

                            try {
                                SetDefaultHome(player, name);
                            } catch (Exception e) {
                                TeleportCommands.LOGGER.error(String.valueOf(e));
                                player.displayClientMessage(Component.literal("Error Changing Default Home!").withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true);
                                return 1;
                            }
                            return 0;
                        })));
    }



    private static void SetHome(ServerPlayer player, String homeName) throws Exception {
        homeName = homeName.toLowerCase();
        Vec3 pos = player.position();
        ServerLevel world = player.serverLevel();

        StorageManager.PlayerStorageResult storages = GetPlayerStorage(player.getStringUUID());
        StorageManager.StorageClass storage = storages.storage;
        StorageManager.StorageClass.Player playerStorage = storages.playerStorage;

        boolean homeNotFound = true;

        // check for duplicates
        for (StorageManager.StorageClass.Player.Home currentHome : playerStorage.Homes) {
            if (Objects.equals(currentHome.name, homeName)) {
                homeNotFound = false;
                break;
            }
        }

        if (homeNotFound) {
            // Create a new Home
            StorageManager.StorageClass.Player.Home homeLocation = new StorageManager.StorageClass.Player.Home();

            homeLocation.name = homeName;
            homeLocation.x = Double.parseDouble(String.format("%.1f", pos.x()));
            homeLocation.y = Double.parseDouble(String.format("%.1f", pos.y()));
            homeLocation.z = Double.parseDouble(String.format("%.1f", pos.z()));
            homeLocation.world = world.dimension().location().toString();

            playerStorage.Homes.add(homeLocation);

            if (playerStorage.Homes.size() == 1) {
                playerStorage.DefaultHome = homeName;
            }

            StorageSaver(storage);
        } else {
            player.displayClientMessage(Component.literal("Home Already Exists!"), true);
        }
    }

    private static void GoHome(ServerPlayer player, String homeName) throws Exception {
        homeName = homeName.toLowerCase();
        StorageManager.StorageClass.Player playerStorage = GetPlayerStorage(player.getStringUUID()).playerStorage;

        // check if there is a default exists
        if (homeName.isEmpty()) {
            if (playerStorage.DefaultHome.isEmpty()) {
                player.displayClientMessage(Component.literal("You Have No Homes!"), true);
                return;
            } else {
                homeName = playerStorage.DefaultHome;
            }
        }

        boolean foundHome = false;
        boolean foundWorld = false;

        // find correct home
        for (StorageManager.StorageClass.Player.Home currentHome : playerStorage.Homes) {
            if (Objects.equals(currentHome.name, homeName)){
                foundHome = true;

                // find correct world
                for (ServerLevel currentWorld : Objects.requireNonNull(player.getServer()).getAllLevels()) {
                    if (Objects.equals(currentWorld.dimension().location().toString(), currentHome.world)) {
                        Teleporter(player, currentWorld, new Vec3(currentHome.x, currentHome.y, currentHome.z));
                        foundWorld = true;
                        break;
                    }
                }
            }
        }

        if (!foundHome) {
            player.displayClientMessage(Component.literal("Home Not Found!"), true);
        } else if (!foundWorld) {
            player.displayClientMessage(Component.literal("World Not Found!"), true);
        }
    }

    private static void DeleteHome(ServerPlayer player, String homeName) throws Exception {
        homeName = homeName.toLowerCase();
        StorageManager.PlayerStorageResult storages = GetPlayerStorage(player.getStringUUID());
        StorageManager.StorageClass storage = storages.storage;
        StorageManager.StorageClass.Player playerStorage = storages.playerStorage;

        StorageManager.StorageClass.Player.Home homeToDelete = null;

        // get correct home
        for (StorageManager.StorageClass.Player.Home currentHome : playerStorage.Homes) {
            if (Objects.equals(currentHome.name, homeName)){
                homeToDelete = currentHome;
                break;
            }
        }

        if (Objects.nonNull(homeToDelete)) {
            playerStorage.Homes.remove(homeToDelete);
            StorageSaver(storage);
        } else {
            player.displayClientMessage(Component.literal("Home Not Found!"), true);
        }
    }

    private static void RenameHome(ServerPlayer player, String homeName, String newHomeName) throws Exception {
        homeName = homeName.toLowerCase();
        newHomeName = newHomeName.toLowerCase();

        StorageManager.PlayerStorageResult storages = GetPlayerStorage(player.getStringUUID());
        StorageManager.StorageClass storage = storages.storage;
        StorageManager.StorageClass.Player playerStorage = storages.playerStorage;

        StorageManager.StorageClass.Player.Home homeToRename = null;
        boolean newNameNotFound = true;

        // check for duplicates
        for (StorageManager.StorageClass.Player.Home currentHome : playerStorage.Homes) {
            if (Objects.equals(currentHome.name, newHomeName)) {
                newNameNotFound = false;
                break;
            }
        }

        if (newNameNotFound) {
            // get correct home
            for (StorageManager.StorageClass.Player.Home currentHome : playerStorage.Homes) {
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
                player.displayClientMessage(Component.literal("Home Not Found!"), true);
            }
        } else {
            player.displayClientMessage(Component.literal("Home Already Exists!"), true);
        }

    }

    private static void PrintHomes(ServerPlayer player) throws Exception {
        StorageManager.StorageClass.Player playerStorage = GetPlayerStorage(player.getStringUUID()).playerStorage;
        boolean anyHomes = false;

        for (StorageManager.StorageClass.Player.Home currenthome : playerStorage.Homes) {
            if (!anyHomes) {
                player.displayClientMessage(Component.literal("Homes: \n").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD), false);
                anyHomes = true;
            }

            String name = String.format("  - %s", currenthome.name);
            String nameDefault = " (Default)";


            String coords = String.format(" [X%.1f Y%.1f Z%.1f]", currenthome.x, currenthome.y, currenthome.z);
            String dimension = String.format(" [%s]", currenthome.world);

            if (Objects.equals(currenthome.name, playerStorage.DefaultHome)) {
                player.displayClientMessage(Component.literal(name).withStyle(ChatFormatting.AQUA)
                                .append(Component.literal(nameDefault).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD)),
                        false
                );
            } else {
                player.displayClientMessage(Component.literal(name).withStyle(ChatFormatting.AQUA), false);
            }


            player.displayClientMessage(Component.literal("     |").withStyle(ChatFormatting.AQUA)
                            .append(Component.literal(coords).withStyle(ChatFormatting.LIGHT_PURPLE).withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, String.format("X%.2f Y%.2f Z%.2f", currenthome.x, currenthome.y, currenthome.z)))))
                            .append(Component.literal(dimension).withStyle(ChatFormatting.DARK_PURPLE).withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, currenthome.world)))),
                    false
            );

            player.displayClientMessage(Component.literal("     |").withStyle(ChatFormatting.AQUA)
                            .append(Component.literal(" [Tp]").withStyle(ChatFormatting.GREEN).withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/home %s", currenthome.name)))))
                            .append(Component.literal(" [Rename]").withStyle(ChatFormatting.BLUE).withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, String.format("/renamehome %s ", currenthome.name)))))
                            .append(Component.literal(" [Delete]\n").withStyle(ChatFormatting.RED).withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, String.format("/delhome %s", currenthome.name))))),
                    false
            );
        }

        if (!anyHomes) {
            player.displayClientMessage(Component.literal("No homes set"), true);
        }
    }

    private static void SetDefaultHome(ServerPlayer player, String homeName) throws Exception {
        homeName = homeName.toLowerCase();
        StorageManager.PlayerStorageResult storages = GetPlayerStorage(player.getStringUUID());
        StorageManager.StorageClass storage = storages.storage;
        StorageManager.StorageClass.Player playerStorage = storages.playerStorage;

        boolean homeExists = false;

        // check if home exists
        for (StorageManager.StorageClass.Player.Home currentHome : playerStorage.Homes) {
            if (Objects.equals(currentHome.name, homeName)){
                homeExists = true;
                break;
            }
        }

        if (homeExists) {
            if (Objects.equals(playerStorage.DefaultHome, homeName)) {
                player.displayClientMessage(Component.literal("Home is already set as default!"), true);

            } else {
                playerStorage.DefaultHome = homeName;
                StorageSaver(storage);
            }
        } else {
            player.displayClientMessage(Component.literal("Home not found!"), true);
        }
    }
}
