/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.renderer;

import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.gl.GpuSampler;

import static florencedevelopment.florenceclient.FlorenceClient.mc;

/**
 * Batches rounded boxes, outlines, shadows, frosted glass and lines into one mesh that is drawn with the signed distance
 * field shader in {@code shaders/ui_shape.frag}. Everything is in framebuffer pixels and colors are packed as 0xAARRGGBB.
 */
public class ShapeRenderer {
    // Keep in sync with the shader
    private static final double MODE_BOX = 0;
    private static final double MODE_SHADOW = 1;
    private static final double MODE_GLASS = 2;
    private static final double MODE_LINE = 3;

    // Extra room around a shape so its anti-aliased edge isn't cut off
    private static final double EDGE = 1.5;

    private final MeshBuilder mesh = new MeshBuilder(FlorenceRenderPipelines.UI_SHAPE);

    public void setAlpha(double alpha) {
        mesh.alpha = alpha;
    }

    public void begin() {
        mesh.begin();
    }

    public void end() {
        if (mesh.isBuilding()) mesh.end();
    }

    /**
     * Draws the batch. The backdrop is only looked at by glass shapes but the shader always needs a texture bound.
     */
    public void render(GpuTextureView backdrop, GpuSampler sampler) {
        end();

        MeshRenderer.begin()
            .attachments(mc.getFramebuffer())
            .pipeline(FlorenceRenderPipelines.UI_SHAPE)
            .mesh(mesh)
            .sampler("u_Backdrop", backdrop, sampler)
            .end();
    }

    // Boxes

    /**
     * A box with a fill color for each corner (blended across the box), a radius for each corner and an optional border
     * drawn inside its edge.
     */
    public void box(double x, double y, double w, double h,
                    double rTopLeft, double rTopRight, double rBottomRight, double rBottomLeft,
                    int cTopLeft, int cTopRight, int cBottomRight, int cBottomLeft,
                    double borderWidth, int borderColor) {
        shape(MODE_BOX, x, y, w, h, borderWidth, EDGE, rTopLeft, rTopRight, rBottomRight, rBottomLeft, cTopLeft, cTopRight, cBottomRight, cBottomLeft, borderColor);
    }

    /**
     * Like {@link #box} but the fill is a tint over the blurred scene behind it.
     */
    public void glass(double x, double y, double w, double h,
                      double rTopLeft, double rTopRight, double rBottomRight, double rBottomLeft,
                      int tint, double borderWidth, int borderColor) {
        shape(MODE_GLASS, x, y, w, h, borderWidth, EDGE, rTopLeft, rTopRight, rBottomRight, rBottomLeft, tint, tint, tint, tint, borderColor);
    }

    /**
     * A soft shadow of a rounded box, to be drawn below it. {@code blur} is the distance over which it fades out.
     */
    public void shadow(double x, double y, double w, double h, double radius, double blur, int color) {
        double sigma = Math.max(blur / 2, 0.5);

        shape(MODE_SHADOW, x, y, w, h, sigma, sigma * 3 + EDGE, radius, radius, radius, radius, color, color, color, color, 0);
    }

    /**
     * A line with round ends.
     */
    public void line(double x1, double y1, double x2, double y2, double thickness, int color) {
        mesh.ensureQuadCapacity();

        double cx = (x1 + x2) / 2;
        double cy = (y1 + y2) / 2;
        double hx = (x2 - x1) / 2;
        double hy = (y2 - y1) / 2;

        double padX = Math.abs(hx) + thickness / 2 + EDGE;
        double padY = Math.abs(hy) + thickness / 2 + EDGE;

        quad(cx, cy, padX, padY, hx, hy, thickness, MODE_LINE, 0, 0, 0, 0, color, color, color, color, 0);
    }

    private void shape(double mode, double x, double y, double w, double h, double extra, double pad,
                       double rTopLeft, double rTopRight, double rBottomRight, double rBottomLeft,
                       int cTopLeft, int cTopRight, int cBottomRight, int cBottomLeft, int borderColor) {
        if (w <= 0 || h <= 0) return;

        mesh.ensureQuadCapacity();

        double halfW = w / 2;
        double halfH = h / 2;

        quad(x + halfW, y + halfH, halfW + pad, halfH + pad, halfW, halfH, extra, mode, rTopLeft, rTopRight, rBottomRight, rBottomLeft, cTopLeft, cTopRight, cBottomRight, cBottomLeft, borderColor);
    }

    /**
     * Writes a quad centered on (cx, cy) that covers a shape. The shader works out the shape from the local position
     * of each pixel inside the quad.
     */
    private void quad(double cx, double cy, double quadHalfW, double quadHalfH,
                      double shapeHalfW, double shapeHalfH, double extra, double mode,
                      double rTopLeft, double rTopRight, double rBottomRight, double rBottomLeft,
                      int cTopLeft, int cTopRight, int cBottomRight, int cBottomLeft, int borderColor) {
        // Same vertex order as the other 2D meshes: top left, bottom left, bottom right, top right
        mesh.quad(
            vertex(cx, cy, -quadHalfW, -quadHalfH, shapeHalfW, shapeHalfH, extra, mode, rTopLeft, rTopRight, rBottomRight, rBottomLeft, cTopLeft, borderColor),
            vertex(cx, cy, -quadHalfW, quadHalfH, shapeHalfW, shapeHalfH, extra, mode, rTopLeft, rTopRight, rBottomRight, rBottomLeft, cBottomLeft, borderColor),
            vertex(cx, cy, quadHalfW, quadHalfH, shapeHalfW, shapeHalfH, extra, mode, rTopLeft, rTopRight, rBottomRight, rBottomLeft, cBottomRight, borderColor),
            vertex(cx, cy, quadHalfW, -quadHalfH, shapeHalfW, shapeHalfH, extra, mode, rTopLeft, rTopRight, rBottomRight, rBottomLeft, cTopRight, borderColor)
        );
    }

    private int vertex(double cx, double cy, double localX, double localY,
                       double shapeHalfW, double shapeHalfH, double extra, double mode,
                       double rTopLeft, double rTopRight, double rBottomRight, double rBottomLeft,
                       int fill, int borderColor) {
        return mesh
            .vec2(cx + localX, cy + localY)
            .vec2(localX, localY)
            .vec4(shapeHalfW, shapeHalfH, extra, mode)
            .vec4(rTopLeft, rTopRight, rBottomRight, rBottomLeft)
            .argb(fill)
            .argb(borderColor)
            .next();
    }
}
