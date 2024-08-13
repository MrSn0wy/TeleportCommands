# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

### [v1.1.2]
- Added Italian Translations (Thanks to [Vlad Andrei Morariu](https://github.com/VladAndreiMorariu)


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
- Added Hungarian translations (Thanks to [Martin Morningstar](https://github.com/RMI637))

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
- Changed build files to support multiple mod loader


### [v1.0.2]

#### Added
- Added project icon
- Added project description

#### Changed
- Changed the array for tpa to only save the player's uuid (not the whole entity lmao)
