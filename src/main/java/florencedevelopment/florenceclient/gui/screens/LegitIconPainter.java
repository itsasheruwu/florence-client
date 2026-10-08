/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.screens;

import florencedevelopment.florenceclient.gui.design.Colors;
import florencedevelopment.florenceclient.gui.renderer.GuiRenderer;

/**
 * Draws the icons of the modules in the Legit tab from lines, circles and rounded boxes, so they are sharp at any size
 * and take the colors of the GUI. Every icon is drawn in a square from -0.5 to 0.5 with y pointing down.
 */
public final class LegitIconPainter {
    private final GuiRenderer r;
    private final double cx, cy, size, t;
    private final int color, dim;

    private LegitIconPainter(GuiRenderer renderer, double cx, double cy, double size, int color) {
        this.r = renderer;
        this.cx = cx;
        this.cy = cy;
        this.size = size;
        this.t = Math.max(1.5, size * 0.075);
        this.color = color;
        this.dim = Colors.mulAlpha(color, 0.45);
    }

    /**
     * @return whether the module has an icon, false means nothing was drawn
     */
    public static boolean draw(GuiRenderer renderer, String module, double centerX, double centerY, double size, int color) {
        LegitIconPainter p = new LegitIconPainter(renderer, centerX, centerY, size, color);

        switch (module) {
            case "zoom" -> p.zoom();
            case "fullbright" -> p.sun();
            case "better-tab" -> p.list();
            case "better-tooltips" -> p.tooltip();
            case "better-chat" -> p.chat();
            case "blur" -> p.blur();
            case "boss-stack" -> p.stack();
            case "break-indicators" -> p.crack();
            case "camera-tweaks" -> p.camera();
            case "free-look" -> p.eye();
            case "hand-view" -> p.hand();
            case "item-physics" -> p.falling();
            case "light-overlay" -> p.bulb();
            case "no-render" -> p.forbidden();
            case "time-changer" -> p.clock();
            case "waypoints" -> p.pin();
            case "trajectories" -> p.trajectory();
            case "item-highlight" -> p.sparkle();
            case "name-protect" -> p.shield();
            case "ambience" -> p.cloud();
            case "auto-reconnect" -> p.refresh();
            case "sound-blocker" -> p.speaker();
            case "block-selection" -> p.brackets();
            case "inventory-tweaks" -> p.chest();
            case "notifier" -> p.bell();
            case "breadcrumbs" -> p.crumbs();
            default -> {
                return false;
            }
        }

        return true;
    }

    // Helpers

    private double px(double x) {
        return cx + x * size;
    }

    private double py(double y) {
        return cy + y * size;
    }

    private void line(double x1, double y1, double x2, double y2) {
        r.line(px(x1), py(y1), px(x2), py(y2), t, color);
    }

    private void line(double x1, double y1, double x2, double y2, int c) {
        r.line(px(x1), py(y1), px(x2), py(y2), t, c);
    }

    private void dot(double x, double y, double radius, int c) {
        r.circle(px(x), py(y), radius * size, c);
    }

    private void ring(double x, double y, double radius, int c) {
        double rad = radius * size;
        r.roundRect(px(x) - rad, py(y) - rad, rad * 2, rad * 2, rad, 0, t, c);
    }

    private void box(double x, double y, double w, double h, double radius, int fill, boolean outline) {
        r.roundRect(px(x), py(y), w * size, h * size, radius * size, fill, outline ? t : 0, outline ? color : 0);
    }

    private void box(double x, double y, double w, double h, double radius) {
        box(x, y, w, h, radius, 0, true);
    }

    /** A curve made of short lines, from one angle to another (degrees, 0 is to the right, clockwise). */
    private void arc(double x, double y, double radius, double from, double to) {
        int steps = 14;
        double lastX = 0, lastY = 0;

        for (int i = 0; i <= steps; i++) {
            double a = Math.toRadians(from + (to - from) * i / steps);
            double ax = x + Math.cos(a) * radius;
            double ay = y + Math.sin(a) * radius;

            if (i > 0) line(lastX, lastY, ax, ay);

            lastX = ax;
            lastY = ay;
        }
    }

