/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.themes.florence.widgets;

import florencedevelopment.florenceclient.gui.renderer.GuiRenderer;
import florencedevelopment.florenceclient.gui.themes.florence.FlorenceWidget;
import florencedevelopment.florenceclient.gui.widgets.WLabel;

public class WFlorenceLabel extends WLabel implements FlorenceWidget {
    public WFlorenceLabel(String text, boolean title) {
        super(text, title);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (text.isEmpty()) return;

        if (color != null) renderer.text(text, x, y, color, title);
        else renderer.text(text, x, y, design().text, title);
    }
}
