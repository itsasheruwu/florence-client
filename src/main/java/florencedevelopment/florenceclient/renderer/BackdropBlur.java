/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.renderer;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import florencedevelopment.florenceclient.FlorenceClient;
import florencedevelopment.florenceclient.events.game.ResolutionChangedEvent;
import florencedevelopment.florenceclient.events.render.RenderAfterWorldEvent;
import florencedevelopment.florenceclient.utils.PostInit;
import it.unimi.dsi.fastutil.ints.IntFloatImmutablePair;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.client.gl.DynamicUniformStorage;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;

import static florencedevelopment.florenceclient.FlorenceClient.mc;

/**
 * Blurs the world once per frame (dual Kawase) for everything that wants a frosted glass look: the full screen blur of
 * the Blur module and the glass panels of the GUI. Whoever needs it asks with {@link #request} before the world has
 * finished rendering and the result can then be sampled while the GUI is drawn.
 */
public final class BackdropBlur {
    public static final int MAX_LEVEL = 20;

    // Strength levels from https://github.com/jonaburg/picom/blob/a8445684fe18946604848efb73ace9457b29bf80/src/backend/backend_common.c#L372
    private static final IntFloatImmutablePair[] STRENGTHS = {
        IntFloatImmutablePair.of(1, 1.25f), // LVL 1
        IntFloatImmutablePair.of(1, 2.25f), // LVL 2
        IntFloatImmutablePair.of(2, 2.0f),  // LVL 3
        IntFloatImmutablePair.of(2, 3.0f),  // LVL 4
        IntFloatImmutablePair.of(2, 4.25f), // LVL 5
        IntFloatImmutablePair.of(3, 2.5f),  // LVL 6
        IntFloatImmutablePair.of(3, 3.25f), // LVL 7
        IntFloatImmutablePair.of(3, 4.25f), // LVL 8
        IntFloatImmutablePair.of(3, 5.5f),  // LVL 9
        IntFloatImmutablePair.of(4, 3.25f), // LVL 10
        IntFloatImmutablePair.of(4, 4.0f),  // LVL 11
        IntFloatImmutablePair.of(4, 5.0f),  // LVL 12
        IntFloatImmutablePair.of(4, 6.0f),  // LVL 13
        IntFloatImmutablePair.of(4, 7.25f), // LVL 14
        IntFloatImmutablePair.of(4, 8.25f), // LVL 15
        IntFloatImmutablePair.of(5, 4.5f),  // LVL 16
        IntFloatImmutablePair.of(5, 5.25f), // LVL 17
        IntFloatImmutablePair.of(5, 6.25f), // LVL 18
        IntFloatImmutablePair.of(5, 7.25f), // LVL 19
        IntFloatImmutablePair.of(5, 8.5f)   // LVL 20
    };

    private static final BackdropBlur INSTANCE = new BackdropBlur();

    private static final int UNIFORM_SIZE = new Std140SizeCalculator()
        .putVec2()
        .putFloat()
        .get();

    private final GpuTextureView[] fbos = new GpuTextureView[6];
    private GpuBufferSlice[] ubos;
    private FixedUniformStorage<BlurUniformData> uniformStorage;
    private float previousOffset = -1;
    private boolean fbosDirty;

    private int requestedLevel;
    private boolean requestedFullscreen;
    private boolean available;

    private BackdropBlur() {}

    public static BackdropBlur get() {
        return INSTANCE;
    }

    @PostInit
    public static void init() {
        FlorenceClient.EVENT_BUS.subscribe(INSTANCE);
    }

    /**
     * Asks for the world to be blurred this frame. Has to be called before the world finishes rendering, requests are
     * forgotten after every frame.
     *
     * @param level      blur strength from 1 to {@link #MAX_LEVEL}
     * @param fullscreen whether the blurred world should also replace the scene on the screen
     */
    public void request(int level, boolean fullscreen) {
        requestedLevel = Math.max(requestedLevel, Math.min(level, MAX_LEVEL));
        requestedFullscreen |= fullscreen;
    }

    /**
     * The blurred world, or null if nothing asked for it this frame or there is no world to blur (for example on the
     * title screen).
     */
    public @Nullable GpuTextureView getTexture() {
        return available ? fbos[0] : null;
    }

