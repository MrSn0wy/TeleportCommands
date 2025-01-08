package dev.mrsnowy.teleport_commands.suggestions;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.mrsnowy.teleport_commands.TeleportCommands;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import dev.mrsnowy.teleport_commands.storage.classes.NamedLocation;
import dev.mrsnowy.teleport_commands.storage.classes.Player;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import static dev.mrsnowy.teleport_commands.storage.StorageManager.STORAGE;

public class HomeSuggestionProvider implements SuggestionProvider<CommandSourceStack> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            Optional<Player> optionalPlayerStorage = STORAGE.getPlayer(player.getStringUUID());

            if (optionalPlayerStorage.isPresent()) {
                Player PlayerStorage = optionalPlayerStorage.get();

                for (NamedLocation currentHome : PlayerStorage.getHomes()) {
                    builder.suggest(currentHome.getName());
                }
            }

            // Build and return the suggestions
            return builder.buildFuture();
        } catch (Exception e) {
            TeleportCommands.LOGGER.error("Error getting home suggestions! ", e);
            return null;
        }
    }
}