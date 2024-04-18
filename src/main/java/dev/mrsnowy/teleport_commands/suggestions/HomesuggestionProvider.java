package dev.mrsnowy.teleport_commands.suggestions;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.mrsnowy.teleport_commands.storage.StorageManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static dev.mrsnowy.teleport_commands.storage.StorageManager.GetPlayerStorage;

public class HomesuggestionProvider implements SuggestionProvider<ServerCommandSource> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {

        ServerPlayerEntity player = Objects.requireNonNull(context.getSource().getPlayer());
        StorageManager.StorageClass.Player playerStorage = GetPlayerStorage(player.getUuidAsString()).playerStorage;

        for (StorageManager.StorageClass.Player.Home currenthome : playerStorage.Homes) {
            builder.suggest(currenthome.name);
        }

        // Build and return the suggestions
        return builder.buildFuture();
    }
}