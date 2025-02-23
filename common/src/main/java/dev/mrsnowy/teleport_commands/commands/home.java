package dev.mrsnowy.teleport_commands.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import dev.mrsnowy.teleport_commands.TeleportCommands;
import dev.mrsnowy.teleport_commands.storage.StorageManager;
import dev.mrsnowy.teleport_commands.common.NamedLocation;
import dev.mrsnowy.teleport_commands.common.Player;
import dev.mrsnowy.teleport_commands.suggestions.HomeSuggestionProvider;

import java.util.ArrayList;
import java.util.Optional;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import static dev.mrsnowy.teleport_commands.storage.StorageManager.STORAGE;
import static dev.mrsnowy.teleport_commands.utils.tools.getTranslatedText;
import static net.minecraft.commands.Commands.argument;
import static dev.mrsnowy.teleport_commands.storage.StorageManager.StorageSaver;
import static dev.mrsnowy.teleport_commands.utils.tools.Teleporter;

public class home {
    public static void register(Commands commandManager) {

        commandManager.getDispatcher().register(Commands.literal("sethome")
                .requires(source -> source.getPlayer() != null)
                .then(argument("name", StringArgumentType.string())
                        .executes(context -> {
                            final String name = StringArgumentType.getString(context, "name");
                            final ServerPlayer player = context.getSource().getPlayerOrException();

                            try {
                                SetHome(player, name);

                            } catch (Exception e) {
                                TeleportCommands.LOGGER.error("Error while setting a home! => ", e);
                                player.displayClientMessage(getTranslatedText("commands.teleport_commands.home.setError", player).withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true);
                                return 1;
                            }
                            return 0;
                        })));


        commandManager.getDispatcher().register(Commands.literal("home")
                .requires(source -> source.getPlayer() != null)
                .executes(context -> {
                    final ServerPlayer player = context.getSource().getPlayerOrException();

                    try {
                        GoHome(player, "");

                    } catch (Exception e) {
                        TeleportCommands.LOGGER.error("Error while going home! => ", e);
                        player.displayClientMessage(getTranslatedText("commands.teleport_commands.home.goError", player).withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true);
                        return 1;
                    }
                    return 0;
                })
                .then(argument("name", StringArgumentType.string()).suggests(new HomeSuggestionProvider())
                        .requires(source -> source.getPlayer() != null)
                        .executes(context -> {
                            final String name = StringArgumentType.getString(context, "name");
                            final ServerPlayer player = context.getSource().getPlayerOrException();

                            try {
                                GoHome(player, name);

                            } catch (Exception e) {
                                TeleportCommands.LOGGER.error("Error while going to a specific home! => ", e);
                                player.displayClientMessage(getTranslatedText("commands.teleport_commands.home.goError", player).withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true);
                                return 1;
                            }
                            return 0;
                        })));

        commandManager.getDispatcher().register(Commands.literal("delhome")
                .requires(source -> source.getPlayer() != null)
                .then(argument("name", StringArgumentType.string()).suggests(new HomeSuggestionProvider())
                        .executes(context -> {
                            final String name = StringArgumentType.getString(context, "name");
                            final ServerPlayer player = context.getSource().getPlayerOrException();

                            try {
                                DeleteHome(player, name);

                            } catch (Exception e) {
                                TeleportCommands.LOGGER.error("Error while deleting a home! => ", e);
                                player.displayClientMessage(getTranslatedText("commands.teleport_commands.home.deleteError", player).withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true);
                                return 1;
                            }
                            return 0;
                        })));

        commandManager.getDispatcher().register(Commands.literal("renamehome")
                .requires(source -> source.getPlayer() != null)
                .then(argument("name", StringArgumentType.string()).suggests(new HomeSuggestionProvider())
                        .then(argument("newName", StringArgumentType.string())
                                .executes(context -> {
                                    final String name = StringArgumentType.getString(context, "name");
                                    final String newName = StringArgumentType.getString(context, "newName");
                                    final ServerPlayer player = context.getSource().getPlayerOrException();

                                    try {
                                        RenameHome(player, name, newName);

                                    } catch (Exception e) {
                                        TeleportCommands.LOGGER.error("Error while renaming a home! => ", e);
                                        player.displayClientMessage(getTranslatedText("commands.teleport_commands.home.renameError", player).withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true);
                                        return 1;
                                    }
                                    return 0;
                                }))));


        commandManager.getDispatcher().register(Commands.literal("defaulthome")
                .requires(source -> source.getPlayer() != null)
                .then(argument("name", StringArgumentType.string()).suggests(new HomeSuggestionProvider())
                        .executes(context -> {
                            final String name = StringArgumentType.getString(context, "name");
                            final ServerPlayer player = context.getSource().getPlayerOrException();

                            try {
                                SetDefaultHome(player, name);

                            } catch (Exception e) {
                                TeleportCommands.LOGGER.error("Error while setting the default home! => ", e);
                                player.displayClientMessage(getTranslatedText("commands.teleport_commands.home.defaultError", player).withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true);
                                return 1;
                            }
                            return 0;
                        })));

        commandManager.getDispatcher().register(Commands.literal("homes")
                .requires(source -> source.getPlayer() != null)
                .executes(context -> {
                    final ServerPlayer player = context.getSource().getPlayerOrException();

                    try {
                        PrintHomes(player);

                    } catch (Exception e) {
                        TeleportCommands.LOGGER.error("Error while printing the homes! => ", e);
                        player.displayClientMessage(getTranslatedText("commands.teleport_commands.homes.error", player).withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true);
                        return 1;
                    }
                    return 0;
                }));
    }


