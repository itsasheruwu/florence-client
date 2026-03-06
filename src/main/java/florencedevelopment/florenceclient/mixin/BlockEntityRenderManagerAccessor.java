/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.mixin;

import net.minecraft.client.render.block.entity.BlockEntityRenderManager;
import net.minecraft.client.texture.SpriteHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockEntityRenderManager.class)
public interface BlockEntityRenderManagerAccessor {
    @Accessor("spriteHolder")
    SpriteHolder getSpriteHolder();
}
