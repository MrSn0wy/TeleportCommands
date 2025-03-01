package dev.mrsnowy.teleport_commands.commands;

import com.mojang.brigadier.arguments.BoolArgumentType;
import dev.mrsnowy.teleport_commands.TeleportCommands;
import dev.mrsnowy.teleport_commands.common.DeathLocation;
import dev.mrsnowy.teleport_commands.storage.DeathLocationStorage;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

import static dev.mrsnowy.teleport_commands.utils.tools.*;
import static net.minecraft.commands.Commands.argument;

// TODO! add option to reload registered commands!

public class main {

    public static void register(Commands commandManager) {

        commandManager.getDispatcher().register(Commands.literal("teleportcommands")
            .then(Commands.literal("help")
            .requires(source -> source.getPlayer() != null)
            .executes(context -> {
                final ServerPlayer player = context.getSource().getPlayerOrException();

                try {
                    printCommands(player);

                } catch (Exception e) {
                    TeleportCommands.LOGGER.error("Error while going back! => ", e);
                    player.displayClientMessage(getTranslatedText("commands.teleport_commands.common.error", player).withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true);
                    return 1;
                }
                return 0;
            }))
            .then(argument("Disable Safety", BoolArgumentType.bool())
                .requires(source -> source.getPlayer() != null)
                .executes(context -> {
                    final boolean safety = BoolArgumentType.getBool(context, "Disable Safety");
                    final ServerPlayer player = context.getSource().getPlayerOrException();

                    try {
//                        ToDeathLocation(player, safety);

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
    private static void printCommands(ServerPlayer player) throws Exception {

        player.displayClientMessage(Component.literal("Thank you for using Teleport Commands (V)!").withStyle(ChatFormatting.AQUA), false);
        player.displayClientMessage(Component.literal("Teleport Commands is a server-side mod that adds various teleportation related commands").withStyle(ChatFormatting.AQUA), false);

        player.displayClientMessage(Component.literal("----").withStyle(ChatFormatting.AQUA), false);

        player.displayClientMessage(Component.literal("Usage:").withStyle(ChatFormatting.AQUA), false);
    }
}
