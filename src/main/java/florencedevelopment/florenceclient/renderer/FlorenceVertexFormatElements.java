/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.renderer;

import com.mojang.blaze3d.vertex.VertexFormatElement;

public abstract class FlorenceVertexFormatElements {
    public static final VertexFormatElement POS2 = VertexFormatElement.register(getNextVertexFormatElementId(), 0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.POSITION, 2);

    // Shape pipeline, see FlorenceVertexFormats.POS2_SHAPE
    public static final VertexFormatElement SHAPE_A = VertexFormatElement.register(getNextVertexFormatElementId(), 0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.GENERIC, 4);
    public static final VertexFormatElement SHAPE_B = VertexFormatElement.register(getNextVertexFormatElementId(), 0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.GENERIC, 4);
    public static final VertexFormatElement COLOR2 = VertexFormatElement.register(getNextVertexFormatElementId(), 0, VertexFormatElement.Type.UBYTE, VertexFormatElement.Usage.COLOR, 4);

    private FlorenceVertexFormatElements() {}

    private static int getNextVertexFormatElementId() {
        int id = 0;

        while (VertexFormatElement.byId(id) != null) {
            id++;

            if (id >= 32) {
                throw new RuntimeException("Too many mods registering VertexFormatElements");
            }
        }

        return id;
    }
}
