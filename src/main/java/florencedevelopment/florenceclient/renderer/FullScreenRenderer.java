/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.renderer;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import florencedevelopment.florenceclient.utils.PreInit;

public class FullScreenRenderer {
    public static GpuBuffer vbo;
    public static GpuBuffer ibo;

    /**
     * Deprecated for performance reasons, use {@link MeshRenderer#fullscreen()} or the {@link FullScreenRenderer#vbo}
     * and {@link FullScreenRenderer#ibo} buffer objects instead.
     */
    @Deprecated(forRemoval = true)
    public static MeshBuilder mesh;

    private FullScreenRenderer() {}

    @PreInit
    public static void init() {
        mesh = new MeshBuilder(FlorenceVertexFormats.POS2, VertexFormat.DrawMode.TRIANGLES, 4, 6);

        mesh.begin();

        mesh.quad(
            mesh.vec2(-1, -1).next(),
            mesh.vec2(-1, 1).next(),
            mesh.vec2(1, 1).next(),
            mesh.vec2(1, -1).next()
        );

        mesh.end();

        vbo = mesh.getVertexBuffer();
        ibo = mesh.getIndexBuffer();
    }
}
