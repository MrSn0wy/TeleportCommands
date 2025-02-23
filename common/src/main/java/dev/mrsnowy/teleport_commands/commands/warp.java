package dev.mrsnowy.teleport_commands.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import dev.mrsnowy.teleport_commands.TeleportCommands;
import dev.mrsnowy.teleport_commands.common.NamedLocation;
import dev.mrsnowy.teleport_commands.suggestions.WarpSuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Optional;

import static dev.mrsnowy.teleport_commands.storage.StorageManager.*;
import static dev.mrsnowy.teleport_commands.utils.tools.Teleporter;
import static dev.mrsnowy.teleport_commands.utils.tools.getTranslatedText;
import static net.minecraft.commands.Commands.argument;

public class warp {
    public static void register(Commands commandManager) {

        commandManager.getDispatcher().register(Commands.literal("setwarp")
                .requires(source ->
                        source.getPlayer() != null &&
                        source.hasPermission(4)
                )
                .then(argument("name", StringArgumentType.string())
                        .executes(context -> {
                            final String name = StringArgumentType.getString(context, "name");
                            final ServerPlayer player = context.getSource().getPlayerOrException();

                            try {
                                SetWarp(player, name);

                            } catch (Exception e) {
                                TeleportCommands.LOGGER.error("Error while setting the warp!", e);
                                player.displayClientMessage(getTranslatedText("commands.teleport_commands.warp.setError", player).withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true);
                                return 1;
                            }
                            return 0;
                        })));

        commandManager.getDispatcher().register(Commands.literal("warp")
                .requires(source -> source.getPlayer() != null)
                .then(argument("name", StringArgumentType.string()).suggests(new WarpSuggestionProvider())
                        .executes(context -> {
                            final String name = StringArgumentType.getString(context, "name");
                            final ServerPlayer player = context.getSource().getPlayerOrException();

                            try {
                                GoToWarp(player, name);

                            } catch (Exception e) {
                                TeleportCommands.LOGGER.error("Error while going to the warp!", e);
                                player.displayClientMessage(getTranslatedText("commands.teleport_commands.warp.goError", player).withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true);
                                return 1;
                            }
                            return 0;
                        })));

        commandManager.getDispatcher().register(Commands.literal("delwarp")
                .requires(source ->
                        source.getPlayer() != null &&
                        source.hasPermission(4)
                )
                .then(argument("name", StringArgumentType.string()).suggests(new WarpSuggestionProvider())
                        .executes(context -> {
                            final String name = StringArgumentType.getString(context, "name");
                            final ServerPlayer player = context.getSource().getPlayerOrException();

                            try {
                                DeleteWarp(player, name);

                            } catch (Exception e) {
                                TeleportCommands.LOGGER.error("Error while deleting to the warp!", e);
                                player.displayClientMessage(getTranslatedText("commands.teleport_commands.warp.deleteError", player).withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true);
                                return 1;
                            }
                            return 0;
                        })));

        commandManager.getDispatcher().register(Commands.literal("renamewarp")
                .requires(source ->
                        source.getPlayer() != null &&
                        source.hasPermission(4)
                )
                .then(argument("name", StringArgumentType.string()).suggests(new WarpSuggestionProvider())
                .then(argument("newName", StringArgumentType.string())
                        .executes(context -> {
                            final String name = StringArgumentType.getString(context, "name");
                            final String newName = StringArgumentType.getString(context, "newName");
                            final ServerPlayer player = context.getSource().getPlayerOrException();

                            try {
                                RenameWarp(player, name, newName);

                            } catch (Exception e) {
                                TeleportCommands.LOGGER.error("Error while renaming the warp!", e);
                                player.displayClientMessage(getTranslatedText("commands.teleport_commands.warp.renameError", player).withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true);
                                return 1;
                            }
                            return 0;
                        }))));

        commandManager.getDispatcher().register(Commands.literal("warps")
                .requires(source -> source.getPlayer() != null)
                .executes(context -> {
                    final ServerPlayer player = context.getSource().getPlayerOrException();

                    try {
                        PrintWarps(player);

                    } catch (Exception e) {
                        TeleportCommands.LOGGER.error("Error while printing warps!", e);
                        player.displayClientMessage(getTranslatedText("commands.teleport_commands.warps.error", player).withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true);
                        return 1;
                    }
                    return 0;
                }));
    }


