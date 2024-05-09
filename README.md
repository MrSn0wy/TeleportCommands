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
-[ ] `/wild` - Teleports you to a random location in the Overworld
-[ ] `/worldspawn` - Teleports you to the worldspawn
-[ ] `/spawn <dimension>` - Teleports you to your spawnpoint in a dimension, defaults to your current dimension

#### Improvements:
-[ ] Modify /back to check if the location is safe and automatically choose a nearby location that is safe
-[ ] Create a config to add any delays and disable commands, also add commands for operators in game
-[ ] Add a perm system
-[ ] Limit tpa requests for a player
-[x] Add translation system
-[x] Improve responses for commands
-[x] Add Quilt support and NeoForge


### Want to help?

#### You can create a Translation file so other people can use the mod in their native language
1. Clone the repository
2. Go to `common/src/main/resources/assets/teleport_commands/lang/`
3. Go to [here](https://minecraft.wiki/w/Language) and pick the in-game locale code for the language you want to translate
4. Copy `en_us.toml` and paste it in a new file called `[in-game locale code here].toml`
5. Translate the file
6. Submit a pull request with your translation :D!