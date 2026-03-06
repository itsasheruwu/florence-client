/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.events.entity.player;

import florencedevelopment.florenceclient.events.Cancellable;

public class DoAttackEvent extends Cancellable {
    private static final DoAttackEvent INSTANCE = new DoAttackEvent();

    public static DoAttackEvent get() {
        return INSTANCE;
    }
}
