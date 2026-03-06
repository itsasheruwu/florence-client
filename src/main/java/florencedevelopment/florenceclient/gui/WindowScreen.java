/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui;

import florencedevelopment.florenceclient.gui.utils.Cell;
import florencedevelopment.florenceclient.gui.widgets.WWidget;
import florencedevelopment.florenceclient.gui.widgets.containers.WWindow;

public abstract class WindowScreen extends WidgetScreen {
    protected final WWindow window;

    public WindowScreen(GuiTheme theme, WWidget icon, String title) {
        super(theme, title);

        window = super.add(theme.window(icon, title)).center().widget();
        window.view.scrollOnlyWhenMouseOver = false;
    }

    public WindowScreen(GuiTheme theme, String title) {
        this(theme, null, title);
    }

    @Override
    public <W extends WWidget> Cell<W> add(W widget) {
        return window.add(widget);
    }

    @Override
    public void clear() {
        window.clear();
    }
}
