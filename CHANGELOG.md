# Changelog

All notable changes to this project will be documented in this file.

## [v1.21.11-3] - 2026-03-10

Interface and movement update focused on the Florence click GUI, combat helpers, and strafe tuning.

### Added

- New `Unbind` command for clearing module and action keybinds from chat.
- New `Mace Assist` combat module to help manage mace-specific attack behavior.
- New `Player Head Finder` world module for locating player heads more easily.
- New `temp-flight` option for Speed strafe damage boost to reuse the current Flight mode while knockback boost is active.
- New low-hop slider for Speed strafe to fine-tune jump height.
- New click GUI grid controls, resizable windows, and active module animation controls.

### Changed

- Refined the Florence theme with account-specific defaults and broader click GUI layout polish.
- Normalized active modules animation speed scaling for more consistent HUD motion.

### Fixed

- Fixed click GUI window handling issues affecting interaction and resizing.
- Fixed a null-safety issue in Jesus tick handling.

### Notes

- Release artifact version is `1.21.11-3`.
- Release artifact name is `florence-client-1.21.11-3.jar`.

## [v1.21.11-2] - 2026-03-08

Combat and movement update focused on target circling, strafe tuning, and smarter ranged KillAura behavior.

### Added

- New `Target Strafe` movement module for circling active KillAura targets while using Speed strafe, with direction control, jump/input gates, optional void checks, and orbit rendering.
- New `damage-boost` and `damage-boost-multiplier` settings for Speed strafe to amplify movement after knockback.

### Changed

- Speed strafe now cooperates with Target Strafe and properly stops its timer override when speed movement is blocked.
- Cobweb handling now allows strafe speed movement to function without NoSlow cancelling cobweb collisions outright.
- KillAura melee range checks now respect entity interaction reach and its bow logic now validates projectile trajectories before committing to ranged attacks.

### Fixed

- Fixed Target Strafe integration so the strafe mode resolves the module dynamically instead of caching a null reference during module initialization.
- Reduced false positives in Target Strafe void checks by using projected player support instead of a single center-point block lookup.
- Prevented KillAura bow retries from repeatedly forcing bad shots at invalid trajectories or Breeze targets.

### Notes

- Release artifact version is `1.21.11-2`.
- Release artifact name is `florence-client-1.21.11-2.jar`.

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

- Release artifact version `1.21.11-1` is built from the first release tag commit.
- This is the first tagged release in the `florence-client` repository.
