# Changelog

All notable changes to this project will be documented in this file.

## [v1.21.11-1] - 2026-03-06

Initial public release of Florence Client for Minecraft 1.21.11.

### Added

- Core Fabric client bootstrap, launcher entrypoint, config loading, addon hooks, and ASM-based patching infrastructure.
- Large module set for utility and gameplay automation, with roughly 200 module classes included in this initial release.
- Command system with 38 built-in command implementations and custom argument handling.
- GUI framework with themed screens, tabs, widgets, custom renderer code, packaged fonts, and shader resources.
- HUD system with 28 HUD element classes for in-game overlays and status displays.
- Persistent account, config, friend, macro, profile, proxy, and waypoint systems.
- Rendering, text, network, entity, schematic, player, world, and file utility layers used across the client.
- Compatibility mixins and compile-time integrations for Baritone, Sodium, Lithium, Iris, ViaFabricPlus, and Mod Menu.
- Bundled runtime libraries including Orbit, Starscript, Discord IPC, Reflections, Netty proxy support, and WaybackAuthLib.
- GitHub automation for builds, pull requests, issue moderation, and repository templates.

### Notes

- Release artifact version `1.21.11-1` is built from commit `5d03cfc`.
- This is the first tagged release in the `florence-client` repository.
