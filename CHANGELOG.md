# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

### [1.3.2]
- Added Traditional Chinese (Hong Kong) translations. (Thanks to [Dicecan](https://github.com/Dicecan)
- Updated Traditional Chinese (Taiwan) and Simplified Chinese translations. (Thanks to [Dicecan](https://github.com/Dicecan))
- Fixed a bug which caused `/back` locations to persist when in single player and joining a different world.

### [1.3.1]
- Updated Russian translations (Thanks to [rfin0](https://github.com/rfin0)!)
- Changed the text color to red when running `/delhome` and the home isn't found. This is now consistent with `/delwarp`

### [1.3.0]
#### Changed
- Storage is loaded in memory instead of reading it again and again, Improves speed and IO usage, however there might be slightly higher passive memory usage.
- Added a new merged jar file that combines the jars of the mod-loaders into one. (Thanks to [forgix](https://github.com/PacifistMC/Forgix)!) (This will also from now on be published on Modrinth)
- Made it so the DeathLocations (for `/back`) are only kept in memory. (before they would get saved and deleted on startup lol)
- Improved the Storage classes and it's functions (I'm doing proper java, yipie)
- Improved the error messages for command suggestions.
- Improved the messages styling for when no safe location is found while running `/back` or `/worldspawn`  
- Improved the sending of the homes/warps when doing `/homes` or `/warps` (They now get sent in one message instead of multiple)
- Improved the English and Dutch translations.
- Reduced the size of the mod icon by 60% (a 40kb reduction) using some `zopflipng` black magic. (sadly this only made the mod jar 5kb smaller :<)

#### Fixed
- Fixed the teleport/delete/rename buttons to work with names that have special characters by putting it in quotations (Fixes issue #12)
- Fixed the markdown file for the translations from being bundled with the mod jar.
- Fixed the usage of an invalid translation string when renaming a Home or Warp to a name that already exists.
- Fixed the error handling for when a world isn't found (when going back, going home or warping).
  - It now throws a warning and gives the player a new error message (before it would give an incorrect `notFound` error)
- Fixed Traditional Chinese - Taiwan (Thanks to [Dicecan](https://github.com/Dicecan)!)

#### Added
- Added a version variable in the mod that gets printed on startup
- Added comments to a lot of code
- Added a new `common.hoverCopy` translation key for when hovering over a location/dimension text in `/warps` or `/homes`.
- Added a new `home.defaultNone` translation key for when there is no default house set. (before this would give `home.homeless` for some reason)
- Added a new `common.worldNotFound` translation key for when a world cannot be found
- Added a new `common.defaultPrompt` translation key for a new "Set Default" button for `/homes`
- Added a new `common.nameExists` translation key for when that name already exists
- Added Simplified Chinese (Thanks to [Dicecan](https://github.com/Dicecan)!)
- Added Bulgarian translations (Thanks to a friend of mine!)

### [v1.2.2]
- Handled a case where the client (geyser) will return the language as uppercase instead of lowercase.
- Fixed null-pointer exceptions being logged when a language file couldn't be found.

### [v1.2.1]
- Added support for 1.21.4
- Added Traditional Chinese translations (Thanks to [hugoalh](https://github.com/hugoalh)!)

### [v1.2.0]
- Added the following warp related commands: `/warp` `/setwarp` `/delwarp` `/renamewarp` and `/warps`
- Added `/worldspawn`
- Improved error handling and error messages a lot, this will make bug reporting (and fixing) a lot easier since it says on what command it fails, and it also gives a stack trace.
- Fixed some small things which may cause errors.
- Improved code of some commands to make them more sane and readable.
- Modified storage code to make it more sane.
- Fixed bug with /home when in a diff dimension. (apparently I did this for /back already but forgot to add it to /home)
- Added Russian Translations (Thanks to [rfin0](https://github.com/rfin0)!)

### [v1.1.3]
- Added support for 1.21.2 - 1.21.3

### [v1.1.2]
- Added Italian Translations (Thanks to [Vlad Andrei Morariu](https://github.com/VladAndreiMorariu)!)

### [v1.1.1]
- Added support for 1.21
- Changed Java version to `21`

### [v1.1.0]

#### Added
- Added a completely server-side translation system (UNLIKE MOJANG'S SYSTEM WHICH IS CLIENT SIDE)
- Added a Json Storage cleaner, which automatically cleans and updates any values
- Added a safety check with `/back` and `/tpa[here]` that automatically chooses a nearby safe location
- Added quilt support
- Added a CHANGELOG.md
- Added Tpa Accept/Deny Suggestions
- Added Dutch translations
- Added Hungarian translations (Thanks to [Martin Morningstar](https://github.com/RMI637)!)

#### Changed
- Limited the requests a player can do to the same player to 1
- Improved command messages and colors
- Fixed Tpa Accept/Deny messages going to the wrong person
- Fixed /back saying "Already back" when on the same death location in another dimension
- Fixed /back giving an error when the player didn't have a deathLocation, instead of the appropriate message
- Improved performance by changing the death event to be player specific (not all entities)
- Replaced all loader specific api events with Mixins
- Edited /back to have a DisableSafety option: `/back [<Disable Safety>]`
- Improved /back and /home `Already there` detection

#### Removed
- Removed Sources and Javadoc files to improve build speed
- Removed Fabric API dependency
- Removed pretty json printing (to save storage)

#### Breaking changes (non-backwards compatible)
- Replaced `Player_UUID` in the storage json to `UUID`
- Changed Death location coords in the storage json from `double` to `int`
- Changed Home coords in the storage json from `double` to `int`

### [v1.0.5]

#### Added
- Added support for NeoForge

#### Changed
- Changed mappings to Mojang.
- Cleaned up commands code
- Changed build files to support multiple mod loaders


### [v1.0.2]

#### Added
- Added project icon
- Added project description

#### Changed
- Changed the array for tpa to only save the player's uuid (not the whole entity lmao)
