package dev.mrsnowy.teleport_commands.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.mrsnowy.teleport_commands.Constants;
import dev.mrsnowy.teleport_commands.TeleportCommands;
import dev.mrsnowy.teleport_commands.storage.ConfigManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.Arrays;

import static dev.mrsnowy.teleport_commands.utils.tools.*;

public class main {

    // TODO! Make this automatically generate based on the commands we have
    private static final String[] available_commands = {
            "back",
            "home",
            "tpa",
            "warp",
            "worldspawn"
    };

    // sum lists
    private static final String[] enabled_commands = available_commands; // TODO! get enabled commands
    private static final String[] disabled_commands = available_commands; // TODO! get disabled commands

    // Create sum suggestion providers
    private static final SuggestionProvider<CommandSourceStack> disabled_commands_suggester = (context, builder) -> SharedSuggestionProvider.suggest(disabled_commands, builder);
    private static final SuggestionProvider<CommandSourceStack> enabled_commands_suggester = (context, builder) -> SharedSuggestionProvider.suggest(enabled_commands, builder);


    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(Commands.literal("teleportcommands")
                .then(Commands.literal("reloadConfig")
                        .executes(context -> {
                            try {
                                ConfigManager.ConfigLoader();
                            } catch (Exception e) {
                                Constants.LOGGER.error("Failed to reload config!", e);
                                throw new SimpleCommandExceptionType(Component.literal(e.toString())).create();
                            }
                            return 0;
                        }))
            .then(Commands.literal("disable")
                .then(Commands.argument("command", StringArgumentType.word())
                    .suggests(enabled_commands_suggester)
                    .requires(source -> source.hasPermission(4)) // Require OP
                    .requires(source -> source.getPlayer() != null)
                    .executes(context -> {
                        final ServerPlayer player = context.getSource().getPlayerOrException();
                        final String string = StringArgumentType.getString(context, "command");

                        // todo! maybe move this check outside for reusability?
                        if (!Arrays.asList(enabled_commands).contains(string)) {
                            // TODO! make this translatable
                            throw new SimpleCommandExceptionType(Component.literal("\"" + string + "\" is not available as an command!").withStyle(ChatFormatting.RED, ChatFormatting.BOLD)).create();
                        }

                        try {
                            player.displayClientMessage(Component.literal("meow " + string), true);

                        } catch (Exception e) {
                            Constants.LOGGER.error("Error while disabling a command! => ", e);
                            // TODO replace the error below with something normal?
                            throw new SimpleCommandExceptionType(getTranslatedText("commands.teleport_commands.common.error", player).withStyle(ChatFormatting.RED, ChatFormatting.BOLD)).create();
                        }
                        return 0;
                    })
                ))
            .then(Commands.literal("enable")
                .then(Commands.argument("command", StringArgumentType.word())
                    .suggests(disabled_commands_suggester)
                    .requires(source -> source.hasPermission(4)) // Require OP
                    .requires(source -> source.getPlayer() != null)
                    .executes(context -> {
                        final ServerPlayer player = context.getSource().getPlayerOrException();
                        final String string = StringArgumentType.getString(context, "command");

                        // todo! maybe move this check outside for reusability?
                        if (!Arrays.asList(disabled_commands).contains(string)) {
                            // TODO! make this translatable
                            throw new SimpleCommandExceptionType(Component.literal("\"" + string + "\" is not available as an command!")).create();
                        }

                        try {
                            player.displayClientMessage(Component.literal("meow " + string), true);

                        } catch (Exception e) {
                            Constants.LOGGER.error("Error while disabling a command! => ", e);
                            // TODO replace the error below with something normal?
                            throw new SimpleCommandExceptionType(getTranslatedText("commands.teleport_commands.common.error", player).withStyle(ChatFormatting.RED, ChatFormatting.BOLD)).create();
                        }
                        return 0;
                    })
                ))
            // Todo! Is this still needed?
            .then(Commands.literal("reload")
                .requires(source -> source.hasPermission(4)) // Require OP
                .executes(context -> {
                    TeleportCommands.registerCommands(context.getSource().dispatcher());
                    return 0;
                }))

            .then(Commands.literal("help")
                .executes(context -> {
                    context.getSource().sendSuccess(main::printCommands, false);
                    return 0;
                }))
        );
    }


    // -----


    private static MutableComponent printCommands()  {
        MutableComponent message = Component.empty();

        message.append(Component.literal("Thank you for using Teleport Commands (V" + Constants.VERSION + ")!").withStyle(ChatFormatting.AQUA));
        message.append(Component.literal("Teleport Commands is a server-side mod that adds various teleportation related commands").withStyle(ChatFormatting.AQUA));

        message.append(Component.literal("----").withStyle(ChatFormatting.AQUA));
        message.append(Component.literal("Usage:").withStyle(ChatFormatting.AQUA));

        return message;
    }
}
