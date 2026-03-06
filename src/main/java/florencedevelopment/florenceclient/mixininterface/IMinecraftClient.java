/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.mixininterface;

import net.minecraft.client.gl.Framebuffer;

public interface IMinecraftClient {
    void florence$rightClick();

    void florence$setFramebuffer(Framebuffer framebuffer);
}
