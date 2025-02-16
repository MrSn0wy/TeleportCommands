package dev.mrsnowy.teleport_commands.storage;

import dev.mrsnowy.teleport_commands.common.DeathLocation;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

public class DeathLocationStorage {
    private static final ArrayList<DeathLocation> deathLocations = new ArrayList<>();

    // filters the deathLocationList and finds the one with the matching player uuid (if there is one)
    public static Optional<DeathLocation> getDeathLocation(String uuid) {
        return deathLocations.stream()
                .filter( deathLocation -> Objects.equals( deathLocation.getUUID(), uuid ))
                .findFirst();
    }

    // updates the deathLocation of a player, if there is no existing entry it will create a new deathLocation.
    public static void setDeathLocation(String uuid, BlockPos pos, String world) {
        Optional<DeathLocation> OptionalDeathLocation = getDeathLocation(uuid);

        if (OptionalDeathLocation.isEmpty()) {
            // create a new deathLocation
            DeathLocation deathLocation = new DeathLocation(uuid, pos, world);
            deathLocations.add(deathLocation);
        } else {
            // modify existing deathLocation
            DeathLocation deathLocation = OptionalDeathLocation.get();

            deathLocation.setBlockPos(pos);
            deathLocation.setWorld(world);
        }
    }
}