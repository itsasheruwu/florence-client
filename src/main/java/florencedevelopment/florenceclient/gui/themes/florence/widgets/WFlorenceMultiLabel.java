/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.themes.florence.widgets;

import florencedevelopment.florenceclient.gui.renderer.GuiRenderer;
import florencedevelopment.florenceclient.gui.themes.florence.FlorenceWidget;
import florencedevelopment.florenceclient.gui.widgets.WMultiLabel;

public class WFlorenceMultiLabel extends WMultiLabel implements FlorenceWidget {
    public WFlorenceMultiLabel(String text, boolean title, double maxWidth) {
        super(text, title, maxWidth);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        double h = theme.textHeight(title);
        int defaultColor = design().text;

        for (int i = 0; i < lines.size(); i++) {
            if (color != null) renderer.text(lines.get(i), x, y + h * i, color, false);
            else renderer.text(lines.get(i), x, y + h * i, defaultColor, false);
        }
    }
}
