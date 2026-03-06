/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.themes.florence.widgets.pressable;

import florencedevelopment.florenceclient.gui.renderer.GuiRenderer;
import florencedevelopment.florenceclient.gui.themes.florence.FlorenceGuiTheme;
import florencedevelopment.florenceclient.gui.themes.florence.FlorenceWidget;
import florencedevelopment.florenceclient.gui.widgets.pressable.WPlus;

public class WFlorencePlus extends WPlus implements FlorenceWidget {
    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        FlorenceGuiTheme theme = theme();
        double pad = pad();
        double s = theme.scale(3);

        renderBackground(renderer, this, pressed, mouseOver);
        renderer.quad(x + pad, y + height / 2 - s / 2, width - pad * 2, s, theme.plusColor.get());
        renderer.quad(x + width / 2 - s / 2, y + pad, s, height - pad * 2, theme.plusColor.get());
    }
}
