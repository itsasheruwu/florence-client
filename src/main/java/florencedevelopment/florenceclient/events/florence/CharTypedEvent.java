/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.events.florence;

import florencedevelopment.florenceclient.events.Cancellable;

public class CharTypedEvent extends Cancellable {
    private static final CharTypedEvent INSTANCE = new CharTypedEvent();

    public char c;

    public static CharTypedEvent get(char c) {
        INSTANCE.setCancelled(false);
        INSTANCE.c = c;
        return INSTANCE;
    }
}
