package dev.mrsnowy.teleport_commands.mixin;

import dev.mrsnowy.teleport_commands.TeleportCommands;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
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

    // This function is not on the ServerPlayer, but on the entity which the ServerPlayer is based on :P
    @Inject(method = "move", at = @At("TAIL"))
    private void onMove(MoverType type, Vec3 delta, CallbackInfo info) {
        if (type != MoverType.PLAYER && type != MoverType.SELF) return;
        if (delta.lengthSqr() == 0) return;

        ServerPlayer self = (ServerPlayer) (Object) this;
        TeleportCommands.INSTANCE.teleporter.reportPlayerMoved(self);
    }

    @Inject(method = "hurtServer", at = @At("TAIL"))
    private void onHurt(ServerLevel leve, DamageSource source, float amount, CallbackInfoReturnable<Boolean> info) {
        // It returns true if the player got meaningfully damaged / they weren't immune
        if (info.getReturnValue()) {
            ServerPlayer self = (ServerPlayer) (Object) this;
            TeleportCommands.INSTANCE.teleporter.reportPlayerHurt(self);
        }
    }
}