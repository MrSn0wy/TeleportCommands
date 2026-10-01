package dev.mrsnowy.teleport_commands.common;

import dev.mrsnowy.teleport_commands.storage.StorageManager;

import java.util.ArrayList;
import java.util.List;
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
    public List<NamedLocation> getHomes() {
        return unmodifiableList(Homes);
    }

    // returns a specific home based on the name (if there is one)
    public Optional<NamedLocation> getHome(String name)  {
        return Homes.stream()
                .filter( home -> Objects.equals( home.getName(), name ))
                .findFirst();
    }

    // -----

    public void setDefaultHome(String defaultHome, StorageManager storageManager) throws Exception {
        this.DefaultHome = defaultHome;
        storageManager.StorageSaver();
    }

    // Adds a NamedLocation to the home list, returns true if it already exists
    public boolean addHome(NamedLocation home, StorageManager storageManager) throws Exception {
        if (getHome(home.getName()).isPresent()) {
            // Home with same name found!
            return true;

        } else {
            Homes.add(home);
            storageManager.StorageSaver();
            return false;
        }
    }

    // -----

    public void deleteHome(NamedLocation home, StorageManager storageManager) throws Exception {
        Homes.remove(home);

        storageManager.StorageSaver();
    }
}
