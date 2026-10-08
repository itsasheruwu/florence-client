/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.events.gui;

import florencedevelopment.florenceclient.gui.GuiTheme;

/**
 * Posted after another GUI theme has been selected.
 */
public class GuiThemeChangedEvent {
    private static final GuiThemeChangedEvent INSTANCE = new GuiThemeChangedEvent();

    public GuiTheme theme;

    public static GuiThemeChangedEvent get(GuiTheme theme) {
        INSTANCE.theme = theme;
        return INSTANCE;
    }
}
