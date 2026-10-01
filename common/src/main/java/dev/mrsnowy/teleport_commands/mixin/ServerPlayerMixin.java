package dev.mrsnowy.teleport_commands.mixin;

import dev.mrsnowy.teleport_commands.TeleportCommands;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {

    @Inject(method = "die", at = @At("HEAD"))
    private void notifyDeath(CallbackInfo info) {
        TeleportCommands.INSTANCE.onPlayerDeath((ServerPlayer) (Object) this);
    }

//    @Inject(method = "onEnterCombat", at = @At("TAIL"))
//    private void combatEntered(CallbackInfo info) {
////        TeleportCommands.INSTANCE.onPlayerDeath((ServerPlayer) (Object) this);
//    }
//
//    @Inject(method = "onLeaveCombat", at = @At("TAIL"))
//    private void combatLeft(CallbackInfo info) {
////        TeleportCommands.INSTANCE.onPlayerDeath((ServerPlayer) (Object) this);
//    }


    @Inject(method = "hurtServer", at = @At("TAIL"))
    private void onHurt(ServerLevel level, DamageSource source, float amount, CallbackInfoReturnable<Boolean> info) {
        // It returns true if the player got meaningfully damaged / they weren't immune
        if (info.getReturnValue()) {
            ServerPlayer self = (ServerPlayer) (Object) this;
            TeleportCommands.INSTANCE.teleporter.reportPlayerHurt(self);
        }
    }
}