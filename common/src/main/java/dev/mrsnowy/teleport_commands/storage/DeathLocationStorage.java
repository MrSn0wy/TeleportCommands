package dev.mrsnowy.teleport_commands.storage;

import dev.mrsnowy.teleport_commands.common.DeathLocation;
import net.minecraft.core.BlockPos;

import java.util.HashMap;
import java.util.Optional;

public class DeathLocationStorage {
    private final HashMap<String, DeathLocation> deathLocations = new HashMap<>();

    // filters the deathLocationList and finds the one with the matching player uuid (if there is one)
    public Optional<DeathLocation> getDeathLocation(String uuid) {
        return Optional.ofNullable(deathLocations.get(uuid));
    }

    // updates the deathLocation of a player, if there is no existing entry it will create a new deathLocation.
    public void setDeathLocation(String uuid, BlockPos pos, String world) {

        if (deathLocations.containsKey(uuid)) {
            // modify existing deathLocation
            DeathLocation deathLocation = deathLocations.get(uuid);
            deathLocation.setBlockPos(pos);
            deathLocation.setWorld(world);
        } else {
            // create a new deathLocation
            DeathLocation deathLocation = new DeathLocation(pos, world);
            deathLocations.put(uuid, deathLocation);
        }
    }

    public void clearDeathLocations() {
        deathLocations.clear();
    }
}