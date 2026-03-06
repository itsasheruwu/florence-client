/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.events.world;

import florencedevelopment.florenceclient.events.Cancellable;

public class ChunkOcclusionEvent extends Cancellable {
    private static final ChunkOcclusionEvent INSTANCE = new ChunkOcclusionEvent();

    public static ChunkOcclusionEvent get() {
        INSTANCE.setCancelled(false);
        return INSTANCE;
    }
}
