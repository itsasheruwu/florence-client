/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.events.render;

public class RenderAfterWorldEvent {
    private static final RenderAfterWorldEvent INSTANCE = new RenderAfterWorldEvent();

    public static RenderAfterWorldEvent get() {
        return INSTANCE;
    }
}
