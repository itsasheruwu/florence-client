/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.themes.florence.widgets.pressable;

import florencedevelopment.florenceclient.gui.animation.Interaction;
import florencedevelopment.florenceclient.gui.renderer.GuiRenderer;
import florencedevelopment.florenceclient.gui.themes.florence.FlorenceGuiTheme;
import florencedevelopment.florenceclient.gui.themes.florence.FlorenceWidget;
import florencedevelopment.florenceclient.gui.widgets.pressable.WPlus;

public class WFlorencePlus extends WPlus implements FlorenceWidget {
    private final Interaction ui = new Interaction();

    @Override
    protected void onCalculateSize() {
        width = theme.textHeight() + theme().space(8);
        height = width;
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        FlorenceGuiTheme theme = theme();

        ui.update(mouseOver, pressed, delta);
        renderControl(renderer, this, ui);

        double arm = width * 0.2;
        double cx = x + width / 2;
        double cy = y + height / 2;
        double t = theme.scale(2);
        int color = design().success;

        renderer.line(cx - arm, cy, cx + arm, cy, t, color);
        renderer.line(cx, cy - arm, cx, cy + arm, t, color);
    }
}
