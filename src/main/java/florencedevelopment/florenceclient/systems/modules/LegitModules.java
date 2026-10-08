/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.systems.modules;

import java.util.Set;

/**
 * The modules that are shown in the Legit tab of the click GUI instead of the Modules tab: quality of life and visual
 * modules that don't give any advantage. The user can change what is in the tab.
 */
public final class LegitModules {
    private static final Set<String> DEFAULTS = Set.of(
        "zoom", "fullbright", "better-tab", "better-tooltips", "better-chat", "blur", "boss-stack", "break-indicators",
        "camera-tweaks", "free-look", "hand-view", "item-physics", "light-overlay", "no-render", "time-changer",
        "waypoints", "trajectories", "item-highlight", "name-protect", "ambience", "auto-reconnect", "sound-blocker",
        "block-selection", "inventory-tweaks", "notifier", "breadcrumbs"
    );

    private LegitModules() {}

    public static boolean isDefault(String moduleName) {
        return DEFAULTS.contains(moduleName);
    }
}
