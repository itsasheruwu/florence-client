/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.mixin;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import florencedevelopment.florenceclient.mixininterface.IRenderPipeline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(RenderPipeline.class)
public abstract class RenderPipelineMixin implements IRenderPipeline {
    @Unique
    private boolean lineSmooth;

    @Override
    public void florence$setLineSmooth(boolean lineSmooth) {
        this.lineSmooth = lineSmooth;
    }

    @Override
    public boolean florence$getLineSmooth() {
        return lineSmooth;
    }
}
