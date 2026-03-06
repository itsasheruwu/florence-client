/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.renderer;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;

public abstract class FlorenceVertexFormats {
    public static final VertexFormat POS2 = VertexFormat.builder()
        .add("Position", FlorenceVertexFormatElements.POS2)
        .build();

    public static final VertexFormat POS2_COLOR = VertexFormat.builder()
        .add("Position", FlorenceVertexFormatElements.POS2)
        .add("Color", VertexFormatElement.COLOR)
        .build();

    public static final VertexFormat POS2_TEXTURE_COLOR = VertexFormat.builder()
        .add("Position", FlorenceVertexFormatElements.POS2)
        .add("Texture", VertexFormatElement.UV)
        .add("Color", VertexFormatElement.COLOR)
        .build();

    private FlorenceVertexFormats() {}
}
