/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.themes.florence.widgets.pressable;

import florencedevelopment.florenceclient.gui.animation.Interaction;
import florencedevelopment.florenceclient.gui.design.Colors;
import florencedevelopment.florenceclient.gui.design.Design;
import florencedevelopment.florenceclient.gui.renderer.GuiRenderer;
import florencedevelopment.florenceclient.gui.themes.florence.FlorenceWidget;
import florencedevelopment.florenceclient.gui.widgets.pressable.WTriangle;

public class WFlorenceTriangle extends WTriangle implements FlorenceWidget {
    private final Interaction ui = new Interaction();

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        Design d = design();

        ui.update(mouseOver, pressed, delta);

        chevron(renderer, x + width / 2, y + height / 2, width * 0.55, rotation, theme.scale(2),
            Colors.lerp(d.textSecondary, d.text, ui.hover()));
    }
}
