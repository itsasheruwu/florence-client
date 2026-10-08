/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.themes.florence.widgets;

import florencedevelopment.florenceclient.gui.design.Colors;
import florencedevelopment.florenceclient.gui.renderer.GuiRenderer;
import florencedevelopment.florenceclient.gui.themes.florence.FlorenceGuiTheme;
import florencedevelopment.florenceclient.gui.widgets.WQuad;
import florencedevelopment.florenceclient.utils.render.color.Color;

public class WFlorenceQuad extends WQuad {
    public WFlorenceQuad(Color color) {
        super(color);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        int argb = (color.a << 24) | (color.r << 16) | (color.g << 8) | color.b;

        if (theme instanceof FlorenceGuiTheme florence) {
            renderer.roundRect(x, y, width, height, florence.radiusSmall(), argb, Math.max(1, Math.round(florence.scale(1))), florence.design().outlineHover);
        }
        else {
            renderer.roundRect(x, y, width, height, 0, argb);
        }
    }
}
