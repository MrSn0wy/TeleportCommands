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

public class back {

    public static void register(Commands commandManager) {

        commandManager.getDispatcher().register(Commands.literal("back").executes(context -> {
            ServerPlayer player = context.getSource().getPlayer();

            if (player == null) {
                TeleportCommands.LOGGER.error("Error while executing the command, No player found!");
                return 1;
            }

            try {
                player.displayClientMessage(Component.literal("Teleporting"), true);
                ToDeathLocation(player);
            } catch (Exception e) {
                TeleportCommands.LOGGER.error(String.valueOf(e));
                player.displayClientMessage(Component.literal("Error Teleporting!").withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true);
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
                Teleporter(player, currentWorld, pos);
                found = true;
                break;
            }
        }

        if (!found) {
            player.displayClientMessage(Component.literal("No Location Found!"), true);
        }
    }
}
