package dev.mrsnowy.teleport_commands.utils;

import dev.mrsnowy.teleport_commands.TeleportCommands;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.*;

import static dev.mrsnowy.teleport_commands.utils.Language.getTranslation;
import static net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT;

public class Teleporter {
    private final TeleportCommands teleportCommands;
    private final Map<UUID, PlayerData> players = new HashMap<>();

    private static class PlayerData {
        /// This value is set to when the teleport cooldown expires (note this is the *cooldown* between teleports, not the delay before teleporting!).
        int teleportCooldownUntil = 0;
        boolean teleportOnCooldown = false;

        /// This value is set to when the fight cooldown expires.
        int fightCooldownUntil = 0;
        boolean fightOnCooldown = false;

        /// A pending teleport, is null when nothing is pending.
        @Nullable
        pendingTeleport pendingTeleport = null;
    }

    private static class pendingTeleport {
        ServerLevel destinationWorld;
        BlockPos startingCoords;
        Vec3 destinationCoords;
        int teleportDelayUntil;
        /// The tps that will be upheld until the teleport is finished (for displaying purposes)
        int tps;
        /// Message that gets sent when the teleportation happens
        String completionMessage;

        pendingTeleport(ServerLevel destinationWorld, Vec3 destinationCoords, BlockPos startingCoords, int teleportDelayUntil, int tps, String completionMessage) {
            this.destinationWorld = destinationWorld;
            this.destinationCoords = destinationCoords;
            this.startingCoords = startingCoords;
            this.teleportDelayUntil = teleportDelayUntil;
            this.tps = tps;
            this.completionMessage = completionMessage;
        }
    }

    // ----

    public Teleporter(TeleportCommands teleportCommands) {
        this.teleportCommands = teleportCommands;
    }

