/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.events.render;

import florencedevelopment.florenceclient.events.Cancellable;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;

public class RenderBlockEntityEvent extends Cancellable {
    private static final RenderBlockEntityEvent INSTANCE = new RenderBlockEntityEvent();

    public BlockEntityRenderState blockEntityState;

    public static RenderBlockEntityEvent get(BlockEntityRenderState blockEntityState) {
        INSTANCE.setCancelled(false);
        INSTANCE.blockEntityState = blockEntityState;
        return INSTANCE;
    }
}
