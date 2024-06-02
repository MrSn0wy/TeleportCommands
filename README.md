# Teleport Commands <img src="https://raw.githubusercontent.com/MrSn0wy/TeleportCommands/main/common/src/main/resources/teleport_commands.png" alt="Teleport Commands Logo" width="30"/>

A Minecraft server-side mod that adds various teleportation related commands, like /home /tpa and /back

Here is the [Changelog](CHANGELOG.md)

#### Currently available commands:

- `/back [<Disable Safety>]` -  Teleports you to the location where you last died, if given true it will not do safety checks
<br>

- `/sethome <name>` - Creates a new home
- `/home [<name>]` - Teleports you home, if no name is giving it will go to the default home
- `/delhome <name>` - Deletes a home
- `/renamehome <name> <newName>` - Renames a home
- `/homes` - Shows a list of your homes
- `/defaulthome <name>` - Sets the default home
<br>

- `/tpa <player>` - Sends a tpa request to another player
- `/tpahere <player>` - Sends a tpaHere request to another player
- `/tpaaccept <player>` -  Accepts the tpa/ tpahere request of that player
- `/tpadeny <player>` - Denies the tpa/tpaHere request of that player

<br>

### TODO:

#### Planned commands:
- [ ] `/wild` - Teleports you to a random location in the Overworld
- [ ] `/worldspawn` - Teleports you to the worldspawn
- [ ] `/spawn <dimension>` - Teleports you to your spawnpoint in a dimension, defaults to your current dimension

#### Improvements:
- [ ] Create a config to add any delays and disable commands, also add commands for operators in game
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