package dev.mrsnowy.teleport_commands.mixin;

import com.mojang.brigadier.CommandDispatcher;
import dev.mrsnowy.teleport_commands.TeleportCommands;
import dev.mrsnowy.teleport_commands.commands.*;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Commands.class)
public class CommandsMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void RegisterTeleportCommands(Commands.CommandSelection selection, CommandBuildContext context, CallbackInfo info) {

        Commands self = (Commands) (Object) this;
        CommandDispatcher<CommandSourceStack> dispatcher = self.getDispatcher();

        TeleportCommands.registerCommands(dispatcher);
    }
}