    // Icons

    private void zoom() {
        ring(-0.1, -0.1, 0.28, color);
        line(0.11, 0.11, 0.4, 0.4);
        line(-0.2, -0.1, 0.0, -0.1, dim);
        line(-0.1, -0.2, -0.1, 0.0, dim);
    }

    private void sun() {
        dot(0, 0, 0.17, color);

        for (int i = 0; i < 8; i++) {
            double a = Math.toRadians(i * 45);
            line(Math.cos(a) * 0.29, Math.sin(a) * 0.29, Math.cos(a) * 0.45, Math.sin(a) * 0.45);
        }
    }

    private void list() {
        for (int i = 0; i < 3; i++) {
            double y = -0.28 + i * 0.28;

            dot(-0.38, y, 0.045, color);
            line(-0.2, y, i == 1 ? 0.3 : 0.42, y, i == 1 ? color : dim);
        }
    }

    private void tooltip() {
        box(-0.44, -0.38, 0.88, 0.56, 0.1);
        line(-0.2, 0.18, -0.3, 0.4);
        line(-0.3, 0.4, 0.0, 0.18);
        line(-0.26, -0.2, 0.26, -0.2, dim);
        line(-0.26, -0.04, 0.1, -0.04, dim);
    }

    private void chat() {
        box(-0.44, -0.36, 0.88, 0.6, 0.16);
        line(-0.22, 0.24, -0.3, 0.42);
        line(-0.3, 0.42, 0.0, 0.24);
        dot(-0.2, -0.06, 0.05, color);
        dot(0, -0.06, 0.05, color);
        dot(0.2, -0.06, 0.05, color);
    }

    private void blur() {
        dot(-0.12, 0, 0.3, Colors.mulAlpha(color, 0.25));
        dot(0.1, 0.02, 0.24, Colors.mulAlpha(color, 0.5));
        dot(0.0, -0.04, 0.15, color);
    }

    private void stack() {
        box(-0.42, -0.34, 0.84, 0.17, 0.08, color, false);
        box(-0.34, -0.08, 0.68, 0.17, 0.08, Colors.mulAlpha(color, 0.65), false);
        box(-0.26, 0.18, 0.52, 0.17, 0.08, Colors.mulAlpha(color, 0.4), false);
    }

    private void crack() {
        box(-0.38, -0.38, 0.76, 0.76, 0.1);
        line(-0.1, -0.38, 0.02, -0.06);
        line(0.02, -0.06, -0.14, 0.1);
        line(-0.14, 0.1, 0.06, 0.38);
    }

    private void camera() {
        box(-0.44, -0.2, 0.88, 0.58, 0.1);
        box(-0.16, -0.34, 0.32, 0.14, 0.05, color, false);
        ring(0, 0.09, 0.17, color);
    }

    private void eye() {
        box(-0.46, -0.22, 0.92, 0.44, 0.22);
        dot(0, 0, 0.12, color);
    }

    private void hand() {
        box(-0.3, -0.04, 0.6, 0.44, 0.12);

        for (int i = 0; i < 4; i++) {
            double x = -0.21 + i * 0.14;
            line(x, -0.04, x, i == 1 || i == 2 ? -0.4 : -0.3);
        }
    }

    private void falling() {
        dot(0, -0.26, 0.14, color);
        line(0, -0.06, 0, 0.34, dim);
        line(-0.16, 0.2, 0, 0.36);
        line(0.16, 0.2, 0, 0.36);
    }

    private void bulb() {
        ring(0, -0.1, 0.26, color);
        line(-0.12, 0.22, 0.12, 0.22);
        line(-0.08, 0.34, 0.08, 0.34);
        line(0, -0.1, 0, 0.1, dim);
    }

    private void forbidden() {
        ring(0, 0, 0.38, color);
        line(-0.27, -0.27, 0.27, 0.27);
    }

