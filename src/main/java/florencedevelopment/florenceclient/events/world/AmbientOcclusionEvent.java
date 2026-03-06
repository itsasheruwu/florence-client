/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.events.world;

public class AmbientOcclusionEvent {
    private static final AmbientOcclusionEvent INSTANCE = new AmbientOcclusionEvent();

    public float lightLevel = -1;

    public static AmbientOcclusionEvent get() {
        INSTANCE.lightLevel = -1;
        return INSTANCE;
    }
}
