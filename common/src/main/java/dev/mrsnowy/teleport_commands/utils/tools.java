package dev.mrsnowy.teleport_commands.utils;

import dev.mrsnowy.teleport_commands.TeleportCommands;
import dev.mrsnowy.teleport_commands.storage.StorageManager;

import java.io.*;
import java.util.*;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import org.tomlj.Toml;
import org.tomlj.TomlParseResult;

import static dev.mrsnowy.teleport_commands.TeleportCommands.MOD_ID;
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

    // Gets the translated text for each player based on their language, this is fully server side and actually works (UNLIKE MOJANG'S TRANSLATED KEY'S WHICH ARE CLIENT SIDE)
    public static MutableComponent getTranslatedText(String key, ServerPlayer player, Object... args) {
        String language = player.clientInformation().language();

        try {
            String filePath = String.format("/assets/%s/lang/%s.toml", MOD_ID, language);
            InputStream stream = TeleportCommands.class.getResourceAsStream(filePath);

            assert stream != null;
            TomlParseResult toml = Toml.parse(stream);
            String translation = toml.getString(key);

            assert translation != null;
            return Component.literal(String.format(translation, args));

        } catch (Exception ignored) {
            try {
                if (!Objects.equals(language, "en_us")) {
                    TeleportCommands.LOGGER.warn("Key \"{}\" not found in the language: {}, falling back to default (en_us)", key, language);

                    String filePath = String.format("/assets/%s/lang/en_us.toml", MOD_ID);
                    InputStream stream = TeleportCommands.class.getResourceAsStream(filePath);

                    assert stream != null;
                    TomlParseResult toml = Toml.parse(stream);
                    String translation = toml.getString(key);

                    assert translation != null;
                    return Component.literal(String.format(translation, args));
                }
            } catch (Exception ignored1) {}
            TeleportCommands.LOGGER.error("Key \"{}\" not found in the default language (en_us), sending raw key as fallback.", key);
            return Component.literal(key);
        }
    }
}