    private static void SetWarp(ServerPlayer player, String warpName) throws Exception {
        warpName = warpName.toLowerCase();

        BlockPos blockPos = new BlockPos(player.getBlockX(), player.getBlockY(), player.getBlockZ());
        String worldString = player.serverLevel().dimension().location().toString();

        // Check if warp already exists
        if ( STORAGE.getWarp(warpName).isEmpty() ) {
            player.displayClientMessage(getTranslatedText("commands.teleport_commands.warp.exists", player).withStyle(ChatFormatting.RED), true);
            return;
        }

        // Create the warp
        STORAGE.setWarp(warpName, blockPos, worldString);

        // Display message that the home as been set
        player.displayClientMessage(getTranslatedText("commands.teleport_commands.warp.set", player), true);
    }

    private static void GoToWarp(ServerPlayer player, String warpName) throws Exception {
        warpName = warpName.toLowerCase();

        // Gets warp
        Optional<NamedLocation> optionalWarp = STORAGE.getWarp(warpName);
        if (optionalWarp.isEmpty()) {
            player.displayClientMessage(getTranslatedText("commands.teleport_commands.warp.notFound", player).withStyle(ChatFormatting.RED), true);
            return;
        }

        NamedLocation warp = optionalWarp.get();

        // Get the world, otherwise throw an exception
        ServerLevel warpWorld = warp.getWorld().orElseThrow(() -> {
            // todo! test this exception
            return new Exception( String.format("Couldn't find a world with the id: %s \nAvailable worlds: %s",
                    warp.getWorldString(), TeleportCommands.SERVER.getAllLevels()));
        });

        BlockPos teleportBlockPos = warp.getBlockPos();

        // Check if the player is already at this location (in the same world)
        if (player.blockPosition().equals(teleportBlockPos) && player.level() == warpWorld) {
            player.displayClientMessage(getTranslatedText("commands.teleport_commands.warp.goSame", player).withStyle(ChatFormatting.AQUA), true);

        } else {
            // Teleport the player!
            Vec3 teleportPos = new Vec3(teleportBlockPos.getX() + 0.5, teleportBlockPos.getY(), teleportBlockPos.getZ() + 0.5);

            player.displayClientMessage(getTranslatedText("commands.teleport_commands.warp.go", player), true);
            Teleporter(player, warpWorld, teleportPos);
        }
    }

    private static void DeleteWarp(ServerPlayer player, String warpName) throws Exception {
        warpName = warpName.toLowerCase();

        // todo! maybe replace with ifPresentOrElse
        // get the existing warp
        Optional<NamedLocation> optionalWarp = STORAGE.getWarp(warpName);

        if (optionalWarp.isPresent()) {
            // Delete the warp
            STORAGE.rmWarp(optionalWarp.get());

            player.displayClientMessage(getTranslatedText("commands.teleport_commands.warp.delete", player), true);

        } else {
            // the warp is not found
            player.displayClientMessage(getTranslatedText("commands.teleport_commands.warp.notFound", player).withStyle(ChatFormatting.RED), true);
        }
    }

    private static void RenameWarp(ServerPlayer player, String warpName, String newWarpName) throws Exception {
        warpName = warpName.toLowerCase();
        newWarpName = newWarpName.toLowerCase();

        // check if there is no existing warp with the new name
        if (STORAGE.getWarp(newWarpName).isPresent()) {
            player.displayClientMessage(getTranslatedText("commands.teleport_commands.warp.renameExists", player).withStyle(ChatFormatting.RED), true);
            return;
        }

        // get the existing warp
        Optional<NamedLocation> warpToRename = STORAGE.getWarp(warpName);

        if (warpToRename.isPresent()) {

            // set the new name
            warpToRename.get().setName(newWarpName);
            player.displayClientMessage(getTranslatedText("commands.teleport_commands.warp.rename", player), true);

        } else {
            // the warp is not found
            player.displayClientMessage(getTranslatedText("commands.teleport_commands.warp.notFound", player).withStyle(ChatFormatting.RED), true);
        }
    }

