/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.themes.florence.widgets;

import florencedevelopment.florenceclient.gui.themes.florence.FlorenceWidget;
import florencedevelopment.florenceclient.gui.widgets.WTopBar;
import florencedevelopment.florenceclient.utils.render.color.Color;

public class WFlorenceTopBar extends WTopBar implements FlorenceWidget {
    @Override
    protected Color getButtonColor(boolean pressed, boolean hovered) {
        return theme().backgroundColor.get(pressed, hovered);
    }

    @Override
    protected Color getNameColor() {
        return theme().textColor.get();
    }
}
