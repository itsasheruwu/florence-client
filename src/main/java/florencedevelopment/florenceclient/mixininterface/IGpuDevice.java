/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.mixininterface;

import com.mojang.blaze3d.systems.RenderPass;

public interface IGpuDevice {
    /**
     * Currently there can only be a single scissor pushed at once.
     */
    void florence$pushScissor(int x, int y, int width, int height);

    void florence$popScissor();

    /**
     * This is an *INTERNAL* method, it shouldn't be called.
     */
    @Deprecated
    @SuppressWarnings("DeprecatedIsStillUsed")
    void florence$onCreateRenderPass(RenderPass pass);
}
