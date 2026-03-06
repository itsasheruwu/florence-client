/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.events.florence;

@SuppressWarnings("InstantiationOfUtilityClass")
public class CustomFontChangedEvent {
    private static final CustomFontChangedEvent INSTANCE = new CustomFontChangedEvent();

    public static CustomFontChangedEvent get() {
        return INSTANCE;
    }
}
