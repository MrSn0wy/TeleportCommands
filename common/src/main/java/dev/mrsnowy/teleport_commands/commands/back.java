package dev.mrsnowy.teleport_commands.commands;

import com.mojang.brigadier.arguments.BoolArgumentType;
import dev.mrsnowy.teleport_commands.TeleportCommands;

import java.util.*;

import dev.mrsnowy.teleport_commands.storage.DeathLocationStorage;
import dev.mrsnowy.teleport_commands.common.DeathLocation;
import dev.mrsnowy.teleport_commands.utils.tools;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import static dev.mrsnowy.teleport_commands.utils.tools.*;
import static net.minecraft.commands.Commands.argument;

public class back {

    public static void register(Commands commandManager) {

        commandManager.getDispatcher().register(Commands.literal("back")
            .requires(source -> source.getPlayer() != null)
            .executes(context -> {
                final ServerPlayer player = context.getSource().getPlayerOrException();

                try {
                    ToDeathLocation(player, false);

                } catch (Exception e) {
                    TeleportCommands.LOGGER.error("Error while going back! => ", e);
                    player.displayClientMessage(getTranslatedText("commands.teleport_commands.common.error", player).withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true);
                    return 1;
                }
                return 0;
            })
            .then(argument("Disable Safety", BoolArgumentType.bool())
                .requires(source -> source.getPlayer() != null)
                .executes(context -> {
                    final boolean safety = BoolArgumentType.getBool(context, "Disable Safety");
                    final ServerPlayer player = context.getSource().getPlayerOrException();

                    try {
                        ToDeathLocation(player, safety);

                    } catch (Exception e) {
                        TeleportCommands.LOGGER.error("Error while going back! => ", e);
                        player.displayClientMessage(getTranslatedText("commands.teleport_commands.common.error", player).withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true);
                        return 1;
                    }
                    return 0;
                }))
        );
    }


    // -----


    // Gets the DeathLocation of the player and teleports the player to it
    private static void ToDeathLocation(ServerPlayer player, boolean safetyDisabled) throws Exception {

        Optional<DeathLocation> optionalDeathLocation = DeathLocationStorage.getDeathLocation(player.getStringUUID());

        if (optionalDeathLocation.isEmpty()) {
            player.displayClientMessage(getTranslatedText("commands.teleport_commands.common.noLocation", player)
                    .withStyle(ChatFormatting.RED), true);
            return;
        }

        DeathLocation deathLocation = optionalDeathLocation.get();

        // Get the world, otherwise give a warning and error message
        Optional<ServerLevel> optionalWorld = deathLocation.getWorld();

        if (optionalWorld.isEmpty()) {
            TeleportCommands.LOGGER.warn("({}) Error while going back! \nCouldn't find a world with the id: \"{}\" \nAvailable worlds: {}",
                    player.getName().getString(),
                    deathLocation.getWorldString(),
                    tools.getWorldIds());

            player.displayClientMessage(getTranslatedText("commands.teleport_commands.common.worldNotFound", player)
                    .withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true);

            return;
        }

        ServerLevel deathLocationWorld = optionalWorld.get();
        BlockPos teleportBlockPos;

        // Sets the teleportBlockPos based on if it should do safety checking
        if (!safetyDisabled) {
            Optional<BlockPos> safeBlockPos = getSafeBlockPos(deathLocation.getBlockPos(), deathLocationWorld);

            // Check if there is a safe BlockPos
            if (safeBlockPos.isPresent()) {
                teleportBlockPos = safeBlockPos.get();

            } else {
                // asks the player if they want to teleport anyway
                player.displayClientMessage(getTranslatedText("commands.teleport_commands.common.noSafeLocation", player).withStyle(ChatFormatting.RED, ChatFormatting.BOLD), false);
                player.displayClientMessage(getTranslatedText("commands.teleport_commands.common.safetyIsForLosers", player).withStyle(ChatFormatting.AQUA), false);
                player.displayClientMessage(getTranslatedText("commands.teleport_commands.common.forceTeleport", player).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD)
                        .withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/back true"))),false);
                return;
            }

        } else {
            // no checking needed, just set it.
            teleportBlockPos = deathLocation.getBlockPos();
        }

        // check if the player is already at this location (in the same world)
        if (player.blockPosition().equals(teleportBlockPos) && player.level() == deathLocationWorld) {
            player.displayClientMessage(getTranslatedText("commands.teleport_commands.back.same", player).withStyle(ChatFormatting.AQUA), true);

        } else {
            // teleport the player!
            Vec3 teleportPos = new Vec3(teleportBlockPos.getX() + 0.5, teleportBlockPos.getY(), teleportBlockPos.getZ() + 0.5);

            player.displayClientMessage(getTranslatedText("commands.teleport_commands.back.go", player), true);
            Teleporter(player, deathLocationWorld, teleportPos);
        }
    }
}
