package dev.mrsnowy.teleport_commands.commands;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.datafixers.util.Pair;
import dev.mrsnowy.teleport_commands.TeleportCommands;
import dev.mrsnowy.teleport_commands.storage.StorageManager;

import java.util.*;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import static dev.mrsnowy.teleport_commands.storage.StorageManager.GetPlayerStorage;
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


    private static void ToDeathLocation(ServerPlayer player, boolean safetyDisabled) throws Exception {
        StorageManager.StorageClass.Player playerStorage = GetPlayerStorage(player.getStringUUID()).getSecond();

        // todo : fix... what do i need to fix LMAO

        if (playerStorage.deathLocation == null) {
            player.displayClientMessage(getTranslatedText("commands.teleport_commands.back.noLocation", player).withStyle(ChatFormatting.RED), true);
        } else {
            final BlockPos pos = new BlockPos(playerStorage.deathLocation.x, playerStorage.deathLocation.y, playerStorage.deathLocation.z);

            boolean found = false;
            for (ServerLevel currentWorld : TeleportCommands.SERVER.getAllLevels()) {

                if (Objects.equals(currentWorld.dimension().location().toString(), playerStorage.deathLocation.world)) {

                    // check if the death location isn't safe and that safety isn't enabled
                    if (!safetyDisabled) {

                        Pair<Integer, Optional<Vec3>> teleportData = teleportSafetyChecker(pos.getX(), pos.getY(), pos.getZ(), currentWorld, player);

                        switch (teleportData.getFirst()) {
                            case 0: // safe location found!
                                if (teleportData.getSecond().isPresent()) {
                                    player.displayClientMessage(getTranslatedText("commands.teleport_commands.back.go", player), true);
                                    Teleporter(player, currentWorld, teleportData.getSecond().get());
                                } else {
                                    player.displayClientMessage(getTranslatedText("commands.teleport_commands.common.error", player).withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true);
                                }

                                break;
                            case 1: // same
                                player.displayClientMessage(getTranslatedText("commands.teleport_commands.back.same", player).withStyle(ChatFormatting.AQUA), true);
                                break;
                            case 2:  // no safe location

                                player.displayClientMessage(getTranslatedText("commands.teleport_commands.common.noSafeLocation", player).withStyle(ChatFormatting.RED, ChatFormatting.BOLD), false);
                                player.displayClientMessage(getTranslatedText("commands.teleport_commands.common.safetyIsForLosers", player).withStyle(ChatFormatting.AQUA), false);
                                player.displayClientMessage(getTranslatedText("commands.teleport_commands.common.forceTeleport", player).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD)
                                        .withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/back true"))),false);
                                break;
                        }

                    } else {
                        BlockPos playerBlockPos = new BlockPos(player.getBlockX(), player.getBlockY(), player.getBlockZ());
                        if (!playerBlockPos.equals(pos) || player.level() != currentWorld) {

                            player.displayClientMessage(getTranslatedText("commands.teleport_commands.back.go", player), true);
                            Teleporter(player, currentWorld, new Vec3(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5));

                        } else {
                            player.displayClientMessage(getTranslatedText("commands.teleport_commands.back.same", player).withStyle(ChatFormatting.AQUA), true);
                        }
                    }


                    found = true;
                    break;
                }
            }

            if (!found) {
                player.displayClientMessage(getTranslatedText("commands.teleport_commands.common.noLocation", player).withStyle(ChatFormatting.RED), true);
            }
        }
    }
}
