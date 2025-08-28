package dev.mrsnowy.teleport_commands.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import dev.mrsnowy.teleport_commands.Constants;

import java.util.*;

import dev.mrsnowy.teleport_commands.storage.DeathLocationStorage;
import dev.mrsnowy.teleport_commands.common.DeathLocation;
import dev.mrsnowy.teleport_commands.utils.tools;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import static dev.mrsnowy.teleport_commands.utils.tools.*;
import static net.minecraft.commands.Commands.argument;

public class back {

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {

        commandDispatcher.register(Commands.literal("back")
            .requires(source -> source.getPlayer() != null)
            .executes(context -> {
                final ServerPlayer player = context.getSource().getPlayerOrException();

                try {
                    ToDeathLocation(player, false);

                } catch (Exception e) {
                    Constants.LOGGER.error("Error while going back! => ", e);
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
                        Constants.LOGGER.error("Error while going back! => ", e);
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
        DeathLocation deathLocation = DeathLocationStorage
                .getDeathLocation(player.getStringUUID())
                .orElse(null);

        if (deathLocation == null) {
            player.displayClientMessage(getTranslatedText("commands.teleport_commands.common.noLocation", player).withStyle(ChatFormatting.RED), true);
            return;
        }

        // Get the world, otherwise give a warning and error message
        ServerLevel deathLocationWorld = deathLocation.getWorld().orElse(null);

        if (deathLocationWorld == null) {
            Constants.LOGGER.warn("({}) Error while going back! \nCouldn't find a world with the id: \"{}\" \nAvailable worlds: {}",
                    player.getName().getString(),
                    deathLocation.getWorldString(),
                    tools.getWorldIds());

            player.displayClientMessage(getTranslatedText("commands.teleport_commands.common.worldNotFound", player)
                    .withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true);

            return;
        }

        BlockPos teleportBlockPos;

        // Sets the teleportBlockPos based on if it should do safety checking
        if (!safetyDisabled) {
            Optional<BlockPos> safeBlockPos = getSafeBlockPos(deathLocation.getBlockPos(), deathLocationWorld);

            // Check if there is a safe BlockPos
            if (safeBlockPos.isEmpty()) {
                // asks the player if they want to teleport anyway
                player.displayClientMessage(
                        Component.empty()
                                .append(getTranslatedText("commands.teleport_commands.common.noSafeLocation", player)
                                        .withStyle(ChatFormatting.RED, ChatFormatting.BOLD)
                                )
                                .append("\n")
                                .append(getTranslatedText("commands.teleport_commands.common.safetyIsForLosers", player)
                                        .withStyle(ChatFormatting.WHITE)
                                )
                                .append("\n")
                                .append(getTranslatedText("commands.teleport_commands.common.forceTeleport", player)
                                        .withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD)
                                        .withStyle(style -> style.withClickEvent(new ClickEvent.RunCommand("/back true")))
                                )
                                .append("\n"), false);
                return;

            } else {
                teleportBlockPos = safeBlockPos.get();
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
            tools.Teleporter(player, deathLocationWorld, teleportPos);
        }
    }
}