    @EventHandler
    private void onResolutionChanged(ResolutionChangedEvent event) {
        fbosDirty = true;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onRenderAfterWorld(RenderAfterWorldEvent event) {
        int level = requestedLevel;
        boolean fullscreen = requestedFullscreen;

        requestedLevel = 0;
        requestedFullscreen = false;
        available = false;

        if (level <= 0) return;

        ensureFbos();

        IntFloatImmutablePair strength = STRENGTHS[level - 1];
        int iterations = strength.leftInt();
        float offset = strength.rightFloat();

        if (previousOffset != offset) {
            updateUniforms(offset);
            previousOffset = offset;
        }

        // Initial downsample
        renderToFbo(fbos[0], mc.getFramebuffer().getColorAttachmentView(), FlorenceRenderPipelines.BLUR_DOWN, ubos[0]);

        // Downsample
        for (int i = 0; i < iterations; i++) {
            renderToFbo(fbos[i + 1], fbos[i], FlorenceRenderPipelines.BLUR_DOWN, ubos[i + 1]);
        }

        // Upsample
        for (int i = iterations; i >= 1; i--) {
            renderToFbo(fbos[i - 1], fbos[i], FlorenceRenderPipelines.BLUR_UP, ubos[i - 1]);
        }

        available = true;

        if (fullscreen) {
            MeshRenderer.begin()
                .attachments(mc.getFramebuffer())
                .pipeline(FlorenceRenderPipelines.BLUR_PASSTHROUGH)
                .fullscreen()
                .sampler("u_Texture", fbos[0], RenderSystem.getSamplerCache().get(FilterMode.LINEAR))
                .end();
        }
    }

    private void renderToFbo(GpuTextureView targetFbo, GpuTextureView sourceTexture, RenderPipeline pipeline, GpuBufferSlice ubo) {
        MeshRenderer.begin()
            .attachments(targetFbo, null)
            .pipeline(pipeline)
            .fullscreen()
            .uniform("BlurData", ubo)
            .sampler("u_Texture", sourceTexture, RenderSystem.getSamplerCache().get(FilterMode.LINEAR))
            .end();
    }

    // Framebuffers

    private void ensureFbos() {
        if (fbosDirty) {
            for (int i = 0; i < fbos.length; i++) {
                if (fbos[i] != null) {
                    fbos[i].close();
                    fbos[i] = null;
                }
            }

            // The uniforms contain the size of the framebuffers
            previousOffset = -1;
            fbosDirty = false;
        }

        for (int i = 0; i < fbos.length; i++) {
            if (fbos[i] == null) fbos[i] = createFbo(i);
        }
    }

    private GpuTextureView createFbo(int i) {
        double scale = 1 / Math.pow(2, i);

        int width = Math.max((int) (mc.getWindow().getFramebufferWidth() * scale), 1);
        int height = Math.max((int) (mc.getWindow().getFramebufferHeight() * scale), 1);

        return RenderSystem.getDevice().createTextureView(RenderSystem.getDevice().createTexture("Blur - " + i, 15, TextureFormat.RGBA8, width, height, 1, 1));
    }

    // Uniforms

    private void updateUniforms(float offset) {
        if (uniformStorage == null) uniformStorage = new FixedUniformStorage<>("Florence - Blur UBO", UNIFORM_SIZE, 6);
        uniformStorage.clear();

        BlurUniformData[] uboData = new BlurUniformData[6];

        for (int i = 0; i < uboData.length; i++) {
            GpuTextureView fbo = fbos[i];

            uboData[i] = new BlurUniformData(
                0.5f / fbo.getWidth(0), 0.5f / fbo.getHeight(0),
                offset
            );
        }

        ubos = uniformStorage.writeAll(uboData);
    }

    private record BlurUniformData(float halfTexelSizeX, float halfTexelSizeY, float offset) implements DynamicUniformStorage.Uploadable {
        @Override
        public void write(ByteBuffer buffer) {
            Std140Builder.intoBuffer(buffer)
                .putVec2(halfTexelSizeX, halfTexelSizeY)
                .putFloat(offset);
        }
    }
}
