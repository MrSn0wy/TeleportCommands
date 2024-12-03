package dev.mrsnowy.teleport_commands.suggestions;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.mrsnowy.teleport_commands.TeleportCommands;
import dev.mrsnowy.teleport_commands.storage.StorageManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static dev.mrsnowy.teleport_commands.storage.StorageManager.GetPlayerStorage;
import static dev.mrsnowy.teleport_commands.storage.StorageManager.getWarpStorage;

public class WarpSuggestionProvider implements SuggestionProvider<CommandSourceStack> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        try {
            List<StorageManager.StorageClass.NamedLocation> WarpStorage = getWarpStorage().getSecond();

            for (StorageManager.StorageClass.NamedLocation currentWarp :  WarpStorage) {
                builder.suggest(currentWarp.name);
            }

            // Build and return the suggestions
            return builder.buildFuture();
        } catch (Exception e) {
            TeleportCommands.LOGGER.error("Error getting suggestions!");
            return null;
        }
    }
}