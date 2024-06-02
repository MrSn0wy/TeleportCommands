package dev.mrsnowy.teleport_commands.commands;

import java.util.*;

import dev.mrsnowy.teleport_commands.suggestions.tpaSuggestionProvider;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import static dev.mrsnowy.teleport_commands.utils.tools.Teleporter;
import static dev.mrsnowy.teleport_commands.utils.tools.getTranslatedText;

public class tpa {

    public static final ArrayList<tpaArrayClass> tpaList = new ArrayList<>();

    public static class tpaArrayClass {
        public String InitPlayer;
        public String RecPlayer;
        boolean here;
    }

    public static void register(Commands commandManager) {

        commandManager.getDispatcher().register(Commands.literal("tpa")
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> {
                            final ServerPlayer TargetPlayer = EntityArgument.getPlayer(context, "player");
                            ServerPlayer player = context.getSource().getPlayerOrException();

                            tpaCommandHandler(player, TargetPlayer, false);
                            return 0;
                        })));

        commandManager.getDispatcher().register(Commands.literal("tpahere")
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> {
                            final ServerPlayer TargetPlayer = EntityArgument.getPlayer(context, "player");
                            ServerPlayer player = context.getSource().getPlayerOrException();


                            tpaCommandHandler(player, TargetPlayer, true);
                            return 0;
                        })));

        commandManager.getDispatcher().register(Commands.literal("tpaaccept")
                .then(Commands.argument("player", EntityArgument.player()).suggests(new tpaSuggestionProvider())
                        .executes(context -> {
                            final ServerPlayer TargetPlayer = EntityArgument.getPlayer(context, "player");
                            ServerPlayer player = context.getSource().getPlayerOrException();

                            tpaAccept(player, TargetPlayer);
                            return 0;
                        })));

        commandManager.getDispatcher().register(Commands.literal("tpadeny")
                .then(Commands.argument("player", EntityArgument.player()).suggests(new tpaSuggestionProvider())
                        .executes(context -> {
                            final ServerPlayer TargetPlayer = EntityArgument.getPlayer(context, "player");
                            ServerPlayer player = context.getSource().getPlayerOrException();

                            tpaDeny(player, TargetPlayer);
                            return 0;
                        })));
    }



    private static void tpaCommandHandler(ServerPlayer FromPlayer, ServerPlayer ToPlayer, boolean here) throws NullPointerException {
        long playerTpaList = tpa.tpaList.stream()
                .filter(tpa -> Objects.equals(FromPlayer.getStringUUID(), tpa.InitPlayer))
                .filter(tpa -> Objects.equals(ToPlayer.getStringUUID(), tpa.RecPlayer))
                .count();


        if (FromPlayer == ToPlayer) {
            FromPlayer.displayClientMessage(getTranslatedText("commands.teleport_commands.tpa.self", FromPlayer).withStyle(ChatFormatting.AQUA),true);

        } else if (playerTpaList >= 1) {
            FromPlayer.displayClientMessage(getTranslatedText("commands.teleport_commands.tpa.alreadySent", FromPlayer, Component.literal(Objects.requireNonNull(ToPlayer.getName().tryCollapseToString())).withStyle(ChatFormatting.BOLD)).withStyle(ChatFormatting.AQUA)
                    ,true
            );

        } else {
            String hereText = here ? "Here" : "";

            // Store da request
            tpaArrayClass tpaRequest = new tpaArrayClass();
            tpaRequest.InitPlayer = FromPlayer.getStringUUID();
            tpaRequest.RecPlayer = ToPlayer.getStringUUID();
            tpaRequest.here = here;
            tpaList.add(tpaRequest);

            String ReceivedFromPlayer = Objects.requireNonNull(FromPlayer.getName().tryCollapseToString());
            String SentToPlayer = Objects.requireNonNull(ToPlayer.getName().tryCollapseToString());

            FromPlayer.displayClientMessage(getTranslatedText("commands.teleport_commands.tpa.sent", FromPlayer, Component.literal(hereText), Component.literal(SentToPlayer).withStyle(ChatFormatting.BOLD))
                    //                            .append(Text.literal("\n[Cancel]").formatted(Formatting.BLUE, Formatting.BOLD))
                    ,true
            );

            ToPlayer.displayClientMessage(getTranslatedText("commands.teleport_commands.tpa.received", ToPlayer, Component.literal(hereText), Component.literal(ReceivedFromPlayer).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD)).withStyle(ChatFormatting.AQUA)
                            .append("\n")
                            .append(getTranslatedText("commands.teleport_commands.tpa.accept", ToPlayer).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD).withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/tpaaccept %s", ReceivedFromPlayer)))))
                            .append(getTranslatedText("commands.teleport_commands.tpa.deny", ToPlayer).withStyle(ChatFormatting.RED, ChatFormatting.BOLD).withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/tpadeny %s", ReceivedFromPlayer))))),
                    false
            );

            Timer timer = new Timer();
            timer.schedule(
                    new TimerTask() {
                        @Override
                        public void run() {
                            boolean successful = tpaList.remove(tpaRequest);
                            if (successful) {
                                FromPlayer.displayClientMessage(getTranslatedText("commands.teleport_commands.tpa.expired", FromPlayer, Component.literal(hereText)).withStyle(ChatFormatting.RED, ChatFormatting.BOLD),true);
                                ToPlayer.displayClientMessage(getTranslatedText("commands.teleport_commands.tpa.expired", ToPlayer, Component.literal(hereText)).withStyle(ChatFormatting.WHITE),true);
                            }
//                            else {
//                                TeleportCommands.LOGGER.error("Error removing tpaRequest from tpaList!");
//                            }
                            // else not needed since it may be cancelled
                        }
                    }, 30 * 1000 // 30 seconds
            );
        }
    }

    private static void tpaAccept(ServerPlayer FromPlayer, ServerPlayer ToPlayer) {
        if (FromPlayer == ToPlayer) {
            FromPlayer.displayClientMessage(getTranslatedText("commands.teleport_commands.tpa.self", FromPlayer).withStyle(ChatFormatting.AQUA),true);

        } else {
            Optional<tpaArrayClass> tpaStorage = tpaList.stream()
                    .filter(tpa -> Objects.equals(ToPlayer.getStringUUID(), tpa.InitPlayer))
                    .filter(tpa -> Objects.equals(FromPlayer.getStringUUID(), tpa.RecPlayer))
                    .findFirst();

            if (tpaStorage.isPresent()) {

                if (tpaStorage.get().here) {
                    Teleporter(FromPlayer, ToPlayer.serverLevel(), ToPlayer.position());

                } else {
                    Teleporter(ToPlayer, FromPlayer.serverLevel(), FromPlayer.position());
                }

                FromPlayer.displayClientMessage(getTranslatedText("commands.teleport_commands.tpa.accepted", FromPlayer).withStyle(ChatFormatting.WHITE),true);
                ToPlayer.displayClientMessage(getTranslatedText("commands.teleport_commands.tpa.accepted", ToPlayer).withStyle(ChatFormatting.GREEN),true);

                tpaList.remove(tpaStorage.get());
            } else {
                FromPlayer.displayClientMessage(getTranslatedText("commands.teleport_commands.tpa.notFound", FromPlayer).withStyle(ChatFormatting.RED),true);
            }
        }
    }

    private static void tpaDeny(ServerPlayer FromPlayer, ServerPlayer ToPlayer) {
        if (FromPlayer == ToPlayer) {
            FromPlayer.displayClientMessage(getTranslatedText("commands.teleport_commands.tpa.self", FromPlayer).withStyle(ChatFormatting.AQUA),true);

        } else {
            Optional<tpaArrayClass> tpaStorage = tpaList.stream()
                    .filter(tpa -> Objects.equals(ToPlayer.getStringUUID(), tpa.InitPlayer))
                    .filter(tpa -> Objects.equals(FromPlayer.getStringUUID(), tpa.RecPlayer))
                    .findFirst();

            if (tpaStorage.isPresent()) {
                tpaList.remove(tpaStorage.get());

                ToPlayer.displayClientMessage(getTranslatedText("commands.teleport_commands.tpa.denied", ToPlayer).withStyle(ChatFormatting.RED, ChatFormatting.BOLD),true);
                FromPlayer.displayClientMessage(getTranslatedText("commands.teleport_commands.tpa.denied", FromPlayer).withStyle(ChatFormatting.WHITE),true);

            } else {
                FromPlayer.displayClientMessage(getTranslatedText("commands.teleport_commands.tpa.notFound", FromPlayer).withStyle(ChatFormatting.RED),true);
            }
        }
    }
}
