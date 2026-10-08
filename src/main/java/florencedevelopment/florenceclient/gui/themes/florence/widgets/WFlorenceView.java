/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.themes.florence.widgets;

import florencedevelopment.florenceclient.gui.animation.AnimatedFloat;
import florencedevelopment.florenceclient.gui.design.Colors;
import florencedevelopment.florenceclient.gui.design.Design;
import florencedevelopment.florenceclient.gui.renderer.GuiRenderer;
import florencedevelopment.florenceclient.gui.themes.florence.FlorenceWidget;
import florencedevelopment.florenceclient.gui.widgets.containers.WView;

public class WFlorenceView extends WView implements FlorenceWidget {
    // The scrollbar is thin until the mouse comes near it
    private final AnimatedFloat emphasis = new AnimatedFloat(0);

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (!canScroll || !hasScrollBar) return;

        Design d = design();

        emphasis.animateTo(handlePressed || handleMouseOver ? 1 : 0, 0.15);
        double e = emphasis.update(delta);

        double w = handleWidth() * (0.45 + 0.4 * e);
        double hx = handleX() + handleWidth() - w - theme.scale(1);

        int color = Colors.lerp(Colors.withAlpha(d.textSecondary, 90), Colors.withAlpha(d.textSecondary, 200), e);
        if (handlePressed) color = d.accent;

        renderer.roundRect(hx, handleY(), w, handleHeight(), w / 2, color);
    }
}