    private void clock() {
        ring(0, 0, 0.38, color);
        line(0, 0, 0, -0.22);
        line(0, 0, 0.16, 0.1);
    }

    private void pin() {
        ring(0, -0.14, 0.22, color);
        dot(0, -0.14, 0.06, color);
        line(-0.17, 0.04, 0, 0.4);
        line(0.17, 0.04, 0, 0.4);
    }

    private void trajectory() {
        double lastX = 0, lastY = 0;

        for (int i = 0; i <= 8; i++) {
            double u = i / 8.0;
            double x = -0.4 + 0.8 * u;
            double y = 0.3 - 0.7 * (1 - (2 * u - 1) * (2 * u - 1));

            if (i > 0) line(lastX, lastY, x, y, dim);

            lastX = x;
            lastY = y;
        }

        dot(-0.4, 0.3, 0.07, color);
        dot(0.4, 0.3, 0.1, color);
    }

    private void sparkle() {
        box(-0.4, -0.18, 0.62, 0.58, 0.1);

        // Four pointed star at the top right
        line(0.28, -0.42, 0.28, -0.06);
        line(0.1, -0.24, 0.46, -0.24);
        dot(0.28, -0.24, 0.05, color);
    }

    private void shield() {
        r.roundRect(px(-0.32), py(-0.4), 0.64 * size, 0.78 * size, 0.1 * size, 0.1 * size, 0.34 * size, 0.34 * size, 0, t, color);
        line(-0.14, 0.0, -0.03, 0.12);
        line(-0.03, 0.12, 0.17, -0.12);
    }

    private void cloud() {
        dot(-0.14, -0.02, 0.2, Colors.mulAlpha(color, 0.55));
        dot(0.12, -0.08, 0.26, color);
        box(-0.4, 0.0, 0.8, 0.28, 0.14, color, false);
    }

    private void refresh() {
        arc(0, 0, 0.32, 50, 330);

        // Arrow head at the end of the circle
        double a = Math.toRadians(330);
        double ex = Math.cos(a) * 0.32;
        double ey = Math.sin(a) * 0.32;

        line(ex, ey, ex - 0.02, ey - 0.2);
        line(ex, ey, ex + 0.2, ey - 0.06);
    }

    private void speaker() {
        box(-0.42, -0.12, 0.2, 0.24, 0.03, color, false);
        line(-0.22, -0.12, 0.0, -0.3);
        line(0.0, -0.3, 0.0, 0.3);
        line(0.0, 0.3, -0.22, 0.12);
        line(0.14, -0.14, 0.4, 0.14);
        line(0.14, 0.14, 0.4, -0.14);
    }

    private void brackets() {
        double a = 0.38;
        double l = 0.2;

        for (int sx = -1; sx <= 1; sx += 2) {
            for (int sy = -1; sy <= 1; sy += 2) {
                line(sx * a, sy * a, sx * (a - l), sy * a);
                line(sx * a, sy * a, sx * a, sy * (a - l));
            }
        }

        dot(0, 0, 0.06, dim);
    }

    private void chest() {
        box(-0.42, -0.22, 0.84, 0.62, 0.08);
        line(-0.42, 0.0, 0.42, 0.0);
        box(-0.06, -0.06, 0.12, 0.16, 0.03, color, false);
    }

    private void bell() {
        r.roundRect(px(-0.28), py(-0.34), 0.56 * size, 0.54 * size, 0.28 * size, 0.28 * size, 0.06 * size, 0.06 * size, 0, t, color);
        line(-0.4, 0.2, 0.4, 0.2);
        dot(0, 0.33, 0.07, color);
        dot(0, -0.42, 0.04, color);
    }

    private void crumbs() {
        dot(-0.34, 0.3, 0.07, Colors.mulAlpha(color, 0.35));
        dot(-0.14, 0.1, 0.09, Colors.mulAlpha(color, 0.55));
        dot(0.07, -0.1, 0.11, Colors.mulAlpha(color, 0.8));
        dot(0.3, -0.3, 0.14, color);
    }
}