    // -----

    // Adds a new home to the homeList of a player
    private static void SetHome(ServerPlayer player, String homeName) throws Exception {
        homeName = homeName.toLowerCase();
        BlockPos blockPos = player.blockPosition();
        String worldString = player.serverLevel().dimension().location().toString();

        // Gets player storage and makes it if it doesn't exist
        Player playerStorage = StorageManager.STORAGE.addPlayer(player.getStringUUID());

        // check for duplicates
        if (playerStorage.getHome(homeName).isPresent()) {
            player.displayClientMessage(getTranslatedText("commands.teleport_commands.home.exists", player).withStyle(ChatFormatting.RED), true);
            return;
        }

        // Create a new NamedLocation
        playerStorage.setHome(homeName, blockPos, worldString);

        // Set it as the default if there are no other homes
        if (playerStorage.getHomes().size() == 1) {
            playerStorage.setDefaultHome(homeName);
        }

        // Display message that the home as been set
        player.displayClientMessage(getTranslatedText("commands.teleport_commands.home.set", player), true);
    }

    // Teleports the player to the home. It will go to the defaultHome if homeName is empty
    private static void GoHome(ServerPlayer player, String homeName) throws Exception {
        homeName = homeName.toLowerCase();

        // Get player storage
        Optional<Player> optionalPlayerStorage = STORAGE.getPlayer(player.getStringUUID());
        if (optionalPlayerStorage.isEmpty()) {
            player.displayClientMessage(getTranslatedText("commands.teleport_commands.home.homeless", player).withStyle(ChatFormatting.AQUA), true);
            return;
        }

        Player playerStorage = optionalPlayerStorage.get();

        // If homeName is empty, get the default home
        if (homeName.isEmpty()) {
            String defaultHome = playerStorage.getDefaultHome();

            if (defaultHome.isEmpty()) {
                // todo! test if this translation key works correctly!
                player.displayClientMessage(getTranslatedText("commands.teleport_commands.home.defaultNone", player).withStyle(ChatFormatting.AQUA), true);
                return;
            } else {
                homeName = defaultHome;
            }
        }

        // Get the home (if it exists)
        Optional<NamedLocation> optionalHome = playerStorage.getHome(homeName);
        if (optionalHome.isEmpty()) {
            player.displayClientMessage(getTranslatedText("commands.teleport_commands.home.notFound", player).withStyle(ChatFormatting.AQUA), true);
            return;
        }

        NamedLocation home = optionalHome.get();

        // Get the world, otherwise throw an exception
        ServerLevel homeWorld = home.getWorld().orElseThrow(() -> {
            // todo! test this exception
            return new Exception( String.format("Couldn't find a world with the id: %s \nAvailable worlds: %s",
                    home.getWorldString(), TeleportCommands.SERVER.getAllLevels()));
        });

        BlockPos teleportBlockPos = home.getBlockPos();

        // Check if the player is already at this location (in the same world)
        if (player.blockPosition().equals(teleportBlockPos) && player.level() == homeWorld) {
            player.displayClientMessage(getTranslatedText("commands.teleport_commands.home.goSame", player).withStyle(ChatFormatting.AQUA), true);

        } else {
            // Teleport the player!
            Vec3 teleportPos = new Vec3(teleportBlockPos.getX() + 0.5, teleportBlockPos.getY(), teleportBlockPos.getZ() + 0.5);

            player.displayClientMessage(getTranslatedText("commands.teleport_commands.home.go", player), true);
            Teleporter(player, homeWorld, teleportPos);
        }
    }

