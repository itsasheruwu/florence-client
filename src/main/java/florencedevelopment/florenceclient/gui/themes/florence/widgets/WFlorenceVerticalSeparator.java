/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.themes.florence.widgets;

import florencedevelopment.florenceclient.gui.renderer.GuiRenderer;
import florencedevelopment.florenceclient.gui.themes.florence.FlorenceWidget;
import florencedevelopment.florenceclient.gui.widgets.WVerticalSeparator;

public class WFlorenceVerticalSeparator extends WVerticalSeparator implements FlorenceWidget {
    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        double thickness = lineWidth();

        renderer.roundRect(x + Math.round(width / 2.0) - thickness / 2, y, thickness, height, thickness / 2, design().divider);
    }
}
