package dev.mrsnowy.teleport_commands.commands;

import dev.mrsnowy.teleport_commands.TeleportCommands;
import java.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import static dev.mrsnowy.teleport_commands.utils.tools.Teleporter;

public class tpa {

    private static final ArrayList<tpaArrayClass> tpaList = new ArrayList<>();

    private static class tpaArrayClass {
        private String InitPlayer;
        private String RecPlayer;
        private boolean here;
    }

    public static void register(Commands commandManager) {

        commandManager.getDispatcher().register(Commands.literal("tpa")
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> {
                            final ServerPlayer TargetPlayer = EntityArgument.getPlayer(context, "player");
                            ServerPlayer player = context.getSource().getPlayer();

                            if (player == null) {
                                TeleportCommands.LOGGER.error("Error while executing the command, No player found!");
                                return 1;
                            }

                            tpaCommandHandler(player, TargetPlayer, false);
                            return 0;
                        })));

        commandManager.getDispatcher().register(Commands.literal("tpahere")
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> {
                            final ServerPlayer TargetPlayer = EntityArgument.getPlayer(context, "player");
                            ServerPlayer player = context.getSource().getPlayer();

                            if (player == null) {
                                TeleportCommands.LOGGER.error("Error while executing the command, No player found!");
                                return 1;
                            }

                            tpaCommandHandler(player, TargetPlayer, true);
                            return 0;
                        })));

        commandManager.getDispatcher().register(Commands.literal("tpaaccept")
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> {
                            final ServerPlayer TargetPlayer = EntityArgument.getPlayer(context, "player");
                            ServerPlayer player = context.getSource().getPlayer();

                            if (player == null) {
                                TeleportCommands.LOGGER.error("Error while executing the command, No player found!");
                                return 1;
                            }

                            tpaAccept(player, TargetPlayer);
                            return 0;
                        })));

        commandManager.getDispatcher().register(Commands.literal("tpadeny")
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> {
                            final ServerPlayer TargetPlayer = EntityArgument.getPlayer(context, "player");
                            ServerPlayer player = context.getSource().getPlayer();

                            if (player == null) {
                                TeleportCommands.LOGGER.error("Error while executing the command, No player found!");
                                return 1;
                            }

                            tpaDeny(player, TargetPlayer);
                            return 0;
                        })));
    }



    private static void tpaCommandHandler(ServerPlayer FromPlayer, ServerPlayer ToPlayer,  boolean here) {

        if (FromPlayer == ToPlayer) {
            FromPlayer.displayClientMessage(Component.literal("Well, that was easy").withStyle(ChatFormatting.AQUA),true);

        } else {
            String ToMessage;
            String FromMessage;

            // Store da request
            tpaArrayClass tpaRequest = new tpaArrayClass();
            tpaRequest.InitPlayer = FromPlayer.getStringUUID();
            tpaRequest.RecPlayer = ToPlayer.getStringUUID();
            tpaRequest.here = here;
            tpaList.add(tpaRequest);

            if (here) {
                ToMessage = "TpaHere Request Received From ";
                FromMessage = "TpaHere Request Send to ";

            } else {
                ToMessage = "Tpa Request Received From ";
                FromMessage = "Tpa Request Send to ";
            }

            String ToPlayerString = Objects.requireNonNull(ToPlayer.getName().tryCollapseToString());
            String FromPlayerString = Objects.requireNonNull(FromPlayer.getName().tryCollapseToString());

            FromPlayer.displayClientMessage(Component.literal(FromMessage).withStyle(ChatFormatting.AQUA)
                            .append(Component.literal(ToPlayerString).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
//                            .append(Text.literal("\n[Cancel]").formatted(Formatting.BLUE, Formatting.BOLD))
                    ,true
            );

            ToPlayer.displayClientMessage(Component.literal(ToMessage).withStyle(ChatFormatting.AQUA)
                            .append(Component.literal(FromPlayerString).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                            .append(Component.literal("\n[Accept]").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD).withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/tpaaccept %s", FromPlayerString)))))
                            .append(Component.literal(" [Deny]").withStyle(ChatFormatting.RED, ChatFormatting.BOLD).withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/tpadeny %s", FromPlayerString))))),
                    false
            );

            Timer timer = new Timer();
            timer.schedule(
                    new TimerTask() {
                        @Override
                        public void run() {
                            boolean successful = tpaList.remove(tpaRequest);
                            if (successful) {
                                if (here) {
                                    FromPlayer.displayClientMessage(Component.literal("TpaHere Request Expired").withStyle(ChatFormatting.AQUA),true);
                                } else {
                                    FromPlayer.displayClientMessage(Component.literal("Tpa Request Expired").withStyle(ChatFormatting.AQUA),true);
                                }
                            }
                        }
                    }, 30 * 1000
            );
        }
    }

    private static void tpaAccept(ServerPlayer FromPlayer, ServerPlayer ToPlayer) {
        if (FromPlayer == ToPlayer) {
            FromPlayer.displayClientMessage(Component.literal("Uhm.. no?").withStyle(ChatFormatting.AQUA),true);
        } else {
            Optional<tpaArrayClass> tpaStorage = tpaList.stream()
                    .filter(tpa -> Objects.equals(ToPlayer.getStringUUID(), tpa.InitPlayer))
                    .filter(tpa -> Objects.equals(FromPlayer.getStringUUID(), tpa.RecPlayer))
                    .findFirst();

            if (tpaStorage.isPresent()) {
                if (tpaStorage.get().here) {
                    FromPlayer.displayClientMessage(Component.literal("Teleporting"),true);
                    ToPlayer.displayClientMessage(Component.literal("Request Accepted"),true);

                    Teleporter(FromPlayer, ToPlayer.serverLevel(), ToPlayer.position());
                } else {
                    ToPlayer.displayClientMessage(Component.literal("Teleporting"),true);
                    FromPlayer.displayClientMessage(Component.literal("Request Accepted"),true);

                    Teleporter(ToPlayer, FromPlayer.serverLevel(), FromPlayer.position());
                }

                tpaList.remove(tpaStorage.get());
            } else {
                FromPlayer.displayClientMessage(Component.literal("No Requests found!").withStyle(ChatFormatting.AQUA),true);
            }
        }
    }

    private static void tpaDeny(ServerPlayer FromPlayer, ServerPlayer ToPlayer) {
        if (FromPlayer == ToPlayer) {
            FromPlayer.displayClientMessage(Component.literal("Uhm.. no?").withStyle(ChatFormatting.AQUA),true);
        } else {
            Optional<tpaArrayClass> tpaStorage = tpaList.stream()
                    .filter(tpa -> Objects.equals(ToPlayer.getStringUUID(), tpa.InitPlayer))
                    .filter(tpa -> Objects.equals(FromPlayer.getStringUUID(), tpa.RecPlayer))
                    .findFirst();

            if (tpaStorage.isPresent()) {
                tpaList.remove(tpaStorage.get());

                if (tpaStorage.get().here) {
                    ToPlayer.displayClientMessage(Component.literal("Request Denied").withStyle(ChatFormatting.AQUA),true);
                } else {
                    FromPlayer.displayClientMessage(Component.literal("Request Denied").withStyle(ChatFormatting.AQUA),true);
                }
            } else {
                FromPlayer.displayClientMessage(Component.literal("No Requests found!").withStyle(ChatFormatting.AQUA),true);
            }
        }
    }
}
