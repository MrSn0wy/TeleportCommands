package dev.mrsnowy.teleport_commands.utils;

import dev.mrsnowy.teleport_commands.TeleportCommands;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.*;

import static dev.mrsnowy.teleport_commands.utils.tools.getTranslatedText;
import static net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT;

public class teleporter {
    private final TeleportCommands teleportCommands;
    private final Map<UUID, PlayerData> playersData = new HashMap<>();

    private static class PlayerData {
        /// This value is set to when the teleport cooldown expires.
        int teleportCooldownExpiry = 0;
        boolean teleportCooldownExpired = true;

        /// This value is set to when the fight cooldown expires.
        int fightCooldownExpiry = 0;
        boolean fightCooldownExpired = true;

        /// A pending teleport, is null when nothing is pending.
        @Nullable
        pendingTeleport pendingTeleport = null;

//        Vec3 lastPosition = Vec3.ZERO;
    }

    private static class pendingTeleport {
        ServerLevel destinationWorld;
        Vec3 destinationCoords;
        int teleportDelayExpiry;

        pendingTeleport(ServerLevel destinationWorld, Vec3 destinationCoords, int teleportDelayExpiry) {
            this.destinationWorld = destinationWorld;
            this.destinationCoords = destinationCoords;
            this.teleportDelayExpiry = teleportDelayExpiry;
        }
    }

    public teleporter(TeleportCommands teleportCommands) {
        this.teleportCommands = teleportCommands;
    }

    /// This gets ran every tick
    public void checkPlayerData(MinecraftServer server) {
        int currentTick = server.getTickCount();

        playersData.entrySet().removeIf(entry -> {
            PlayerData data = entry.getValue();

            // Check if we are past the cooldown
            if (!data.teleportCooldownExpired && (currentTick >= data.teleportCooldownExpiry)) {
                data.teleportCooldownExpired = true;

                ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
                if (player != null) {
                    ///  TODO! add actual generic message (just copied this one)
                    player.displayClientMessage(getTranslatedText("commands.teleport_commands.warp.exists", player).withStyle(ChatFormatting.WHITE), false);
                }
            }

            // Check if we are past the cooldown
            if (!data.fightCooldownExpired && (currentTick >= data.fightCooldownExpiry)) {
                data.fightCooldownExpired = true;

                ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
                if (player != null) {
                    ///  TODO! add actual generic message (just copied this one)
                    player.displayClientMessage(getTranslatedText("commands.teleport_commands.warp.exists", player).withStyle(ChatFormatting.WHITE), false);
                }
            }

            ///  Check if there is a pending teleport request and if we are ready to teleport (we ignore the fightDelay since that is only relevant for starting a request)
            if (data.pendingTeleport != null && (currentTick >= data.pendingTeleport.teleportDelayExpiry)) {
                ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
                if (player != null) {
                    teleport(player, data.pendingTeleport.destinationWorld, data.pendingTeleport.destinationCoords);
                }

                data.pendingTeleport = null;
            }

            return (data.teleportCooldownExpired && data.fightCooldownExpired);
        });
    }

    /// Teleport the player :P
    public void teleportQueue( ServerPlayer player, ServerLevel world, Vec3 coords) {
        // Check if user is allowed to teleport by config settings

        UUID playerUUID = player.getUUID();

        if (!playersData.containsKey(playerUUID)) {
            playersData.put(playerUUID, new PlayerData());
        }

        PlayerData data = playersData.get(playerUUID);

        // Check if we are already teleporting
        if (data.pendingTeleport != null) {
            ///  TODO! add actual generic message (just copied this one)
            player.displayClientMessage(getTranslatedText("commands.teleport_commands.teleporting.delay", player).withStyle(ChatFormatting.WHITE), false);
            return;
        }

        // Check if the teleport cooldowns are expired.
        if (!data.teleportCooldownExpired || !data.fightCooldownExpired) {
            ///  TODO! add actual generic message (just copied this one) (and add seconds left :P)
            player.displayClientMessage(getTranslatedText("commands.teleport_commands.teleporting.delay", player).withStyle(ChatFormatting.WHITE), false);
            return;
        }

        // teleport
        int teleportingDelay = teleportCommands.config.config.teleporting.getDelay();

        if (teleportingDelay >= 0) {
            int currentTick = teleportCommands.server.getTickCount();
            data.pendingTeleport = new pendingTeleport(world, coords, currentTick + (teleportingDelay * 20));
        } else {
            // bypass the delay
            teleport(player, world, coords);
        }


        // save pos and check if they have moved.

        // check if they got hit? whileFighting

        // save when they last got hit and if it exceeds fightCooldown

    }


    private void teleport(ServerPlayer player, ServerLevel world, Vec3 coords) {
        // teleportation effects & sounds before teleporting
        world.sendParticles(ParticleTypes.SNOWFLAKE, player.getX(), player.getY() + 1, player.getZ(), 20, 0.0D, 0.0D, 0.0D, 0.01);
        world.sendParticles(ParticleTypes.WHITE_SMOKE, player.getX(), player.getY(), player.getZ(), 15, 0.0D, 1.0D, 0.0D, 0.03);
        world.playSound(null, player.blockPosition(), SoundEvent.createVariableRangeEvent(ENDERMAN_TELEPORT.location()), SoundSource.PLAYERS, 0.4f, 1.0f);

        // check if the player is currently flying
        boolean flying = player.getAbilities().flying;

        // teleport!
        player.teleportTo(world, coords.x, coords.y, coords.z, Set.of(), player.getYRot(), player.getXRot(), false);

        // Restore flying when teleporting trough dimensions
        if (flying) {
            player.getAbilities().flying = true;
            player.onUpdateAbilities();
        }

        // teleportation sound && effects after teleport
        world.playSound(null, player.blockPosition(), SoundEvent.createVariableRangeEvent(ENDERMAN_TELEPORT.location()), SoundSource.PLAYERS, 0.4f, 1.0f);
        world.sendParticles(ParticleTypes.SNOWFLAKE, player.getX(), player.getY() , player.getZ(), 20, 0.0D, 1.0D, 0.0D, 0.01);
        world.sendParticles(ParticleTypes.WHITE_SMOKE, player.getX(), player.getY(), player.getZ(), 15, 0.0D, 0.0D, 0.0D, 0.03);
    }
}
