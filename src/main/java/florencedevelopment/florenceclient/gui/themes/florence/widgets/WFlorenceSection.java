/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.themes.florence.widgets;

import florencedevelopment.florenceclient.gui.animation.Interaction;
import florencedevelopment.florenceclient.gui.design.Colors;
import florencedevelopment.florenceclient.gui.design.Design;
import florencedevelopment.florenceclient.gui.renderer.GuiRenderer;
import florencedevelopment.florenceclient.gui.themes.florence.FlorenceWidget;
import florencedevelopment.florenceclient.gui.widgets.WWidget;
import florencedevelopment.florenceclient.gui.widgets.containers.WSection;
import florencedevelopment.florenceclient.gui.widgets.pressable.WTriangle;

public class WFlorenceSection extends WSection {
    public WFlorenceSection(String title, boolean expanded, WWidget headerWidget) {
        super(title, expanded, headerWidget);
    }

    @Override
    protected WHeader createHeader() {
        return new WFlorenceHeader(title);
    }

    protected class WFlorenceHeader extends WHeader {
        private WTriangle triangle;

        public WFlorenceHeader(String title) {
            super(title);
        }

        @Override
        public void init() {
            add(theme.horizontalSeparator(title)).expandX();

            if (headerWidget != null) add(headerWidget);

            triangle = new WHeaderTriangle();
            triangle.theme = theme;
            triangle.action = this::onClick;

            add(triangle);
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            triangle.rotation = (1 - expandedAmount()) * -90;
        }
    }

    protected static class WHeaderTriangle extends WTriangle implements FlorenceWidget {
        private final Interaction ui = new Interaction();

        @Override
        protected void onCalculateSize() {
            width = theme.textHeight();
            height = width;
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            Design d = design();

            ui.update(mouseOver, pressed, delta);

            chevron(renderer, x + width / 2, y + height / 2, width * 0.5, rotation, theme.scale(2), Colors.lerp(d.textSecondary, d.text, ui.hover()));
        }
    }
}
