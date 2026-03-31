package dev.mrsnowy.teleport_commands.mixin;

import dev.mrsnowy.teleport_commands.TeleportCommands;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public class ServerStartMixin {

    @Inject(method = "runServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;buildServerStatus()Lnet/minecraft/network/protocol/status/ServerStatus;", ordinal = 0))
    private void runServer(CallbackInfo info) {

        new TeleportCommands().initializeMod((MinecraftServer) (Object) this);
    }

    @Inject(method = "tickServer", at = @At(value = "TAIL"))
    private void tickServer(BooleanSupplier hasTimeLeft, CallbackInfo info) {
        TeleportCommands.INSTANCE.teleporter.tick((MinecraftServer) (Object) this);

        // todo! do the same for TPA
    }
}