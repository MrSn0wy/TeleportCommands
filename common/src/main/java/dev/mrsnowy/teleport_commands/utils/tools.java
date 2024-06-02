package dev.mrsnowy.teleport_commands.utils;

import com.mojang.datafixers.util.Pair;
import dev.mrsnowy.teleport_commands.TeleportCommands;
import dev.mrsnowy.teleport_commands.storage.StorageManager;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
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

    private static final Set<String> unsafeCollisionFreeBlocks = Set.of("block.minecraft.lava", "block.minecraft.flowing_lava", "block.minecraft.end_portal", "block.minecraft.end_gateway","block.minecraft.fire", "block.minecraft.soul_fire", "block.minecraft.powder_snow", "block.minecraft.nether_portal");

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
            }, 100 // hopefully good, ~ 2 ticks
        );
    }


    public static void DeathLocationUpdater(Vec3 pos, ServerLevel world, String UUID) throws Exception {
        StorageManager.PlayerStorageClass storages = GetPlayerStorage(UUID);

        StorageManager.StorageClass storage = storages.storage;
        StorageManager.StorageClass.Player playerStorage = storages.playerStorage;

        // to ensure compatibility with older versions we cast it to double
        playerStorage.deathLocation.x = (int) Math.round(pos.x());
        playerStorage.deathLocation.y = (int) Math.round(pos.y());
        playerStorage.deathLocation.z = (int) Math.round(pos.z());

        playerStorage.deathLocation.world = world.dimension().location().toString();

        StorageSaver(storage);
    }


    public static Pair<Integer, Optional<Vec3>> teleportSafetyChecker(int playerX, int playerY, int playerZ, ServerLevel world, ServerPlayer player) {
        int row = 1;
        int rows = 3;

        // find a safe location in an x row radius
        if (isBlockPosUnsafe(new BlockPos(playerX, playerY, playerZ), world)) {

            while (row <= rows) {
    //            TeleportCommands.LOGGER.info("currently doing row " + row + " of " + rows); //debug

                for (int z = -row; z <= row; z++) {
                    for (int x = -row; x <= row; x++) {
                        for (int y = -row; y <= row; y++) {

                            if ((x == -row || x == row) || (z == -row || z == row) || (y == -row || y == row)) {
                                if (!isBlockPosUnsafe(new BlockPos(playerX + x, playerY + y, playerZ + z), world)) {

                                    Vec3 toTeleportTo = new Vec3(playerX + x + 0.5, playerY + y, playerZ + z + 0.5);

                                    if (!player.getPosition(0).equals(toTeleportTo) || player.level() != world) {
                                        return new Pair<>(0, Optional.of(toTeleportTo)); // safe!

                                    } else {
                                        return new Pair<>(1, Optional.empty()); // same
                                    }
                                }
                            }
                        }
                    }
                }

                row++;
            }

            // no safe location
            player.displayClientMessage(getTranslatedText("commands.teleport_commands.common.noSafeLocation", player).withStyle(ChatFormatting.RED, ChatFormatting.BOLD), false);
            player.displayClientMessage(getTranslatedText("commands.teleport_commands.common.safetyIsForLosers", player).withStyle(ChatFormatting.AQUA), false);

            return new Pair<>(2, Optional.empty()); // no safe location
        } else {
            return new Pair<>(0, Optional.of(new Vec3(playerX + 0.5, playerY, playerZ + 0.5))); // safe!
        }
    }



    // Gets the translated text for each player based on their language, this is fully server side and actually works (UNLIKE MOJANG'S TRANSLATED KEY'S WHICH ARE CLIENT SIDE)
    public static MutableComponent getTranslatedText(String key, ServerPlayer player, MutableComponent... args) {
        String language = player.clientInformation().language();
        String regex = "%(\\d+)%";
        Pattern pattern = Pattern.compile(regex);

        // the try catch stuff is so wacky, but it works fine and I don't need to check everything
        try {
            String filePath = String.format("/assets/%s/lang/%s.toml", MOD_ID, language);
            InputStream stream = TeleportCommands.class.getResourceAsStream(filePath);

            TomlParseResult toml = Toml.parse(Objects.requireNonNull(stream));
            String translation = toml.getString(key);

            Matcher matcher = pattern.matcher(Objects.requireNonNull(translation));

            MutableComponent component = Component.literal("");
            int lastIndex = 0;

            while (matcher.find()) {
                component.append(Component.literal(translation.substring(lastIndex, matcher.start())));

                int index = Integer.parseInt(matcher.group(1));
                component.append(args[index]);

                lastIndex = matcher.end();
            }
            component.append(translation.substring(lastIndex));

            return component;

        } catch (Exception e) {
            TeleportCommands.LOGGER.error(e.toString());
            try {
                if (!Objects.equals(language, "en_us")) {
//                    TeleportCommands.LOGGER.warn("Key \"{}\" not found in the language: {}, falling back to default (en_us)", key, language);

                    String filePath = String.format("/assets/%s/lang/en_us.toml", MOD_ID);
                    InputStream stream = TeleportCommands.class.getResourceAsStream(filePath);

                    TomlParseResult toml = Toml.parse(Objects.requireNonNull(stream));
                    String translation = toml.getString(key);

                    Matcher matcher = pattern.matcher(Objects.requireNonNull(translation));

                    MutableComponent component = Component.literal("");
                    int lastIndex = 0;

                    while (matcher.find()) {
                        component.append(Component.literal(translation.substring(lastIndex, matcher.start())));

                        int index = Integer.parseInt(matcher.group(1));
                        component.append(args[index]);

                        lastIndex = matcher.end();
                    }
                    component.append(translation.substring(lastIndex));

                    return component;
                }
            } catch (Exception ignored1) {}
            TeleportCommands.LOGGER.error("Key \"{}\" not found in the default language (en_us), sending raw key as fallback.", key);
            return Component.literal(key);
        }
    }


    private static boolean isBlockPosUnsafe(BlockPos bottomPlayer, ServerLevel world) {
        // bottomPlayer is presumed to be the bottom of the player character

        BlockPos belowPlayer = new BlockPos(bottomPlayer.getX(), bottomPlayer.getY() -1, bottomPlayer.getZ()); // below the player
        String belowPlayerId = world.getBlockState(belowPlayer).getBlock().getDescriptionId(); // below the player

        String BottomPlayerId = world.getBlockState(bottomPlayer).getBlock().getDescriptionId(); // bottom of player

        BlockPos TopPlayer = new BlockPos(bottomPlayer.getX(), bottomPlayer.getY() + 1, bottomPlayer.getZ()); // top of player
        String TopPlayerId = world.getBlockState(TopPlayer).getBlock().getDescriptionId(); // top of player


        // check if the death location isn't safe
        if (
                (belowPlayerId.equals("block.minecraft.water") || !world.getBlockState(belowPlayer).getCollisionShape(world, belowPlayer).isEmpty()) // check if the player is going to fall on teleport
                        && (world.getBlockState(bottomPlayer).getCollisionShape(world, bottomPlayer).isEmpty() && !unsafeCollisionFreeBlocks.contains(BottomPlayerId)) // check if it is a collision free block, that isn't dangerous
                        && (!unsafeCollisionFreeBlocks.contains(TopPlayerId)) // check if it is a dangerous collision free block, if it is solid then the player crawls
        ){
            return false; // it's safe
        }
        return true; // it's not safe!
    }
}
