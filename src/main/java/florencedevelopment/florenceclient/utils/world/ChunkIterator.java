/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.utils.world;

import florencedevelopment.florenceclient.mixin.ClientChunkManagerAccessor;
import florencedevelopment.florenceclient.mixin.ClientChunkMapAccessor;
import net.minecraft.world.chunk.Chunk;

import java.util.Iterator;

import static florencedevelopment.florenceclient.FlorenceClient.mc;

public class ChunkIterator implements Iterator<Chunk> {
    private final ClientChunkMapAccessor map = (ClientChunkMapAccessor) (Object) ((ClientChunkManagerAccessor) mc.world.getChunkManager()).florence$getChunks();
    private final boolean onlyWithLoadedNeighbours;

    private int i = 0;
    private Chunk chunk;

    public ChunkIterator(boolean onlyWithLoadedNeighbours) {
        this.onlyWithLoadedNeighbours = onlyWithLoadedNeighbours;

        getNext();
    }

    private Chunk getNext() {
        Chunk prev = chunk;
        chunk = null;

        while (i < map.florence$getChunks().length()) {
            chunk = map.florence$getChunks().get(i++);
            if (chunk != null && (!onlyWithLoadedNeighbours || isInRadius(chunk))) break;
        }

        return prev;
    }

    private boolean isInRadius(Chunk chunk) {
        int x = chunk.getPos().x;
        int z = chunk.getPos().z;

        return mc.world.getChunkManager().isChunkLoaded(x + 1, z) && mc.world.getChunkManager().isChunkLoaded(x - 1, z) && mc.world.getChunkManager().isChunkLoaded(x, z + 1) && mc.world.getChunkManager().isChunkLoaded(x, z - 1);
    }

    @Override
    public boolean hasNext() {
        return chunk != null;
    }

    @Override
    public Chunk next() {
        return getNext();
    }
}
