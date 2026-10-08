/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.themes.florence.widgets.pressable;

import florencedevelopment.florenceclient.gui.animation.AnimatedFloat;
import florencedevelopment.florenceclient.gui.animation.Interaction;
import florencedevelopment.florenceclient.gui.animation.Spring;
import florencedevelopment.florenceclient.gui.design.Colors;
import florencedevelopment.florenceclient.gui.design.Design;
import florencedevelopment.florenceclient.gui.renderer.GuiRenderer;
import florencedevelopment.florenceclient.gui.themes.florence.FlorenceGuiTheme;
import florencedevelopment.florenceclient.gui.themes.florence.FlorenceWidget;
import florencedevelopment.florenceclient.gui.widgets.pressable.WCheckbox;

/**
 * An on/off switch.
 */
public class WFlorenceSwitch extends WCheckbox implements FlorenceWidget {
    private final Interaction ui = new Interaction();
    private final AnimatedFloat position;

    public WFlorenceSwitch(boolean checked) {
        super(checked);
        position = new AnimatedFloat(checked ? 1 : 0);
    }

    @Override
    protected void onCalculateSize() {
        height = theme.textHeight() + theme().space(2);
        width = height * 1.85;
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        FlorenceGuiTheme theme = theme();
        Design d = design();

        ui.update(mouseOver, pressed, delta);

        position.springTo(checked ? 1 : 0, Spring.SNAPPY);
        double p = position.update(delta);
        double clamped = Math.max(0, Math.min(1, p));

        double radius = height / 2;

        // Track
        int off = Colors.lerp(d.trackOff, Colors.lerp(d.trackOff, d.outlineHover, 0.6), ui.hover());
        int on = Colors.lerp(d.accent, d.accentHover, ui.hover());
        int track = Colors.lerp(off, on, clamped);

        if (clamped > 0.05 && theme.shadows()) {
            renderer.shadow(x, y + height * 0.15, width, height, radius, height * 0.5, Colors.mulAlpha(d.accent, 0.5 * clamped));
        }

        renderer.roundRect(x, y, width, height, radius, track);

        // Thumb, it stretches a little while pressed
        double gap = Math.max(2, height * 0.14);
        double size = height - gap * 2;
        double stretch = size * 0.18 * ui.press();
        double travel = width - gap * 2 - size - stretch;

        double thumbX = x + gap + travel * p;
        renderer.roundRect(thumbX, y + gap, size + stretch, size, size / 2, d.thumb);
    }
}
