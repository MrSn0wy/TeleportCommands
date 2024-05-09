package dev.mrsnowy.teleport_commands;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;

public class quiltInit implements ModInitializer {

    @Override
    public void onInitialize(ModContainer mod)  {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        TeleportCommands.MOD_LOADER = "Quilt";
    }
}