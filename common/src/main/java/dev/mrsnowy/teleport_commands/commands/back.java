package dev.mrsnowy.teleport_commands.commands;

import dev.mrsnowy.teleport_commands.TeleportCommands;
import dev.mrsnowy.teleport_commands.storage.StorageManager;
import java.util.Objects;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import static dev.mrsnowy.teleport_commands.storage.StorageManager.GetPlayerStorage;
import static dev.mrsnowy.teleport_commands.utils.tools.Teleporter;
import static dev.mrsnowy.teleport_commands.utils.tools.getTranslatedText;

public class back {

    public static void register(Commands commandManager) {

        commandManager.getDispatcher().register(Commands.literal("back").executes(context -> {
            ServerPlayer player = context.getSource().getPlayerOrException();

            try {
                player.displayClientMessage(getTranslatedText("commands.teleport_commands.common.teleport", player), true);
                ToDeathLocation(player);
            } catch (Exception e) {
                TeleportCommands.LOGGER.error(String.valueOf(e));
                player.displayClientMessage(getTranslatedText("commands.teleport_commands.back.error", player).withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true);
                return 1;
            }
            return 0;
        }));

        commandManager.getDispatcher().register(Commands.literal("lang").executes(context -> {
            ServerPlayer player = context.getSource().getPlayerOrException();

            try {
                player.displayClientMessage(getTranslatedText("commands.teleport_commands.tpa.self", player), true);
            } catch (Exception e) {
                TeleportCommands.LOGGER.error(String.valueOf(e));
//                player.displayClientMessage(Component.translatable("commands.teleport_commands.back.error").withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true);
                return 1;
            }
            return 0;
        }));
    }



    private static void ToDeathLocation(ServerPlayer player) throws Exception {
        StorageManager.StorageClass.Player playerStorage = GetPlayerStorage(player.getStringUUID()).playerStorage;

        Vec3 pos = new Vec3(playerStorage.deathLocation.x, playerStorage.deathLocation.y, playerStorage.deathLocation.z);

        boolean found = false;
        for (ServerLevel currentWorld : Objects.requireNonNull(player.getServer()).getAllLevels()) {
            if (Objects.equals(currentWorld.dimension().location().toString(), playerStorage.deathLocation.world)) {
                if (!player.getPosition(0).equals(pos)) {
                    Teleporter(player, currentWorld, pos);
                } else {
                    player.displayClientMessage(getTranslatedText("commands.teleport_commands.back.same", player).withStyle(ChatFormatting.AQUA), true);
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
