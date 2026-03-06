/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.renderer;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import florencedevelopment.florenceclient.FlorenceClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.resource.ResourceManager;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public abstract class FlorenceRenderPipelines {
    private static final List<RenderPipeline> PIPELINES = new ArrayList<>();

    // Snippets

    private static final RenderPipeline.Snippet MESH_UNIFORMS = RenderPipeline.builder()
        .withUniform("MeshData", UniformType.UNIFORM_BUFFER)
        .buildSnippet();

    // World

    public static final RenderPipeline WORLD_COLORED = add(new ExtendedRenderPipelineBuilder(MESH_UNIFORMS)
        .withLocation(FlorenceClient.identifier("pipeline/world_colored"))
        .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.TRIANGLES)
        .withVertexShader(FlorenceClient.identifier("shaders/pos_color.vert"))
        .withFragmentShader(FlorenceClient.identifier("shaders/pos_color.frag"))
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
        .withDepthWrite(false)
        .withBlend(BlendFunction.TRANSLUCENT)
        .withCull(false)
        .build()
    );

    public static final RenderPipeline WORLD_COLORED_LINES = add(new ExtendedRenderPipelineBuilder(MESH_UNIFORMS)
        .withLineSmooth()
        .withLocation(FlorenceClient.identifier("pipeline/world_colored_lines"))
        .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.DEBUG_LINES)
        .withVertexShader(FlorenceClient.identifier("shaders/pos_color.vert"))
        .withFragmentShader(FlorenceClient.identifier("shaders/pos_color.frag"))
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
        .withDepthWrite(false)
        .withBlend(BlendFunction.TRANSLUCENT)
        .withCull(false)
        .build()
    );

    public static final RenderPipeline WORLD_COLORED_DEPTH = add(new ExtendedRenderPipelineBuilder(MESH_UNIFORMS)
        .withLocation(FlorenceClient.identifier("pipeline/world_colored_depth"))
        .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.TRIANGLES)
        .withVertexShader(FlorenceClient.identifier("shaders/pos_color.vert"))
        .withFragmentShader(FlorenceClient.identifier("shaders/pos_color.frag"))
        .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
        .withDepthWrite(false)
        .withBlend(BlendFunction.TRANSLUCENT)
        .withCull(false)
        .build()
    );

    public static final RenderPipeline WORLD_COLORED_LINES_DEPTH = add(new ExtendedRenderPipelineBuilder(MESH_UNIFORMS)
        .withLineSmooth()
        .withLocation(FlorenceClient.identifier("pipeline/world_colored_lines_depth"))
        .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.DEBUG_LINES)
        .withVertexShader(FlorenceClient.identifier("shaders/pos_color.vert"))
        .withFragmentShader(FlorenceClient.identifier("shaders/pos_color.frag"))
        .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
        .withDepthWrite(false)
        .withBlend(BlendFunction.TRANSLUCENT)
        .withCull(false)
        .build()
    );

    // UI

    public static final RenderPipeline UI_COLORED = add(new ExtendedRenderPipelineBuilder(MESH_UNIFORMS)
        .withLocation(FlorenceClient.identifier("pipeline/ui_colored"))
        .withVertexFormat(FlorenceVertexFormats.POS2_COLOR, VertexFormat.DrawMode.TRIANGLES)
        .withVertexShader(FlorenceClient.identifier("shaders/pos_color.vert"))
        .withFragmentShader(FlorenceClient.identifier("shaders/pos_color.frag"))
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
        .withDepthWrite(false)
        .withBlend(BlendFunction.TRANSLUCENT)
        .withCull(true)
        .build()
    );

    public static final RenderPipeline UI_COLORED_LINES = add(new ExtendedRenderPipelineBuilder(MESH_UNIFORMS)
        .withLocation(FlorenceClient.identifier("pipeline/ui_colored_lines"))
        .withVertexFormat(FlorenceVertexFormats.POS2_COLOR, VertexFormat.DrawMode.DEBUG_LINES)
        .withVertexShader(FlorenceClient.identifier("shaders/pos_color.vert"))
        .withFragmentShader(FlorenceClient.identifier("shaders/pos_color.frag"))
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
        .withDepthWrite(false)
        .withBlend(BlendFunction.TRANSLUCENT)
        .withCull(true)
        .build()
    );

    public static final RenderPipeline UI_TEXTURED = add(new ExtendedRenderPipelineBuilder(MESH_UNIFORMS)
        .withLocation(FlorenceClient.identifier("pipeline/ui_textured"))
        .withVertexFormat(FlorenceVertexFormats.POS2_TEXTURE_COLOR, VertexFormat.DrawMode.TRIANGLES)
        .withVertexShader(FlorenceClient.identifier("shaders/pos_tex_color.vert"))
        .withFragmentShader(FlorenceClient.identifier("shaders/pos_tex_color.frag"))
        .withSampler("u_Texture")
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
        .withDepthWrite(false)
        .withBlend(BlendFunction.TRANSLUCENT)
        .withCull(true)
        .build()
    );

    public static final RenderPipeline UI_TEXT = add(new ExtendedRenderPipelineBuilder(MESH_UNIFORMS)
        .withLocation(FlorenceClient.identifier("pipeline/ui_text"))
        .withVertexFormat(FlorenceVertexFormats.POS2_TEXTURE_COLOR, VertexFormat.DrawMode.TRIANGLES)
        .withVertexShader(FlorenceClient.identifier("shaders/text.vert"))
        .withFragmentShader(FlorenceClient.identifier("shaders/text.frag"))
        .withSampler("u_Texture")
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
        .withDepthWrite(false)
        .withBlend(BlendFunction.TRANSLUCENT)
        .withCull(true)
        .build()
    );

    // Post Process

    public static final RenderPipeline POST_OUTLINE = add(new ExtendedRenderPipelineBuilder()
        .withLocation(FlorenceClient.identifier("pipeline/post/outline"))
        .withVertexFormat(FlorenceVertexFormats.POS2, VertexFormat.DrawMode.TRIANGLES)
        .withVertexShader(FlorenceClient.identifier("shaders/post-process/base.vert"))
        .withFragmentShader(FlorenceClient.identifier("shaders/post-process/outline.frag"))
        .withSampler("u_Texture")
        .withUniform("PostData", UniformType.UNIFORM_BUFFER)
        .withUniform("OutlineData", UniformType.UNIFORM_BUFFER)
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
        .withDepthWrite(false)
        .withBlend(BlendFunction.TRANSLUCENT)
        .withCull(false)
        .build()
    );

    public static final RenderPipeline POST_IMAGE = add(new ExtendedRenderPipelineBuilder(MESH_UNIFORMS)
        .withLocation(FlorenceClient.identifier("pipeline/post/image"))
        .withVertexFormat(FlorenceVertexFormats.POS2, VertexFormat.DrawMode.TRIANGLES)
        .withVertexShader(FlorenceClient.identifier("shaders/post-process/base.vert"))
        .withFragmentShader(FlorenceClient.identifier("shaders/post-process/image.frag"))
        .withSampler("u_Texture")
        .withSampler("u_TextureI")
        .withUniform("PostData", UniformType.UNIFORM_BUFFER)
        .withUniform("ImageData", UniformType.UNIFORM_BUFFER)
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
        .withDepthWrite(false)
        .withBlend(BlendFunction.TRANSLUCENT)
        .withCull(false)
        .build()
    );

    // Blur

    public static final RenderPipeline BLUR_DOWN = add(new ExtendedRenderPipelineBuilder(MESH_UNIFORMS)
        .withLocation(FlorenceClient.identifier("pipeline/blur/down"))
        .withVertexFormat(FlorenceVertexFormats.POS2, VertexFormat.DrawMode.TRIANGLES)
        .withVertexShader(FlorenceClient.identifier("shaders/blur.vert"))
        .withFragmentShader(FlorenceClient.identifier("shaders/blur_down.frag"))
        .withSampler("u_Texture")
        .withUniform("BlurData", UniformType.UNIFORM_BUFFER)
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
        .withDepthWrite(false)
        .withBlend(BlendFunction.TRANSLUCENT)
        .withCull(false)
        .build()
    );

    public static final RenderPipeline BLUR_UP = add(new ExtendedRenderPipelineBuilder(MESH_UNIFORMS)
        .withLocation(FlorenceClient.identifier("pipeline/blur/up"))
        .withVertexFormat(FlorenceVertexFormats.POS2, VertexFormat.DrawMode.TRIANGLES)
        .withVertexShader(FlorenceClient.identifier("shaders/blur.vert"))
        .withFragmentShader(FlorenceClient.identifier("shaders/blur_up.frag"))
        .withSampler("u_Texture")
        .withUniform("BlurData", UniformType.UNIFORM_BUFFER)
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
        .withDepthWrite(false)
        .withBlend(BlendFunction.TRANSLUCENT)
        .withCull(false)
        .build()
    );

    public static final RenderPipeline BLUR_PASSTHROUGH = add(new ExtendedRenderPipelineBuilder(MESH_UNIFORMS)
        .withLocation(FlorenceClient.identifier("pipeline/blur/up"))
        .withVertexFormat(FlorenceVertexFormats.POS2, VertexFormat.DrawMode.TRIANGLES)
        .withVertexShader(FlorenceClient.identifier("shaders/passthrough.vert"))
        .withFragmentShader(FlorenceClient.identifier("shaders/passthrough.frag"))
        .withSampler("u_Texture")
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
        .withDepthWrite(false)
        .withBlend(BlendFunction.TRANSLUCENT)
        .withCull(false)
        .build()
    );

    private static RenderPipeline add(RenderPipeline pipeline) {
        PIPELINES.add(pipeline);
        return pipeline;
    }

    public static void precompile() {
        GpuDevice device = RenderSystem.getDevice();
        ResourceManager resources = MinecraftClient.getInstance().getResourceManager();

        for (RenderPipeline pipeline : PIPELINES) {
            device.precompilePipeline(pipeline, (identifier, shaderType) -> {
                var resource = resources.getResource(identifier).get();

                try (var in = resource.getInputStream()) {
                    return IOUtils.toString(in, StandardCharsets.UTF_8);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    private FlorenceRenderPipelines() {}
}
