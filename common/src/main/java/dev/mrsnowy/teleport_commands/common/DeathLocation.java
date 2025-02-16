package dev.mrsnowy.teleport_commands.common;

import dev.mrsnowy.teleport_commands.TeleportCommands;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.StreamSupport;

public class DeathLocation {
    private final String UUID;
    private BlockPos pos;
    private String world;

    public DeathLocation(String uuid, BlockPos pos, String world) {
        this.UUID = uuid;
        this.pos = pos;
        this.world = world;
    }

    // -----

    public String getUUID() {
        return UUID;
    }

    public BlockPos getBlockPos() {
        return pos;
    }

    // maybe add getX getY and getZ? todo!

    public String getWorldString() {
        return world;
    }

    // function to quickly filter the worlds and get the ServerLevel for the string
    public Optional<ServerLevel> getWorld() {
        return StreamSupport.stream( TeleportCommands.SERVER.getAllLevels().spliterator(), false ) // woa, this looks silly
                .filter(level -> Objects.equals( level.dimension().location().toString(), this.world ))
                .findFirst();
    }

    // ----- note to self: these don't need to be saved since this class isn't a part of the storage :3

    public void setBlockPos(BlockPos pos) {
        this.pos = pos;
    }

    public void setWorld(String world) {
        this.world = world;
    }
}
