package dev.mrsnowy.teleport_commands;

import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.minecraft.server.level.ServerPlayer;

@Mod(TeleportCommands.MOD_ID)
public class neoforgeInit {

    public neoforgeInit(IEventBus eventBus) {
        // This method is invoked by the NeoForge mod loader when it is ready
        // to load your mod. You can access NeoForge and Common code in this
        // project.

        TeleportCommands.LOGGER.info("Teleport Commands loaded! Hello NeoForge!");
    }

    @Mod.EventBusSubscriber(modid = TeleportCommands.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    private static class NeoForgeEventSubscriber {

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        private static void onServerStarting(ServerStartingEvent event) {
            // initialize the mod
            TeleportCommands.initializeMod(event.getServer(), "NeoForge");
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        private static void onEntityUnload(LivingDeathEvent event) {
            // check if it is a player
            if (event.getEntity() instanceof ServerPlayer player) {
                TeleportCommands.onPlayerDeath(player);
            }
        }
    }
}

