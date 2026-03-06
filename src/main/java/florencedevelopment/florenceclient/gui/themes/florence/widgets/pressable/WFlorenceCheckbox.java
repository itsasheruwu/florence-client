/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.themes.florence.widgets.pressable;

import florencedevelopment.florenceclient.gui.renderer.GuiRenderer;
import florencedevelopment.florenceclient.gui.themes.florence.FlorenceGuiTheme;
import florencedevelopment.florenceclient.gui.themes.florence.FlorenceWidget;
import florencedevelopment.florenceclient.gui.widgets.pressable.WCheckbox;
import florencedevelopment.florenceclient.utils.render.color.Color;
import net.minecraft.util.math.MathHelper;

public class WFlorenceCheckbox extends WCheckbox implements FlorenceWidget {
    private double animProgress;

    public WFlorenceCheckbox(boolean checked) {
        super(checked);
        animProgress = checked ? 1 : 0;
    }

    private double hoverProgress = 0;

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        FlorenceGuiTheme theme = theme();

        // Smooth animation
        double animSpeed = theme.smoothAnimations() ? 14 : 20;
        animProgress += (checked ? 1 : -1) * delta * animSpeed;
        animProgress = MathHelper.clamp(animProgress, 0, 1);

        // Smooth hover animation
        if (theme.smoothAnimations()) {
            double target = mouseOver ? 1 : 0;
            hoverProgress += (target - hoverProgress) * delta * 14;
            hoverProgress = Math.max(0, Math.min(1, hoverProgress));
        } else {
            hoverProgress = mouseOver ? 1 : 0;
        }

        // Modern background with hover effect
        Color bgColor = theme.backgroundColor.get(pressed, mouseOver);
        if (hoverProgress > 0 && !pressed) {
            Color accentColor = theme.accentColor.get();
            bgColor = new Color(
                (int)(bgColor.r + (accentColor.r - bgColor.r) * hoverProgress * 0.1),
                (int)(bgColor.g + (accentColor.g - bgColor.g) * hoverProgress * 0.1),
                (int)(bgColor.b + (accentColor.b - bgColor.b) * hoverProgress * 0.1),
                bgColor.a
            );
        }
        
        renderBackground(renderer, this, theme.outlineColor.get(pressed, mouseOver), bgColor);

        // Modern checkmark with smooth animation
        if (animProgress > 0) {
            Color checkColor = theme.checkboxColor.get();
            
            // Add glow effect
            if (theme.enableShadows() && animProgress > 0.5) {
                Color glowColor = new Color(checkColor.r, checkColor.g, checkColor.b, (int)(checkColor.a * 0.3 * animProgress));
                double glowSize = (width - theme.scale(2)) / 1.75 * animProgress + 2;
                renderer.quad(x + (width - glowSize) / 2, y + (height - glowSize) / 2, glowSize, glowSize, GuiRenderer.CIRCLE, glowColor);
            }
            
            double cs = (width - theme.scale(2)) / 1.75 * animProgress;
            renderer.quad(x + (width - cs) / 2, y + (height - cs) / 2, cs, cs, GuiRenderer.CIRCLE, checkColor);
        }
    }
}
