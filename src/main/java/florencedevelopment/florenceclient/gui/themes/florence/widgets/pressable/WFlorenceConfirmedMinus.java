/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.themes.florence.widgets.pressable;

import florencedevelopment.florenceclient.gui.animation.Interaction;
import florencedevelopment.florenceclient.gui.design.Colors;
import florencedevelopment.florenceclient.gui.design.Design;
import florencedevelopment.florenceclient.gui.renderer.GuiRenderer;
import florencedevelopment.florenceclient.gui.themes.florence.FlorenceGuiTheme;
import florencedevelopment.florenceclient.gui.themes.florence.FlorenceWidget;
import florencedevelopment.florenceclient.gui.widgets.pressable.WConfirmedMinus;

public class WFlorenceConfirmedMinus extends WConfirmedMinus implements FlorenceWidget {
    private final Interaction ui = new Interaction();

    @Override
    protected void onCalculateSize() {
        width = theme.textHeight() + theme().space(8);
        height = width;
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        FlorenceGuiTheme theme = theme();
        Design d = design();

        ui.update(mouseOver, pressed, delta);

        int color;

        if (pressedOnce) {
            // Waiting for the second click
            renderer.roundRect(x, y, width, height, theme.radiusMedium(), Colors.lerp(Colors.withAlpha(d.danger, 210), d.danger, ui.hover()), lineWidth(), d.danger);
            color = Colors.rgb(255, 255, 255);
        }
        else {
            renderControl(renderer, this, ui);
            color = d.danger;
        }

        double arm = width * 0.2;
        double cx = x + width / 2;
        double cy = y + height / 2;

        renderer.line(cx - arm, cy, cx + arm, cy, theme.scale(2), color);
    }
}
