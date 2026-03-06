/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.themes.florence.widgets;

import florencedevelopment.florenceclient.gui.renderer.GuiRenderer;
import florencedevelopment.florenceclient.gui.themes.florence.FlorenceGuiTheme;
import florencedevelopment.florenceclient.gui.themes.florence.FlorenceWidget;
import florencedevelopment.florenceclient.gui.utils.AlignmentX;
import florencedevelopment.florenceclient.gui.widgets.pressable.WPressable;
import florencedevelopment.florenceclient.systems.modules.Module;
import florencedevelopment.florenceclient.utils.render.color.Color;
import net.minecraft.util.math.MathHelper;

import static florencedevelopment.florenceclient.FlorenceClient.mc;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;

public class WFlorenceModule extends WPressable implements FlorenceWidget {
    private final Module module;
    private final String title;

    private double titleWidth;

    private double animationProgress1;

    private double animationProgress2;

    public WFlorenceModule(Module module, String title) {
        this.module = module;
        this.title = title;
        this.tooltip = module.description;

        if (module.isActive()) {
            animationProgress1 = 1;
            animationProgress2 = 1;
        } else {
            animationProgress1 = 0;
            animationProgress2 = 0;
        }
    }

    @Override
    public double pad() {
        return theme.scale(4);
    }

    @Override
    protected void onCalculateSize() {
        double pad = pad();

        if (titleWidth == 0) titleWidth = theme.textWidth(title);

        width = pad + titleWidth + pad;
        height = pad + theme.textHeight() + pad;
    }

    @Override
    protected void onPressed(int button) {
        if (button == GLFW_MOUSE_BUTTON_LEFT) module.toggle();
        else if (button == GLFW_MOUSE_BUTTON_RIGHT) mc.setScreen(theme.moduleScreen(module));
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        FlorenceGuiTheme theme = theme();
        double pad = pad();

        // Smooth animations
        double animSpeed1 = theme.smoothAnimations() ? 4 : 6;
        double animSpeed2 = theme.smoothAnimations() ? 6 : 8;
        
        animationProgress1 += delta * animSpeed1 * ((module.isActive() || mouseOver) ? 1 : -1);
        animationProgress1 = MathHelper.clamp(animationProgress1, 0, 1);

        animationProgress2 += delta * animSpeed2 * (module.isActive() ? 1 : -1);
        animationProgress2 = MathHelper.clamp(animationProgress2, 0, 1);

        // Modern background with gradient
        if (animationProgress1 > 0) {
            Color bgColor = theme.moduleBackground.get();
            
            // Add hover effect
            if (mouseOver && !module.isActive()) {
                Color hoverColor = theme.accentColor.get();
                bgColor = new Color(
                    (int)(bgColor.r + (hoverColor.r - bgColor.r) * 0.2),
                    (int)(bgColor.g + (hoverColor.g - bgColor.g) * 0.2),
                    (int)(bgColor.b + (hoverColor.b - bgColor.b) * 0.2),
                    bgColor.a
                );
            }
            
            // Gradient effect for active modules
            if (module.isActive()) {
                Color accent1 = theme.accentColor.get();
                Color accent2 = theme.accentSecondaryColor();
                Color bgStart = new Color(
                    (int)(bgColor.r + (accent1.r - bgColor.r) * 0.3),
                    (int)(bgColor.g + (accent1.g - bgColor.g) * 0.3),
                    (int)(bgColor.b + (accent1.b - bgColor.b) * 0.3),
                    bgColor.a
                );
                Color bgEnd = new Color(
                    (int)(bgColor.r + (accent2.r - bgColor.r) * 0.2),
                    (int)(bgColor.g + (accent2.g - bgColor.g) * 0.2),
                    (int)(bgColor.b + (accent2.b - bgColor.b) * 0.2),
                    bgColor.a
                );
                renderer.quad(x, y, width * animationProgress1, height, bgStart, bgEnd, bgEnd, bgStart);
            } else {
                renderer.quad(x, y, width * animationProgress1, height, bgColor);
            }
        }
        
        // Modern accent indicator with glow
        if (animationProgress2 > 0) {
            Color accentColor = theme.accentColor.get();
            
            // Glow effect
            if (theme.enableShadows()) {
                Color glowColor = new Color(accentColor.r, accentColor.g, accentColor.b, (int)(accentColor.a * 0.5));
                renderer.quad(x - 1, y + height * (1 - animationProgress2) - 1, theme.scale(4), height * animationProgress2 + 2, glowColor);
            }
            
            renderer.quad(x, y + height * (1 - animationProgress2), theme.scale(2), height * animationProgress2, accentColor);
        }

        double x = this.x + pad;
        double w = width - pad * 2;

        if (theme.moduleAlignment.get() == AlignmentX.Center) {
            x += w / 2 - titleWidth / 2;
        }
        else if (theme.moduleAlignment.get() == AlignmentX.Right) {
            x += w - titleWidth;
        }

        // Text color with active state
        Color textColor = theme.textColor.get();
        if (module.isActive()) {
            textColor = new Color(
                Math.min(255, textColor.r + 20),
                Math.min(255, textColor.g + 20),
                Math.min(255, textColor.b + 20),
                textColor.a
            );
        }
        
        renderer.text(title, x, y + pad, textColor, false);
    }
}
