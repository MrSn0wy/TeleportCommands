package dev.mrsnowy.teleport_commands;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Constants.MOD_ID)
public class neoforgeInit {

    public neoforgeInit(IEventBus eventBus) {
        // This method is invoked by the NeoForge mod loader when it is ready
        // to load your mod. You can access NeoForge and Common code in this
        // project.

        TeleportCommands.MOD_LOADER = "NeoForge";
    }
}

