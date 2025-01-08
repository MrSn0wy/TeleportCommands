package dev.mrsnowy.teleport_commands.suggestions;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import dev.mrsnowy.teleport_commands.TeleportCommands;
import dev.mrsnowy.teleport_commands.storage.StorageManager;
import dev.mrsnowy.teleport_commands.storage.classes.NamedLocation;

import net.minecraft.commands.CommandSourceStack;

import java.util.List;
import java.util.concurrent.CompletableFuture;


public class WarpSuggestionProvider implements SuggestionProvider<CommandSourceStack> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        try {
            List<NamedLocation> WarpStorage = StorageManager.STORAGE.getWarps();

            for (NamedLocation currentWarp : WarpStorage) {
                builder.suggest(currentWarp.getName());
            }

            // Build and return the suggestions
            return builder.buildFuture();
        } catch (Exception e) {
            TeleportCommands.LOGGER.error("Error getting warp suggestions! ", e);
            return null;
        }
    }
}