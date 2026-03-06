/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.events.florence;

public class ActiveModulesChangedEvent {
    private static final ActiveModulesChangedEvent INSTANCE = new ActiveModulesChangedEvent();

    public static ActiveModulesChangedEvent get() {
        return INSTANCE;
    }
}
