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

    public void removeDeathLocation(String uuid, DeathLocation expected) {
        deathLocations.remove(uuid, expected);
    }

    // updates the deathLocation of a player, if there is no existing entry it will create a new deathLocation.
    public void setDeathLocation(String uuid, BlockPos pos, String world) {

        deathLocations.put(uuid, new DeathLocation(pos, world));
    }

//    public void clearDeathLocations() {
//        deathLocations.clear();
//    }
}