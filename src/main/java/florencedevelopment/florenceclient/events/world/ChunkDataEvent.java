/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.events.world;

import net.minecraft.world.chunk.WorldChunk;

/**
 * @implNote Shouldn't be put in a {@link florencedevelopment.florenceclient.utils.misc.Pool} to avoid a race-condition, or in a {@link ThreadLocal} as it is shared between threads.
 * @author Crosby
 */
public record ChunkDataEvent(WorldChunk chunk) {}
