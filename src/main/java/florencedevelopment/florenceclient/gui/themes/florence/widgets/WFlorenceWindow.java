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
import florencedevelopment.florenceclient.gui.themes.florence.FlorenceGuiTheme;
import florencedevelopment.florenceclient.gui.themes.florence.FlorenceWidget;
import florencedevelopment.florenceclient.gui.widgets.WWidget;
import florencedevelopment.florenceclient.gui.widgets.containers.WHorizontalList;
import florencedevelopment.florenceclient.gui.widgets.containers.WWindow;
import net.minecraft.client.gui.Click;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;

/**
 * A rounded window with frosted glass behind it, a soft shadow that grows while it is dragged and a header with a line
 * of the accent color under the title.
 */
public class WFlorenceWindow extends WWindow implements FlorenceWidget {
    // How far the window is lifted off the screen, 1 while it is being dragged
    private final AnimatedFloat lift = new AnimatedFloat(0);

    public WFlorenceWindow(WWidget icon, String title) {
        super(icon, title);
    }

    @Override
    protected WHeader header(WWidget icon) {
        return new WFlorenceHeader(icon);
    }

    @Override
    public void calculateSize() {
        super.calculateSize();

        // Room below the last row so it doesn't touch the rounded corners
        height += theme().space(4);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        FlorenceGuiTheme theme = theme();
        Design d = design();

        lift.springTo(dragging || resizing ? 1 : 0, Spring.SNAPPY);
        double l = Math.max(0, lift.update(delta));

        double visibleHeight = visibleHeight();
        double radius = theme.radiusLarge();
        double bottomRadius = radius;

        // The shadow grows while the window is being moved
        if (theme.shadows()) {
            double blur = theme.scale(24 + 22 * l);
            double offset = theme.scale(6 + 8 * l);

            renderer.shadow(x, y + offset, width, visibleHeight, radius, blur, Colors.mulAlpha(d.shadow, 0.8 + 0.2 * l));
        }

        // Body
        if (theme.glass() && renderer.hasBackdrop()) {
            renderer.glass(x, y, width, visibleHeight, radius, radius, bottomRadius, bottomRadius, d.panel, lineWidth(), Colors.lerp(d.outline, d.outlineHover, l));
        }
        else {
            renderer.roundRect(x, y, width, visibleHeight, radius, radius, bottomRadius, bottomRadius, d.panelSolid, lineWidth(), Colors.lerp(d.outline, d.outlineHover, l));
        }

        // Header, it only has square bottom corners while the window is open
        double openAmount = expandedAmount();
        double headerBottom = radius * (1 - openAmount);

        renderer.roundRect(x, y, width, header.height, radius, radius, headerBottom, headerBottom, d.header, 0, 0);

        // Line under the header, with a stretch of accent color at the start
        if (openAmount > 0.02) {
            double thickness = lineWidth();
            double lineY = y + header.height - thickness;

            renderer.roundRect(x, lineY, width, thickness, 0, Colors.mulAlpha(d.divider, openAmount));

            double accentWidth = Math.min(width * 0.4, theme.scale(120));
            double accentThickness = Math.max(2, theme.scale(2));

            int accent = this.accent != 0 ? this.accent : d.accent;
            int accentEnd = Colors.withAlpha(this.accent != 0 ? this.accent : d.accent2, 0);

            renderer.roundRectHorizontal(x + radius * 0.6, y + header.height - accentThickness, accentWidth, accentThickness, accentThickness / 2,
                Colors.mulAlpha(accent, openAmount), accentEnd, 0, 0);
        }
    }

    private class WFlorenceHeader extends WHeader {
        private final Interaction ui = new Interaction();
        private final AnimatedFloat turn = new AnimatedFloat(0);

        private WHorizontalList list;

        public WFlorenceHeader(WWidget icon) {
            super(icon);
        }

        @Override
        public void init() {
            list = add(theme.horizontalList()).expandX().widget();
            list.spacing = 6;

            if (icon != null) {
                list.add(icon).centerY();
            }

            if (beforeHeaderInit != null) {
                beforeHeaderInit.accept(list);
            }

            // Space before the title when there is no icon in front of it
            list.add(theme.label(title)).expandCellX().centerY().padLeft(icon == null && beforeHeaderInit == null ? 12 : 4);
        }

        @Override
        public <T extends WWidget> florencedevelopment.florenceclient.gui.utils.Cell<T> add(T widget) {
            if (list != null) return list.add(widget);
            return super.add(widget);
        }

        @Override
        protected void onCalculateSize() {
            FlorenceGuiTheme theme = theme();

            super.onCalculateSize();

            double th = theme.textHeight();
            double pad = theme.space(8);

            // Make room on the right for the badge and the arrow
            width += chipSpace();
            height = Math.max(height, th + pad * 2);
        }

        private double chipSpace() {
            FlorenceGuiTheme theme = theme();
            double space = theme.textHeight() + theme.space(10);

            if (badge != null) space += theme.textWidth(badge) + theme.space(16);

            return space;
        }

        private double arrowX() {
            FlorenceGuiTheme theme = theme();
            return x + width - theme.space(12) - theme.textHeight() * 0.6;
        }

        private boolean overArrow(double mouseX, double mouseY) {
            double pad = theme().space(6);
            double size = theme().textHeight();

            return id != null && mouseX >= arrowX() - pad - size / 2 && mouseX <= arrowX() + pad + size / 2 && mouseY >= y && mouseY <= y + height;
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            FlorenceGuiTheme theme = theme();
            Design d = design();

            ui.update(overArrow(mouseX, mouseY), false, delta);

            double th = theme.textHeight();
            double centerY = y + height / 2;
            double right = x + width - theme.space(12);

            // Arrow that collapses the window
            if (id != null) {
                turn.animateTo(expanded ? 0 : -90, 0.2);
                double rotation = turn.update(delta);

                chevron(renderer, arrowX(), centerY, th * 0.55, rotation, Math.max(1.5, theme.scale(2)), Colors.lerp(d.textSecondary, d.text, ui.hover()));
                right = arrowX() - th * 0.6 - theme.space(6);
            }

            // How many things are in the window
            if (badge != null) {
                double badgeWidth = theme.textWidth(badge) + theme.space(12);
                double badgeHeight = th * 0.95;

                renderer.roundRect(right - badgeWidth, centerY - badgeHeight / 2, badgeWidth, badgeHeight, badgeHeight / 2, d.field);
                renderer.text(badge, right - badgeWidth + (badgeWidth - theme.textWidth(badge)) / 2, centerY - th / 2, d.textSecondary, false);
            }
        }

        @Override
        public boolean onMouseClicked(Click click, boolean doubled) {
            if (mouseOver && !doubled) {
                if (id != null && (click.button() == GLFW_MOUSE_BUTTON_RIGHT || (click.button() == GLFW_MOUSE_BUTTON_LEFT && overArrow(click.x(), click.y())))) {
                    setExpanded(!expanded);
                    return true;
                }

                WFlorenceWindow.this.startDragging();
                return true;
            }

            return false;
        }

        @Override
        public boolean onMouseReleased(Click click) {
            if (dragging) {
                WFlorenceWindow.this.finishDragging();
                // Releasing only ends the drag, the window opens and closes with the arrow or a right click
            }

            return false;
        }
    }
}
