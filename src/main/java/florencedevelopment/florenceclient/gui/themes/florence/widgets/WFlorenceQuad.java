/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.themes.florence.widgets;

import florencedevelopment.florenceclient.gui.renderer.GuiRenderer;
import florencedevelopment.florenceclient.gui.widgets.WQuad;
import florencedevelopment.florenceclient.utils.render.color.Color;

public class WFlorenceQuad extends WQuad {
    public WFlorenceQuad(Color color) {
        super(color);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        renderer.quad(x, y, width, height, color);
    }
}
