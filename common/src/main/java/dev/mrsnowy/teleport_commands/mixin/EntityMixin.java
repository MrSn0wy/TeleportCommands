package dev.mrsnowy.teleport_commands.mixin;

import dev.mrsnowy.teleport_commands.TeleportCommands;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {

    @Inject(method = "move", at = @At("TAIL"))
    private void onMove(MoverType type, Vec3 movement, CallbackInfo info) {
        if (type != MoverType.PLAYER && type != MoverType.SELF) return;
        if (movement.lengthSqr() == 0) return;

        Entity self = (Entity) (Object) this;
        if (!(self instanceof ServerPlayer serverPlayer)) return;

        TeleportCommands.INSTANCE.teleporter.reportPlayerMoved(serverPlayer);
    }
}
