package dev.mrsnowy.teleport_commands.commands;

import dev.mrsnowy.teleport_commands.TeleportCommands;
import java.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

import static dev.mrsnowy.teleport_commands.utils.tools.Teleporter;
import static dev.mrsnowy.teleport_commands.utils.tools.getTranslatedText;

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
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> {
                            final ServerPlayer TargetPlayer = EntityArgument.getPlayer(context, "player");
                            ServerPlayer player = context.getSource().getPlayerOrException();

                            tpaAccept(player, TargetPlayer);
                            return 0;
                        })));

        commandManager.getDispatcher().register(Commands.literal("tpadeny")
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> {
                            final ServerPlayer TargetPlayer = EntityArgument.getPlayer(context, "player");
                            ServerPlayer player = context.getSource().getPlayerOrException();

                            tpaDeny(player, TargetPlayer);
                            return 0;
                        })));
    }



    private static void tpaCommandHandler(ServerPlayer FromPlayer, ServerPlayer ToPlayer, boolean here) throws NullPointerException {

        if (FromPlayer == ToPlayer) {
            FromPlayer.displayClientMessage(getTranslatedText("commands.teleport_commands.tpa.self", FromPlayer).withStyle(ChatFormatting.AQUA),true);

        } else {
            String hereText = here ? "Here" : "";
            MutableComponent ReceivedFromMessage = getTranslatedText("commands.teleport_commands.tpa.received", ToPlayer, hereText);
            MutableComponent SentToMessage = getTranslatedText("commands.teleport_commands.tpa.sent", FromPlayer, hereText);

            // Store da request
            tpaArrayClass tpaRequest = new tpaArrayClass();
            tpaRequest.InitPlayer = FromPlayer.getStringUUID();
            tpaRequest.RecPlayer = ToPlayer.getStringUUID();
            tpaRequest.here = here;
            tpaList.add(tpaRequest);

            String ReceivedFromPlayer = Objects.requireNonNull(FromPlayer.getName().tryCollapseToString());
            String SentToPlayer = Objects.requireNonNull(ToPlayer.getName().tryCollapseToString());

            FromPlayer.displayClientMessage(SentToMessage
                            .append(Component.literal(SentToPlayer).withStyle(ChatFormatting.BOLD))
                    //                            .append(Text.literal("\n[Cancel]").formatted(Formatting.BLUE, Formatting.BOLD))
                    ,true
            );

            ToPlayer.displayClientMessage(ReceivedFromMessage.withStyle(ChatFormatting.AQUA)
                            .append(Component.literal(ReceivedFromPlayer).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
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
                                FromPlayer.displayClientMessage(getTranslatedText("commands.teleport_commands.tpa.expired", FromPlayer, hereText).withStyle(ChatFormatting.RED, ChatFormatting.BOLD),true);
                            }
                        }
                    }, 30 * 1000
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
                    FromPlayer.displayClientMessage(getTranslatedText("commands.teleport_commands.common.teleport", FromPlayer).withStyle(ChatFormatting.AQUA),true);
                    ToPlayer.displayClientMessage(getTranslatedText("commands.teleport_commands.tpa.accepted", ToPlayer).withStyle(ChatFormatting.GREEN),true);

                    Teleporter(FromPlayer, ToPlayer.serverLevel(), ToPlayer.position());
                } else {
                    ToPlayer.displayClientMessage(getTranslatedText("commands.teleport_commands.common.teleport", ToPlayer).withStyle(ChatFormatting.AQUA),true);
                    FromPlayer.displayClientMessage(getTranslatedText("commands.teleport_commands.tpa.accepted", FromPlayer).withStyle(ChatFormatting.GREEN),true);

                    Teleporter(ToPlayer, FromPlayer.serverLevel(), FromPlayer.position());
                }

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

                if (tpaStorage.get().here) {
                    ToPlayer.displayClientMessage(getTranslatedText("commands.teleport_commands.tpa.denied", ToPlayer).withStyle(ChatFormatting.RED, ChatFormatting.BOLD),true);
                } else {
                    FromPlayer.displayClientMessage(getTranslatedText("commands.teleport_commands.tpa.denied", FromPlayer).withStyle(ChatFormatting.RED, ChatFormatting.BOLD),true);
                }
            } else {
                FromPlayer.displayClientMessage(getTranslatedText("commands.teleport_commands.tpa.notFound", FromPlayer).withStyle(ChatFormatting.RED),true);
            }
        }
    }
}
