/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.menu;

import florencedevelopment.florenceclient.gui.GuiTheme;
import florencedevelopment.florenceclient.gui.animation.AnimatedFloat;
import florencedevelopment.florenceclient.gui.animation.Spring;
import florencedevelopment.florenceclient.gui.design.Colors;
import florencedevelopment.florenceclient.gui.design.Design;
import florencedevelopment.florenceclient.gui.renderer.GuiRenderer;
import florencedevelopment.florenceclient.gui.themes.florence.FlorenceGuiTheme;
import florencedevelopment.florenceclient.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

/**
 * A menu that pops up at the mouse. A screen owns one, calls {@link #render} from its overlay and passes mouse clicks
 * to {@link #click} while it is open. Positions are in framebuffer pixels like the rest of the GUI.
 */
public class ContextMenu {
    private record Item(String label, Runnable action, boolean danger, BooleanSupplier checked) {
        boolean isSeparator() {
            return label == null;
        }
    }

    private final List<Item> items = new ArrayList<>();
    private final AnimatedFloat enter = new AnimatedFloat(0);

    private boolean open;
    private double x, y;
    private double width, height;
    private int hovered = -1;

    public boolean isOpen() {
        return open;
    }

    // Building

    public ContextMenu clear() {
        items.clear();
        return this;
    }

    public ContextMenu add(String label, Runnable action) {
        items.add(new Item(label, action, false, null));
        return this;
    }

    /** An item that shows a check mark while the supplier returns true. */
    public ContextMenu addToggle(String label, BooleanSupplier checked, Runnable action) {
        items.add(new Item(label, action, false, checked));
        return this;
    }

    /** An item for something that can't be undone, shown in red. */
    public ContextMenu addDanger(String label, Runnable action) {
        items.add(new Item(label, action, true, null));
        return this;
    }

    public ContextMenu addSeparator() {
        items.add(new Item(null, null, false, null));
        return this;
    }

    // Showing

    /**
     * Shows the menu with its corner at the position, moved if needed to keep it on the screen.
     */
    public void open(double x, double y) {
        this.x = x;
        this.y = y;
        this.open = true;
        this.hovered = -1;
        this.width = 0;

        enter.snap(0);
        enter.springTo(1, Spring.SNAPPY);
    }

    public void close() {
        open = false;
    }

    // Input

    /**
     * Handles a mouse press while the menu is open: clicking an item runs it, clicking anywhere closes the menu.
     *
     * @return whether the click was used, which is always the case while the menu is open
     */
    public boolean click(double mouseX, double mouseY, int button) {
        if (!open) return false;

        int index = itemAt(mouseX, mouseY);

        close();

        if (index >= 0 && button == 0) {
            Item item = items.get(index);
            if (!item.isSeparator() && item.action() != null) item.action().run();
        }

        return true;
    }

    private double rowHeight(GuiTheme theme, int index) {
        double base = theme.textHeight() + padding(theme) * 1.1;
        return items.get(index).isSeparator() ? padding(theme) * 0.9 : base;
    }

    private double padding(GuiTheme theme) {
        return theme instanceof FlorenceGuiTheme florence ? florence.space(8) : theme.scale(8);
    }

    private int itemAt(double mouseX, double mouseY) {
        if (mouseX < x || mouseX > x + width || mouseY < y || mouseY > y + height) return -1;

        double rowY = y + inner();
        for (int i = 0; i < items.size(); i++) {
            double h = rowHeightCached[i];

            if (mouseY >= rowY && mouseY < rowY + h) return items.get(i).isSeparator() ? -1 : i;
            rowY += h;
        }

        return -1;
    }

    private double inner;
    private double[] rowHeightCached = new double[0];

    private double inner() {
        return inner;
    }

    // Rendering

