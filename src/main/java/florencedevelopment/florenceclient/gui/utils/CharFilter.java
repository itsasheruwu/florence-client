/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.utils;

public interface CharFilter {
    boolean filter(String text, char c);

    default boolean filter(String text, int i) {
        return filter(text, (char) i);
    }
}
