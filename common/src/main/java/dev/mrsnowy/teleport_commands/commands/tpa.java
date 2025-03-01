package dev.mrsnowy.teleport_commands.commands;

import java.util.*;

import dev.mrsnowy.teleport_commands.TeleportCommands;
import dev.mrsnowy.teleport_commands.suggestions.tpaSuggestionProvider;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import static dev.mrsnowy.teleport_commands.utils.tools.*;

public class tpa {

    public static final ArrayList<tpaArrayClass> tpaList = new ArrayList<>();

    public static class tpaArrayClass {
        public final String InitPlayer;
        public final String RecPlayer;
        final boolean here;

        public tpaArrayClass(String initPlayer, String recPlayer, boolean here) {
            InitPlayer = initPlayer;
            RecPlayer = recPlayer;
            this.here = here;
            tpaList.add(this);
        }
    }

    public static void register(Commands commandManager) {

        commandManager.getDispatcher().register(Commands.literal("tpa")
                .requires(source -> source.getPlayer() != null)
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> {
                            final ServerPlayer TargetPlayer = EntityArgument.getPlayer(context, "player");
                            final ServerPlayer player = context.getSource().getPlayerOrException();

                            try {
                                tpaCommandHandler(player, TargetPlayer, false);

                            } catch (Exception e) {
                                // this shouldn't happen with any of these commands, but if it does happen I am at least printing it to the logs and catching it.
                                // if it appears that this can happen then I'll add error messages for the client, for now the default minecraft ones will do
                                TeleportCommands.LOGGER.error("Error while sending a tpa request! => ", e);
                                return 1;
                            }
                            return 0;
                        })));

        commandManager.getDispatcher().register(Commands.literal("tpahere")
                .requires(source -> source.getPlayer() != null)
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> {
                            final ServerPlayer TargetPlayer = EntityArgument.getPlayer(context, "player");
                            final ServerPlayer player = context.getSource().getPlayerOrException();

                            try {
                                tpaCommandHandler(player, TargetPlayer, true);

                            } catch (Exception e) {
                                TeleportCommands.LOGGER.error("Error while sending a tpahere request! => ", e);
                                return 1;
                            }
                            return 0;
                        })));

        commandManager.getDispatcher().register(Commands.literal("tpaaccept")
                .requires(source -> source.getPlayer() != null)
                .then(Commands.argument("player", EntityArgument.player()).suggests(new tpaSuggestionProvider())
                        .executes(context -> {
                            final ServerPlayer TargetPlayer = EntityArgument.getPlayer(context, "player");
                            final ServerPlayer player = context.getSource().getPlayerOrException();

                            try {
                                tpaAccept(player, TargetPlayer);

                            } catch (Exception e) {
                                TeleportCommands.LOGGER.error("Error while accepting a tpa(here) request! => ", e);
                                return 1;
                            }

                            return 0;
                        })));

        commandManager.getDispatcher().register(Commands.literal("tpadeny")
                .requires(source -> source.getPlayer() != null)
                .then(Commands.argument("player", EntityArgument.player()).suggests(new tpaSuggestionProvider())
                        .executes(context -> {
                            final ServerPlayer TargetPlayer = EntityArgument.getPlayer(context, "player");
                            final ServerPlayer player = context.getSource().getPlayerOrException();

                            try {
                                tpaDeny(player, TargetPlayer);

                            } catch (Exception e) {
                                TeleportCommands.LOGGER.error("Error while denying a tpa(here) request! => ", e);
                                player.displayClientMessage(getTranslatedText("commands.teleport_commands.home.setError", player).withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true);
                                return 1;
                            }

                            return 0;
                        })));
    }


    private static void tpaCommandHandler(ServerPlayer FromPlayer, ServerPlayer ToPlayer, boolean here) throws NullPointerException {
        long playerTpaList = tpa.tpaList.stream()
                .filter(tpa -> Objects.equals(FromPlayer.getStringUUID(), tpa.InitPlayer))
                .filter(tpa -> Objects.equals(ToPlayer.getStringUUID(), tpa.RecPlayer))
                .count();

        if (FromPlayer == ToPlayer) {
            FromPlayer.displayClientMessage(getTranslatedText("commands.teleport_commands.tpa.self", FromPlayer).withStyle(ChatFormatting.AQUA), true);

        } else if (playerTpaList >= 1) {
            FromPlayer.displayClientMessage(getTranslatedText("commands.teleport_commands.tpa.alreadySent", FromPlayer, Component.literal(Objects.requireNonNull(ToPlayer.getName().getString(), "ToPlayer name cannot be null")).withStyle(ChatFormatting.BOLD)).withStyle(ChatFormatting.AQUA)
                    , true
            );

        } else {
            String hereText = here ? "Here" : "";

            // Store da request
            tpaArrayClass tpaRequest = new tpaArrayClass( FromPlayer.getStringUUID(), ToPlayer.getStringUUID(), here );

            String ReceivedFromPlayer = Objects.requireNonNull(FromPlayer.getName().getString(), "FromPlayer name cannot be null");
            String SentToPlayer = Objects.requireNonNull(ToPlayer.getName().getString(), "ToPlayer name cannot be null");

            FromPlayer.displayClientMessage(getTranslatedText("commands.teleport_commands.tpa.sent", FromPlayer, Component.literal(hereText), Component.literal(SentToPlayer).withStyle(ChatFormatting.BOLD))
                    //                            .append(Text.literal("\n[Cancel]").formatted(Formatting.BLUE, Formatting.BOLD))
                    ,true
            );

            ToPlayer.displayClientMessage(getTranslatedText("commands.teleport_commands.tpa.received", ToPlayer, Component.literal(hereText), Component.literal(ReceivedFromPlayer).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD)).withStyle(ChatFormatting.AQUA)
                    .append("\n")
                    .append(getTranslatedText("commands.teleport_commands.tpa.accept", ToPlayer).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD).withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/tpaaccept %s", ReceivedFromPlayer)))))
                    .append(" ")
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
                            // else not needed since it may be denied/cancelled
                        }
                    }, 30 * 1000 // 30 seconds
            );
        }
    }

    private static void tpaAccept(ServerPlayer FromPlayer, ServerPlayer ToPlayer) {
        if (FromPlayer == ToPlayer) {
            FromPlayer.displayClientMessage(getTranslatedText("commands.teleport_commands.tpa.self", FromPlayer).withStyle(ChatFormatting.AQUA), true);
            return;
        }

        // Check if there is a request
        Optional<tpaArrayClass> tpaStorage = tpaList.stream()
                .filter(tpa -> Objects.equals(ToPlayer.getStringUUID(), tpa.InitPlayer))
                .filter(tpa -> Objects.equals(FromPlayer.getStringUUID(), tpa.RecPlayer))
                .findFirst();

        if (tpaStorage.isPresent()) {
            // Request found
            ServerPlayer destinationPlayer = tpaStorage.get().here ? ToPlayer : FromPlayer;
            ServerPlayer toSentPlayer = tpaStorage.get().here ? FromPlayer : ToPlayer;

            Optional<BlockPos> teleportData = getSafeBlockPos(destinationPlayer.blockPosition(), destinationPlayer.serverLevel());

            if (teleportData.isPresent()) {
                BlockPos safeBlockPos = teleportData.get();
                Vec3 teleportPos = new Vec3(safeBlockPos.getX() + 0.5, safeBlockPos.getY(), safeBlockPos.getZ() + 0.5);

                Teleporter(toSentPlayer, destinationPlayer.serverLevel(), teleportPos);
            } else {
                // if no safe location then just teleport to the player
                Teleporter(toSentPlayer, destinationPlayer.serverLevel(), destinationPlayer.position());
            }

            // if the player teleported then these messages get sent && the request gets removed
            FromPlayer.displayClientMessage(getTranslatedText("commands.teleport_commands.tpa.accepted", FromPlayer).withStyle(ChatFormatting.WHITE),true);
            ToPlayer.displayClientMessage(getTranslatedText("commands.teleport_commands.tpa.accepted", ToPlayer).withStyle(ChatFormatting.GREEN),true);
            tpaList.remove(tpaStorage.get());

        } else {
            // No request found
            FromPlayer.displayClientMessage(getTranslatedText("commands.teleport_commands.tpa.notFound", FromPlayer).withStyle(ChatFormatting.RED),true);
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
