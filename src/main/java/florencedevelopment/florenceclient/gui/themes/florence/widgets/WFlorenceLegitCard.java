/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.themes.florence.widgets;

import florencedevelopment.florenceclient.gui.animation.Interaction;
import florencedevelopment.florenceclient.gui.design.CategoryColors;
import florencedevelopment.florenceclient.gui.design.Colors;
import florencedevelopment.florenceclient.gui.design.Design;
import florencedevelopment.florenceclient.gui.renderer.GuiRenderer;
import florencedevelopment.florenceclient.gui.screens.LegitIconPainter;
import florencedevelopment.florenceclient.gui.themes.florence.FlorenceGuiTheme;
import florencedevelopment.florenceclient.gui.themes.florence.FlorenceWidget;
import florencedevelopment.florenceclient.gui.widgets.WWidget;
import florencedevelopment.florenceclient.settings.Setting;
import florencedevelopment.florenceclient.settings.SettingGroup;
import florencedevelopment.florenceclient.systems.modules.Module;
import net.minecraft.client.gui.Click;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;

/**
 * A module in the Legit tab: its icon, its name, a button for its options and a bar that shows if it is enabled and
 * turns it on or off when clicked.
 */
public class WFlorenceLegitCard extends WWidget implements FlorenceWidget {
    private final Module module;
    private final Runnable openOptions;
    private final boolean hasSettings;

    private final Interaction ui = new Interaction();
    private final Interaction optionsUi = new Interaction();
    private final Interaction barUi = new Interaction();

    public WFlorenceLegitCard(Module module, Runnable openOptions) {
        this.module = module;
        this.openOptions = openOptions;
        this.tooltip = module.description;
        this.hasSettings = hasSettings(module);
    }

    private static boolean hasSettings(Module module) {
        for (SettingGroup group : module.settings.groups) {
            for (Setting<?> ignored : group) return true;
        }

        return false;
    }

    // Layout

    private double inner() {
        return theme().space(10);
    }

    private double iconSize() {
        return theme.scale(44);
    }

    private double buttonHeight() {
        return theme.textHeight() + theme().space(10);
    }

    @Override
    protected void onCalculateSize() {
        FlorenceGuiTheme theme = theme();

        width = theme.scale(184);
        height = inner() + iconSize() + theme.space(6) + theme.textHeight() + theme.space(10) + buttonHeight() + theme.space(6) + buttonHeight() + theme.space(8);
    }

    private double barY() {
        return y + height - theme().space(8) - buttonHeight();
    }

    private double optionsY() {
        return barY() - theme().space(6) - buttonHeight();
    }

    private double sideInset() {
        return theme().space(8);
    }

    private boolean overBar(double mx, double my) {
        return mx >= x + sideInset() && mx <= x + width - sideInset() && my >= barY() && my <= barY() + buttonHeight();
    }

    private boolean overOptions(double mx, double my) {
        return mx >= x + sideInset() && mx <= x + width - sideInset() && my >= optionsY() && my <= optionsY() + buttonHeight();
    }

    // Input

    @Override
    public boolean onMouseClicked(Click click, boolean doubled) {
        if (!mouseOver) return false;

        double mx = click.x();
        double my = click.y();

        if (click.button() == GLFW_MOUSE_BUTTON_LEFT && overBar(mx, my)) {
            module.toggle();
            return true;
        }

        boolean open = (click.button() == GLFW_MOUSE_BUTTON_LEFT && overOptions(mx, my)) || click.button() == GLFW_MOUSE_BUTTON_RIGHT;

        if (open && hasSettings) {
            openOptions.run();
            return true;
        }

        return false;
    }

    // Rendering

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        FlorenceGuiTheme theme = theme();
        Design d = design();

        ui.update(mouseOver, false, delta);
        optionsUi.update(mouseOver && overOptions(mouseX, mouseY), false, delta);
        barUi.update(mouseOver && overBar(mouseX, mouseY), false, delta);