    private static void DeleteHome(ServerPlayer player, String homeName) throws Exception {
        homeName = homeName.toLowerCase();

        // Gets player storage
        Optional<Player> optionalPlayerStorage = STORAGE.getPlayer(player.getStringUUID());
        if (optionalPlayerStorage.isEmpty()) {
            player.displayClientMessage(getTranslatedText("commands.teleport_commands.home.homeless", player).withStyle(ChatFormatting.AQUA), true);
            return;
        }

        Player playerStorage = optionalPlayerStorage.get();

        // Get the home from the player
        Optional<NamedLocation> optionalHome = playerStorage.getHome(homeName);
        if (optionalHome.isEmpty()) {
            player.displayClientMessage(getTranslatedText("commands.teleport_commands.home.notFound", player).withStyle(ChatFormatting.AQUA), true);
            return;
        }

        // delete the home
        playerStorage.deleteHome(optionalHome.get());

        // check if it's the default home, if it is set it to the default value
        if (playerStorage.getDefaultHome().equals(homeName)) {
            playerStorage.setDefaultHome("");

            // todo! maybe ask the player if they want to set a new default home? :3
        }

        player.displayClientMessage(getTranslatedText("commands.teleport_commands.home.delete", player), true);
    }

    private static void RenameHome(ServerPlayer player, String homeName, String newHomeName) throws Exception {
        homeName = homeName.toLowerCase();
        newHomeName = newHomeName.toLowerCase();

        // Gets player storage
        Optional<Player> optionalPlayerStorage = STORAGE.getPlayer(player.getStringUUID());
        if (optionalPlayerStorage.isEmpty()) {
            player.displayClientMessage(getTranslatedText("commands.teleport_commands.home.homeless", player).withStyle(ChatFormatting.AQUA), true);
            return;
        }

        Player playerStorage = optionalPlayerStorage.get();

        // Check if there already is a home with the new name
        playerStorage.getHome("newHomeName").ifPresent(name -> {
            player.displayClientMessage(getTranslatedText("commands.teleport_commands.home.renameExists", player).withStyle(ChatFormatting.RED), true);
            return;
            // todo! does this exit correctly?
        });

        // Get the home that needs to be renamed
        Optional<NamedLocation> optionalHome = playerStorage.getHome(homeName);
        if (optionalHome.isEmpty()) {
            player.displayClientMessage(getTranslatedText("commands.teleport_commands.home.notFound", player).withStyle(ChatFormatting.RED), true);
            return;
        }

        // Rename home
        optionalHome.get().setName(newHomeName);

        // check if the current home is the default, then change it to the new name
        if (playerStorage.getDefaultHome().equals(homeName)) {
            playerStorage.setDefaultHome(newHomeName);
        }

        player.displayClientMessage(getTranslatedText("commands.teleport_commands.home.rename", player), true);
    }

    private static void SetDefaultHome(ServerPlayer player, String homeName) throws Exception {
        homeName = homeName.toLowerCase();

        // Gets player storage
        Optional<Player> optionalPlayerStorage = STORAGE.getPlayer(player.getStringUUID());
        if (optionalPlayerStorage.isEmpty()) {
            player.displayClientMessage(getTranslatedText("commands.teleport_commands.home.homeless", player).withStyle(ChatFormatting.AQUA), true);
            return;
        }

        Player playerStorage = optionalPlayerStorage.get();

        // Check if the new default home exists
        if ( playerStorage.getHome(homeName).isEmpty() ) {
            player.displayClientMessage(getTranslatedText("commands.teleport_commands.home.notFound", player).withStyle(ChatFormatting.RED), true);
            return;
        }

        // Check if the home is already the default
        if (playerStorage.getDefaultHome().equals(homeName)) {
            player.displayClientMessage(getTranslatedText("commands.teleport_commands.home.defaultSame", player).withStyle(ChatFormatting.AQUA), true);
            return;
        }

        // set the new default
        playerStorage.setDefaultHome(homeName);
        player.displayClientMessage(getTranslatedText("commands.teleport_commands.home.default", player), true);
    }

