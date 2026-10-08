/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.design;

import java.util.Locale;
import java.util.Map;

/**
 * Gives every module category a color of its own, used for the accents of its window and of the modules in it.
 */
public final class CategoryColors {
    // Hue from 0 to 1 for the categories that come with the client
    private static final Map<String, Double> HUES = Map.of(
        "combat", 0.99,
        "player", 0.36,
        "movement", 0.58,
        "render", 0.78,
        "world", 0.48,
        "misc", 0.09
    );

    private static final double SATURATION = 0.6;
    private static final double VALUE = 0.95;

    private CategoryColors() {}

    /**
     * @return an opaque color, the same every time for the same name
     */
    public static int of(String categoryName) {
        String name = categoryName == null ? "" : categoryName.toLowerCase(Locale.ROOT);

        Double hue = HUES.get(name);

        // Categories added by addons get a hue worked out from the name
        if (hue == null) hue = Math.floorMod(name.hashCode() * 2654435761L, 1000) / 1000.0;

        return Colors.fromHsv(hue, SATURATION, VALUE, 255);
    }
}