    private static void PrintWarps(ServerPlayer player) throws Exception {
        // Get warps
        ArrayList<NamedLocation> warps = STORAGE.getWarps();

        // Check if there are any warps lol
        if (warps.isEmpty()) {
            player.displayClientMessage(getTranslatedText("commands.teleport_commands.warp.homeless", player).withStyle(ChatFormatting.AQUA), true);
            return;
        }

        // Print the stuffz

        player.displayClientMessage(getTranslatedText("commands.teleport_commands.warps.warps", player).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)
                .append("\n"), false);

        for (NamedLocation currentWarp : warps) {

            String name = String.format("  - %s", currentWarp.getName());
            String coords = String.format("[X%d Y%d Z%d]", currentWarp.getX(), currentWarp.getY(), currentWarp.getZ());
            String dimension = String.format(" [%s]", currentWarp.getWorldString());

            player.displayClientMessage(Component.literal(name).withStyle(ChatFormatting.AQUA), false);

            player.displayClientMessage(Component.literal("     | ").withStyle(ChatFormatting.AQUA)
                            .append(Component.literal(coords)
                                    .withStyle(ChatFormatting.LIGHT_PURPLE)
                                    .withStyle(style ->
                                            style.withClickEvent(
                                                    new ClickEvent(
                                                        ClickEvent.Action.COPY_TO_CLIPBOARD,
                                                        String.format("X%d Y%d Z%d", currentWarp.getX(), currentWarp.getY(), currentWarp.getZ())
                                                    )
                                            )
                                    )
                                    //todo! test the hover
                                    .withStyle(style ->
                                            style.withHoverEvent(new HoverEvent(
                                                    HoverEvent.Action.SHOW_TEXT, getTranslatedText("commands.teleport_commands.common.hoverCopy", player)
                                            ))
                                    )
                            )
                            .append(Component.literal(dimension)
                                    .withStyle(ChatFormatting.DARK_PURPLE)
                                    .withStyle(style ->
                                            style.withClickEvent(
                                                    new ClickEvent(
                                                        ClickEvent.Action.COPY_TO_CLIPBOARD,
                                                        currentWarp.getWorldString()
                                                    )
                                            )
                                    )
                                    .withStyle(style -> style
                                            .withHoverEvent(
                                                    new HoverEvent(
                                                        HoverEvent.Action.SHOW_TEXT,
                                                        getTranslatedText("commands.teleport_commands.common.hoverCopy", player)
                                                    )
                                            )
                                    )
                            ),
                    false
            );

            // todo! maybe make this a boolean at the begging of the function.
            if (player.hasPermissions(4)) {
                player.displayClientMessage(Component.literal("     | ").withStyle(ChatFormatting.AQUA)
                                .append(getTranslatedText("commands.teleport_commands.common.tp", player)
                                        .withStyle(ChatFormatting.GREEN)
                                        .withStyle(style ->
                                                style.withClickEvent(new ClickEvent(
                                                        ClickEvent.Action.RUN_COMMAND,
                                                        String.format("/warp %s", currentWarp.getName())
                                                ))
                                        )
                                )
                                .append(" ")
                                .append(getTranslatedText("commands.teleport_commands.common.rename", player)
                                        .withStyle(ChatFormatting.BLUE)
                                        .withStyle(style -> style
                                                .withClickEvent(new ClickEvent(
                                                        ClickEvent.Action.SUGGEST_COMMAND,
                                                        String.format("/renamewarp %s ", currentWarp.getName()))
                                                )
                                        )
                                )
                                .append(" ")
                                .append(getTranslatedText("commands.teleport_commands.common.delete", player)
                                        .withStyle(ChatFormatting.RED)
                                        .withStyle(style -> style
                                                .withClickEvent(new ClickEvent(
                                                        ClickEvent.Action.SUGGEST_COMMAND,
                                                        String.format("/delwarp %s", currentWarp.getName()))
                                                )
                                        )
                                )
                                .append("\n"),
                        false
                );
            } else {
                player.displayClientMessage(Component.literal("     | ")
                                .withStyle(ChatFormatting.AQUA)
                                .append(getTranslatedText("commands.teleport_commands.common.tp", player)
                                        .withStyle(ChatFormatting.GREEN)
                                        .withStyle(style -> style
                                                .withClickEvent(new ClickEvent(
                                                        ClickEvent.Action.RUN_COMMAND,
                                                        String.format("/warp %s", currentWarp.getName()))
                                                )
                                        )
                                )
                                .append("\n"),
                        false
                );
            }
        }
    }
}
