/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.tabs.builtin;

import florencedevelopment.florenceclient.gui.GuiTheme;
import florencedevelopment.florenceclient.gui.screens.LegitScreen;
import florencedevelopment.florenceclient.gui.tabs.Tab;
import florencedevelopment.florenceclient.gui.tabs.TabScreen;
import net.minecraft.client.gui.screen.Screen;

public class LegitTab extends Tab {
    public LegitTab() {
        super("Legit");
    }

    @Override
    public TabScreen createScreen(GuiTheme theme) {
        return new LegitScreen(theme, this);
    }

    @Override
    public boolean isScreen(Screen screen) {
        return screen instanceof LegitScreen;
    }
}
