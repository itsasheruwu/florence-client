/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.events.world;

import net.minecraft.block.BlockState;

public class BlockActivateEvent {
    private static final BlockActivateEvent INSTANCE = new BlockActivateEvent();

    public BlockState blockState;

    public static BlockActivateEvent get(BlockState blockState) {
        INSTANCE.blockState = blockState;
        return INSTANCE;
    }
}
