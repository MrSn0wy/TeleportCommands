package dev.mrsnowy.teleport_commands.utils;

import com.google.gson.*;
import dev.mrsnowy.teleport_commands.Constants;
import dev.mrsnowy.teleport_commands.TeleportCommands;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

import static dev.mrsnowy.teleport_commands.Constants.MOD_ID;
import static net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT;

public class tools {

    private static final Set<String> unsafeCollisionFreeBlocks = Set.of("block.minecraft.lava", "block.minecraft.flowing_lava", "block.minecraft.end_portal", "block.minecraft.end_gateway","block.minecraft.fire", "block.minecraft.soul_fire", "block.minecraft.powder_snow", "block.minecraft.nether_portal");

    public static void Teleporter(ServerPlayer player, ServerLevel world, Vec3 coords) {
        // teleportation effects & sounds before teleporting
        world.sendParticles(ParticleTypes.SNOWFLAKE, player.getX(), player.getY() + 1, player.getZ(), 20, 0.0D, 0.0D, 0.0D, 0.01);
        world.sendParticles(ParticleTypes.WHITE_SMOKE, player.getX(), player.getY(), player.getZ(), 15, 0.0D, 1.0D, 0.0D, 0.03);
        world.playSound(null, player.blockPosition(), SoundEvent.createVariableRangeEvent(ENDERMAN_TELEPORT.getLocation()), SoundSource.PLAYERS, 0.4f, 1.0f);

        // check if the player is currently flying
        boolean flying = player.getAbilities().flying;

        // teleport!
        player.teleportTo(world, coords.x, coords.y, coords.z, player.getYRot(), player.getXRot());

        // Restore flying when teleporting trough dimensions
        if (flying) {
            player.getAbilities().flying = true;
            player.onUpdateAbilities();
        }

        // teleportation sound after teleport
        world.playSound(null, player.blockPosition(), SoundEvent.createVariableRangeEvent(ENDERMAN_TELEPORT.getLocation()), SoundSource.PLAYERS, 0.4f, 1.0f);

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


    // checks a 7x7x7 location around the player in order to find a safe place to teleport them to.
    public static Optional<BlockPos> getSafeBlockPos(BlockPos blockPos, ServerLevel world) {
        int row = 1;
        int rows = 3;

        int blockPosX = blockPos.getX();
        int blockPosY = blockPos.getY();
        int blockPosZ = blockPos.getZ();

        if (isBlockPosSafe(blockPos, world)) {
            return Optional.of(blockPos); // safe location found!

        } else {
            // find a safe location in an x row radius
            while (row <= rows) {
    //            TeleportCommands.LOGGER.info("currently doing row " + row + " of " + rows); //debug
                for (int z = -row; z <= row; z++) {
                    for (int x = -row; x <= row; x++) {
                        for (int y = -row; y <= row; y++) {

                            // checks if we are on the outer layer of the row, not on the inside
                            if ((x == -row || x == row) || (z == -row || z == row) || (y == -row || y == row)) {

                                // calculate a new blockPos based on the offset we generated
                                BlockPos newPos = new BlockPos(blockPosX + x, blockPosY + y, blockPosZ + z);

                                if (isBlockPosSafe(newPos, world)) {
//                                    return Optional.of(new Vec3(newPos.getX() + 0.5, newPos.getY(), newPos.getZ() + 0.5)); // safe location found!
                                    return Optional.of(newPos);
                                }
                            }
                        }
                    }
                }

                row++;
            }

            // no safe location
            return Optional.empty(); // no safe location found!
        }
    }


    // Gets the translated text for each player based on their language, this is fully server side and actually works (UNLIKE MOJANG'S TRANSLATED KEY'S WHICH ARE CLIENT SIDE) (I'm not mad, I swear!)
    public static MutableComponent getTranslatedText(String key, ServerPlayer player, MutableComponent... args) {
        //todo! maybe make this also loaded in memory?
        String language = player.clientInformation().language().toLowerCase();
        String regex = "%(\\d+)%";
        Pattern pattern = Pattern.compile(regex);

//        MinecraftServer server = player.getServer();

//         MinecraftServer.ServerResourcePackInfo silly2 = server.getResourceManager().listResources()

//        java.util.stream.Stream<net.minecraft.server.packs.PackResources> SILLY = server.getResourceManager().listPacks();
//
//        SILLY.forEach(pack -> {
//            Constants.LOGGER.info("{} : {} : {}", pack.packId(), pack.location(), pack.getClass());
//        });

//        player.displayClientMessage(Component.literal(.toString()), false);

        // the try catch stuff is so wacky, but it works fine and I don't need to check everything
        try {
            String filePath = String.format("/assets/%s/lang/%s.json", MOD_ID, language);
            InputStream stream = TeleportCommands.class.getResourceAsStream(filePath);

            Reader reader = new InputStreamReader(Objects.requireNonNull(stream, String.format("Couldn't find the required language file for \"%s\"", language)), StandardCharsets.UTF_8);
            JsonElement json = JsonParser.parseReader(reader);
            String translation = json.getAsJsonObject().get(key).getAsString();


            // Adds the optional MutableComponents in the correct places
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

            try {
                if (!Objects.equals(language, "en_us")) {
//                    TeleportCommands.LOGGER.warn("Key \"{}\" not found in the language: {}, falling back to default (en_us)", key, language);

                    String filePath = String.format("/assets/%s/lang/en_us.json", MOD_ID);
                    InputStream stream = TeleportCommands.class.getResourceAsStream(filePath);

                    Reader reader = new InputStreamReader(Objects.requireNonNull(stream, String.format("Couldn't find the required language file for \"%s\"", language)), StandardCharsets.UTF_8);
                    JsonElement json = JsonParser.parseReader(reader);
                    String translation = json.getAsJsonObject().get(key).getAsString();

                    // Adds the optional MutableComponents in the correct places
                    Matcher matcher = pattern.matcher(Objects.requireNonNull(translation, "translation cannot be null"));

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
            Constants.LOGGER.error("Key \"{}\" not found in the default language (en_us), sending raw key as fallback.", key);
            return Component.literal(key);
        }
    }


    // Gets the ids of all the worlds
    public static List<String> getWorldIds() {
        return StreamSupport.stream(TeleportCommands.SERVER.getAllLevels().spliterator(), false)
                .map(level -> level.dimension().location().toString())
                .toList();
    }


    // checks if a BlockPos is safe, used by the teleportSafetyChecker.
    private static boolean isBlockPosSafe(BlockPos bottomPlayer, ServerLevel world) {

        // get the block below the player
        BlockPos belowPlayer = new BlockPos(bottomPlayer.getX(), bottomPlayer.getY() -1, bottomPlayer.getZ()); // below the player
        String belowPlayerId = world.getBlockState(belowPlayer).getBlock().getDescriptionId(); // below the player

        // get the bottom of the player
        String BottomPlayerId = world.getBlockState(bottomPlayer).getBlock().getDescriptionId(); // bottom of player

        // get the top of the player
        BlockPos TopPlayer = new BlockPos(bottomPlayer.getX(), bottomPlayer.getY() + 1, bottomPlayer.getZ()); // top of player
        String TopPlayerId = world.getBlockState(TopPlayer).getBlock().getDescriptionId(); // top of player


        // check if the block position isn't safe
        if ((belowPlayerId.equals("block.minecraft.water") || !world.getBlockState(belowPlayer).getCollisionShape(world, belowPlayer).isEmpty()) // check if the player is going to fall on teleport
            && (world.getBlockState(bottomPlayer).getCollisionShape(world, bottomPlayer).isEmpty() && !unsafeCollisionFreeBlocks.contains(BottomPlayerId)) // check if it is a collision free block that isn't dangerous
            && (!unsafeCollisionFreeBlocks.contains(TopPlayerId))) // check if it is a dangerous collision free block, if it is solid then the player crawls
        {
            return true; // it's safe
        }
        return false; // it's not safe!
    }
}
