# Changelog

All notable changes to this project will be documented in this file.

## [v1.21.11-4] - 2026-10-08

Click GUI redesign update: a new renderer and theme, rebuilt widgets, a Legit tab, a command palette and notifications.

### Added

- Rounded shapes, borders, soft shadows and frosted glass in the click GUI, drawn with a new signed distance field shader so edges stay smooth at any scale.
- Glass windows that show the blurred world behind them. The blur is now shared with the `Blur` module instead of being tied to it.
- New Legit tab next to Modules: a Lunar Client style menu with a category sidebar and a grid of cards, each with a custom icon, an Options button and an Enabled / Disabled bar. Options open as a page in the same window. The modules in it are not in the Modules tab, and the Edit button changes which ones they are.
- Inline color picker with a saturation and brightness square, hue and opacity strips and a rainbow switch, replacing the separate color window.
- Right-clicking a HUD element in the editor opens a floating panel with its settings, and right-clicking empty space opens a small menu (a reusable `ContextMenu`).
- New `Window Width` GUI setting for the module windows.
- Command palette opened with `Ctrl + K` (or the Search button in the top bar) to find modules, settings and tabs with fuzzy matching, highlighted letters and full keyboard control.
- Notifications that slide in at the top right when a module is turned on or off or bound to a key from the GUI. Other code can show one with `NotificationManager.push`.
- Animated on/off switches, a sliding tab highlight, a count badge on every category window and a marker on modules whose settings have been changed.
- New GUI settings: preset (Florence Dark, Midnight, Light, High Contrast, Enderstorm), accent colors, density, working corner radius, glass, blur strength, panel opacity, shadows, animation speed and reduced motion.
- New events for the GUI: `GuiScreenEvent` (opened, closing, closed), `GuiThemeChangedEvent`, `SettingChangedEvent`, `ModuleToggledEvent`, `ModuleFavoriteChangedEvent` and `NotificationEvent`.
- Animation engine for the GUI with easing curves, frame rate independent springs and motion settings that apply everywhere.
- Unit tests (JUnit 5) for the animation engine, colors, design tokens, fuzzy search and theme migration.

### Changed

- Rebuilt every widget of the Florence theme: modules, windows, buttons, switches, sliders, dropdowns, text boxes, tooltips, sections, scroll bars and separators.
- Module settings are only built the first time a module is opened, and they fit inside the width of the window.
- Module search uses fuzzy matching and only lists real matches instead of always filling the list.
- Replaced Meteor with Florence in the GUI-facing texts of the config, HUD and Blur module (the Blur `meteor` option is now `florence`).
- The theme is now called `Florence`. Settings saved under the old `Meteor` name are still picked up.
- The GUI animates in real time, so the Timer module no longer speeds it up or slows it down.
- The `Blur` module now asks the shared blur for the world behind the GUI.

### Fixed

- Click GUI windows were twice as wide as intended when the GUI scale was not 1.
- Clicking a module window no longer moves it to the end of the layout.
- Category icons no longer show through the windows in front of them.
- Windows that overlap no longer show the text of the windows below them through their own.
- The `Render2DEvent` screen height was the screen width.

### Notes

- Release artifact version is `1.21.11-4`.
- Release artifact name is `florence-client-1.21.11-4.jar`.
- Saved GUI theme settings under the old `Meteor` name are still read, the Blur `meteor` option was renamed to `florence` and resets once.
- Developers can set `FLORENCE_DEV_OPEN_GUI` (and optionally `world`) in a development environment to open the click GUI by itself, see `DevAutoOpen`.

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
