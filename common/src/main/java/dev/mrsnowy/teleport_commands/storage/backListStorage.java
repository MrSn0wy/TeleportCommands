package dev.mrsnowy.teleport_commands.storage;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

public class backListStorage {
    public static final backList backList = new backList();

    public static class backList {
        private final ArrayList<deathLocationClass> backList = new ArrayList<>();

        // filters the deathLocationList and finds the one with the matching player uuid (if there is one)
        public Optional<deathLocationClass> getDeathLocation(String uuid) {
            return backList.stream()
                    .filter( deathLocation -> Objects.equals( deathLocation.UUID, uuid ))
                    .findFirst();
        }

        // updates the deathLocation of a player, if there is no existing entry it will create a new deathLocation.
        public void setDeathLocation(String uuid, BlockPos pos, String world) {
            Optional<deathLocationClass> OptionalDeathLocation = getDeathLocation(uuid);

            if (OptionalDeathLocation.isEmpty()) {
                // create a new deathLocation
                deathLocationClass deathLocation = new deathLocationClass(uuid, pos, world);
                backList.add(deathLocation);
            } else {
                // modify existing deathLocation
                deathLocationClass deathLocation = OptionalDeathLocation.get();

                deathLocation.pos = pos;
                deathLocation.world = world;
            }
        }
    }

    public static class deathLocationClass {
        public String UUID;
        public BlockPos pos;
        public String world;

        private deathLocationClass(String uuid, BlockPos pos, String world) {
            this.UUID = uuid;
            this.pos = pos;
            this.world = world;
        }
    }
}