package dev.mrsnowy.teleport_commands.common;

import dev.mrsnowy.teleport_commands.storage.StorageManager;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

import static java.util.Collections.unmodifiableList;

public class Player {
    private final String UUID;
    private String DefaultHome = "";
    private final ArrayList<NamedLocation> Homes = new ArrayList<>();

    public Player(String uuid) {
        this.UUID = uuid;
    }

    // -----

    public String getUUID() {
        return UUID;
    }

    public String getDefaultHome() {
        return DefaultHome;
    }

    // returns all homes
    public ArrayList<NamedLocation> getHomes() {
        return (ArrayList<NamedLocation>) unmodifiableList(Homes);
    }

    // returns a specific home based on the name (if there is one)
    public Optional<NamedLocation> getHome(String name)  {
        return Homes.stream()
                .filter( home -> Objects.equals( home.getName(), name ))
                .findFirst();
    }

    // -----

    public void setDefaultHome(String defaultHome) throws Exception {
        this.DefaultHome = defaultHome;
        StorageManager.StorageSaver();
    }

    // todo! modify this so it uses a NamedLocation
    // creates a new home, if there already is a home it will update the existing one
    public void setHome(String name, BlockPos pos, String world) throws Exception {
        Optional<NamedLocation> optionalHome = getHome(name);
        NamedLocation home;

        if (optionalHome.isEmpty()) {
            home = new NamedLocation(name, pos, world);

            Homes.add(home);
        } else {
            home = optionalHome.get();

            home.setName(name);
        }

        StorageManager.StorageSaver();
    }

    // -----

    public void deleteHome(NamedLocation home) throws Exception {
        Homes.remove(home);

        StorageManager.StorageSaver();
    }
}
