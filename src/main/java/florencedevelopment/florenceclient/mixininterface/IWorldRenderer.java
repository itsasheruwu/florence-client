/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.mixininterface;

import net.minecraft.client.gl.Framebuffer;

public interface IWorldRenderer {
    void florence$pushEntityOutlineFramebuffer(Framebuffer framebuffer);

    void florence$popEntityOutlineFramebuffer();
}
