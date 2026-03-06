/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.themes.florence.widgets.input;

import florencedevelopment.florenceclient.gui.renderer.GuiRenderer;
import florencedevelopment.florenceclient.gui.themes.florence.FlorenceGuiTheme;
import florencedevelopment.florenceclient.gui.themes.florence.FlorenceWidget;
import florencedevelopment.florenceclient.gui.widgets.input.WSlider;
import florencedevelopment.florenceclient.utils.render.color.Color;

public class WFlorenceSlider extends WSlider implements FlorenceWidget {
    public WFlorenceSlider(double value, double min, double max) {
        super(value, min, max);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        double valueWidth = valueWidth();

        renderBar(renderer, valueWidth);
        renderHandle(renderer, valueWidth);
    }

    private void renderBar(GuiRenderer renderer, double valueWidth) {
        FlorenceGuiTheme theme = theme();

        double s = theme.scale(3);
        double handleSize = handleSize();

        double x = this.x + handleSize / 2;
        double y = this.y + height / 2 - s / 2;

        // Modern gradient bar
        Color leftColor = theme.sliderLeft.get();
        Color rightColor = theme.sliderRight.get();
        
        // Active portion with gradient
        Color activeStart = new Color(leftColor.r, leftColor.g, leftColor.b, leftColor.a);
        Color activeEnd = new Color(
            Math.min(255, leftColor.r + 20),
            Math.min(255, leftColor.g + 20),
            Math.min(255, leftColor.b + 20),
            leftColor.a
        );
        renderer.quad(x, y, valueWidth, s, activeStart, activeEnd, activeEnd, activeStart);

        // Inactive portion
        renderer.quad(x + valueWidth, y, width - valueWidth - handleSize, s, rightColor);
    }

    private void renderHandle(GuiRenderer renderer, double valueWidth) {
        FlorenceGuiTheme theme = theme();
        double s = handleSize();

        // Modern handle with glow effect
        Color handleColor = theme.sliderHandle.get(dragging, handleMouseOver);
        
        // Add glow shadow
        if (theme.enableShadows() && (dragging || handleMouseOver)) {
            Color glowColor = new Color(handleColor.r, handleColor.g, handleColor.b, 80);
            renderer.quad(x + valueWidth - 1, y - 1, s + 2, s + 2, GuiRenderer.CIRCLE, glowColor);
        }
        
        // Main handle
        renderer.quad(x + valueWidth, y, s, s, GuiRenderer.CIRCLE, handleColor);
        
        // Inner highlight
        if (dragging || handleMouseOver) {
            Color highlight = new Color(255, 255, 255, 100);
            renderer.quad(x + valueWidth + s * 0.25, y + s * 0.25, s * 0.5, s * 0.5, GuiRenderer.CIRCLE, highlight);
        }
    }
}
