package dev.mrsnowy.teleport_commands.utils;

import dev.mrsnowy.teleport_commands.TeleportCommands;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

import java.util.*;

import static net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT;

public class teleporter {
    private final TeleportCommands teleportCommands;
    private final Map<UUID, PlayerData> playerData = new HashMap<>();

    private static class PlayerData {
        long lastTeleportTime = 0;
        long lastHitTime = 0;
        Vec3 lastPosition = Vec3.ZERO;
    }

    public teleporter(TeleportCommands teleportCommands) {
        this.teleportCommands = teleportCommands;
    }

    /// Teleport the player :P
    public void teleport(ServerPlayer player, ServerLevel world, Vec3 coords) {
        // Check if user is allowed to teleport by config settings

        int delay = teleportCommands.config.CONFIG.teleporting.getDelay();
        UUID playerUUID = player.getUUID();
        // save when they last teleported and check delay
        if (playerData.containsKey(playerUUID)) {
            PlayerData playerdata = playerData.get(playerUUID);
        }

        // save pos and check if they have moved.

        // check if they got hit? whileFighting

        // save when they last got hit and if it exceeds fightCooldown


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

        // teleportation sound after teleport
        world.playSound(null, player.blockPosition(), SoundEvent.createVariableRangeEvent(ENDERMAN_TELEPORT.location()), SoundSource.PLAYERS, 0.4f, 1.0f);

        // delay visual effects so the player can see it when switching dimensions
        Timer timer = new Timer();
        timer.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        world.sendParticles(ParticleTypes.SNOWFLAKE, player.getX(), player.getY() , player.getZ(), 20, 0.0D, 1.0D, 0.0D, 0.01);
                        world.sendParticles(ParticleTypes.WHITE_SMOKE, player.getX(), player.getY(), player.getZ(), 15, 0.0D, 0.0D, 0.0D, 0.03);
                    }
                }, 100 // hopefully a good delay, ~ 2 ticks
        );
    }

}
