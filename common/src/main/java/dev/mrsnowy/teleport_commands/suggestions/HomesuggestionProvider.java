package dev.mrsnowy.teleport_commands.suggestions;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.mrsnowy.teleport_commands.TeleportCommands;
import dev.mrsnowy.teleport_commands.storage.StorageManager;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import static dev.mrsnowy.teleport_commands.storage.StorageManager.GetPlayerStorage;

public class HomesuggestionProvider implements SuggestionProvider<CommandSourceStack> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        try {
            ServerPlayer player = Objects.requireNonNull(context.getSource().getPlayer());
            StorageManager.StorageClass.Player playerStorage = GetPlayerStorage(player.getStringUUID()).playerStorage;

            for (StorageManager.StorageClass.Player.Home currenthome : playerStorage.Homes) {
                builder.suggest(currenthome.name);
            }

            // Build and return the suggestions
            return builder.buildFuture();
        } catch (Exception e) {
            TeleportCommands.LOGGER.error("Error getting suggestions!");
            return null;
        }
    }
}