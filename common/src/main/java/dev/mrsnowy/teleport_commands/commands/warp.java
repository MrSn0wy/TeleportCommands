package dev.mrsnowy.teleport_commands.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.datafixers.util.Pair;
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
import java.util.List;
import java.util.Objects;
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
        ServerLevel world = player.serverLevel();

        Pair<StorageClass, List<StorageClass.NamedLocation>> storages = getWarpStorage();
        StorageClass storage = storages.getFirst();
        List<StorageClass.NamedLocation> WarpStorage = storages.getSecond();

        boolean warpNotFound = true;

        // check for duplicates
        for (StorageClass.NamedLocation currentWarp : WarpStorage) {
            if (Objects.equals(currentWarp.name, warpName)) {
                warpNotFound = false;
                break;
            }
        }

        if (warpNotFound) {
            // Create a new NamedLocation
            StorageClass.NamedLocation warpData = new StorageClass.NamedLocation(warpName, blockPos, world.dimension().location().toString());
            storage.Warps.add(warpData);

            StorageSaver();
            player.displayClientMessage(getTranslatedText("commands.teleport_commands.warp.set", player), true);
        } else {
            player.displayClientMessage(getTranslatedText("commands.teleport_commands.warp.exists", player).withStyle(ChatFormatting.RED), true);
        }
    }

    private static void GoToWarp(ServerPlayer player, String warpName) throws Exception {
        warpName = warpName.toLowerCase();
        List<NamedLocation> WarpStorage = getWarpStorage().getSecond();

        boolean foundWorld = false;

        // find correct warp
        for (NamedLocation currentWarp : WarpStorage) {
            if (Objects.equals(currentWarp.name, warpName)) {

                // find correct world
                for (ServerLevel currentWorld : TeleportCommands.SERVER.getAllLevels()) {
                    if (Objects.equals(currentWorld.dimension().location().toString(), currentWarp.world)) {
                        foundWorld = true;

                        BlockPos coords = new BlockPos(currentWarp.x, currentWarp.y, currentWarp.z);
                        BlockPos playerBlockPos = new BlockPos(player.getBlockX(), player.getBlockY(), player.getBlockZ());

                        if (!playerBlockPos.equals(coords)) {
                            player.displayClientMessage(getTranslatedText("commands.teleport_commands.warp.go", player), true);
                            Teleporter(player, currentWorld, new Vec3(currentWarp.x + 0.5, currentWarp.y, currentWarp.z + 0.5));
                        } else {
                            player.displayClientMessage(getTranslatedText("commands.teleport_commands.warp.goSame", player).withStyle(ChatFormatting.AQUA), true);
                        }
                        break;
                    }
                }
            }
        }

        if (!foundWorld) {
            player.displayClientMessage(getTranslatedText("commands.teleport_commands.warp.notFound", player).withStyle(ChatFormatting.RED), true);
        }
    }

    private static void DeleteWarp(ServerPlayer player, String warpName) throws Exception {
        warpName = warpName.toLowerCase();

        // get the existing warp
        Optional<NamedLocation> optionalWarp = STORAGE.getWarp(warpName);

        if (optionalWarp.isPresent()) {
            STORAGE.rmWarp(optionalWarp.get());

        } else {
            // the warp is not found
            player.displayClientMessage(getTranslatedText("commands.teleport_commands.warp.notFound", player)
                    .withStyle(ChatFormatting.RED), true);
        }
    }

    private static void RenameWarp(ServerPlayer player, String warpName, String newWarpName) throws Exception {
        warpName = warpName.toLowerCase();
        newWarpName = newWarpName.toLowerCase();

        // get the existing warp
        Optional<NamedLocation> warp = STORAGE.getWarp(warpName);

        if (warp.isPresent()) {
            NamedLocation homeToRename = warp.get();

            // check if there is no existing warp with the new name
            if (STORAGE.getWarp(newWarpName).isEmpty()) {

                // set the new name
                homeToRename.setName(newWarpName);
                player.displayClientMessage(getTranslatedText("commands.teleport_commands.warp.rename", player), true);
            } else {

                // there is already a warp with the new name
                player.displayClientMessage(getTranslatedText("commands.teleport_commands.warp.renameExists", player).withStyle(ChatFormatting.RED), true);
            }

        } else {
            // the warp is not found
            player.displayClientMessage(getTranslatedText("commands.teleport_commands.warp.notFound", player).withStyle(ChatFormatting.RED), true);
        }

    }

    private static void PrintWarps(ServerPlayer player) throws Exception {
        ArrayList<NamedLocation> warps = STORAGE.getWarps();

        if (warps.isEmpty()) {
            player.displayClientMessage(getTranslatedText("commands.teleport_commands.warp.homeless", player).withStyle(ChatFormatting.AQUA), true);

        } else {
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
                                        .withStyle(style -> style
                                                .withClickEvent(new ClickEvent(
                                                        ClickEvent.Action.COPY_TO_CLIPBOARD,
                                                        String.format("X%d Y%d Z%d", currentWarp.getX(), currentWarp.getY(), currentWarp.getZ())
                                                ))
                                        )
                                        //todo! test the hover
                                        .withStyle(style -> style
                                                .withHoverEvent(new HoverEvent(
                                                        HoverEvent.Action.SHOW_TEXT, getTranslatedText("commands.teleport_commands.common.hoverCopy", player)
                                                ))
                                        )
                                )
                                .append(Component.literal(dimension)
                                        .withStyle(ChatFormatting.DARK_PURPLE)
                                        .withStyle(style -> style
                                                .withClickEvent(new ClickEvent(
                                                        ClickEvent.Action.COPY_TO_CLIPBOARD,
                                                        currentWarp.getWorldString()
                                                ))
                                        )
                                        .withStyle(style -> style
                                                .withHoverEvent(new HoverEvent(
                                                        HoverEvent.Action.SHOW_TEXT, getTranslatedText("commands.teleport_commands.common.hoverCopy", player)
                                                ))
                                        )
                                ),
                        false
                );

                if (player.hasPermissions(4)) {
                    player.displayClientMessage(Component.literal("     | ").withStyle(ChatFormatting.AQUA)
                                    .append(getTranslatedText("commands.teleport_commands.common.tp", player)
                                            .withStyle(ChatFormatting.GREEN)
                                            .withStyle(style -> style
                                                    .withClickEvent(new ClickEvent(
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
}
