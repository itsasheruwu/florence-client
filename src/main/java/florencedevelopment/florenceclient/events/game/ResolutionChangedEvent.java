/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.events.game;

public class ResolutionChangedEvent {
    private static final ResolutionChangedEvent INSTANCE = new ResolutionChangedEvent();

    public static ResolutionChangedEvent get() {
        return INSTANCE;
    }
}
