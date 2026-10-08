/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.themes.florence;

import florencedevelopment.florenceclient.gui.animation.Interaction;
import florencedevelopment.florenceclient.gui.design.Colors;
import florencedevelopment.florenceclient.gui.design.Design;
import florencedevelopment.florenceclient.gui.renderer.GuiRenderer;
import florencedevelopment.florenceclient.gui.utils.BaseWidget;
import florencedevelopment.florenceclient.gui.widgets.WWidget;

public interface FlorenceWidget extends BaseWidget {
    default FlorenceGuiTheme theme() {
        return (FlorenceGuiTheme) getTheme();
    }

    default Design design() {
        return theme().design();
    }

    /**
     * Width of the thin lines around controls.
     */
    default double lineWidth() {
        return Math.max(1, Math.round(theme().scale(1)));
    }

    /**
     * Draws the background and outline shared by buttons and other things that can be clicked: it brightens when the
     * mouse is over, gets the accent color when pressed.
     */
    default void renderControl(GuiRenderer renderer, WWidget widget, Interaction ui) {
        Design d = design();
        FlorenceGuiTheme theme = theme();

        int fill = Colors.lerp(d.field, d.fieldHover, ui.hover());
        fill = Colors.lerp(fill, Colors.withAlpha(d.accent, 210), ui.press());

        int border = Colors.lerp(d.outline, Colors.withAlpha(d.accent, 170), ui.hover());

        renderer.roundRect(widget.x, widget.y, widget.width, widget.height, theme.radiusMedium(), fill, lineWidth(), border);
    }

    /**
     * Draws a chevron. At a rotation of 0 it points down, positive angles turn it clockwise.
     *
     * @param size width of the chevron
     */
    default void chevron(GuiRenderer renderer, double centerX, double centerY, double size, double rotation, double thickness, int color) {
        double rad = Math.toRadians(rotation);
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);

        double hw = size / 2;
        double hh = size / 4;

        // Left end, tip and right end of a chevron pointing down
        double lx = centerX + (-hw) * cos - (-hh) * sin;
        double ly = centerY + (-hw) * sin + (-hh) * cos;
        double tx = centerX - hh * sin;
        double ty = centerY + hh * cos;
        double rx = centerX + hw * cos - (-hh) * sin;
        double ry = centerY + hw * sin + (-hh) * cos;

        renderer.line(lx, ly, tx, ty, thickness, color);
        renderer.line(tx, ty, rx, ry, thickness, color);
    }

    /**
     * The color text on a control should have: brighter on hover, readable on the accent when pressed.
     */
    default int controlTextColor(Interaction ui) {
        Design d = design();
        return Colors.lerp(d.text, d.textOnAccent, ui.press());
    }
}
