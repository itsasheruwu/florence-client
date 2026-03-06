/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.themes.florence;

import florencedevelopment.florenceclient.gui.renderer.GuiRenderer;
import florencedevelopment.florenceclient.gui.utils.BaseWidget;
import florencedevelopment.florenceclient.gui.widgets.WWidget;
import florencedevelopment.florenceclient.utils.render.color.Color;

public interface FlorenceWidget extends BaseWidget {
    default FlorenceGuiTheme theme() {
        return (FlorenceGuiTheme) getTheme();
    }

    default void renderBackground(GuiRenderer renderer, WWidget widget, Color outlineColor, Color backgroundColor) {
        FlorenceGuiTheme theme = theme();
        double s = theme.scale(2);
        double radius = theme.roundedCorners() ? theme.cornerRadius() : 0;

        // Render shadow if enabled
        if (theme.enableShadows() && radius > 0) {
            Color shadowColor = new Color(0, 0, 0, 80);
            renderer.quad(widget.x + 2, widget.y + 2, widget.width, widget.height, shadowColor);
        }

        // Render main background with rounded corners simulation
        if (radius > 0) {
            // For now, use regular quads but with better spacing
            // In a full implementation, you'd use actual rounded rectangle rendering
            renderer.quad(widget.x + s, widget.y + s, widget.width - s * 2, widget.height - s * 2, backgroundColor);
        } else {
            renderer.quad(widget.x + s, widget.y + s, widget.width - s * 2, widget.height - s * 2, backgroundColor);
        }

        // Render outline with gradient effect
        Color outlineTop = outlineColor;
        Color outlineBottom = new Color(outlineColor.r, outlineColor.g, outlineColor.b, (int)(outlineColor.a * 0.7));
        
        renderer.quad(widget.x, widget.y, widget.width, s, outlineTop);
        renderer.quad(widget.x, widget.y + widget.height - s, widget.width, s, outlineBottom);
        renderer.quad(widget.x, widget.y + s, s, widget.height - s * 2, outlineTop);
        renderer.quad(widget.x + widget.width - s, widget.y + s, s, widget.height - s * 2, outlineTop);
    }

    default void renderBackground(GuiRenderer renderer, WWidget widget, boolean pressed, boolean mouseOver) {
        FlorenceGuiTheme theme = theme();
        Color bgColor = theme.backgroundColor.get(pressed, mouseOver);
        Color outlineColor = theme.outlineColor.get(pressed, mouseOver);
        
        // Add glass effect on hover
        if (mouseOver && !pressed && theme.enableShadows()) {
            Color glassColor = theme.glassEffectColor();
            renderer.quad(widget.x, widget.y, widget.width, widget.height, glassColor);
        }
        
        renderBackground(renderer, widget, outlineColor, bgColor);
    }

    default void renderRoundedBackground(GuiRenderer renderer, WWidget widget, Color backgroundColor, Color outlineColor) {
        FlorenceGuiTheme theme = theme();
        double radius = theme.roundedCorners() ? theme.cornerRadius() : 0;
        double s = theme.scale(2);

        // Shadow
        if (theme.enableShadows() && radius > 0) {
            Color shadowColor = new Color(0, 0, 0, 60);
            renderer.quad(widget.x + 1, widget.y + 1, widget.width, widget.height, shadowColor);
        }

        // Background
        renderer.quad(widget.x + s, widget.y + s, widget.width - s * 2, widget.height - s * 2, backgroundColor);
        
        // Outline
        renderer.quad(widget.x, widget.y, widget.width, s, outlineColor);
        renderer.quad(widget.x, widget.y + widget.height - s, widget.width, s, outlineColor);
        renderer.quad(widget.x, widget.y + s, s, widget.height - s * 2, outlineColor);
        renderer.quad(widget.x + widget.width - s, widget.y + s, s, widget.height - s * 2, outlineColor);
    }
}
