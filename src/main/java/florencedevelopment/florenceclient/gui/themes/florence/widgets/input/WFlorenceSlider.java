/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.themes.florence.widgets.input;

import florencedevelopment.florenceclient.gui.animation.AnimatedFloat;
import florencedevelopment.florenceclient.gui.animation.Spring;
import florencedevelopment.florenceclient.gui.design.Colors;
import florencedevelopment.florenceclient.gui.design.Design;
import florencedevelopment.florenceclient.gui.renderer.GuiRenderer;
import florencedevelopment.florenceclient.gui.themes.florence.FlorenceGuiTheme;
import florencedevelopment.florenceclient.gui.themes.florence.FlorenceWidget;
import florencedevelopment.florenceclient.gui.widgets.input.WSlider;

public class WFlorenceSlider extends WSlider implements FlorenceWidget {
    // How big the thumb is: 0 when idle, 1 when hovered or dragged
    private final AnimatedFloat grow = new AnimatedFloat(0);

    public WFlorenceSlider(double value, double min, double max) {
        super(value, min, max);
    }

    @Override
    protected double handleSize() {
        return theme.textHeight() * 0.72;
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        FlorenceGuiTheme theme = theme();
        Design d = design();

        grow.springTo(dragging ? 1 : (handleMouseOver || mouseOver ? 0.6 : 0), Spring.SNAPPY);
        double g = grow.update(delta);

        double handle = handleSize();
        double valueWidth = valueWidth();

        double trackHeight = Math.max(3, theme.scale(4));
        double trackX = x + handle / 2;
        double trackWidth = width - handle;
        double centerY = y + height / 2;
        double trackY = centerY - trackHeight / 2;

        // Track and the filled part of it
        renderer.roundRect(trackX, trackY, trackWidth, trackHeight, trackHeight / 2, d.trackOff);

        double fill = Math.max(valueWidth, trackHeight);
        renderer.roundRectHorizontal(trackX, trackY, fill, trackHeight, trackHeight / 2, d.accent, Colors.lerp(d.accent, d.accent2, trackWidth > 0 ? valueWidth / trackWidth : 0), 0, 0);

        // Thumb
        double radius = handle * (0.5 + 0.15 * g);
        double cx = trackX + valueWidth;

        if (theme.shadows()) {
            renderer.shadow(cx - radius, centerY - radius + 1, radius * 2, radius * 2, radius, radius * 1.2, d.shadow);
        }

        renderer.circle(cx, centerY, radius, d.thumb);

        if (g > 0.01) {
            double ring = Math.max(1.5, theme.scale(2));
            renderer.roundRect(cx - radius - ring, centerY - radius - ring, (radius + ring) * 2, (radius + ring) * 2, radius + ring, 0, ring, Colors.mulAlpha(d.accent, Math.min(1, g)));
        }
    }
}
