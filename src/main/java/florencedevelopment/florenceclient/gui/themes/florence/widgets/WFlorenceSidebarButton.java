/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.themes.florence.widgets;

import florencedevelopment.florenceclient.gui.animation.AnimatedFloat;
import florencedevelopment.florenceclient.gui.animation.Interaction;
import florencedevelopment.florenceclient.gui.animation.Spring;
import florencedevelopment.florenceclient.gui.design.Colors;
import florencedevelopment.florenceclient.gui.design.Design;
import florencedevelopment.florenceclient.gui.renderer.GuiRenderer;
import florencedevelopment.florenceclient.gui.themes.florence.FlorenceGuiTheme;
import florencedevelopment.florenceclient.gui.themes.florence.FlorenceWidget;
import florencedevelopment.florenceclient.gui.widgets.pressable.WPressable;

import java.util.function.BooleanSupplier;

/**
 * An entry of a list down the left side of a screen, with a colored dot, a name and a count.
 */
public class WFlorenceSidebarButton extends WPressable implements FlorenceWidget {
    private final String label;
    private final int count;
    private final int color;
    private final BooleanSupplier selected;

    private final Interaction ui = new Interaction();
    private final AnimatedFloat select = new AnimatedFloat(0);

    public WFlorenceSidebarButton(String label, int count, int color, BooleanSupplier selected, Runnable action) {
        this.label = label;
        this.count = count;
        this.color = color;
        this.selected = selected;
        this.action = action;

        select.snap(selected.getAsBoolean() ? 1 : 0);
    }

    @Override
    protected void onCalculateSize() {
        FlorenceGuiTheme theme = theme();

        width = theme.scale(150);
        height = theme.textHeight() + theme.space(14);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        FlorenceGuiTheme theme = theme();
        Design d = design();

        ui.update(mouseOver, pressed, delta);

        select.springTo(selected.getAsBoolean() ? 1 : 0, Spring.CRITICAL);
        double s = Math.max(0, Math.min(1, select.update(delta)));

        double th = theme.textHeight();
        double line = lineWidth();

        int fill = Colors.lerp(Colors.mulAlpha(d.cardHover, ui.hover()), d.cardActive, s);
        renderer.roundRect(x, y, width, height, theme.radiusMedium(), fill, line, Colors.lerp(0, Colors.withAlpha(color, 120), s));

        // Bar on the left edge of the chosen entry
        if (s > 0.01) {
            double bar = Math.max(3, theme.scale(3));
            renderer.roundRect(x + theme.scale(2), y + height / 2 - height * 0.28 * s, bar, height * 0.56 * s, bar / 2, color);
        }

        double dot = Math.max(7, theme.scale(8));
        renderer.circle(x + theme.space(16), y + height / 2, dot / 2, Colors.lerp(Colors.withAlpha(color, 150), color, s));

        renderer.text(label, x + theme.space(30), y + (height - th) / 2, Colors.lerp(d.textSecondary, d.text, Math.max(s, ui.hover())), false);

        String number = String.valueOf(count);
        renderer.text(number, x + width - theme.space(12) - theme.textWidth(number), y + (height - th) / 2, d.textDisabled, false);
    }
}