        double pad = inner();
        double th = theme.textHeight();
        double line = lineWidth();
        double radius = theme.radiusLarge() * 0.8;
        double inset = sideInset();
        int category = CategoryColors.of(module.category.name);
        boolean on = module.isActive();

        // Card
        int fill = Colors.lerp(d.card, d.cardHover, ui.hover());
        int border = Colors.lerp(d.outline, d.outlineHover, ui.hover());

        if (ui.hover() > 0.01 && theme.shadows()) {
            renderer.shadow(x, y + theme.scale(3), width, height, radius, theme.scale(12), Colors.mulAlpha(d.shadow, 0.5 * ui.hover()));
        }

        renderer.roundRect(x, y, width, height, radius, fill, line, border);

        // Icon, on a soft glow in the color of the category
        double iconSize = iconSize();
        double iconCenterX = x + width / 2;
        double iconTop = y + pad;

        renderer.roundRect(iconCenterX - iconSize * 0.62, iconTop - iconSize * 0.08, iconSize * 1.24, iconSize * 1.24, iconSize * 0.62, Colors.withAlpha(category, on ? 56 : 28));

        // Custom icon, the first letter for modules without one
        int iconColor = Colors.lerp(Colors.lighten(category, 0.3), Colors.rgb(255, 255, 255), on ? 0.55 : 0);
        double iconCenterY = iconTop + iconSize / 2;

        if (!LegitIconPainter.draw(renderer, module.name, iconCenterX, iconCenterY, iconSize * 0.62, iconColor)) {
            String letter = module.title.isEmpty() ? "?" : module.title.substring(0, 1).toUpperCase();
            renderer.text(letter, iconCenterX - theme.textWidth(letter) / 2, iconCenterY - th / 2, iconColor, false);
        }

        // Name
        String name = fit(module.title, width - pad * 2);
        renderer.text(name, x + (width - theme.textWidth(name)) / 2, iconTop + iconSize + theme.space(6), Colors.lerp(d.textSecondary, d.text, Math.max(ui.hover(), on ? 1 : 0)), false);

        // Options button
        double bh = buttonHeight();
        double bw = width - inset * 2;
        double oy = optionsY();

        int optionsFill = Colors.lerp(d.field, d.fieldHover, hasSettings ? optionsUi.hover() : 0);
        int optionsBorder = Colors.lerp(d.outline, Colors.withAlpha(d.accent, 170), hasSettings ? optionsUi.hover() : 0);

        renderer.roundRect(x + inset, oy, bw, bh, theme.radiusMedium(), optionsFill, line, optionsBorder);

        String options = "OPTIONS";
        int optionsText = hasSettings ? Colors.lerp(d.textSecondary, d.text, optionsUi.hover()) : d.textDisabled;
        renderer.text(options, x + (width - theme.textWidth(options)) / 2, oy + (bh - th) / 2, optionsText, false);

        // Enabled / disabled bar
        double by = barY();
        int barColor = on ? d.success : d.danger;
        int barFill = Colors.lerp(Colors.withAlpha(barColor, 205), barColor, barUi.hover());

        renderer.roundRect(x + inset, by, bw, bh, theme.radiusMedium(), barFill, line, Colors.withAlpha(Colors.lighten(barColor, 0.3), 120));

        String state = on ? "ENABLED" : "DISABLED";
        renderer.text(state, x + (width - theme.textWidth(state)) / 2, by + (bh - th) / 2, Colors.rgb(255, 255, 255), false);
    }

    private String fit(String text, double space) {
        FlorenceGuiTheme theme = theme();

        if (space <= 0) return "";
        if (theme.textWidth(text) <= space) return text;

        double dots = theme.textWidth("...");
        int end = text.length();

        while (end > 0 && theme.textWidth(text.substring(0, end)) + dots > space) end--;

        return end <= 0 ? "..." : text.substring(0, end).stripTrailing() + "...";
    }
}
