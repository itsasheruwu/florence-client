/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.mixin;

import florencedevelopment.florenceclient.mixininterface.IText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Language;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MutableText.class)
public abstract class MutableTextMixin implements IText {
    @Shadow
    private @Nullable Language language;

    @Override
    public void florence$invalidateCache() {
        this.language = null;
    }
}
