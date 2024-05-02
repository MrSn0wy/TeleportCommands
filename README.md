# Teleport Commands <img src="https://raw.githubusercontent.com/MrSn0wy/TeleportCommands/main/common/src/main/resources/teleport_commands.png" alt="Teleport Commands Logo" width="30"/>


A Minecraft Fabric and NeoForge server-side mod that adds various teleportation related commands, like /home /tpa and /back

### This mod is still in beta, if there are any problems then let me know!

#### Currently available commands:

- `/back` -  Teleports you to the location where you last died
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
- `/wild` - Teleports you to a random location in the Overworld
- `/worldspawn` - Teleports you to the worldspawn
- `/spawn <dimension>` - Teleports you to your spawnpoint in a dimension, defaults to your current dimension

#### Improvements:
- Modify /back to check if the location is safe and automatically choose a nearby location that is safe
- Create a config to add any delays and disable commands, also add commands for operators in game
- Add translation file
- Improve responses for commands
- Add a perm system
- Limit tpa requests for a player
- Add Quilt support and maybe NeoForge
