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
import florencedevelopment.florenceclient.gui.screens.palette.CommandPaletteScreen;
import florencedevelopment.florenceclient.gui.tabs.Tab;
import florencedevelopment.florenceclient.gui.themes.florence.FlorenceGuiTheme;
import florencedevelopment.florenceclient.gui.themes.florence.FlorenceWidget;
import florencedevelopment.florenceclient.gui.utils.Cell;
import florencedevelopment.florenceclient.gui.widgets.WTopBar;
import florencedevelopment.florenceclient.gui.widgets.pressable.WPressable;
import florencedevelopment.florenceclient.utils.render.color.Color;
import net.minecraft.client.util.MacWindowUtil;

import static florencedevelopment.florenceclient.FlorenceClient.mc;

/**
 * The tabs of the GUI in a rounded bar, with a highlight that slides to the tab that is open.
 */
public class WFlorenceTopBar extends WTopBar implements FlorenceWidget {
    private final AnimatedFloat indicatorX = new AnimatedFloat(0);
    private final AnimatedFloat indicatorWidth = new AnimatedFloat(0);
    private boolean indicatorPlaced;

    @Override
    public void init() {
        spacing = 2;

        super.init();

        add(new WSearchButton());
    }

    @Override
    protected Color getButtonColor(boolean pressed, boolean hovered) {
        return theme().textColor();
    }

    @Override
    protected Color getNameColor() {
        return theme().textColor();
    }

    @Override
    protected WTopBarButton createButton(Tab tab) {
        return new WFlorenceTopBarButton(tab);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        FlorenceGuiTheme theme = theme();
        Design d = design();

        double pad = theme.space(4);
        double bx = x - pad;
        double by = y - pad;
        double bw = width + pad * 2;
        double bh = height + pad * 2;
        double radius = theme.radiusMedium() + pad;

        // Bar
        if (theme.shadows()) renderer.shadow(bx, by + 2, bw, bh, radius, theme.scale(18), d.shadow);

        if (theme.glass() && renderer.hasBackdrop()) renderer.glass(bx, by, bw, bh, radius, d.panel, lineWidth(), d.outline);
        else renderer.roundRect(bx, by, bw, bh, radius, d.panelSolid, lineWidth(), d.outline);

        // Highlight behind the tab that is open
        for (Cell<?> cell : cells) {
            if (cell.widget() instanceof WFlorenceTopBarButton button && button.isCurrent()) {
                if (!indicatorPlaced) {
                    indicatorX.snap(button.x);
                    indicatorWidth.snap(button.width);
                    indicatorPlaced = true;
                }

                indicatorX.springTo(button.x, Spring.CRITICAL);
                indicatorWidth.springTo(button.width, Spring.CRITICAL);

                break;
            }
        }

        if (indicatorPlaced) {
            double ix = indicatorX.update(delta);
            double iw = indicatorWidth.update(delta);

            renderer.roundRect(ix, y, iw, height, theme.radiusMedium(), d.cardActive, lineWidth(), Colors.withAlpha(d.accent, 120));
        }
    }

    /**
     * Opens the search, and shows which keys do the same.
     */
    private class WSearchButton extends WPressable implements FlorenceWidget {
        private final Interaction ui = new Interaction();
        private final String keys = MacWindowUtil.IS_MAC ? "Cmd K" : "Ctrl K";

        @Override
        protected void onCalculateSize() {
            FlorenceGuiTheme theme = theme();

            width = theme.space(14) + theme.textWidth("Search") + theme.space(10) + theme.textWidth(keys) + theme.space(12) + theme.space(14);
            height = theme.space(7) + theme.textHeight() + theme.space(7);
        }

        @Override
        protected void onPressed(int button) {
            mc.setScreen(new CommandPaletteScreen(theme));
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            FlorenceGuiTheme theme = theme();
            Design d = design();

            ui.update(mouseOver, pressed, delta);

            if (ui.hover() > 0.01) {
                renderer.roundRect(x, y, width, height, theme.radiusMedium(), Colors.mulAlpha(d.cardHover, ui.hover()));
            }

            double th = theme.textHeight();
            double textX = x + theme.space(14);

            renderer.text("Search", textX, y + (height - th) / 2, Colors.lerp(d.textSecondary, d.text, ui.hover()), false);

            // The keys
            double chipWidth = theme.textWidth(keys) + theme.space(12);
            double chipHeight = th * 0.95;
            double chipX = x + width - theme.space(14) - chipWidth;

            renderer.roundRect(chipX, y + (height - chipHeight) / 2, chipWidth, chipHeight, theme.radiusSmall(), d.field, lineWidth(), d.outline);
            renderer.text(keys, chipX + (chipWidth - theme.textWidth(keys)) / 2, y + (height - th) / 2, d.textDisabled, false);
        }
    }

    private class WFlorenceTopBarButton extends WTopBarButton {
        private final Interaction ui = new Interaction();

        public WFlorenceTopBarButton(Tab tab) {
            super(tab);
        }

        @Override
        protected void onCalculateSize() {
            FlorenceGuiTheme theme = theme();

            width = theme.space(14) + theme.textWidth(tab.name) + theme.space(14);
            height = theme.space(7) + theme.textHeight() + theme.space(7);
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            FlorenceGuiTheme theme = theme();
            Design d = design();

            ui.update(mouseOver, pressed, delta);

            boolean current = isCurrent();

            if (!current && ui.hover() > 0.01) {
                renderer.roundRect(x, y, width, height, theme.radiusMedium(), Colors.mulAlpha(d.cardHover, ui.hover()));
            }

            int color = current ? d.text : Colors.lerp(d.textSecondary, d.text, ui.hover());
            renderer.text(tab.name, x + (width - theme.textWidth(tab.name)) / 2, y + (height - theme.textHeight()) / 2, color, false);
        }
    }
}
