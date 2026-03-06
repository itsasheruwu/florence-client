/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.themes.florence.widgets.pressable;

import florencedevelopment.florenceclient.gui.renderer.GuiRenderer;
import florencedevelopment.florenceclient.gui.renderer.packer.GuiTexture;
import florencedevelopment.florenceclient.gui.themes.florence.FlorenceGuiTheme;
import florencedevelopment.florenceclient.gui.themes.florence.FlorenceWidget;
import florencedevelopment.florenceclient.gui.widgets.pressable.WButton;
import florencedevelopment.florenceclient.utils.render.color.Color;

public class WFlorenceButton extends WButton implements FlorenceWidget {
    public WFlorenceButton(String text, GuiTexture texture) {
        super(text, texture);
    }

    private double hoverProgress = 0;

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        FlorenceGuiTheme theme = theme();
        double pad = pad();

        // Smooth hover animation
        if (theme.smoothAnimations()) {
            double target = mouseOver ? 1 : 0;
            hoverProgress += (target - hoverProgress) * delta * 14;
            hoverProgress = Math.max(0, Math.min(1, hoverProgress));
        } else {
            hoverProgress = mouseOver ? 1 : 0;
        }

        // Render background with modern styling
        renderBackground(renderer, this, pressed, mouseOver);

        // Add accent glow on hover
        if (hoverProgress > 0 && !pressed && theme.enableShadows()) {
            Color glowColor = new Color(theme.accentColor.get().r, theme.accentColor.get().g, theme.accentColor.get().b, (int)(hoverProgress * 30));
            renderer.quad(x - 1, y - 1, width + 2, height + 2, glowColor);
        }

        // Text color with hover effect
        Color textColor = theme.textColor.get();
        if (hoverProgress > 0 && !pressed) {
            Color accentColor = theme.accentColor.get();
            textColor = new Color(
                (int)(textColor.r + (accentColor.r - textColor.r) * hoverProgress * 0.3),
                (int)(textColor.g + (accentColor.g - textColor.g) * hoverProgress * 0.3),
                (int)(textColor.b + (accentColor.b - textColor.b) * hoverProgress * 0.3),
                textColor.a
            );
        }

        if (text != null) {
            renderer.text(text, x + width / 2 - textWidth / 2, y + pad, textColor, false);
        }
        else {
            double ts = theme.textHeight();
            renderer.quad(x + width / 2 - ts / 2, y + pad, ts, ts, texture, textColor);
        }
    }
}
