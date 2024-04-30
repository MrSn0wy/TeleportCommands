package dev.mrsnowy.teleport_commands.utils;

import com.mojang.brigadier.arguments.StringArgumentType;
import dev.mrsnowy.teleport_commands.TeleportCommands;
import dev.mrsnowy.teleport_commands.suggestions.HomesuggestionProvider;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import javax.swing.text.html.parser.Entity;
import java.util.Objects;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class commands {
    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("back")
                .executes(context -> {
                    ServerPlayerEntity player = Objects.requireNonNull(context.getSource().getPlayer());
                    try {
                        player.sendMessage(Text.literal("Teleporting"), true);
                        tools.ToDeathLocation(player);
                    } catch (Exception e) {
                        TeleportCommands.LOGGER.error(String.valueOf(e));
                        player.sendMessage(Text.literal("Error Teleporting!").formatted(Formatting.RED, Formatting.BOLD), true);
                        return 1;
                    }
                    return 0;
                })));


        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("sethome")
                .then(argument("name", StringArgumentType.string())
                        .executes(context -> {
                            final String name = StringArgumentType.getString(context, "name");
                            ServerPlayerEntity player = Objects.requireNonNull(context.getSource().getPlayer());

                            try {
                                player.sendMessage(Text.literal("Home Set"), true);
                                tools.SetHome(player, name);

                            } catch (Exception e) {
                                TeleportCommands.LOGGER.error(String.valueOf(e));
                                player.sendMessage(Text.literal("Error Setting Home!").formatted(Formatting.RED, Formatting.BOLD), true);
                                return 1;
                            }
                            return 0;
                        }))));

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("home")
                .executes(context -> {
                    ServerPlayerEntity player = Objects.requireNonNull(context.getSource().getPlayer());

                    try {
                        player.sendMessage(Text.literal("Going Home"), true);
                        tools.GoHome(player, "");

                    } catch (Exception e) {
                        TeleportCommands.LOGGER.error(String.valueOf(e));
                        player.sendMessage(Text.literal("Error Going Home!").formatted(Formatting.RED, Formatting.BOLD), true);
                        return 1;
                    }
                    return 0;
                })
                .then(argument("name", StringArgumentType.string()).suggests(new HomesuggestionProvider())
                        .executes(context -> {
                            final String name = StringArgumentType.getString(context, "name");
                            ServerPlayerEntity player = Objects.requireNonNull(context.getSource().getPlayer());

                            try {
                                player.sendMessage(Text.literal("Going Home"), true);
                                tools.GoHome(player, name);

                            } catch (Exception e) {
                                player.sendMessage(Text.literal("Error Going Home!").formatted(Formatting.RED, Formatting.BOLD), true);
                                TeleportCommands.LOGGER.error(String.valueOf(e));
                                return 1;
                            }
                            return 0;
                        }))));

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("delhome")
                .then(argument("name", StringArgumentType.string()).suggests(new HomesuggestionProvider())
                        .executes(context -> {
                            final String name = StringArgumentType.getString(context, "name");
                            ServerPlayerEntity player = Objects.requireNonNull(context.getSource().getPlayer());

                            try {
                                player.sendMessage(Text.literal("Home Deleted"), true);
                                tools.DeleteHome(player, name);

                            } catch (Exception e) {
                                TeleportCommands.LOGGER.error(String.valueOf(e));
                                player.sendMessage(Text.literal("Error Deleting Home!").formatted(Formatting.RED, Formatting.BOLD), true);
                                return 1;
                            }
                            return 0;
                        }))));

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("renamehome")
                .then(argument("name", StringArgumentType.string()).suggests(new HomesuggestionProvider())
                .then(argument("newName", StringArgumentType.string())
                        .executes(context -> {
                            final String name = StringArgumentType.getString(context, "name");
                            final String newName = StringArgumentType.getString(context, "newName");
                            ServerPlayerEntity player = Objects.requireNonNull(context.getSource().getPlayer());

                            try {
                                player.sendMessage(Text.literal("Home Renamed"), true);
                                tools.RenameHome(player, name, newName);
                            } catch (Exception e) {
                                TeleportCommands.LOGGER.error(String.valueOf(e));
                                player.sendMessage(Text.literal("Error Renaming Home!").formatted(Formatting.RED, Formatting.BOLD), true);
                                return 1;
                            }
                            return 0;
                        })))));


        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("homes")
                .executes(context -> {
                    ServerPlayerEntity player = Objects.requireNonNull(context.getSource().getPlayer());
                    try {
                        tools.PrintHomes(player);
                    } catch (Exception e) {
                        TeleportCommands.LOGGER.error(String.valueOf(e));
                        player.sendMessage(Text.literal("Error Getting Homes!").formatted(Formatting.RED, Formatting.BOLD), true);
                        return 1;
                    }
                    return 0;
                })));

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("defaulthome")
                .then(argument("name", StringArgumentType.string()).suggests(new HomesuggestionProvider())
                .executes(context -> {
                    final String name = StringArgumentType.getString(context, "name");
                    ServerPlayerEntity player = Objects.requireNonNull(context.getSource().getPlayer());
                    try {
                        tools.SetDefaultHome(player, name);
                    } catch (Exception e) {
                        TeleportCommands.LOGGER.error(String.valueOf(e));
                        player.sendMessage(Text.literal("Error Changing Default Home!").formatted(Formatting.RED, Formatting.BOLD), true);
                        return 1;
                    }
                    return 0;
                }))));

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("tpa")
                .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(context -> {
                            final ServerPlayerEntity TargetPlayer = EntityArgumentType.getPlayer(context, "player");
                            ServerPlayerEntity player = Objects.requireNonNull(context.getSource().getPlayer());

                            tools.tpaCommandHandler(player, TargetPlayer, false);
                            return 0;
                        }))));

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("tpahere")
                .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(context -> {
                            final ServerPlayerEntity TargetPlayer = EntityArgumentType.getPlayer(context, "player");
                            ServerPlayerEntity player = Objects.requireNonNull(context.getSource().getPlayer());

                            tools.tpaCommandHandler(player, TargetPlayer, true);
                            return 0;
                        }))));

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("tpaaccept")
                .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(context -> {
                            final ServerPlayerEntity TargetPlayer = EntityArgumentType.getPlayer(context, "player");
                            ServerPlayerEntity player = Objects.requireNonNull(context.getSource().getPlayer());

                            tools.tpaAccept(player, TargetPlayer);
                            return 0;
                        }))));

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("tpadeny")
                .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(context -> {
                            final ServerPlayerEntity TargetPlayer = EntityArgumentType.getPlayer(context, "player");
                            ServerPlayerEntity player = Objects.requireNonNull(context.getSource().getPlayer());

                            tools.tpaDeny(player, TargetPlayer);
                            return 0;
                        }))));
    }
}
