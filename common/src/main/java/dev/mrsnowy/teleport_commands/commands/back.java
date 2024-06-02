package dev.mrsnowy.teleport_commands.commands;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.datafixers.util.Pair;
import dev.mrsnowy.teleport_commands.TeleportCommands;
import dev.mrsnowy.teleport_commands.storage.StorageManager;

import java.util.*;

import dev.mrsnowy.teleport_commands.suggestions.HomeSuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.tomlj.Toml;

import javax.swing.text.html.Option;

import static dev.mrsnowy.teleport_commands.storage.StorageManager.GetPlayerStorage;
import static dev.mrsnowy.teleport_commands.utils.tools.*;
import static net.minecraft.commands.Commands.argument;

public class back {

    public static void register(Commands commandManager) {

        commandManager.getDispatcher().register(Commands.literal("back").executes(context -> {
            ServerPlayer player = context.getSource().getPlayerOrException();

            try {
                ToDeathLocation(player, false);

            } catch (Exception e) {
                TeleportCommands.LOGGER.error(String.valueOf(e));
                player.displayClientMessage(getTranslatedText("commands.teleport_commands.common.error", player).withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true);
                return 1;
            }
            return 0;
        })
        .then(argument("Disable Safety", BoolArgumentType.bool()).executes(context -> {
                final boolean safety = BoolArgumentType.getBool(context, "Disable Safety");
                ServerPlayer player = context.getSource().getPlayerOrException();

                try {
                    ToDeathLocation(player, safety);

                } catch (Exception e) {
                    TeleportCommands.LOGGER.error(String.valueOf(e));
                    player.displayClientMessage(getTranslatedText("commands.teleport_commands.common.error", player).withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true);
                    return 1;
                }
                return 0;
            }))
        );

        commandManager.getDispatcher().register(Commands.literal("test").executes(context -> {
            ServerPlayer player = context.getSource().getPlayerOrException();


            player.displayClientMessage(Component.literal("Yellow").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD), true);
            TeleportCommands.LOGGER.info("Yellow info");
            TeleportCommands.LOGGER.warn("Yellow warn");
            TeleportCommands.LOGGER.error("Yellow error");
            TeleportCommands.LOGGER.error(String.valueOf(Toml.parse("commands.teleport_commands.back.go = \"Going Back\"")));
            Toml.parse("commands.teleport_commands.back.go = \"Going Back\"");
            return 0;
        }));
    }



    private static void ToDeathLocation(ServerPlayer player, boolean safetyDisabled) throws Exception {
        StorageManager.StorageClass.Player playerStorage = GetPlayerStorage(player.getStringUUID()).playerStorage;

        // todo : fix

        if (playerStorage.deathLocation == null) {
            player.displayClientMessage(getTranslatedText("commands.teleport_commands.back.noLocation", player).withStyle(ChatFormatting.RED), true);
        } else {
            final Vec3 pos = new Vec3(playerStorage.deathLocation.x, playerStorage.deathLocation.y, playerStorage.deathLocation.z);

            boolean found = false;
            for (ServerLevel currentWorld : Objects.requireNonNull(player.getServer()).getAllLevels()) {

                if (Objects.equals(currentWorld.dimension().location().toString(), playerStorage.deathLocation.world)) {

                    int playerX = (int) pos.x;
                    int playerY = (int) pos.y;
                    int playerZ = (int) pos.z;

                    // check if the death location isn't safe and that safety isn't enabled
                    if (!safetyDisabled) {
                        Pair<Integer, Optional<Vec3>> silly = teleportSafetyChecker(playerX, playerY, playerZ, currentWorld, player);


                        switch (silly.getFirst()) {
                            case 0: // safe!
                                if (silly.getSecond().isPresent()) {
                                    player.displayClientMessage(getTranslatedText("commands.teleport_commands.back.go", player), true);
                                    Teleporter(player, currentWorld, silly.getSecond().get());
                                } else {
                                    player.displayClientMessage(getTranslatedText("commands.teleport_commands.common.error", player).withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true);
                                }

                                break;
                            case 1: // same
                                player.displayClientMessage(getTranslatedText("commands.teleport_commands.back.same", player).withStyle(ChatFormatting.AQUA), true);
                                break;
                            case 2:  // no safe location
                                player.displayClientMessage(getTranslatedText("commands.teleport_commands.common.forceTeleport", player)
                                        .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD)
                                        .withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/back true")))
                                        ,false);
                                break;
                        }

//                    if (!safetyDisabled && isBlockPosUnsafe(new BlockPos(playerX, playerY, playerZ), currentWorld)) {
//                        int row = 1;
//                        int rows = 3;
//                        boolean safeLocationFound = false;
//
//                        // find a safe location in an x row radius
//                        whileLoop:
//                        while (row <= rows) {
////                            TeleportCommands.LOGGER.info("currently doing row " + row + " of " + rows); //debug
//
//                            for (int z = -row; z <= row; z++) {
//                                for (int x = -row; x <= row; x++) {
//                                    for (int y = -row; y <= row; y++ ) {
//
//                                        if ((x == -row || x == row) || (z == -row || z == row) || (y == -row || y == row)) {
//                                            if (!isBlockPosUnsafe(new BlockPos(playerX + x, playerY + y, playerZ + z), currentWorld)) {
//
//                                                Vec3 PlayerToTeleport = new Vec3(playerX + x + 0.5, playerY + y, playerZ + z + 0.5);
//
//                                                if (!player.getPosition(0).equals(PlayerToTeleport) || player.level() != currentWorld) {
//
//                                                    Teleporter(player, currentWorld, PlayerToTeleport);
//                                                } else {
//
//                                                }
//
//                                                safeLocationFound = true;
//                                                break whileLoop;
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//
//                            row++;
//                        }
//
//                        if (!safeLocationFound)  {
//                            player.displayClientMessage(
//                                    getTranslatedText("commands.teleport_commands.back.noSafeLocation", player)
//                                            .withStyle(ChatFormatting.RED, ChatFormatting.BOLD)
//                                    , false);
//
//                            player.displayClientMessage(
//                                    getTranslatedText("commands.teleport_commands.back.safetyIsForLosers", player).withStyle(ChatFormatting.AQUA)
//                                            .append("\n")
//                                            .append(
//                                                    getTranslatedText("commands.teleport_commands.back.forceTeleport", player)
//                                                            .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD)
//                                                            .withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/back true")))
//                                            )
//                                    ,false);
//                        }

                    } else {
                        if (!player.getPosition(0).equals(pos) || player.level() != currentWorld) {

                            player.displayClientMessage(getTranslatedText("commands.teleport_commands.back.go", player), true);
                            Teleporter(player, currentWorld, new Vec3(playerX + 0.5, playerY, playerZ + 0.5));

                        } else {
                            player.displayClientMessage(getTranslatedText("commands.teleport_commands.back.same", player).withStyle(ChatFormatting.AQUA), true);
                        }
                    }


                    found = true;
                    break;
                }
            }

            if (!found) {
                player.displayClientMessage(getTranslatedText("commands.teleport_commands.back.noLocation", player).withStyle(ChatFormatting.RED), true);
            }
        }
    }

