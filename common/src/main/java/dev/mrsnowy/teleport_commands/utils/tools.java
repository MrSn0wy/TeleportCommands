package dev.mrsnowy.teleport_commands.utils;

import dev.mrsnowy.teleport_commands.storage.StorageManager;
import java.util.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

import static dev.mrsnowy.teleport_commands.storage.StorageManager.GetPlayerStorage;
import static dev.mrsnowy.teleport_commands.storage.StorageManager.StorageSaver;
import static net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT;

public class tools {

    public static void Teleporter(ServerPlayer player, ServerLevel world, Vec3 coords) {
        world.sendParticles(ParticleTypes.SNOWFLAKE, player.getX(), player.getY() + 1, player.getZ(), 20, 0.0D, 0.0D, 0.0D, 0.01);
        world.sendParticles(ParticleTypes.WHITE_SMOKE, player.getX(), player.getY(), player.getZ(), 15, 0.0D, 1.0D, 0.0D, 0.03);
        world.playSound(null, player.blockPosition(), SoundEvent.createVariableRangeEvent(ENDERMAN_TELEPORT.getLocation()), SoundSource.PLAYERS, 0.4f, 1.0f);

        var flying = player.getAbilities().flying;

        player.teleportTo(world, coords.x, coords.y, coords.z, player.getYRot(), player.getXRot());

        // Restore flying when teleporting dimensions
        if (flying) {
            player.getAbilities().flying = true;
            player.onUpdateAbilities();
        }

        world.playSound(null, player.blockPosition(), SoundEvent.createVariableRangeEvent(ENDERMAN_TELEPORT.getLocation()), SoundSource.PLAYERS, 0.4f, 1.0f);
        Timer timer = new Timer();

        timer.schedule(
            new TimerTask() {
                @Override
                public void run() {
                    world.sendParticles(ParticleTypes.SNOWFLAKE, player.getX(), player.getY() , player.getZ(), 20, 0.0D, 1.0D, 0.0D, 0.01);
                    world.sendParticles(ParticleTypes.WHITE_SMOKE, player.getX(), player.getY(), player.getZ(), 15, 0.0D, 0.0D, 0.0D, 0.03);
                }
            }, 100 // hopefully good, 2 ticks
        );
    }



    public static void DeathLocationUpdater(Vec3 pos, ServerLevel world, String UUID) throws Exception {
        StorageManager.PlayerStorageResult storages = GetPlayerStorage(UUID);

        StorageManager.StorageClass storage = storages.storage;
        StorageManager.StorageClass.Player playerStorage = storages.playerStorage;

        playerStorage.deathLocation.x = Double.parseDouble(String.format("%.1f", pos.x()));
        playerStorage.deathLocation.y = Double.parseDouble(String.format("%.1f", pos.y()));
        playerStorage.deathLocation.z = Double.parseDouble(String.format("%.1f", pos.z()));
        playerStorage.deathLocation.world = world.dimension().location().toString();

        StorageSaver(storage);
    }
}