    private static void PrintHomes(ServerPlayer player) throws Exception {

        // Gets player storage
        Optional<Player> optionalPlayerStorage = STORAGE.getPlayer(player.getStringUUID());
        if (optionalPlayerStorage.isEmpty()) {
            player.displayClientMessage(getTranslatedText("commands.teleport_commands.home.homeless", player).withStyle(ChatFormatting.AQUA), true);
            return;
        }

        Player playerStorage = optionalPlayerStorage.get();

        ArrayList<NamedLocation> homes = playerStorage.getHomes();

        // Check if there are any homes lol
        if (homes.isEmpty()) {
            player.displayClientMessage(getTranslatedText("commands.teleport_commands.home.homeless", player).withStyle(ChatFormatting.AQUA), true);
            return;
        }

        // Print the stuffz

        player.displayClientMessage(getTranslatedText("commands.teleport_commands.homes.homes", player).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)
                .append("\n"), false);

        // todo! Make it send it all at once?
        for (NamedLocation currentHome : homes) {

            String name = String.format("  - %s", currentHome.getName());
            String coords = String.format("[X%d Y%d Z%d]", currentHome.getX(), currentHome.getY(), currentHome.getZ());
            String dimension = String.format(" [%s]", currentHome.getWorldString());

            // Check if the home is the default home, then add text showcasing that it is
            if (playerStorage.getDefaultHome().equals(currentHome.getName())) {
                player.displayClientMessage(Component.literal(name)
                                .withStyle(ChatFormatting.AQUA)
                                .append(" ")
                                .append(getTranslatedText("commands.teleport_commands.common.default", player)
                                        .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD)
                                ),
                        false
                );
            } else {
                player.displayClientMessage(Component.literal(name).withStyle(ChatFormatting.AQUA), false);
            }

            // Cords and dimension
            player.displayClientMessage(Component.literal("     | ")
                            .withStyle(ChatFormatting.AQUA)
                            .append(Component.literal(coords)
                                    .withStyle(ChatFormatting.LIGHT_PURPLE)
                                    .withStyle(style ->
                                            style.withClickEvent(
                                                    new ClickEvent(
                                                            ClickEvent.Action.COPY_TO_CLIPBOARD,
                                                            String.format("X%d Y%d Z%d", currentHome.getX(), currentHome.getY(), currentHome.getZ())
                                                    )
                                            )
                                    )
                            )
                            .append(Component.literal(dimension)
                                    .withStyle(ChatFormatting.DARK_PURPLE)
                                    .withStyle(style ->
                                            style.withClickEvent(
                                                    new ClickEvent(
                                                            ClickEvent.Action.COPY_TO_CLIPBOARD,
                                                            currentHome.getWorldString()
                                                    )
                                            )
                                    )
                            ),
                    false
            );

            // Teleport, rename and delete buttons
            player.displayClientMessage(Component.literal("     | ").withStyle(ChatFormatting.AQUA)
                            .append(getTranslatedText("commands.teleport_commands.common.tp", player)
                                    .withStyle(ChatFormatting.GREEN)
                                    .withStyle(style ->
                                            style.withClickEvent(
                                                    new ClickEvent(
                                                            ClickEvent.Action.RUN_COMMAND,
                                                            String.format("/home %s", currentHome.getName())
                                                    )
                                            )
                                    )
                            )
                            .append(" ")
                            .append(getTranslatedText("commands.teleport_commands.common.rename", player)
                                    .withStyle(ChatFormatting.BLUE)
                                    .withStyle(style ->
                                            style.withClickEvent(
                                                    new ClickEvent(
                                                            ClickEvent.Action.SUGGEST_COMMAND,
                                                            String.format("/renamehome %s ", currentHome.getName())
                                                    )
                                            )
                                    )
                            )
                            .append(" ")
                            .append(getTranslatedText("commands.teleport_commands.common.delete", player)
                                    .withStyle(ChatFormatting.RED)
                                    .withStyle(style ->
                                            style.withClickEvent(
                                                    new ClickEvent(
                                                            ClickEvent.Action.SUGGEST_COMMAND,
                                                            String.format("/delhome %s", currentHome.getName())
                                                    )
                                            )
                                    )
                            )
                            .append("\n"),
                    false
            );
        }

    }
}