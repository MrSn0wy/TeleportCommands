package dev.mrsnowy.teleport_commands.mixin;

import dev.mrsnowy.teleport_commands.TeleportCommands;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class PlayerDeathMixin {

    @Inject(method = "die", at = @At("HEAD"))
    private void notifyDeath(CallbackInfo ci) {

        TeleportCommands.onPlayerDeath((ServerPlayer) (Object) this);
    }
}