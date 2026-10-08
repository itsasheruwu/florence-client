/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.design;

/**
 * Helpers for colors packed into an int as 0xAARRGGBB. Using ints in the GUI means no object is allocated for every
 * color that is drawn.
 */
public final class Colors {
    public static final int TRANSPARENT = 0;

    private Colors() {}

    public static int argb(int r, int g, int b, int a) {
        return (clamp(a) << 24) | (clamp(r) << 16) | (clamp(g) << 8) | clamp(b);
    }

    public static int rgb(int r, int g, int b) {
        return argb(r, g, b, 255);
    }

    /**
     * Converts 0xRRGGBB to an opaque color.
     */
    public static int hex(int rgb) {
        return 0xFF000000 | (rgb & 0xFFFFFF);
    }

    public static int alpha(int color) {
        return color >>> 24;
    }

    public static int red(int color) {
        return (color >> 16) & 0xFF;
    }

    public static int green(int color) {
        return (color >> 8) & 0xFF;
    }

    public static int blue(int color) {
        return color & 0xFF;
    }

    public static int withAlpha(int color, int alpha) {
        return (clamp(alpha) << 24) | (color & 0xFFFFFF);
    }

    /**
     * Multiplies the alpha of the color.
     */
    public static int mulAlpha(int color, double factor) {
        return withAlpha(color, (int) Math.round(alpha(color) * factor));
    }

    public static int lerp(int from, int to, double t) {
        if (t <= 0) return from;
        if (t >= 1) return to;

        return argb(
            mix(red(from), red(to), t),
            mix(green(from), green(to), t),
            mix(blue(from), blue(to), t),
            mix(alpha(from), alpha(to), t)
        );
    }

    /**
     * Mixes the color with white, 0 leaves it as is and 1 makes it white.
     */
    public static int lighten(int color, double amount) {
        return lerp(color, withAlpha(0xFFFFFF, alpha(color)), amount);
    }

    /**
     * Mixes the color with black, 0 leaves it as is and 1 makes it black.
     */
    public static int darken(int color, double amount) {
        return lerp(color, withAlpha(0, alpha(color)), amount);
    }

    /**
     * @param hue        0 to 1
     * @param saturation 0 to 1
     * @param value      0 to 1
     */
    public static int fromHsv(double hue, double saturation, double value, int alpha) {
        hue = hue - Math.floor(hue);

        double h = hue * 6;
        int sector = (int) h;
        double f = h - sector;

        double p = value * (1 - saturation);
        double q = value * (1 - saturation * f);
        double t = value * (1 - saturation * (1 - f));

        double r, g, b;

        switch (sector) {
            case 0 -> { r = value; g = t; b = p; }
            case 1 -> { r = q; g = value; b = p; }
            case 2 -> { r = p; g = value; b = t; }
            case 3 -> { r = p; g = q; b = value; }
            case 4 -> { r = t; g = p; b = value; }
            default -> { r = value; g = p; b = q; }
        }

        return argb((int) Math.round(r * 255), (int) Math.round(g * 255), (int) Math.round(b * 255), alpha);
    }

    /**
     * @return the hue, saturation and value of the color, each from 0 to 1
     */
    public static double[] toHsv(int color) {
        double r = red(color) / 255.0;
        double g = green(color) / 255.0;
        double b = blue(color) / 255.0;

        double max = Math.max(r, Math.max(g, b));
        double min = Math.min(r, Math.min(g, b));
        double delta = max - min;

        double hue;

        if (delta == 0) hue = 0;
        else if (max == r) hue = ((g - b) / delta) % 6;
        else if (max == g) hue = (b - r) / delta + 2;
        else hue = (r - g) / delta + 4;

        hue /= 6;
        if (hue < 0) hue += 1;

        return new double[] {hue, max == 0 ? 0 : delta / max, max};
    }

    /**
     * Relative luminance as used by the WCAG contrast ratio, 0 for black and 1 for white.
     */
    public static double luminance(int color) {
        return 0.2126 * linear(red(color)) + 0.7152 * linear(green(color)) + 0.0722 * linear(blue(color));
    }

    /**
     * WCAG contrast ratio between two colors, from 1 (identical) to 21 (black on white).
     */
    public static double contrast(int a, int b) {
        double la = luminance(a);
        double lb = luminance(b);

        return (Math.max(la, lb) + 0.05) / (Math.min(la, lb) + 0.05);
    }

    private static double linear(int channel) {
        double c = channel / 255.0;
        return c <= 0.03928 ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4);
    }

    private static int mix(int from, int to, double t) {
        return (int) Math.round(from + (to - from) * t);
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }
}
