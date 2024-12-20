package dev.mrsnowy.teleport_commands.commands;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.datafixers.util.Pair;
import dev.mrsnowy.teleport_commands.TeleportCommands;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;
import java.util.Optional;

import static dev.mrsnowy.teleport_commands.utils.tools.*;
import static net.minecraft.commands.Commands.argument;

import static net.minecraft.world.level.Level.OVERWORLD;

public class worldspawn {

    public static void register(Commands commandManager) {
        commandManager.getDispatcher().register(Commands.literal("worldspawn")
                .requires(source -> source.getPlayer() != null)
                .executes(context -> {
                    final ServerPlayer player = context.getSource().getPlayerOrException();

                    try {
                        toWorldSpawn(player, false);

                    } catch (Exception error) {
                        TeleportCommands.LOGGER.error("Error while going back! => ", error);
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
                                toWorldSpawn(player, safety);

                            } catch (Exception error) {
                                TeleportCommands.LOGGER.error("Error while going back! => ", error);
                                player.displayClientMessage(getTranslatedText("commands.teleport_commands.common.error", player).withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true);
                                return 1;
                            }
                            return 0;
                        })));


    }

    private static void toWorldSpawn(ServerPlayer player, boolean safetyDisabled) throws NullPointerException {
        ServerLevel world = TeleportCommands.SERVER.getLevel(OVERWORLD);
        BlockPos worldSpawn = Objects.requireNonNull(world,"Overworld cannot be null").getSharedSpawnPos();

        if (!safetyDisabled) {
            Pair<Integer, Optional<Vec3>> teleportData = teleportSafetyChecker(worldSpawn, world, player);

            switch (teleportData.getFirst()) {
                case 0: // safe location found!
                    if (teleportData.getSecond().isPresent()) {
                        player.displayClientMessage(getTranslatedText("commands.teleport_commands.worldspawn.go", player), true);
                        Teleporter(player, world, teleportData.getSecond().get());
                    } else {
                        player.displayClientMessage(getTranslatedText("commands.teleport_commands.common.error", player).withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true);
                    }

                    break;
                case 1: // the location is already safe!
                    player.displayClientMessage(getTranslatedText("commands.teleport_commands.worldspawn.same", player).withStyle(ChatFormatting.AQUA), true);
                    break;
                case 2: // no safe location found!

                    player.displayClientMessage(getTranslatedText("commands.teleport_commands.common.noSafeLocation", player).withStyle(ChatFormatting.RED, ChatFormatting.BOLD), false);
                    player.displayClientMessage(getTranslatedText("commands.teleport_commands.common.safetyIsForLosers", player).withStyle(ChatFormatting.AQUA), false);
                    player.displayClientMessage(getTranslatedText("commands.teleport_commands.common.forceTeleport", player).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD)
                            .withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/worldspawn true"))),false);
                    break;
            }
        } else {
            BlockPos playerBlockPos = new BlockPos(player.getBlockX(), player.getBlockY(), player.getBlockZ());

            if (!playerBlockPos.equals(worldSpawn) || player.level() != world) {

                player.displayClientMessage(getTranslatedText("commands.teleport_commands.worldspawn.go", player), true);
                Teleporter(player, world, new Vec3(worldSpawn.getX() + 0.5, worldSpawn.getY(), worldSpawn.getZ() + 0.5));

            } else {
                player.displayClientMessage(getTranslatedText("commands.teleport_commands.worldspawn.same", player).withStyle(ChatFormatting.AQUA), true);
            }
        }
    }
}
