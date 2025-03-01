# Teleport Commands <img src="https://raw.githubusercontent.com/MrSn0wy/TeleportCommands/main/common/src/main/resources/teleport_commands.png" alt="Teleport Commands Logo" width="30"/>

A Minecraft server-side mod that adds various teleportation related commands, like /home /tpa and /back

Here is the [Changelog](https://github.com/MrSn0wy/TeleportCommands/blob/main/CHANGELOG.md)

#### Currently available commands:

- `/worldspawn [<Disable Safety>]` - Teleports you to the world spawn (in the overworld), if given true it will not do safety checks
- `/back [<Disable Safety>]` -  Teleports you to the location where you last died, if given true it will not do safety checks
<br><br>
    **Homes are player specific locations that only that player can teleport to**
- `/sethome <name>` - Creates a new home
- `/home [<name>]` - Teleports you to the home, if no name is giving it will go to the default home
- `/delhome <name>` - Deletes a home
- `/renamehome <name> <newName>` - Renames a home
- `/homes` - Shows a list of your homes
- `/defaulthome <name>` - Sets the default home
<br><br>
    **Warps are op managed locations that all players can teleport to**
- `/warp <name>` - Teleports you to the warp
- `/warps` - Shows a list of the available warp
- `/setwarp <name>` - Sets a warp. Permission level of 4 required (op)
- `/delwarp <name>` - Deletes a warp. Permission level of 4 required (op)
- `/renamewarp <name> <newName>` - Renames a warp. Permission level of 4 required (op)
<br><br>
    **With tpa you can teleport to other players or make them teleport to you**
- `/tpa <player>` - Sends a tpa request to another player
- `/tpahere <player>` - Sends a tpaHere request to another player
- `/tpaaccept <player>` -  Accepts the tpa/tpahere request of that player
- `/tpadeny <player>` - Denies the tpa/tpaHere request of that player

<br>

### TODO:

#### Planned commands:
- [ ] `/wild` - Teleports you to a random location in the Overworld
- [x] `/worldspawn` - Teleports you to the worldspawn
- [ ] `/spawn <dimension>` - Teleports you to your spawnpoint in a dimension, defaults to your current dimension
- [ ] `/previous` - Go to the last teleported location

#### Improvements:
- [ ] Look into changing the mod into the more safe and sane kotlin (I love java)
- [ ] Find a way to combine the mod loader specific jars into one
- [ ] Find the easiest way to backport the mod to older version (help)
- [ ] Create a config to add any delays and disable commands, with commands for operators in game
- [ ] Add a perm system
- [x] Json Storage automatic updater & cleaner
- [x] Modify /back to check if the location is safe and automatically choose a nearby location that is safe
- [x] Limit tpa requests for a player
- [x] Add translation system
- [x] Improve responses for commands
- [x] Add Quilt support and NeoForge


### Want to help?

1. You can create a translation file so other people can use the mod in their native language: [translations.md](./common/src/main/resources/assets/teleport_commands/lang/translations.md)

#### Notes

Colors: 
- Green = When something succeeds and an action will happen
- Aqua = When something needs attention
- White = When something is done
- Red = When something fails