/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.themes.florence.widgets.input;

import florencedevelopment.florenceclient.gui.animation.Interaction;
import florencedevelopment.florenceclient.gui.design.Colors;
import florencedevelopment.florenceclient.gui.design.Design;
import florencedevelopment.florenceclient.gui.renderer.GuiRenderer;
import florencedevelopment.florenceclient.gui.themes.florence.FlorenceGuiTheme;
import florencedevelopment.florenceclient.gui.themes.florence.FlorenceWidget;
import florencedevelopment.florenceclient.gui.widgets.input.WDropdown;

public class WFlorenceDropdown<T> extends WDropdown<T> implements FlorenceWidget {
    private final Interaction ui = new Interaction();

    public WFlorenceDropdown(T[] values, T value) {
        super(values, value);
    }

    @Override
    protected WDropdownRoot createRootWidget() {
        return new WRoot();
    }

    @Override
    protected WDropdownValue createValueWidget() {
        return new WValue();
    }

    @Override
    protected void onCalculateSize() {
        FlorenceGuiTheme theme = theme();

        maxValueWidth = 0;
        for (T value : values) {
            maxValueWidth = Math.max(maxValueWidth, theme.textWidth(value.toString()));
        }

        root.calculateSize();

        double padX = theme.space(10);
        double padY = theme.space(6);

        width = padX + maxValueWidth + theme.space(10) + theme.textHeight() * 0.6 + padX;
        height = padY + theme.textHeight() + padY;

        root.width = width;
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        FlorenceGuiTheme theme = theme();
        Design d = design();

        ui.update(mouseOver || expanded, pressed, delta);

        // Square off the bottom corners while the list is attached to them
        double radius = theme.radiusMedium();
        double open = animProgress;
        double bottom = radius * (1 - open);

        int fill = Colors.lerp(d.field, d.fieldHover, Math.max(ui.hover(), open));
        int border = Colors.lerp(d.outline, Colors.withAlpha(d.accent, 190), Math.max(ui.hover() * 0.8, open));

        renderer.roundRect(x, y, width, height, radius, radius, bottom, bottom, fill, lineWidth(), border);

        String text = get().toString();
        double padX = theme.space(10);
        renderer.text(text, x + padX, y + (height - theme.textHeight()) / 2, d.text, false);

        // Chevron that turns upside down when the list is open
        double s = theme.textHeight() * 0.6;
        chevron(renderer, x + width - padX - s / 2, y + height / 2, s, 180 * open, theme.scale(2), Colors.lerp(d.textSecondary, d.text, Math.max(ui.hover(), open)));
    }

    private static class WRoot extends WDropdownRoot implements FlorenceWidget {
        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            FlorenceGuiTheme theme = theme();
            Design d = design();

            double radius = theme.radiusMedium();
            renderer.roundRect(x, y, width, height, 0, 0, radius, radius, Colors.withAlpha(d.header, 252), lineWidth(), d.outlineHover);
        }
    }

    private class WValue extends WDropdownValue implements FlorenceWidget {
        private final Interaction ui = new Interaction();

        @Override
        protected void onCalculateSize() {
            FlorenceGuiTheme theme = theme();

            width = theme.space(10) + theme.textWidth(value.toString()) + theme.space(10);
            height = theme.space(5) + theme.textHeight() + theme.space(5);
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            FlorenceGuiTheme theme = theme();
            Design d = design();

            ui.update(mouseOver, pressed, delta);

            boolean selected = WFlorenceDropdown.this.value.equals(value);

            if (ui.hover() > 0.01 || selected) {
                int color = selected ? Colors.lerp(d.accentSoft, Colors.withAlpha(d.accent, 80), ui.hover()) : Colors.withAlpha(d.accentSoft, (int) (Colors.alpha(d.accentSoft) * ui.hover()));
                renderer.roundRect(x, y, width, height, theme.radiusSmall(), color);
            }

            String text = value.toString();
            int textColor = selected ? d.text : Colors.lerp(d.textSecondary, d.text, ui.hover());
            renderer.text(text, x + theme.space(10), y + (height - theme.textHeight()) / 2, textColor, false);

            if (selected) {
                double dot = theme.scale(5);
                renderer.circle(x + width - theme.space(10) - dot / 2, y + height / 2, dot / 2, d.accent);
            }
        }
    }
}
