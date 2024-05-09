# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

### [Unreleased]

#### Added
- Added completely server-side translation system (UNLIKE MOJANG'S SYSTEM WHICH IS CLIENT SIDE)
- Added quilt support
- Added CHANGELOG.md

#### Changed
- Improved command messages and colors
- improved performance by changing the death event to be player specific (not all entities)
- Replaced all loader specific events with Mixins

#### Removed
- Removed Sources and Javadoc files to improve build speed
- Removed Fabric API dependency


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