/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.themes.florence.widgets;

import florencedevelopment.florenceclient.gui.renderer.GuiRenderer;
import florencedevelopment.florenceclient.gui.themes.florence.FlorenceWidget;
import florencedevelopment.florenceclient.gui.widgets.WMultiLabel;
import florencedevelopment.florenceclient.utils.render.color.Color;

public class WFlorenceMultiLabel extends WMultiLabel implements FlorenceWidget {
    public WFlorenceMultiLabel(String text, boolean title, double maxWidth) {
        super(text, title, maxWidth);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        double h = theme.textHeight(title);
        Color defaultColor = theme().textColor.get();

        for (int i = 0; i < lines.size(); i++) {
            renderer.text(lines.get(i), x, y + h * i, color != null ? color : defaultColor, false);
        }
    }
}
