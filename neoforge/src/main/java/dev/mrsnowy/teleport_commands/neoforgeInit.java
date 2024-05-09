package dev.mrsnowy.teleport_commands;

import net.neoforged.fml.common.Mod;

@Mod(TeleportCommands.MOD_ID)
public class neoforgeInit {

    public neoforgeInit() {
        // This method is invoked by the NeoForge mod loader when it is ready
        // to load your mod. You can access NeoForge and Common code in this
        // project.

        TeleportCommands.MOD_LOADER = "NeoForge";
    }
}