    public void render(GuiRenderer renderer, GuiTheme theme, double mouseX, double mouseY, double delta) {
        if (!open && enter.get() <= 0.01) return;
        if (items.isEmpty()) return;

        FlorenceGuiTheme florence = theme instanceof FlorenceGuiTheme f ? f : null;
        Design d = florence != null ? florence.design() : null;

        double th = theme.textHeight();
        double pad = padding(theme);
        inner = pad * 0.5;

        // Size
        double widest = 0;
        boolean anyCheck = false;
        rowHeightCached = new double[items.size()];

        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            rowHeightCached[i] = rowHeight(theme, i);

            if (!item.isSeparator()) widest = Math.max(widest, theme.textWidth(item.label()));
            if (item.checked() != null) anyCheck = true;
        }

        double checkSpace = anyCheck ? th + pad * 0.6 : 0;
        width = widest + pad * 2.4 + checkSpace;

        height = inner * 2;
        for (double h : rowHeightCached) height += h;

        // Keep it on the screen
        double screenW = Utils.getWindowWidth();
        double screenH = Utils.getWindowHeight();
        if (x + width > screenW - 4) x = Math.max(4, screenW - width - 4);
        if (y + height > screenH - 4) y = Math.max(4, screenH - height - 4);

        enter.springTo(open ? 1 : 0, Spring.SNAPPY);
        double amount = Math.max(0, Math.min(1, enter.update(delta)));

        hovered = open ? itemAt(mouseX, mouseY) : -1;

        // Slides down a little while appearing
        double slide = (1 - amount) * -pad * 0.8;
        double top = y + slide;

        renderer.setAlpha(amount);

        double radius = florence != null ? florence.radiusMedium() * 1.2 : theme.scale(6);
        double line = Math.max(1, Math.round(theme.scale(1)));

        if (florence == null || florence.shadows()) {
            renderer.shadow(x, top + theme.scale(4), width, height, radius, theme.scale(18), d != null ? d.shadow : Colors.argb(0, 0, 0, 150));
        }

        if (d != null) renderer.roundRect(x, top, width, height, radius, Colors.withAlpha(d.header, 250), line, d.outlineHover);
        else renderer.roundRect(x, top, width, height, radius, Colors.argb(24, 24, 28, 250), line, Colors.argb(90, 90, 100, 255));

        int text = d != null ? d.text : Colors.rgb(235, 235, 240);
        int secondary = d != null ? d.textSecondary : Colors.rgb(150, 150, 160);
        int danger = d != null ? d.danger : Colors.rgb(248, 113, 113);
        int accent = d != null ? d.accent : Colors.rgb(108, 92, 231);

        double rowY = top + inner;
        double side = pad * 0.5;

        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            double h = rowHeightCached[i];

            if (item.isSeparator()) {
                renderer.roundRect(x + pad, rowY + h / 2 - line / 2, width - pad * 2, line, 0, d != null ? d.divider : Colors.argb(90, 90, 100, 120));
                rowY += h;
                continue;
            }

            boolean hover = i == hovered;

            if (hover) {
                int highlight = item.danger() ? Colors.withAlpha(danger, 46) : (d != null ? d.cardActive : Colors.argb(108, 92, 231, 60));
                renderer.roundRect(x + side * 0.6, rowY, width - side * 1.2, h, radius * 0.6, highlight);
            }

            double textY = rowY + (h - th) / 2;
            int color = item.danger() ? danger : (hover ? text : Colors.lerp(secondary, text, 0.75));

            if (item.checked() != null && item.checked().getAsBoolean()) {
                double t = Math.max(1.5, theme.scale(2));
                double cx = x + side + pad * 0.6 + th * 0.2;
                double cy = rowY + h / 2;

                renderer.line(cx - th * 0.2, cy, cx - th * 0.05, cy + th * 0.15, t, accent);
                renderer.line(cx - th * 0.05, cy + th * 0.15, cx + th * 0.25, cy - th * 0.18, t, accent);
            }

            renderer.text(item.label(), x + side + pad * 0.6 + checkSpace, textY, color, false);

            rowY += h;
        }

        renderer.setAlpha(1);
    }
}
