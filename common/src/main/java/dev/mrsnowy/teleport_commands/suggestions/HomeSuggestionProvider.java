package dev.mrsnowy.teleport_commands.suggestions;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.mrsnowy.teleport_commands.Constants;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import dev.mrsnowy.teleport_commands.TeleportCommands;
import dev.mrsnowy.teleport_commands.common.NamedLocation;
import dev.mrsnowy.teleport_commands.common.Player;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;


public class HomeSuggestionProvider implements SuggestionProvider<CommandSourceStack> {
    private final TeleportCommands teleportCommands;

    public HomeSuggestionProvider(TeleportCommands teleportCommands) {
        this.teleportCommands = teleportCommands;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            Optional<Player> optionalPlayerStorage = teleportCommands.storageManager.storage.getPlayer(player.getStringUUID());

            if (optionalPlayerStorage.isPresent()) {
                Player playerStorage = optionalPlayerStorage.get();

                for (NamedLocation currentHome : playerStorage.getHomes()) {
                    builder.suggest(currentHome.getName());
                }
            }

            // Build and return the suggestions
            return builder.buildFuture();
        } catch (Exception e) {
            Constants.LOGGER.error("Error getting home suggestions! ", e);
            return null;
        }
    }
}