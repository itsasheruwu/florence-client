/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.mixin;

import net.minecraft.client.texture.PlayerSkinProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.nio.file.Path;

@Mixin(PlayerSkinProvider.FileCache.class)
public interface FileCacheAccessor {
    @Accessor("directory")
    Path florence$getDirectory();
}
