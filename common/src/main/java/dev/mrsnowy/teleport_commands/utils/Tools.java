package dev.mrsnowy.teleport_commands.utils;

import com.google.gson.*;

import java.io.*;
import java.util.*;
import java.util.stream.StreamSupport;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import static dev.mrsnowy.teleport_commands.utils.Language.getTranslation;


public class Tools {
    private static final Set<String> unsafeCollisionFreeBlocks = Set.of("block.minecraft.lava", "block.minecraft.flowing_lava", "block.minecraft.end_portal", "block.minecraft.end_gateway","block.minecraft.fire", "block.minecraft.soul_fire", "block.minecraft.powder_snow", "block.minecraft.nether_portal");

    // checks a 7x7x7 location around the player in order to find a safe place to teleport them to.
    public static Optional<BlockPos> getSafeBlockPos(BlockPos blockPos, ServerLevel world) {
        int row = 1;
        int rows = 3;

        int blockPosX = blockPos.getX();
        int blockPosY = blockPos.getY();
        int blockPosZ = blockPos.getZ();

        if (isBlockPosSafe(blockPos, world)) {
            return Optional.of(blockPos); // safe location found!

        } else {
            // find a safe location in an x row radius
            while (row <= rows) {
    //            TeleportCommands.LOGGER.info("currently doing row " + row + " of " + rows); //debug
                for (int z = -row; z <= row; z++) {
                    for (int x = -row; x <= row; x++) {
                        for (int y = -row; y <= row; y++) {

                            // checks if we are on the outer layer of the row, not on the inside
                            if ((x == -row || x == row) || (z == -row || z == row) || (y == -row || y == row)) {

                                // calculate a new blockPos based on the offset we generated
                                BlockPos newPos = new BlockPos(blockPosX + x, blockPosY + y, blockPosZ + z);

                                if (isBlockPosSafe(newPos, world)) {
//                                    return Optional.of(new Vec3(newPos.getX() + 0.5, newPos.getY(), newPos.getZ() + 0.5)); // safe location found!
                                    return Optional.of(newPos);
                                }
                            }
                        }
                    }
                }

                row++;
            }

            // no safe location
            return Optional.empty(); // no safe location found!
        }
    }

    public static void sendSafetyWarning(ServerPlayer player, String command) {
        // asks the player if they want to teleport anyway
        player.displayClientMessage(
                Component.empty()
                        .append(getTranslation("commands.teleport_commands.common.noSafeLocation", player)
                                .withStyle(ChatFormatting.RED, ChatFormatting.BOLD)
                        )
                        .append("\n")
                        .append(getTranslation("commands.teleport_commands.common.safetyIsForLosers", player)
                                .withStyle(ChatFormatting.WHITE)
                        )
                        .append("\n")
                        .append(getTranslation("commands.teleport_commands.common.forceTeleport", player)
                                .withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD)
                                .withStyle(style -> style.withClickEvent(new ClickEvent.RunCommand(command)))
                        )
                        .append("\n"), false);
    }

    // Gets the ids of all the worlds
    public static List<String> getWorldIds(MinecraftServer server) {
        return StreamSupport.stream(server.getAllLevels().spliterator(), false)
                .map(level -> level.dimension().location().toString())
                .toList();
    }


    // checks if a BlockPos is safe, used by the teleportSafetyChecker.
    private static boolean isBlockPosSafe(BlockPos bottomPlayer, ServerLevel world) {

        // get the block below the player
        BlockPos belowPlayer = new BlockPos(bottomPlayer.getX(), bottomPlayer.getY() -1, bottomPlayer.getZ()); // below the player
        String belowPlayerId = world.getBlockState(belowPlayer).getBlock().getDescriptionId(); // below the player

        // get the bottom of the player
        String BottomPlayerId = world.getBlockState(bottomPlayer).getBlock().getDescriptionId(); // bottom of player

        // get the top of the player
        BlockPos TopPlayer = new BlockPos(bottomPlayer.getX(), bottomPlayer.getY() + 1, bottomPlayer.getZ()); // top of player
        String TopPlayerId = world.getBlockState(TopPlayer).getBlock().getDescriptionId(); // top of player


        // check if the block position isn't safe
        if ((belowPlayerId.equals("block.minecraft.water") || !world.getBlockState(belowPlayer).getCollisionShape(world, belowPlayer).isEmpty()) // check if the player is going to fall on teleport
            && (world.getBlockState(bottomPlayer).getCollisionShape(world, bottomPlayer).isEmpty() && !unsafeCollisionFreeBlocks.contains(BottomPlayerId)) // check if it is a collision free block that isn't dangerous
            && (!unsafeCollisionFreeBlocks.contains(TopPlayerId))) // check if it is a dangerous collision free block, if it is solid then the player crawls
        {
            return true; // it's safe
        }
        return false; // it's not safe!
    }

    /// This function reloads "reloadable resources" which includes commands
    public static void reloadResources(MinecraftServer server) {
        Collection<String> collection = server.getPackRepository().getSelectedIds();
        server.reloadResources(collection);
    }
}
