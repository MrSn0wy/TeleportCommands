package dev.mrsnowy.teleport_commands.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import dev.mrsnowy.teleport_commands.Constants;
import dev.mrsnowy.teleport_commands.TeleportCommands;
import dev.mrsnowy.teleport_commands.utils.Tools;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;
import java.util.Optional;

import static dev.mrsnowy.teleport_commands.utils.Language.getTranslation;
import static dev.mrsnowy.teleport_commands.utils.Tools.*;
import static net.minecraft.commands.Commands.argument;

import static net.minecraft.world.level.Level.OVERWORLD;

public class worldspawn {

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register(Commands.literal("worldspawn")
                .requires(source -> source.getPlayer() != null)
                .executes(context -> {
                    final ServerPlayer player = context.getSource().getPlayerOrException();

                    try {
                        toWorldSpawn(player, false);

                    } catch (Exception error) {
                        Constants.LOGGER.error("Error while going to the worldspawn! => ", error);
                        player.displayClientMessage(getTranslation("commands.teleport_commands.common.error", player).withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true);
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
                                toWorldSpawn(player, safety);

                            } catch (Exception error) {
                                Constants.LOGGER.error("Error while going to the worldspawn! => ", error);
                                player.displayClientMessage(getTranslation("commands.teleport_commands.common.error", player).withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true);
                                return 1;
                            }
                            return 0;
                        })));


    }

    private static void toWorldSpawn(ServerPlayer player, boolean safetyDisabled) throws NullPointerException {
        // todo! make the dimension customizable
        ServerLevel world = player.server.getLevel(OVERWORLD);
        BlockPos worldSpawn = Objects.requireNonNull(world,"Overworld cannot be null!").getSharedSpawnPos();

        if (!safetyDisabled) {
            Optional<BlockPos> teleportData = getSafeBlockPos(worldSpawn, world);

            if (teleportData.isPresent()) {
                BlockPos safeBlockPos = teleportData.get();

                // check if the player is already at this location
                if (player.blockPosition().equals(safeBlockPos) && player.level() == world) {

                    player.displayClientMessage(getTranslation("commands.teleport_commands.worldspawn.same", player).withStyle(ChatFormatting.AQUA), true);
                } else {
                    Vec3 teleportPos = new Vec3(safeBlockPos.getX() + 0.5, safeBlockPos.getY(), safeBlockPos.getZ() + 0.5);
                    TeleportCommands.INSTANCE.teleporter.queue(player, world, teleportPos, "commands.teleport_commands.worldspawn.go");
                }

            } else {
                Tools.sendSafetyWarning(player, "/worldspawn true");
            }

        } else {

            if (player.blockPosition().equals(worldSpawn) && player.level() == world) {

                player.displayClientMessage(getTranslation("commands.teleport_commands.worldspawn.same", player).withStyle(ChatFormatting.AQUA), true);
            } else {
                Vec3 teleportPos = new Vec3(worldSpawn.getX() + 0.5, worldSpawn.getY(), worldSpawn.getZ() + 0.5);
                TeleportCommands.INSTANCE.teleporter.queue(player, world, teleportPos, "commands.teleport_commands.worldspawn.go");
            }
        }
    }
}
