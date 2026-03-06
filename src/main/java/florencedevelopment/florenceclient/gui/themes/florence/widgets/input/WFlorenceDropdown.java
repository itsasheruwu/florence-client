/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.themes.florence.widgets.input;

import florencedevelopment.florenceclient.gui.renderer.GuiRenderer;
import florencedevelopment.florenceclient.gui.themes.florence.FlorenceGuiTheme;
import florencedevelopment.florenceclient.gui.themes.florence.FlorenceWidget;
import florencedevelopment.florenceclient.gui.widgets.input.WDropdown;
import florencedevelopment.florenceclient.utils.render.color.Color;

public class WFlorenceDropdown<T> extends WDropdown<T> implements FlorenceWidget {
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

    private double hoverProgress = 0;

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        FlorenceGuiTheme theme = theme();
        double pad = pad();
        double s = theme.textHeight();

        // Smooth hover animation
        if (theme.smoothAnimations()) {
            double target = mouseOver ? 1 : 0;
            hoverProgress += (target - hoverProgress) * delta * 14;
            hoverProgress = Math.max(0, Math.min(1, hoverProgress));
        } else {
            hoverProgress = mouseOver ? 1 : 0;
        }

        // Modern background with hover effect
        renderBackground(renderer, this, pressed, mouseOver);

        // Add accent border on hover
        if (hoverProgress > 0 && !pressed && theme.enableShadows()) {
            Color accentColor = theme.accentColor.get();
            Color borderColor = new Color(accentColor.r, accentColor.g, accentColor.b, (int)(hoverProgress * 100));
            double borderWidth = theme.scale(1);
            renderer.quad(x, y, width, borderWidth, borderColor);
            renderer.quad(x, y + height - borderWidth, width, borderWidth, borderColor);
            renderer.quad(x, y + borderWidth, borderWidth, height - borderWidth * 2, borderColor);
            renderer.quad(x + width - borderWidth, y + borderWidth, borderWidth, height - borderWidth * 2, borderColor);
        }

        String text = get().toString();
        double w = theme.textWidth(text);
        
        // Text color with hover effect
        Color textColor = theme.textColor.get();
        if (hoverProgress > 0 && !pressed) {
            Color accentColor = theme.accentColor.get();
            textColor = new Color(
                (int)(textColor.r + (accentColor.r - textColor.r) * hoverProgress * 0.2),
                (int)(textColor.g + (accentColor.g - textColor.g) * hoverProgress * 0.2),
                (int)(textColor.b + (accentColor.b - textColor.b) * hoverProgress * 0.2),
                textColor.a
            );
        }
        
        renderer.text(text, x + pad + maxValueWidth / 2 - w / 2, y + pad, textColor, false);

        // Modern triangle icon with color transition
        Color triangleColor = hoverProgress > 0 && !pressed ? theme.accentColor.get() : theme.textColor.get();
        renderer.rotatedQuad(x + pad + maxValueWidth + pad, y + pad, s, s, 0, GuiRenderer.TRIANGLE, triangleColor);
    }

    private static class WRoot extends WDropdownRoot implements FlorenceWidget {
        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            FlorenceGuiTheme theme = theme();
            double s = theme.scale(2);
            
            // Modern dropdown background with glass effect
            Color bgColor = theme.backgroundColor.get();
            renderer.quad(x, y, width, height, bgColor);
            
            // Glass effect overlay
            if (theme.enableShadows()) {
                Color glassColor = theme.glassEffectColor();
                renderer.quad(x, y, width, height, glassColor);
            }
            
            // Modern outline with gradient
            Color outlineColor = theme.outlineColor.get();
            renderer.quad(x, y + height - s, width, s, outlineColor);
            renderer.quad(x, y, s, height - s, outlineColor);
            renderer.quad(x + width - s, y, s, height - s, outlineColor);
        }
    }

    private class WValue extends WDropdownValue implements FlorenceWidget {
        @Override
        protected void onCalculateSize() {
            double pad = pad();

            width = pad + theme.textWidth(value.toString()) + pad;
            height = pad + theme.textHeight() + pad;
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            FlorenceGuiTheme theme = theme();

            // Modern hover effect
            Color color = theme.backgroundColor.get(pressed, mouseOver, true);
            
            // Add accent tint on hover
            if (mouseOver && !pressed) {
                Color accentColor = theme.accentColor.get();
                color = new Color(
                    (int)(color.r + (accentColor.r - color.r) * 0.15),
                    (int)(color.g + (accentColor.g - color.g) * 0.15),
                    (int)(color.b + (accentColor.b - color.b) * 0.15),
                    color.a
                );
            }
            
            int preA = color.a;
            color.a += color.a / 2;
            color.validate();

            renderer.quad(this, color);

            color.a = preA;

            // Text with hover effect
            Color textColor = theme.textColor.get();
            if (mouseOver && !pressed) {
                textColor = theme.accentColor.get();
            }
            
            String text = value.toString();
            renderer.text(text, x + width / 2 - theme.textWidth(text) / 2, y + pad(), textColor, false);
        }
    }
}