//    private static boolean isBlockPosUnsafe(BlockPos bottomPlayer, ServerLevel world) {
//        // bottomPlayer is presumed to be the bottom of the player character
//
//        BlockPos belowPlayer = new BlockPos(bottomPlayer.getX(), bottomPlayer.getY() -1, bottomPlayer.getZ()); // below the player
//        String belowPlayerId = world.getBlockState(belowPlayer).getBlock().getDescriptionId(); // below the player
//
//        String BottomPlayerId = world.getBlockState(bottomPlayer).getBlock().getDescriptionId(); // bottom of player
//
//        BlockPos TopPlayer = new BlockPos(bottomPlayer.getX(), bottomPlayer.getY() + 1, bottomPlayer.getZ()); // top of player
//        String TopPlayerId = world.getBlockState(TopPlayer).getBlock().getDescriptionId(); // top of player
//
//
//        // check if the death location isn't safe
//        if (
//                (belowPlayerId.equals("block.minecraft.water") || !world.getBlockState(belowPlayer).getCollisionShape(world, belowPlayer).isEmpty()) // check if the player is gonna fall on teleport
//                && (world.getBlockState(bottomPlayer).getCollisionShape(world, bottomPlayer).isEmpty() && !unsafeCollisionFreeBlocks.contains(BottomPlayerId)) // check if it is a collision free block, that isnt dangerous
//                && (!unsafeCollisionFreeBlocks.contains(TopPlayerId)) // check if it is a dangerous collision free block, if it is solid then the player crawls
//        ){
//            return false; // it's safe
//        }
//        return true; // it's not safe!
//    }
}
