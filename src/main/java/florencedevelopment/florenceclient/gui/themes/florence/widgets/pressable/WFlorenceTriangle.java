/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.themes.florence.widgets.pressable;

import florencedevelopment.florenceclient.gui.renderer.GuiRenderer;
import florencedevelopment.florenceclient.gui.themes.florence.FlorenceWidget;
import florencedevelopment.florenceclient.gui.widgets.pressable.WTriangle;

public class WFlorenceTriangle extends WTriangle implements FlorenceWidget {
    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        renderer.rotatedQuad(x, y, width, height, rotation, GuiRenderer.TRIANGLE, theme().backgroundColor.get(pressed, mouseOver));
    }
}
