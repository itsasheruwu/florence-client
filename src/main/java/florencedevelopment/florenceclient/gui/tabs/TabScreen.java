/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.tabs;

import florencedevelopment.florenceclient.gui.GuiTheme;
import florencedevelopment.florenceclient.gui.WidgetScreen;
import florencedevelopment.florenceclient.gui.utils.Cell;
import florencedevelopment.florenceclient.gui.widgets.WWidget;

public abstract class TabScreen extends WidgetScreen {
    public final Tab tab;

    public TabScreen(GuiTheme theme, Tab tab) {
        super(theme, tab.name);

        this.tab = tab;
    }

    public <T extends WWidget> Cell<T> addDirect(T widget) {
        return super.add(widget);
    }
}
