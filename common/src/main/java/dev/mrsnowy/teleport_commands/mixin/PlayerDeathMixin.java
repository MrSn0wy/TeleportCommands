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
    private void notifyDeath(CallbackInfo info) {
        TeleportCommands.INSTANCE.onPlayerDeath((ServerPlayer) (Object) this);
    }

    @Inject(method = "onEnterCombat", at = @At("TAIL"))
    private void combatEntered(CallbackInfo info) {
//        TeleportCommands.INSTANCE.onPlayerDeath((ServerPlayer) (Object) this);
    }

    @Inject(method = "onLeaveCombat", at = @At("TAIL"))
    private void combatLeft(CallbackInfo info) {
//        TeleportCommands.INSTANCE.onPlayerDeath((ServerPlayer) (Object) this);
    }
}