    /// This function checks if any of the timers/cooldowns/delays have expired of each player in the "players" hashmap.
    /// This gets ran every tick.
    public void tick(MinecraftServer server) {
        int currentTick = server.getTickCount();

        players.entrySet().removeIf(entry -> {
            PlayerData data = entry.getValue();
            boolean wasOnCooldown = data.teleportOnCooldown || data.fightOnCooldown;

            if (data.teleportOnCooldown && (currentTick >= data.teleportCooldownUntil)) {
                data.teleportOnCooldown = false;
            }

            if (data.fightOnCooldown && (currentTick >= data.fightCooldownUntil)) {
                data.fightOnCooldown = false;
            }

            // Let the player know when the cooldowns are expired and that they are cool to teleport again
            if (wasOnCooldown && !data.teleportOnCooldown && !data.fightOnCooldown) {
                ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
                if (player != null) {
                    player.displayClientMessage(getTranslation("commands.teleport_commands.teleport.ready", player).withStyle(ChatFormatting.WHITE), true);
                }
            }

            // Check if there is a pending teleport request (and if the delay is over)
            if (data.pendingTeleport != null) {
                ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());

                /// delay is over
                if (currentTick >= data.pendingTeleport.teleportDelayUntil) {
                    if (player != null) {
                        teleport(player, data.pendingTeleport.destinationWorld, data.pendingTeleport.destinationCoords, data.pendingTeleport.completionMessage);
                    }

                    data.pendingTeleport = null;
                } else {
                    // check if this is a whole number
                    float secondsLeft = (float) (data.pendingTeleport.teleportDelayUntil - currentTick) / data.pendingTeleport.tps;
                    if ((secondsLeft % 1) == 0 && player != null) {
                        player.displayClientMessage(getTranslation("commands.teleport_commands.teleport.progress", player).withStyle(ChatFormatting.WHITE), true);
                    }
                }
            }



            // remove the entry if there is no cooldowns and no teleports happening
            return (!data.teleportOnCooldown && !data.fightOnCooldown && data.pendingTeleport == null);
        });
    }

    /// Adds the player to the teleportation queue
    public void queue(ServerPlayer player, ServerLevel world, Vec3 coords) {
        queue(player, world, coords, "commands.teleport_commands.teleport.go");
    }

    /// Adds the player to the teleportation queue
    public void queue(ServerPlayer player, ServerLevel world, Vec3 coords, String completionMessage) {
        // Check if user is allowed to teleport by config settings

        UUID playerUUID = player.getUUID();
        if (!players.containsKey(playerUUID)) {
            players.put(playerUUID, new PlayerData());
        }

        PlayerData data = players.get(playerUUID);
        int currentTick = teleportCommands.server.getTickCount();
        int tps = (int) (1.0 / teleportCommands.server.tickRateManager().tickrate());

        // Check if we are already teleporting
        if (data.pendingTeleport != null) {
            int ticksLeft = data.pendingTeleport.teleportDelayUntil - currentTick;
            int secondsLeft = (int) Math.ceil( (double) ticksLeft / data.pendingTeleport.tps);

            player.displayClientMessage(getTranslation("commands.teleport_commands.teleport.delay", player,
                    Component.literal(String.valueOf(secondsLeft)),
                    Component.literal(String.valueOf(ticksLeft))
            ).withStyle(ChatFormatting.WHITE), false);

            return;
        }

        // Check if we are still on cooldown
        if (data.teleportOnCooldown || data.fightOnCooldown) {
            int cooldownUntil = Math.max(data.teleportCooldownUntil, data.fightCooldownUntil);
            int ticksLeft = cooldownUntil - currentTick;
            int secondsLeft = (int) Math.ceil( (double) ticksLeft / tps);

            player.displayClientMessage(getTranslation("commands.teleport_commands.teleport.cooldown", player,
                    Component.literal(String.valueOf(secondsLeft)),
                    Component.literal(String.valueOf(ticksLeft))
            ).withStyle(ChatFormatting.WHITE), false);

            return;
        }

        // teleport
        int teleportingDelay = teleportCommands.config.config.teleporting.getDelay();

        if (teleportingDelay >= 0) {
            BlockPos currentPos = player.blockPosition();

            data.pendingTeleport = new pendingTeleport(world, coords, currentPos, currentTick + (teleportingDelay * 20), tps, completionMessage);
        } else {
            // bypass the delay
            teleport(player, world, coords, completionMessage);
        }
    }

    ///  Teleports the player :P
    private void teleport(ServerPlayer player, ServerLevel to_world, Vec3 to_coords, String completionMessage) {
        // teleportation effects & sounds before teleporting
        ServerLevel from_world = player.serverLevel();
        from_world.sendParticles(ParticleTypes.SNOWFLAKE, player.getX(), player.getY() + 1, player.getZ(), 20, 0.0D, 0.0D, 0.0D, 0.01);
        from_world.sendParticles(ParticleTypes.WHITE_SMOKE, player.getX(), player.getY(), player.getZ(), 15, 0.0D, 1.0D, 0.0D, 0.03);
        from_world.playSound(null, player.blockPosition(), SoundEvent.createVariableRangeEvent(ENDERMAN_TELEPORT.location()), SoundSource.PLAYERS, 0.4f, 1.0f);

        boolean flying = player.getAbilities().flying;

        player.displayClientMessage(getTranslation(completionMessage, player).withStyle(ChatFormatting.WHITE), true);
        player.teleportTo(to_world, to_coords.x, to_coords.y, to_coords.z, Set.of(), player.getYRot(), player.getXRot(), false);

        // Restore flying when teleporting trough dimensions
        if (flying) {
            player.getAbilities().flying = true;
            player.onUpdateAbilities();
        }

        // teleportation sound && effects after teleport
        to_world.playSound(null, player.blockPosition(), SoundEvent.createVariableRangeEvent(ENDERMAN_TELEPORT.location()), SoundSource.PLAYERS, 0.4f, 1.0f);
        to_world.sendParticles(ParticleTypes.SNOWFLAKE, player.getX(), player.getY() , player.getZ(), 20, 0.0D, 1.0D, 0.0D, 0.01);
        to_world.sendParticles(ParticleTypes.WHITE_SMOKE, player.getX(), player.getY(), player.getZ(), 15, 0.0D, 0.0D, 0.0D, 0.03);
    }

    /// This function gets called by the mixin to notify that the player has moved
    public void reportPlayerMoved(ServerPlayer player) {
        if (teleportCommands.config.config.teleporting.isAllowMoving()) {
            return; // The config option is disabled
        }

        PlayerData data = players.get(player.getUUID());

        if (data != null && data.pendingTeleport != null) {
            BlockPos pos = player.blockPosition();
            boolean tooFar = data.pendingTeleport.startingCoords.closerThan(pos, 1.5); // Checks if they moved more than one block (1.5 for diagonals)

            if (tooFar) {
                player.displayClientMessage(getTranslation("commands.teleport_commands.teleport.moving", player).withStyle(ChatFormatting.WHITE), true);
                data.pendingTeleport = null;
            }

        }
    }

    /// This function gets called by the mixin to notify that the player got damaged
    public void reportPlayerHurt(ServerPlayer player) {
        if (teleportCommands.config.config.teleporting.isAllowFighting()) {
            return; // The config option is disabled
        }

        PlayerData data = players.get(player.getUUID());

        if (data != null) {
            int currentTick = teleportCommands.server.getTickCount();

            data.fightCooldownUntil = currentTick + teleportCommands.config.config.teleporting.getFightCooldown();
            data.fightOnCooldown = true;

            if (data.pendingTeleport != null) {
                player.displayClientMessage(getTranslation("commands.teleport_commands.teleport.fighting", player).withStyle(ChatFormatting.WHITE), true);
                data.pendingTeleport = null;
            }
        }
    }
}
